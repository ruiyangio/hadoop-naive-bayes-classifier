package rui.classifier.bayes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class BayesReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    public void reduce(Text token, Iterable<Text> records, Context context) throws IOException, InterruptedException {
        int negativeSum = 0;
        int positiveSum = 0;

        for (Text record : records) {
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(record.toString());
            if (element.isJsonObject()){
                JsonObject jsonObject = element.getAsJsonObject();
                positiveSum += jsonObject.get("positive").getAsInt();
                negativeSum += jsonObject.get("negative").getAsInt();
            }
        }
        JsonObject sumRecord = new JsonObject();
        sumRecord.addProperty("positive", positiveSum);
        sumRecord.addProperty("negative", negativeSum);

        context.write(token, new Text(sumRecord.toString()));
    }
}
