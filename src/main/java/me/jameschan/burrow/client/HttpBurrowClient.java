package me.jameschan.burrow.client;

import com.google.gson.Gson;
import java.io.IOException;

import me.jameschan.burrow.common.BurrowRequest;
import me.jameschan.burrow.common.BurrowResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class HttpBurrowClient extends BurrowClient {
  public HttpBurrowClient() throws BurrowClientInitializationException {}

  @Override
  protected BurrowResponse sendRequest(final BurrowRequest request) {
    try (final var httpClient = HttpClients.createDefault()) {
      final HttpClientResponseHandler<String> responseHandler =
          response -> new String(response.getEntity().getContent().readAllBytes());

      if (currentChamberName != null) {
        request.setCommand(currentChamberName + " " + request.getCommand());
      }

      final Gson gson = new Gson();
      final HttpPost postRequest = new HttpPost(uri);
      final var json = gson.toJson(request);
      postRequest.setEntity(new StringEntity(json));
      postRequest.setHeader("Content-type", "application/json");
      final String responseBody = httpClient.execute(postRequest, responseHandler);
      return new Gson().fromJson(responseBody, BurrowResponse.class);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}