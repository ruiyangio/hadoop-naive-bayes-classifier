package rui.classifier.bayes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.util.ToolRunner;

public class Runner {
    public static void main(String[] args) {
        try {
            int exitCode = ToolRunner.run(new BayesDriver(), args);
            System.exit(exitCode);
        } catch (Exception ex) {
            Logger.getLogger(BayesDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
