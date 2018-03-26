package rui.classifier.bayes;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import com.google.gson.JsonParser;

public class Util {
    final static String IGNORE_PATTERN = " \t\n\r\f,.:;?![]";

    public static List<String> tokenize(String s) {
        List<String> res = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(s, IGNORE_PATTERN);
        while (tokenizer.hasMoreTokens()) {
            res.add(tokenizer.nextToken());
        }
        return res;
    }
}
