package helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        // Code to parse bucket data
        String key_name = "quotes.txt";
        String bucket_name = "kevinhan-bucket";
        System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
        String result = "";
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        try {
            S3Object o = s3.getObject(bucket_name, key_name);
            S3ObjectInputStream s3is = o.getObjectContent();
            result = new BufferedReader(new InputStreamReader(s3is)).lines().parallel().collect(Collectors.joining("\n"));
            // System.out.println(result);
            // FileOutputStream fos = new FileOutputStream(new File(key_name));
            // byte[] read_buf = new byte[1024];
            // int read_len = 0;
            // while ((read_len = s3is.read(read_buf)) > 0) {
            //     fos.write(read_buf, 0, read_len);
            // }
            s3is.close();
            // fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done!");
        
        // try {
        //     FileWriter myWriter = new FileWriter(key_name);
        //     myWriter.write(result);
        //     myWriter.close();
        //     System.out.println("Successfully wrote to the file.");
        // } catch (IOException e) {
        //     System.out.println("An error occurred.");
        //     e.printStackTrace();
        // }
        
        //Code to setup quote
        // System.out.println(System.getProperty("user.dir"));
        ArrayList<String> quoteList = new ArrayList<String>();
//         BufferedReader reader;
// 		try {
// 			reader = new BufferedReader(new FileReader("/home/ec2-user/environment/KevinHan-1521885-sam-app/HelloWorldFunction/quotes.txt"));
// 			String line = reader.readLine();
// 			while (line != null) {
// 				// System.out.println(line);
// 				// read next line
// 				line = reader.readLine();
// 				quoteList.add(line);
// 			}
// 			reader.close();
// 		} catch (IOException e) {
// 			e.printStackTrace();
// 		}
        String[] tempList = result.split(System.lineSeparator());
        for(int i = 0; i < tempList.length; i++) {
            quoteList.add(tempList[i]);
        }
        System.out.println(quoteList.size());
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        try {
            int randomNum = ThreadLocalRandom.current().nextInt(0, quoteList.size() + 1);
            String quote = quoteList.get(randomNum);
            System.out.println(quote);
            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            String output = String.format("{ \"message\": \""+quote+"\", \"location\": \"%s\" }", pageContents);

            return response.withStatusCode(200).withBody(output);
        } catch (IOException e) {
            return response.withBody("{}").withStatusCode(500);
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
