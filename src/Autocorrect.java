import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Deven Dharni
 */
public class Autocorrect {

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */;

    private String[] words;
    private int threshold;
    private ArrayList<Integer>[] combinations;

    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;

        // Set up the combinations map
        combinations = new ArrayList[676];

        // Fill in each slot
        for (int i = 0; i < 676; i++) {
            combinations[i] = new ArrayList<Integer>();
        }

        // Loop through every word in the dictionary
        for (int i = 0; i < words.length; i++) {
            // Loop through each two letter combo
            for (int j = 0; j < words[i].length() - 1; j++) {
                int firstLetter = words[i].charAt(j) - 'a';
                int secondLetter = words[i].charAt(j+1) - 'a';

                // Skip if the character isn't a lowercase letter because some words in dictionary could have hyphen, etc.
                if (firstLetter < 0 || firstLetter > 26 || secondLetter < 0 || secondLetter > 26) {
                    continue;
                }

                // Use * 26 + letter as a formula to fill in
                int locationInMap = firstLetter * 26 + secondLetter;
                combinations[locationInMap].add(i);
            }
        }
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // Create an arraylist of words that work in threshold
        ArrayList<Word> compatibleWords = new ArrayList<Word>();

        // Only use the combination map if the words are greater than 4
        if (typed.length() > 4) {
            // Find the two letter combinations for the word typed
            ArrayList<Integer> potentialCandidates = new ArrayList<Integer>();

            for (int i = 0; i < typed.length() - 1; i++) {
                int firstLetter = typed.charAt(i) - 'a';
                int secondLetter = typed.charAt(i + 1) - 'a';

                int slotInMap = firstLetter * 26 + secondLetter;

                // Loop through all the possible word choices for that specific two letter combination
                for (int j = 0; j < combinations[slotInMap].size(); j++) {
                    // Get the actual word
                    int index = combinations[slotInMap].get(j);

                    // If it's not already there, then add it
                    if (!potentialCandidates.contains(index)) {
                        potentialCandidates.add(index);
                    }
                }
            }

            // Loop through each word in the new map to find edit distance
            for (int index: potentialCandidates) {
                // Call helper function to find edit distance using tabulation approach
                int distanceBetweenWords = findEditDistance(typed, words[index]);

                // Only add if it works for the threshold
                if (distanceBetweenWords <= threshold) {
                    // Create new word object and add it
                    Word wordToAdd = new Word(words[index], distanceBetweenWords);
                    compatibleWords.add(wordToAdd);
                }
            }
        }
        else {
            // Loop through each word in the dictionary and find the edit distance
            for (String word: words) {
                // Call helper function to find edit distance using tabulation approach
                int distanceBetweenWords = findEditDistance(typed, word);

                // Only add if it works for the threshold
                if (distanceBetweenWords <= threshold) {
                    // Create new word object and add it
                    Word wordToAdd = new Word(word, distanceBetweenWords);
                    compatibleWords.add(wordToAdd);
                }
            }
        }

        // Sort the lists properly using comparator
        compatibleWords.sort(Comparator.comparing(Word::getStr));
        compatibleWords.sort(Comparator.comparing(Word::getEditDistance));

        // Convert the result to a string
        String[] arrayToReturn = new String[compatibleWords.size()];
        for (int i = 0; i < compatibleWords.size(); i++) {
            arrayToReturn[i] = compatibleWords.get(i).getStr();
        }

        return arrayToReturn;
    }

    private int findEditDistance (String typed, String word) {
        int lengthTyped = typed.length();
        int lengthWord = word.length();

        // Create the tabulation array but add 1 just so it's easier to index and think about
        int[][] tabulation = new int[lengthTyped + 1][lengthWord + 1];

        // Fill in all the base cases in the table

        // For deleting all of typed
        for (int i = 0; i <= lengthTyped; i++) {
            tabulation[i][0] = i;
        }
        // For inserting all of word
        for (int i = 0; i <= lengthWord; i++) {
            tabulation[0][i] = i;
        }

        // Loop through the table starting at 1 because table size
        for (int i = 1; i < lengthTyped + 1; i++) {
            for (int j = 1; j < lengthWord + 1; j++) {
                // Case 1: characters match (go diagonal)
                if (typed.charAt(i - 1) == word.charAt(j - 1)) {
                    tabulation[i][j] = tabulation[i-1][j-1];
                }

                // Case 2: they don't match (otherwise take the least of delete/insert/substitute
                else {
                    // Check lowest between deletion and insertion
                    int option1 = Math.min (tabulation[i-1][j], tabulation[i][j-1]);

                    // Check lowest between whatever one and substitution
                    int option2 = Math.min (option1, tabulation[i-1][j-1]);

                    // Add one to total because you are carrying out an action
                    tabulation[i][j] = option2 + 1;
                }
            }
        }

        return tabulation[lengthTyped][lengthWord];
    }

    public static void main (String[] args) {
        // Initialize all the relevant variables
        String[] dictionary = loadDictionary("Large");
        Autocorrect autocorrect = new Autocorrect(dictionary, 2);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Print not print ln because I want not space
            System.out.print("Enter a word: ");

            // Lowercase it in case the user starts their thing with uppercase so the - 'a' doesn't fail
            String typed = scanner.nextLine().toLowerCase();
            String[] compatibleWords = autocorrect.runTest(typed);
            System.out.println("------------");

            // Print out the valid words
            if (compatibleWords.length == 0) {
                System.out.println("Sorry, there are no compatible words");
            }
            else {
                System.out.println("Did you mean...");
                for (String word: compatibleWords) {
                    System.out.println(" " + word);
                }
            }

            System.out.println("------------");
        }

    }

    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}