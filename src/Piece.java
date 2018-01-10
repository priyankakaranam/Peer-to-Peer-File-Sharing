import java.util.Arrays;

public class Piece extends Message {

	public Piece(byte[] msgPayload) {
		super(MessageTypes.Piece, msgPayload);
	}
	
		
	public Piece(int index, byte[] pieceContent) {
		super(MessageTypes.Piece, Merge(index, pieceContent));
	}
	
	private static byte[] Merge(int index, byte[] pieceContent) {
		int pieceContentlen = (pieceContent == null) ? 0 : pieceContent.length;
		byte[] piecePayload = new byte[4 + pieceContentlen];
		System.arraycopy(getPieceIndexinBytes(index), 0, piecePayload, 0, 4);
		System.arraycopy(pieceContent, 0, piecePayload, 4, pieceContent.length);
		return piecePayload;
	}
	
	public byte[] getPieceContent() {
		if (msgPayload == null ) {
			return null;
		}
		
		return Arrays.copyOfRange(msgPayload, 4, msgPayload.length);
			
	}
	
	

}
