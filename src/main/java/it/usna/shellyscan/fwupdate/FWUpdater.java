package it.usna.shellyscan.fwupdate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class FWUpdater {
	private final static int PORT = 8000;
	private final static String CONTEXT = "shellyfw";

	private final static String IPV4_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

	public static void main(String... args) throws IOException {
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

		HttpServer server = null;
		try {
			InetSocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
			server = HttpServer.create(socket, 0);
			server.createContext("/" + CONTEXT, new MyHandler(file));
			server.setExecutor(null); // creates a default executor
			server.start();

			System.out.println("Temporary server ready at address " + socket.getAddress().getHostAddress());
			System.out.println("Updating firmware ...");
			final URL url = new URL("http://" + shellyIP + "/ota?url=http://" + socket.getAddress().getHostAddress() + ":" + PORT + "/" + CONTEXT);
			URLConnection urlcon = url.openConnection();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlcon.getInputStream()))) {
				System.out.println("Device response to command:");
				String out;
				while ((out = br.readLine()) != null) {
					System.out.println(out);
				}
			}
			System.out.println("Press ctrl^C when firmware update is complete.\nDO NOT close terminal before");

		} catch (ConnectException e) {
			System.out.println("Shelly device at " + shellyIP + " is not responding");
			System.exit(4);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			if (server != null) {
//				server.stop(5000);
//				System.exit(0);
//			}
		}
	}

	private static class MyHandler implements HttpHandler {
		private final File file;

		private MyHandler(File file) {
			this.file = file;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
				t.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + file.getName());
				t.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
				OutputStream os = t.getResponseBody();
				int c;
				while ((c = in.read()) != -1) {
					os.write(c);
				}
				os.close();
			}
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar shellyscanner-fwupdater-1.0.0.jar <shelly ip> <firmware file>");
	}
}