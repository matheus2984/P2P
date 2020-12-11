package br.com.p2p.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import br.com.p2p.*;

import javax.swing.JTabbedPane;

public class ArquivosFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public static void Run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ArquivosFrame frame = new ArquivosFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	
	
	public ArquivosFrame() throws Exception {
		setResizable(false);
		setTitle("Distribuição de Arquivos Multimídia. PORTA: "+SharedResources.ServerPort);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 475);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		ArquivosPanel arquivosPanel = new ArquivosPanel();
		tabbedPane.addTab("Arquivos", arquivosPanel);
		DownloadsPanel downloadsPanel = new DownloadsPanel();
		tabbedPane.addTab("Meus downloads", downloadsPanel);

		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		SharedResources.setEvent("ResponseFileChunk", new Callable<Integer>() {
			   public Integer call() {
			        return UpdateScreen();
			   }
			});
	}
	
	public int UpdateScreen() {
		System.out.println("ResponseFileChunk EVENT");
		return 0;
	}
}
