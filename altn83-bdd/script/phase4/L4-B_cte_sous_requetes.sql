--------------------------------------------------
-- NANOORBIT - PHASE 4
-- L4-B - CTE ET SOUS-REQUETES
--------------------------------------------------

--------------------------------------------------
-- Ex. 5 - CTE simple
-- Top 3 des satellites ayant telecharge le plus de donnees
--------------------------------------------------
WITH stats_satellite AS (
    SELECT
        f.id_satellite,
        COUNT(*) AS nb_fenetres_realisees,
        SUM(f.volume_donnees) AS volume_total,
        ROUND(AVG(f.volume_donnees), 2) AS volume_moyen_par_passage
    FROM FENETRE_COM f
    WHERE f.statut = 'Réalisée'
    GROUP BY f.id_satellite
)
SELECT
    s.id_satellite,
    s.nom_satellite,
    ss.nb_fenetres_realisees,
    ss.volume_total,
    ss.volume_moyen_par_passage
FROM stats_satellite ss
JOIN SATELLITE s
    ON s.id_satellite = ss.id_satellite
ORDER BY ss.volume_total DESC, s.id_satellite
FETCH FIRST 3 ROWS ONLY;

-- Resultat attendu :
-- SAT-003 (1680), SAT-001 (1250), SAT-002 (890)

--------------------------------------------------
-- Ex. 6 - CTE multiples
-- Analyse comparative par centre de controle sur le mois le plus recent
--------------------------------------------------
WITH mois_reference AS (
    SELECT TRUNC(MAX(datetime_debut), 'MM') AS mois_ref
    FROM FENETRE_COM
),
fenetres_mois AS (
    SELECT
        af.id_centre,
        c.nom_centre,
        f.code_station,
        st.nom_station,
        f.volume_donnees
    FROM FENETRE_COM f
    JOIN mois_reference mr
        ON TRUNC(f.datetime_debut, 'MM') = mr.mois_ref
    JOIN STATION_SOL st
        ON st.code_station = f.code_station
    JOIN AFFECTATION_STATION af
        ON af.code_station = f.code_station
    JOIN CENTRE_CONTROLE c
        ON c.id_centre = af.id_centre
),
stats_centres AS (
    SELECT
        id_centre,
        nom_centre,
        COUNT(*) AS nb_fenetres_mois,
        NVL(SUM(volume_donnees), 0) AS volume_total_mois
    FROM fenetres_mois
    GROUP BY
        id_centre,
        nom_centre
),
stations_classees AS (
    SELECT
        id_centre,
        nom_station,
        COUNT(*) AS nb_fenetres_station,
        ROW_NUMBER() OVER (
            PARTITION BY id_centre
            ORDER BY COUNT(*) DESC, nom_station
        ) AS rn
    FROM fenetres_mois
    GROUP BY
        id_centre,
        nom_station
)
SELECT
    sc.id_centre,
    sc.nom_centre,
    sc.nb_fenetres_mois,
    sc.volume_total_mois,
    stc.nom_station AS station_plus_active,
    stc.nb_fenetres_station
FROM stats_centres sc
LEFT JOIN stations_classees stc
    ON stc.id_centre = sc.id_centre
   AND stc.rn = 1
ORDER BY sc.id_centre;

-- Resultat attendu :
-- Centre 1 uniquement sur le mois 2024-01
-- 5 fenetres, volume 3820, station la plus active = Toulouse Ground Station

--------------------------------------------------
-- Ex. 7 - Hierarchie Centre -> Station -> Fenetres recentes
-- Version Oracle lisible avec CONNECT BY
--------------------------------------------------
WITH fenetres AS (
    SELECT
        f.id_fenetre,
        f.code_station,
        f.datetime_debut,
        f.statut,
        f.volume_donnees
    FROM FENETRE_COM f
),
noeuds AS (
    ---------- ANCRE : niveau 1 — Centres de contrôle actifs ----------
    SELECT
        'C:' || c.id_centre AS node_id,
        CAST(NULL AS VARCHAR2(30)) AS parent_id,
        c.nom_centre AS libelle,
        'CENTRE' AS type_noeud,
        1 AS ordre_niveau_1,
        0 AS ordre_niveau_2,
        0 AS ordre_niveau_3,
        CAST(NULL AS TIMESTAMP) AS date_evt,
        CAST(NULL AS NUMBER) AS volume_donnees
    FROM CENTRE_CONTROLE c
    UNION ALL
    ---------- RÉCURSION : niveau 2 — Stations rattachées au centre ----------
    SELECT
        'S:' || st.code_station AS node_id,
        'C:' || af.id_centre AS parent_id,
        st.nom_station || ' (' || st.statut || ')' AS libelle,
        'STATION' AS type_noeud,
        TO_NUMBER(af.id_centre) AS ordre_niveau_1,
        ROW_NUMBER() OVER (
            PARTITION BY af.id_centre
            ORDER BY st.code_station
        ) AS ordre_niveau_2,
        0 AS ordre_niveau_3,
        CAST(NULL AS TIMESTAMP) AS date_evt,
        CAST(NULL AS NUMBER) AS volume_donnees
    FROM AFFECTATION_STATION af
    JOIN STATION_SOL st
        ON st.code_station = af.code_station
    UNION ALL
    ---------- RÉCURSION : niveau 3 — Fenêtres récentes de la station ----------
    SELECT
        'F:' || TO_CHAR(fr.id_fenetre) AS node_id,
        'S:' || fr.code_station AS parent_id,
        'Fenetre #' || fr.id_fenetre
            || ' - ' || TO_CHAR(fr.datetime_debut, 'YYYY-MM-DD HH24:MI')
            || ' - ' || fr.statut AS libelle,
        'FENETRE' AS type_noeud,
        TO_NUMBER(af.id_centre) AS ordre_niveau_1,
        ROW_NUMBER() OVER (
            PARTITION BY fr.code_station
            ORDER BY fr.datetime_debut DESC, fr.id_fenetre DESC
        ) AS ordre_niveau_3,
        fr.id_fenetre AS ordre_niveau_2,
        fr.datetime_debut AS date_evt,
        fr.volume_donnees
    FROM fenetres fr
    JOIN AFFECTATION_STATION af
        ON af.code_station = fr.code_station
)
SELECT
    LPAD(' ', 4 * (LEVEL - 1)) || libelle AS arbre_hierarchique,
    type_noeud,
    date_evt,
    volume_donnees
FROM noeuds
START WITH parent_id IS NULL
CONNECT BY PRIOR node_id = parent_id
ORDER SIBLINGS BY ordre_niveau_1, ordre_niveau_2, ordre_niveau_3;

-- Resultat attendu :
-- Arbre a 3 niveaux avec indentation visuelle :
-- Centre -> Station -> Fenetre recente

--------------------------------------------------
-- Ex. 8 - Sous-requete scalaire
-- Fenetres dont le volume depasse la moyenne generale
--------------------------------------------------
SELECT
    f.id_fenetre,
    f.id_satellite,
    f.code_station,
    f.volume_donnees,
    ROUND(
        f.volume_donnees - (
            SELECT AVG(f2.volume_donnees)
            FROM FENETRE_COM f2
            WHERE f2.statut = 'Réalisée'
        ),
        2
    ) AS ecart_moyenne_generale
FROM FENETRE_COM f
WHERE f.statut = 'Réalisée'
  AND f.volume_donnees > (
        SELECT AVG(f2.volume_donnees)
        FROM FENETRE_COM f2
        WHERE f2.statut = 'Réalisée'
  )
ORDER BY f.volume_donnees DESC, f.id_fenetre;

-- Resultat attendu :
-- La seule fenetre au-dessus de la moyenne est celle de SAT-003 (1680)

--------------------------------------------------
-- Ex. 9 - Sous-requete correlee
-- Derniere fenetre realisee pour chaque satellite
--------------------------------------------------
SELECT
    s.id_satellite,
    s.nom_satellite,
    f.datetime_debut AS derniere_date_realisee,
    f.code_station AS derniere_station,
    f.volume_donnees AS dernier_volume
FROM SATELLITE s
LEFT JOIN FENETRE_COM f
    ON f.id_satellite = s.id_satellite
   AND f.statut = 'Réalisée'
   AND f.id_fenetre = (
        SELECT MAX(f2.id_fenetre) KEEP (
            DENSE_RANK LAST ORDER BY f2.datetime_debut, f2.id_fenetre
        )
        FROM FENETRE_COM f2
        WHERE f2.id_satellite = s.id_satellite
          AND f2.statut = 'Réalisée'
   )
ORDER BY s.id_satellite;

-- Resultat attendu :
-- SAT-001, SAT-002 et SAT-003 ont une derniere fenetre realisee renseignee
-- SAT-004 et SAT-005 restent a NULL

--------------------------------------------------
-- Ex. 10 - EXISTS / NOT EXISTS
-- Satellites sans fenetre realisee
-- et stations sans fenetre sur le trimestre de reference
--------------------------------------------------
WITH trimestre_reference AS (
    SELECT TRUNC(MAX(datetime_debut), 'Q') AS debut_trimestre
    FROM FENETRE_COM
)
SELECT
    'SATELLITE' AS type_objet,
    s.id_satellite AS identifiant,
    s.nom_satellite AS libelle,
    'Aucune fenetre realisee' AS commentaire_metier
FROM SATELLITE s
WHERE NOT EXISTS (
    SELECT 1
    FROM FENETRE_COM f
    WHERE f.id_satellite = s.id_satellite
      AND f.statut = 'Réalisée'
)
UNION ALL
SELECT
    'STATION' AS type_objet,
    st.code_station AS identifiant,
    st.nom_station AS libelle,
    CASE
        WHEN st.statut = 'Maintenance'
            THEN 'Station en maintenance sur le trimestre'
        ELSE 'Aucun passage traite sur le trimestre'
    END AS commentaire_metier
FROM STATION_SOL st
CROSS JOIN trimestre_reference tr
WHERE NOT EXISTS (
    SELECT 1
    FROM FENETRE_COM f
    WHERE f.code_station = st.code_station
      AND f.datetime_debut >= tr.debut_trimestre
      AND f.datetime_debut < ADD_MONTHS(tr.debut_trimestre, 3)
)
ORDER BY type_objet, identifiant;

-- Resultat attendu :
-- Satellites : SAT-004, SAT-005
-- Station : GS-SGP-01
-- Une station peut etre dans cette situation si elle est en maintenance
-- ou si aucun satellite n'a ete visible durant le trimestre.

COMMIT;
