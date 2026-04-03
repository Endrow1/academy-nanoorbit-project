--------------------------------------------------
-- TRIGGERS
--------------------------------------------------

--------------------------------------------------
-- T1 : Satellite désorbité interdit
--------------------------------------------------
CREATE OR REPLACE TRIGGER trg_satellite_desorbite
    BEFORE INSERT ON FENETRE_COM
    FOR EACH ROW
DECLARE
    v_statut SATELLITE.statut%TYPE;
BEGIN
    SELECT statut INTO v_statut
    FROM SATELLITE
    WHERE id_satellite = :NEW.id_satellite;

    IF v_statut = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Satellite désorbité interdit');
    END IF;
END;
/
SHOW ERRORS;

--------------------------------------------------
-- TESTS T1
--------------------------------------------------

-- CAS VALIDE
-- Prérequis : satellite non désorbité + station existante
/*
INSERT INTO SATELLITE VALUES ('SAT-TEST','Test',SYSDATE,10,'1U','Opérationnel',5,100,NULL);

INSERT INTO STATION_SOL VALUES ('ST-1','Station Test',45,2,10,'X',100,'Active');

INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,120,40,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- Insertion réussie

-- CAS EN ERREUR
/*
UPDATE SATELLITE
SET statut = 'Désorbité'
WHERE id_satellite = 'SAT-TEST';

INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,120,40,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- ORA-20001: Satellite désorbité interdit

--------------------------------------------------
-- T2 : Station en maintenance
--------------------------------------------------
CREATE OR REPLACE TRIGGER trg_station_maintenance
    BEFORE INSERT ON FENETRE_COM
    FOR EACH ROW
DECLARE
    v_statut STATION_SOL.statut%TYPE;
BEGIN
    SELECT statut INTO v_statut
    FROM STATION_SOL
    WHERE code_station = :NEW.code_station;

    IF v_statut = 'Maintenance' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Station en maintenance');
    END IF;
END;
/
SHOW ERRORS;

--------------------------------------------------
-- TESTS T2
--------------------------------------------------

-- CAS VALIDE
/*
UPDATE STATION_SOL
SET statut = 'Active'
WHERE code_station = 'ST-1';

INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,100,30,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- Insertion réussie

-- CAS EN ERREUR
/*
UPDATE STATION_SOL
SET statut = 'Maintenance'
WHERE code_station = 'ST-1';

INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,100,30,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- ORA-20002: Station en maintenance

--------------------------------------------------
-- T3 : Volume cohérent
--------------------------------------------------
CREATE OR REPLACE TRIGGER trg_volume_realise
    BEFORE INSERT OR UPDATE ON FENETRE_COM
    FOR EACH ROW
BEGIN
    IF :NEW.statut = 'Planifiée' AND :NEW.volume_donnees IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20003, 'Volume interdit si planifiée');
    END IF;

    IF :NEW.statut = 'Réalisée' AND :NEW.volume_donnees IS NULL THEN
        RAISE_APPLICATION_ERROR(-20003, 'Volume obligatoire si réalisée');
    END IF;
END;
/
SHOW ERRORS;

--------------------------------------------------
-- TESTS T3
--------------------------------------------------

-- CAS VALIDE
/*
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,120,40,NULL,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- Insertion réussie

-- CAS EN ERREUR
/*
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station)
VALUES (SYSTIMESTAMP,120,40,500,'Planifiée','SAT-TEST','ST-1');
*/

-- RESULTAT ATTENDU :
-- ORA-20003: Volume interdit si planifiée

--------------------------------------------------
-- T4 : Mission terminée
--------------------------------------------------
CREATE OR REPLACE TRIGGER trg_mission_terminee
    BEFORE INSERT ON PARTICIPATION
    FOR EACH ROW
DECLARE
    v_statut MISSION.statut_mission%TYPE;
BEGIN
    SELECT statut_mission INTO v_statut
    FROM MISSION
    WHERE id_mission = :NEW.id_mission;

    IF v_statut = 'Terminée' THEN
        RAISE_APPLICATION_ERROR(-20004, 'Mission terminée');
    END IF;
END;
/
SHOW ERRORS;

--------------------------------------------------
-- TESTS T4
--------------------------------------------------

-- CAS VALIDE
/*
INSERT INTO MISSION VALUES ('M1','Mission Test','Obj','Zone',SYSDATE,NULL,'En cours');

INSERT INTO PARTICIPATION VALUES ('SAT-TEST','M1','Imageur');
*/

-- RESULTAT ATTENDU :
-- Insertion réussie

-- CAS EN ERREUR
/*
UPDATE MISSION
SET statut_mission = 'Terminée'
WHERE id_mission = 'M1';

INSERT INTO PARTICIPATION VALUES ('SAT-TEST','M1','Relais');
*/

-- RESULTAT ATTENDU :
-- ORA-20004: Mission terminée

--------------------------------------------------
-- T5 : Historique des statuts
--------------------------------------------------
CREATE OR REPLACE TRIGGER trg_historique_statut
    AFTER UPDATE OF statut ON SATELLITE
    FOR EACH ROW
BEGIN
    INSERT INTO HISTORIQUE_STATUT (
        id_satellite,
        ancien_statut,
        nouveau_statut
    )
    VALUES (
               :OLD.id_satellite,
               :OLD.statut,
               :NEW.statut
           );
END;
/
SHOW ERRORS;

--------------------------------------------------
-- TESTS T5
--------------------------------------------------

-- CAS VALIDE
/*
UPDATE SATELLITE
SET statut = 'En veille'
WHERE id_satellite = 'SAT-TEST';
*/

-- RESULTAT ATTENDU :
-- Mise à jour réussie + insertion dans HISTORIQUE_STATUT

-- VERIFICATION
/*
SELECT * FROM HISTORIQUE_STATUT
WHERE id_satellite = 'SAT-TEST';
*/

-- RESULTAT ATTENDU :
-- Une ligne avec ancien_statut et nouveau_statut renseignés

--------------------------------------------------
COMMIT;
--------------------------------------------------

