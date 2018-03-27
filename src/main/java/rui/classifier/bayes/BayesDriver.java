package rui.classifier.bayes;

import java.util.logging.Logger;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.Text;

import rui.classifier.bayes.BayesCounter;

public class BayesDriver extends Configured implements Tool {
    private static final Logger LOG = Logger.getLogger(BayesDriver.class.getName());

    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), this.getClass().getName());
        for (int i = 0; i < args.length; i += 1) {
            if ("-skip".equals(args[i])) {
                job.getConfiguration().setBoolean("bayes.skip.patterns", true);
                i += 1;
                job.addCacheFile(new Path(args[i]).toUri());
                LOG.info("Added file to the distributed cache: " + args[i]);
            }
        }

        job.setJarByClass(this.getClass());
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setMapperClass(BayesMapper.class);
        job.setCombinerClass(BayesReducer.class);
        job.setReducerClass(BayesReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        int jobRes = job.waitForCompletion(true) ? 1 : 0;
        long positiveCount = job.getCounters().findCounter(BayesCounter.PositiveCounter).getValue();
        long negativeCount = job.getCounters().findCounter(BayesCounter.NegativeCounter).getValue();
        LOG.info("Positive words: " + positiveCount);
        LOG.info("Negative words: " + negativeCount);
        return jobRes;
    }
}
