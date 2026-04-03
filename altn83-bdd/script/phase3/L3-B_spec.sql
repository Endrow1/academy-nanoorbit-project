--------------------------------------------------
-- PACKAGE SPEC : PKG_NANOORBIT
--------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_nanoorbit IS

    --------------------------------------------------
    -- TYPE PUBLIC
    --------------------------------------------------
    TYPE t_stats_satellite IS RECORD (
        nb_fenetres NUMBER,
        volume_total NUMBER,
        duree_moy_secondes NUMBER
    );

    --------------------------------------------------
    -- CONSTANTES MÉTIER
    --------------------------------------------------
    c_statut_min_fenetre CONSTANT VARCHAR2(30) := 'Opérationnel';
    c_duree_max_fenetre CONSTANT NUMBER := 900;
    c_seuil_revision CONSTANT NUMBER := 50;

    --------------------------------------------------
    -- PROCÉDURES
    --------------------------------------------------
    PROCEDURE planifier_fenetre(
        p_id_satellite IN VARCHAR2,
        p_code_station IN VARCHAR2,
        p_datetime_debut IN TIMESTAMP,
        p_duree IN NUMBER,
        p_id_fenetre OUT NUMBER
    );

    PROCEDURE cloturer_fenetre(
            p_id_fenetre IN NUMBER,
            p_volume_donnees IN NUMBER
        );

    PROCEDURE affecter_satellite_mission(
            p_id_satellite IN VARCHAR2,
            p_id_mission IN VARCHAR2,
            p_role IN VARCHAR2
        );

    PROCEDURE mettre_en_revision(
            p_id_satellite IN VARCHAR2
        );

    --------------------------------------------------
    -- FONCTIONS
    --------------------------------------------------
    FUNCTION calculer_volume_theorique(
        p_id_fenetre IN NUMBER
    ) RETURN NUMBER;

    FUNCTION statut_constellation
        RETURN VARCHAR2;

    FUNCTION stats_satellite(
            p_id_satellite IN VARCHAR2
        ) RETURN t_stats_satellite;

END pkg_nanoorbit;
/
SHOW ERRORS;