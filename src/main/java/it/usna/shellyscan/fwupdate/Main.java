package it.usna.shellyscan.fwupdate;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class Main {
	public final static String APP_NAME = "Shelly FW Updater";
	public final static String VER = "1.2.0";
	public final static String IPV4_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

	public static void main(String... args) throws IOException {
		System.out.println(APP_NAME + " " + VER);
		if(args.length > 0) {
			if (args.length != 2) {
				usage();
				System.exit(1);
			}
			String shellyIP = args[0];
			if (shellyIP.matches(IPV4_REGEX) == false) {
				System.out.println("invalid IP.");
				usage();
				System.exit(2);
			}
			File file = new File(args[1]);
			if (file.exists() == false) {
				System.out.println("file not found.");
				usage();
				System.exit(3);
			}

			try {
				FWUpdater upd = new FWUpdater();
				String res = upd.update(shellyIP, file);
				System.out.println("Device response to update command:\n" + res);
				System.out.println("Press ctrl^C when firmware update is complete\nDO NOT close terminal before");
				System.exit(0);
			} catch (ConnectException e) {
				System.out.println("Shelly device at " + shellyIP + " is not responding");
				System.exit(4);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(5);
			}
		} else {
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
			new MainView();
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar shellyscanner-fwupdater-1.0.0.jar <shelly IP> <firmware file>");
	}
}
