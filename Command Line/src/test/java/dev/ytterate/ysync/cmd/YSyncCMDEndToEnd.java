package dev.ytterate.ysync.cmd;

import com.beust.jcommander.JCommander;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class YSyncCMDEndToEnd {

    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-tempDir");
    }

    @Test
    public void copyWithCommandLineArgs() throws IOException {
        Path directory1 = Files.createDirectory(tempDir.resolve("Directory1"));
        Path directory2 = Files.createDirectory(tempDir.resolve("Directory2"));

        Path fileToCopy = directory1.resolve("copyOfFile.txt");
        Files.createFile(fileToCopy);

        String[] args = {directory1.toString(), directory2.toString()};
        YSyncCMD.main(args);
        CommandLineArgs commandLineArgs = parseCommandLine(args);
        commandLineArgs.directories.get(0);
        commandLineArgs.directories.get(1);

        Path copiedFile = directory2.resolve("copyOfFile.txt");
        assertTrue(Files.exists(copiedFile));
    }

    @Test
    public void shouldCopyFromJsonFileWhenSourceIsAFile() throws IOException {
        //Test2 in resources
        File testJsonFile = new File("src/test/resources/test2/test-sync.json");

        String[] args = {"-cf", testJsonFile.getPath()};
        YSyncCMD.main(args);

        JSONArray jsonArray = YSyncCMD.readSyncFile(testJsonFile);
        assertEquals(1, jsonArray.length());

        JSONObject pair = jsonArray.getJSONObject(0);
        String destinationDirectoryFromJson = pair.getString("destination");

        Path destDir = Paths.get(destinationDirectoryFromJson);

        JSONArray copyFromJson = pair.optJSONArray("copy");
        assertNotNull(copyFromJson);
        JSONArray ignoreFromJson = pair.optJSONArray("ignore");
        assertNull(ignoreFromJson);

        assertFilesInCopyArray(copyFromJson, destDir);
        assertTrue(Files.isRegularFile(destDir.resolve("viol.jpeg")));

    }

    @Test
    public void shouldCopyFromJsonFileWhenSourceIsADirectory() throws IOException {
        //Test1 in resources
        File testJsonFile = new File("src/test/resources/test1/test-sync.json");

        String[] args = {"-cf", testJsonFile.getPath()};
        YSyncCMD.main(args);

        JSONArray jsonArray = YSyncCMD.readSyncFile(testJsonFile);
        assertEquals(1, jsonArray.length());


        JSONObject object = jsonArray.getJSONObject(0);
        String destinationDirectoryFromJson = object.getString("destination");

        Path destDir = Paths.get(destinationDirectoryFromJson);

        JSONArray copyFromJson = object.optJSONArray("copy");
        assertNotNull(copyFromJson);
        JSONArray ignoreFromJson = object.optJSONArray("ignore");
        assertNull(ignoreFromJson);

        assertFilesInCopyArray(copyFromJson, destDir);
        assertTrue(Files.isDirectory(destDir.resolve("krabba.HEIC")));

    }

    @Test
    public void shouldIgnoreFromJsonFileWhenIsAFile() throws IOException {
        //Test3 in resources
        File testJsonFile = new File("src/test/resources/test3/test-sync.json");

        String[] args = {"-cf", testJsonFile.getPath()};
        YSyncCMD.main(args);

        JSONArray jsonArray = YSyncCMD.readSyncFile(testJsonFile);
        assertEquals(1, jsonArray.length());

        JSONObject object = jsonArray.getJSONObject(0);
        String destinationDirectoryFromJson = object.getString("destination");

        Path destDir = Paths.get(destinationDirectoryFromJson);

        JSONArray copyFromJson = object.optJSONArray("copy");
        assertNull(copyFromJson);
        JSONArray ignoreFromJson = object.optJSONArray("ignore");
        assertNotNull(ignoreFromJson);

        assertFilesInIgnoreArray(ignoreFromJson, destDir);
        assertTrue(Files.isRegularFile(destDir.resolve("oj.HEIC")));

    }

    @Test
    public void shouldIgnoreFromJsonFileWhenIsADirectory() throws IOException {
        //Test4 in resources
        File testJsonFile = new File("src/test/resources/test4/test-sync.json");

        String[] args = {"-cf", testJsonFile.getPath()};
        YSyncCMD.main(args);

        JSONArray jsonArray = YSyncCMD.readSyncFile(testJsonFile);
        assertEquals(1, jsonArray.length());

        JSONObject object = jsonArray.getJSONObject(0);
        String destinationDirectoryFromJson = object.getString("destination");

        Path destDir = Paths.get(destinationDirectoryFromJson);

        JSONArray copyFromJson = object.optJSONArray("copy");
        assertNull(copyFromJson);
        JSONArray ignoreFromJson = object.optJSONArray("ignore");
        assertNotNull(ignoreFromJson);

        assertFilesInIgnoreArray(ignoreFromJson, destDir);
        assertTrue(Files.isRegularFile(destDir.resolve("krabba.HEIC")));

    }

    private static void assertFilesInIgnoreArray(JSONArray ignoreArray, Path destDir) {
        if (ignoreArray != null) {
            for (int j = 0; j < ignoreArray.length(); j++) {
                String filename = ignoreArray.getString(j);
                assertTrue(Files.exists(destDir.resolve(filename)), "File " + filename + " should be ignored.");
            }
        }
    }

    private static void assertFilesInCopyArray(JSONArray copyArray, Path destDir) {
        if (copyArray != null) {
            for (int j = 0; j < copyArray.length(); j++) {
                String filename = copyArray.getString(j);
                assertTrue(Files.exists(destDir.resolve(filename)), "File " + filename + " should be copied.");
            }
        }
    }

    private static CommandLineArgs parseCommandLine(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander.newBuilder()
                .addObject(commandLineArgs)
                .build()
                .parse(args);
        return commandLineArgs;
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}
