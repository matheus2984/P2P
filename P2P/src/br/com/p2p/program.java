package br.com.p2p;

import java.io.IOException;
import java.net.UnknownHostException;

import br.com.p2p.gui.*;
import br.com.p2p.packets.*;
import br.com.p2p.util.*;

public class program {

	public static void main(String[] args) throws IOException, InterruptedException {
		String[] config = FileUtil.ReadAllTextFile("./config.txt").split(":");
		String[] peers = FileUtil.ReadAllTextFile("./peers.txt").split("\\r?\\n");
		SharedResources.ServerPort = Integer.parseInt(config[1]);
		
		new Thread(()->ArquivosFrame.Run()).start();
		Thread.sleep(300);
		
		SharedResources.MyServer = new TcpServer(config[0], Integer.parseInt(config[1]));
		new Thread(()-> SharedResources.MyServer.run()).start();
		
		synchronized(SharedResources.SharedLock) {
			for(var fdt: FileUtil.GetFilesData("./download")) {
				SharedResources.MyDHT.put(fdt.Hash, fdt);
			}
		}
		
		for(var peer: peers) {
			var addr = peer.split(":");
			var ip = addr[0];
			var port = Integer.parseInt(addr[1]);
			
			try {
				var client = new Client(ip, port);
				RequestConnection rc = new RequestConnection();
				rc.ip = SharedResources.MyServer.ip;
				rc.port = (short) SharedResources.MyServer.port;
				client.Send(rc.Serialize());
			}catch(UnknownHostException ex) {}
			catch(IOException ex) {}
		}	
	}
}
