import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class fileops {

	private int myPeerID = 0;
	private String filename = null;
	private File PiecesDir;
	private File destfile;
	private String Piecesloc = "files/pieces";
	private int PieceSize;
	private int numOfpieces;

	public fileops(int myPeerID, String filename, int PieceSize, int numOfpieces) {

		this.myPeerID = myPeerID;
		this.filename = filename;
		String pieceloc = "./peer_" + myPeerID+ "/"+ Piecesloc;

		this.PiecesDir = new File(pieceloc);
		PiecesDir.mkdirs();

		//String fileloc = PiecesDir.getParent() + "/../" +filename;
		
		String fileloc = "./peer_" + myPeerID + "/files/" + filename;
		
		
		System.out.println("File is being Stroed in " + fileloc);
		System.out.println("Pieces are being Stroed in " + pieceloc);
		
		System.out.println(" while merging this path is used  " + PiecesDir.getAbsolutePath());
		
		

		this.destfile = new File(fileloc);
		System.out.println("File size +" +destfile.length());
		this.PieceSize = PieceSize;
		this.numOfpieces = numOfpieces;
	}

	public void writePiece(int pieceIndex, byte[] pieceContent) throws IOException {

		File file = new File(PiecesDir.getAbsolutePath()+ "/" + pieceIndex);

		FileOutputStream FileOutStream = new FileOutputStream(file);
		FileOutStream.write(pieceContent);
		FileOutStream.flush();
		FileOutStream.close();

	}

	public byte[] getPiece(int pieceIndex) throws IOException {
		File file = new File(PiecesDir.getAbsolutePath()+ "/" + pieceIndex);

		FileInputStream fileStream = new FileInputStream(file);
		int filelen = (int) file.length();

		byte[] PieceContent = new byte[filelen];

		int bytesread = fileStream.read(PieceContent, 0, filelen);

		if (bytesread != filelen) {
			System.out.println("Not completely read the PieceContents");
		}


		return PieceContent;
	}

	public void splitfile() throws IOException {

		File file = this.destfile;
		int filelength = (int) file.length();
		int PieceSize = this.PieceSize;

		byte[] Piece ;

		FileInputStream fin;
		FileOutputStream fout;

		fin = new FileInputStream(file);
		int bytesread = 0;


		int filesize = filelength;
		int pieceIndex = 0;


		while (filesize > 0) {


			Piece = new byte[PieceSize];
			if (filesize < PieceSize) {
				System.out.println("File size is lesser, Reading the size " +filesize);
				bytesread = fin.read(Piece, 0, filesize);
			}	else {
				System.out.println("Reading the size " +PieceSize);
				bytesread = fin.read(Piece, 0, PieceSize);
				assert (bytesread == PieceSize);
			}

			
			filesize -= bytesread;

			String Piecename = file.getParent() + "/pieces/" +Integer.toString(pieceIndex);
			pieceIndex++;
			File PieceFile = new File(Piecename);

			fout = new FileOutputStream(PieceFile);
			fout.write(Piece);

			fout.flush();
			fout.close();
		}


		
		if (pieceIndex != this.numOfpieces) {
			System.out.println("Pieces not read properly, pieceIndex"  +pieceIndex +"numOfpieces" +this.numOfpieces);
		}

	}

	public void merge(int totalPieces) throws IOException {
		
		List<File> fileParts = new ArrayList<> ();
		byte[] piece;
		
		for (int i = 0 ; i < totalPieces; i++) {
			fileParts.add(new File(PiecesDir.getAbsolutePath()+ "/" + i));
		}
		
		File file = this.destfile;
		
		FileOutputStream fileOutStream = new FileOutputStream(file);
		FileInputStream fileInStream = null;
		for (File part : fileParts) {
			
			fileInStream = new FileInputStream(part);
			
			int partlen = (int) part.length();
			piece = new byte[partlen];
			int bytesread = fileInStream.read(piece, 0, partlen);
			assert(bytesread == partlen);
			
			fileOutStream.write(piece);
			fileOutStream.flush();
			
			fileInStream.close();
			fileInStream = null;
			
			piece = null;
		}
		
		fileOutStream.close();
		fileOutStream = null;
				
	}







}
