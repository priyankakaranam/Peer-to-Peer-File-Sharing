
public class Request extends Message {

	Request(byte[] pieceIndex) {
		// TODO Auto-generated constructor stub
		super(MessageTypes.Request, pieceIndex);
	}
	
	public Request(int pieceIndex) {
		this(getPieceIndexinBytes(pieceIndex));
	}

}

