package me.jameschan.burrow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.lang.NonNull;

public class BurrowClient {
  public static void main(final String[] args) {
    final var data = sendRequestToServer("http://localhost:1128", ". root");
    System.out.println(data.get("output"));
    System.exit(Integer.parseInt(data.get("code")));
  }

  @NonNull
  public static Map<String, String> sendRequestToServer(final String uri, final String command) {
    try (final var httpClient = HttpClients.createDefault()) {
      final HttpClientResponseHandler<String> responseHandler =
          response -> new String(response.getEntity().getContent().readAllBytes());

      final HttpPost postRequest = new HttpPost(uri);
      final var json = String.format("{\"command\": \"%s\"}", command);
      postRequest.setEntity(new StringEntity(json));
      postRequest.setHeader("Content-type", "application/json");
      final String responseBody = httpClient.execute(postRequest, responseHandler);
      final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
      return new Gson().fromJson(responseBody, mapType);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Properties loadApplicationProperties() {
    final Properties properties = new Properties();

    try (final InputStream inputStream =
        BurrowClient.class.getClassLoader().getResourceAsStream("application.properties")) {
      properties.load(inputStream);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }

    return properties;
  }
}
