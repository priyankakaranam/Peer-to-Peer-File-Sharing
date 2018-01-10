import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.chrono.MinguoChronology;
import java.util.Arrays;

public class Message implements p2pInOut {
	
	private byte[] msgLeninBytes = new byte[4];
	
	private int msgLen = 0; 
	private MessageTypes msgType;
	protected byte[] msgPayload;
	
	public Message(MessageTypes msgType) {
		this(msgType, null);
	}
	
	public Message(MessageTypes msgType, byte[] msgPayload) {
		super();
		this.msgType = msgType;
		this.msgPayload = msgPayload;
		
		this.msgLen = (msgPayload == null ? 0 : msgPayload.length) + 1; 
	}

	public int getMsgLen() {
		return msgLen;
	}

	public void setMsgLen(int msgLen) {
		this.msgLen = msgLen;
	}

	public MessageTypes getMsgType() {
		return msgType;
	}

	public void setMsgType(MessageTypes msgType) {
		this.msgType = msgType;
	}

	public byte[] getMsgPayload() {
		return msgPayload;
	}

	public void setMsgPayload(byte[] msgPayload) {
		this.msgPayload = msgPayload;
	}


	public int getPieceIndex() {
		return ByteBuffer.wrap(Arrays.copyOfRange(msgPayload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	public static byte[] getPieceIndexinBytes(int pieceIndex) {
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceIndex).array();
	}
	
	public static Message getInstance(int msgPayloadlen , MessageTypes msgType) throws ClassNotFoundException {
		System.out.println("getInstance: " +msgType.getMessageTypeVal());
		switch(msgType) {
		
			case Choke:
				return new Choke();
			case Unchoke:
				return new Unchoke();
			case Interested:
				return new Interested();
			case NotInterested:
				return new NotInterested();
			case Have:
				return new Have(new byte[msgPayloadlen]);
			case BitField_Type:
				return new BitField_Type(new byte[msgPayloadlen]);
			case Request:
				return new Request(new byte[msgPayloadlen]);
			case Piece:
				return new Piece(new byte[msgPayloadlen]);
			default:
				throw new ClassNotFoundException( "Messagetype is not found" +msgType);
		}
	}
	
	
	public void read(DataInputStream mIn) throws IOException {
		/*read into the Msg payload */
		if (this.msgPayload != null && this.msgPayload.length != 0) {
			mIn.readFully(this.msgPayload, 0, msgPayload.length);
		}
	}
	
	public void write(DataOutputStream mOut) throws IOException {
		/*write into the socket */
		mOut.writeInt(msgLen);
		System.out.println("writing Message type" +msgType.getMessageTypeVal());
		mOut.writeByte(msgType.getMessageTypeVal());
		
		/*write into the Msg payload */
		if (msgPayload != null && msgPayload.length != 0) {
			mOut.write(msgPayload, 0, msgPayload.length);
		}
	}
}
