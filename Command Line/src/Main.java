import dev.ytterate.ysync.ContinueCallback;
import dev.ytterate.ysync.FileComparison;
import dev.ytterate.ysync.SyncAction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Specify the path to source directory and destination Directory.");
            return;
        }

        File sourceDir = new File(args[0]);
        File destDir = new File(args[1]);


        if (!sourceDir.isDirectory() || !destDir.isDirectory()) {
            System.out.println("Type in the correct paths.");
            return;
        }
        if (!sourceDir.exists() || !destDir.exists()) {
            System.out.println("Source or destination directory does not exist.");
            return;
        }

        ContinueCallback continueCallback = new ContinueCallback() {
            @Override
            public CompletableFuture<Boolean> onGotMisMatches(List<SyncAction> syncActions) {
                menu(syncActions);
                return CompletableFuture.completedFuture(true);
            }
        };
        FileComparison fileComparison = new FileComparison(sourceDir, destDir, continueCallback);


        fileComparison.compareAndCopyFiles()
                .thenAccept(result -> {
                    System.out.println("File comparison and copying completed.");
                })
                .exceptionally(ex -> {
                    System.err.println("Error occurred during file comparison and copying: " + ex.getMessage());
                    return null;
                });

//            try {
//                action.run();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            Scanner scanner = new Scanner(System.in);
//            List<String> mismatchList = new ArrayList<>();
//            Set<String> chosenMismatches = new HashSet<>();
//
//            mismatchList.add("up.png");
//            mismatchList.add("down.png");
//            mismatchList.add("other.jpg");
//            List<String> unChosenMismatches = new ArrayList<>(mismatchList);
//
//            int inputAsInt;
//            while (!unChosenMismatches.isEmpty()) {
//                menu(unChosenMismatches);
//                String inputAsString = scanner.nextLine();
//                if (inputAsString.isEmpty()) {
//                    printRemainingMismatches(unChosenMismatches, chosenMismatches);
//                    break;
//                } else {
//                    try {
//                        inputAsInt = Integer.parseInt(inputAsString);
//                        if (inputAsInt > unChosenMismatches.size() || inputAsInt < 1) {
//                            System.out.println("invalid number");
//                        }
//                        String chosenMismatch = unChosenMismatches.get(inputAsInt - 1);
//
//                        if (chosenMismatches.contains(chosenMismatch)) {
//                            System.out.println("Number already chosen");
//                        }
//                        chosenMismatches.add(chosenMismatch);
//                        unChosenMismatches.remove(chosenMismatch);
//                        printRemainingMismatches(unChosenMismatches, chosenMismatches);
//
//                    } catch (NumberFormatException e) {
//                        System.out.println("you must enter a number");
//                    } catch (IndexOutOfBoundsException e) {
//                        System.out.println("invalid index");
//                    }
//
//                }
//            }
    }

    private static void menu(List<SyncAction> unChosenMismatches) {
        System.out.println("MisMatches: ");
        for (int i = 0; i < unChosenMismatches.size(); i++) {
            System.out.println((i + 1) + ". " + unChosenMismatches.get(i));
        }
        System.out.println("Enter a number:");
    }

    private static void printRemainingMismatches(List<String> unChosenMismatches, Set<String> chosenMismatches) {
        System.out.println("Remaining Mismatches:");
        int index = 1;
        for (String mismatch : unChosenMismatches) {
            if (!chosenMismatches.contains(mismatch)) {
                System.out.println(index + ". " + mismatch);
                index++;
            }
        }

    }

}