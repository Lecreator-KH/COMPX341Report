package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;

public class AppTest {
  @Test
  public void successfulResponse() {
    App app = new App();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
    assertEquals(200, result.getStatusCode().intValue());
    assertEquals("application/json", result.getHeaders().get("Content-Type"));
    ArrayList<String> quoteList = new ArrayList<String>();
    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("/home/ec2-user/environment/KevinHan-1521885-sam-app/HelloWorldFunction/quotes.txt"));
			String line = reader.readLine();
			while (line != null) {
				// System.out.println(line);
				// read next line
				line = reader.readLine();
				quoteList.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    String content = result.getBody();
    // System.out.println(content);
    String quote = result.getBody();
    quote = quote.substring(14);
    quote = quote.split("\"")[0];
    // System.out.println(quote);
    assertNotNull(content);
    assertTrue(content.contains("\"message\""));
    assertTrue(quoteList.contains(quote));
    assertTrue(content.contains("\"location\""));
  }
}
