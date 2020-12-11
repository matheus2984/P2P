package br.com.p2p.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import br.com.p2p.*;
import br.com.p2p.models.FileData;
import br.com.p2p.packets.*;
import br.com.p2p.util.FileUtil;
import br.com.p2p.util.StringFormat;

public class DownloadsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private String statusEmAndamento = "Em andamento", 
			statusPausado = "Pausado", 
			statusCancelado = "Cancelado", 
			statusConcluido = "Concluído";
	private int indexPausar = 3, 
			indexRetomar = 4, 
			indexCancelar = 5, 
			indexExcluir = 6;

	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public DownloadsPanel() throws Exception {
		setToolTipText("Meus downloads");
		Color defaultColor = new Color(240, 248, 255);
		Color white = new Color(255, 255, 255);
		Font defaultFontTitle = new Font("Tahoma", Font.BOLD, 12);
		setBackground(defaultColor);
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Meus downloads");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(defaultFontTitle);
		lblNewLabel.setBounds(10, 11, 808, 14);
		add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 36, 808, 333);
		add(scrollPane);
		
		DefaultTableModel model = new DefaultTableModel(getColumnsData(), getColumnNames());
	    table = new JTable(model) {
			private static final long serialVersionUID = 1L;

			@Override
	        public boolean isCellEditable(int row, int column) {
				if (column >= 3) {
					String statusArquivo = table.getModel().getValueAt(row, 2).toString();
					
					if (statusArquivo.equals(statusEmAndamento)) {
						return column == indexPausar || column == indexCancelar; 
					} else if (statusArquivo.equals(statusPausado)) {
						return column == indexRetomar || column == indexCancelar;
					} else if (statusArquivo.equals(statusConcluido) || statusArquivo.equals(statusCancelado)) {
						return column == indexExcluir;
					} 
				}
				return false;         
	        };
	    };
	    
	    table.setOpaque(true);
	    table.setFillsViewportHeight(true);
		table.setEnabled(true);
		table.setBorder(null);
		table.setBackground(white);
		setDoubleBuffered(true);
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setBackground(white);
		tableHeader.setFont(defaultFontTitle);

		addButtons();		
		scrollPane.setViewportView(table);
		
		SharedResources.setEvent("Download", new Callable<Integer>() {
			   public Integer call() throws Exception {
			        return UpdateScreen();
			   }
			});
	}
	
	public int UpdateScreen() throws Exception {
		System.out.println("[GUI] -> DOWNLOAD PANEL UPDATE");
		((DefaultTableModel)table.getModel()).setNumRows(0);
		
		HashMap<String, Download> list = SharedResources.Downloads;
		for (var item : list.entrySet()) {
			FileData f =  SharedResources.GlobalDHT.get(item.getKey());

			Vector<Object> arquivoVector = new Vector<Object>();

			arquivoVector.add(f.Name);
			arquivoVector.add(StringFormat.byteFormat(f.Size));
			arquivoVector.add(item.getValue().getStatus());
			arquivoVector.add("");
			arquivoVector.add("");
			arquivoVector.add("");
			arquivoVector.add("");
			
			((DefaultTableModel)table.getModel()).addRow(arquivoVector);
		}
		
		return 0;
	}
	
	private Vector<Vector<Object>> getColumnsData(){
		/*ArrayList<FileViewModel> arquivos = new ArrayList<FileViewModel>();
		int rowCount = 0;

		List<FileData> downloadedFiles = FileUtil.GetFilesData("./downloads/");
		
		for(FileData file : downloadedFiles) {
			FileViewModel arq = new FileViewModel(file.Name, String.valueOf(file.Size/1024)+"KB", StatusDownloadEnum.Concluido);
			arquivos.add(arq);
		}*/
		
		Vector<Vector<Object>> dataColumns = new Vector<Vector<Object>>();

		/*for (FileViewModel a : arquivos) {
			Vector<Object> arquivoVector = new Vector<Object>();
		    arquivoVector.add(a.nome);
		    arquivoVector.add(a.tamanho);
		    arquivoVector.add(a.statusDownload.getText());
		    addActionButtonsNames(arquivoVector, a.statusDownload.codigoStatusDownload, rowCount);
		    rowCount++;
		    dataColumns.add(arquivoVector);
		}
		*/
		return dataColumns;
	}
	
	private void addActionButtonsNames(Vector<Object> obj, int codigoStatusDownload, int row) {		
		if (codigoStatusDownload == CodigoStatusDownloadConstant.EmAndamento) {
			obj.add("Pausar");
			obj.add("");
		    obj.add("Cancelar");
		    obj.add("");
		} else if (codigoStatusDownload == CodigoStatusDownloadConstant.Pausado) {
			obj.add("");
			obj.add("Retormar");
		    obj.add("Cancelar");
		    obj.add("");
		 } else if (codigoStatusDownload == CodigoStatusDownloadConstant.Concluido || 
				codigoStatusDownload == CodigoStatusDownloadConstant.Cancelado) {
			obj.add("");
			obj.add("");
		    obj.add("");
		    obj.add("Limpar");
		} 
	}
	
	private Vector<String> getColumnNames() {
		Vector<String> nameColumns = new Vector<String>();
		nameColumns.add("Nome");
		nameColumns.add("Tamanho");
		nameColumns.add("Status");
		nameColumns.add(""); //pausar
		nameColumns.add(""); //retormar
		nameColumns.add(""); //cancelar
		nameColumns.add(""); //limpar da lista
		  
		return nameColumns;
	}

	private void addButtons() {
		addPausarDownloadButton();
		addRetomarDownloadButton();		
		addCancelarDownloadButton();
		addExcluirDownloadButton();
	}

	private void addPausarDownloadButton() {
		Action pausarDownload = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Trigger para pausar download");
			}
		    
		};		
		ButtonColumn btnPausar = new ButtonColumn(table, pausarDownload, indexPausar);
		btnPausar.setMnemonic(KeyEvent.VK_D);
	}
	
	private void addRetomarDownloadButton() {
		Action retomarDownload = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Trigger para retomar download");
			}
		    
		};
		
		ButtonColumn btnRetomar = new ButtonColumn(table, retomarDownload, indexRetomar);
		btnRetomar.setMnemonic(KeyEvent.VK_D);
	}
	
	private void addCancelarDownloadButton() {
		Action cancelarDownload = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Trigger para cancelar download");
			}
		    
		};
		
		ButtonColumn btnCancelar = new ButtonColumn(table, cancelarDownload, indexCancelar);
		btnCancelar.setMnemonic(KeyEvent.VK_D);
	}
	
	private void addExcluirDownloadButton() {
		Action excluirDownload = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//removendo da lista do JPanel
				JTable table = (JTable)e.getSource();
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        ((DefaultTableModel)table.getModel()).removeRow(modelRow);
		        
				System.out.println("Trigger para excluir download no backend");
			}
		};
		ButtonColumn btnExcluir = new ButtonColumn(table, excluirDownload, indexExcluir);
		btnExcluir.setMnemonic(KeyEvent.VK_D);
	}
}
