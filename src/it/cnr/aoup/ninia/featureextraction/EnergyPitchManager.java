package it.cnr.aoup.ninia.featureextraction;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;
import it.cnr.aoup.ninia.anomalydetection.utils.UtilsVectors;
import it.cnr.speech.audiofeatures.AudioBits;
import it.cnr.speech.audiofeatures.Energy;
import it.cnr.speech.audiofeatures.PitchExtractor;
import it.cnr.speech.filters.ModulationSpectrogram;

public class EnergyPitchManager {

	public List<double[]> extractEnergyPitch(File entireSignal, boolean delta, boolean doubledelta) throws Exception{
		
		List<double []> features = new ArrayList<>();
		double energy_analysis_window = Double.parseDouble(ConfigManager.getProperty("energy_analysis_window"));
		double pitch_analysis_window = Double.parseDouble(ConfigManager.getProperty("pitch_analysis_window"));
		
		System.out.println("Extracting energy with frames of duration "+energy_analysis_window+" s");
		Energy e = new Energy();
		double[] nrg = e.energyCurve((float)energy_analysis_window, entireSignal);
		System.out.println("Energy curve has  "+nrg.length+" samples");
		
		System.out.println("Extracting pitch with frames of duration "+pitch_analysis_window+" s");
		PitchExtractor pitchExtractor = new PitchExtractor();
		pitchExtractor.setPitchWindowSec(pitch_analysis_window);
		pitchExtractor.calculatePitch(entireSignal.getAbsolutePath());
		Double[] pitchCurve = pitchExtractor.pitchCurve;
		double pitch [] = new double[pitchCurve.length];
		int undef = 0;
		for (int i = 0;i<pitchCurve.length;i++) {
			Double p = pitchCurve[i];
			if (p == null) {
				pitch[i]= 0;
				undef++;
			}else
				pitch[i]= p.doubleValue();
		}
		
		System.out.println("Pitch curve has  "+pitch.length+" samples; "+undef+" frames were undefined");
		
		double[] nrgdelta = null;
		double[] pitchdelta = null;
		double[] nrgdeltadelta = null;
		double [] pitchdeltadelta = null;
		
		int nfeat = 1;
		
		if (delta) {
			System.out.println("Extracting deltas");
			nrgdelta = UtilsVectors.computeDelta(nrg);
			pitchdelta = UtilsVectors.computeDelta(pitch);
			System.out.println("Deltas curves for energy and pitch have lengths: "+nrgdelta.length+";"+pitchdelta.length);
			nfeat++;
		}
		
		if (doubledelta) {
			System.out.println("Extracting double deltas");
			nrgdeltadelta = UtilsVectors.computeDoubleDelta(nrg);
			pitchdeltadelta = UtilsVectors.computeDoubleDelta(pitch);
			System.out.println("DDeltas curves for energy and pitch have lengths: "+nrgdeltadelta.length+";"+pitchdeltadelta.length);
			nfeat++;
		}
		
		System.out.println("Building feature list");
		for (int i=0;i<nrg.length;i++) {
			double[] featureVector = new double[nfeat*2];
			int j = 0;
			featureVector[j] = nrg[i];
			j++;
			featureVector[j] = pitch[i];
			j++;
			if (delta) {
				featureVector[j] = nrgdelta[i];
				j++;
				featureVector[j] = pitchdelta[i];
				j++;
			}if (doubledelta) {
				featureVector[j] = nrgdeltadelta[i];
				j++;
				featureVector[j] = pitchdeltadelta[i];
				j++;
			}
			
			features.add(featureVector);
		}
		
		System.out.println("Feature list complete. Size "+nrg.length+" X "+(nfeat*2));
		
		return features;
	}

	
	public static double getTime (int index) {
		double energy_analysis_window = Double.parseDouble(ConfigManager.getProperty("energy_analysis_window"));
		
		return ((double)index)*energy_analysis_window;
	}
	

}
