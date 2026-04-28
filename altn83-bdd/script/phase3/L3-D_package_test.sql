--------------------------------------------------
-- TEST GLOBAL PACKAGE : PKG_NANOORBIT
--------------------------------------------------
SET SERVEROUTPUT ON;

DECLARE
    v_id_fenetre     NUMBER;
    v_id_fenetre2    NUMBER;
    v_stats          pkg_nanoorbit.t_stats_satellite;
    v_resume         VARCHAR2(200);
    v_volume         NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('       TEST GLOBAL PACKAGE              ');
    DBMS_OUTPUT.PUT_LINE('========================================');

    --------------------------------------------------
    -- 1. Planifier fenêtre (cas nominal)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 1. PLANIFIER FENETRE (nominal) ---');

    pkg_nanoorbit.planifier_fenetre(
            'SAT-001',
            'GS-KIR-01',
            SYSTIMESTAMP,
            300,
            v_id_fenetre
    );

    DBMS_OUTPUT.PUT_LINE('Fenêtre créée ID = ' || v_id_fenetre);

    --------------------------------------------------
    -- 1b. Planifier fenêtre (durée trop grande → erreur attendue)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 1b. PLANIFIER FENETRE (duree > 900 → erreur) ---');
    BEGIN
        pkg_nanoorbit.planifier_fenetre(
                'SAT-001',
                'GS-KIR-01',
                SYSTIMESTAMP,
                9999,           -- dépasse c_duree_max_fenetre = 900
                v_id_fenetre2
        );
        DBMS_OUTPUT.PUT_LINE('ERREUR : exception non levée !');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Exception attendue : ' || SQLERRM);
    END;

    --------------------------------------------------
    -- 1c. Planifier fenêtre (satellite non opérationnel → erreur attendue)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 1c. PLANIFIER FENETRE (satellite non opérationnel → erreur) ---');
    BEGIN
        pkg_nanoorbit.planifier_fenetre(
                'SAT-004',      -- satellite supposé "En veille" ou "En révision"
                'GS-KIR-01',
                SYSTIMESTAMP,
                300,
                v_id_fenetre2
        );
        DBMS_OUTPUT.PUT_LINE('ERREUR : exception non levée !');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Exception attendue : ' || SQLERRM);
    END;

    --------------------------------------------------
    -- 2. Clôturer fenêtre (cas nominal)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 2. CLOTURER FENETRE (nominal) ---');

    pkg_nanoorbit.cloturer_fenetre(
            v_id_fenetre,
            1200
    );

    DBMS_OUTPUT.PUT_LINE('Fenêtre ' || v_id_fenetre || ' clôturée avec volume = 1200');

    --------------------------------------------------
    -- 2b. Clôturer fenêtre inexistante (→ erreur attendue)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 2b. CLOTURER FENETRE (id inexistant → erreur) ---');
    BEGIN
        pkg_nanoorbit.cloturer_fenetre(
                -999,           -- id qui n'existe pas
                500
        );
        DBMS_OUTPUT.PUT_LINE('ERREUR : exception non levée !');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Exception attendue : ' || SQLERRM);
    END;

    --------------------------------------------------
    -- 3. Affectation mission (cas nominal)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 3. AFFECTER SATELLITE MISSION (nominal) ---');

    pkg_nanoorbit.affecter_satellite_mission(
            'SAT-004',
            'MSN-ARC-2023',
            'Satellite de relais'
    );

    DBMS_OUTPUT.PUT_LINE('SAT-004 affecté à MSN-ARC-2023 OK');

    --------------------------------------------------
    -- 4. Mettre en révision (cas nominal)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 4. METTRE EN REVISION (nominal) ---');

    pkg_nanoorbit.mettre_en_revision('SAT-002');

    DBMS_OUTPUT.PUT_LINE('SAT-002 mis en veille OK');

    --------------------------------------------------
    -- 4b. Mettre en révision (satellite inexistant → erreur attendue)
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 4b. METTRE EN REVISION (id inexistant → erreur) ---');
    BEGIN
        pkg_nanoorbit.mettre_en_revision('SAT-999');
        DBMS_OUTPUT.PUT_LINE('ERREUR : exception non levée !');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Exception attendue : ' || SQLERRM);
    END;

    --------------------------------------------------
    -- 5. Volume théorique
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 5. CALCULER VOLUME THEORIQUE ---');

    v_volume := pkg_nanoorbit.calculer_volume_theorique(v_id_fenetre);

    DBMS_OUTPUT.PUT_LINE('Volume théorique fenêtre ' || v_id_fenetre || ' = ' || v_volume);
    -- Attendu : duree (300) * debit_max station GS-KIR-01

    --------------------------------------------------
    -- 6. Stats satellite
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 6. STATS SATELLITE (SAT-001) ---');

    v_stats := pkg_nanoorbit.stats_satellite('SAT-001');

    DBMS_OUTPUT.PUT_LINE('Nb fenêtres      : ' || v_stats.nb_fenetres);
    DBMS_OUTPUT.PUT_LINE('Volume total     : ' || v_stats.volume_total);
    DBMS_OUTPUT.PUT_LINE('Durée moyenne    : ' || v_stats.duree_moy_secondes);

    --------------------------------------------------
    -- 6b. Stats satellite sans historique
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 6b. STATS SATELLITE sans fenetre (SAT-005) ---');

    v_stats := pkg_nanoorbit.stats_satellite('SAT-005');

    DBMS_OUTPUT.PUT_LINE('Nb fenêtres      : ' || v_stats.nb_fenetres);   -- attendu : 0
    DBMS_OUTPUT.PUT_LINE('Volume total     : ' || v_stats.volume_total);   -- attendu : 0
    DBMS_OUTPUT.PUT_LINE('Durée moyenne    : ' || v_stats.duree_moy_secondes); -- attendu : 0

    --------------------------------------------------
    -- 7. Statut constellation
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('--- 7. STATUT CONSTELLATION ---');

    v_resume := pkg_nanoorbit.statut_constellation;

    DBMS_OUTPUT.PUT_LINE('Résumé : ' || v_resume);

    --------------------------------------------------
    -- ROLLBACK GLOBAL
    --------------------------------------------------
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('========================================');
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Rollback effectué — aucune donnée persistée');
    DBMS_OUTPUT.PUT_LINE('========================================');

END;
/