package com.example.hybridanalysis.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String USERS_COLLECTION = "users";
    private static final String ANALYSIS_COLLECTION = "analysis_history";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface DatabaseCallback {
        void onSuccess(Object data);
        void onError(String error);
    }

    // Sauvegarder un utilisateur
    public void saveUser(FirebaseUser user, DatabaseCallback callback) {
        if (user == null) {
            callback.onError("Utilisateur null");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("uid", user.getUid());
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLogin", System.currentTimeMillis());

        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully");
                    callback.onSuccess(userData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user", e);
                    callback.onError("Erreur lors de la sauvegarde: " + e.getMessage());
                });
    }

    // Sauvegarder l'historique d'analyse
    public void saveAnalysisHistory(String input, String type, String service, String result, DatabaseCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("userId", currentUser.getUid());
        analysisData.put("input", input);
        analysisData.put("type", type);
        analysisData.put("service", service);
        analysisData.put("result", result);
        analysisData.put("timestamp", System.currentTimeMillis());

        db.collection(ANALYSIS_COLLECTION)
                .add(analysisData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Analysis history saved with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving analysis history", e);
                    callback.onError("Erreur lors de la sauvegarde de l'historique: " + e.getMessage());
                });
    }

    // Récupérer les informations utilisateur
    public void getUserInfo(String userId, DatabaseCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getData());
                    } else {
                        callback.onError("Utilisateur non trouvé");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user info", e);
                    callback.onError("Erreur lors de la récupération des données: " + e.getMessage());
                });
    }

    // Mettre à jour la dernière connexion
    public void updateLastLogin(DatabaseCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", System.currentTimeMillis());

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError("Erreur lors de la mise à jour: " + e.getMessage()));
    }
}