package br.com.p2p.packets;

import java.nio.ByteBuffer;
import br.com.p2p.interfaces.*;

public class TestPacket implements IPacket{
	public EnumPacketType Type;
	public int len;
	public String Text;
	
	public TestPacket()
	{
		this.Type = EnumPacketType.TestPacket;
	}
	
	public TestPacket(byte[] packet)
	{
		this.Type = EnumPacketType.TestPacket;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(8 + this.Text.length());
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue()); // 2
		data.putInt(this.len); // 4
		data.putShort((short)this.Text.length()); //2
		data.put(this.Text.getBytes());
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
		this.len = data.getInt();
		int len = data.getShort();
		byte[] dataBuffer = new byte[len]; 
		data.get(dataBuffer);
		this.Text = new String(dataBuffer);
	}
}