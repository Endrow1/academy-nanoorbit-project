--------------------------------------------------
-- SCRIPT DE CONTRÔLE DU SCHÉMA
-- NANOORBIT
--------------------------------------------------

--------------------------------------------------
-- 1. VÉRIFICATION DES TABLES
--------------------------------------------------
SELECT table_name
FROM user_tables
ORDER BY table_name;

-- RESULTAT ATTENDU :
-- Liste des tables :
-- AFFECTATION_STATION
-- CENTRE_CONTROLE
-- EMBARQUEMENT
-- FENETRE_COM
-- HISTORIQUE_STATUT
-- INSTRUMENT
-- MISSION
-- ORBITE
-- PARTICIPATION
-- SATELLITE
-- STATION_SOL

--------------------------------------------------
-- 2. VÉRIFICATION DES CONTRAINTES
--------------------------------------------------
SELECT constraint_name,
       constraint_type,
       table_name,
       status
FROM user_constraints
ORDER BY table_name, constraint_type;

-- TYPES :
-- P = Primary Key
-- R = Foreign Key
-- C = Check
-- U = Unique

--------------------------------------------------
-- CONTRÔLE DES CLÉS PRIMAIRES
--------------------------------------------------
SELECT constraint_name, table_name
FROM user_constraints
WHERE constraint_type = 'P';

-- RESULTAT ATTENDU :
-- Une PK pour chaque table principale

--------------------------------------------------
-- CONTRÔLE DES CLÉS ÉTRANGÈRES
--------------------------------------------------
SELECT constraint_name, table_name, r_constraint_name
FROM user_constraints
WHERE constraint_type = 'R';

-- RESULTAT ATTENDU :
-- FKs présentes :
-- SATELLITE → ORBITE
-- EMBARQUEMENT → SATELLITE + INSTRUMENT
-- AFFECTATION_STATION → CENTRE_CONTROLE + STATION_SOL
-- PARTICIPATION → SATELLITE + MISSION
-- FENETRE_COM → SATELLITE + STATION_SOL
-- HISTORIQUE_STATUT → SATELLITE

--------------------------------------------------
-- CONTRÔLE DES CHECK CONSTRAINTS
--------------------------------------------------
SELECT constraint_name, table_name, search_condition
FROM user_constraints
WHERE constraint_type = 'C'
ORDER BY table_name;

-- RESULTAT ATTENDU :
-- Vérification des règles métiers :
-- statut, bornes numériques, formats cubesat, etc.

--------------------------------------------------
-- CONTRÔLE DES CONTRAINTES UNIQUES
--------------------------------------------------
SELECT constraint_name, table_name
FROM user_constraints
WHERE constraint_type = 'U';

-- RESULTAT ATTENDU :
-- Exemple : uq_orbite

--------------------------------------------------
-- 3. VÉRIFICATION DES TRIGGERS
--------------------------------------------------
SELECT trigger_name,
       table_name,
       triggering_event,
       status
FROM user_triggers
ORDER BY table_name, trigger_name;

-- RESULTAT ATTENDU :
-- trg_satellite_desorbite → FENETRE_COM
-- trg_station_maintenance → FENETRE_COM
-- trg_volume_realise → FENETRE_COM
-- trg_mission_terminee → PARTICIPATION
-- trg_historique_statut → SATELLITE

--------------------------------------------------
-- 4. CONTRÔLE DES TRIGGERS ACTIFS
--------------------------------------------------
SELECT trigger_name, status
FROM user_triggers
WHERE status = 'ENABLED';

-- RESULTAT ATTENDU :
-- Tous les triggers doivent être ENABLED

--------------------------------------------------
-- 5. CONTRÔLE DES ERREURS ÉVENTUELLES
--------------------------------------------------
SELECT name, type, line, text
FROM user_errors;

-- RESULTAT ATTENDU :
-- Aucune ligne retournée (sinon erreurs de compilation)

--------------------------------------------------
-- 6. CONTRÔLE GLOBAL RAPIDE
--------------------------------------------------

-- Nombre de tables
SELECT COUNT(*) AS nb_tables FROM user_tables;

-- Nombre de contraintes
SELECT COUNT(*) AS nb_constraints FROM user_constraints;

-- Nombre de triggers
SELECT COUNT(*) AS nb_triggers FROM user_triggers;

-- RESULTAT ATTENDU :
-- Cohérent avec le modèle (≈ 10 tables, plusieurs contraintes, 5 triggers)

--------------------------------------------------