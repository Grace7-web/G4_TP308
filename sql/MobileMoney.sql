CREATE DATABASE IF NOT EXISTS MobileMoney CHARACTER SET utf8mb4;
USE MobileMoney;

CREATE TABLE IF NOT EXISTS comptes (
    numero      VARCHAR(20)     PRIMARY KEY,
    titulaire   VARCHAR(100)    NOT NULL,
    solde       DECIMAL(15,2)   DEFAULT 0.00,
    actif       BOOLEAN         DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS transactions (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    source      VARCHAR(20),
    destination VARCHAR(20),
    montant     DECIMAL(15,2)   NOT NULL,
    type        VARCHAR(20)     NOT NULL,
    date        DATETIME        DEFAULT CURRENT_TIMESTAMP
);

-- Données de test
INSERT INTO comptes VALUES
    ('CM-001', 'GHADEUNE Grace',  500000.00, TRUE),
    ('CM-002', 'GWOS Christine',    320000.00, TRUE),
    ('CM-003', 'FOKOU Tedy',  150000.00, TRUE),
    ('CM-004', 'GUIFFO Tatchim',     80000.00, TRUE),
    ('CM-005', 'GULENYONGA Chelsy',  210000.00, TRUE),
    ('CM-006', 'HEBGA Joseph',  500000.00, TRUE),
    ('CM-007', 'IBRAHIM Kori',    320000.00, TRUE),
    ('CM-008', 'JOU Tadjeu',  150000.00, TRUE),
    ('CM-009', 'JOUFOGANG Oceanne',     80000.00, TRUE);
