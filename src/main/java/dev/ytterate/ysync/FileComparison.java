package dev.ytterate.ysync;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;

public class FileComparison {
    void copyFile(File sourceDir, File destDir, File[] sourceFiles, File[] destFiles, ArrayList<String> differencesModel) throws IOException {
        if(sourceFiles != null && destFiles != null) {
            for (File sourceFile : sourceFiles) {
                boolean found = false;

                for (File destFile : destFiles) {
                    if (sourceFile.getName().equals(destFile.getName())) {
                        found = true;

                        if (sourceFile.lastModified() > destFile.lastModified()) {
                            differencesModel.add("File: " + sourceFile.getName() + " - in directory: " + destDir.getName() + " - last modified: " + new Date(sourceFile.lastModified()));
                            copyFile(sourceDir, destDir, sourceFiles, destFiles, differencesModel);
                        }
                        break;
                    }
                }
                if (!found) {
                    differencesModel.add("File: " + sourceFile.getName() + " - in directory: " + destDir.getName() + " - last modified: " + new Date(sourceFile.lastModified()));
                    copyFile(sourceDir, destDir, sourceFiles, destFiles, differencesModel);
                }
            }
        }
    }


    void copyAndUpdate(File sourceFile, File destDir) throws IOException {
        File destFile = new File(destDir, sourceFile.getName());
        if (destFile.exists()) {
            destFile.delete();
        }
        if (sourceFile.isDirectory()){ //TODO
            return;
        }
        copyFile(sourceFile, destFile);
        updateAndSyncFile(destDir, sourceFile.getName(), sourceFile.lastModified());
    }

    void copyFile(File sourceFile, File destFile) throws IOException {
        Path sourcePath = sourceFile.toPath();
        Path destPath = destFile.toPath();

        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    void updateAndSyncFile(File destDir, String fileName, long lastModified) {
        File syncFile = new File(destDir, ".ysync");
        JSONArray filesArray = readSyncFile(syncFile);

        updateOrCreateEntry(filesArray, fileName, lastModified);
        writeSyncFile(syncFile, filesArray);
    }

    JSONArray readSyncFile(File syncFile) {
        JSONArray filesArray = new JSONArray();
        if (syncFile.exists()) {
            try {
                String content = FileUtils.readFileToString(syncFile);
                filesArray = new JSONArray(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filesArray;
    }

    void updateOrCreateEntry(JSONArray filesArray, String fileName, long lastModified) {
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
