import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class RemotePeerManager implements Runnable {

	//Remote Peer List
	List<peer> remotePeerList = new ArrayList<>();

	// List of Interested Remote Peer ID's 
	Collection<Integer> InterestedRemotePeerIDs = new ArrayList<> ();

	//List of Choked Peers
	/* Choked Peers, Initially all the peers in the remotePeerList are Choked */
	HashSet<Integer> chokedPeerIDs = new HashSet<Integer>();

	//Unchoked Peer List where the remote peers are unchoked (pref + op unchoked)
	HashSet<Integer> unchokedPeerIDs = new HashSet<Integer>();

	// Which has highest download rates among Interested and we unchoke them , P 
	HashSet<Integer> preferredPeerIDs = new HashSet<Integer>();
	public int numberOfPreferredPeers;

	//List of probable op unchoked candidates
	HashSet<Integer> canbeOpUnchokedPeerIDs = new HashSet<Integer> ();


	List<peer> InterestedRemotePeers = new ArrayList<peer> ();
	List<peer> ChokedPeers = new ArrayList<peer> ();
	List<peer> preferredPeers = new ArrayList<peer> ();
	List<peer> canbeOpUnchokedPeers = new ArrayList<peer>();


	public AtomicBoolean randomSelectPeer = new AtomicBoolean(false);

	public AtomicBoolean isAtTheStart = new AtomicBoolean(true);


	private int unchokingInterval = 0;
	private int numOfPieces = 0;

	private RemotePeerManagerListener mListener;
	private LogHandler mLogHandler = null;


	public Collection<Integer> getInterestedRemotePeerIDs() {
		return InterestedRemotePeerIDs;
	}
	public void setInterestedRemotePeerIDs(List<Integer> interestedRemotePeerIDs) {
		InterestedRemotePeerIDs = interestedRemotePeerIDs;
	}


	public void addChokedPeers(List<peer> remotePeerList) {
		for (peer remotePeer: remotePeerList) {
			ChokedPeers.add(remotePeer);
		}

	}

	private OpUnchokedneighbor opUnchoked = null;

	public RemotePeerManager(peerProcess currPeer, List<peer> remotePeerList, commonConfig commConfig, LogHandler loghdlr){
		this.remotePeerList = remotePeerList;
		this.numberOfPreferredPeers = commConfig.getPreferredNeighbors();

		addChokedPeers(remotePeerList);

		unchokingInterval = commConfig.getUnchokingInterval();
		opUnchoked = new OpUnchokedneighbor(commConfig.getOptimisticUnchokeInterval());
		numOfPieces = commConfig.getNumofPieces();
		this.mLogHandler = loghdlr;

	}


	class OpUnchokedneighbor extends Thread {

		int opUnchokingInterval = 0;

		public RemotePeerManagerListener Listener = null;

		public OpUnchokedneighbor(int opUnchokingInterval) {
			this.opUnchokingInterval = opUnchokingInterval;

		}

		//List<peer> canbeOpUnchokedPeers = new ArrayList<peer> ();
		HashSet<Integer> unchokedPeerIDS = new HashSet<Integer> ();

		@Override
		public void run() {

			while (true) {
				try {
					Thread.sleep(opUnchokingInterval * 1000);
				} catch (InterruptedException e) {
					System.out.println("Exception while waiting for unchoking Interval");
					e.printStackTrace();
				}
				

				/* Inform the listener about the Unchoked neighbor and send it;
				 */
				synchronized(canbeOpUnchokedPeers) {
					
					if (canbeOpUnchokedPeers.size() == 0) {
						continue;
					}
					Collections.shuffle(canbeOpUnchokedPeers);
					List<peer> unchokedpeers = new ArrayList<peer> ();

					unchokedpeers.addAll(canbeOpUnchokedPeers.subList(0 ,1));

					unchokedPeerIDS.clear();
					unchokedPeerIDS.addAll(ConvertpeerstoIDsSet(unchokedpeers));
						
					String str = "canbe Op Unchoked Peers size " + canbeOpUnchokedPeers.size() + "unchokedPeerIDS size "+unchokedPeerIDS.size();
					System.out.println(str);
					//mLogHandler.custommessage(str);
					
					
					
					
					mLogHandler.PrintChangeOfOpUnchokedNeighbors(unchokedPeerIDS);
					
				}

				this.Listener.unchokedPeers(unchokedPeerIDS);
			}

		}

		synchronized void setOpUnChoked(List<peer> canbeOpUnchokedPeers) {
			// TODO Auto-generated method stub

			//this.canbeOpUnchokedPeers = canbeOpUnchokedPeers;
		}

		public HashSet<Integer> getUnchokedOpneighbor() {

			return unchokedPeerIDS;
		}
	}

	public void addListener(RemotePeerManagerListener Listener) {
		this.mListener = Listener;
		this.opUnchoked.Listener = Listener;
	}



	public BitSet getReceivedParts(int remotePeerID) {
		// TODO Auto-generated method stub
		peer p = getRemotePeer(remotePeerID);
		BitSet bitset = new BitSet();

		if (p != null) {
			bitset = (BitSet) p.getMbitField().clone();
			return bitset;
		} else {
			System.out.println("Peer Not found for getReceivedParts");
		}

		return bitset;
	}



	public void receivedPiece(int remotePeerID, int length) {
		peer p = getRemotePeer(remotePeerID);
		if(p != null){
			p.BytesDownloadRate.addAndGet(length);
		} else {
			System.out.println("Peer Not found for getReceivedParts");
		}
	}



	private peer getRemotePeer(int remotePeerID) {
		for(peer p:remotePeerList){
			if(p.getPID()==remotePeerID)
				return p;
		}
		return null;
	}


	synchronized void addInterestedRemotePeerbyIDs(int remotePeerID) {
		synchronized (this.InterestedRemotePeerIDs) {
			if(!InterestedRemotePeerIDs.contains(remotePeerID)) {
				InterestedRemotePeerIDs.add(remotePeerID);

				peer remotePeer = getRemotePeer(remotePeerID);
				InterestedRemotePeers.add(remotePeer);
			}
		}

	}

	synchronized void RemoveInterestedRemotePeerbyIDs(int remotePeerID) {
		synchronized (this.InterestedRemotePeerIDs) {
			if(InterestedRemotePeerIDs.contains(remotePeerID)) {
				InterestedRemotePeerIDs.remove(remotePeerID);
				peer remotePeer = getRemotePeer(remotePeerID);
				InterestedRemotePeers.remove(remotePeer);
			}
		}
	}




	synchronized void haveArrived(int remotePeerID, int pieceIndex) {
		peer p = getRemotePeer(remotePeerID);

		if(p != null){
			p.mbitField.set(pieceIndex);;
		} else {
			System.out.println("Peer Not found for bitfieldArrived");
		}

		CheckifPeersCompleteddownload();

	}

	synchronized void CheckifPeersCompleteddownload() {
		for(peer p:remotePeerList){
			if (p.getMbitField().cardinality() < numOfPieces) {
				System.out.println("peer " + p.getPID() + " Not completed the download yet");
				return;
			}
		}

		this.mListener.PeersCompletedthedownload();
	}



	synchronized void setBitfieldForRemotePeer(int remotePeerID, BitSet bitset) {
		peer p = getRemotePeer(remotePeerID);
		if(p != null){
			p.mbitField = bitset;
		} else {
			System.out.println("Peer Not found for bitfieldArrived");
		}

		CheckifPeersCompleteddownload();
	}



	synchronized void fileComplete(){
		randomSelectPeer.set(true);
	}


	synchronized static Set<Integer> ConvertpeerstoIDsSet(List<peer> peers) {
		Set<Integer> peerIDs = new HashSet<>();
		for(peer p:peers){
			peerIDs.add(p.getPID());
		}
		return peerIDs;
	}

	public void run(){

		opUnchoked.start();

		while (true) {

			//TODO - should we wait till we have some Interested Peers

			try {
				Thread.sleep(unchokingInterval * 1000);
			} catch (InterruptedException e) {
				System.out.println("Exception while waiting for unchoking Interval");
				e.printStackTrace();
			}

			if (this.isAtTheStart.get()) {
				System.out.println("Selecting Neighbors randomly for the first time");
				Collections.shuffle(this.InterestedRemotePeers);
				this.isAtTheStart.set(false);
			} else{
				//System.out.println("Selecting Neighbors based on sort");
				Collections.sort(this.InterestedRemotePeers);
			}

			synchronized (this) {


				//Add top K in Interested to Preferred
				int numOfPreferred = (this.InterestedRemotePeers.size() > numberOfPreferredPeers) ? numberOfPreferredPeers : this.InterestedRemotePeers.size() ;

				preferredPeers.clear();
				preferredPeerIDs.clear();
				preferredPeers.addAll(this.InterestedRemotePeers.subList(0, numOfPreferred));
				preferredPeerIDs.addAll(ConvertpeerstoIDsSet(preferredPeers));






				//Optimistically Unchoked neighbors
				synchronized (canbeOpUnchokedPeers) {
					canbeOpUnchokedPeers.clear();
					canbeOpUnchokedPeerIDs.clear();
					canbeOpUnchokedPeers.addAll(this.InterestedRemotePeers.subList(numOfPreferred, this.InterestedRemotePeers.size()));
					canbeOpUnchokedPeerIDs.addAll(ConvertpeerstoIDsSet(canbeOpUnchokedPeers));

				}
				
				chokedPeerIDs.clear();
				ChokedPeers.removeAll(preferredPeers);
				chokedPeerIDs.addAll(ConvertpeerstoIDsSet(ChokedPeers));

				if (numOfPreferred > 0 ){
					System.out.println("Interested Peers exists, preferred PeerID size " +preferredPeers.size() + "ID size" + preferredPeerIDs.size() );

					System.out.println("Number of choked peers " +ChokedPeers.size() + " ID size" + chokedPeerIDs.size());
					String str = "Interested peers size" + this.InterestedRemotePeers.size() + "Set numberOfPreferredPeers" + numberOfPreferredPeers + "Chosen number of Preferred" + numOfPreferred;
					//this.mLogHandler.custommessage(str);

					this.mLogHandler.PrintChangeOfPrefNeighbors(preferredPeerIDs);

				}
			}
			/*Send unchoke to preferredPeers */
			/*send this Info to TCP Initiator, this will take care of */
			if (preferredPeerIDs.size() > 0) {
				this.mListener.unchokedPeers(preferredPeerIDs);
			}
			if (chokedPeerIDs.size() > 0) {
				this.mListener.chokedPeers(chokedPeerIDs);
			}


			/*send unchoke to OpunchokedPeers */
			/* Run this in a seperate thread over the unchoking Interval */

			this.opUnchoked.setOpUnChoked(canbeOpUnchokedPeers);
		}
	}
	public boolean isInteresting(int remotePeerID, BitSet myBitSet) {
		peer p = getRemotePeer(remotePeerID);
		if(p != null){
			BitSet remotePeerBitSet = (BitSet) p.getMbitField().clone();
			remotePeerBitSet.andNot(myBitSet);

			return !remotePeerBitSet.isEmpty();
		} 

		return false;
	}

	synchronized boolean canSendthePiece(int remotePeerID) {
		if (preferredPeerIDs.contains(remotePeerID) || this.opUnchoked.getUnchokedOpneighbor().contains(remotePeerID)) {
			return true;
		}

		return false;
	}

}
