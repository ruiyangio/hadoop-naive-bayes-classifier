package rui.classifier.bayes;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;

public class BayesReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    public void reduce(Text token, Iterable<Text> records, Context context) throws IOException, InterruptedException {
        for (Text record : records) {
            context.write(token, record);
        }
    }
}
