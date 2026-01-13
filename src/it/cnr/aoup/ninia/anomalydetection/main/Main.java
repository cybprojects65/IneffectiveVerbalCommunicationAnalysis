package it.cnr.aoup.ninia.anomalydetection.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.cnr.aoup.ninia.anomalydetection.main.anomalyanalysis.ToneUnitAnomalyAnalyser;
import it.cnr.aoup.ninia.anomalydetection.main.anomalyanalysis.VaeManager;
import it.cnr.aoup.ninia.anomalydetection.utils.Cache;
import it.cnr.aoup.ninia.anomalydetection.utils.UtilsVectors;
import it.cnr.aoup.ninia.featureextraction.EnergyPitchManager;
import it.cnr.aoup.ninia.featureextraction.ModulationSpectrogramManager;
import it.cnr.aoup.ninia.toneunitsegmentation.LAB2Audio;
import it.cnr.aoup.ninia.toneunitsegmentation.ToneUnitSegmentationManager;

public class Main {

	public static void main(String[] args) throws Exception {
		//TODO: include/exclude modulation spectrogram in the anomaly detection 
		//TODO: get energy and pitch automatically for anomaly detection
		//TODO: manage absence of delta and double deltas
		//TODO: merge adjacent ranges in the transcriptions
		//TODO: validation 

		
		//Step 1: I/O File definition
		File testFile = new File("./Corpus_16kHz/PS16Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS15Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS14Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS13Audio.wav");
		
		//File testFile = new File("./Corpus_16kHz/PS12Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS7Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS5Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS4Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS3Audio.wav");
		//File testFile = new File("./Corpus_16kHz/PS1Audio.wav");
		
		File energypitchfeaturesFileCache = new File(
				testFile.getAbsolutePath().replace(".wav", "_energy_pitch_features.ser"));
		File msfeaturesFileCache = new File(
				testFile.getAbsolutePath().replace(".wav", "_modulation_spectrogram_features.ser"));
		File allfeaturesFileCache = new File(testFile.getAbsolutePath().replace(".wav", "_aligned_features.ser"));

		File vaeOutputFolder = new File("./vae_" + testFile.getName().replace(".wav", "") + "_"
				+ ConfigManager.getProperty("vae_training_epochs") + "e" + "_"
				+ ConfigManager.getProperty("vae_hidden_nodes_first_layer") + "h");
		
		File gold_standard = new File(
				testFile.getAbsolutePath().replace(".wav", "_gold.lab"));
		
		if (!vaeOutputFolder.exists())
			vaeOutputFolder.mkdir();
		
		
		File energypitchfeaturesFile = new File(
				testFile.getAbsolutePath().replace(".wav", "_energy_pitch_features.csv"));
		File msfeaturesFile = new File(
				testFile.getAbsolutePath().replace(".wav", "_modulation_spectrogram_features.csv"));
		File allfeaturesFile = new File(testFile.getAbsolutePath().replace(".wav", "_aligned_features.csv"));
		
		boolean use_deltas_energy_pitch = Boolean
				.parseBoolean(ConfigManager.getProperty("use_deltas_energy_pitch"));
		boolean use_doubledeltas_energy_pitch = Boolean
				.parseBoolean(ConfigManager.getProperty("use_doubledeltas_energy_pitch"));
		boolean use_deltas_modulation_spectrogram = Boolean
				.parseBoolean(ConfigManager.getProperty("use_deltas_modulation_spectrogram"));
		boolean use_doubledeltas_modulation_spectrogram = Boolean
				.parseBoolean(ConfigManager.getProperty("use_doubledeltas_modulation_spectrogram"));
		
		//Step 2: feature definition and collection
		List<double[]> energyPitchFeatures = Cache.getCachedFeatures(energypitchfeaturesFileCache);
		List<double[]> msFeatures = Cache.getCachedFeatures(msfeaturesFileCache);
		List<double[]> allFeatures = Cache.getCachedFeatures(allfeaturesFileCache);
		
		if (energyPitchFeatures == null || msFeatures == null || allFeatures == null) {
			
			EnergyPitchManager epm = new EnergyPitchManager();
			energyPitchFeatures = epm.extractEnergyPitch(testFile, use_deltas_energy_pitch,
					use_doubledeltas_energy_pitch);

			UtilsVectors.saveFeaturesToFile(energyPitchFeatures, energypitchfeaturesFile, true);
			Cache.cacheFeatures(energyPitchFeatures, energypitchfeaturesFileCache, true);

			ModulationSpectrogramManager msm = new ModulationSpectrogramManager();
			msFeatures = msm.extractModulationSpectrogram(testFile, use_deltas_modulation_spectrogram,
					use_doubledeltas_modulation_spectrogram);

			UtilsVectors.saveFeaturesToFile(msFeatures, msfeaturesFile, true);
			Cache.cacheFeatures(msFeatures, msfeaturesFileCache, true);

			allFeatures = ModulationSpectrogramManager.alignToEnergyPitch(energyPitchFeatures, msFeatures);

			UtilsVectors.saveFeaturesToFile(allFeatures, allfeaturesFile, true);
			Cache.cacheFeatures(allFeatures, allfeaturesFileCache, true);
		}

		int ms_1 = -1;
		int ms_2 = -1;
		int ms_d1 = -1;
		int ms_d2 = -1;
		int ms_dd1 = -1;
		int ms_dd2 = -1;
		
		if (msFeatures.size()>0) {
			ms_1=0;
			if (!use_deltas_modulation_spectrogram && !use_doubledeltas_modulation_spectrogram) {
				ms_2 = msFeatures.get(0).length-1;
			}
			else if (use_deltas_modulation_spectrogram && !use_doubledeltas_modulation_spectrogram){
				ms_2 = (msFeatures.get(0).length/2)-1;
				ms_d1 = (msFeatures.get(0).length/2);
				ms_d2 = msFeatures.get(0).length-1;
			}else {
				ms_2 = (msFeatures.get(0).length/3)-1;
				ms_d1 = (msFeatures.get(0).length/3);
				ms_d2 = (2*msFeatures.get(0).length/3)-1;
				ms_dd1 = (2*msFeatures.get(0).length/3); 
				ms_dd2 = msFeatures.get(0).length-1;
			}
		}
		int ep_1 = -1;
		int ep_2 = -1;
		int ep_d1 = -1;
		int ep_d2 = -1;
		int ep_dd1 = -1;
		int ep_dd2 = -1;
		
		if (energyPitchFeatures.size()>0) {
			ep_1=Math.max(ms_2, Math.max(ms_d2,ms_dd2))+1;
			
			if (!use_deltas_energy_pitch && !use_doubledeltas_energy_pitch) {
				ep_2=ep_1+energyPitchFeatures.size()-1;
			}
			else if (use_deltas_energy_pitch && !use_doubledeltas_energy_pitch){
				ep_2 = ep_1 + (energyPitchFeatures.get(0).length/2)-1;
				ep_d1 = ep_1 + (energyPitchFeatures.get(0).length/2);
				ep_d2 = ep_1 + energyPitchFeatures.get(0).length-1;
			}else {
				ep_2 = ep_1 + (energyPitchFeatures.get(0).length/3)-1;
				ep_d1 = ep_1 + (energyPitchFeatures.get(0).length/3);
				ep_d2 = ep_1 + (2*energyPitchFeatures.get(0).length/3)-1;
				ep_dd1 = ep_1 + (2*energyPitchFeatures.get(0).length/3); 
				ep_dd2 = ep_1 + energyPitchFeatures.get(0).length-1;
			}
		}
		//Features to use to detect anomalies in the speech segments identified by the first passage
		String features = "";
		int featureset = Integer.parseInt(ConfigManager.getProperty("feature_set_for_speech_anomaly"));
		int f1 = -1;
		int f2 = -1;
		
		if (featureset==1) {
		//Energy
		
		f1 = ep_1;
		f2 = ep_1;
		
		}else if (featureset==2) {
		//Pitch
		
		f1 = ep_2;
		f2 = ep_2;
		
		}else if (featureset==3) {
		//Energy + Pitch
		
		f1 = ep_1;
		f2 = ep_2;
		
		}else if (featureset==4) {
		
		//Energy + Pitch + Deltas
		
		f1 = ep_1;
		f2 = ep_d2;
			
		}else if (featureset==5) {
		//Energy + Pitch + Deltas + Double Deltas
		
		f1 = ep_1;
		f2 = ep_dd2;
		}else if (featureset==6) {

		//Modulation Spectrogram
		
		f1 = ms_1;
		f2 = ms_2;
		}else if (featureset==7) {
			
		
		//Modulation Spectrogram + Deltas

		f1 = ms_1;
		f2 = ms_d2;
		}else if (featureset==8) {

		
		//Modulation Spectrogram + Deltas + Double Deltas
		
		f1 = ms_1;
		f2 = ms_dd2;
		}else if (featureset==9) {
		
		//Modulation Spectrogram + Energy + Pitch
		features = "F0,F1,F2,F3,F4,F5,F6,F7,F24,F25";
		}else if (featureset==10) {
		//Modulation Spectrogram + Energy + Pitch + Deltas
		features = "F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F24,F25,F26,F27";
		}else if (featureset==11) {
		
		//Modulation Spectrogram + Energy + Pitch + Deltas + Double Deltas
				
		f1 = ms_1;
		f2 = ep_dd2;
		}

		
		File vaeDeepOutputFolder = new File("./vae_deep_" + testFile.getName().replace(".wav", "") + "_"
				+ ConfigManager.getProperty("vae_training_epochs") + "e" + "_"
				+ ConfigManager.getProperty("vae_hidden_nodes_first_layer") + "h" + "_F"+f1+"-F"+f2);
		
		if (features.length()>0)
			vaeDeepOutputFolder = new File("./vae_deep_" + testFile.getName().replace(".wav", "") + "_"
					+ ConfigManager.getProperty("vae_training_epochs") + "e" + "_"
					+ ConfigManager.getProperty("vae_hidden_nodes_first_layer") + "h" + "_"+features.replace(",", "_"));
			
		if (!vaeDeepOutputFolder.exists())
			vaeDeepOutputFolder.mkdir();
		
		System.out.println("VAE folder to search in the cache: "+vaeDeepOutputFolder.getName());
		
		System.out.println("modulation spectrogram indexes:");
		System.out.println("MS: "+ms_1+"-"+ms_2);
		System.out.println("MS DELTAS: "+ms_d1+"-"+ms_d2);
		System.out.println("MS D-DELTAS: "+ms_dd1+"-"+ms_dd2);
		
		System.out.println("EP: "+ep_1+"-"+ep_2);
		System.out.println("EP DELTAS: "+ep_d1+"-"+ep_d2);
		System.out.println("EP D-DELTAS: "+ep_dd1+"-"+ep_dd2);
		
		//Step 3: anomaly detection to filter out non-speech segments: use all features!
		VaeManager vaeman = new VaeManager();
		File reconstruction = vaeman.anomalyDetection(allfeaturesFile, vaeOutputFolder);
		
		//Step 4: detect anomalous vectors as those with the lowest (<25th perc) reconstruction probability
		System.out.println("Detecting anomalies out of the VAE");
		List<Boolean> anomalousIndices = VaeManager.detectAnomalies(reconstruction);
		
		//Step 5: calculate the time of each vector, including the anomalous ones 
		System.out.println("Assigning times to the anomalies");		
		List<Double> anomalousTimes = new ArrayList<Double>();
		for (int i = 0;i<anomalousIndices.size();i++) {
			double time = ModulationSpectrogramManager.getTime(i);
			anomalousTimes.add(time);
		}

		//Step 6: detect the tone units within the audio
		System.out.println("Detecting tone units");
		ToneUnitSegmentationManager tum = new ToneUnitSegmentationManager(); 
		File labfile = tum.detectToneUnits(testFile);
		System.out.println("Tone units reported in "+labfile.getAbsolutePath());
		System.out.println("Average Tone unit duration (s) "+LAB2Audio.calcAvgAnnotationDurationInSec(labfile));
		
		//Step 7: calculate the level of anomality of each tone unit as the ratio between anomalous and non-anomalous vectors; then compare with vae_noise_removal_percentage to classify TUs
		System.out.println("Assessing tone unit anomaly level");
		ToneUnitAnomalyAnalyser tuaa = new ToneUnitAnomalyAnalyser();
		tuaa.evaluateAnomalyLevel(labfile, anomalousTimes, anomalousIndices,allFeatures);
		
		//labFileOutAnnotations
		double totalLengthSpeechSegments = LAB2Audio.calcTotalAnnotationDurationInSec(tuaa.labFileOutAnnotations);
		//Step 8: execute the anomaly detection on the non-anomalous vectors saved from the previous step in featureOutFile
		System.out.println("Detecting voiced anomalies out of the VAE");
		VaeManager vaemandeep = new VaeManager();
		//File reconstructiondeep = vaemandeep.anomalyDetection(tuaa.featureOutFile, vaeDeepOutputFolder);
		//File reconstructiondeep = vaemandeep.anomalyDetectionEnergyPitch(tuaa.featureOutFile, vaeDeepOutputFolder);
		//File reconstructiondeep = vaemandeep.anomalyDetectionCustomIndex(tuaa.featureOutFile, vaeDeepOutputFolder,ep_1,ep_2); //optimal
		File reconstructiondeep = null;
		if (features.length()==0)
			reconstructiondeep = vaemandeep.anomalyDetectionCustomIndex(tuaa.featureOutFile, vaeDeepOutputFolder,f1,f2);
		else 
			reconstructiondeep = vaemandeep.anomalyDetectionCustomFeatures(tuaa.featureOutFile, vaeDeepOutputFolder,features);
		
		//Step 9: detect anomalous vectors
		System.out.println("Re-Detecting voiced anomalies out of the VAE");
		List<Boolean> anomalousIndicesVoiced = VaeManager.detectAnomalies(reconstructiondeep);
		System.out.println("Re-Assigning times to the voiced anomalies");		
		
		//Step 10: anomalous vectors will now need a minimum duration and an anomalous level within a range - output is written in _anomalies_deleted.lab
		List<Double> anomalousVoicedTimes = tuaa.nonAnomalousFeatureTimes;
		ToneUnitAnomalyAnalyser tuaaDeep = new ToneUnitAnomalyAnalyser();
		tuaaDeep.reEvaluateAnomalyLevel(tuaa.labFileOutAnnotations, anomalousVoicedTimes, anomalousIndicesVoiced,tuaa.non_anomalous_features);
		//tuaaDeep.labFileOutAnnotations
		
		
		double totalLengthIneffectiveSegments = LAB2Audio.calcTotalAnnotationDurationInSec(tuaaDeep.labFileOutAnnotations);
		double qualityScore = 1-totalLengthIneffectiveSegments/totalLengthSpeechSegments;
		
		System.out.println("Average Speech Tone unit duration (s) "+LAB2Audio.calcAvgAnnotationDurationInSecE(tuaa.labFileOutAnnotations));
		System.out.println("Average Anomalous Tone unit duration (s) "+LAB2Audio.calcAvgAnnotationDurationInSec(tuaaDeep.labFileOutAnnotations));
		System.out.println("Percentage of effective/total speech duration "+qualityScore);
		System.out.println("##INTERPRETATION OF THE QS: "+interpretQS(qualityScore));
		//Step 11: calculate accuracy
		calcAccuracy(gold_standard,tuaaDeep.labFileOutAnnotations);
		System.out.println(LAB2Audio.calcAvgAnnotationDurationInSec(tuaaDeep.labFileOutAnnotations));
		
		//TODO: merge adjacent transcriptions
		
		//List<short[]> tu_signals = LAB2Audio.getSignals(testFile, labfile);
		
	}
	
	
	static double calcAccuracy(File lab1, File lab2) throws Exception {
		if (!lab1.exists()) {
			System.out.println("Cannot calculate accuracy: Lab file "+lab1.getAbsolutePath()+" does not exist.");
			return 0;
		}
		//lab1 is the gold reference, lab2 is the tested transcription
		
		List<String> labels1 = LAB2Audio.getLabels(lab1);
		List<String> labels2 = LAB2Audio.getLabels(lab2);
		List<double[]> times1 = LAB2Audio.getTimeRanges(lab1);
		List<double[]> times2 = LAB2Audio.getTimeRanges(lab2);
		int g = 0;
		int TP = 0;
		int TN = 0;
		int FP = 0;
		int FN = 0;
		double tolerance = 0.05;
		
		for (double[] t1: times1) {
			double t10 = (int)(t1[0]*100)/100d;
			double t11 = (int)(t1[1]*100)/100d;
			String l1 = labels1.get(g);
			String l2 = "";
			int k=0;
			double t20 = -1;
			double t21 = -1;
			boolean found = false;
			for (double[] t2: times2) {
				 t20 = (int)(t2[0]*100)/100d;
				 t21 = (int)(t2[1]*100)/100d;
				 
				 if ( Math.abs(t20-t10)<=tolerance && Math.abs(t21-t11)<=tolerance) {
					 l2 = labels2.get(k);
					 found = true;
					 break;
				 }
				 k++;
			 }
			if (!found) {
				t20 = -1;
				t21 = -1;
				l2 = "n";
			}
			if (l1.contains("x")) {
				double anomalyLevel = -1;
				try {
					anomalyLevel = Double.parseDouble(l2);
					TP++;
				}catch(Exception e) {
					FN++;
				}
			}else {
				{
					double anomalyLevel = -1;
					try {
						anomalyLevel = Double.parseDouble(l2);
						FP++;
					}catch(Exception e) {
						TN++;
					}
				}
			}
			
			 g++;	
		}
		
		System.out.println("TP\t"+TP);
		System.out.println("TN\t"+TN);
		System.out.println("FP\t"+FP);
		System.out.println("FN\t"+FN);
		double accuracy = (double)(TP+TN)*100d/(double)(TP+TN+FP+FN);
		System.out.println("Accuracy\t"+((int)(accuracy*100d)/100d));
		double kappa = cohensKappa(TP,FP,TN,FN);
		System.out.println("Kappa\t"+kappa);
		System.out.println("Summary row:");
		System.out.print(((int)(accuracy*100d)/100d)+"\t"+TP+"\t"+TN+"\t"+FP+"\t"+FN+"\t");
		
		return accuracy;
	}

	public static double cohensKappa(long tp, long fp, long tn, long fn) {
	    double total = tp + fp + tn + fn;

	    if (total == 0) {
	        throw new IllegalArgumentException("Total number of samples is zero");
	    }

	    // Observed agreement
	    double po = (tp + tn) / total;

	    // Expected agreement
	    double pe = (
	            ((tp + fp) * (tp + fn)) +
	            ((fn + tn) * (fp + tn))
	    ) / (total * total);

	    // Avoid division by zero when pe == 1
	    if (pe == 1.0) {
	        return 1.0;
	    }

	    return (po - pe) / (1.0 - pe);
	}

	
	public static String interpretQS(double qs) {
		double q1=Double.parseDouble(ConfigManager.getProperty("qualityScore25thperc"));
		double q2=Double.parseDouble(ConfigManager.getProperty("qualityScore50thperc"));
		double q3=Double.parseDouble(ConfigManager.getProperty("qualityScore75thperc"));
		if (qs<q1)
			return "low";
		else if (qs<q2)
			return "medium-low";
		else if (qs<q3)
			return "medium-high";
		else 
			return "high";
		
	}
	
}
