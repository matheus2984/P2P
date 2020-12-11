package br.com.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import br.com.p2p.interfaces.ICypher;
import br.com.p2p.packets.*;
import br.com.p2p.util.AES;
import br.com.p2p.util.FileUtil;

public class TcpClient {
	public String ip;
	public int port;
	private Socket socket;
	private ICypher cypher;
	
	public TcpClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.cypher = new AES("E6A87940CC33A7F194A4B1E3691289EA");
	}
	
	public void run() throws UnknownHostException, IOException {
			this.socket = new Socket(this.ip, this.port);
			new Thread(() -> {
					try {
						ReceiveLoop(this.socket);
					} catch (IOException e) {
						Disconnect(this.socket);
					}
			}).start();
	}
	
	private void ReceiveLoop(Socket socket) throws IOException {
		try (InputStream is = socket.getInputStream()) {
			while (true) {
				int len = ((0xff & is.read()) << 24 | (0xff & is.read()) << 16 | (0xff & is.read()) << 8 | (0xff & is.read()) << 0);
				byte[] buffer = is.readNBytes(len);
				buffer = this.cypher.decrypt(buffer);
				Handle(socket, buffer);
			}
		} catch (IOException e) {
			Disconnect(socket);
		}
	}
	
	public void Send(byte[] data) throws IOException {
		data = this.cypher.encrypt(data);
		OutputStream out = socket.getOutputStream();

		out.write((byte) (data.length >>> 24));
		out.write((byte) (data.length >>> 16));
		out.write((byte) (data.length >>> 8));
		out.write((byte) (data.length));
		out.write(data);
	}
	
	protected void Disconnect(Socket socket) {
	}
	
	protected void Handle(Socket socket, byte[] packet) throws IOException {
	}
}
