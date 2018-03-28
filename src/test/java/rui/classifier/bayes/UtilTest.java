import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import rui.classifier.bayes.Util;

public class UtilTest {
    @Test public void testNlpTokenizeMethod() {
        String testStr = "my |recommendation : duck's in I don't like while you're waiting( one-hour Marry_Jane\n";
        String[] expected = {
            "my",
            "recommendation",
            "duck",
            "'s",
            "in",
            "I",
            "do",
            "n't",
            "like",
            "while",
            "you",
            "'re",
            "waiting",
            "one-hour",
            "Marry_Jane"
        };

        List<String> tokens = Util.tokenize(testStr);
        assertArrayEquals(expected, tokens.toArray());
    }

    @Test public void testLikeihoodCalculation() {
        Double res = Util.calculateLikelihood(0, 405611, 45558);
        assertEquals("0.000002", String.format("%.6f", res));
    }
}
