package rui.classifier.bayes;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.StringReader;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;

public class Util {
    final static String IGNORE_PATTERN = " \t\n\r\f,.:;?![]()|~";

    private static String removeIgnorePattern(String s) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(s, IGNORE_PATTERN);
        String prefix = "";
        while (tokenizer.hasMoreTokens()) {
            sb.append(prefix);
            sb.append(tokenizer.nextToken());
            prefix = " ";
        }
        return sb.toString();
    }

    public static List<String> tokenize(String s) {
        s = removeIgnorePattern(s);
        List<String> res = new ArrayList<>();
        PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
        while (tokenizer.hasNext()) {
            res.add(tokenizer.next().toString());
        }
        return res;
    }

    public static String calculateLikelihood(int wordCount, long labelWordCountTotal, long uniqueWords) {
        // Laplace smoothing
        Double res = Math.log(1 + ((double)wordCount + 1) / ((double)labelWordCountTotal + (double)uniqueWords));
        return String.format("%.6f", res);
    }

    public static String[] calculatePrior(long positiveCount, long negativeCount) {
        String[] res = new String[2];
        Double res1 = Math.log(1 + (double) positiveCount / ((double)positiveCount + (double)negativeCount));
        Double res2 = Math.log(1 + (double) negativeCount / ((double)positiveCount + (double)negativeCount));

        res[0] = String.format("%.6f", res1);
        res[1] = String.format("%.6f", res2);
        return res;
    }
}
