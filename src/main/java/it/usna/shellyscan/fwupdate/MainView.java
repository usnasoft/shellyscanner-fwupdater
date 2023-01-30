package it.usna.shellyscan.fwupdate;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class MainView extends JFrame {
	private static final long serialVersionUID = 1L;
	private FWUpdater upd = new FWUpdater();
	public final static ResourceBundle LABELS = ResourceBundle.getBundle("LabelsBundle");

	public MainView() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {}
		}
		
		setTitle(Main.APP_NAME);
		setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/ShSc24.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.NORTH);
		
		JPanel dPanel = new DownloadPanel(this);
		tabbedPane.addTab(LABELS.getString("downloadTab"), null, dPanel, null);
		JPanel uPanel = new UpdatePanel(this);
		tabbedPane.addTab(LABELS.getString("updateTab"), null, uPanel, null);
		setVisible(true);
		pack();
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
	}

	@Override
	public void dispose() {
		upd.stop();
		super.dispose();
		System.exit(0);
	}
}