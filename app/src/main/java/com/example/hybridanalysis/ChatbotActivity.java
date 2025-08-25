package com.example.hybridanalysis;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvConversation;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    private Button btnQuick1, btnQuick2, btnQuick3;
    private ImageView btnBack;
    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();

    private static final String OPENAI_API_KEY = "****";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initializeViews();
        setupClickListeners();
        addMessageToConversation("Assistant", "Bonjour! Je suis votre assistant pour l'analyse de sécurité. Comment puis-je vous aider?");
    }

    private void initializeViews() {
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        rvChat = findViewById(R.id.rv_chat);
        chatAdapter = new ChatAdapter(messageList);
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));

        btnQuick1 = findViewById(R.id.btn_quick_1);
        btnQuick2 = findViewById(R.id.btn_quick_2);
        btnQuick3 = findViewById(R.id.btn_quick_3);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnBack.setOnClickListener(v -> finish());

        btnQuick1.setOnClickListener(v -> sendQuickMessage("Qu'est-ce qu'un malware?"));
        btnQuick2.setOnClickListener(v -> sendQuickMessage("Quels sont les types d'attaques courantes?"));
        btnQuick3.setOnClickListener(v -> sendQuickMessage("Quelles sont les bonnes pratiques de sécurité?"));

        etMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendQuickMessage(String message) {
        etMessage.setText(message);
        sendMessage();
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
            return;
        }

        addMessageToConversation("Vous", message);
        etMessage.setText("");

        new ChatbotTask().execute(message);
    }

    private void addMessageToConversation(String sender, String message) {
        Message.Sender msgSender = sender.equals("Vous") ? Message.Sender.USER : Message.Sender.ASSISTANT;
        messageList.add(new Message(message, msgSender));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }


    private class ChatbotTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String userMessage = params[0];
                return callOpenAI(userMessage);
            } catch (Exception e) {
                return "Erreur: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            btnSend.setEnabled(true);
            addMessageToConversation("Assistant", response);
        }
    }

    private String callOpenAI(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un expert en cybersécurité spécialisé dans l'analyse de menaces, la détection de malware et la protection des systèmes. Réponds de manière concise et technique tout en restant accessible. Tu aides les utilisateurs avec: l'analyse de fichiers suspects, l'interprétation des résultats VirusTotal/OTX, les bonnes pratiques de sécurité, et la compréhension des indicateurs de compromission.");
        messages.put(systemMessage);

        JSONObject userMessageObj = new JSONObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", message);
        messages.put(userMessageObj);

        jsonPayload.put("messages", messages);
        jsonPayload.put("max_tokens", 500);
        jsonPayload.put("temperature", 0.7);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(jsonPayload.toString());
        writer.flush();
        writer.close();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            throw new Exception("HTTP Error: " + responseCode + " - " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject messageObj = firstChoice.getJSONObject("message");
            return messageObj.getString("content");
        } else {
            return "Aucune réponse de l'assistant";
        }
    }
}