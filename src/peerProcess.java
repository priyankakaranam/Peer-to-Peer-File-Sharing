import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
/**
 * @author OPrime
 *
 */
public class peerProcess {

	class peerFields {
		int portid;
		String name;
	}

	public int getMy_PID() {
		return my_PID;
	}

	public void setMy_PID(int my_PID) {
		this.my_PID = my_PID;
	}

	public boolean isiHaveFile() {
		return iHaveFile;
	}

	public void setiHaveFile(boolean iHaveFile) {
		this.iHaveFile = iHaveFile;
	}

	public List<peer> getPeer_list() {
		return peer_list;
	}

	public void setPeer_list(List<peer> peer_list) {
		this.peer_list = peer_list;
	}

	private int my_PID = -1;
	private boolean iHaveFile = false;

	/* Maintain a list of peers */
	private List<peer> peer_list = new ArrayList<peer>();





	/* populate the PeerInfo , we are adding ourself as well to the peer list*/
	private void populatePeerInfo(commonConfig commConfig) {

		String line;


		FileReader fileReader = null;
		try {
			fileReader = new FileReader("src/PeerInfo.cfg");
		} catch (FileNotFoundException e1) {
			
			e1.printStackTrace();
		}

		BufferedReader bufferedReader = new BufferedReader(fileReader);

		try {
			while((line = bufferedReader.readLine()) != null) {
				String[] splitStrings = line.split(" ");

				peer new_peer = new peer();
				new_peer.PID= Integer.parseInt(splitStrings[0]);
				new_peer.name = splitStrings[1];
				new_peer.port_id = Integer.parseInt(splitStrings[2]);
				new_peer.has_file = Integer.parseInt(splitStrings[3]);
				
				if (new_peer.PID == this.my_PID && new_peer.has_file == 1) {
					iHaveFile = true; 
				}

				new_peer.mbitField = new BitSet(commConfig.getNumofPieces());

				/* Add this peer to list of peers, we are adding ourself as well */	
				peer_list.add(new_peer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getPortNum(List<peer> peerList) {

		int portnum = -1;

		for (peer currpeer: peerList) {
			if (currpeer.PID == my_PID) {
				portnum = currpeer.port_id;
			}
		}
		
		if (portnum == -1) {
			System.out.println("Port Number not found" +portnum);
		}
		
		return portnum;
	}

	private String getHostName(List<peer> peerList) {

		String hostName = null;

		for (peer currpeer: peerList) {
			if (currpeer.PID == my_PID) {
				hostName = currpeer.name;
			}
		}
		
		if (hostName == null) {
			System.out.println("hostName not found" +hostName);
		}

		return hostName;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/* Read PID */
		if (args.length != 1) {
			System.out.println("Input is incorrect : Enter in this format: peerProcess <PID>");
			return;
		}

		peerProcess current_peer = new peerProcess();

		String PID_String = args[0];
		current_peer.my_PID = Integer.valueOf(PID_String);

		System.out.println(" PID of this process is " +current_peer.my_PID);


		/* call a method to read the common.cfg file */
		commonConfig commConfig = new commonConfig();
		commConfig.readCommonConfig();
		

		/* call a method to populate peer Info */
		current_peer.populatePeerInfo(commConfig);
		
		
	
		List<peer> peerList = new ArrayList<peer>(current_peer.peer_list);

		/* Fetch the current peer Portnum */
		int portNum = current_peer.getPortNum(current_peer.peer_list);
		String hostName = current_peer.getHostName(current_peer.peer_list);

		/* call a method to establish a TCP connection to other Peers*/
		peerTCPInitiator tcpinitiator = new peerTCPInitiator(current_peer, portNum, hostName, commConfig);
		Thread t = new Thread(tcpinitiator);
		t.start();
		

		tcpinitiator.registerListeners();
		tcpinitiator.startRemotePeerManager();
		
		tcpinitiator.connectToPeers(peerList);
		
		
		
		
	}
}
