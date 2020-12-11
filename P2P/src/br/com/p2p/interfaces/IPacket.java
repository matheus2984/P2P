package br.com.p2p.interfaces;

public interface IPacket {
	byte[] Serialize();
	void Deserialize(byte[] packet);
}