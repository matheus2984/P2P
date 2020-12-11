package br.com.p2p;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import br.com.p2p.models.*;
import br.com.p2p.packets.*;
import br.com.p2p.util.*;

public class Client extends TcpClient{
	public Map<String, FileData> DHT = new HashMap<String, FileData>();

	public Client(String ip, int port) throws UnknownHostException, IOException {
		super(ip, port);
		super.run();
		
		synchronized(SharedResources.SharedLock) {
			SharedResources.Providers.add(this);
		}
	}
	
	public void Send(byte[] data) throws IOException {
		super.Send(data);
	}
	
	@Override
	protected void Disconnect(Socket socket) {
		synchronized (SharedResources.SharedLock) {
			for(var dhtItem: this.DHT.entrySet()) {
				int count = 0;
				for(var provider: SharedResources.Providers) {
					if(provider == this) continue;
					if(provider.DHT.containsKey(dhtItem.getKey())) {
						count++;
					}
				}
				
				if(count == 0) {
					SharedResources.GlobalDHT.remove(dhtItem.getKey());
				}
			}
			
			SharedResources.Providers.remove(this);
		}
		SharedResources.triggerEvent("UpdateDHT");
	}
	
	@Override
	protected void Handle(Socket socket, byte[] packet) throws IOException {
		EnumPacketType packetType = EnumPacketType.values()[(int)(short)(((packet[0] & 0xFF) << 8) | (packet[1] & 0xFF))];
		System.out.println("[CLIENT] -> RECEBEU -> "+packetType);
		switch(packetType) {
		case TestPacket:
			TestPacketHandle(socket, new TestPacket(packet));
			break;
		case ResponseDHT:
			ResponseDHTHandle(socket, new ResponseDHT(packet));
			break;
		case ResponseFileChunk:
			ResponseFileChunkHandle(socket, new ResponseFileChunk(packet));
			break;
		case SendDHT:
			SendDHTHandle(socket, new SendDHT(packet));
			break;
		case ResponseConnection:
			ResponseConnectionHandle(socket, new ResponseConnection(packet));
			break;
		default:
			break;
		
		}
	}
	
	private void TestPacketHandle(Socket socket, TestPacket packet) {
		System.out.println(packet.Text);
	}
	
	private void ResponseDHTHandle(Socket socket, ResponseDHT packet) {
		synchronized(SharedResources.SharedLock) {
			for(var item: packet.DHT) {
				DHT.put(item.Hash, item);
				SharedResources.GlobalDHT.put(item.Hash, item);
			}
			SharedResources.triggerEvent("UpdateDHT");
		}
	}
	
	private void ResponseFileChunkHandle(Socket socket, ResponseFileChunk packet) throws IOException {
		synchronized(SharedResources.SharedLock) {
			if(SharedResources.Downloads.get(packet.Hash).WriteChunk(packet)) {
				System.out.println("Download concluído");
				var fdata = SharedResources.GlobalDHT.get(packet.Hash);
				this.DHT.put(packet.Hash, fdata);
				SharedResources.MyDHT.put(packet.Hash, fdata);
				
				ArrayList<FileData> dht = new ArrayList<FileData>();
				
				for(var item: SharedResources.MyDHT.entrySet()) {
					dht.add(item.getValue());
				}
				
				SendDHT sdht = new SendDHT();
				sdht.DHT = dht;
				SharedResources.MyServer.SendBroadcast(sdht.Serialize());
				
				SharedResources.UpdateScreen = 0;
				
				SharedResources.triggerEvent("Download");
			}
			else {
				SharedResources.UpdateScreen++;
				if(SharedResources.UpdateScreen > 100) {
					SharedResources.UpdateScreen = 0;
					SharedResources.triggerEvent("Download");
				}
			}
		}
	}
	
	private void SendDHTHandle(Socket client, SendDHT packet) {
		synchronized(SharedResources.SharedLock) {
			for(var item: packet.DHT) {
				DHT.put(item.Hash, item);
	
				SharedResources.GlobalDHT.put(item.Hash, item);
			}
			SharedResources.triggerEvent("UpdateDHT");
		}
	}
	
	private void ResponseConnectionHandle(Socket client, ResponseConnection packet) throws IOException {
		this.Send(new RequestDHT().Serialize());
	}
}
