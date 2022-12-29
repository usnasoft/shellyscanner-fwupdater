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
	private HttpServer server = null;

	public String update(String shellyIP, File file) throws ConnectException, IOException {
		// try {
		InetSocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
		server = HttpServer.create(socket, 0);
		server.createContext("/" + CONTEXT, new MyHandler(file));
		server.setExecutor(null); // creates a default executor
		server.start();

		System.out.println("Temporary server now responding at " + socket.getAddress().getHostAddress() + ":" + PORT);
		
		System.out.println("Updating firmware ...");

		final URL url = new URL("http://" + shellyIP + "/ota?url=http://" + socket.getAddress().getHostAddress() + ":" + PORT + "/" + CONTEXT);
		URLConnection urlcon = url.openConnection();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(urlcon.getInputStream()))) {
			String out = "";
			String line;
			while ((line = br.readLine()) != null) {
				out += line + '\n';
			}
			return out;
		}

		// } catch (ConnectException e) {
		// System.out.println("Shelly device at " + shellyIP + " is not
		// responding");
		// System.exit(4);
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		//// if (server != null) {
		//// server.stop(5000);
		//// System.exit(0);
		//// }
		// }
	}
	
	public void stop() {
		if(server != null) {
			server.stop(1);
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
}