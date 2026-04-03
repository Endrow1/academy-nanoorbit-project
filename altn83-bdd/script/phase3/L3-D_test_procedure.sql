--------------------------------------------------
-- NANOORBIT - PHASE 3
-- SCRIPT DE TEST (PROCÉDURES & FONCTIONS)
--------------------------------------------------

SET SERVEROUTPUT ON;

--------------------------------------------------
-- TEST EX.13 : ajouter_fenetre
--------------------------------------------------

BEGIN
    DBMS_OUTPUT.PUT_LINE('Test ajouter_fenetre (cas OK)');
    ajouter_fenetre('SAT-001', 'GS-TLS-01');
END;
/
-- Résultat attendu :
-- Insertion valide

BEGIN
    DBMS_OUTPUT.PUT_LINE('Test ajouter_fenetre (satellite invalide)');
    ajouter_fenetre('SAT-005', 'GS-TLS-01'); -- désorbité
END;
/
-- Résultat attendu :
-- ORA-20001: Satellite non opérationnel

BEGIN
    DBMS_OUTPUT.PUT_LINE('Test ajouter_fenetre (station inactive)');
    ajouter_fenetre('SAT-001', 'GS-SGP-01'); -- maintenance
END;
/
-- Résultat attendu :
-- ORA-20002: Station inactive


--------------------------------------------------
-- TEST EX.14 : afficher_statut_satellite
--------------------------------------------------

BEGIN
    DBMS_OUTPUT.PUT_LINE('Test afficher_statut_satellite');
    afficher_statut_satellite('SAT-001');
END;
/
-- Résultat attendu :
-- Opérationnel | SSO | Caméra optique
-- Opérationnel | SSO | Infrarouge


--------------------------------------------------
-- TEST EX.15 : mettre_a_jour_statut
--------------------------------------------------

DECLARE
    v_ancien_statut VARCHAR2(30);
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test mise à jour statut');

    mettre_a_jour_statut('SAT-001', 'En veille', v_ancien_statut);

    DBMS_OUTPUT.PUT_LINE('Ancien statut : ' || v_ancien_statut);

        -- Vérification
    FOR rec IN (SELECT statut FROM SATELLITE WHERE id_satellite = 'SAT-001')
        LOOP
            DBMS_OUTPUT.PUT_LINE('Nouveau statut : ' || rec.statut);
    END LOOP;

    ROLLBACK;
END;
/
-- Résultat attendu :
-- Ancien statut : Opérationnel
-- Nouveau statut : En veille


--------------------------------------------------
-- TEST EX.16 : calculer_volume_session
--------------------------------------------------

DECLARE
    v_volume NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test calculer_volume_session');

-- id_fenetre = 1 (à adapter si besoin)
v_volume := calculer_volume_session(1);

DBMS_OUTPUT.PUT_LINE('Volume calculé : ' || v_volume);
END;
/
-- Résultat attendu :
-- Volume calculé : (duree * debit)
-- ex: 420 * 400 = 168000


--------------------------------------------------
-- TEST : appel multiple
--------------------------------------------------

BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST GLOBAL ---');

    afficher_statut_satellite('SAT-003');

    ajouter_fenetre('SAT-003', 'GS-KIR-01');

END;
/
-- Résultat attendu :
-- Infos SAT-003 + insertion valide


