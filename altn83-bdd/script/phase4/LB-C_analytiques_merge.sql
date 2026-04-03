--------------------------------------------------
-- NANOORBIT - PHASE 4
-- L4-C - ANALYTIQUES ET MERGE
--------------------------------------------------

--------------------------------------------------
-- Ex. 11 - ROW_NUMBER / RANK / DENSE_RANK
-- Classement des satellites par volume telecharge
--------------------------------------------------
WITH volumes_satellites AS (
    SELECT
        s.id_satellite,
        s.nom_satellite,
        o.type_orbite,
        NVL(SUM(f.volume_donnees), 0) AS volume_total
    FROM SATELLITE s
    JOIN ORBITE o
        ON o.id_orbite = s.id_orbite
    LEFT JOIN FENETRE_COM f
        ON f.id_satellite = s.id_satellite
        AND f.statut = 'Réalisée'
    GROUP BY
        s.id_satellite,
        s.nom_satellite,
        o.type_orbite
)
SELECT
    id_satellite,
    nom_satellite,
    type_orbite,
    volume_total,
    ROW_NUMBER() OVER (ORDER BY volume_total DESC, id_satellite) AS row_number_global,
    RANK() OVER (ORDER BY volume_total DESC) AS rank_global,
    DENSE_RANK() OVER (PARTITION BY type_orbite ORDER BY volume_total DESC) AS dense_rank_par_orbite
FROM volumes_satellites
ORDER BY volume_total DESC, id_satellite;

-- Resultat attendu :
-- SAT-003 en tete, puis SAT-001, puis SAT-002

--------------------------------------------------
-- Ex. 12 - LAG / LEAD
-- Comparaison du volume entre deux fenetres d'une meme station
--------------------------------------------------
WITH fenetres_realisees AS (
    SELECT
        st.code_station,
        st.nom_station,
        f.id_fenetre,
        f.datetime_debut,
        f.volume_donnees
    FROM FENETRE_COM f
        JOIN STATION_SOL st
        ON st.code_station = f.code_station
    WHERE f.statut = 'Réalisée'
),
     avec_lag_lead AS (
         SELECT
             code_station,
             nom_station,
             id_fenetre,
             datetime_debut,
             volume_donnees,
             LAG(volume_donnees)  OVER (PARTITION BY code_station ORDER BY datetime_debut) AS volume_precedent,
             LEAD(volume_donnees) OVER (PARTITION BY code_station ORDER BY datetime_debut) AS volume_suivant
         FROM fenetres_realisees
     )
SELECT
    code_station,
    nom_station,
    id_fenetre,
    datetime_debut,
    volume_donnees,
    volume_precedent,
    volume_suivant,
    ROUND((volume_donnees - volume_precedent) * 100 / NULLIF(volume_precedent, 0),2
    ) AS evolution_pct
FROM avec_lag_lead
ORDER BY code_station, datetime_debut;

-- Resultat attendu :
-- A Kiruna, la 2e fenetre compare 1680 a 1250

--------------------------------------------------
-- Ex. 13 - SUM OVER
-- Volumes cumules par centre avec moyenne mobile sur 3 fenetres
--------------------------------------------------
WITH volumes_centres AS (
    SELECT
        c.id_centre,
        c.nom_centre,
        f.id_fenetre,
        f.datetime_debut,
        f.volume_donnees
    FROM FENETRE_COM f
    JOIN AFFECTATION_STATION af
        ON af.code_station = f.code_station
    JOIN CENTRE_CONTROLE c
        ON c.id_centre = af.id_centre
    WHERE f.statut = 'Réalisée'
)
SELECT
    id_centre,
    nom_centre,
    id_fenetre,
    datetime_debut,
    volume_donnees,
    SUM(volume_donnees) OVER (
        PARTITION BY id_centre
        ORDER BY datetime_debut
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS volume_cumule,
    ROUND(
        AVG(volume_donnees) OVER (
            PARTITION BY id_centre
            ORDER BY datetime_debut
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
        ),
        2
    ) AS moyenne_mobile_3
FROM volumes_centres
ORDER BY id_centre, datetime_debut;

-- Resultat attendu :
-- Pour le centre 1 : cumul 1250 -> 2140 -> 3820

--------------------------------------------------
-- Ex. 14 - Tableau de bord constellation
-- Rang, part de volume, cumul et comparaison a la moyenne
--------------------------------------------------
WITH volumes_mensuels AS (
    SELECT
        TRUNC(f.datetime_debut, 'MM') AS mois_reference,
        s.id_satellite,
        s.nom_satellite,
        SUM(f.volume_donnees) AS volume_mensuel
    FROM FENETRE_COM f
    JOIN SATELLITE s
        ON s.id_satellite = f.id_satellite
    WHERE f.statut = 'Réalisée'
    GROUP BY
        TRUNC(f.datetime_debut, 'MM'),
        s.id_satellite,
        s.nom_satellite
)
SELECT
    TO_CHAR(mois_reference, 'YYYY-MM') AS mois_reference,
    id_satellite,
    nom_satellite,
    volume_mensuel,
    RANK() OVER (PARTITION BY mois_reference ORDER BY volume_mensuel DESC) AS rang_satellite,
    ROUND(volume_mensuel * 100 / SUM(volume_mensuel) OVER (PARTITION BY mois_reference),2) AS part_volume_pct,
    SUM(volume_mensuel) OVER (PARTITION BY mois_reference ORDER BY volume_mensuel DESC, id_satellite ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumul_volume,
    ROUND(AVG(volume_mensuel) OVER (PARTITION BY mois_reference), 2) AS moyenne_mois,
    ROUND(volume_mensuel - AVG(volume_mensuel) OVER (PARTITION BY mois_reference),2) AS ecart_moyenne
FROM volumes_mensuels
ORDER BY mois_reference, rang_satellite, id_satellite;

-- Resultat attendu :
-- Tableau mensuel sur 2024-01 avec 3 satellites classes

--------------------------------------------------
-- Ex. 15 - MERGE INTO
-- Synchronisation des statuts satellites depuis un lot IoT
-- ROLLBACK final pour laisser le jeu de reference intact
--------------------------------------------------
SAVEPOINT ex15_merge_satellite;

MERGE INTO SATELLITE s
USING (
    SELECT
        'SAT-002' AS id_satellite,
        'NanoOrbit-Beta' AS nom_satellite,
        'En veille' AS statut,
        '2' AS id_orbite,
        DATE '2022-03-15' AS date_lancement,
        1.30 AS masse,
        '3U' AS format_cubesat,
        60 AS duree_vie_prevue,
        18 AS capacite_batterie
    FROM dual
    UNION ALL
    SELECT
        'SAT-006',
        'NanoOrbit-Zeta',
        'Opérationnel',
        '3',
        DATE '2024-09-01',
        1.10,
        '1U',
        48,
        15
    FROM dual
) src
ON (s.id_satellite = src.id_satellite)
WHEN MATCHED THEN
    UPDATE SET
        s.statut = src.statut,
        s.id_orbite = src.id_orbite
WHEN NOT MATCHED THEN
    INSERT (
        id_satellite,
        nom_satellite,
        date_lancement,
        masse,
        format_cubesat,
        statut,
        duree_vie_prevue,
        capacite_batterie,
        id_orbite
    )
    VALUES (
        src.id_satellite,
        src.nom_satellite,
        src.date_lancement,
        src.masse,
        src.format_cubesat,
        'En veille',
        src.duree_vie_prevue,
        src.capacite_batterie,
        src.id_orbite
    );

SELECT
    id_satellite,
    nom_satellite,
    statut,
    id_orbite
FROM SATELLITE
WHERE id_satellite IN ('SAT-002', 'SAT-006')
ORDER BY id_satellite;

ROLLBACK TO ex15_merge_satellite;

-- Resultat attendu :
-- SAT-002 mis a jour
-- SAT-006 insere avec statut "En veille"

--------------------------------------------------
-- Ex. 16 - MERGE INTO
-- Synchronisation des affectations de stations
-- ROLLBACK final pour laisser le jeu de reference intact
--------------------------------------------------
SAVEPOINT ex16_merge_affectation;

MERGE INTO AFFECTATION_STATION a
USING (
    SELECT
        '1' AS id_centre,
        'GS-TLS-01' AS code_station,
        DATE '2024-02-01' AS date_affectation
    FROM dual
    UNION ALL
    SELECT
        '2',
        'GS-SGP-01',
        DATE '2024-02-15'
    FROM dual
) src
ON (
    a.id_centre = src.id_centre
    AND a.code_station = src.code_station
)
WHEN MATCHED THEN
    UPDATE SET
        a.date_affectation = src.date_affectation
WHEN NOT MATCHED THEN
    INSERT (
        id_centre,
        code_station,
        date_affectation
    )
    VALUES (
        src.id_centre,
        src.code_station,
        src.date_affectation
    );

SELECT
    id_centre,
    code_station,
    date_affectation
FROM AFFECTATION_STATION
WHERE (id_centre = '1' AND code_station = 'GS-TLS-01')
   OR (id_centre = '2' AND code_station = 'GS-SGP-01')
ORDER BY id_centre, code_station;

ROLLBACK TO ex16_merge_affectation;

-- Resultat attendu :
-- (1, GS-TLS-01) date mise à jour
-- (2, GS-SGP-01) nouvelle affectation creee

COMMIT;