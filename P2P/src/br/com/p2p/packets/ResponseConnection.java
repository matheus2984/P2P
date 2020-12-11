package br.com.p2p.packets;

import java.nio.ByteBuffer;
import br.com.p2p.interfaces.*;

public class ResponseConnection implements IPacket {
	public EnumPacketType Type;
	public String ip;
	public short port;
	
	public ResponseConnection()
	{
		this.Type = EnumPacketType.ResponseConnection;
	}
	
	public ResponseConnection(byte[] packet)
	{
		this.Type = EnumPacketType.ResponseConnection;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(2);
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue());
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
	}
}
