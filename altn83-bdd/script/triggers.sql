--------------------------------------------------
-- TRIGGERS
--------------------------------------------------

-- T1 : Satellite désorbité interdit
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

-- T2 : Station en maintenance
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

-- T3 : Volume cohérent
CREATE OR REPLACE TRIGGER trg_volume_realise
              BEFORE INSERT OR UPDATE ON FENETRE_COM
    FOR EACH ROW
BEGIN
    IF :NEW.statut = 'Planifiée' AND :NEW.volume_donnees IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20003, 'Volume interdit si planifiée');
END IF;
END;
/

-- T4 : Mission terminée
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

-- T5 : Historique des statuts
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
--------------------------------------------------
COMMIT;
--------------------------------------------------