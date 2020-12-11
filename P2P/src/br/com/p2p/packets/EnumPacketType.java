package br.com.p2p.packets;

public enum EnumPacketType {
	RequestDHT(0x00),
	ResponseDHT(0x01),
	TestPacket(0x02),
	RequestFileChunk(0x03),
	ResponseFileChunk(0x04),
	RequestConnection(0x05),
	ResponseConnection(0x06),
	SendDHT(0x07);
	
	private short value;
	
	EnumPacketType(int value){
		this.value = (short)value;
	}
	
	public short getValue() {
		return this.value;
	}
}