package com.example.hybridanalysis;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AnalysisActivity extends AppCompatActivity {

    private TextView tvResults;
    private ProgressBar progressBar;
    private String input, analysisType, service;

    private static final String VT_API_KEY = "******";
    private static final String OTX_API_KEY = "******";
    private static final String IPINFO_TOKEN = "******";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        input = getIntent().getStringExtra("input");
        analysisType = getIntent().getStringExtra("type");
        service = getIntent().getStringExtra("service");

        initializeViews();
        startAnalysis();

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
        });


        findViewById(R.id.btn_new_analysis).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btn_share).setOnClickListener(v -> {
            shareResults();
        });
    }

    private void initializeViews() {
        tvResults = findViewById(R.id.tv_results);
        progressBar = findViewById(R.id.progress_bar);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("Analyse " + service.toUpperCase() + " - " + analysisType.toUpperCase());
    }

    private void startAnalysis() {
        progressBar.setVisibility(View.VISIBLE);
        tvResults.setText("Analyse en cours...");

        if (analysisType.equals("ip")) {
            new IPAnalysisTask().execute(input, service);
        } else {
            new GeneralAnalysisTask().execute(input, analysisType, service);
        }
    }

    private void shareResults() {
        String results = tvResults.getText().toString();
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Résultats d'analyse");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, results);
        startActivity(android.content.Intent.createChooser(shareIntent, "Partager les résultats"));
    }

    private class IPAnalysisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ip = params[0];
            String securityService = params[1]; // "vt" ou "otx"

            StringBuilder result = new StringBuilder();

            try {
                String ipInfoResult = getIPInfoData(ip);
                result.append(ipInfoResult).append("\n\n");

                String securityResult = "";
                if (securityService.equals("vt")) {
                    securityResult = performVirusTotalAnalysis(ip, "ip");
                } else if (securityService.equals("otx")) {
                    securityResult = performOTXAnalysis(ip, "ip");
                } else {
                    securityResult = "Service de sécurité non supporté: " + securityService;
                }

                result.append(securityResult);

            } catch (Exception e) {
                return "Erreur lors de l'analyse IP: " + e.toString();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            tvResults.setText(result);
        }

        private String getIPInfoData(String ip) throws Exception {
            URL url = new URL("https://ipinfo.io/" + ip + "?token=" + IPINFO_TOKEN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return "Erreur HTTP IPInfo: " + responseCode;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            return formatIPInfo(response.toString());
        }
    }

    private class GeneralAnalysisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String input = params[0];
            String type = params[1];
            String service = params[2];

            try {
                if (service.equals("vt")) {
                    return performVirusTotalAnalysis(input, type);
                } else if (service.equals("otx")) {
                    return performOTXAnalysis(input, type);
                } else {
                    return "Service non supporté";
                }
            } catch (Exception e) {
                return "Erreur lors de l'analyse: " + e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            tvResults.setText(result);
        }
    }

    private String performVirusTotalAnalysis(String input, String type) throws Exception {
        if (type.equals("file")) {
            if (isFileHash(input)) {
                return analyzeFileByHash(input);
            } else {
                File file = new File(input);
                if (file.exists()) {
                    return uploadAndAnalyzeFile(file);
                } else {
                    return "Fichier non trouvé: " + input;
                }
            }
        } else {
            return analyzeStandardType(input, type);
        }
    }

    private boolean isFileHash(String input) {
        return input.matches("^[a-fA-F0-9]{32}$") ||  // MD5
                input.matches("^[a-fA-F0-9]{40}$") ||  // SHA1
                input.matches("^[a-fA-F0-9]{64}$");    // SHA256
    }

    private String analyzeFileByHash(String hash) throws Exception {
        URL url = new URL("https://www.virustotal.com/api/v3/files/" + hash);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("x-apikey", VT_API_KEY);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return formatVirusTotalResponse(response.toString(), "file");
        } else {
            return "Fichier non trouvé dans VirusTotal (Code: " + responseCode + ")";
        }
    }

    private String uploadAndAnalyzeFile(File file) throws Exception {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        URL url = new URL("https://www.virustotal.com/api/v3/files");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("x-apikey", VT_API_KEY);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream();
             FileInputStream fileInputStream = new FileInputStream(file)) {

            outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" +
                    file.getName() + "\"" + lineEnd).getBytes());
            outputStream.write(("Content-Type: application/octet-stream" + lineEnd + lineEnd).getBytes());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.write((lineEnd + twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);

            // Extraire l'ID d'analyse et récupérer les résultats
            JSONObject json = new JSONObject(response.toString());
            String analysisId = json.getJSONObject("data").getString("id");
            return getAnalysisResult(analysisId);
        } else {
            return "Erreur lors de l'upload: " + responseCode;
        }
    }

    private String getAnalysisResult(String analysisId) throws Exception {
        URL url = new URL("https://www.virustotal.com/api/v3/analyses/" + analysisId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("x-apikey", VT_API_KEY);

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return formatVirusTotalResponse(response.toString(), "file");
        } else {
            return "Erreur lors de la récupération des résultats: " + responseCode;
        }
    }

    private String analyzeStandardType(String input, String type) throws Exception {
        String endpoint = getVirusTotalEndpoint(type);

        if (type.equals("url")) {
            String encodedUrl = android.util.Base64.encodeToString(
                    input.getBytes(StandardCharsets.UTF_8),
                    android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP
            );
            endpoint += encodedUrl;
        } else {
            endpoint += input;
        }

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("x-apikey", VT_API_KEY);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            return "Erreur HTTP VirusTotal: " + responseCode;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);

        return formatVirusTotalResponse(response.toString(), type);
    }

    private String getVirusTotalEndpoint(String type) {
        switch (type) {
            case "url": return "https://www.virustotal.com/api/v3/urls/";
            case "domain": return "https://www.virustotal.com/api/v3/domains/";
            case "file": return "https://www.virustotal.com/api/v3/files/";
            default: return "https://www.virustotal.com/api/v3/ip_addresses/";
        }
    }

    private String performOTXAnalysis(String input, String type) throws Exception {
        String endpoint = getOTXEndpoint(type);
        URL url = new URL(endpoint + input + "/general");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-OTX-API-KEY", OTX_API_KEY);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) return "Erreur HTTP OTX: " + responseCode;

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        return formatOTXResponse(response.toString(), type);
    }

    private String getOTXEndpoint(String type) {
        switch (type) {
            case "url": return "https://otx.alienvault.com/api/v1/indicators/url/";
            case "domain": return "https://otx.alienvault.com/api/v1/indicators/domain/";
            case "file": return "https://otx.alienvault.com/api/v1/indicators/file/";
            default: return "https://otx.alienvault.com/api/v1/indicators/IPv4/";
        }
    }

    private String formatIPInfo(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            if (json.has("error")) return "IPInfo erreur: " + json.getString("error");

            StringBuilder result = new StringBuilder();
            result.append("=== INFORMATIONS GÉOGRAPHIQUES (IPInfo) ===\n\n");
            result.append("IP: ").append(json.optString("ip","N/A")).append("\n");
            result.append("Pays: ").append(json.optString("country","N/A")).append("\n");
            result.append("Région: ").append(json.optString("region","N/A")).append("\n");
            result.append("Ville: ").append(json.optString("city","N/A")).append("\n");
            result.append("Organisation: ").append(json.optString("org","N/A")).append("\n");
            result.append("Timezone: ").append(json.optString("timezone","N/A")).append("\n");
            result.append("Localisation: ").append(json.optString("loc","N/A")).append("\n");
            return result.toString();
        } catch (Exception e) {
            return "Erreur formatage IPInfo: " + e.toString();
        }
    }

    private String formatVirusTotalResponse(String jsonResponse, String type) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder();
            result.append("=== ANALYSE DE SÉCURITÉ (VIRUSTOTAL) ===\n\n");
            result.append("Type: ").append(type.toUpperCase()).append("\n");

            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                JSONObject attributes = data.getJSONObject("attributes");

                if (type.equals("ip")) {
                    result.append("Pays: ").append(attributes.optString("country", "N/A")).append("\n");
                    result.append("ASN: ").append(attributes.optInt("asn", 0)).append("\n");
                    result.append("AS Owner: ").append(attributes.optString("as_owner", "N/A")).append("\n");
                    result.append("Réseau: ").append(attributes.optString("network", "N/A")).append("\n\n");

                    if (attributes.has("last_analysis_stats")) {
                        JSONObject stats = attributes.getJSONObject("last_analysis_stats");
                        result.append("--- STATISTIQUES ---\n");
                        result.append("Malveillant: ").append(stats.optInt("malicious", 0)).append("\n");
                        result.append("Suspect: ").append(stats.optInt("suspicious", 0)).append("\n");
                        result.append("Propre: ").append(stats.optInt("harmless", 0)).append("\n");
                        result.append("Non détecté: ").append(stats.optInt("undetected", 0)).append("\n");
                    }
                }
                else {
                    if (attributes.has("last_analysis_stats")) {
                        JSONObject stats = attributes.getJSONObject("last_analysis_stats");
                        result.append("\n--- STATISTIQUES ---\n");
                        result.append("Malveillant: ").append(stats.optInt("malicious",0)).append("\n");
                        result.append("Suspect: ").append(stats.optInt("suspicious",0)).append("\n");
                        result.append("Propre: ").append(stats.optInt("harmless",0)).append("\n");
                        result.append("Non détecté: ").append(stats.optInt("undetected",0)).append("\n");
                    }
                    if (attributes.has("reputation")) {
                        result.append("Réputation: ").append(attributes.getInt("reputation")).append("\n");
                    }
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "Erreur formatage VirusTotal: " + e.toString();
        }
    }

    private String formatOTXResponse(String jsonResponse, String type) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder();
            result.append("=== ANALYSE DE SÉCURITÉ (OTX) ===\n\n");
            result.append("Type: ").append(type.toUpperCase()).append("\n");

            if (type.equals("ip")) {
                if (json.has("base_indicator")) {
                    JSONObject base = json.getJSONObject("base_indicator");
                    result.append("Pays: ").append(base.optString("country", "N/A")).append("\n");
                    result.append("ASN: ").append(base.optInt("asn", 0)).append("\n");
                }
                if (json.has("pulse_info")) {
                    JSONObject pulseInfo = json.getJSONObject("pulse_info");
                    result.append("Nombre de pulses: ").append(pulseInfo.optInt("count", 0)).append("\n");
                }
            }

            else {
                if (json.has("reputation"))
                    result.append("Réputation: ").append(json.getInt("reputation")).append("\n");
                if (json.has("pulse_info")) {
                    JSONObject pulseInfo = json.getJSONObject("pulse_info");
                    result.append("Nombre de pulses: ").append(pulseInfo.optInt("count",0)).append("\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "Erreur formatage OTX: " + e.toString();
        }
    }

}