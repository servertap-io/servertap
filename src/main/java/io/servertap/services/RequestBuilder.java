package io.servertap.services;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.servertap.enums.RequestType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class RequestBuilder {
    String url;
    JsonParser jsonParser;

    public RequestBuilder(String url) {
        this.url = url;
        this.jsonParser = new JsonParser();
    }


    public JsonObject sendUnguardedRequest(String requestSlug, RequestType requestType, String requestBody) throws IOException {
        return this.sendRequest(requestSlug, requestType, requestBody, "");
    }
    public JsonObject sendGuardedRequest(String requestSlug, RequestType requestType, String requestBody, String token) throws IOException {
        System.out.println("sending request");
        return this.sendRequest(requestSlug, requestType, requestBody, token);
    }

    private JsonObject sendRequest(String requestSlug, RequestType requestType, String requestBody, String token) throws IOException {
        try {
            String apiUrl = this.url + requestSlug;
            System.out.println(apiUrl);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up HTTP POST request
            connection.setRequestMethod(String.valueOf(requestType));
            connection.setRequestProperty("Content-Type", "application/json");
            if (token == "") {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            connection.setDoOutput(true);

            // Set the request body
            if (requestBody != "") {
                try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    outputStream.writeBytes(requestBody);
                    outputStream.flush();
                }
            }


            int responseCode = connection.getResponseCode();
            System.out.println(responseCode);

            if (responseCode < 300) {
                System.out.println(connection.getOutputStream());

                return getJson(connection);
            } else {
                System.out.println("Error with " + requestSlug + " with an error code of " + responseCode);
                throw new IOException();
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new IOException("Error with the request");
        }
    }

    public JsonObject getJson(HttpURLConnection connection) throws IOException {
        System.out.println(connection.getInputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        System.out.println("here we are");

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        System.out.println(response);
        // Parse the JSON response into a JsonObject
        JsonObject jsonObject = this.jsonParser.parse(response.toString()).getAsJsonObject();

        // Now you can work with the JsonObject
        System.out.println(jsonObject);
        return jsonObject;
    }

    public JsonObject getPublicKeysJson() throws IOException {
        // get public keys
        URI uri = URI.create("https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com");
        GenericUrl url = new GenericUrl(uri);
        HttpTransport http = new NetHttpTransport();
        HttpResponse response = http.createRequestFactory().buildGetRequest(url).execute();
        System.out.println(response);
        // store json from request
        String json = response.parseAsString();
        // disconnect
        response.disconnect();

        // parse json to object
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

        return jsonObject;
    }
}
