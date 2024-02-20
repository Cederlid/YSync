package dev.ytterate.ysync;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileComparison {


    List<String> compareAndCopyFiles(File sourceDir, File destDir, File sourceRoot, File destRoot) throws IOException {
        List<String> errors = new ArrayList<>();

        if (sourceDir != null && destDir != null) {
            for (File sourceFile : sourceDir.listFiles()) {
                boolean found = false;

                // Optional<File> filtered = Arrays.stream(destDir.listFiles()).filter(f -> f.getName().equals(sourceFile.getName())).findFirst();
                for (File destFile : destDir.listFiles()) {
                    MisMatchAction misMatchDestAction = new MisMatchAction(destFile,destDir);
                    MisMatchAction misMatchSourceAction = new MisMatchAction(sourceFile,destDir);

                    if (sourceFile.getName().equals(destFile.getName())) {
                        found = true;
                        if (sourceFile.isDirectory() || destFile.isDirectory()) {
                            if (destFile.isFile()) {
                                misMatchDestAction.run();
                            } else if (sourceFile.isFile()) {
                                misMatchSourceAction.run();
                            } else {
                                List<String> subDirErrors = compareAndCopyFiles(sourceFile, destFile, sourceRoot, destRoot);
                                errors.addAll(subDirErrors);
                            }
                        } else if (sourceFile.lastModified() > destFile.lastModified()) {
                            copyFile(sourceFile, destDir);
                        }
                        break;
                    }
                }
                if (!found) {
                    if (getInYsync(destDir, sourceFile.getName()) != null) {

                        long fileTimeInSource = getInYsync(sourceDir, sourceFile.getName()).getLong("lastModified");
                        long fileTimeInDestination = getInYsync(destDir, sourceFile.getName()).getLong("lastModified");

                        if (fileTimeInDestination > fileTimeInSource) {
                            deleteInSource(sourceFile);
                        }
                    } else {
                        copyNewSourceToDest(sourceFile, destDir);
                    }
                }
            }
            for (File destFile : destDir.listFiles()) {
                updateSyncFile(destDir, destFile.getName(), destFile.lastModified());
            }
        }
        return errors;
    }


    private void copyNewSourceToDest(File notSyncedSource, File destDir) throws IOException {
        assert destDir.isDirectory();
        if (notSyncedSource.isDirectory()) {
            File targetDirectory = new File(destDir, notSyncedSource.getName());

            recursivelyUpdateSyncFiles(notSyncedSource);
            copyDirectory(notSyncedSource, targetDirectory);
            recursivelyUpdateSyncFiles(targetDirectory);
        } else {
            copyFile(notSyncedSource, destDir);
        }
    }

    private void recursivelyUpdateSyncFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                recursivelyUpdateSyncFiles(file);
            }
            updateSyncFile(directory, file.getName(), file.lastModified());
        }
    }

    void copyFile(File sourceFile, File destDir) throws IOException {
        Path sourcePath = sourceFile.toPath();
        Path pathToFolder = destDir.toPath().resolve(sourcePath.getFileName());
        if (sourceFile.getName().equals(".ysync")) {
            return;
        }
        Files.copy(sourcePath, pathToFolder, StandardCopyOption.REPLACE_EXISTING);
        File destFile = new File(destDir, sourceFile.getName());
        destFile.setLastModified(sourceFile.lastModified());
    }

    void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            boolean isCreated = destDir.mkdir();
        }
        for (File f : sourceDir.listFiles()) {
            if (f.isDirectory()) {
                copyDirectory(f, new File(destDir, f.getName()));
            } else {
                copyFile(f, destDir);
            }
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

    void deleteInSource(File sourceFile) {
        File [] contents = sourceFile.listFiles();
        if (contents != null){
            for (File f : contents){
                if (! Files.isSymbolicLink(f.toPath())){
                    deleteInSource(f);
                }
            }
        }
        sourceFile.delete();
    }


    //TODO inefficient to write sync file for ever row
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
                syncFile.createNewFile();
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

}
