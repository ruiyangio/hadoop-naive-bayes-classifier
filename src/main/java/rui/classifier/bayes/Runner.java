package rui.classifier.bayes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.util.ToolRunner;

import rui.classifier.bayes.Util;

public class Runner {
    public static void main(String[] args) {
        try {
            Boolean doEvaluation = false;
            for (String arg : args) {
                if ("-eval".equals(arg)) {
                    doEvaluation = true;
                }
            }

            if (doEvaluation) {
                Double accuracy = Util.evaluateAccuracy(args[0], args[1]);
                System.out.println(accuracy);
            }
            else {
                int exitCode = ToolRunner.run(new BayesDriver(), args);
                System.exit(exitCode);
            }
        } catch (Exception ex) {
            Logger.getLogger(BayesDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
