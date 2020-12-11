package br.com.p2p;

import java.util.*;
import java.util.concurrent.Callable;

import br.com.p2p.models.*;

public class SharedResources {
	public static Object SharedLock = new Object();
	public static TcpServer MyServer;
	public static HashMap<String, FileData> MyDHT = new HashMap<String, FileData>();
	public static HashMap<String, FileData> GlobalDHT = new HashMap<String, FileData>();
	public static HashMap<String, Download> Downloads = new HashMap<String, Download>();
	public static ArrayList<Client> Providers = new ArrayList<Client>();
	public static int UpdateScreen = 0;
	private static HashMap<String, ArrayList<Callable<Integer>>> events =  new HashMap<String, ArrayList<Callable<Integer>>>();
	public static int ServerPort;
	
	public static void setEvent(String key, Callable<Integer> evt) {
		if(events.get(key) != null) {
			events.get(key).add(evt);
		}
		else {
			ArrayList<Callable<Integer>> lst = new ArrayList<Callable<Integer>>();
			lst.add(evt);
			events.put(key, lst);
		}
	}
	
	public static void triggerEvent(String key) {
		synchronized(SharedLock) {
			try {
				ArrayList<Callable<Integer>> lst = events.get(key);
				for(Callable<Integer> evt: lst) {
					evt.call();
				}
			} catch (Exception e) {}
		}
	}
}
