import java.util.HashSet;

public interface RemotePeerManagerListener {

	void unchokedPeers(HashSet<Integer> preferredPeerIDs);

	void chokedPeers(HashSet<Integer> chokedPeerIDs);

	void PeersCompletedthedownload();
	
	

}
