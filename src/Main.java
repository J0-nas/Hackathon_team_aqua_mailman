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
				//printLine(line);
				handleLine(line, ip);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void printLine(String line) {		
		byte[] b = line.getBytes();
		if (b.length > 0) {
			int offset = b[0] & 0xF0;
			if (b.length > 2) {
				//IMU
				if (b.length > 14) {
					if (b[0] == 0xF0) {
						int acx = (b[1] << 8) + b [2];
						int acy = (b[3] << 8) + b [4];
						int acz = (b[5] << 8) + b [6];
						int tmp = (b[7] << 8) + b [8];
						int gyx = (b[9] << 8) + b [10];
						int gyy = (b[11] << 8) + b [12];
						int gyz = (b[13] << 8) + b [14];
						System.out.println("IMU:\t "+ 
								" AcX: " + acx + 
								" AcY: " + acy + 
								" AcZ: " + acz + 
								" TMP: " + tmp +
								" GyX: " + gyx +
								" GyY: " + gyy +
								" GyZ: " + gyz);
					}
				}
			} else if (b.length == 2) {
				int a = (b[0] & 0x0F) << 4;
				int value = a + b[1];
				
				//SensorType
				switch (offset) {
				case 0x00:
					System.out.println(value + " -" + "Temperature");
					break;
				case 0x01:
					System.out.println(value + " -" + "Photo");
					break;
				case 0x02:
					System.out.println(value + " -" + "Depth");
					break;
				case 0x03:
					System.out.println(value + " -" + "Con");
					break;
				case 0x04:
					System.out.println(value + " -" + "Barrery");
					break;
				default:
					System.out.println("Unknown Sensor!");
				}
			} else {
				System.out.println("Bad length of measurement");
			}
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
