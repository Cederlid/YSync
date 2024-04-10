package dev.ytterate.ysync.cmd;

import com.beust.jcommander.JCommander;
import dev.ytterate.ysync.ContinueCallback;
import dev.ytterate.ysync.FileComparison;
import dev.ytterate.ysync.MisMatchAction;
import dev.ytterate.ysync.SyncAction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        JSONArray jsonArray = loadJsonSyncFileFromDesktop();

        CommandLineArgs commandLineArgs = parseCommandLine(args);

//        if (commandLineArgs.directories.size() != 2) {
//            System.out.println("Specify the source and destination directories.");
//            return;
//        }

//        String sourceDirectory = commandLineArgs.directories.get(0);
//        String destinationDirectory = commandLineArgs.directories.get(1);

//        File sourceDir = new File(sourceDirectory);
//        File destDir = new File(destinationDirectory);

//        if (!sourceDir.isDirectory() || !destDir.isDirectory()) {
//            System.out.println("Type in the correct paths.");
//            return;
//        }
//        if (!sourceDir.exists() || !destDir.exists()) {
//            System.out.println("Source or destination directory does not exist.");
//            return;
//        }

        ContinueCallback continueCallback = syncActions -> {
            handleUserInput(syncActions);
            return CompletableFuture.completedFuture(true);
        };

//        syncDirectories(commandLineArgs, continueCallback, sourceDir, destDir, commandLineArgs.filesToCopy, commandLineArgs.ignoredFiles);
//
        syncDirectoriesFromJsonArray(jsonArray, commandLineArgs, continueCallback);
    }

    private static JSONArray loadJsonSyncFileFromDesktop() {
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        String jsonFilePath = desktopPath + "/JsonSyncFile.json";
        JSONArray jsonArray = readSyncFile(new File(jsonFilePath));
        System.out.println("Sync file includes: " + jsonArray.toString());
        return jsonArray;
    }

    private static void syncDirectoriesFromJsonArray(JSONArray jsonArray, CommandLineArgs commandLineArgs, ContinueCallback continueCallback) throws IOException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject pair = jsonArray.getJSONObject(i);
            String sourceDirectoryFromJson = pair.getString("source");
            String destinationDirectoryFromJson = pair.getString("destination");
            JSONArray copyFromJson = pair.optJSONArray("copy");
            JSONArray ignoreFromJson = pair.optJSONArray("ignore");

            File sourceDirFromJson = new File(sourceDirectoryFromJson);
            File destDirFromJson = new File(destinationDirectoryFromJson);

            List<String> fileToCopy = new ArrayList<>();
            List<String> fileToIgnore = new ArrayList<>();


            if (copyFromJson != null) {
                for (int j = 0; j < copyFromJson.length(); j++) {
                    String filename = copyFromJson.getString(j);
                    fileToCopy.add(filename);
                    System.out.println("Added file to copy: " + filename);
                }

            }

            if (ignoreFromJson != null) {
                for (int j = 0; j < ignoreFromJson.length(); j++) {
                    String filename = ignoreFromJson.getString(j);
                    fileToIgnore.add(filename);
                    System.out.println("Added file to ignore: " + filename);
                }

            }
            syncDirectories(commandLineArgs, continueCallback, sourceDirFromJson, destDirFromJson, fileToCopy, fileToIgnore);

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

    private static void handleUserInput(List<SyncAction> actionList) {
        if (actionList.isEmpty()) {
            System.out.println("There are no mismatches!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        List<SyncAction> unChosenMismatches = actionList.stream()
                .filter(action -> action instanceof MisMatchAction).collect(Collectors.toList());

        int inputAsInt;
        while (!unChosenMismatches.isEmpty()) {
            showMismatches(unChosenMismatches);
            String inputAsString = scanner.nextLine();
            if (inputAsString.isEmpty()) {
                printRemainingMismatches(unChosenMismatches);
                break;
            } else {
                try {
                    inputAsInt = Integer.parseInt(inputAsString);
                    if (inputAsInt > unChosenMismatches.size() || inputAsInt < 1) {
                        System.out.println("Choose the correct number");
                    }

                    SyncAction chosenMisMatch = unChosenMismatches.get(inputAsInt - 1);
                    if (chosenMisMatch.isMisMatch() && chosenMisMatch instanceof MisMatchAction misMatchAction) {
                        misMatchAction.confirm();
                    }

                    unChosenMismatches.remove(inputAsInt - 1);
                    printRemainingMismatches(unChosenMismatches);

                } catch (NumberFormatException e) {
                    System.out.println("you must enter a number");
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("invalid index");
                }

            }
        }

    }

    private static void showMismatches(List<SyncAction> mismatches) {
        System.out.println("MisMatches: ");
        int index = 1;
        for (SyncAction mismatch : mismatches) {
            System.out.println(index + ". " + mismatch);
            index++;
        }
        System.out.println("Enter a number:");
    }

    private static void printRemainingMismatches(List<SyncAction> unChosenMismatches) {
        System.out.println("Remaining Mismatches:");
        int index = 1;
        for (SyncAction mismatch : unChosenMismatches) {
            System.out.println(index + ". " + mismatch);
            index++;
        }

    }

    private static JSONArray readSyncFile(File jsonFile) {
        JSONArray filesArray;

        try {
            if (jsonFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(jsonFile);
                JSONTokener jsonTokener = new JSONTokener(fileInputStream);
                filesArray = new JSONArray(jsonTokener);
            } else {
                filesArray = new JSONArray();
                boolean newFile;
                try {
                    newFile = jsonFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assert newFile;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return filesArray;
    }


    private static void syncDirectories(CommandLineArgs commandLineArgs, ContinueCallback continueCallback, File sourceDir, File destDir, List<String> fileToCopy, List<String> fileToIgnore) throws IOException {
        CompletableFuture<Void> operationCompleted = new CompletableFuture<>();

        FileComparison fileComparison = new FileComparison(sourceDir, destDir, continueCallback, fileToCopy, fileToIgnore);

        fileComparison.compareAndCopyFiles(fileToCopy, fileToIgnore)
                .thenAccept(result -> {
                    System.out.println("File comparison and copying completed.");
                    operationCompleted.complete(null);
                })
                .exceptionally(ex -> {
                    System.err.println("Error occurred during file comparison and copying: " + ex.getMessage());
                    operationCompleted.completeExceptionally(ex);
                    return null;
                });
    }


}
