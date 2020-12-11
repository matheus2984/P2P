package br.com.p2p.packets;

import java.nio.ByteBuffer;
import br.com.p2p.interfaces.*;

public class RequestConnection implements IPacket {
	public EnumPacketType Type;
	public String ip;
	public short port;
	
	public RequestConnection()
	{
		this.Type = EnumPacketType.RequestConnection;
	}
	
	public RequestConnection(byte[] packet)
	{
		this.Type = EnumPacketType.RequestConnection;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(6 + this.ip.length());
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue()); // 2
		data.putShort((short)this.ip.length());
		data.put(this.ip.getBytes());
		data.putShort(this.port);
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
		short len = data.getShort();
		byte[] buffer = new byte[len];
		data.get(buffer);
		this.ip = new String(buffer);
		this.port = data.getShort();
	}
}
