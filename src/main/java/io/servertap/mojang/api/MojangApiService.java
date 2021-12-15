package io.servertap.mojang.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.servertap.mojang.api.models.NameChange;
import io.servertap.mojang.api.models.PlayerInfo;
import io.servertap.utils.GsonSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class MojangApiService {
    private static final String getUuidResource = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String getNameHistoryResource = "https://api.mojang.com/user/profiles/%s/names";

    public static String getUuid(String username) throws IOException {
        Gson gson = GsonSingleton.getInstance();

        ApiResponse apiResponse = getApiResponse(String.format(getUuidResource, username));
        if (apiResponse.getHttpStatus() == HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IllegalArgumentException("The given username was not found by the Mojang API.");
        }

        return gson.fromJson(apiResponse.getContent(), PlayerInfo.class).getId();
    }

    public static List<NameChange> getNameHistory(String uuid) throws IOException {
        Type listType = new TypeToken<List<NameChange>>() {
        }.getType();
        Gson gson = GsonSingleton.getInstance();

        //This API call doesn't accept UUIDS with dashes
        ApiResponse apiResponse = getApiResponse(String.format(getNameHistoryResource, uuid).replace("-", ""));
        if (apiResponse.getHttpStatus() == HttpURLConnection.HTTP_BAD_REQUEST ||
                apiResponse.getHttpStatus() == HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IllegalArgumentException("The given uuid was not found by the Mojang API.");
        }

        return gson.fromJson(apiResponse.getContent(), listType);
    }

    private static ApiResponse getApiResponse(String resource) throws IOException {
        try {
            String responseContent;

            URL url = new URL(resource);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setDoInput(true);

            http.connect();

            if (http.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                return new ApiResponse("", http.getResponseCode());
            }

            try (InputStream is = http.getInputStream()) {
                responseContent = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            }

            return new ApiResponse(responseContent, http.getResponseCode());
        } catch (MalformedURLException ignored) {
            throw new IllegalArgumentException("The given resource string is not a valid URL.");
        }
    }

    private static <T> ApiResponse getApiResponse(String resource, T requestData) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private static class ApiResponse {
        private String content;
        private int httpStatus;

        public ApiResponse(String content, int httpStatus) {
            setContent(content);
            setHttpStatus(httpStatus);
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
        }
    }
}
