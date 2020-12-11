package br.com.p2p.packets;
import java.nio.ByteBuffer;
import br.com.p2p.interfaces.*;

public class RequestFileChunk implements IPacket {
	public EnumPacketType Type;
	public long offset;
	public int len;
	public String FileName;
	public String Hash;
	
	public RequestFileChunk()
	{
		this.Type = EnumPacketType.RequestFileChunk;
	}
	
	public RequestFileChunk(byte[] packet)
	{
		this.Type = EnumPacketType.RequestFileChunk;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(18 + this.FileName.length() + this.Hash.length());
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue()); // 2
		data.putLong(this.offset); // 8
		data.putInt(this.len); // 4
		data.putShort((short)this.FileName.length()); //2
		data.put(this.FileName.getBytes());
		data.putShort((short)this.Hash.length()); //2
		data.put(this.Hash.getBytes());
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
		this.offset = data.getLong();
		this.len = data.getInt();
		int len = data.getShort();
		byte[] dataBuffer = new byte[len]; 
		data.get(dataBuffer);
		this.FileName = new String(dataBuffer);
		len = data.getShort();
		dataBuffer = new byte[len]; 
		data.get(dataBuffer);
		this.Hash = new String(dataBuffer);
	}
}