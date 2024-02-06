package dev.ytterate.ysync;

import org.apache.commons.io.FileUtils;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class FileComparison {
    void compareAndCopyFiles(File sourceDir, File destDir) throws IOException {
        if (sourceDir != null && destDir != null) {
            for (File sourceFile : sourceDir.listFiles()) {
                if (sourceFile.isDirectory()) {
                    boolean found = false;

                    // Optional<File> filtered = Arrays.stream(destDir.listFiles()).filter(f -> f.getName().equals(sourceFile.getName())).findFirst();
                    for (File destFile : destDir.listFiles()) {
                        if (sourceFile.getName().equals(destFile.getName())) {
                            found = true;
                            compareAndCopyFiles(sourceFile, destFile);
                            break;
                        }
                    }
                    if (!found) {
                        File destFile = new File(destDir, sourceFile.getName());
                        try {
                            FileUtils.copyDirectory(sourceFile, destFile);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                } else {

                    boolean found = false;

                    // Optional<File> filtered = Arrays.stream(destDir.listFiles()).filter(f -> f.getName().equals(sourceFile.getName())).findFirst();
                    for (File destFile : destDir.listFiles()) {
                        if (sourceFile.getName().equals(destFile.getName())) {
                            found = true;

                            if (sourceFile.lastModified() > destFile.lastModified()) {
                                copyFile(sourceFile, destDir);
                            }
                            break;
                        }
                    }
                    if (!found) {
                        copyFile(sourceFile, destDir);
                    }
                }
            }

        }
        for (File destFile : destDir.listFiles()) {
            updateAndSyncFile(destDir, destFile.getName(), destFile.lastModified());
        }
    }

    void copyFile(File sourceFile, File destFile) throws IOException {
        Path sourcePath = sourceFile.toPath();
        Path PathToFolder = destFile.toPath().resolve(sourcePath.getFileName());

        Files.copy(sourcePath, PathToFolder, StandardCopyOption.REPLACE_EXISTING);
    }

    void updateAndSyncFile(File destDir, String fileName, long lastModified) {
        File syncFile = new File(destDir, ".ysync");
        JSONArray filesArray = readSyncFile(syncFile);

        updateOrCreateEntry(filesArray, fileName, lastModified);
        writeSyncFile(syncFile, filesArray);
    }

    JSONArray readSyncFile(File syncFile) {
        JSONArray filesArray = null;
        try {
            if (!syncFile.exists()) {
                syncFile.createNewFile();
                filesArray = new JSONArray("[]");
            } else {
                FileInputStream fileInputStream = new FileInputStream(syncFile);
                JSONTokener tokener = new JSONTokener(fileInputStream);
                filesArray = new JSONArray(tokener);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
