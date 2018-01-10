import java.util.BitSet;

public class MessageHandler {

	private boolean isChokedbyPeer = true;
	
	private int remotePeerID = -1;
	private FileManager mFileManager = null;
	private RemotePeerManager mRemotePeerManager = null;
	private LogHandler mLogHandler = null;
	
	public MessageHandler() {
		
	}

	public MessageHandler(int remotePeerID, FileManager mFileManager, RemotePeerManager mRemotePeerManager,
			LogHandler mLogHandler) {
		super();
		this.remotePeerID = remotePeerID;
		this.mFileManager = mFileManager;
		this.mRemotePeerManager = mRemotePeerManager;
		this.mLogHandler = mLogHandler;
	}



	public Message handle(Object msg) throws Exception {
		if (msg instanceof HandShake) {
			/* return Bitfield Info */
			BitSet bitset = mFileManager.getbitfieldInfo();

			return (new BitField_Type(bitset));
			
		}

		/* Handle all other msg types */
		Message message = (Message) msg;
		
		
		switch (message.getMsgType()) {

			case Choke:
				isChokedbyPeer = true;
				this.mLogHandler.PrintMessage(message.getMsgType(), this.remotePeerID);
				break;
			case Unchoke:
				isChokedbyPeer = false;
				this.mLogHandler.PrintMessage(message.getMsgType(), this.remotePeerID);
				return requestPiece();
				
			case Interested:
				/* Add to the Interested peers list */
				mRemotePeerManager.addInterestedRemotePeerbyIDs(this.remotePeerID);
				this.mLogHandler.PrintMessage(message.getMsgType(), this.remotePeerID);
				break;
			case NotInterested:
				/* Remove from the Interested peers list */
				System.out.println("Received Not Interested");
				int localremotePeerID = this.remotePeerID;
				mRemotePeerManager.RemoveInterestedRemotePeerbyIDs(localremotePeerID);
				this.mLogHandler.PrintMessage(message.getMsgType(), this.remotePeerID);
				break;
			case Have:
				Have have = (Have) message;
				int pieceIndex = have.getPieceIndex();
				mRemotePeerManager.haveArrived(this.remotePeerID, pieceIndex);
				
				this.mLogHandler.PrintHaveMessage(this.remotePeerID, pieceIndex);
				
				/* send Interested or Not interested */
				if (mFileManager.getbitfieldInfo().get(pieceIndex)) {
					return new NotInterested();
				} else {
					return new Interested();
				}
				
			case BitField_Type:
				
				System.out.println("Received a bitfiled message");
				BitField_Type bitfield = (BitField_Type) message;
				
				BitSet remotePeerbitset = bitfield.getBitSet();
				
				mRemotePeerManager.setBitfieldForRemotePeer(this.remotePeerID, remotePeerbitset);
				
				remotePeerbitset.andNot(mFileManager.getbitfieldInfo());
				
				if (remotePeerbitset.isEmpty()) {
					System.out.println("Received a bitfiled , sent Not interested message");
					return new NotInterested();
				} else {
					System.out.println("Received a bitfiled , sent interested message");
					return new Interested();
				}
			
			case Request:
				Request req = (Request) message;
				
				if (mRemotePeerManager.canSendthePiece(remotePeerID)) {
					byte[] piece = mFileManager.getPiece(req.getPieceIndex());
					if (piece != null) {
						System.out.println("Incoming request message , returning piece" +req.getPieceIndex());
						return new Piece(req.getPieceIndex(), piece);
					}
						
				}
				return null;
				
			case Piece:
				Piece piece = (Piece) message;
				//Send it to file Manager
				mFileManager.addPiecetoFile(piece.getPieceIndex(), piece.getPieceContent());
				mRemotePeerManager.receivedPiece(this.remotePeerID, piece.getPieceContent().length);
				
				/*Log the messsages */
				this.mLogHandler.PrintPieceDownloadedMessage(remotePeerID, piece.getPieceIndex(), this.mFileManager.getbitfieldInfo().cardinality());
				/* request a new piece again */
				return requestPiece();
				
			default:
				throw new Exception("Messagetype is not handled"  +message.getMsgType());

		}
		
		return null;

	}
	
	
	private Message requestPiece() {
		
		if (isChokedbyPeer) {
			System.out.println("Got a request Piece even though the peer is Choked");
			return null;
		} else {
			int PieceIndex  = mFileManager.getPieceIndexToRequest(mRemotePeerManager.getReceivedParts(this.remotePeerID));
			
			if (PieceIndex >= 0) {
				// Log it
				return new Request(PieceIndex);
				/*Check what if multiple Requests are created for the same Piece */
			} else {
				//Log it
			}
			
			return null;
		}
	}
}
