package msmb.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import msmb.utility.Constants;

public class ProgressBarFrame extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private JTextArea taskOutput;
	private JPanel newContentPane;
	
	public ProgressBarFrame(JFrame owner, String title) throws InterruptedException {
		setTitle(title);
		initialize();
	}
		
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		newContentPane = new JPanel();
	    newContentPane.setOpaque(true); 
	    setContentPane(newContentPane);
	  
	    newContentPane.setLayout(new BorderLayout());

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		taskOutput = new JTextArea();
		taskOutput.setMargin(new Insets(3,3,3,3));
		taskOutput.setEditable(false);
		taskOutput.setFont(new Font("Serif", Font.PLAIN, 13));
		taskOutput.setLineWrap(true);
		taskOutput.setWrapStyleWord(true);
		
		JPanel panel = new JPanel();
		panel.add(progressBar);

		add(panel, BorderLayout.NORTH);
		add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		newContentPane.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		newContentPane.add(taskOutput, BorderLayout.CENTER);
		
		setSize(195, 268);
		setResizable(false);
		setLocationRelativeTo(null); 
	    
	    pack();
		newContentPane.revalidate();
		progressBar.revalidate();
		getContentPane().paintAll(progressBar.getGraphics());
		newContentPane.repaint();
	
		this.setVisible(true);
		
		
	}




	public void progress(int prog) {
		if(prog== this.progress) return;
		this.progress = prog;
		progressBar.setValue(prog);
		taskOutput.append(Constants.ProgressBar.getDescriptionFromProgress(progressBar.getValue())+"\n");
		SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        	taskOutput.setCaretPosition(taskOutput.getText().length());
        }
		});
		taskOutput.revalidate();
		pack();
		newContentPane.revalidate();
		progressBar.revalidate();
		getContentPane().paintAll(newContentPane.getGraphics());
		newContentPane.repaint();
	}
	
	public int progress = 0;
	
	
	
	public void run() {
		try {
			while(progress<100) { 	
				synchronized (this) {
					this.wait();
				}
				this.progress(progress);
			}
			this.dispose();

		} catch (InterruptedException e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				e.printStackTrace();
		}
    }

	

   
}  



