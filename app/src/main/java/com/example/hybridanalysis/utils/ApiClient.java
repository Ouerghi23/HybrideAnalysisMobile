package com.example.hybridanalysis.utils;

import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String TAG = "ApiClient";

    public static class ApiResponse {
        public String data;
        public boolean success;
        public String error;

        public ApiResponse(String data, boolean success, String error) {
            this.data = data;
            this.success = success;
            this.error = error;
        }
    }

    // Méthode générique pour les requêtes GET
    public static ApiResponse get(String urlString, String apiKey, String headerName) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (apiKey != null && headerName != null) {
                connection.setRequestProperty(headerName, apiKey);
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            int responseCode = connection.getResponseCode();

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (responseCode >= 200 && responseCode < 300) {
                return new ApiResponse(response.toString(), true, null);
            } else {
                return new ApiResponse(null, false, "HTTP Error: " + responseCode + " - " + response.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "API Request failed", e);
            return new ApiResponse(null, false, "Network Error: " + e.getMessage());
        }
    }

    // Méthode pour les requêtes POST (pour le chatbot)
    public static ApiResponse post(String urlString, String jsonPayload, String apiKey, String headerName) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            if (apiKey != null && headerName != null) {
                connection.setRequestProperty(headerName, apiKey);
            }

            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            // Écrire le payload
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(jsonPayload);
            writer.flush();
            writer.close();

            int responseCode = connection.getResponseCode();

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (responseCode >= 200 && responseCode < 300) {
                return new ApiResponse(response.toString(), true, null);
            } else {
                return new ApiResponse(null, false, "HTTP Error: " + responseCode + " - " + response.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "POST Request failed", e);
            return new ApiResponse(null, false, "Network Error: " + e.getMessage());
        }
    }
}