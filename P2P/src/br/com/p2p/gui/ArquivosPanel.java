package br.com.p2p.gui;

import java.awt.*;

import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.table.*;

import br.com.p2p.*;
import br.com.p2p.models.FileData;
import br.com.p2p.util.StringFormat;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

public class ArquivosPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField tfNomeArquivo;
	private JTable table;
	private String filter = "";
	
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public int UpdateScreen() throws Exception {
		System.out.println("[GUI] -> DHT PANEL UPDATE");
		((DefaultTableModel)table.getModel()).setNumRows(0);
		
		Map<String, FileData> list = SharedResources.GlobalDHT;

		for (var item : list.entrySet()) {
			FileData f = item.getValue();
			
			if(this.filter.length() > 0 && !f.Name.startsWith(this.filter)) continue;
			
			Vector<Object> arquivoVector = new Vector<Object>();

			arquivoVector.add(f.Name);
			arquivoVector.add(StringFormat.byteFormat(f.Size));
			arquivoVector.add(f.Hash);
			
			String origin = "";
			
			synchronized(SharedResources.SharedLock) {
				for(var provider: SharedResources.Providers)
				{
					if(provider.DHT.containsKey(f.Hash)) {
						origin += provider.ip+":"+provider.port+"|";
					}
				}
			}
			
			arquivoVector.add(origin);
			arquivoVector.add("Download");
			
			((DefaultTableModel)table.getModel()).addRow(arquivoVector);
		}
		
		return 0;
	}
	
	public ArquivosPanel() throws Exception {
		setToolTipText("Arquivos");
		Color defaultColor = new Color(240, 248, 255);
		Color white = new Color(255, 255, 255);
		Font defaultFontTitle = new Font("Tahoma", Font.BOLD, 12);
		setBackground(defaultColor);
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Nome do arquivo");
		lblNewLabel.setBounds(10, 11, 104, 15);
		add(lblNewLabel);
		lblNewLabel.setFont(defaultFontTitle);
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		tfNomeArquivo = new JTextField();
		tfNomeArquivo.setBounds(10, 37, 810, 20);
		add(tfNomeArquivo);
		tfNomeArquivo.setColumns(10);
		
		JButton btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.setBounds(716, 68, 104, 23);
		add(btnPesquisar);
		btnPesquisar.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnPesquisar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Trigger para pesquisar arquivos a serem baixados");
				setFilter(tfNomeArquivo.getText());
			}
		});
		
		JButton btnLimpar = new JButton("Limpar");
		btnLimpar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				limparFiltro();
				
			}
		});
		btnLimpar.setBounds(623, 68, 83, 23);
		add(btnLimpar);
		btnLimpar.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 102, 810, 262);
		add(scrollPane);
		
	    DefaultTableModel model = new DefaultTableModel(getColumnsData(), getColumnNames());
	    table = new JTable(model) {
			private static final long serialVersionUID = 1L;

			//habilitando apenas botão de download
			@Override
	        public boolean isCellEditable(int row, int column) {
				return column == 4;         
	        };
	    };
	   	    
	    table.setOpaque(true);
	    table.setFillsViewportHeight(true);
		table.setEnabled(true);
		table.setBorder(null);
		setDoubleBuffered(true);
		table.setBackground(white);
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setBackground(white);
		tableHeader.setFont(defaultFontTitle);
		
		Action download = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Trigger para iniciar download ");
				
				table.getSelectedRow();
				@SuppressWarnings("unchecked")
				Vector<Object> row = ((DefaultTableModel)table.getModel()).getDataVector().get(table.getSelectedRow());
				System.out.println("Trigger Solicitando arquivo: "+row.get(0));
				String hash = (String) row.get(2);
				
				try {
					ArrayList<Client> fileProviders = new ArrayList<Client>();
					synchronized(SharedResources.SharedLock) {
						for(var provider: SharedResources.Providers) {
							if(provider.DHT.containsKey(hash)) {
								fileProviders.add(provider);
							}
						}
					}
					var f = fileProviders.get(0).DHT.get(hash);
					synchronized(SharedResources.SharedLock) {
						var download = new Download("./downloads/", f, fileProviders);
						SharedResources.Downloads.put(f.Hash, download);
						download.Start();
					}
				}
				catch(IOException ex) {} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		    
		};
		 
		ButtonColumn buttonColumn = new ButtonColumn(table, download, 4);
		buttonColumn.setMnemonic(KeyEvent.VK_D);
		
		scrollPane.setViewportView(table);
		
	
		SharedResources.setEvent("UpdateDHT", new Callable<Integer>() {
			   public Integer call() throws Exception {
			        return UpdateScreen();
			   }
			});
	}
	
	//Retorna a lista de arquivos a ser exibida
	private Vector<Vector<Object>> getColumnsData() throws Exception{
		//TODO: obter a lista de arquivos no tipo ArrayList<viewmodels.FileViewModel>
		ArrayList<FileViewModel> arquivos = new ArrayList<FileViewModel>();
		
		/*FileViewModel arquivo1 = new FileViewModel("Nome arquivo teste 1", "12 KB", "AUAFSD$F52314353", "Servidor B");
		FileViewModel arquivo2 = new FileViewModel("Nome arquivo teste 2", "35 KB", "EUR45F$&*567TRGD", "Servidor A");		
		
		arquivos.add(arquivo1);
		arquivos.add(arquivo2);*/
		
		
		Vector<Vector<Object>> dataColumns = new Vector<Vector<Object>>();

		for(FileViewModel a : arquivos) {
			Vector<Object> arquivoVector = new Vector<Object>();

			arquivoVector.add(a.nome);
			arquivoVector.add(a.tamanho);
			arquivoVector.add(a.hash);
			arquivoVector.add(a.servidorOrigem);
			arquivoVector.add("Download");
			
			dataColumns.add(arquivoVector);
		}
		
		
		return dataColumns;
	}
	
	private Vector<String> getColumnNames() {
		Vector<String> nameColumns = new Vector<String>();
		nameColumns.add("Nome");
		nameColumns.add("Tamanho");
		nameColumns.add("Hash");
		nameColumns.add("Servidor");
		nameColumns.add("");
		  
		return nameColumns;
	}
	
	private void setFilter(String value) {
		this.filter = value;
		try {
			UpdateScreen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void limparFiltro() {
		  tfNomeArquivo.setText("");
		  this.filter = "";
		  try {
			UpdateScreen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
