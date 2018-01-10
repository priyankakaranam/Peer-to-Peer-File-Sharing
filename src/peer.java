import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class peer implements Comparable  {

	/* fields that should be filled with the info from PeerInfo.cfg */
	public int PID;
	public String name;
	public int port_id;
	public int has_file;

	public BitSet mbitField;
	
	
	public AtomicInteger BytesDownloadRate = new AtomicInteger(0); 

	public int getPID() {
		return PID;
	}

	public void setPID(int pID) {
		PID = pID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort_id() {
		return port_id;
	}

	public void setPort_id(int port_id) {
		this.port_id = port_id;
	}

	public int getHas_file() {
		return has_file;
	}

	public void setHas_file(int has_file) {
		this.has_file = has_file;
	}

	public BitSet getMbitField() {
		return mbitField;
	}

	public void setMbitField(BitSet mbitField) {
		this.mbitField = mbitField;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		
		peer remotePeer = (peer) o; 
		return this.BytesDownloadRate.get()-remotePeer.BytesDownloadRate.get();
	}

}