package it.cnr.aoup.ninia.anomalydetection.main.anomalyanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;
import it.cnr.aoup.ninia.anomalydetection.utils.UtilsVectors;
import it.cnr.aoup.ninia.toneunitsegmentation.LAB2Audio;

public class ToneUnitAnomalyAnalyser {

	public List<Double> TUanomalyPercs = new ArrayList<Double>();
	public List<String> TUanomalyAnnotations = new ArrayList<String>();
	public List<Boolean> featureAnomaly = new ArrayList<Boolean>();
	public List<Double> nonAnomalousFeatureTimes = new ArrayList<Double>();
	public List<double[]> non_anomalous_features = new ArrayList<double[]>();
	
	public File labFileOut;
	public File labFileOutAnnotations;
	public File featureOutFile;
	
	public File evaluateAnomalyLevel(File labFile, List<Double> anomalousTimes, List<Boolean> anomalousIndices, List<double[]> allFeatures) throws Exception{
		
		int nToneUnits = LAB2Audio.getNumberofLabels(labFile);
		int nFrames = anomalousTimes.size();
		
		System.out.println("Analysing "+nToneUnits+" tone units");

		List<double[]> allTUranges = LAB2Audio.getTimeRanges(labFile);
		List<String> allTUlabels = LAB2Audio.getLabels(labFile);
		
		TUanomalyPercs = new ArrayList<Double>();
		TUanomalyAnnotations = new ArrayList<String>();
		featureAnomaly = new ArrayList<Boolean>();
		int k=0;
		double threshold = Double.parseDouble(ConfigManager.getProperty("vae_noise_removal_percentage"));
		int tuK = 0;
		for (double [] tuRange:allTUranges) {
			String label = allTUlabels.get(tuK);
			//if (label.equals("-"))
				//continue;
			
			boolean inrange = true;
			double tf = tuRange[1];
			int anomalies = 0;
			int allframes = 0;
			int fk = 0;
			while (inrange) {
				
				if (k>=nFrames || anomalousTimes.get(k)>tf) {
					//System.out.println("T1:"+tf+" Tf"+anomalousTimes.get(k));
					inrange = false;
				}else {
					if (anomalousIndices.get(k)) {
						anomalies++;
					}					
					allframes++;
					k++;
					fk++;
				}
				
			}
			double anomalyPerc = ((int) ((double)anomalies*100d/(double) allframes)*100d)/100d;
			TUanomalyPercs.add(anomalyPerc);
			//manage not anomalous (below percentage of anomalies
			if (anomalyPerc<threshold) {
				TUanomalyAnnotations.add(""+anomalyPerc);
				for (int g=0;g<fk;g++)
					featureAnomaly.add(false);
			}else {
				for (int g=0;g<fk;g++)
					featureAnomaly.add(true);
				TUanomalyAnnotations.add("-");
				
			}
			tuK++;	
		}
		
		
		 labFileOut = new File(labFile.getAbsolutePath().replace(".lab", "_anomaly_level.lab"));
		 labFileOutAnnotations = new File(labFile.getAbsolutePath().replace(".lab", "_anomalies_deleted.lab"));
		 featureOutFile = new File(labFile.getAbsolutePath().replace(".lab", "_non_anomalous_vectors.csv"));
		System.out.println("Reporting tone unit anomaly levels in "+labFileOut.getAbsolutePath());
		
		savePercentagesToFile(TUanomalyPercs, allTUranges, labFileOut);
		saveAnnotationsToFile(TUanomalyAnnotations,allTUranges,labFileOutAnnotations);
		
		
		int h = 0;
		for (double[] features:allFeatures) {
			
			if (!featureAnomaly.get(h)) { 
				non_anomalous_features.add(features);
				nonAnomalousFeatureTimes.add(anomalousTimes.get(h));
			}
			h++;
		}
		
		UtilsVectors.saveFeaturesToFile(non_anomalous_features, featureOutFile, true);
		
		return labFileOut;
		
	}
	
	

public File reEvaluateAnomalyLevel(File labFile, List<Double> anomalousTimes, List<Boolean> anomalousIndices, List<double[]> allFeatures) throws Exception{
		
		int nToneUnits = LAB2Audio.getNumberofLabels(labFile);
		int nFrames = anomalousTimes.size();
		
		System.out.println("Analysing "+nToneUnits+" tone units");

		List<double[]> allTUranges = LAB2Audio.getTimeRanges(labFile);
		List<String> allTUlabels = LAB2Audio.getLabels(labFile);
		
		TUanomalyPercs = new ArrayList<Double>();
		TUanomalyAnnotations = new ArrayList<String>();
		featureAnomaly = new ArrayList<Boolean>();
		int k=0;
		double threshold_min = Double.parseDouble(ConfigManager.getProperty("vae_anomaly_removal_percentage_min"));
		double threshold_max = Double.parseDouble(ConfigManager.getProperty("vae_anomaly_removal_percentage_max"));
		double min_duration = Double.parseDouble(ConfigManager.getProperty("vae_anomaly_removal_min_duration"));
		int tuK = 0;
		
		for (double [] tuRange:allTUranges) {
			String label = allTUlabels.get(tuK);
			double duration = tuRange[1]-tuRange[0];
			
			
			boolean inrange = true;
			double tf = tuRange[1];
			int anomalies = 0;
			int allframes = 0;
			int fk = 0;
			while (inrange) {
				
				if (k>=nFrames || anomalousTimes.get(k)>tf) {
					//System.out.println("T1:"+tf+" Tf"+anomalousTimes.get(k));
					inrange = false;
				}else {
					if (anomalousIndices.get(k)) {
						anomalies++;
					}					
					allframes++;
					k++;
					fk++;
				}
				
			}
			double anomalyPerc = ((int) ((double)anomalies*100d/(double) allframes)*100d)/100d;
			if (label.equals("-") || (duration<min_duration)) {
				anomalyPerc = 100;
			}
			
			TUanomalyPercs.add(anomalyPerc);
			if ((anomalyPerc>threshold_min) && (anomalyPerc<threshold_max)) {
				TUanomalyAnnotations.add(""+anomalyPerc);
				for (int g=0;g<fk;g++)
					featureAnomaly.add(false);
			}else {
				for (int g=0;g<fk;g++)
					featureAnomaly.add(true);
				TUanomalyAnnotations.add("-");
				
			}
			
			tuK++;	
		}
		
		
		 labFileOut = new File(labFile.getAbsolutePath().replace(".lab", "_anomaly_level.lab"));
		 labFileOutAnnotations = new File(labFile.getAbsolutePath().replace(".lab", "_anomalies_deleted.lab"));
		 featureOutFile = new File(labFile.getAbsolutePath().replace(".lab", "_non_anomalous_vectors.csv"));
		System.out.println("Reporting tone unit anomaly levels in "+labFileOut.getAbsolutePath());
		
		savePercentagesToFile(TUanomalyPercs, allTUranges, labFileOut);
		saveGoodAnnotationsToFile(TUanomalyAnnotations,allTUranges,labFileOutAnnotations);
		
		
		int h = 0;
		for (double[] features:allFeatures) {
			
			if (!featureAnomaly.get(h)) { 
				non_anomalous_features.add(features);
				nonAnomalousFeatureTimes.add(anomalousTimes.get(h));
			}
			h++;
		}
		
		UtilsVectors.saveFeaturesToFile(non_anomalous_features, featureOutFile, true);
		
		return labFileOut;
		
	}

	public void savePercentagesToFile(List<Double> allTUanomalyPercs,List<double[]> allTUranges, File labFileOut) throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter(labFileOut));
		int i =0;
		for (double[] turange:allTUranges) {
			String toWrite = turange[0]+" "+turange[1]+" "+allTUanomalyPercs.get(i)+"\n";
			bw.append(toWrite);
			i++;
		}
		
		bw.close();
			
	}
	
	public void saveAnnotationsToFile(List<String> allTUanomalyPercs,List<double[]> allTUranges, File labFileOut) throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter(labFileOut));
		int i =0;
		for (double[] turange:allTUranges) {
			String toWrite = turange[0]+" "+turange[1]+" "+allTUanomalyPercs.get(i)+"\n";
			bw.append(toWrite);
			i++;
		}
		
		bw.close();
			
	}
	
	public void saveGoodAnnotationsToFile(List<String> allTUanomalyPercs,List<double[]> allTUranges, File labFileOut) throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter(labFileOut));
		int i =0;
		for (double[] turange:allTUranges) {
			if (!allTUanomalyPercs.get(i).equals("-")) {
				String toWrite = turange[0]+" "+turange[1]+" "+allTUanomalyPercs.get(i)+"\n";
				bw.append(toWrite);
			}
			i++;
		}
		
		bw.close();
			
	}
	
	
}
