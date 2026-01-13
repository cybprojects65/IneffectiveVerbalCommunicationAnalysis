package it.cnr.aoup.ninia.anomalydetection.main.anomalyanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.cnr.anomaly.JavaVAEGenerator;
import it.cnr.aoup.ninia.anomalydetection.main.ConfigManager;

public class VaeManager {

	//public List<Boolean> anomalousIndices = new ArrayList<>();
	
	public File anomalyDetection(File allfeaturesFile, File vaeOutputFolder) throws Exception{
		
		File reconstruction = new File(vaeOutputFolder.getAbsolutePath(), "reconstruction.csv");
		if (!reconstruction.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(allfeaturesFile));
		String headerLine = br.readLine();
		br.close();

		String[] vaeArgs = { "-i\"" + allfeaturesFile.getAbsolutePath() + "\"", "-v\"" + headerLine + "\"",
				// "-h"+allFeatures.get(0).length,
				"-h" + ConfigManager.getProperty("vae_hidden_nodes_first_layer"),
				"-e" + ConfigManager.getProperty("vae_training_epochs"), "-o" + vaeOutputFolder.getName() + "",
				"-r16", "-ttrue" };

		System.out.println(
				"Training VAE with the following parameters:" + Arrays.toString(vaeArgs).replace(" ", "\n"));
		// JavaVAE.main(vaeArgs);
		JavaVAEGenerator.main(vaeArgs);
		
		}
		
		return reconstruction;	
	}
	public File anomalyDetectionCustomFeatures(File allfeaturesFile, File vaeOutputFolder, String features) throws Exception{
		File reconstruction = new File(vaeOutputFolder.getAbsolutePath(), "reconstruction.csv");
		if (!reconstruction.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(allfeaturesFile));
		String headerLine = br.readLine();
		br.close();
		
		System.out.println("Applying VAE with the following features: "+features);
		
		String[] vaeArgs = { "-i\"" + allfeaturesFile.getAbsolutePath() + "\"", "-v\"" + features+ "\"",
				// "-h"+allFeatures.get(0).length,
				"-h" + ConfigManager.getProperty("vae_hidden_nodes_first_layer"),
				"-e" + ConfigManager.getProperty("vae_training_epochs"), "-o" + vaeOutputFolder.getName() + "",
				"-r16", "-ttrue" };

		System.out.println(
				"Training VAE with the following parameters:" + Arrays.toString(vaeArgs).replace(" ", "\n"));
		// JavaVAE.main(vaeArgs);
		JavaVAEGenerator.main(vaeArgs);
		
		}
		
		return reconstruction;	
	}
	
	public File anomalyDetectionCustomIndex(File allfeaturesFile, File vaeOutputFolder,int startIdx, int endIdx) throws Exception{
		
		File reconstruction = new File(vaeOutputFolder.getAbsolutePath(), "reconstruction.csv");
		if (!reconstruction.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(allfeaturesFile));
		String headerLine = br.readLine();
		br.close();
		String features = "";
		for (int k=startIdx;k<=endIdx;k++) {
			features+="F"+k;
			if (k<endIdx)
				features+=",";	
		}
		System.out.println("Applying VAE with the following features: "+features);
		
		String[] vaeArgs = { "-i\"" + allfeaturesFile.getAbsolutePath() + "\"", "-v\"" + features+ "\"",
				// "-h"+allFeatures.get(0).length,
				"-h" + ConfigManager.getProperty("vae_hidden_nodes_first_layer"),
				"-e" + ConfigManager.getProperty("vae_training_epochs"), "-o" + vaeOutputFolder.getName() + "",
				"-r16", "-ttrue" };

		System.out.println(
				"Training VAE with the following parameters:" + Arrays.toString(vaeArgs).replace(" ", "\n"));
		// JavaVAE.main(vaeArgs);
		JavaVAEGenerator.main(vaeArgs);
		
		}
		
		return reconstruction;	
	}

	public File anomalyDetectionEnergyPitch(File allfeaturesFile, File vaeOutputFolder) throws Exception{
		
		File reconstruction = new File(vaeOutputFolder.getAbsolutePath(), "reconstruction.csv");
		if (!reconstruction.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(allfeaturesFile));
		String headerLine = br.readLine();
		br.close();

		String[] vaeArgs = { "-i\"" + allfeaturesFile.getAbsolutePath() + "\"", "-v\"" + "F24,F25"+ "\"",
				// "-h"+allFeatures.get(0).length,
				"-h" + ConfigManager.getProperty("vae_hidden_nodes_first_layer"),
				"-e" + ConfigManager.getProperty("vae_training_epochs"), "-o" + vaeOutputFolder.getName() + "",
				"-r16", "-ttrue" };

		System.out.println(
				"Training VAE with the following parameters:" + Arrays.toString(vaeArgs).replace(" ", "\n"));
		// JavaVAE.main(vaeArgs);
		JavaVAEGenerator.main(vaeArgs);
		
		}
		
		return reconstruction;	
	}

	public static List<Boolean> detectAnomalies(File reconstruction) throws Exception{
		List<Boolean> anomalousIndices = new ArrayList<Boolean>();
		
		List<String> allLines = Files.readAllLines(reconstruction.toPath());
		int i = 0;
		double problog = 0;
		for (String line : allLines) {
			if (i > 0) {
				String linelements[] = line.split(",");
				problog += Double.parseDouble(linelements[linelements.length - 2]);
				String classification = linelements[linelements.length - 1];
				
				if (classification.equals("0p-25p"))
					anomalousIndices.add(true);
				else
					anomalousIndices.add(false);
			}
			i++;
		}
		
		System.out.println("Total problog:\n " + problog);
		return anomalousIndices;
	}
	
}
