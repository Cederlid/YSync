package dev.ytterate.ysync.cmd.resources;


import com.beust.jcommander.JCommander;
import dev.ytterate.ysync.cmd.CommandLineArgs;
import dev.ytterate.ysync.cmd.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class MainEndToEnd {

    private Path tempDir;
    private Path directory1;
    private Path directory2;
    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-tempDir");
        directory1 = Files.createDirectory(tempDir.resolve("Directory1"));
        directory2 =  Files.createDirectory(tempDir.resolve("Directory2"));
    }

    @Test
    public void mainWithValidArguments() throws IOException {
        Path fileToCopy = directory1.resolve("copyOfFile.txt");
        Files.createFile(fileToCopy);

        String[] args = {directory1.toString(), directory2.toString()};
        Main.main(args);
        CommandLineArgs commandLineArgs = parseCommandLine(args);
        commandLineArgs.directories.get(0);
        commandLineArgs.directories.get(1);

        Path copiedFile = directory2.resolve("copyOfFile.txt");

        assertTrue(Files.exists(copiedFile));
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
