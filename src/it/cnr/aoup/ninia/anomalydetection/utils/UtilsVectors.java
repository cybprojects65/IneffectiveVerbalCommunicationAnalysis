package it.cnr.aoup.ninia.anomalydetection.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class UtilsVectors {

	// Compute delta features
	public static double[] computeDelta(double[] feats) {
	    int N = 2;  // typical value
	    double[] delta = new double[feats.length];

	    double denom = 0.0;
	    for (int i = 1; i <= N; i++) {
	        denom += i * i;
	    }
	    denom *= 2;

	    for (int t = 0; t < feats.length; t++) {
	        double numerator = 0.0;

	        for (int n = 1; n <= N; n++) {
	            int tPlus = Math.min(feats.length - 1, t + n);
	            int tMinus = Math.max(0, t - n);
	            numerator += n * (feats[tPlus] - feats[tMinus]);
	        }
	        delta[t] = numerator / denom;
	    }

	    return delta;
	}

	// Compute double delta
	public static double[] computeDoubleDelta(double[] feats) {
	    return computeDelta(computeDelta(feats));
	}

	/**
	 * Computes delta (first derivative) features for a feature matrix.
	 *
	 * @param feats feature matrix [numFrames][numFeatures]
	 * @return delta matrix with same size
	 */
	public static double[][] computeDeltaMatrix(double[][] feats) {
	    int T = feats.length;
	    int D = feats[0].length;
	    int N = 2;  // standard regression window

	    double[][] delta = new double[T][D];

	    double denom = 0.0;
	    for (int i = 1; i <= N; i++) {
	        denom += i * i;
	    }
	    denom *= 2;

	    for (int t = 0; t < T; t++) {
	        for (int d = 0; d < D; d++) {

	            double numerator = 0.0;

	            for (int n = 1; n <= N; n++) {
	                int tPlus = Math.min(T - 1, t + n);
	                int tMinus = Math.max(0, t - n);

	                numerator += n * (feats[tPlus][d] - feats[tMinus][d]);
	            }

	            delta[t][d] = numerator / denom;
	        }
	    }

	    return delta;
	}

	/**
	 * Computes double-delta by applying delta() to the delta matrix.
	 */
	public static double[][] computeDoubleDeltaMatrix(double[][] feats) {
	    return computeDeltaMatrix(computeDeltaMatrix(feats));
	}

	
	public static void saveFeaturesToFile(List<double[]> features, File outputFile, boolean addHeaders) {
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
	    	if (addHeaders) {
	    		// Build CSV header line
	            StringBuilder sbh = new StringBuilder();
	            int nfeatures = features.get(0).length;
	            
	            for (int i = 0; i < nfeatures; i++) {
	                sbh.append("F"+i);
	                if (i < nfeatures - 1) {
	                    sbh.append(",");
	                }
	            }

	            writer.write(sbh.toString());
	            writer.newLine();
	    	}
	    	
	        for (double[] frame : features) {
	            if (frame == null || frame.length == 0) {
	                writer.write("\n");
	                continue;
	            }

	            // Build CSV line
	            StringBuilder sb = new StringBuilder();
	            for (int i = 0; i < frame.length; i++) {
	                sb.append(frame[i]);
	                if (i < frame.length - 1) {
	                    sb.append(",");
	                }
	            }

	            writer.write(sb.toString());
	            writer.newLine();
	        }
	        System.out.println("Features saved to "+outputFile.getAbsolutePath());
	    } catch (IOException e) {
	        throw new RuntimeException("Error writing features to file: " + outputFile, e);
	    }
	}
	
	
}
