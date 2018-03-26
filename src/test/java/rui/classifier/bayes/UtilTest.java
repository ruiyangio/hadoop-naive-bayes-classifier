import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import rui.classifier.bayes.Util;

public class UtilTest {
    @Test public void testTokenizeMethod() {
        String testStr = "my recommendation : duck in while you're waiting for another movie to start";
        String[] expected = { "my", "recommendation", "duck", "in", "while", "you're", "waiting", "for", "another", "movie", "to", "start" };
        List<String> tokens = Util.tokenize(testStr);
        for (String token : tokens) {
            System.out.println(token);
        }
        assertArrayEquals(expected, tokens.toArray());
    }
}
