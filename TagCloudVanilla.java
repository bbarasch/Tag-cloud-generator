import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Put a short phrase describing the program here.
 *
 * @author Ben Barasch, Michael Sustar
 *
 */
public final class TagCloudVanilla {

    /**
     * Separator characters.
     */
    private static final String SEPARATOR = " \t\n\r,-.!?[]';:/()_*`";
    /**
     * Smallest size possible for text.
     */
    private static final int MIN_SIZE = 11;
    /**
     * Largest size possible for text.
     */
    private static final int MAX_SIZE = 48;

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudVanilla() {
    }

    /**
     * Comparator<Map.Pair> implementation to be used to sort alphabetically.
     * Compare {@code String}s in lexicographic order.
     */
    private static class StringLT
            implements Comparator<Entry<String, Integer>> {

        @Override
        public int compare(Entry<String, Integer> s1,
                Entry<String, Integer> s2) {
            return s1.getKey().compareToIgnoreCase(s2.getKey());
        }

    }

    /**
     * Comparator<Map.Pair> implementation to be used to get rid of lowest
     * values until N number of objects is reached. Compare {@code String} in
     * increasing order.
     */
    private static class IntLT implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> s1,
                Entry<String, Integer> s2) {
            return s1.getValue().compareTo(s2.getValue());
        }
    }

    /**
     * Comparator instance.
     */
    private static final StringLT ORDERALPHA = new StringLT();

    /**
     * Comparator instance.
     */
    private static final IntLT ORDERINT = new IntLT();

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int endIndex = position;
        boolean ifSep = SEPARATOR.contains(text.charAt(position) + "");
        while (endIndex < text.length()
                && SEPARATOR.contains(text.charAt(endIndex) + "") == ifSep) {
            endIndex++;
        }
        return text.substring(position, endIndex);

    }

    /**
     * Prints out the tag cloud.
     *
     * @param name
     *            name of the output file
     * @param words
     *            all words sorted alphabetically
     * @param output
     *            the output file
     * @param n
     *            the number of words found
     * @throws IOException
     */
    private static void print(String name,
            List<Map.Entry<String, Integer>> words, FileWriter output, int n) {
        try {
            output.write("<html>\n<head>\n");
            String title = "Top " + n + " words in " + name;
            output.write("<title>" + title + "</title>\n");
            //Broke up print statement for better style, so the line isn't way too long
            output.write(
                    "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2");
            output.write(
                    "/assignments/projects/tag-cloud-generator/data/tagcloud.css");
            output.write("\"rel=\"stylesheet\" type=\"text/css\">");
            output.write("\n</head>\n<body>\n");
            output.write("<h2>" + title + "</h2>\n<hr>\n");
            output.write("<div class=\"cdiv\">\n<p class=\"cbox\">\n");
            while (words.size() > 0) {
                Map.Entry<String, Integer> word = words.remove(0);
                int size = MIN_SIZE + word.getValue() / 20;
                if (size > MAX_SIZE) {
                    size = MAX_SIZE;
                }
                output.write("<span style=\"cursor:default\" class=\"f" + size
                        + "\"");
                output.write(" title=\"count: " + word.getValue() + "\">"
                        + word.getKey() + "</span>\n");
            }
            output.write("</p>\n</div>\n</body>\n</html>");
        } catch (IOException e) {
            System.err.println("Error printing to file.");
            return;
        }
    }

    /**
     * Sort words and set their counts.
     *
     * @param input
     *            input file
     * @param n
     *            number of words
     * @throws IOException
     *             exception
     * @return List of map entries
     */
    private static List<Map.Entry<String, Integer>> sort(BufferedReader input,
            int n) {
        List<Map.Entry<String, Integer>> allWords = new ArrayList<Map.Entry<String, Integer>>();
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        String line = "";
        try {
            line = input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading from file.");
        }
        while (line != null) {

            //System.out.println(line);
            while (line.length() > 0) {
                String word = nextWordOrSeparator(line, 0).toLowerCase();
                if (!SEPARATOR.contains(word.charAt(0) + "")) {
                    if (wordCount.containsKey(word)) {
                        int count = wordCount.remove(word);
                        Integer newCount = count + 1;
                        wordCount.put(word, newCount);
                    } else {
                        Integer newCount = 1;
                        //System.out.println(word);
                        wordCount.put(word, newCount);
                    }
                }
                line = line.substring(word.length());
            }
            try {
                line = input.readLine();
            } catch (IOException e) {
                System.err.println("Error reading from file.");
            }
        }
        for (Entry<String, Integer> x : wordCount.entrySet()) {
            allWords.add(x);
        }
        Collections.sort(allWords, ORDERINT);
        while (allWords.size() > n) {
            allWords.remove(0);
        }
        Collections.sort(allWords, ORDERALPHA);
        return allWords;
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     * @throws IOException
     *             stuff
     */
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        System.out.println("Please enter the name of the input file.");
        String inFile, outFile, number;
        inFile = "";
        outFile = "";
        number = "";
        try {
            inFile = in.readLine();
            System.out.println("Please enter the name of the output file.");
            outFile = in.readLine();
            System.out.println(
                    "Please enter the number of words included in tag cloud.");
            number = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading line from console.");
            return;
        }
        int n = 0;
        try {
            n = Integer.parseInt(number);
        } catch (Exception e) {
            System.out.println("Input was not an integer");
            return;
        }
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(inFile));
        } catch (IOException e) {
            System.err.println("Error opening file.");
            return;
        }
        FileWriter output;
        try {
            output = new FileWriter(outFile);
        } catch (IOException e) {
            System.err.println("Error creating output file.");
            return;
        }
        List<Map.Entry<String, Integer>> allWords = sort(input, n);
        print(inFile, allWords, output, n);
        try {
            in.close();
            input.close();
            output.close();
        } catch (IOException e) {
            System.err.println("Error closing file.");
            return;
        }
    }

}
