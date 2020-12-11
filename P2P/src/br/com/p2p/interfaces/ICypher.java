package br.com.p2p.interfaces;

public interface ICypher {
	byte[] encrypt(byte[] data);
	byte[] decrypt(byte[] data);
}
