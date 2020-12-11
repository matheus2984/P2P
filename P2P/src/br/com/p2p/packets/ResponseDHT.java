package br.com.p2p.packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import br.com.p2p.models.*;
import br.com.p2p.interfaces.*;

public class ResponseDHT implements IPacket {
	public EnumPacketType Type;
	public ArrayList<FileData> DHT;
	
	public ResponseDHT()
	{
		this.Type = EnumPacketType.ResponseDHT;
	}
	
	public ResponseDHT(byte[] packet)
	{
		this.Type = EnumPacketType.ResponseDHT;
		this.Deserialize(packet);
	}
	
	public byte[] Serialize() {
		int PacketSize = (short)(6 + 16*this.DHT.size());
		for(var item: this.DHT) {
			PacketSize += item.Hash.length() + item.Name.length();
		}
		
		ByteBuffer data = ByteBuffer.allocate(PacketSize);
		data.putShort(this.Type.getValue());
		data.putInt(this.DHT.size());
		for(var item: this.DHT) {
			data.putInt(item.Hash.length());
			data.put(item.Hash.getBytes());
			
			data.putInt(item.Name.length());
			data.put(item.Name.getBytes());
			
			data.putLong(item.Size);
		}
		return data.array();
	}

	public void Deserialize(byte[] packet) {
		ByteBuffer data = ByteBuffer.wrap(packet);
		this.Type = EnumPacketType.values()[(int)data.getShort()];
		int lenDHT = data.getInt();
		this.DHT = new ArrayList<FileData>(lenDHT);
		
		for(int i = 0; i < lenDHT; i++) {
			FileData fd = new FileData();
			
			int len = data.getInt();
			byte[] buffer = new byte[len]; 
			data.get(buffer);
			fd.Hash = new String(buffer);
			
			len = data.getInt();
			buffer = new byte[len]; 
			data.get(buffer);
			fd.Name = new String(buffer);
			
			fd.Size = data.getLong();
			
			this.DHT.add(fd);
		}
	}
}
