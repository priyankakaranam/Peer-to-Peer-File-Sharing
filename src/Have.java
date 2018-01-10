
public class Have extends Message {

	Have(byte[] pieceIndex) {
		// TODO Auto-generated constructor stub
		super(MessageTypes.Have, pieceIndex);
	}
	
	public Have(int pieceIndex) {
		this(getPieceIndexinBytes(pieceIndex));
	}

	//TODO , check if we have to read, read is being done in the Message
}
