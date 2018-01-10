
public enum MessageTypes {
	/* Add the Types of the messages */
	Choke ((byte) 0),
	Unchoke ((byte) 1),
	Interested ((byte) 2),
	NotInterested ((byte) 3),
	Have ((byte) 4),
	BitField_Type ((byte) 5),
	Request ((byte) 6),
	Piece ((byte) 7);
	
	private byte MessageTypeVal;
	
	MessageTypes(byte msgTypeVal) {
		this.MessageTypeVal = msgTypeVal;
	}

	public byte getMessageTypeVal() {
		return MessageTypeVal;
	}

	public void setMessageTypeVal(byte messageTypeVal) {
		MessageTypeVal = messageTypeVal;
	}
	
	public static MessageTypes getType(byte b) {
		System.out.println("Incoming Message type" +b);
		
		for (MessageTypes m: MessageTypes.values()) {
		
			if (m.getMessageTypeVal() == b) {
				return m;
			}
		}
		
		throw new IllegalArgumentException();
	}
	
}
