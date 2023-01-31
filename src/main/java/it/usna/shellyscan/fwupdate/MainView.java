package it.usna.shellyscan.fwupdate;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

public class MainView extends JFrame {
	private static final long serialVersionUID = 1L;
	private FWUpdater upd = new FWUpdater();
	private UpdatePanel updatePanel = new UpdatePanel(this, upd);
	public final static ResourceBundle LABELS = ResourceBundle.getBundle("LabelsBundle");

	public MainView() {
		setTitle(Main.APP_NAME + " " + Main.VER);
		setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/ShSc24.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.NORTH);
		
		JPanel dPanel = new DownloadPanel(this);
		tabbedPane.addTab(LABELS.getString("downloadTab"), null, dPanel, null);
		tabbedPane.addTab(LABELS.getString("updateTab"), null, updatePanel, null);
		setVisible(true);
		pack();
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
	}
	
	public void downloadSuccess(String path) {
		updatePanel.setFirmwareFilename(path);
	}
	
	public void info() {
		JEditorPane ep = new JEditorPane("text/html", "<html><h1><font color=#00005a>" + Main.APP_NAME + " <img src=\"usna16.gif\"></h1></font><p>" + LABELS.getString("aboutApp") + "</html>");
		ep.setEditable(false);
		((HTMLDocument)ep.getDocument()).setBase(getClass().getResource("/"));
		ep.addHyperlinkListener(ev -> {
			try {
				if(ev.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						Desktop.getDesktop().browse(new URI(ev.getURL().toString()));
					} else {
						JOptionPane.showMessageDialog(this, ev.getURL(), "", JOptionPane.PLAIN_MESSAGE);
					}
				}
			} catch (IOException | URISyntaxException ex) {}
		});
		JOptionPane.showMessageDialog(this, ep, Main.APP_NAME, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/ShSc24.png")));
	}

	@Override
	public void dispose() {
		upd.stop();
		super.dispose();
		System.exit(0);
	}
}