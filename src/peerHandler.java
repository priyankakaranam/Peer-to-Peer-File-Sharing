import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author OPrime
 *
 */

/*
 * 
 * 
 */
public class peerHandler implements Runnable {

	private int my_PID;
	public int remotePeerID; 
	private Socket mSocket;
	private boolean asClient = false; 
	private FileManager mFileManager = null;
	private RemotePeerManager mRemotePeerManager = null;
	private LogHandler mLogHandler = null;

	/*Out and In Handlers on the socket */
	private p2pOutputstream mOutput = null;
	private p2pInputstream mInput = null;

	BlockingQueue<Message> msgQ = new LinkedBlockingQueue<Message>();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof peerHandler) {
			peerHandler pH = (peerHandler) obj;
			return pH.remotePeerID == remotePeerID;
		}

		return false;
	}

	class TimerforRequest extends TimerTask{

		private FileManager mFileManager = null;

		private int remotePeerID;
		private p2pOutputstream mOutput = null;
		private Message message;


		public TimerforRequest(FileManager mFileManager, int remotePeerID, p2pOutputstream mOutput, Message message) {
			super();
			this.mFileManager = mFileManager;
			this.remotePeerID = remotePeerID;
			this.mOutput = mOutput;
			this.message = message;
		}


		@Override
		public void run() {
			Request request = (Request) message;
			int pieceIndex = request.getPieceIndex();
			if (this.mFileManager.hasPiece(pieceIndex)) {
				System.out.println("Piece Already received, Not requesting the piece" +pieceIndex);
			} else {
				try {
					if (!mSocket.isClosed()) {
						mOutput.writeObject(message);
					} else {
						String str = "Socket is closed , Still trying to write the messages + " +message.getMsgType();
						System.out.println(str);
						//mLogHandler.custommessage(str);
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}



	}

	public peerHandler(int my_PID, int remotePeerID, Socket socket, boolean asClient, FileManager fmgr, RemotePeerManager rmgr, LogHandler logger) throws IOException {
		super();
		this.my_PID = my_PID;

		this.remotePeerID = remotePeerID;
		System.out.println("In peerHandler Remote peer ID" + this.remotePeerID + " Passed peerID " +remotePeerID);
		this.mSocket = socket;

		/* This is to identify if the local peer is getting connected to another peer
		 * That means this peer is the client and remote is the server */
		this.asClient = asClient;

		this.mFileManager = fmgr;
		this.mRemotePeerManager = rmgr;
		this.mLogHandler = logger;


		/* Assign the output handler */
		mOutput = new p2pOutputstream(mSocket.getOutputStream());

		/* Assign the Input Handler */
		mInput = new p2pInputstream(mSocket.getInputStream());
	}

	private boolean isValidPeer(HandShake handshake) {

		System.out.println("Remote peer ID" + remotePeerID + " Handshake peerID " +handshake.getPeerID());
		if (this.remotePeerID == handshake.getPeerID()) {
			return true;
		}

		return false;	
	}

	private synchronized void writeOutmessages(Message msg){
		if (msg != null) {
			try {
				if (!this.mSocket.isClosed()) {
					mOutput.writeObject(msg);
				}  else {
					System.out.println("Socket is closed , Still trying to write the messages + " +msg.getMsgType());
				}
			} catch (IOException e) {
				System.out.println("Exception while writing message Type +" +msg.getMsgType());
				e.printStackTrace();
			}
			if (msg.getMsgType() == MessageTypes.Request) {
				new java.util.Timer().schedule(new TimerforRequest(this.mFileManager, this.remotePeerID, this.mOutput, msg), this.mFileManager.getUnchokingInterval() * 2 * 1000);
			}
		}
	}

	@Override
	public void run(){
		/* Here we have to do handshake on the socket, and get the Handshake message from other peers */


		// Start another thread and queue for the choked/unchoked messages */

		new Thread() {

			private boolean isremotePeerChoked = true; 

			@Override
			public void run() {

				while(true) {
					try {
						Message msg = msgQ.take();

						if (msg == null) {
							continue;
						}


						if(remotePeerID != -1) {
							if (msg.getMsgType() == MessageTypes.Choke) {
								isremotePeerChoked = true;
							} else if (msg.getMsgType() == MessageTypes.Unchoke) {
								isremotePeerChoked = false;
							}
							writeOutmessages(msg);
						} else {

						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}

		}.start();

		/*send a Handshake on the socket out write */
		HandShake handshake = new HandShake(this.my_PID);

		//TODO - Thread that processes, choke and unchoke message while sending


		try {
			mOutput.writeObject(handshake);
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		/* Receive the handshake and check the peerID */
		HandShake receivedHandShake = new HandShake();

		try {
			receivedHandShake = (HandShake) mInput.readObject();
			this.remotePeerID = receivedHandShake.getPeerID();
			if (receivedHandShake != null) {
				if (this.asClient && !isValidPeer(receivedHandShake)) {
					throw new Exception(" Remote PeerID " +remotePeerID+"  different in handshake" +receivedHandShake.getPeerID());
				}
				System.out.println("Received handshake peerID" + receivedHandShake.getPeerID());
				this.mLogHandler.PrintmakesTCPConnection(this.remotePeerID, this.asClient);


			} else {
				throw new Exception("Invalid HandShake object");
			}
			
			
			MessageHandler mHdlr = new MessageHandler(this.remotePeerID, this.mFileManager, this.mRemotePeerManager, this.mLogHandler);
			/* Here we just send the bitfield info  */
			try {
				if (mHdlr != null) {
					Message msgTobeSent = mHdlr.handle(receivedHandShake);

					if (msgTobeSent == null) {
						System.out.println("Not sending Bitfield as it is null");
					}
					writeOutmessages(msgTobeSent);
				} else {
					System.out.println("Message Handler is null");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}



			/* we will read the Incoming messages and process based on the Type */
			while(true) {
				try {

					Message msg=(Message) mInput.readObject();
					Message msgTobeSent = mHdlr.handle(msg);
					writeOutmessages(msgTobeSent);

				} catch (Exception e) {
					String str = my_PID + "******WARNING***** , Issue with remote Socket" + remotePeerID +  "closing our socket, In msg Type " ;
					System.out.println(str);
					//mLogHandler.custommessage(str);
					e.printStackTrace();
					break;
				} 
			}			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				this.mSocket.close();
				String str2 = "Closing the socket for this remotepeer" +this.remotePeerID;
				System.out.println(str2);
				//mLogHandler.custommessage(str2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

				

	}

	public void sendmessage(Message msg) throws IOException {
		//TODO - Check if this msgs has to be added to Queue 
		msgQ.add(msg);
	}
}
