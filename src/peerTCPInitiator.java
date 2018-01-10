import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * @author OPrime
 *
 */

/* This class starts the TCP connections and adds it to peerHandlers */

public class peerTCPInitiator implements Runnable, RemotePeerManagerListener, FileManagerListener {

	private int my_PID = -1;

	private int portNum = 0;

	//TODO- put it in Client connect
	private String HostName = null;
	

	private FileManager mFileManager = null;
	private RemotePeerManager mRemotePeerManager = null;
	private LogHandler mLogHandler = null;

	List<peerHandler> mPeerHandlers =new ArrayList<peerHandler> ();
	private AtomicBoolean myFileDownloadCompleted = new AtomicBoolean(false);
	private AtomicBoolean remotePeersFileDownloadCompleted = new AtomicBoolean(false);
	private AtomicBoolean terminated = new AtomicBoolean(false);
	
	public peerTCPInitiator(peerProcess currPeer, int portNum, String name, commonConfig commConfig) throws IOException {
		super();
		this.my_PID = currPeer.getMy_PID();
		this.portNum = portNum;
		this.HostName = name;
		
		
		/* Create instances of Filemanager , RemotePeerManager and logger */
		this.mFileManager = new FileManager(commConfig, currPeer);
		List<peer> remotePeerList = new CopyOnWriteArrayList<peer>(currPeer.getPeer_list());

		for (peer remotePeer: remotePeerList) {
			if (remotePeer.PID == my_PID) {
				remotePeerList.remove(remotePeer);
			} 
		}

		this.mLogHandler = new LogHandler(this.my_PID);
		
		/* RemotePeer Manager */
		this.mRemotePeerManager = new RemotePeerManager(currPeer, remotePeerList, commConfig, mLogHandler);
		String str = "Log starting ";
		//mLogHandler.custommessage(str);
		if (currPeer.isiHaveFile())
			myFileDownloadCompleted.set(true);
		
		
	}
	
	public void startRemotePeerManager() {
		
			Thread t= new Thread(this.mRemotePeerManager);
			t.start();
	}


	@Override
	public void run() {
		System.out.println("peerTCPinitiator is running");
		/* Start the server and wait for the connections, 
		 * as and when you see a connection, add the socket to
		 * peerHandler,
		 */
		
		ServerSocket listener = null;
		try {
			
			listener = new ServerSocket(this.portNum);
			System.out.println("The server is running." +this.portNum); 
			while (!terminated.get()) {
				/* Add this to peerHandler */
				Socket IncomingSocket = listener.accept();
				System.out.println("The server accepted a connection on " +this.portNum);
				peerHandler peerHdlr = new peerHandler(this.my_PID, -1, IncomingSocket, false, this.mFileManager, this.mRemotePeerManager, this.mLogHandler);
				addPeerHandlers(peerHdlr);
			}
		} catch (IOException e) {
				e.printStackTrace();
		} finally {
			String str = this.my_PID + "This peer server being terminated";
			System.out.println(str);
			//mLogHandler.custommessage(str);
			if (listener != null) {
				try {
					listener.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}


	public void connectToPeers(List<peer> peer_list) throws IOException {

		List<peer> peerstoconnect = new CopyOnWriteArrayList<peer>(peer_list);

		
		for (peer curr: peerstoconnect) {
			if (curr.PID == my_PID) {
				peerstoconnect.remove(curr);
			} 
		}

		System.out.println("Starting Connecting to peers");
		/* Iterate over the peerstoConnect and create client sockets and add it to the Handler */
		for(peer remotepeer: peerstoconnect) {
			//create a socket to connect to the server
			Socket requestSocket = null;
			//TODO - replace with the host name
			InetAddress addr = InetAddress.getByName(remotepeer.name);
			String dest = addr.getHostName();
			
			try {
				requestSocket = new Socket(); 
				requestSocket.connect(new InetSocketAddress(dest, remotepeer.port_id), 1000);
				
				
				/* Add this to peerHandler */
				System.out.println("Trying to connect to curr PID" + remotepeer.PID +" name" + remotepeer.name);
				peerHandler peerHdlr = new peerHandler(this.my_PID, remotepeer.PID, requestSocket, true, this.mFileManager, this.mRemotePeerManager, this.mLogHandler);
				addPeerHandlers(peerHdlr);
			} catch (UnknownHostException e) {
				System.out.println("WARNING: Not able to connect to "+ remotepeer.PID +" name" + remotepeer.name);
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("WARNING: Not able to connect to "+ remotepeer.PID +" name" + remotepeer.name);
			}

			
		}

	}

	private void addPeerHandlers(peerHandler peerHdlr) {
		if (!mPeerHandlers.contains(peerHdlr)) {
			mPeerHandlers.add(peerHdlr);
			System.out.println("Added peer Handler " +peerHdlr.remotePeerID);
			Thread t = new Thread(peerHdlr);
			t.start();
		}
	}

	@Override
	synchronized public void unchokedPeers(HashSet<Integer> preferredPeerIDs) {
		String str = "In Unchoked peers";
		System.out.println(str);
		//mLogHandler.custommessage(str);
		for (peerHandler peerHdlr: mPeerHandlers) {
			if (preferredPeerIDs.contains(peerHdlr.remotePeerID)) {
				try {
					System.out.println("PeerTCP Initiator sending Unchoked message");
					//mLogHandler.custommessage("PeerTCP Initiator sending Unchoked message to " +peerHdlr.remotePeerID);
					peerHdlr.sendmessage(new Unchoke());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	synchronized public void chokedPeers(HashSet<Integer> chokedPeerIDs) {
		for (peerHandler peerHdlr: mPeerHandlers) {
			if (chokedPeerIDs.contains(peerHdlr.remotePeerID)) {
				try {
					peerHdlr.sendmessage(new Choke());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}

	public void registerListeners() {
		this.mFileManager.addListener(this);
		this.mRemotePeerManager.addListener(this);
		
	}

	@Override
	public synchronized void gotthePiece(int pieceIndex) {
		
		for (peerHandler peerHdlr: mPeerHandlers) {
			try {
				peerHdlr.sendmessage(new Have(pieceIndex));
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Send Not interested message, If we have all the Pieces that the remote peer has
			if (!mRemotePeerManager.isInteresting(peerHdlr.remotePeerID, mFileManager.getbitfieldInfo())) {
				try {
					peerHdlr.sendmessage(new NotInterested());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public synchronized void filedownloadCompleted() {
		this.mLogHandler.PrintFileDownloadedMessage();
		myFileDownloadCompleted.set(true);
		if (myFileDownloadCompleted.get() && remotePeersFileDownloadCompleted.get()) {
			System.out.println("Terminating the server");
			terminated.set(true);
			System.exit(0);
		}
		
	}

	@Override
	public synchronized void PeersCompletedthedownload() {
		System.out.println("All Remote peers have downloaded the file");
		remotePeersFileDownloadCompleted.set(true);
		if (myFileDownloadCompleted.get() && remotePeersFileDownloadCompleted.get()) {
			System.out.println("Terminating the server");
			terminated.set(true);
			System.exit(0);
		}
	}
}
