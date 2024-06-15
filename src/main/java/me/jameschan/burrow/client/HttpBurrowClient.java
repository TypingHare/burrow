package me.jameschan.burrow.client;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import me.jameschan.burrow.kernel.common.BurrowRequest;
import me.jameschan.burrow.kernel.common.BurrowResponse;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class HttpBurrowClient extends BurrowClient {
  public HttpBurrowClient() throws BurrowClientInitializationException {}

  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  @Override
  protected BurrowResponse sendRequest(final BurrowRequest request) {
    try (final var httpClient = HttpClients.createDefault()) {
      final HttpClientResponseHandler<String> responseHandler =
          response -> new String(response.getEntity().getContent().readAllBytes());

      if (currentChamberName != null) {
        request.setCommand(currentChamberName + " " + request.getCommand());
      }

      final HttpPost postRequest = new HttpPost(uri);
      postRequest.setEntity(new StringEntity(request.getCommand(), StandardCharsets.UTF_8));
      postRequest.setHeader("Content-type", "text/plain");
      postRequest.setHeader("Working-Directory", request.getWorkingDirectory());
      postRequest.setHeader("Console-Width", request.getConsoleWidth());

      final String responseBody = httpClient.execute(postRequest, responseHandler);
      return new Gson().fromJson(responseBody, BurrowResponse.class);
    } catch (final HttpHostConnectException ex) {
      final var response = new BurrowResponse();
      response.setCode(1);
      response.setMessage("Fail to connect to server: " + uri);
      return response;
    } catch (final Throwable ex) {
      final var response = new BurrowResponse();
      response.setCode(1);
      response.setMessage("Unknown local error: " + ex.getMessage());
      return response;
    }
  }
}
