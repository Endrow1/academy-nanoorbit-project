--------------------------------------------------
-- V1 - v_satellites_operationnels
--------------------------------------------------
CREATE OR REPLACE VIEW v_satellites_operationnels AS
SELECT
    s.id_satellite,
    s.nom_satellite,
    s.statut,
    o.type_orbite || ' / ' || NVL(o.zone_couverture, 'Zone non renseignee') AS nom_orbite,
    COUNT(e.ref_instrument) AS nb_instruments_embarques,
    CASE
        WHEN s.capacite_batterie >= 60 THEN 'Confort'
        WHEN s.capacite_batterie >= 30 THEN 'Surveillance'
        ELSE 'Critique'
        END AS statut_batterie
FROM SATELLITE s
         JOIN ORBITE o
              ON o.id_orbite = s.id_orbite
         LEFT JOIN EMBARQUEMENT e
                   ON e.id_satellite = s.id_satellite
WHERE s.statut = 'Opérationnel'
GROUP BY
    s.id_satellite,
    s.nom_satellite,
    s.statut,
    o.type_orbite,
    o.zone_couverture,
    s.capacite_batterie;

--------------------------------------------------
-- V2 - v_fenetres_detail
--------------------------------------------------
CREATE OR REPLACE VIEW v_fenetres_detail AS
SELECT
    f.id_fenetre,
    f.datetime_debut,
    TRUNC(f.datetime_debut) AS date_passage,
    TO_CHAR(f.datetime_debut, 'YYYY-MM') AS mois_reference,
    f.id_satellite,
    s.nom_satellite,
    f.code_station,
    st.nom_station,
    c.id_centre,
    NVL(c.nom_centre, 'Centre non affecte') AS nom_centre,
    f.duree AS duree_secondes,
    LPAD(TRUNC(f.duree / 3600), 2, '0')
        || ':' || LPAD(TRUNC(MOD(f.duree, 3600) / 60), 2, '0')
        || ':' || LPAD(MOD(f.duree, 60), 2, '0') AS duree_formatee,
    f.elevation_max,
    f.volume_donnees,
    f.statut
FROM FENETRE_COM f
         JOIN SATELLITE s
              ON s.id_satellite = f.id_satellite
         JOIN STATION_SOL st
              ON st.code_station = f.code_station
         LEFT JOIN AFFECTATION_STATION af
                   ON af.code_station = st.code_station
         LEFT JOIN CENTRE_CONTROLE c
                   ON c.id_centre = af.id_centre;


--------------------------------------------------
-- V3 - v_stats_missions
--------------------------------------------------
CREATE OR REPLACE VIEW v_stats_missions AS
SELECT
    m.id_mission,
    m.nom_mission,
    m.statut_mission,
    COUNT(DISTINCT p.id_satellite) AS nb_satellites,
    (
        SELECT LISTAGG(type_orbite, ', ') WITHIN GROUP (ORDER BY type_orbite)
        FROM (
            SELECT DISTINCT o2.type_orbite
            FROM PARTICIPATION p2
                  JOIN SATELLITE s2 ON s2.id_satellite = p2.id_satellite
                  JOIN ORBITE o2 ON o2.id_orbite = s2.id_orbite
            WHERE p2.id_mission = m.id_mission
        )
    ) AS types_orbites_representes,
    NVL(SUM(
        CASE
            WHEN f.statut = 'Réalisée'
                AND f.datetime_debut >= CAST(m.date_debut AS TIMESTAMP)
                AND (
                     m.date_fin IS NULL
                         OR f.datetime_debut < CAST(m.date_fin + 1 AS TIMESTAMP)
                     )
                THEN f.volume_donnees
            END
        ), 0) AS volume_total_telecharge
FROM MISSION m
    LEFT JOIN PARTICIPATION p ON p.id_mission = m.id_mission
    LEFT JOIN SATELLITE s ON s.id_satellite = p.id_satellite
    LEFT JOIN ORBITE o ON o.id_orbite = s.id_orbite
    LEFT JOIN FENETRE_COM f ON f.id_satellite = s.id_satellite
GROUP BY
    m.id_mission,
    m.nom_mission,
    m.statut_mission,
    m.date_debut,
    m.date_fin;


--------------------------------------------------
-- V4 - mv_volumes_mensuels
--------------------------------------------------
CREATE MATERIALIZED VIEW mv_volumes_mensuels
            BUILD IMMEDIATE
    REFRESH COMPLETE ON DEMAND
AS
SELECT
    TRUNC(f.datetime_debut, 'MM') AS mois_reference,
    c.id_centre,
    c.nom_centre,
    s.format_cubesat AS type_satellite,
    COUNT(*) AS nb_fenetres_realisees,
    SUM(f.volume_donnees) AS volume_total_donnees,
    ROUND(AVG(f.volume_donnees), 2) AS volume_moyen_donnees
FROM FENETRE_COM f
         JOIN SATELLITE s
              ON s.id_satellite = f.id_satellite
         JOIN AFFECTATION_STATION af
              ON af.code_station = f.code_station
         JOIN CENTRE_CONTROLE c
              ON c.id_centre = af.id_centre
WHERE f.statut = 'Réalisée'
GROUP BY
    TRUNC(f.datetime_debut, 'MM'),
    c.id_centre,
    c.nom_centre,
    s.format_cubesat;

COMMIT;