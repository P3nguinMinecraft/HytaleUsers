package feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import util.Config;

public class Checker {
	public static Boolean checkUsername(String username) {
	    try {
	        URI uri = new URI(Config.API_BASE + username);
	        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("User-Agent",
	            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
	            "AppleWebKit/537.36 (KHTML, like Gecko) " +
	            "Chrome/117.0.0.0 Safari/537.36");
	        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	        conn.setRequestProperty("Cookie", "ory_kratos_session=" + Config.COOKIE);
	                
	        int status = conn.getResponseCode();
	        boolean available = status >= 200 && status < 300;
	        String response = read(new InputStreamReader(available ? conn.getInputStream() : conn.getErrorStream()));
	
	        if (response.equals("error code: 1015")) {
	            System.out.println("[CF] Rate limited - ERROR 1015");
	            return null;
	        }
	        if (Config.PRINT_RESPONSE && !response.isEmpty()) {
	            System.out.println(response);
	        }
	        return available;
	
	    } 
	    catch (IOException | URISyntaxException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private static String read(InputStreamReader isr) throws IOException {
	    BufferedReader reader = new BufferedReader(isr);
	    StringBuilder response = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        response.append(line);
	    }
	    reader.close();
	
	    return response.toString();
	}
}
