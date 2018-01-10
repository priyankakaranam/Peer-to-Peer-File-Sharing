import java.io.IOException;
import java.util.BitSet;

public class FileManager {

	
	/*received parts */
	private BitSet mHavePieces = null;
	
	/* requesting parts */
	private requestedPieces mRequestedPieces = null;
	private int unChokingInterval = 0;
	private fileops mfile;
	private FileManagerListener fmListener;
	private int numOfPieces = 0;
	

	public FileManager(commonConfig commConfig, peerProcess currPeer) throws IOException {
		
		numOfPieces = commConfig.getNumofPieces();
		this.mHavePieces = new BitSet(numOfPieces);
		this.mfile = new fileops(currPeer.getMy_PID(), commConfig.getFileName(), commConfig.getPieceSize(), commConfig.getNumofPieces());
		System.out.println("Current peer Has File" + currPeer.isiHaveFile());
		if (currPeer.isiHaveFile()) {
			System.out.println("This Peer has the complete file");
			this.mHavePieces.set(0, numOfPieces, true);
			
			System.out.println("Splitting the file");
			this.mfile.splitfile();
			//this.mfile.merge(5);
		}
		
		this.mRequestedPieces = new requestedPieces(numOfPieces, commConfig.getUnchokingInterval());
		this.unChokingInterval = commConfig.getUnchokingInterval();
		
		
		
	}
	
	


	synchronized BitSet getMyPieceInfo() {
		return (BitSet)mHavePieces.clone();
	}
	
	synchronized int getPieceIndexToRequest(BitSet remotePeerPieces) {
		remotePeerPieces.andNot(getMyPieceInfo());
		System.out.println("remote Pieces cardinality " +remotePeerPieces.cardinality());
		
		return mRequestedPieces.getPiecestoRequest(remotePeerPieces);
	}

	

	synchronized void addPiecetoFile(int pieceIndex, byte[] pieceContent) throws IOException {
		//Should update index in Requested Pieces
		if (mHavePieces.get(pieceIndex)) {
			System.out.println("This piece is already received  " +pieceIndex);
		} else {
			mHavePieces.set(pieceIndex);
			
			//write the part of the piece to the Fileops
			/*This just writes the piece */
			this.mfile.writePiece(pieceIndex, pieceContent);
			
			/* Inform to Listener so that It can send Have message */
			this.fmListener.gotthePiece(pieceIndex);
			
		}
		
		
		if (isAllPiecesReceived()) {
			if (mHavePieces.cardinality() != numOfPieces) {
				System.out.println(" There is a problem with the number of bits set");
			}
			this.mfile.merge(mHavePieces.cardinality());
			this.fmListener.filedownloadCompleted();
			
		}
	}

	synchronized boolean isAllPiecesReceived() {
		for (int i = 0; i < this.numOfPieces; i++) {
			if (!mHavePieces.get(i)) {
				return false;
			}
		}
		
		return true;
	}

	synchronized BitSet getbitfieldInfo() {
		// TODO Auto-generated method stub
		return this.mHavePieces;
	}


	synchronized byte[] getPiece(int pieceIndex) throws IOException {
		// TODO Auto-generated method stub
		return this.mfile.getPiece(pieceIndex);
	}


	synchronized boolean hasPiece(int pieceIndex) {
		// TODO Auto-generated method stub
		return this.mHavePieces.get(pieceIndex);
	}


	public int getUnchokingInterval() {
		return this.unChokingInterval;
	}


	public void addListener(FileManagerListener listener) {
		this.fmListener = listener;
		
	}

}
