package rui.classifier.bayes;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.StringReader;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

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

    public static String classifySentence(String sentence, Map<String, JsonObject> model) {
        List<String> tokens = Util.tokenize(sentence);
        Double positiveScore = 0.0;
        Double negativeScore = 0.0;
        Double positivePrior = 0.0;
        Double negativePrior = 0.0;

        for (String token : tokens) {
            if (!model.containsKey(token)) {
                continue;
            }
            
            JsonObject currModel = model.get(token);
            Double positive = Double.parseDouble(currModel.get("positive").getAsString());
            Double negative = Double.parseDouble(currModel.get("negative").getAsString());
            positivePrior = Double.parseDouble(currModel.get("positivePrior").getAsString());
            negativePrior = Double.parseDouble(currModel.get("negativePrior").getAsString());
            positiveScore += positive;
            negativeScore += negative;
        }

        positiveScore += positivePrior;
        negativeScore += negativePrior;

        return positiveScore >= negativeScore ? "POS" : "NEG";
    }

    public static Double evaluateAccuracy(String validationSetFile, String modelFile) {
        long totalLines = 0;
        long correctLines = 0;

        Path validationSetPath = Paths.get(validationSetFile);
        Path modelPath = Paths.get(modelFile);
        Map<String, JsonObject> model = new HashMap<>();

        try {
            List<String> validationContent = Files.readAllLines(validationSetPath, StandardCharsets.UTF_8);
            List<String> modelContent = Files.readAllLines(modelPath, StandardCharsets.UTF_8);

            for (String modelLine : modelContent) {
                String[] parts = modelLine.split(";");
                JsonParser jsonParser = new JsonParser();
                JsonElement element = jsonParser.parse(parts[1]);
                if (element.isJsonObject()){
                    model.put(parts[0].trim(), element.getAsJsonObject());
                }
            }

            for (String validationLine : validationContent) {
                totalLines++;
                validationLine = validationLine.trim().replaceAll("\\r|\\n", "");
                String[] parts = validationLine.split("@@@@");
                String sentence = parts[0].toLowerCase();
                String label = parts[1];

                if (label.equals(classifySentence(sentence, model))) {
                    correctLines++;
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return totalLines == 0 ? 0.0 : (double)correctLines / (double)totalLines;
    }
}
