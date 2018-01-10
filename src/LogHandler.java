import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogHandler {


	private int my_PID;

	private String msg;

	public LogHandler() {

	}
	private Logger logger;
	
	class Myformatter extends Formatter {
		@Override
		public String format(LogRecord record) {
	        return new java.util.Date() + " " + record.getLevel() + " " + record.getMessage() + "\r\n";
	    }
	}

	public LogHandler(int my_PID) {
		super();
		this.my_PID = my_PID;
		this.msg = ": Peer " + my_PID;
		String path = "./log_peer_" +my_PID+ ".log";
		logger = Logger.getLogger(this.getClass().getName());
		try {
			FileHandler filehdlr =  new FileHandler(path);
			
			Myformatter formatter = new Myformatter();
			
			
						
			filehdlr.setFormatter(formatter);
			logger.addHandler(filehdlr);
			
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTime() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}
	/* write the methods that are required */

	public void PrintmakesTCPConnection(int remotePeerID , boolean isConnectingPeer) {

		String msg_final = getTime() + this.msg + (isConnectingPeer ?	 " makes a connection to Peer %d." : " is connected from Peer %d.");
		System.out.println(String.format(msg_final, remotePeerID));
		logger.log(Level.INFO, String.format(msg_final, remotePeerID));

	}

	public void PrintChangeOfPrefNeighbors(String PrefNeighbors) {

		String msg_final = getTime() + this.msg + " has the preferred neighbors %s";
		System.out.println(String.format(msg_final, PrefNeighbors));
		logger.log(Level.INFO, String.format(msg_final, PrefNeighbors));
	}

	public void PrintChangeOfOpUnchokedNeighbors(int OpUnchokedNeighbor) {

		String msg_final = getTime() + this.msg + " has the optimistically Unchoked neighbor %d";
		System.out.println(String.format(msg_final, OpUnchokedNeighbor));
		logger.log(Level.INFO, String.format(msg_final, OpUnchokedNeighbor));
	}

	public void PrintMessage(MessageTypes msgType, int remotePeerID) {

		String msg_final;
		switch(msgType) {

		case Choke:
			msg_final = getTime() + this.msg + " is choked by %d.";
			System.out.println(String.format(msg_final, remotePeerID));
			logger.log(Level.INFO, String.format(msg_final, remotePeerID));
			break;
		case Unchoke:
			msg_final = getTime() + this.msg + " is unchoked by %d.";
			System.out.println(String.format(msg_final, remotePeerID));
			logger.log(Level.INFO, String.format(msg_final, remotePeerID));
			break;
		case Interested:
			msg_final = getTime() + this.msg + " received the 'interested' message from %d.";
			System.out.println(String.format(msg_final, remotePeerID));
			logger.log(Level.INFO, String.format(msg_final, remotePeerID));
			break;
		case NotInterested:
			msg_final = getTime() + this.msg + " received the 'Not interested' message from %d.";
			System.out.println(String.format(msg_final, remotePeerID));
			logger.log(Level.INFO, String.format(msg_final, remotePeerID));
			break;
		default:
			System.out.println("Messagetype is not found" +msgType);
		}
	}


	public void PrintHaveMessage(int remotePeerID, int pieceIndex) {
		String msg_final = getTime() + this.msg + "  received the 'have' message from %d for the piece %d.";
		System.out.println(String.format(msg_final, remotePeerID, pieceIndex));
		logger.log(Level.INFO, String.format(msg_final, remotePeerID, pieceIndex));
	}

	public void PrintPieceDownloadedMessage(int remotePeerID, int pieceIndex, int numOfPieces) {
		String msg_final = getTime() + this.msg + "  has downloaded the piece %d from peer %d. Now the number of pieces it has is %d.";
		System.out.println(String.format(msg_final, pieceIndex, remotePeerID, numOfPieces));
		logger.log(Level.INFO, String.format(msg_final, pieceIndex, remotePeerID, numOfPieces));
	}

	public void PrintFileDownloadedMessage() {
		String msg_final = getTime() + this.msg + " has downloaded the complete file.";
		System.out.println(String.format(msg_final));
		logger.log(Level.INFO, String.format(msg_final));
	}

	public void PrintChangeOfPrefNeighbors(HashSet<Integer> preferredPeerIDs) {
		if (preferredPeerIDs.size() == 0) {
			String msg_final = getTime() + this.msg + " has the preferred neighbors %d";
			System.out.println(String.format(msg_final, preferredPeerIDs.size()));
			logger.log(Level.INFO, String.format(msg_final, preferredPeerIDs.size()));

		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(" [ ");
			for (Integer peerID: preferredPeerIDs) {
			
				sb.append(Integer.toString(peerID));
				sb.append(" , ");
			}
			sb.append(" ] ");
			PrintChangeOfPrefNeighbors(sb.toString());
		}
		
	}

	public void PrintChangeOfOpUnchokedNeighbors(HashSet<Integer> unchokedPeerIDS) {
		if (unchokedPeerIDS.size() == 0) {
			String msg_final = getTime() + this.msg + " has the Unchoked neighbors %d";
			System.out.println(String.format(msg_final, unchokedPeerIDS.size()));
			logger.log(Level.INFO, String.format(msg_final, unchokedPeerIDS.size()));

		} else {
			
			String str = "In else of unchoked PeerIDs";
			logger.log(Level.INFO, str);
			
			for (Integer peerID: unchokedPeerIDS) {
				String str2 = "Calling change Of Op unchoked neighbors" +peerID;
				logger.log(Level.INFO, str2);
					
				PrintChangeOfOpUnchokedNeighbors(peerID);
			}
			
			
		}
		
	}

	public void custommessage(String str) {
		
		String msg_final = getTime() + this.msg + str;
		System.out.println(String.format(msg_final));
		logger.log(Level.INFO, String.format(msg_final));
	}


}
