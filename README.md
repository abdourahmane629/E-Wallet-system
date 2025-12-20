💳 Système de Portefeuille Électronique (E-Wallet)
📌 Présentation du projet

Ce projet vise à concevoir et développer un système de portefeuille électronique (E-Wallet) permettant aux utilisateurs d’effectuer des opérations financières de manière sécurisée, rapide et traçable à travers une interface numérique.

Le système permet la gestion des comptes utilisateurs, des portefeuilles électroniques, des transactions financières, ainsi que la génération de rapports et statistiques destinés aux administrateurs.

Ce projet est réalisé dans le cadre d’un projet académique de groupe.

🎯 Objectifs du projet
Objectif général

Développer une application E-Wallet assurant la gestion sécurisée des comptes et des transactions financières via une interface intuitive et performante.

Objectifs spécifiques

Permettre la création et la gestion des comptes utilisateurs

Faciliter les dépôts, retraits et transferts d’argent

Assurer la traçabilité complète des transactions

Garantir la sécurité et la confidentialité des données

Fournir des rapports et historiques clairs

👥 Acteurs du système

Administrateur : gestion globale, supervision et rapports

Utilisateur (Client) : opérations financières et consultation du solde

Agent : dépôt et retrait pour le compte des utilisateurs

⚙️ Fonctionnalités principales
🔐 Authentification et sécurité

Inscription et connexion des utilisateurs

Hashage des mots de passe

Gestion des rôles et permissions

Verrouillage du compte après tentatives échouées

Journalisation des actions

👤 Gestion des utilisateurs

Création et modification de comptes

Activation / désactivation des comptes

Gestion des informations personnelles

💼 Gestion du portefeuille électronique

Création automatique d’un portefeuille par utilisateur

Consultation du solde en temps réel

Historique des opérations

Blocage / déblocage du portefeuille

💰 Gestion des transactions

Dépôt d’argent

Retrait d’argent

Transfert entre portefeuilles

Paiement de services

Suivi du statut des transactions

📊 Rapports et statistiques

Historique des transactions par utilisateur

Rapports journaliers et mensuels

Export PDF / Excel

Tableau de bord statistique

🏗️ Architecture du système

Le système repose sur une architecture client–serveur :

Backend : API REST

Frontend : Interface utilisateur web ou desktop

Base de données : MySQL

Communication sécurisée : HTTPS

🛠️ Technologies utilisées

Langage Backend : Java (Spring Boot)

Base de données : MySQL

Frontend : HTML, CSS, JavaScript / JavaFX

Serveur : Apache Tomcat

Sécurité : Hashage des mots de passe, SSL/TLS

Gestion de version : Git & GitHub

📁 Structure du projet
e-wallet-system/
│
├── backend/        # API backend Java
├── frontend/       # Interface utilisateur
├── database/       # Scripts SQL et MCD
├── docs/           # Cahier des charges, diagrammes
│
├── README.md
└── .gitignore

👨‍💻 Équipe du projet

⚠️ Cette section sera complétée après la constitution définitive de l’équipe.

Chef de projet : À définir

Responsable Base de Données : À définir

Développeur Backend – Sécurité : À définir

Développeur Backend – Transactions : À définir

Développeur Frontend : À définir

🚀 Installation et exécution (exemple)
1️⃣ Cloner le projet
git clone https://github.com/nom_du_depot/e-wallet-system.git

2️⃣ Configuration de la base de données

Créer une base MySQL nommée ewallet_db

Importer les scripts SQL depuis le dossier /database

3️⃣ Lancer le backend

Ouvrir le projet backend dans un IDE (IntelliJ, Eclipse)

Lancer l’application Spring Boot

4️⃣ Lancer le frontend

Ouvrir index.html dans un navigateur

Ou lancer l’application JavaFX

📄 Livrables attendus

Cahier des charges validé

Modèle Conceptuel de Données (MCD)

Scripts SQL MySQL

Application fonctionnelle

Code source documenté

Manuel utilisateur

Guide d’administration

📜 Licence

Projet réalisé à des fins académiques.
