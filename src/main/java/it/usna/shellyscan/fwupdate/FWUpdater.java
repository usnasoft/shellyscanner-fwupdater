package it.usna.shellyscan.fwupdate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FWUpdater {
	private final static int PORT = 8000;
	private final static String CONTEXT = "shellyfw";

	private final static String IPV4_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

	public static void main(String ... args) throws IOException {
		if(args.length != 2) {
			usage();
			System.exit(1);
		}
		String shellyIP = args[0];
		if(shellyIP.matches(IPV4_REGEX) == false) {
			System.out.println("invalid IP.");
			usage();
			System.exit(2);
		}
		File file = new File(args[1]);
		if(file.exists() == false) {
			System.out.println("file not found.");
			usage();
			System.exit(3);
		}

		try {
			InetSocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
			HttpServer server = HttpServer.create(socket, 0);
			server.createContext("/" + CONTEXT, new MyHandler(file));
			server.setExecutor(null); // creates a default executor
			server.start();

			System.out.println("Server is ready at " + socket.getAddress().getHostAddress());
			System.out.println("Updating firmware ...\nDO NOT close terminal.");
			//		System.out.println("Write following link into browser: http://" + socket.getAddress().getHostAddress() + ":" + PORT + "/" + CONTEXT);
			final URL url = new URL("http://" + shellyIP + "/ota?url=http://" + socket.getAddress().getHostAddress() + ":" + PORT + "/" + CONTEXT);
			System.out.println(url.getContent());
			server.stop(1000);
			System.out.println("Process end");

//			System.out.println("Press ctrl^C when firmware update is concluded.\nDo not close terminal before.");

		} catch(Exception e) {
			e.printStackTrace();
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
		System.out.println("Usage: java -jar ... <shelly ip> <firmware file>");
	}
}

// http://<ip>/ota?url=http:// ...