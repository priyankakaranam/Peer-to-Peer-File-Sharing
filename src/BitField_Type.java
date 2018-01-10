import java.util.BitSet;

public class BitField_Type extends Message {

	public BitField_Type(byte[] bitField) {
		super(MessageTypes.BitField_Type, bitField);
	}
	
	public BitField_Type(BitSet bitSet) {
		super(MessageTypes.BitField_Type, bitSet.toByteArray());
	}
	
	public BitSet getBitSet() {
		return BitSet.valueOf(msgPayload);
	}

}
