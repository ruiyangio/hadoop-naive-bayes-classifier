package rui.classifier.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import rui.classifier.bayes.Util;

public class BayesMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger LOG = Logger.getLogger(BayesMapper.class.getName());
    private long numRecords = 0;
    private String input;
    private Set<String> patternsToSkip = new HashSet<String>();

    protected void setup(Mapper.Context context) throws IOException, InterruptedException {
        if (context.getInputSplit() instanceof FileSplit) {
            this.input = ((FileSplit) context.getInputSplit()).getPath().toString();
        } else {
            this.input = context.getInputSplit().toString();
        }
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
        } catch (IOException ioe) {
            System.err.println("Caught exception while parsing the cached file '" + patternsURI + "' : " + StringUtils.stringifyException(ioe));
        }
    }

    public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
        String line = lineText.toString();
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(line);
        if (element.isJsonObject()){
            JsonObject jsonObject = element.getAsJsonObject();
            String sentence = jsonObject.get("s").getAsString();
            String label = jsonObject.get("label").getAsString();
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

                }
                else {
                    record.addProperty("positive", 0);
                    record.addProperty("negative", 1);
                }

                Text tokenKey = new Text(token);
                Text rcordText = new Text(record.toString());
                context.write(tokenKey, rcordText);
            }
        }
    }
}
