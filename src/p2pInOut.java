import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface p2pInOut {
	
	public void write(DataOutputStream mOut) throws IOException;
	public void read(DataInputStream mIn) throws IOException;

}
