package it.usna.shellyscan.fwupdate;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

public class MainDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	private FWUpdater upd = new FWUpdater();
	private final static ResourceBundle LABELS = ResourceBundle.getBundle("LabelsBundle");

	public MainDialog() {
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
		
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		setContentPane(panel);
		setTitle(Main.APP_NAME);
		setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/ShSc24.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel selectionPanel = new JPanel();
		getContentPane().add(selectionPanel, BorderLayout.NORTH);
		selectionPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblNewLabel = new JLabel("Device IP (IP of the Shelly device to be updated)");
		selectionPanel.add(lblNewLabel);
		
		JPanel panelDevice = new JPanel(new BorderLayout(1, 0));
		selectionPanel.add(panelDevice);
		
		JTextField deviceIP = new JTextField();
		panelDevice.add(deviceIP, BorderLayout.CENTER);
		deviceIP.setColumns(15);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setToolTipText("Open web interface on system browser");
		btnBrowse.setPreferredSize(new Dimension(75, 16));
		btnBrowse.addActionListener(e -> {
			String shellyIP = deviceIP.getText();
			if (shellyIP.matches(Main.IPV4_REGEX) == false) {
				JOptionPane.showMessageDialog(this, "Invalid device IP.", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					Desktop.getDesktop().browse(new URI("http://" + shellyIP));
				} catch (IOException | URISyntaxException ex) {
					JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		panelDevice.add(btnBrowse, BorderLayout.EAST);
		
		JLabel lblNewLabel_1 = new JLabel("Firmware image file (zip file)");
		selectionPanel.add(lblNewLabel_1);
		
		JPanel panelFile = new JPanel(new BorderLayout(1, 0));
		selectionPanel.add(panelFile);
		
		JTextField fwFileName = new JTextField();
		fwFileName.setColumns(42);
		panelFile.add(fwFileName, BorderLayout.CENTER);
		
		JButton fileSelectorButton = new JButton("Select");
		fileSelectorButton.setPreferredSize(new Dimension(75, 16));
		fileSelectorButton.addActionListener(e -> {
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("zip file", "zip"));
			fc.setCurrentDirectory(new File(fwFileName.getText()));
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				fwFileName.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});
		panelFile.add(fileSelectorButton, BorderLayout.EAST);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(e -> dispose());
		
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(e -> {
			String shellyIP = deviceIP.getText();
			if (shellyIP.matches(Main.IPV4_REGEX) == false) {
				JOptionPane.showMessageDialog(this, "Invalid device IP.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String fileName = fwFileName.getText();
			if(fileName.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please select an image file.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			File fwFile = new File(fileName);
			if(fwFile.exists() == false) {
				JOptionPane.showMessageDialog(this, "Firmware image file not found.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			upd.stop();
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String res = upd.update(shellyIP, fwFile);
				String msg = "Device responce to update command:\n" + res + "\n\nDO NOT close main application window or begin a new update before this one is complete.";
				int ans = JOptionPane.showOptionDialog(this, msg, "Updating", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {"Close", "Check"}, null);
				if(ans == 1) {
					try {
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						Desktop.getDesktop().browse(new URI("http://" + shellyIP + "/shelly"));
					} catch (IOException | URISyntaxException ex) {
						JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
					} finally {
						setCursor(Cursor.getDefaultCursor());
					}
				}
			} catch (ConnectException ex) {
				JOptionPane.showMessageDialog(this, "Shelly device at " + shellyIP + " is not responding.", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (/*IO*/Exception ex) {
				JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				setCursor(Cursor.getDefaultCursor());
			}
		});
		
		buttonPanel.add(Box.createHorizontalStrut(24));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(btnUpdate);
		buttonPanel.add(btnClose);
		buttonPanel.add(Box.createHorizontalGlue());
		
		JButton btnInfo = new JButton(null, new ImageIcon(getClass().getResource("/Question24.png")));
		btnInfo.setContentAreaFilled(false);
		btnInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
		btnInfo.addActionListener(e -> {
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
			JOptionPane.showMessageDialog(MainDialog.this, ep, Main.APP_NAME, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/ShSc24.png")));
		});
		buttonPanel.add(btnInfo);
		
		pack();
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
		setVisible(true);
	}

	@Override
	public void dispose() {
		upd.stop();
		super.dispose();
		System.exit(0);
	}
}