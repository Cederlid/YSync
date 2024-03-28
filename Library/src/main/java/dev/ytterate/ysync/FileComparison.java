package dev.ytterate.ysync;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class FileComparison {
    LinkedList<SyncAction> syncActions = new LinkedList<>();
    private final File sourceRoot;
    private final File destRoot;
    private final ContinueCallback continueCallback;
    private final List<String> filesToCopy;
    private final List<String> ignoredFiles;
    private final CompletableFuture<Void> copyCompleteFuture = new CompletableFuture<>();

    public FileComparison(File sourceDir, File destDir, ContinueCallback continueCallback, List<String> fileToCopy, List<String> ignoredFiles) {
        this.sourceRoot = sourceDir;
        this.destRoot = destDir;
        this.continueCallback = continueCallback;
        this.filesToCopy = fileToCopy;
        this.ignoredFiles = ignoredFiles;
    }

    public CompletableFuture<Void> compareAndCopyFiles() throws IOException {
        List<String> emptyList = new ArrayList<>();
        return compareAndCopyFiles(emptyList, emptyList);
    }

    public CompletableFuture<Void> compareAndCopyFiles(List<String> copyList, List<String> ignoreList) throws IOException {
        boolean hasMismatches = compareAndCopyRecursively(copyList, ignoreList, sourceRoot, destRoot);
        MisMatchAction misMatchAction = new MisMatchAction(sourceRoot.getPath(), destRoot.getPath());
        if (hasMismatches){
            misMatchAction.confirm();
            CompletableFuture<Boolean> gotMisMatchesFuture = continueCallback.onGotMisMatches(syncActions);
            gotMisMatchesFuture.thenApply(result -> {
                try {
                    onResolvedMisMatches();
                    return null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw t;
                }
            });
        }
        return copyCompleteFuture;
    }

    private Boolean compareAndCopyRecursively(List<String> copyList, List<String> ignoreList, File sourceDir, File destDir) {
        boolean hasMismatches = false;
        if (sourceDir != null && destDir != null) {
            for (File sourceFile : sourceDir.listFiles()) {
                String relativeSourceFilePath = sourceFile.getPath().substring(sourceRoot.getPath().length() + 1);

                if (sourceFile.getName().equals(".ysync")) {
                    continue;
                }

                File destFile = tryGetFile(destDir, sourceFile);
                if (destFile == null) {
                    if (getInYsync(destDir, sourceFile.getName()) == null) {
                        copyNewSourceToDest(sourceFile, destDir);
                    }
                } else {
                    if (sourceFile.isDirectory() || destFile.isDirectory()) {
                        if (destFile.isFile() || sourceFile.isFile()) {
                            if (ignoreList.contains(relativeSourceFilePath)) {

                            } else if (copyList.contains(relativeSourceFilePath)) {
                                copyNewSourceToDest(sourceFile, destDir);
                            } else {
                                MisMatchAction misMatchSourceAction = new MisMatchAction(sourceFile.getPath(), destFile.getPath());
                                syncActions.add(misMatchSourceAction);
                                misMatchSourceAction.isMisMatch();
                                hasMismatches = true;
                            }
                        } else {
                            hasMismatches |= compareAndCopyRecursively(copyList, ignoreList, sourceFile, destFile);
                        }
                    } if (sourceFile.lastModified() > destFile.lastModified()) {
                        CopyFileAction copyFileAction = new CopyFileAction(sourceFile.getPath(), destDir.getPath());
                        syncActions.add(copyFileAction);
                    }
                }

            }
            for (File destFile : destDir.listFiles()) {
                if (tryGetFile(sourceDir, destFile) == null) {
                    if (getInYsync(sourceDir, destFile.getName()) != null) {
                        long fileTimeInDestination = getInYsync(destDir, destFile.getName()).getLong("lastModified");
                        long fileTimeInSource = getInYsync(sourceDir, destFile.getName()).getLong("lastModified");

                        if (fileTimeInDestination < fileTimeInSource) {
                            DeleteFileAction deleteFileAction = new DeleteFileAction(destFile.getPath());
                            syncActions.add(deleteFileAction);
                        }
                    }
                }
            }
        }

        return hasMismatches;
    }

    public void runActions() throws IOException {
        for (SyncAction action : syncActions) {
            action.run();
        }
    }

    public void clearActions() {
        syncActions.clear();
    }

    File tryGetFile(File destDir, File sourceFile) {
        String name = sourceFile.getName();
        for (File destFile : destDir.listFiles()) {
            if (name.equals(destFile.getName())) {
                return destFile;
            }
        }
        return null;
    }

    void recursivelyUpdateSyncFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                recursivelyUpdateSyncFiles(file);
            }
            updateSyncFile(directory, file.getName(), file.lastModified());
        }
    }

    private void copyNewSourceToDest(File notSyncedSource, File destDir) {
        assert destDir.isDirectory();
        if (notSyncedSource.isDirectory()) {
            CopyDirectoryAction copyDirectoryAction = new CopyDirectoryAction(notSyncedSource.getPath(), destDir.getPath());
            syncActions.add(copyDirectoryAction);
        } else {
            CopyFileAction copyFileAction = new CopyFileAction(notSyncedSource.getPath(), destDir.getPath());
            syncActions.add(copyFileAction);
        }
    }

    JSONObject getInYsync(File directory, String fileName) {

        JSONArray filesArray = readSyncFile(directory);

        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject jsonObject = filesArray.getJSONObject(i);
            if (jsonObject.getString("name").equals(fileName)) {
                return jsonObject;
            }
        }
        return null;
    }


    //TODO inefficient destDir write sync sourceFile for ever row
    void updateSyncFile(File destDir, String fileName, long lastModified) {
        if (fileName.equals(".ysync")) {
            return;
        }
        JSONArray filesArray = readSyncFile(destDir);

        createJsonObjectInArray(filesArray, fileName, lastModified);
        writeSyncFile(destDir, filesArray);
    }

    JSONArray readSyncFile(File directory) {
        File syncFile = new File(directory, ".ysync");
        JSONArray filesArray = null;

        try {
            if (syncFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(syncFile);
                JSONTokener jsonTokener = new JSONTokener(fileInputStream);
                filesArray = new JSONArray(jsonTokener);
            } else {
                filesArray = new JSONArray();
                boolean newFile = false;
                try {
                    newFile = syncFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assert newFile;
                writeSyncFile(directory, filesArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filesArray;
    }

    void createJsonObjectInArray(JSONArray filesArray, String fileName, long lastModified) {
        boolean found = false;
        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject jsonObject = filesArray.getJSONObject(i);
            if (jsonObject.getString("name").equals(fileName)) {
                jsonObject.put("lastModified", (lastModified));
                found = true;
                break;
            }
        }

        if (!found) {
            JSONObject newFileObj = new JSONObject();
            newFileObj.put("name", fileName);
            newFileObj.put("lastModified", (lastModified));
            filesArray.put(newFileObj);
        }
    }

    void writeSyncFile(File directory, JSONArray filesArray) {
        File syncFile = new File(directory, ".ysync");
        try (FileWriter writer = new FileWriter(syncFile)) {
            writer.write(filesArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onResolvedMisMatches() throws IOException {
        runActions();
        clearActions();
        recursivelyUpdateSyncFiles(destRoot);
        copyCompleteFuture.complete(null);
    }
}
