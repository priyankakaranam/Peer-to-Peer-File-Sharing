import java.util.BitSet;

public class requestedPieces {

	private BitSet requestPieceInfo = null;
	private int unChokingInterval = 0;
	
	public requestedPieces(int numOfPieces, int unChokingInterval) {
	
		this.requestPieceInfo = new BitSet(numOfPieces);
		this.unChokingInterval = unChokingInterval;
	}
	
	public BitSet getRequestPieceInfo() {
		return requestPieceInfo;
	}
	public void setRequestPieceInfo(BitSet requestPieceInfo) {
		this.requestPieceInfo = requestPieceInfo;
	}

	synchronized int getPiecestoRequest(BitSet remotePeerPieces) {
		remotePeerPieces.andNot(this.requestPieceInfo);
		
		if (!remotePeerPieces.isEmpty()) {
			//Convert the available requestPieceInfo to String and then Pick 1
			String str = remotePeerPieces.toString();
			String[] strarray = str.substring(1, str.length()-1).split(",");
			
			
			final int pieceIndex = Integer.parseInt(strarray[(int)(Math.random()*(strarray.length-1))].trim());
			System.out.println("getting the Piece Index as : "+pieceIndex);
			requestPieceInfo.set(pieceIndex);
			
			
			/* set a timer to clear this Info */
			new java.util.Timer().schedule(
					new java.util.TimerTask() {

						@Override
						public void run() {
							synchronized (requestPieceInfo) {
								requestPieceInfo.clear(pieceIndex);
								System.out.println("Clearing the PieceIndex " + pieceIndex);
							}
							
						}
						
					}, unChokingInterval * 2 * 1000);
			
			
			return pieceIndex;
			
		} else {
			System.out.println("Getting Index as -1");
			return -1;
		}
	}
	
	

}
