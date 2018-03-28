package rui.classifier.bayes;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import rui.classifier.bayes.BayesCounter;
import rui.classifier.bayes.Util;

public class BayesMapper extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
        String line = lineText.toString().trim();
        String[] parts = line.split(";");
        long negativeCount = context.getCounter(BayesCounter.NegativeCounter).getValue();
        long negativeDocuments = context.getCounter(BayesCounter.NegativeDocument).getValue();
        long positiveCount = context.getCounter(BayesCounter.PositiveCounter).getValue();
        long positiveDocuments = context.getCounter(BayesCounter.PositiveDocument).getValue();
        long uniqueCount = context.getCounter(BayesCounter.UniqueTokenCounter).getValue();

        String token = parts[0];
        String record = parts[1];
        JsonObject model = new JsonObject();
        
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(record);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            int positives = jsonObject.get("positive").getAsInt();
            int negatives = jsonObject.get("negative").getAsInt();
            String[] priors = Util.calculatePrior(positiveDocuments, negativeDocuments);
            model.addProperty("token", token);
            model.addProperty("positive", Util.calculateLikelihood(positives, positiveCount, uniqueCount));
            model.addProperty("negative", Util.calculateLikelihood(negatives, negativeCount, uniqueCount));
            model.addProperty("positivePrior", priors[0]);
            model.addProperty("negativePrior", priors[1]);
        }

        context.write(new Text(token), new Text(model.toString()));
    }
}
