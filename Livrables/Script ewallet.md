-- Script de création de la base de données E-Wallet pour phpMyAdmin
-- Version simplifiée - 8 tables

-- -----------------------------------------------------
-- Base de données
-- -----------------------------------------------------
DROP DATABASE IF EXISTS ewallet_db;
CREATE DATABASE ewallet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ewallet_db;

-- -----------------------------------------------------
-- Table `role`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `role` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `nom_role` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE INDEX `nom_role_UNIQUE` (`nom_role` ASC)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `utilisateur`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `utilisateur_id` INT NOT NULL AUTO_INCREMENT,
  `nom` VARCHAR(100) NOT NULL,
  `prenom` VARCHAR(100) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `telephone` VARCHAR(20) NULL,
  `adresse` VARCHAR(255) NULL,
  `mot_de_passe_hash` VARCHAR(255) NOT NULL,
  `pin_hash` VARCHAR(255) NULL,
  `role_id` INT NOT NULL,
  `statut` ENUM('actif', 'inactif', 'suspendu', 'en_attente') NULL DEFAULT 'en_attente',
  `tentatives_echecs` INT NULL DEFAULT 0,
  `compte_verrouille` TINYINT NULL DEFAULT 0,
  `date_verrouillage` DATETIME NULL,
  `date_inscription` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login` TIMESTAMP NULL,
  PRIMARY KEY (`utilisateur_id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `telephone_UNIQUE` (`telephone` ASC),
  INDEX `fk_utilisateur_role1_idx` (`role_id` ASC),
  CONSTRAINT `fk_utilisateur_role1`
    FOREIGN KEY (`role_id`)
    REFERENCES `role` (`role_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `portefeuille`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `portefeuille` (
  `portefeuille_id` INT NOT NULL AUTO_INCREMENT,
  `utilisateur_id` INT NOT NULL,
  `numero_portefeuille` VARCHAR(50) NOT NULL,
  `solde` DECIMAL(15,2) NULL DEFAULT 0.00,
  `devise` VARCHAR(3) NULL DEFAULT 'GNF',
  `statut` ENUM('actif', 'inactif', 'bloque', 'en_attente') NULL DEFAULT 'actif',
  `limite_retrait_quotidien` DECIMAL(15,2) NULL DEFAULT 100000.00,
  `limite_transfert` DECIMAL(15,2) NULL DEFAULT 500000.00,
  `date_creation` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`portefeuille_id`),
  UNIQUE INDEX `numero_portefeuille_UNIQUE` (`numero_portefeuille` ASC),
  UNIQUE INDEX `utilisateur_id_UNIQUE` (`utilisateur_id` ASC),
  INDEX `fk_portefeuille_utilisateur1_idx` (`utilisateur_id` ASC),
  CONSTRAINT `fk_portefeuille_utilisateur1`
    FOREIGN KEY (`utilisateur_id`)
    REFERENCES `utilisateur` (`utilisateur_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `type_transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `type_transaction` (
  `type_id` INT NOT NULL AUTO_INCREMENT,
  `nom_type` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`type_id`),
  UNIQUE INDEX `nom_type_UNIQUE` (`nom_type` ASC)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `transaction` (
  `transaction_id` INT NOT NULL AUTO_INCREMENT,
  `numero_transaction` VARCHAR(50) NOT NULL,
  `type_id` INT NOT NULL,
  `montant` DECIMAL(15,2) NOT NULL,
  `portefeuille_source_id` INT NULL,
  `portefeuille_destination_id` INT NULL,
  `statut` ENUM('en_attente', 'termine', 'annule', 'echoue', 'en_cours') NULL DEFAULT 'en_attente',
  `description` VARCHAR(255) NULL,
  `date_transaction` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `date_confirmation` DATETIME NULL,
  `agent_id` INT NULL,
  `date_modification` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  UNIQUE INDEX `numero_transaction_UNIQUE` (`numero_transaction` ASC),
  INDEX `fk_transaction_portefeuille1_idx` (`portefeuille_source_id` ASC),
  INDEX `fk_transaction_portefeuille2_idx` (`portefeuille_destination_id` ASC),
  INDEX `fk_transaction_type_transaction1_idx` (`type_id` ASC),
  INDEX `fk_transaction_utilisateur1_idx` (`agent_id` ASC),
  INDEX `idx_statut` (`statut` ASC),
  INDEX `idx_date_transaction` (`date_transaction` ASC),
  CONSTRAINT `fk_transaction_portefeuille1`
    FOREIGN KEY (`portefeuille_source_id`)
    REFERENCES `portefeuille` (`portefeuille_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_portefeuille2`
    FOREIGN KEY (`portefeuille_destination_id`)
    REFERENCES `portefeuille` (`portefeuille_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_type_transaction1`
    FOREIGN KEY (`type_id`)
    REFERENCES `type_transaction` (`type_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_utilisateur1`
    FOREIGN KEY (`agent_id`)
    REFERENCES `utilisateur` (`utilisateur_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `commission`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `commission` (
  `commission_id` INT NOT NULL AUTO_INCREMENT,
  `agent_id` INT NOT NULL,
  `transaction_id` INT NULL,
  `montant_commission` DECIMAL(15,2) NOT NULL,
  `pourcentage` DECIMAL(5,2) NULL,
  `date_creation` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `statut` ENUM('en_attente', 'paye', 'annule') NULL DEFAULT 'en_attente',
  PRIMARY KEY (`commission_id`),
  INDEX `fk_commission_utilisateur1_idx` (`agent_id` ASC),
  INDEX `fk_commission_transaction1_idx` (`transaction_id` ASC),
  CONSTRAINT `fk_commission_utilisateur1`
    FOREIGN KEY (`agent_id`)
    REFERENCES `utilisateur` (`utilisateur_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_commission_transaction1`
    FOREIGN KEY (`transaction_id`)
    REFERENCES `transaction` (`transaction_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `journal_audit`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `journal_audit` (
  `journal_id` INT NOT NULL AUTO_INCREMENT,
  `utilisateur_id` INT NULL,
  `action` VARCHAR(100) NOT NULL,
  `entite` VARCHAR(100) NULL,
  `entite_id` INT NULL,
  `ancienne_valeur` TEXT NULL,
  `nouvelle_valeur` TEXT NULL,
  `adresse_ip` VARCHAR(45) NULL,
  `date_action` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`journal_id`),
  INDEX `fk_journal_audit_utilisateur1_idx` (`utilisateur_id` ASC),
  INDEX `idx_action` (`action` ASC),
  INDEX `idx_date_action` (`date_action` ASC),
  CONSTRAINT `fk_journal_audit_utilisateur1`
    FOREIGN KEY (`utilisateur_id`)
    REFERENCES `utilisateur` (`utilisateur_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification` (
  `notification_id` INT NOT NULL AUTO_INCREMENT,
  `utilisateur_id` INT NOT NULL,
  `titre` VARCHAR(255) NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(50) NULL,
  `est_lue` TINYINT NULL DEFAULT 0,
  `date_creation` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  INDEX `fk_notification_utilisateur1_idx` (`utilisateur_id` ASC),
  INDEX `idx_est_lue` (`est_lue` ASC),
  CONSTRAINT `fk_notification_utilisateur1`
    FOREIGN KEY (`utilisateur_id`)
    REFERENCES `utilisateur` (`utilisateur_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Vues
-- -----------------------------------------------------

-- Vue pour les soldes des utilisateurs
CREATE OR REPLACE VIEW `v_soldes_utilisateurs` AS
SELECT 
    u.utilisateur_id,
    u.email,
    u.nom,
    u.prenom,
    p.numero_portefeuille,
    p.solde,
    p.devise,
    p.statut,
    p.date_creation
FROM utilisateur u
INNER JOIN portefeuille p ON u.utilisateur_id = p.utilisateur_id;

-- Vue pour l'historique des transactions
CREATE OR REPLACE VIEW `v_historique_transactions` AS
SELECT 
    t.transaction_id,
    t.numero_transaction,
    tt.nom_type AS type_transaction,
    t.montant,
    u1.email AS email_source,
    u2.email AS email_destination,
    t.statut,
    t.date_transaction,
    t.description
FROM transaction t
INNER JOIN type_transaction tt ON t.type_id = tt.type_id
LEFT JOIN portefeuille ps ON t.portefeuille_source_id = ps.portefeuille_id
LEFT JOIN utilisateur u1 ON ps.utilisateur_id = u1.utilisateur_id
LEFT JOIN portefeuille pd ON t.portefeuille_destination_id = pd.portefeuille_id
LEFT JOIN utilisateur u2 ON pd.utilisateur_id = u2.utilisateur_id;

-- Vue pour les commissions des agents
CREATE OR REPLACE VIEW `v_commissions_agents` AS
SELECT 
    u.utilisateur_id,
    u.email,
    u.nom,
    u.prenom,
    COUNT(c.commission_id) AS nombre_commissions,
    SUM(c.montant_commission) AS total_commissions,
    SUM(CASE WHEN c.statut = 'en_attente' THEN c.montant_commission ELSE 0 END) AS commissions_en_attente,
    SUM(CASE WHEN c.statut = 'paye' THEN c.montant_commission ELSE 0 END) AS commissions_payees
FROM utilisateur u
INNER JOIN commission c ON u.utilisateur_id = c.agent_id
GROUP BY u.utilisateur_id, u.email, u.nom, u.prenom;

-- -----------------------------------------------------
-- Insertion des données minimales nécessaires
-- -----------------------------------------------------

-- Rôles
INSERT INTO `role` (`nom_role`, `description`) VALUES
('admin', 'Administrateur système avec tous les droits'),
('agent', 'Agent pouvant effectuer des opérations pour les clients'),
('client', 'Utilisateur standard du portefeuille électronique');

-- Types de transactions
INSERT INTO `type_transaction` (`nom_type`, `description`) VALUES
('depot', 'Dépôt d''argent sur le portefeuille'),
('retrait', 'Retrait d''argent du portefeuille'),
('transfert', 'Transfert d''argent entre portefeuilles'),
('paiement', 'Paiement de services');


-- -----------------------------------------------------
-- Index supplémentaires
-- -----------------------------------------------------

CREATE INDEX `idx_utilisateur_email` ON `utilisateur` (`email`);
CREATE INDEX `idx_portefeuille_numero` ON `portefeuille` (`numero_portefeuille`);
CREATE INDEX `idx_transaction_dates` ON `transaction` (`date_transaction`, `date_confirmation`);
CREATE INDEX `idx_commission_statut` ON `commission` (`statut`, `date_creation`);
CREATE INDEX `idx_notification_date` ON `notification` (`date_creation`, `est_lue`);

-- -----------------------------------------------------
-- Fin du script
-- -----------------------------------------------------

-- Message de confirmation
SELECT 'Base de données E-Wallet créée avec succès!' AS message;
SELECT COUNT(*) AS nombre_tables FROM information_schema.tables 
WHERE table_schema = DATABASE();


SELECT 'Base de données E-Wallet créée avec succès dans phpMyAdmin!' AS message;
SELECT COUNT(*) AS nombre_tables FROM information_schema.tables 
WHERE table_schema = DATABASE();
SELECT table_name AS tables_créées FROM information_schema.tables 
WHERE table_schema = DATABASE() ORDER BY table_name;