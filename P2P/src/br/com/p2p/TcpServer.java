package br.com.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.*;

import br.com.p2p.interfaces.ICypher;
import br.com.p2p.packets.*;
import br.com.p2p.util.*;

public class TcpServer {
	public String ip;
	public int port;
	private Object _lock = new Object();
	private ArrayList<Socket> clients;
	private ICypher cypher;
	
	public TcpServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.clients = new ArrayList<Socket>();
		this.cypher = new AES("E6A87940CC33A7F194A4B1E3691289EA");
	}
	
	public void run() {
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket server = new ServerSocket(port)) {
			while (true) {
				Socket client = server.accept();
				synchronized(_lock) {
					clients.add(client);
				}
				pool.execute(() -> {
					try {
						ReceiveLoop(client);
					} catch (IOException e) {}
				});
			}
		} catch (IOException e1) {}
	}
	
	private void ReceiveLoop(Socket client) throws IOException {
		try (InputStream is = client.getInputStream()) {
			while (true) {
				int len = ((0xff & is.read()) << 24 | (0xff & is.read()) << 16 | (0xff & is.read()) << 8 | (0xff & is.read()) << 0);
				byte[] buffer = is.readNBytes(len);
				buffer = this.cypher.decrypt(buffer);
				Handle(client, buffer);
			}
		} catch (IOException e) {
			Disconnect(client);
		}
	}
	
	private void Send(Socket client, byte[] data) throws IOException {
		data = this.cypher.encrypt(data);
		
		OutputStream out = client.getOutputStream();
		
		out.write((byte) (data.length >>> 24));
		out.write((byte) (data.length >>> 16));
		out.write((byte) (data.length >>> 8));
		out.write((byte) (data.length));
		out.write(data);
	}
	
	public void SendBroadcast(byte[] data) throws IOException {
		for(var client: clients) {
			this.Send(client, data);
		}
	}
	
	private void Disconnect(Socket client) throws IOException {
		synchronized(_lock) {
			clients.remove(client);
		}
	}
	
	private void Handle(Socket client, byte[] packet) throws IOException {
		EnumPacketType packetType = EnumPacketType.values()[(int)(short)(((packet[0] & 0xFF) << 8) | (packet[1] & 0xFF))];
		System.out.println("[SERVIDOR] -> RECEBEU -> "+packetType);
		switch(packetType) {
		case TestPacket:
			TestPacketHandle(client, new TestPacket(packet));
			break;
		case RequestDHT:
			RequestDHTHandle(client, new RequestDHT(packet));
			break;
		case RequestFileChunk:
			RequestFileChunkHandle(client , new RequestFileChunk(packet));
			break;
		case RequestConnection:
			RequestConnectionHandle(client , new RequestConnection(packet));
			break;
		default:
			break;
		
		}
	}
	
	private void TestPacketHandle(Socket client, TestPacket packet) {
		System.out.println(packet.Text);
	}
	
	private void RequestDHTHandle(Socket client, RequestDHT packet) throws IOException {
		ResponseDHT rdht = new ResponseDHT();
		rdht.DHT = FileUtil.GetFilesData("./downloads/");
		Send(client, rdht.Serialize());
	}
	
	private void RequestFileChunkHandle(Socket client, RequestFileChunk packet) throws IOException {
		ResponseFileChunk rfc = new ResponseFileChunk();
		rfc.FileName = packet.FileName;
		rfc.Hash = packet.Hash;
		rfc.offset = packet.offset;
		rfc.chunk = FileUtil.ReadFileChunk("./downloads/"+packet.FileName, packet.offset, packet.len);
		System.out.println("[SERVIDOR] -> Enviando -> "+rfc.FileName+" offset: "+rfc.offset+" len: "+rfc.chunk.length);
		Send(client, rfc.Serialize());
	}
	
	private void RequestConnectionHandle(Socket client, RequestConnection packet) throws IOException {
		System.out.println("[SERVIDOR] -> Solicitada conexão para o servidor: "+packet.ip+":"+packet.port);
		try {
			this.Send(client, new ResponseConnection().Serialize());
			var cl = new Client(packet.ip, packet.port);
			cl.Send(new RequestDHT().Serialize());
		}catch(UnknownHostException ex) {}
		catch(IOException ex) {}
	}
}
