import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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

    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
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
        for (int i = 0; i < lengthTyped; i++) {
            tabulation[i][0] = i;
        }
        // For inserting all of word
        for (int i = 0; i < lengthWord; i++) {
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