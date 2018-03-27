package rui.classifier.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;
import com.google.gson.JsonObject;

import rui.classifier.bayes.Util;
import rui.classifier.bayes.BayesCounter;

public class BayesMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger LOG = Logger.getLogger(BayesMapper.class.getName());
    private static final String LABEL_SEPRATOR = "###";
    private Set<String> patternsToSkip = new HashSet<String>();
    private Text POSITIVE_COUNT = new Text("POSITIVE_COUNTS");
    private Text NEGATIVE_COUNT = new Text("NEGATIVE_COUNTS");

    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration config = context.getConfiguration();
        if (config.getBoolean("bayes.skip.patterns", false)) {
            URI[] localPaths = context.getCacheFiles();
            parseStopWords(localPaths[0]);
        }
    }

    private void parseStopWords(URI patternsURI) {
        LOG.info("Added file to the distributed cache: " + patternsURI);
        try {
            BufferedReader fis = new BufferedReader(new FileReader(new File(patternsURI.getPath()).getName()));
            String pattern;
            while ((pattern = fis.readLine()) != null) {
                pattern = pattern.trim().toLowerCase();
                patternsToSkip.add(pattern);
            }
            fis.close();
        } catch (IOException ioe) {
            System.err.println("Caught exception while parsing the cached file '" + patternsURI + "' : " + StringUtils.stringifyException(ioe));
        }
    }

    public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
        String line = lineText.toString().trim();
        line = line.replaceAll("\\r|\\n", "");
        String[] parts = line.split(LABEL_SEPRATOR);
        String sentence = parts[0].toLowerCase();
        String label = parts[1];

        List<String> tokens = Util.tokenize(sentence);
        for (String token : tokens) {
            if (this.patternsToSkip.contains(token)) {
                continue;
            }

            JsonObject record = new JsonObject();
            record.addProperty("token", token);
            if (label.equals("Positive")) {
                record.addProperty("positive", 1);
                record.addProperty("negative", 0);
                context.write(POSITIVE_COUNT, new Text(record.toString()));
                context.getCounter(BayesCounter.PositiveCounter).increment(1);
            }
            else {
                record.addProperty("positive", 0);
                record.addProperty("negative", 1);
                context.write(NEGATIVE_COUNT, new Text(record.toString()));
                context.getCounter(BayesCounter.NegativeCounter).increment(1);
            }

            Text tokenKey = new Text(token);
            context.write(tokenKey, new Text(record.toString()));
        }
    }
}
