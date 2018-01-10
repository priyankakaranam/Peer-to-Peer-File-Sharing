import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class commonConfig {

	/* common cfg variables */
	private int preferredNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokeInterval;
	private String fileName;
	public int getPreferredNeighbors() {
		return preferredNeighbors;
	}

	public void setPreferredNeighbors(int preferredNeighbors) {
		this.preferredNeighbors = preferredNeighbors;
	}

	public int getUnchokingInterval() {
		return unchokingInterval;
	}

	public void setUnchokingInterval(int unchokingInterval) {
		this.unchokingInterval = unchokingInterval;
	}

	public int getOptimisticUnchokeInterval() {
		return optimisticUnchokeInterval;
	}

	public void setOptimisticUnchokeInterval(int optimisticUnchokeInterval) {
		this.optimisticUnchokeInterval = optimisticUnchokeInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public void setPieceSize(int pieceSize) {
		this.pieceSize = pieceSize;
	}

	public int getNumofPieces() {
		return numofPieces;
	}

	public void setNumofPieces(int numofPieces) {
		this.numofPieces = numofPieces;
	}

	public String[] getCommonValues() {
		return commonValues;
	}

	public void setCommonValues(String[] commonValues) {
		this.commonValues = commonValues;
	}

	private int fileSize;
	private int pieceSize;
	private int numofPieces;
	private String[] commonValues = new String[6];

	/* Method to initialize config variables */
	public void readCommonConfig() {

		int i=0;
		String line;


		FileReader fileReader = null;
		System.out.println(new File(".").getAbsolutePath());
		try {
			fileReader = new FileReader("src/Common.cfg");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		BufferedReader bufferedReader = new BufferedReader(fileReader);

		try {
			while((line = bufferedReader.readLine()) != null) {
				String[] splitStrings = line.split(" ");
				commonValues[i++] = splitStrings[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		preferredNeighbors = Integer.parseInt(commonValues[0]);
		unchokingInterval = Integer.parseInt(commonValues[1]);
		optimisticUnchokeInterval = Integer.parseInt(commonValues[2]);
		fileName = commonValues[3];
		fileSize = Integer.parseInt(commonValues[4]);
		pieceSize = Integer.parseInt(commonValues[5]);
		double val = (double) fileSize/pieceSize;
		numofPieces = (int) Math.ceil(val);

		System.out.println("numofPieces" +numofPieces);

		try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public commonConfig() {
		// TODO Auto-generated constructor stub
	}

}
