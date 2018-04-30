import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import rui.classifier.bayes.Util;

public class UtilTest {
    @Test public void testNlpTokenizeMethod() {
        String testStr = "my :) |recommendation : duck's in I don't like while you're waiting( one-hour Marry_Jane\n";
        String[] expected = {
            "my",
            ")",
            "recommendation",
            "duck",
            "s",
            "in",
            "I",
            "don",
            "t",
            "like",
            "while",
            "you",
            "re",
            "waiting(",
            "one",
            "hour",
            "Marry_Jane"
        };

        List<String> tokens = Util.tokenize(testStr);
        assertArrayEquals(expected, tokens.toArray());
    }

    @Test public void testLikeihoodCalculation() {
        assertEquals("-13.019597", Util.calculateLikelihood(0, 405611, 45558));
    }

    @Test public void testPriorCalculation() {
        String[] expected = { "-0.106447", "-2.292856" };
        assertArrayEquals(expected, Util.calculatePrior(405611, 45558));
    }
}
