package br.com.p2p.packets;

import java.nio.ByteBuffer;
import br.com.p2p.interfaces.*;

public class ResponseFileChunk implements IPacket{
	public EnumPacketType Type;
	public String FileName;
	public String Hash;
	public long offset;
	public byte[] chunk;
	
	public ResponseFileChunk()
	{
		this.Type = EnumPacketType.ResponseFileChunk;
	}
	
	public ResponseFileChunk(byte[] packet)
	{
		this.Type = EnumPacketType.ResponseFileChunk;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(18 + this.FileName.length() + this.chunk.length + this.Hash.length());
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue()); // 2
		data.putShort((short)this.FileName.length());
		data.put(this.FileName.getBytes());
		data.putShort((short)this.Hash.length());
		data.put(this.Hash.getBytes());
		data.putLong(this.offset);
		data.putShort((short)this.chunk.length); //2
		data.put(this.chunk);
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
		int len = data.getShort();
		byte[] dataBuffer = new byte[len]; 
		data.get(dataBuffer);
		this.FileName = new String(dataBuffer);
		len = data.getShort();
		dataBuffer = new byte[len]; 
		data.get(dataBuffer);
		this.Hash = new String(dataBuffer);
		this.offset = data.getLong();
		len = data.getShort();
		this.chunk = new byte[len]; 
		data.get(this.chunk);
	}
}
