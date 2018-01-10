import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

public class p2pInputstream extends DataInputStream implements ObjectInput{

	private boolean isHandShakereceived = false;
	
	public p2pInputstream(InputStream instream) {
		super(instream);
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		if (isHandShakereceived) {
			/* Identify the payload length and create a Message instance */
			int msgPayloadLen = readInt() - 1;
			
			Message msg =  Message.getInstance(msgPayloadLen, MessageTypes.getType(readByte()));
			//TODO
			msg.read(this);
			
			return msg;
			
		} else {
			
			/* Assumed that first one would be always handshake */
			HandShake handShakeMsg = new HandShake();
			handShakeMsg.read(this);
			isHandShakereceived = true;
			return handShakeMsg;
		}
		
		
	}
	

}
