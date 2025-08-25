# HybrideAnalysisMobile

Application Android combinant cyber threat intelligence et chatbot interactif.
Elle permet d'analyser des IP, fichiers, URLs et domaines grâce à plusieurs services tout en offrant une authentification utilisateur et une interface chatbot.

## 🚀 Fonctionnalités

### 🔐 Authentification
- **Inscription (Sign Up)** : création de compte utilisateur
- **Connexion (Login)** : accès sécurisé à l'application
- **Déconnexion (Logout)** : fermeture de session sécurisée

### 📝 Interface d'Analyse
- **Formulaire à remplir** : Interface simple pour demander une analyse (IP, fichier, URL, domaine)
- Recevoir les résultats détaillés
- Relancer une analyse ou partager les résultats
- Navigation intuitive et commandes naturelles

### 🤖 Chatbot Interactif
Chatbot pour les questions et réponses sur la cybersécurité

### 🛡️ Analyses de Sécurité

#### 📍 Analyse IP
- **Géolocalisation** via IPInfo API
- **Réputation & détection** via VirusTotal et OTX AlienVault
- **Informations réseau** : ISP, organisation, timezone

#### 📁 Analyse de Fichiers
- **Vérification par hash** (MD5, SHA1, SHA256)
- **Upload direct** vers VirusTotal pour scan complet
- **Détection malware** et analyse comportementale

#### 🌐 Analyse URLs & Domaines
- **Détection malveillante** via VirusTotal et OTX
- **Réputation de domaine** et historique
- **Analyse de contenu web** et redirections

#### 📊 Résultats Détaillés
- Nombre de détections malveillantes
- Score de réputation
- Pulses de threat intelligence
- Rapports détaillés et partage

## 🛠️ Technologies Utilisées

- **Java** (Android SDK)
- **Firebase Authentication** (gestion utilisateurs)
- **AsyncTask + HttpURLConnection** pour les requêtes API
- **JSON (org.json)** pour parser les réponses
- **RecyclerView** pour l'interface chatbot
- **Material Design** pour l'UI/UX

## 📋 Prérequis

- Android Studio 4.0+
- JDK 8+
- Compte Firebase
- Clés API pour les services externes

## ⚡ Installation

### 1. Cloner le projet
```bash
git clone https://github.com/Ouerghi23/HybrideAnalysisMobile.git
cd HybrideAnalysisMobile
```

### 2. Configuration Android Studio
1. Ouvrir le projet dans Android Studio
2. Synchroniser les dépendances Gradle
3. Attendre la fin de l'indexation

### 3. Configuration des API Keys
Dans `AnalysisActivity.java`, remplacer les placeholders :

```java
public class AnalysisActivity extends AppCompatActivity {
    // API Keys - À remplacer par vos clés personnelles
    private static final String VT_API_KEY = "VOTRE_CLE_API_VIRUSTOTAL";
    private static final String OTX_API_KEY = "VOTRE_CLE_API_OTX";
    private static final String IPINFO_TOKEN = "VOTRE_TOKEN_IPINFO";
    
    // URLs des APIs
    private static final String VT_BASE_URL = "https://www.virustotal.com/vtapi/v2/";
    private static final String OTX_BASE_URL = "https://otx.alienvault.com/api/v1/";
    private static final String IPINFO_BASE_URL = "https://ipinfo.io/";
}
```

### 4. Configuration Firebase
1. Créer un projet sur [Firebase Console](https://console.firebase.google.com/)
2. Activer **Email/Password Authentication** :
   - Aller dans Authentication → Sign-in method
   - Activer "Email/Password"
3. Télécharger le fichier `google-services.json`
4. Placer le fichier dans le dossier `app/` du projet
5. Vérifier la configuration dans `build.gradle` :

```gradle
apply plugin: 'com.google.gms.google-services'

dependencies {
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-core:20.0.0'
}
```

## 🔑 Obtention des API Keys

### VirusTotal API
1. Créer un compte sur [VirusTotal](https://www.virustotal.com/)
2. Aller dans votre profil → API Key
3. Copier votre clé API

### OTX AlienVault API
1. Créer un compte sur [AlienVault OTX](https://otx.alienvault.com/)
2. Aller dans Settings → API Integration
3. Générer votre clé API

### IPInfo Token
1. Créer un compte sur [IPInfo](https://ipinfo.io/)
2. Accéder au dashboard
3. Copier votre token d'accès

## 🚀 Utilisation

### Démarrage
1. Lancer l'application
2. Créer un compte ou se connecter
3. Accéder à l'interface d'analyse ou au chatbot

### Commandes Disponibles
- `analyser ip 8.8.8.8` - Analyse d'adresse IP
- `scan fichier [hash]` - Analyse de fichier par hash
- `vérifier url https://example.com` - Analyse d'URL
- `domaine google.com` - Analyse de domaine
- `aide` - Afficher l'aide

## 🔒 Sécurité

- **Authentification** : Firebase Authentication sécurisée
- **API Keys** : Stockage sécurisé des clés (ne pas committer)
- **Permissions** : INTERNET et NETWORK_STATE uniquement
- **Validation** : Validation des entrées utilisateur

## 🐛 Dépannage

### Erreurs communes

**Erreur Firebase**
```
Solution : Vérifier que google-services.json est bien placé dans app/
```

**Erreur API Key**
```
Solution : Vérifier que les clés API sont valides et non expirées
```

**Problème de réseau**
```
Solution : Vérifier les permissions INTERNET dans AndroidManifest.xml
```

## 📊 API Limits

- **VirusTotal Free tier**: 4 requests per minute
- **OTX Free tier**: 2000 requests/day
- **IPInfo Free tier**: 50,000 requests/month


## 🤝 Contact

**Développé par Chaima Ouerghi**  
📧 Email: shaymaouerghi0@gmail.com

---
