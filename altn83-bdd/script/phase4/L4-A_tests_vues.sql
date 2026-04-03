-- Test V1
SELECT
    id_satellite,
    nom_satellite,
    nom_orbite,
    nb_instruments_embarques,
    statut_batterie
FROM v_satellites_operationnels
ORDER BY id_satellite;

-- Resultat attendu sur le jeu courant :
-- SAT-001 / SAT-002 / SAT-003
-- Nb instruments : 2 / 1 / 2
-- Statut batterie : Critique / Critique / Surveillance


-- Test V2
SELECT
    id_fenetre,
    nom_satellite,
    nom_station,
    nom_centre,
    duree_formatee,
    volume_donnees,
    statut
FROM v_fenetres_detail
ORDER BY id_fenetre;

-- Resultat attendu sur le jeu courant :
-- 5 lignes, avec Kiruna/Toulouse rattachees a NanoOrbit Paris HQ
-- et Singapore Station rattachee a NanoOrbit Singapore.


-- Test V3
SELECT
    id_mission,
    nom_mission,
    nb_satellites,
    types_orbites_representes,
    volume_total_telecharge
FROM v_stats_missions
ORDER BY id_mission;

-- Resultat attendu sur le jeu courant :
-- MSN-ARC-2023 : 3 satellites, orbite SSO, volume 3820
-- MSN-COAST-2024 : 2 satellites, orbite SSO, volume 0
-- MSN-DEF-2022 : 2 satellites, orbites LEO, SSO, volume 0


-- Test V4
SELECT
    TO_CHAR(mois_reference, 'YYYY-MM') AS mois_reference,
    nom_centre,
    type_satellite,
    nb_fenetres_realisees,
    volume_total_donnees,
    volume_moyen_donnees
FROM mv_volumes_mensuels
ORDER BY mois_reference, nom_centre, type_satellite;

-- Resultat attendu sur le jeu courant :
-- 2024-01 / NanoOrbit Paris HQ / 3U / 2 / 2140 / 1070
-- 2024-01 / NanoOrbit Paris HQ / 6U / 1 / 1680 / 1680