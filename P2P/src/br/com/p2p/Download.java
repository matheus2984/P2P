package br.com.p2p;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import br.com.p2p.models.*;
import br.com.p2p.packets.*;
import br.com.p2p.util.*;

public class Download {
	private String path;
	private FileData file;
	private ArrayList<Client> providers;
	private Queue<Chunk> chunks;
	private Queue<Chunk> downloaded;
	private Object _lock = new Object();
	private int qtdChunks;
	private String status;
	
	private ArrayList<Integer> chunkage(long val, int max){
		long count = Math.abs(val/max);
		long rem = Math.abs(val % max);
		int size = (int)(count + (rem == 0 ? 0 : 1));
		ArrayList<Integer> output = new ArrayList<Integer>(size);
		
		for(int i = 0; i < size ; i++)
		{
		    if (i == size - 1)
		        output.add((int)rem);
		    else
		    	output.add((int)max);
		}
		return output;
	}
	
	public Download(String path, FileData file, ArrayList<Client> providers) throws IOException {
		this.path = path;
		this.file = file;
		this.providers = providers;
		this.chunks = new ArrayDeque<Chunk>();
		this.downloaded = new ArrayDeque<Chunk>();
		
		FileUtil.AllocateFile(path+file.Name, file.Size);
		
		long maxSize = file.Size/providers.size();
		if(maxSize > 4096)
			maxSize = 4096;
		
		var _chunks = chunkage(file.Size, (int)maxSize);
		int offset = 0;
		
		for(var item: _chunks) {
			var chunk = new Chunk();
			chunk.len = item;
			chunk.offset = offset;
			offset += chunk.len;
			chunks.add(chunk);
		}
		
		this.qtdChunks = chunks.size();
	}
	
	public void Start() throws IOException, InterruptedException {
		new Thread(()->{
		
		while(chunks.size() > 0) {
			synchronized(_lock) {
					for(var provider: providers) {
						var chunk = chunks.poll();
						var rfc = new RequestFileChunk();
						rfc.FileName = this.file.Name;
						rfc.Hash = this.file.Hash;
						rfc.offset = chunk.offset;
						rfc.len = chunk.len;
						
						try {
							provider.Send(rfc.Serialize());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		
						System.out.println("Faltam: "+chunks.size());
						if(chunks.size() == 0) break;
					}
			}
		}
		

		}).start();
	}
	
	public boolean WriteChunk(ResponseFileChunk rfc) throws IOException {
		var chunk = new Chunk();
		chunk.offset = (int) rfc.offset;
		chunk.len = rfc.chunk.length;
		
		FileUtil.WriteFileChunk(path+this.file.Name, rfc.chunk, rfc.offset);
		
		downloaded.add(chunk);
		if(downloaded.size() == this.qtdChunks) {
			
			
			if(FileUtil.GetSha256OfFile(path+this.file.Name).equals(file.Hash)) {
				this.status = "Concluído";
			}else {
				this.status = "ERR0 HASH INVÁLIDO";
			}
			
			return true;
		}else {
			var percent = 100*((float)(downloaded.size())/(float)(this.qtdChunks));
			this.status = "Em andamento ("+(int)Math.ceil(percent)+"%)";
		}
		return false;
	}
	
	public void Stop() {
		this.status = "Pausado";
	}
	
	public void Continue() {

	}
	
	public String getStatus() {
		return this.status;
	}
	
	protected class Chunk{
		public int offset;
		public int len;
	}
}
