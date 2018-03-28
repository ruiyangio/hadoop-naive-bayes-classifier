package rui.classifier.bayes;

import java.util.logging.Logger;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.TaskCounter;

public class BayesDriver extends Configured implements Tool {
    private static final Logger LOG = Logger.getLogger(BayesDriver.class.getName());

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf1 = new Configuration();
        conf1.set(TextOutputFormat.SEPERATOR, ";");

        Job job = Job.getInstance(conf1, this.getClass().getName());
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
        job.setMapperClass(PreprocessMapper.class);
        job.setCombinerClass(PreprocessReducer.class);
        job.setReducerClass(PreprocessReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.waitForCompletion(true);
        
        // Build the model
        Configuration conf2 = new Configuration();
        conf2.set(TextOutputFormat.SEPERATOR, ";");
        
        Job job2 = Job.getInstance(getConf(), this.getClass().getName());
        job2.getCounters().findCounter(BayesCounter.NegativeCounter).setValue(
            job.getCounters().findCounter(BayesCounter.NegativeCounter).getValue()
        );
        job2.getCounters().findCounter(BayesCounter.PositiveCounter).setValue(
            job.getCounters().findCounter(BayesCounter.PositiveCounter).getValue()
        );
        job2.getCounters().findCounter(BayesCounter.PositiveDocument).setValue(
            job.getCounters().findCounter(BayesCounter.PositiveDocument).getValue()
        );
        job2.getCounters().findCounter(BayesCounter.NegativeDocument).setValue(
            job.getCounters().findCounter(BayesCounter.NegativeDocument).getValue()
        );
        job2.getCounters().findCounter(BayesCounter.UniqueTokenCounter).setValue(
            job.getCounters().findCounter(TaskCounter.REDUCE_OUTPUT_RECORDS).getValue()
        );

        job2.setJarByClass(this.getClass());
        FileInputFormat.addInputPath(job2, new Path(args[1]));
        FileOutputFormat.setOutputPath(job2, new Path(args[2]));
        job2.setMapperClass(BayesMapper.class);
        job2.setCombinerClass(BayesReducer.class);
        job2.setReducerClass(BayesReducer.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        return job2.waitForCompletion(true) ? 0 : 1;
    }
}
