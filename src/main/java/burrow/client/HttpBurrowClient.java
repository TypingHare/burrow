package burrow.client;

import burrow.core.common.Environment;
import com.google.gson.Gson;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;

public final class HttpBurrowClient extends BurrowClient {
    public HttpBurrowClient() throws BurrowClientInitializationException {
        super();
    }

    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    @Override
    protected BurrowResponse sendRequest(@NonNull final String command) {
        try (final var httpClient = HttpClients.createDefault()) {
            final HttpClientResponseHandler<String> responseHandler =
                response -> new String(response.getEntity().getContent().readAllBytes());

            final HttpPost postRequest = new HttpPost(uri);
            final Environment environment = getEnvironment();
            postRequest.setEntity(new StringEntity(command, StandardCharsets.UTF_8));
            postRequest.setHeader("Content-type", "text/plain");
            postRequest.setHeader("Working-Directory", environment.getWorkingDirectory());
            postRequest.setHeader("Console-Width", environment.getConsoleWidth());

            final String responseBody = httpClient.execute(postRequest, responseHandler);
            return new Gson().fromJson(responseBody, BurrowResponse.class);
        } catch (final HttpHostConnectException ex) {
            final var response = new BurrowResponse();
            response.setExitCode(CommandLine.ExitCode.SOFTWARE);
            response.setMessage("Fail to connect to server: " + uri);
            return response;
        } catch (final Throwable ex) {
            final var response = new BurrowResponse();
            response.setExitCode(CommandLine.ExitCode.SOFTWARE);
            response.setMessage("Unknown local error: " + ex.getMessage());
            return response;
        }
    }
}
