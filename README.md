# 💳 Système de Portefeuille Électronique (E-Wallet)

### Application de gestion financière numérique sécurisée

</div>

---

## 📌 Présentation du projet

Ce projet vise à concevoir et développer un **système de portefeuille électronique (E-Wallet)** permettant aux utilisateurs d’effectuer des opérations financières de manière **sécurisée, rapide et traçable** à travers une interface numérique.

Le système permet :
- La gestion des **comptes utilisateurs**
- La gestion des **portefeuilles électroniques**
- Le traitement des **transactions financières**
- La génération de **rapports et statistiques** pour les administrateurs

🎓 **Projet académique réalisé en groupe**

---

## 🎯 Objectifs du projet

### 🎯 Objectif général

Développer une application **E-Wallet** assurant la gestion sécurisée des comptes et des transactions financières via une interface **intuitive et performante**.

### 🎯 Objectifs spécifiques

- 👤 Permettre la création et la gestion des comptes utilisateurs  
- 💰 Faciliter les dépôts, retraits et transferts d’argent  
- 📜 Assurer la traçabilité complète des transactions  
- 🔐 Garantir la sécurité et la confidentialité des données  
- 📊 Fournir des rapports et historiques clairs  

---

## 👥 Acteurs du système

| Acteur | Rôle |
|------|------|
| 👨‍💼 **Administrateur** | Gestion globale, supervision, rapports |
| 👤 **Utilisateur (Client)** | Opérations financières, consultation du solde |
| 🧑‍💻 **Agent** | Dépôt et retrait pour le compte des utilisateurs |

---

## ⚙️ Fonctionnalités principales

### 🔐 Authentification et sécurité
- Inscription et connexion des utilisateurs  
- Hashage des mots de passe  
- Gestion des rôles et permissions  
- Verrouillage du compte après tentatives échouées  
- Journalisation des actions  

### 👤 Gestion des utilisateurs
- Création et modification des comptes  
- Activation / désactivation des comptes  
- Gestion des informations personnelles  

### 💼 Gestion du portefeuille électronique
- Création automatique d’un portefeuille par utilisateur  
- Consultation du solde en temps réel  
- Historique des opérations  
- Blocage / déblocage du portefeuille  

### 💰 Gestion des transactions
- Dépôt d’argent  
- Retrait d’argent  
- Transfert entre portefeuilles  
- Paiement de services  
- Suivi du statut des transactions  

### 📊 Rapports et statistiques
- Historique des transactions par utilisateur  
- Rapports journaliers et mensuels  
- Tableau de bord statistique  

---

## 🏗️ Architecture du système

Le système repose sur une **architecture client–serveur** :

- **Backend** : API REST  
- **Frontend** : Interface utilisateur Web ou Desktop  
- **Base de données** : MySQL  
- **Communication sécurisée** : HTTPS  

---

## 🛠️ Technologies utilisées

### Backend
- ☕ Java (Spring Boot)  
- 🌐 Apache Tomcat  

### Frontend
- 🌐 HTML  
- 🎨 CSS  
- ⚙️ JavaScript / JavaFX  

### Base de données
- 🗄️ MySQL  

### Sécurité
- 🔐 Hashage des mots de passe  
- 🔒 SSL / TLS  

### Outils
- 🧰 Git & GitHub  

---

## 📁 Structure du projet


e-wallet-system/
│
├── backend/        # API backend Java (Spring Boot)
├── frontend/       # Interface utilisateur
├── database/       # Scripts SQL, MCD, MLD
├── docs/           # Cahier des charges, diagrammes UML
│
├── README.md
└── .gitignore
