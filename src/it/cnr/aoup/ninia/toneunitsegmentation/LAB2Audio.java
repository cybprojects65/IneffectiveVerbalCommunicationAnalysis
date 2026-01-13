package it.cnr.aoup.ninia.toneunitsegmentation;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;
import it.cnr.speech.audiofeatures.AudioBits;

public class LAB2Audio {

	public static List<short[]> getSignals(File audio,File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		//read the audio file
		AudioBits audiobits = new AudioBits(audio);
		short [] entireSignal = audiobits.getShortVectorAudio();
		float sfrequency = audiobits.getAudioFormat().getSampleRate();
		List<short[]> subSignals = new ArrayList<>();
		
		for (String label:labs) {
			String [] labels = label.split(" ");
			double t0 = Double.parseDouble(labels[0]);
			double t1 = Double.parseDouble(labels[1]);
			int i0 = (int) (t0 * sfrequency);
			int i1 = Math.min((int) (t1 * sfrequency),(entireSignal.length-1));
			//extract the audio slices from the file
			short[] subsignal = new short[i1 - i0 + 1];
			for (int k = i0; k <= i1; k++) {
				subsignal[k - i0] = entireSignal[k];			
			}
			//accumulate the slices
			subSignals.add(subsignal);
		}
		
		return subSignals;
		
	}
	
	public static double[] getTime(int index,File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		String label = labs.get(index);
	    String [] labels = label.split(" ");
		double t0 = Double.parseDouble(labels[0]);
		double t1 = Double.parseDouble(labels[1]);
		double range []= {t0,t1};	
		return range;
	}

	public static List<double[]> getTimeRanges(File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		List<double[]> ranges = new ArrayList<double[]>();
		int maxLabs = labs.size();
		for (int i=0;i<maxLabs;i++) {
			
		String label = labs.get(i);
	    String [] labels = label.split(" ");
		double t0 = Double.parseDouble(labels[0]);
		double t1 = Double.parseDouble(labels[1]);
		double range []= {t0,t1};	
		ranges.add(range);
		}
		
		return ranges;
	}

	public static List<String> getLabels(File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		List<String> labelscollected = new ArrayList<String>();
		
		int maxLabs = labs.size();
		for (int i=0;i<maxLabs;i++) {
			
		String label = labs.get(i);
	    String [] labels = label.split(" ");
		String l = labels[2];
		labelscollected.add(l);
		}
		
		return labelscollected;
	}

	public static int getNumberofLabels(File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		return labs.size();
		
	}
	
	public static double calcAvgAnnotationDurationInSec(File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		List<double[]> ranges = new ArrayList<double[]>();
		int maxLabs = labs.size();
		double totalduration = 0;
		for (int i=0;i<maxLabs;i++) {
				String label = labs.get(i);
			    String [] labels = label.split(" ");
				double t0 = Double.parseDouble(labels[0]);
				double t1 = Double.parseDouble(labels[1]);
				double duration = t1-t0;
				totalduration = totalduration + duration;
		}
		
		return totalduration/(double)maxLabs;
	}

	public static double calcAvgAnnotationDurationInSecE(File lab) throws Exception{
		
		//read the lab file
		List<String> labs=Files.readAllLines(lab.toPath());
		int maxLabs = labs.size();
		int g = 0;
		double min_duration = Double.parseDouble(ConfigManager.getProperty("vae_anomaly_removal_min_duration"));
		double totalduration = 0;
		for (int i=0;i<maxLabs;i++) {
				String label = labs.get(i);
				if (!label.contains("-")) {
			    String [] labels = label.split(" ");
			    
				double t0 = Double.parseDouble(labels[0]);
				double t1 = Double.parseDouble(labels[1]);
				double duration = t1-t0;
				if (duration>=min_duration) {
					totalduration = totalduration + duration;
					g++;
					}
				}
		}
		
		return totalduration/(double) g;
	}

	public static double calcTotalAnnotationDurationInSec(File lab) throws Exception{
		
		List<String> labs=Files.readAllLines(lab.toPath());
		int maxLabs = labs.size();
		int g = 0;
		double min_duration = Double.parseDouble(ConfigManager.getProperty("vae_anomaly_removal_min_duration"));
		double totalduration = 0;
		for (int i=0;i<maxLabs;i++) {
				String label = labs.get(i);
				if (!label.contains("-")) {
			    String [] labels = label.split(" ");
			    
				double t0 = Double.parseDouble(labels[0]);
				double t1 = Double.parseDouble(labels[1]);
				double duration = t1-t0;
				if (duration>=min_duration) {
					totalduration = totalduration + duration;
					g++;
					}
				}
		}
		return totalduration;
	}

}
