package br.com.p2p.gui;

public enum StatusDownloadEnum {
	EmAndamento(CodigoStatusDownloadConstant.EmAndamento),
	Pausado(CodigoStatusDownloadConstant.Pausado),
	Concluido(CodigoStatusDownloadConstant.Concluido),
	Cancelado(CodigoStatusDownloadConstant.Cancelado);
	
	public int codigoStatusDownload;

	StatusDownloadEnum(int codigo) {
		codigoStatusDownload = codigo;
	}
	
	public String getText() {
		switch(this) {
		case EmAndamento:
			return "Em andamento";
		case Pausado:
			return "Pausado";
		case Concluido:
			return "Concluído";
		case Cancelado:
			return "Cancelado";
		default:
			return "";
		}
	}
}
