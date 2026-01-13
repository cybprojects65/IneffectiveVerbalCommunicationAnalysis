package it.cnr.aoup.ninia.featureextraction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;
import it.cnr.aoup.ninia.anomalydetection.utils.UtilsVectors;
import it.cnr.speech.filters.ModulationSpectrogram;
import it.cnr.workflow.utils.UtilsVectorMatrix;

public class ModulationSpectrogramManager {

	ModulationSpectrogram ms = new ModulationSpectrogram();
	public List<double[]> extractModulationSpectrogram(File entireSignal, boolean delta, boolean doubledelta) throws Exception{
		
		List<double[]> features = new ArrayList<double[]>();
		
		boolean saturate = true;
		File output = null;
		boolean addDeltas=false;
				
		int modulation_spectrogram_nfeatures = Integer.parseInt(ConfigManager.getProperty("modulation_spectrogram_nfeatures"));
		double modulation_spectrogram_max_frequency = Double.parseDouble(ConfigManager.getProperty("modulation_spectrogram_max_frequency"));;
		
		System.out.println("Calculating Modulation Spectrogram with "+modulation_spectrogram_nfeatures+" features and a max frequency of "+modulation_spectrogram_max_frequency);
		ms.calcMS(entireSignal, output , saturate, addDeltas, modulation_spectrogram_nfeatures, modulation_spectrogram_max_frequency);
		double [][] mspec = UtilsVectorMatrix.traspose(ms.modulationSpectrogram);
		int nfeat = 1;
		double [][] mspecdelta = null;
		double [][] mspecdeltadelta = null;
		
		System.out.println("Modulation Spectrogram matrix has size "+mspec.length+" X "+mspec[0].length);
		
		if (delta) {
			System.out.println("Adding deltas");
			mspecdelta = UtilsVectors.computeDeltaMatrix(mspec);
			System.out.println("Added deltas "+mspecdelta.length+" X "+mspecdelta[0].length);
			nfeat++;
		}
		
		if (doubledelta) {
			System.out.println("Adding double deltas");
			mspecdeltadelta = UtilsVectors.computeDoubleDeltaMatrix(mspec);
			System.out.println("Added double deltas "+mspecdeltadelta.length+" X "+mspecdeltadelta[0].length);
			nfeat++;
		}
		
		System.out.println("Building feature sequence");
		for (int i=0;i<mspec.length;i++) {
			int j = 0;
			double [] featureRow = new double[nfeat*modulation_spectrogram_nfeatures];
			System.arraycopy(mspec[i], 0, featureRow, 0, modulation_spectrogram_nfeatures);
			j += modulation_spectrogram_nfeatures;
			if (delta) {
				System.arraycopy(mspecdelta[i], 0, featureRow, j, modulation_spectrogram_nfeatures);
				j += modulation_spectrogram_nfeatures;
			}
			if (doubledelta) {
				System.arraycopy(mspecdeltadelta[i], 0, featureRow, j, modulation_spectrogram_nfeatures);
				j += modulation_spectrogram_nfeatures;
			}
			features.add(featureRow);
		}
		
		System.out.println("Feature list complete. Size "+mspec.length+" X "+(nfeat*modulation_spectrogram_nfeatures));
		
		return features;
	}
	
	public static double getTime (int index) {
		return ((double)index)*ModulationSpectrogram.windowShift;
	}
	
	
	public static List<double[]> alignToEnergyPitch(List<double[]> energyPitchFeatures, List<double[]> msFeatures) throws Exception{
		System.out.println("Aligning Modulation Spectrogram to Energy and Pitch");
		List<double[]> mergedFeatures = new ArrayList<>();
		double[] sampleEpFeatures = energyPitchFeatures.get(0);
		int i=0;
		int epindex = 0;
		
		for (double[] msfeature:msFeatures) {
			double [] newfeatures = new double[msfeature.length+sampleEpFeatures.length];
			System.arraycopy(msfeature, 0, newfeatures, 0, msfeature.length);
			double timeMs= ModulationSpectrogramManager.getTime(i);
			
			double timeEp= EnergyPitchManager.getTime(epindex);
			
			if (timeMs>timeEp) {
				epindex++;
			}
			timeEp= EnergyPitchManager.getTime(epindex);
			
			//System.out.println(timeMs+"s<"+timeEp+"s");
			if (timeMs>timeEp)
				throw new Exception("Inconsistent times");
			
			double[] epFeature = energyPitchFeatures.get(epindex);
			System.arraycopy(epFeature, 0, newfeatures, msfeature.length, epFeature.length);
			mergedFeatures.add(newfeatures);
			i++;
		}
		
		System.out.println("Alignment done.");
		return mergedFeatures;
	}
}
