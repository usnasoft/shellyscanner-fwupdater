package it.usna.shellyscan.fwupdate;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://api.shelly.cloud/files/firmware
public class DownloadPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final static String G1_CATALOG_URL = "https://api.shelly.cloud/files/firmware";

	// windowsbuilder
	public DownloadPanel() {
		this(null);
	}

	public DownloadPanel(MainView main) {
		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton(MainView.LABELS.getString("close"));
		btnNewButton.addActionListener(e -> main.dispose());
		panel.add(btnNewButton);
		
		try {
			init();
		} catch (IOException e1) {
//			JOptionPane.showMessageDialog("", e1);
			e1.printStackTrace();
		}
	}

	private void init() throws IOException {
		final URL url = new URL(G1_CATALOG_URL);
		URLConnection urlcon = url.openConnection();
		final ObjectMapper jsonMapper = new ObjectMapper();
		JsonNode node = jsonMapper.readTree(urlcon.getInputStream());
	}
}
