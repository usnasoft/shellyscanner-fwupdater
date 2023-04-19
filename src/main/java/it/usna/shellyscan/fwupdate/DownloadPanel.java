package it.usna.shellyscan.fwupdate;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DownloadPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final static String G1_CATALOG_URL = "https://api.shelly.cloud/files/firmware";
	private JComboBox<FW> comboBox;

	// windowsbuilder
	public DownloadPanel() {
		this(null);
	}

	public DownloadPanel(MainView main) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

		JButton closeButton = new JButton(MainView.LABELS.getString("close"));
		closeButton.addActionListener(e -> main.dispose());

		JButton btnDownload = new JButton(MainView.LABELS.getString("downloadBtn"));
		btnDownload.addActionListener(e -> {
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String urlStr = ((FW)comboBox.getSelectedItem()).getUrl();
				String filename = urlStr.substring(urlStr.lastIndexOf('/'));
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("zip file", "zip"));
				fc.setSelectedFile(new File(filename));
				if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					URLConnection urlcon = new URL(urlStr).openConnection();
					InputStream in = urlcon.getInputStream();
					File outFile = fc.getSelectedFile();
					try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
						byte[] buffer = new byte[8 * 1024];
						int bytesRead;
						while ((bytesRead = in.read(buffer)) != -1) {
							outStream.write(buffer, 0, bytesRead);
						}
					}
					main.downloadSuccess(outFile.getPath());
					JOptionPane.showMessageDialog(this, MainView.LABELS.getString("downloadOK"), Main.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (/*MalformedURL*/Exception e1) {
				JOptionPane.showMessageDialog(this, MainView.LABELS.getString("fwWriteFileError"), MainView.LABELS.getString("error"), JOptionPane.ERROR_MESSAGE);
			} finally {
				setCursor(Cursor.getDefaultCursor());
			}
		});

		JPanel downloadPanel = new JPanel();
		add(downloadPanel, BorderLayout.NORTH);
		downloadPanel.setLayout(new GridLayout(0, 1, 0, 0));

		FlowLayout fl_panel_1 = new FlowLayout(FlowLayout.LEFT);
		fl_panel_1.setHgap(0);
		JPanel panel_1 = new JPanel(fl_panel_1);
		downloadPanel.add(panel_1);

		JLabel label = new JLabel(MainView.LABELS.getString("deviceSelection"));
		panel_1.add(label);

		comboBox = new JComboBox<FW>();

		panel_1.add(comboBox);

		JLabel idLabel = new JLabel();
		downloadPanel.add(idLabel);

		JLabel versionLabel = new JLabel();
		downloadPanel.add(versionLabel);

		JLabel urlLabel = new JLabel();
		downloadPanel.add(urlLabel);

		comboBox.addActionListener(event -> {
			final FW selected = (FW)comboBox.getSelectedItem();
			idLabel.setText(selected.getId());
			versionLabel.setText(selected.getVersion());
			urlLabel.setText(selected.getUrl());
		});

		JPanel buttonPanel = main.buttonPanel(btnDownload, closeButton);
		add(buttonPanel, BorderLayout.SOUTH);

		try {
			init();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, MainView.LABELS.getString("httpNoConnection"), MainView.LABELS.getString("error"), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		final URL url = new URL(G1_CATALOG_URL);
		URLConnection urlcon = url.openConnection();
		final ObjectMapper jsonMapper = new ObjectMapper();
		JsonNode node = jsonMapper.readTree(urlcon.getInputStream());

		ArrayList<FW> cList = new ArrayList<>();
		node.get("data").fields().forEachRemaining(entry -> {
			cList.add(new FW(entry.getKey(), entry.getValue().get("url").asText(), entry.getValue().get("version").asText(), false));
			JsonNode beta;
			if((beta = entry.getValue().get("beta_url")) != null && beta.asText().length() > 0) {
				cList.add(new FW(entry.getKey(), beta.asText(), entry.getValue().get("beta_ver").asText(), true));
			}
		});
		Collections.sort(cList);
		cList.forEach(fw -> comboBox.addItem(fw));
	}

	private static class FW implements Comparable<FW> {
		private final String id, url, version;
		private final boolean beta;

		private FW(String id, String url, String version, boolean beta) {
			this.id = id;
			this.url = url;
			this.version = version;
			this.beta = beta;
		}

		private String getId() {
			return id;
		}

		private String getUrl() {
			return url;
		}

		private String getVersion() {
			return version;
		}

		@Override
		public String toString() {
			String res = MainView.LABELS.containsKey(id) ? MainView.LABELS.getString(id) + " (" + id + ")" : id;
			if(beta) {
				res += " beta";
			}
			return res;
		}

		@Override
		public int compareTo(FW o) {
			return toString().toLowerCase().compareTo(o.toString().toLowerCase());
		}
	}
}

/*
https://updates.shelly.cloud/update/One
https://updates.shelly.cloud/update/OnePM
https://updates.shelly.cloud/update/Plus2PM
https://updates.shelly.cloud/update/PlusI4
*/