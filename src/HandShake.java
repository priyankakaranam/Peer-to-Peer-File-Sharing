import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class HandShake {
	
	private final String header = "P2PFILESHARINGPROJ";
	private byte[] zeroBits = new byte[10];
	private byte[] peerID = new byte[4];
	
	
	//TODO - peerID , How is it being used?
	public HandShake(int peerID) {
		this.peerID = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerID).array();
	}
	
	public HandShake(byte[] peerID) {
		super();
		
		if (peerID.length != 4) {
			System.out.println("peerID length is incorrect");
		}
		
		int i = 0;
		for (byte pIDbyte: peerID)
			peerID[i++] = pIDbyte;
	}
	
	public HandShake() {
		
	}

	public int getPeerID() {
		return ByteBuffer.wrap(peerID).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	public void write(p2pOutputstream out) throws IOException {
		byte[] header_array = header.getBytes(Charset.forName("US-ASCII"));
		out.write(header_array, 0, header_array.length);
		out.write(zeroBits, 0, zeroBits.length);
		out.write(peerID, 0, peerID.length);
		 	
	}
	
	public void read(p2pInputstream in) throws IOException {
		byte[] header_array =new byte[header.length()];
		
		
		if (in.read(header_array, 0, header.length()) <  header.length()) {
			throw new IOException("Exception in read of header in Handshake" +header.length());
		}
		
		if (!header.equals(new String(header_array, "US-ASCII"))) {
			throw new IOException("Exception in read of header in Handshake" +header.length());
		}
		
		if (in.read(zeroBits, 0, zeroBits.length) < zeroBits.length) {
			throw new IOException("Exception in read of zeroBits in Handshake" +zeroBits.length);
		}
		
		if (in.read(this.peerID, 0, peerID.length) < peerID.length) {
			throw new IOException("Exception in read of peerID in Handshake" +peerID.length);
		}
		
		
	}
	
}
