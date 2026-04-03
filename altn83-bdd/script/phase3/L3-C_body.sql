--------------------------------------------------
-- PACKAGE BODY : PKG_NANOORBIT
--------------------------------------------------
CREATE OR REPLACE PACKAGE BODY pkg_nanoorbit IS

--------------------------------------------------
-- PLANIFIER FENETRE
--------------------------------------------------
PROCEDURE planifier_fenetre(
    p_id_satellite IN VARCHAR2,
    p_code_station IN VARCHAR2,
    p_datetime_debut IN TIMESTAMP,
    p_duree IN NUMBER,
    p_id_fenetre OUT NUMBER
) IS
    v_statut VARCHAR2(30);
BEGIN
    -- Vérification satellite
    SELECT statut INTO v_statut
    FROM SATELLITE
    WHERE id_satellite = p_id_satellite;

    IF v_statut <> c_statut_min_fenetre THEN
            RAISE_APPLICATION_ERROR(-20010, 'Satellite non opérationnel');
    END IF;

    IF p_duree > c_duree_max_fenetre THEN
            RAISE_APPLICATION_ERROR(-20011, 'Durée trop grande');
    END IF;

    INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max,statut, id_satellite, code_station)
        VALUES (p_datetime_debut, p_duree, 50,
                   'Planifiée', p_id_satellite, p_code_station)
    RETURNING id_fenetre INTO p_id_fenetre;
END;

--------------------------------------------------
-- CLOTURER FENETRE
--------------------------------------------------
PROCEDURE cloturer_fenetre(
    p_id_fenetre IN NUMBER,
    p_volume_donnees IN NUMBER
) IS
BEGIN
    UPDATE FENETRE_COM
    SET statut = 'Réalisée',
        volume_donnees = p_volume_donnees
    WHERE id_fenetre = p_id_fenetre;

    IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20012, 'Fenêtre inexistante');
    END IF;
END;

--------------------------------------------------
-- AFFECTER SATELLITE
--------------------------------------------------
PROCEDURE affecter_satellite_mission(
    p_id_satellite IN VARCHAR2,
    p_id_mission IN VARCHAR2,
    p_role IN VARCHAR2
) IS
BEGIN
    INSERT INTO PARTICIPATION
    VALUES (p_id_satellite, p_id_mission, p_role);
END;

--------------------------------------------------
-- METTRE EN REVISION
--------------------------------------------------
PROCEDURE mettre_en_revision(
    p_id_satellite IN VARCHAR2
) IS
BEGIN
    UPDATE SATELLITE
    SET statut = 'En veille'
    WHERE id_satellite = p_id_satellite;

    IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20013, 'Satellite introuvable');
    END IF;
END;

--------------------------------------------------
-- FONCTION VOLUME THEORIQUE
--------------------------------------------------
FUNCTION calculer_volume_theorique(
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

--------------------------------------------------
-- STATUT CONSTELLATION
--------------------------------------------------
FUNCTION statut_constellation
RETURN VARCHAR2 IS
    v_total NUMBER;
    v_op NUMBER;
    v_missions NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_total FROM SATELLITE;
    SELECT COUNT(*) INTO v_op FROM SATELLITE WHERE statut = 'Opérationnel';
    SELECT COUNT(*) INTO v_missions FROM MISSION WHERE statut_mission = 'Active';

    RETURN v_op || '/' || v_total || ' satellites opérationnels, ' ||
               v_missions || ' missions actives';
END;

--------------------------------------------------
-- STATS SATELLITE
--------------------------------------------------
FUNCTION stats_satellite(
    p_id_satellite IN VARCHAR2
) RETURN t_stats_satellite IS
    v_stats t_stats_satellite;
BEGIN
    SELECT COUNT(*),
           NVL(SUM(volume_donnees),0),
           NVL(AVG(duree),0)
    INTO v_stats.nb_fenetres,
        v_stats.volume_total,
        v_stats.duree_moy_secondes
    FROM FENETRE_COM
    WHERE id_satellite = p_id_satellite;

    RETURN v_stats;
END;

END pkg_nanoorbit;
/
SHOW ERRORS;