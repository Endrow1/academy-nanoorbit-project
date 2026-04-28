--------------------------------------------------
-- TEST GLOBAL PACKAGE
--------------------------------------------------
SET SERVEROUTPUT ON;

DECLARE
    v_id_fenetre NUMBER;
    v_stats pkg_nanoorbit.t_stats_satellite;
    v_resume VARCHAR2(200);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST PACKAGE ---');

    --------------------------------------------------
    -- 1. Planifier fenêtre
    --------------------------------------------------
    pkg_nanoorbit.planifier_fenetre(
        'SAT-001',
        'GS-KIR-01',
        SYSTIMESTAMP,
        300,
        v_id_fenetre
    );

    DBMS_OUTPUT.PUT_LINE('Fenêtre créée ID = ' || v_id_fenetre);

    --------------------------------------------------
    -- 2. Clôturer fenêtre
    --------------------------------------------------
    pkg_nanoorbit.cloturer_fenetre(
        v_id_fenetre,
        1200
    );

    DBMS_OUTPUT.PUT_LINE('Fenêtre clôturée');

    --------------------------------------------------
    -- 3. Affectation mission
    --------------------------------------------------
    pkg_nanoorbit.affecter_satellite_mission(
        'SAT-004',
        'MSN-ARC-2023',
        'Satellite de relais'
    );

    DBMS_OUTPUT.PUT_LINE('Affectation OK');

    --------------------------------------------------
    -- 4. Stats satellite
    --------------------------------------------------
    v_stats := pkg_nanoorbit.stats_satellite('SAT-001');

    DBMS_OUTPUT.PUT_LINE('Nb fenêtres : ' || v_stats.nb_fenetres);
    DBMS_OUTPUT.PUT_LINE('Volume total : ' || v_stats.volume_total);
    DBMS_OUTPUT.PUT_LINE('Durée moyenne : ' || v_stats.duree_moy_secondes);

    --------------------------------------------------
    -- 5. Statut constellation
    --------------------------------------------------
    v_resume := pkg_nanoorbit.statut_constellation;

    DBMS_OUTPUT.PUT_LINE('Résumé : ' || v_resume);

    --------------------------------------------------
    -- ROLLBACK GLOBAL
    --------------------------------------------------
    ROLLBACK;

    DBMS_OUTPUT.PUT_LINE('Rollback effectué (aucune donnée persistée)');

END;
/


--------------------------------------------------
-- FIN SCRIPT DE TEST
--------------------------------------------------

-- --- TEST PACKAGE RESULT ---

-- 1. Planifier fenêtre
-- Fenêtre créée ID = X
-- (X = nouvel ID généré automatiquement)

-- 2. Clôturer fenêtre
-- Fenêtre clôturée

-- 3. Affectation mission
-- Affectation OK

-- 4. Stats satellite (SAT-001)
-- Nb fenêtres : 3
-- Volume total : 2450
-- Durée moyenne : ~366

-- 5. Statut constellation
-- Résumé : 3/5 satellites opérationnels, 2 missions actives
