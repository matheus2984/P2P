package br.com.p2p.gui;

public class FileViewModel {
	public String nome;
	public String tamanho;
	public String hash;
	public StatusDownloadEnum statusDownload;
	public String servidorOrigem;
	
	public FileViewModel() {
		
	}

	public FileViewModel(String nome, String tamanho, StatusDownloadEnum statusDownload) {
		super();
		this.nome = nome;
		this.tamanho = tamanho;
		this.statusDownload = statusDownload;
	}

	public FileViewModel(String nome, String tamanho, String hash, String servidorOrigem) {
		super();
		this.nome = nome;
		this.tamanho = tamanho;
		this.hash = hash;
		this.servidorOrigem = servidorOrigem;
	}	
}
