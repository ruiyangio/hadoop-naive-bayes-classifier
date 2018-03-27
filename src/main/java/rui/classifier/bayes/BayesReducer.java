package rui.classifier.bayes;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.NullWritable;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class BayesReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    public void reduce(Text token, Iterable<Text> records, Context context) throws IOException, InterruptedException {
        long negativeSum = 0;
        long positiveSum = 0;

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
        sumRecord.addProperty("token", token.toString());
        sumRecord.addProperty("positive", positiveSum);
        sumRecord.addProperty("negative", negativeSum);

        context.write(new Text(sumRecord.toString()), new Text(""));
    }
}
