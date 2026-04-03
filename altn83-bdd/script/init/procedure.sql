--------------------------------------------------
-- NANOORBIT - PHASE 3
-- PL/SQL - PALIERS 1 À 5
-- EFREI - ALTN83
--------------------------------------------------

SET SERVEROUTPUT ON;

--------------------------------------------------
-- PALIER 1 — BLOC ANONYME
--------------------------------------------------

--------------------------------------------------
-- Ex.1 : Message + nombre d’objets
--------------------------------------------------
BEGIN
    DECLARE
        v_nb_sat NUMBER;
        v_nb_station NUMBER;
        v_nb_mission NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_nb_sat FROM SATELLITE;
        SELECT COUNT(*) INTO v_nb_station FROM STATION_SOL;
        SELECT COUNT(*) INTO v_nb_mission FROM MISSION;

        DBMS_OUTPUT.PUT_LINE('Bienvenue dans NanoOrbit 🚀');
        DBMS_OUTPUT.PUT_LINE('Satellites : ' || v_nb_sat);
        DBMS_OUTPUT.PUT_LINE('Stations : ' || v_nb_station);
        DBMS_OUTPUT.PUT_LINE('Missions : ' || v_nb_mission);
    END;
END;
/
-- Résultat attendu :
-- Bienvenue dans NanoOrbit
-- Satellites : 5
-- Stations : 3
-- Missions : 3

--------------------------------------------------
-- Ex.2 : SELECT INTO SAT-001
--------------------------------------------------
BEGIN
    DECLARE
        v_nom VARCHAR2(100);
        v_statut VARCHAR2(30);
        v_batterie NUMBER;
    BEGIN
        SELECT nom_satellite, statut, capacite_batterie
        INTO v_nom, v_statut, v_batterie
        FROM SATELLITE
        WHERE id_satellite = 'SAT-001';

        DBMS_OUTPUT.PUT_LINE(v_nom || ' - ' || v_statut || ' - Batterie : ' || v_batterie);
    END;
END;
/
-- Résultat attendu :
-- NanoOrbit-Alpha - Opérationnel - Batterie : 20

--------------------------------------------------
-- PALIER 2 — VARIABLES ET TYPES
--------------------------------------------------

--------------------------------------------------
-- Ex.3 : %ROWTYPE
--------------------------------------------------
DECLARE
    v_sat SATELLITE%ROWTYPE;
BEGIN
    SELECT * INTO v_sat
    FROM SATELLITE
    WHERE id_satellite = 'SAT-001';

    DBMS_OUTPUT.PUT_LINE('Statut : ' || v_sat.statut);
    DBMS_OUTPUT.PUT_LINE('Batterie : ' || v_sat.capacite_batterie);
END;
/
-- Résultat attendu :
-- Statut : Opérationnel
-- Batterie : 20

--------------------------------------------------
-- Ex.4 : NVL
--------------------------------------------------
DECLARE
    v_resolution VARCHAR2(20);
BEGIN
    SELECT NVL(TO_CHAR(resolution), 'N/A')
    INTO v_resolution
    FROM INSTRUMENT
    WHERE ref_instrument = 'INS-AIS-01';

    DBMS_OUTPUT.PUT_LINE('Résolution : ' || v_resolution);
END;
/
-- Résultat attendu :
-- Résolution : N/A

--------------------------------------------------
-- PALIER 3 — STRUCTURES DE CONTRÔLE
--------------------------------------------------

--------------------------------------------------
-- Ex.5 : IF / ELSIF
--------------------------------------------------
DECLARE
    v_statut VARCHAR2(30);
    v_duree NUMBER;
BEGIN
    SELECT statut, duree_vie_prevue
    INTO v_statut, v_duree
    FROM SATELLITE
    WHERE id_satellite = 'SAT-001';

    IF v_statut = 'Opérationnel' AND v_duree > 50 THEN
        DBMS_OUTPUT.PUT_LINE('Satellite performant');
    ELSIF v_statut = 'En veille' THEN
        DBMS_OUTPUT.PUT_LINE('Satellite en attente');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Satellite critique');
    END IF;
END;
/
-- Résultat attendu :
-- Satellite performant

--------------------------------------------------
-- Ex.6 : CASE + vitesse orbitale
--------------------------------------------------
DECLARE
    v_type VARCHAR2(10);
    v_altitude NUMBER;
    v_periode NUMBER;
    v_vitesse NUMBER;
BEGIN
    SELECT o.type_orbite, o.altitude, o.periode_orbitale
    INTO v_type, v_altitude, v_periode
    FROM SATELLITE s
             JOIN ORBITE o ON s.id_orbite = o.id_orbite
    WHERE s.id_satellite = 'SAT-001';

    v_vitesse := 2 * 3.1416 * (6371 + v_altitude) / v_periode;

    CASE v_type
        WHEN 'SSO' THEN DBMS_OUTPUT.PUT_LINE('Orbite héliosynchrone');
        WHEN 'LEO' THEN DBMS_OUTPUT.PUT_LINE('Orbite basse');
        ELSE DBMS_OUTPUT.PUT_LINE('Autre orbite');
        END CASE;

    DBMS_OUTPUT.PUT_LINE('Vitesse approx : ' || ROUND(v_vitesse,2));
END;
/
-- Résultat attendu :
-- Orbite héliosynchrone
-- Vitesse approx : ~455 km/min

--------------------------------------------------
-- Ex.7 : Boucle FOR
--------------------------------------------------
DECLARE
    v_debit NUMBER;
BEGIN
    SELECT debit_max INTO v_debit
    FROM STATION_SOL
    WHERE code_station = 'GS-TLS-01';

    FOR i IN 5..15 LOOP
            DBMS_OUTPUT.PUT_LINE('Durée ' || i || ' min → Volume : ' || (i * v_debit));
        END LOOP;
END;
/
-- Résultat attendu :
-- Durée 5 → 750
-- ...
-- Durée 15 → 2250

--------------------------------------------------
-- PALIER 4 — CURSEURS
--------------------------------------------------

--------------------------------------------------
-- Ex.8 : SQL%ROWCOUNT
--------------------------------------------------
BEGIN
    UPDATE SATELLITE
    SET statut = 'En veille'
    WHERE statut = 'Opérationnel';

    DBMS_OUTPUT.PUT_LINE('Lignes modifiées : ' || SQL%ROWCOUNT);

    ROLLBACK;
END;
/
-- Résultat attendu :
-- Lignes modifiées : 3

--------------------------------------------------
-- Ex.9 : Cursor FOR Loop
--------------------------------------------------
BEGIN
    FOR rec IN (
        SELECT s.id_satellite, s.statut, o.type_orbite, i.type_instrument
        FROM SATELLITE s
                 LEFT JOIN ORBITE o ON s.id_orbite = o.id_orbite
                 LEFT JOIN EMBARQUEMENT e ON s.id_satellite = e.id_satellite
                 LEFT JOIN INSTRUMENT i ON e.ref_instrument = i.ref_instrument
        )
        LOOP
            DBMS_OUTPUT.PUT_LINE(rec.id_satellite || ' | ' || rec.type_orbite || ' | ' || rec.statut || ' | ' || rec.type_instrument);
        END LOOP;
END;
/
-- Résultat attendu :
-- Liste de tous les satellites avec instruments

--------------------------------------------------
-- Ex.10 : Curseur explicite
--------------------------------------------------
DECLARE
    CURSOR c_sat IS
        SELECT s.id_satellite, f.code_station
        FROM SATELLITE s
                 JOIN FENETRE_COM f ON s.id_satellite = f.id_satellite
        WHERE s.statut = 'Opérationnel';

    v_rec c_sat%ROWTYPE;
BEGIN
    OPEN c_sat;
    LOOP
        FETCH c_sat INTO v_rec;
        EXIT WHEN c_sat%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(v_rec.id_satellite || ' → ' || v_rec.code_station);
    END LOOP;
    CLOSE c_sat;
END;
/
-- Résultat attendu :
-- SAT-001 → GS-KIR-01 ...

--------------------------------------------------
-- Ex.11 : Curseur paramétré
--------------------------------------------------
DECLARE
    CURSOR c_fenetre(p_station VARCHAR2) IS
        SELECT NVL(volume_donnees,0) volume
        FROM FENETRE_COM
        WHERE code_station = p_station;

    v_total NUMBER := 0;
BEGIN
    FOR rec IN c_fenetre('GS-TLS-01') LOOP
            v_total := v_total + rec.volume;
        END LOOP;

    DBMS_OUTPUT.PUT_LINE('Volume total : ' || v_total);
END;
/
-- Résultat attendu :
-- Volume total : 890

--------------------------------------------------
-- PALIER 5 — PROCÉDURES & FONCTIONS
--------------------------------------------------

--------------------------------------------------
-- Ex.12 : Exceptions
--------------------------------------------------
DECLARE
    v_nom VARCHAR2(100);
BEGIN
    SELECT nom_satellite INTO v_nom
    FROM SATELLITE
    WHERE id_satellite = 'SAT-999';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Satellite introuvable');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erreur : ' || SQLERRM);
END;
/
-- Résultat attendu :
-- Satellite introuvable

--------------------------------------------------
-- Ex.13 : RAISE_APPLICATION_ERROR
--------------------------------------------------
CREATE OR REPLACE PROCEDURE ajouter_fenetre (
    p_sat VARCHAR2,
    p_station VARCHAR2
) IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM SATELLITE
    WHERE id_satellite = p_sat AND statut = 'Opérationnel';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Satellite non opérationnel');
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM STATION_SOL
    WHERE code_station = p_station AND statut = 'Active';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Station inactive');
    END IF;

    DBMS_OUTPUT.PUT_LINE('Insertion valide');
END;
/
-- Résultat attendu :
-- OK ou erreur personnalisée

--------------------------------------------------
-- Ex.14 : Procédure afficher_statut_satellite
--------------------------------------------------
CREATE OR REPLACE PROCEDURE afficher_statut_satellite(p_id IN VARCHAR2) IS
BEGIN
    FOR rec IN (
        SELECT s.statut, o.type_orbite, i.type_instrument
        FROM SATELLITE s
                 LEFT JOIN ORBITE o ON s.id_orbite = o.id_orbite
                 LEFT JOIN EMBARQUEMENT e ON s.id_satellite = e.id_satellite
                 LEFT JOIN INSTRUMENT i ON e.ref_instrument = i.ref_instrument
        WHERE s.id_satellite = p_id
        )
        LOOP
            DBMS_OUTPUT.PUT_LINE(rec.statut || ' | ' || rec.type_orbite || ' | ' || rec.type_instrument);
        END LOOP;
END;
/
-- Résultat attendu :
-- Opérationnel | SSO | Caméra optique ...

--------------------------------------------------
-- Ex.15 : Procédure mise à jour
--------------------------------------------------
CREATE OR REPLACE PROCEDURE mettre_a_jour_statut (
    p_id IN VARCHAR2,
    p_statut IN VARCHAR2,
    p_ancien_statut OUT VARCHAR2
) IS
BEGIN
    SELECT statut INTO p_ancien_statut
    FROM SATELLITE
    WHERE id_satellite = p_id;

    UPDATE SATELLITE
    SET statut = p_statut
    WHERE id_satellite = p_id;
END;
/
-- Résultat attendu :
-- Ancien statut retourné via OUT

--------------------------------------------------
-- Ex.16 : Fonction volume
--------------------------------------------------
CREATE OR REPLACE FUNCTION calculer_volume_session (
    p_id_fenetre IN NUMBER
) RETURN NUMBER IS
    v_duree NUMBER;
    v_debit NUMBER;
BEGIN
    SELECT f.duree, s.debit_max
    INTO v_duree, v_debit
    FROM FENETRE_COM f
             JOIN STATION_SOL s ON f.code_station = s.code_station
    WHERE f.id_fenetre = p_id_fenetre;

    RETURN v_duree * v_debit;
END;
/
-- Résultat attendu :
-- Volume calculé (ex: 420 * 400 = 168000)

--------------------------------------------------
-- FIN PHASE 3
--------------------------------------------------