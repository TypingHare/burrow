package me.jameschan.burrow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class BurrowClient {
  public static void main(final String[] args) {
    try (final var httpClient = HttpClients.createDefault()) {
      final HttpClientResponseHandler<String> responseHandler =
          response -> new String(response.getEntity().getContent().readAllBytes());

      final var uri = "http://localhost:1128";
      final HttpPost postRequest = new HttpPost(uri);
      final var command = "commands";
      final var json = String.format("{\"command\": \"%s\"}", command);
      final var entity = new StringEntity(json);
      postRequest.setEntity(entity);
      postRequest.setHeader("Content-type", "application/json");
      final String responseBody = httpClient.execute(postRequest, responseHandler);
      final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
      final Map<String, String> data = new Gson().fromJson(responseBody, mapType);
      System.out.println(data.get("output"));

      final var statusCode = Integer.parseInt(data.get("code"));
      System.exit(statusCode);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
