package it.cnr.aoup.ninia.toneunitsegmentation;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;
import it.cnr.speech.toneunit.ToneUnitMarker;

//calls the TUM on the current audio file
public class ToneUnitSegmentationManager {

	//public static String executeTUM = "java -cp ./tum.jar it.cnr.speech.toneunit.ToneUnitMarker -i./PS1Audio.wav -w0.2 -e60 -p0 -t1 -L30 -Strue -F\"./output_of_segmentation\"";
	/*		
	-i: path to the input audio file
	-m: minimum number of tone unit to search for (=3)
	-t: maximum number of iterations (=100)
	-w: analysis window in seconds (=0.2)
	-e: energy threshold loss to set a marker (percent) (=90.0)
	-p: quartile of the energy distribution to use as the minimum energy threshold (0=25th percentile; 1=50th; 2=75th; 3=100th)
	-o: output file (.LAB) to write (=<audiofilename>.lab)
	-s: start time (in seconds) of  the analysis on the input file (=-1 entire file)
	-d: end time (in seconds) of the analysis on the input file (=-1 entire file)
	-L: maximum length of the Tone Unit in s (suggested value=30) - This an alternative to the search for the minimum number of tone units
	-S: option to save or not the detected TUs when using maximum length search (default=true)
	-F: The folder in which the TUs will be saved when in max-length mode
	-h: help
	*/
	public File detectToneUnits(File input) throws Exception{
		File labfile = new File(input.getAbsolutePath().replace(".wav", ".lab"));
		if (labfile.exists())
			return labfile;
		
		double tone_unit_analysis_window = Double.parseDouble(ConfigManager.getProperty("tone_unit_analysis_window"));
		double tone_unit_energy_loss = Double.parseDouble(ConfigManager.getProperty("tone_unit_energy_loss"));
		double tone_unit_energy_quartile = Double.parseDouble(ConfigManager.getProperty("tone_unit_energy_quartile"));
		int tone_unit_minimum_to_search = Integer.parseInt(ConfigManager.getProperty("tone_unit_minimum_to_search"));
		int tone_unit_max_iterations = Integer.parseInt(ConfigManager.getProperty("tone_unit_max_iterations"));
		/*
		String tone_unit_folder = ConfigManager.getProperty("tone_unit_folder");
		File initial_tu_folder = new File(new File(tone_unit_folder).getAbsolutePath());
		if (!initial_tu_folder.exists()) {
			Files.createDirectories(initial_tu_folder.toPath());
			System.out.println("Main folder "+initial_tu_folder.getAbsolutePath()+" did not exist. Creating it.");
		}
		
		tone_unit_folder=new File(new File(tone_unit_folder), "tus_"+UUID.randomUUID()).getAbsolutePath();
		
		if (!new File(tone_unit_folder).exists()) {
			new File(tone_unit_folder).mkdir();
		}
		System.out.println("Folder of the execution "+tone_unit_folder);
		*/
		
		String [] args = {
				"-i"+input.getAbsolutePath(),
				"-w"+tone_unit_analysis_window,
				"-e"+tone_unit_energy_loss,
				"-p"+tone_unit_energy_quartile,
				"-t"+tone_unit_max_iterations,
				"-m"+tone_unit_minimum_to_search //,
				//"-Strue",
				//"-F"+tone_unit_folder,
		};		
		ToneUnitMarker.main(args);
		
		//File tone_unit_folderOutput = new File(tone_unit_folder);
		
		
		//return tone_unit_folderOutput.listFiles();
		return labfile;
	}
	
}
