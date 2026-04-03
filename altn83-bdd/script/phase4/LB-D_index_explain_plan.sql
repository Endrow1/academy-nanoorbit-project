--------------------------------------------------
-- NANOORBIT - PHASE 4
-- L4-D - INDEX, EXPLAIN PLAN ET RAPPORT FINAL
--------------------------------------------------

--------------------------------------------------
-- Ex. 17 - Index strategiques manquants
--------------------------------------------------
CREATE INDEX idx_fenetre_satellite
    ON FENETRE_COM (id_satellite);

CREATE INDEX idx_fenetre_station
    ON FENETRE_COM (code_station);

CREATE INDEX idx_participation_satellite
    ON PARTICIPATION (id_satellite);

CREATE INDEX idx_participation_mission
    ON PARTICIPATION (id_mission);

CREATE INDEX idx_satellite_statut
    ON SATELLITE (statut);

CREATE INDEX idx_satellite_statut_orbite
    ON SATELLITE (statut, id_orbite);

CREATE INDEX idx_orbite_type
    ON ORBITE (type_orbite, id_orbite);

CREATE INDEX idx_fenetre_mois
    ON FENETRE_COM (TRUNC(datetime_debut, 'MM'));

CREATE INDEX idx_affect_station_code
    ON AFFECTATION_STATION (code_station);

--------------------------------------------------
-- Ex. 18 - EXPLAIN PLAN
-- Requete de reporting mensuel sur 4 tables
--------------------------------------------------
EXPLAIN PLAN SET STATEMENT_ID = 'L4D_EX18' FOR
SELECT
    TRUNC(f.datetime_debut, 'MM') AS mois_reference,
    c.nom_centre,
    s.format_cubesat,
    COUNT(*) AS nb_fenetres,
    SUM(f.volume_donnees) AS volume_total
FROM FENETRE_COM f
JOIN SATELLITE s
    ON s.id_satellite = f.id_satellite
JOIN AFFECTATION_STATION af
    ON af.code_station = f.code_station
JOIN CENTRE_CONTROLE c
    ON c.id_centre = af.id_centre
WHERE f.statut = 'Réalisée'
  AND TRUNC(f.datetime_debut, 'MM') = DATE '2024-01-01'
GROUP BY
    TRUNC(f.datetime_debut, 'MM'),
    c.nom_centre,
    s.format_cubesat;

SELECT plan_table_output
FROM TABLE(DBMS_XPLAN.DISPLAY(NULL, 'L4D_EX18', 'BASIC +PREDICATE +ALIAS'));

-- Lecture attendue :
-- Sur un petit jeu de donnees, Oracle peut garder du TABLE ACCESS FULL.
-- Sur un volume plus grand, les index sur le mois, la station et le satellite
-- deviennent pertinents pour limiter les scans complets.

--------------------------------------------------
-- Ex. 19 - Impact d'un index invisible
--------------------------------------------------
ALTER SESSION SET optimizer_use_invisible_indexes = FALSE;

ALTER INDEX idx_satellite_statut INVISIBLE;

EXPLAIN PLAN SET STATEMENT_ID = 'L4D_EX19_INV' FOR
SELECT
    s.id_satellite,
    s.nom_satellite,
    s.statut
FROM SATELLITE s
WHERE s.statut = 'Opérationnel';

SELECT plan_table_output
FROM TABLE(DBMS_XPLAN.DISPLAY(NULL, 'L4D_EX19_INV', 'BASIC +PREDICATE +ALIAS'));

ALTER INDEX idx_satellite_statut VISIBLE;

EXPLAIN PLAN SET STATEMENT_ID = 'L4D_EX19_VIS' FOR
SELECT
    s.id_satellite,
    s.nom_satellite,
    s.statut
FROM SATELLITE s
WHERE s.statut = 'Opérationnel';

SELECT plan_table_output
FROM TABLE(DBMS_XPLAN.DISPLAY(NULL, 'L4D_EX19_VIS', 'BASIC +PREDICATE +ALIAS'));

-- Lecture attendue :
-- Invisible : Oracle ignore idx_satellite_statut
-- Visible   : Oracle peut de nouveau s'appuyer dessus
-- Sur un tres petit jeu de donnees, le plan peut rester proche
-- car un FULL SCAN peut couter moins cher qu'un acces indexe.

--------------------------------------------------
-- Rapport de pilotage integral
-- Requete de synthese avec CTE, analytique et vue materialisee
--------------------------------------------------
WITH volumes_centres AS (
    SELECT
        mois_reference,
        id_centre,
        nom_centre,
        SUM(volume_total_donnees) AS volume_total
    FROM mv_volumes_mensuels
    GROUP BY
        mois_reference,
        id_centre,
        nom_centre
),
classement AS (
    SELECT
        vc.mois_reference,
        vc.id_centre,
        vc.nom_centre,
        vc.volume_total,
        RANK() OVER (PARTITION BY vc.mois_reference ORDER BY vc.volume_total DESC) AS rang_centre,
        ROUND(vc.volume_total * 100 / SUM(vc.volume_total) OVER (PARTITION BY vc.mois_reference),2) AS part_volume_pct,
        LAG(vc.volume_total) OVER (PARTITION BY vc.id_centre ORDER BY vc.mois_reference) AS volume_mois_precedent
    FROM volumes_centres vc
),
satellites_centres AS (
    SELECT DISTINCT
        TRUNC(f.datetime_debut, 'MM') AS mois_reference,
        c.id_centre,
        s.id_satellite,
        s.statut
    FROM FENETRE_COM f
    JOIN SATELLITE s
        ON s.id_satellite = f.id_satellite
    JOIN AFFECTATION_STATION af
        ON af.code_station = f.code_station
    JOIN CENTRE_CONTROLE c
        ON c.id_centre = af.id_centre
    WHERE f.statut = 'Réalisée'
),
statuts_agreges AS (
    SELECT
        mois_reference,
        id_centre,
        LISTAGG(id_satellite || ' (' || statut || ')',', ') WITHIN GROUP (ORDER BY id_satellite) AS satellites_rattaches
    FROM satellites_centres
    GROUP BY
        mois_reference,
        id_centre
)
SELECT
    TO_CHAR(c.mois_reference, 'YYYY-MM') AS mois_reference,
    c.rang_centre,
    c.nom_centre,
    c.volume_total,
    c.part_volume_pct,
    c.volume_mois_precedent,
    ROUND(c.volume_total - NVL(c.volume_mois_precedent, 0),2) AS evolution_mois_precedent,
    sa.satellites_rattaches
FROM classement c
LEFT JOIN statuts_agreges sa
    ON sa.mois_reference = c.mois_reference
   AND sa.id_centre = c.id_centre
ORDER BY c.mois_reference, c.rang_centre, c.id_centre;

-- Resultat attendu sur le jeu courant :
-- 2024-01 / centre 1 / 3820 / 100%
-- satellites rattaches :
-- SAT-001 (Opérationnel), SAT-002 (Opérationnel), SAT-003 (Opérationnel)

COMMIT;
