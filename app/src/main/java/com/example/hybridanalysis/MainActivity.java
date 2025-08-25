package com.example.hybridanalysis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etInput, etFilePath;
    private RadioGroup rgAnalysisType, rgService;
    private Button btnAnalyze, btnChatbot, btnLogout, btnUploadFile;
    private RadioButton rbUrl, rbIp, rbDomain, rbFile;
    private RadioButton rbOTX, rbVT;

    private static final int PICK_FILE_REQUEST = 100;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();

        updateInputHint(R.id.rb_url);
    }

    private void initializeViews() {
        etInput = findViewById(R.id.et_input);
        etFilePath = findViewById(R.id.et_file_path);
        rgAnalysisType = findViewById(R.id.rg_analysis_type);
        rgService = findViewById(R.id.rg_service);
        btnAnalyze = findViewById(R.id.btn_analyze);
        btnChatbot = findViewById(R.id.btn_chatbot);
        btnLogout = findViewById(R.id.btn_logout);
        btnUploadFile = findViewById(R.id.btn_upload_file);

        rbUrl = findViewById(R.id.rb_url);
        rbIp = findViewById(R.id.rb_ip);
        rbDomain = findViewById(R.id.rb_domain);
        rbFile = findViewById(R.id.rb_file);

        rbOTX = findViewById(R.id.rb_otx);
        rbVT = findViewById(R.id.rb_vt);
    }

    private void setupClickListeners() {
        btnAnalyze.setOnClickListener(v -> performAnalysis());

        btnChatbot.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatbotActivity.class)));

        btnLogout.setOnClickListener(v -> logout());

        btnUploadFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // tous types de fichiers
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Sélectionner un fichier"), PICK_FILE_REQUEST);
        });

        rgAnalysisType.setOnCheckedChangeListener((group, checkedId) -> updateInputHint(checkedId));
    }

    private void updateInputHint(int checkedId) {
        if (checkedId == R.id.rb_url) {
            etInput.setVisibility(View.VISIBLE);
            etInput.setHint("Entrez l'URL à analyser");
            btnUploadFile.setVisibility(View.GONE);
            etFilePath.setVisibility(View.GONE);
        } else if (checkedId == R.id.rb_ip) {
            etInput.setVisibility(View.VISIBLE);
            etInput.setHint("Entrez l'adresse IP");
            btnUploadFile.setVisibility(View.GONE);
            etFilePath.setVisibility(View.GONE);
        } else if (checkedId == R.id.rb_domain) {
            etInput.setVisibility(View.VISIBLE);
            etInput.setHint("Entrez le domaine");
            btnUploadFile.setVisibility(View.GONE);
            etFilePath.setVisibility(View.GONE);
        } else if (checkedId == R.id.rb_file) {
            etInput.setVisibility(View.GONE);
            btnUploadFile.setVisibility(View.VISIBLE);
            etFilePath.setVisibility(View.VISIBLE);
            etFilePath.setText("");
        }
    }

    private void performAnalysis() {
        String input;
        String analysisType = "";
        int selectedTypeId = rgAnalysisType.getCheckedRadioButtonId();

        // Déterminer le type d'analyse
        if (selectedTypeId == R.id.rb_url) {
            analysisType = "url";
            input = etInput.getText().toString().trim();
        } else if (selectedTypeId == R.id.rb_ip) {
            analysisType = "ip";
            input = etInput.getText().toString().trim();
        } else if (selectedTypeId == R.id.rb_domain) {
            analysisType = "domain";
            input = etInput.getText().toString().trim();
        } else if (selectedTypeId == R.id.rb_file) {
            analysisType = "file";
            if (selectedFileUri != null) {
                // Pour les fichiers, on passe l'URI en string
                input = selectedFileUri.toString();
            } else {
                // Ou le hash si entré manuellement
                input = etInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(this, "Veuillez sélectionner un fichier ou entrer un hash", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner un type d'analyse", Toast.LENGTH_SHORT).show();
            return;
        }

        if (input.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer ou sélectionner quelque chose à analyser", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedServiceId = rgService.getCheckedRadioButtonId();
        String service = "";

        if (selectedServiceId == R.id.rb_otx) {
            service = "otx";
        } else if (selectedServiceId == R.id.rb_vt) {
            service = "vt";
        } else {
            Toast.makeText(this, "Veuillez sélectionner un service d'analyse", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
        intent.putExtra("input", input);
        intent.putExtra("type", analysisType);
        intent.putExtra("service", service);

        if (selectedFileUri != null && analysisType.equals("file")) {
            intent.putExtra("fileUri", selectedFileUri.toString());
        }

        startActivity(intent);
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String fileName = getFileNameFromUri(selectedFileUri);
                etFilePath.setText(fileName);
                Toast.makeText(this, "Fichier sélectionné: " + fileName, Toast.LENGTH_SHORT).show();

                etInput.setText("");
                etInput.setHint("Ou entrez un hash MD5/SHA1/SHA256");
            }
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}