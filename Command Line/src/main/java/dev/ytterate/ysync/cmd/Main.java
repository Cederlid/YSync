package dev.ytterate.ysync.cmd;

import com.beust.jcommander.JCommander;
import dev.ytterate.ysync.ContinueCallback;
import dev.ytterate.ysync.FileComparison;
import dev.ytterate.ysync.MisMatchAction;
import dev.ytterate.ysync.SyncAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws IOException {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander.newBuilder()
                .addObject(commandLineArgs)
                .build()
                .parse(args);

        if (commandLineArgs.sourceDirectory == null || commandLineArgs.destinationDirectory == null) {
            System.out.println("Specify the path to source directory and destination Directory.");
            return;
        }

        File sourceDir = new File(commandLineArgs.sourceDirectory);
        File destDir = new File(commandLineArgs.destinationDirectory);

        if (args.length < 2) {
            System.out.println("Specify the path to source directory and destination Directory.");
            return;
        }

        if (!sourceDir.isDirectory() || !destDir.isDirectory()) {
            System.out.println("Type in the correct paths.");
            return;
        }
        if (!sourceDir.exists() || !destDir.exists()) {
            System.out.println("Source or destination directory does not exist.");
            return;
        }

        ContinueCallback continueCallback = syncActions -> {
            handleUserInput(syncActions);
            return CompletableFuture.completedFuture(true);
        };
        FileComparison fileComparison = new FileComparison(sourceDir, destDir, continueCallback);


        fileComparison.compareAndCopyFiles()
                .thenAccept(result -> System.out.println("File comparison and copying completed."))
                .exceptionally(ex -> {
                    System.err.println("Error occurred during file comparison and copying: " + ex.getMessage());
                    return null;
                });

    }

    private static void handleUserInput(List<SyncAction> mismatchList) {
        if (mismatchList.isEmpty()) {
            System.out.println("There are no mismatches!");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        List<SyncAction> unChosenMismatches = new ArrayList<>(mismatchList);

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

}