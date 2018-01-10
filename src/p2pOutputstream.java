import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

public class p2pOutputstream extends DataOutputStream implements ObjectOutput{

	
	public p2pOutputstream(OutputStream out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void writeObject(Object obj) throws IOException {
		// TODO Auto-generated method stub
		
		if (obj instanceof HandShake ) {
			((HandShake) obj).write(this);
		} else if (obj instanceof Message){
			((Message) obj).write(this);
		} else {
			throw new UnsupportedOperationException(" Message is not supported to write ");
		}
	}

	

}
