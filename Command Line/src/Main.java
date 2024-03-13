import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> mismatchList = new ArrayList<>();
        Set<String> chosenMismatches = new HashSet<>();

        mismatchList.add("up.png");
        mismatchList.add("down.png");
        mismatchList.add("other.jpg");
        List<String> unChosenMismatches = new ArrayList<>(mismatchList);

        int inputAsInt;
        while (!unChosenMismatches.isEmpty()) {
            menu(unChosenMismatches);
            String inputAsString = scanner.nextLine();
            if (inputAsString.isEmpty()) {
                printRemainingMismatches(unChosenMismatches, chosenMismatches);
                break;
            } else {
                try {
                    inputAsInt = Integer.parseInt(inputAsString);
                    if (inputAsInt > unChosenMismatches.size() || inputAsInt < 1) {
                        System.out.println("invalid number");
                    }
                    String chosenMismatch = unChosenMismatches.get(inputAsInt - 1);

                    if (chosenMismatches.contains(chosenMismatch)) {
                        System.out.println("Number already chosen");
                    }
                        chosenMismatches.add(chosenMismatch);
                        unChosenMismatches.remove(chosenMismatch);
                        printRemainingMismatches(unChosenMismatches, chosenMismatches);

                } catch (NumberFormatException e) {
                    System.out.println("you must enter a number");
                } catch (IndexOutOfBoundsException e) {
                System.out.println("invalid index");
            }

            }
        }
    }

    private static void menu(List<String> unChosenMismatches) {
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