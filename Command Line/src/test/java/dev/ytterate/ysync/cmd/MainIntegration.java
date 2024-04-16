package dev.ytterate.ysync.cmd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.ytterate.ysync.cmd.Main.readSyncFile;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainIntegration {

    private FileTime newLastModifiedTime;
    private File tempFile;
    private Path directory1;
    private Path directory2;
    private Path tempDir;

    @BeforeEach
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("test-tmpDir");
        directory1 = Files.createDirectory(tempDir.resolve("Directory1"));
        directory2 =  Files.createDirectory(tempDir.resolve("Directory2"));
    }

    private void createTempFileWithDate() throws IOException {
        tempFile = new File(directory1.toFile(), "text.txt");
        LocalDate newLocalDate = LocalDate.of(2004, 11, 5);
        Instant instant = newLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Files.setLastModifiedTime(tempFile.toPath(), FileTime.from(instant));
        newLastModifiedTime = Files.readAttributes(tempFile.toPath(),
                BasicFileAttributes.class).lastModifiedTime();
    }

    @Test
    public void syncDirectoriesFromJson() throws IOException {
        JSONArray jsonArray = loadJsonSyncFileFromDesktop();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject pair = jsonArray.getJSONObject(i);
            String sourceDirectory = pair.getString("source");
            String destinationDirectory = pair.getString("destination");

            JSONArray copyFromJson = pair.optJSONArray("copy");
            List<String> fileToCopy = copyFromJson != null ? toList(copyFromJson) : new ArrayList<>();

            JSONArray ignoreFromJson = pair.optJSONArray("ignore");
            List<String> fileToIgnore = ignoreFromJson != null ? toList(ignoreFromJson) : new ArrayList<>();

            CommandLineArgs commandLineArgs = new CommandLineArgs();

            Main.syncDirectories(commandLineArgs, null, new File(sourceDirectory), new File(destinationDirectory), fileToCopy, fileToIgnore);
        }


    }

    private List<String> toList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int j = 0; j < jsonArray.length(); j++) {
            list.add(jsonArray.getString(j));
        }
        return list;
    }

    private static JSONArray loadJsonSyncFileFromDesktop() {
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        String jsonFilePath = desktopPath + "/JsonSyncFile.json";
        return readSyncFile(new File(jsonFilePath));
    }

    @Test
    public void copyFileToADirectory() throws IOException {
        File sourceFile = new File(directory1.toFile(), "testFile.txt");
        sourceFile.createNewFile();

        File destFile = new File(directory2.toFile(), "testFile.txt");
        Files.copy(sourceFile.toPath(), destFile.toPath());

        assertTrue(destFile.exists());
    }

    @AfterEach
    public void cleanUp() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile.toPath());
        }

        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }


}