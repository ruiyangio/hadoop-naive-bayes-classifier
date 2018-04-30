package rui.classifier.bayes;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.regex.Pattern;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class Util {
    private static final Pattern IGNORE_PATTERN = Pattern.compile("[^(a-zA-Z0-9_)+\\s]");
    private static final String PRECISION_FORMAT = "%.6f";
    private static final String SEPERATOR = "@@@@";

    public static List<String> tokenize(String s) {
        s = IGNORE_PATTERN.matcher(s).replaceAll(" ");
        return Arrays.asList(s.split("\\s+"));
    }

    public static String calculateLikelihood(int wordCount, long labelWordCountTotal, long uniqueWords) {
        // Laplace smoothing
        Double res = Math.log( ((double)wordCount + 1) / ((double)labelWordCountTotal + (double)uniqueWords));
        return String.format(PRECISION_FORMAT, res);
    }

    public static String[] calculatePrior(long positiveCount, long negativeCount) {
        String[] res = new String[2];
        Double res1 = Math.log((double) positiveCount / ((double)positiveCount + (double)negativeCount));
        Double res2 = Math.log((double) negativeCount / ((double)positiveCount + (double)negativeCount));

        res[0] = String.format(PRECISION_FORMAT, res1);
        res[1] = String.format(PRECISION_FORMAT, res2);
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
                String[] parts = validationLine.split(SEPERATOR);
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
