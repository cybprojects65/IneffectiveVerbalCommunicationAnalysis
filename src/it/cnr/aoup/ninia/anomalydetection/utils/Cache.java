package it.cnr.aoup.ninia.anomalydetection.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class Cache {

	public static void cacheFeatures(List<double[]> listOfDoubleArrays, File outfile, boolean overwrite) {
		// Write
		if (outfile.exists() && !overwrite) {
			System.out.println("File already exists and overwrite is disabled");
			return;
		}
			
		
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile))) {
			oos.writeObject(listOfDoubleArrays); // List<double[]>
			oos.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static List<double[]> getCachedFeatures(File file) {
		// Read
		if (!file.exists())
			return null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			@SuppressWarnings("unchecked")
			List<double[]> rows = (List<double[]>) ois.readObject();
			ois.close();
			return rows;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
