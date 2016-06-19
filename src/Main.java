import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
	private String ip = "0.0.0.0";
	
	
	public static void main(String[] args) {
		String ip;
		String usbOffset;
		if (args.length >= 2) {
			ip = args[0];
			usbOffset = args[1];
		} else {
			ip = "0.0.0.0";
			usbOffset = "0";
		}
		
		// TODO Auto-generated method stub
		String location = "/dev/ttyUSB" + usbOffset;
		Path p = FileSystems.getDefault().getPath(location);
		System.out.println("Path to open: " + p.toAbsolutePath().toString());
		System.out.println("PAth as URI: " + p.toUri().toString());
		try {
			BufferedReader reader = Files.newBufferedReader(p);
			System.out.println("Reader initialized on: " + location);
			String line;
			while (true) {
				line = reader.readLine();
				System.out.println("Read line: " + line);
				handleLine(line, ip);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void handleLine(String line, String ip) {
		if (line.startsWith("{") && (line.endsWith("}") || line.endsWith("\n"))) {
			postLine(line, ip);
		}
	}
	
	static void postLine(String line, String ip) {
		String uri = ip + "/coordinates";
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(line.getBytes());
			os.flush();
			os.close();
			System.out.println("Line sent at Target: " + url.toString());
			
			//Response
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				System.out.println(response.toString());
			} else {
				System.out.println("POST request not worked");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
