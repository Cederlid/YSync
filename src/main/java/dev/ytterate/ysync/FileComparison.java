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
import java.util.Date;

public class FileComparison {
    void compareAndCopyFiles(File sourceDir, File destDir) throws IOException { //TODO add a list of errors and make this methods return it
        if (sourceDir != null && destDir != null) {
            for (File sourceFile : sourceDir.listFiles()) {

                boolean found = false;

                // Optional<File> filtered = Arrays.stream(destDir.listFiles()).filter(f -> f.getName().equals(sourceFile.getName())).findFirst();
                for (File destFile : destDir.listFiles()) {
                    if (sourceFile.getName().equals(destFile.getName())) {
                        found = true;
                        if (sourceFile.isDirectory() || destFile.isDirectory()) {//TODO add a check inside if it's a directory, to check if the destFile i also a directory (if it is a directory continue copy, if it isn't show a popup with text "this files are ignored"
                           if (sourceFile.isDirectory() && destFile.isDirectory()){
                               compareAndCopyFiles(sourceFile, destFile);
                           }else {
                               System.out.printf("Can't copy, %s is a directory and %s is a file!\n",sourceFile.getName(),destFile.getName());
                           }
                        } else if (sourceFile.lastModified() > destFile.lastModified()) {//TODO add a elseif before this to check if destFile isDirectory and make the same error.
                            copyFile(sourceFile, destDir);
                        }
                        break;
                    }
                }
                if (!found) {
                    if (sourceFile.isDirectory()) { //TODO move the code inside the if-statement to a method
                        File destFile = new File(destDir, sourceFile.getName());

                        recursiveSyncAndUpdate(sourceFile);
                        // FileUtils.copyDirectory(sourceFile, destFile);
                        copyDirectory(sourceFile, destFile);
                    } else {
                        copyFile(sourceFile, destDir);
                    }
                }

            }
        }
        for (File destFile : destDir.listFiles()) {
            updateAndSyncFile(destDir, destFile.getName(), destFile.lastModified());
        }
    }
    //Todo resolve the bug why its not creating .ysync in subdirectory in destDir
    private void recursiveSyncAndUpdate(File sourceDir) {
        for (File sourceFile : sourceDir.listFiles()) {
            if (sourceFile.isDirectory()) {
                recursiveSyncAndUpdate(sourceFile);
            }
            updateAndSyncFile(sourceDir, sourceFile.getName(), sourceFile.lastModified());
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
                copyFile(f,destDir);
            }
        }
    }

    void updateAndSyncFile(File destDir, String fileName, long lastModified) {
        if (fileName.equals(".ysync")) {
            return;
        }
        File syncFile = new File(destDir, ".ysync");
        JSONArray filesArray = readSyncFile(syncFile);

        createJsonObjectInArray(filesArray, fileName, lastModified);
        writeSyncFile(syncFile, filesArray);
    }

    JSONArray readSyncFile(File syncFile) {
        JSONArray filesArray = null;
        try {
            if (syncFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(syncFile);
                JSONTokener tokener = new JSONTokener(fileInputStream);
                filesArray = new JSONArray(tokener);
            } else {
                syncFile.createNewFile();
                filesArray = new JSONArray("[]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filesArray;
    }

    void createJsonObjectInArray(JSONArray filesArray, String fileName, long lastModified) {
        boolean found = false;
        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObj = filesArray.getJSONObject(i);
            if (fileObj.getString("name").equals(fileName)) {
                fileObj.put("lastModified", new Date(lastModified));
                found = true;
                break;
            }
        }

        if (!found) {
            JSONObject newFileObj = new JSONObject();
            newFileObj.put("name", fileName);
            newFileObj.put("lastModified", new Date(lastModified));
            filesArray.put(newFileObj);
        }
    }

    void writeSyncFile(File syncFile, JSONArray filesArray) {
        try (FileWriter writer = new FileWriter(syncFile)) {
            writer.write(filesArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
