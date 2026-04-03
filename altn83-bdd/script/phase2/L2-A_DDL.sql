--------------------------------------------------
-- NANOORBIT - PHASE 2
--------------------------------------------------

--------------------------------------------------
-- 1. TABLE ORBITE
--------------------------------------------------
CREATE TABLE ORBITE (
                        id_orbite VARCHAR2(20) PRIMARY KEY,
                        type_orbite VARCHAR2(10) NOT NULL,
                        altitude NUMBER(5) NOT NULL,
                        inclinaison NUMBER(5,2) NOT NULL,
                        periode_orbitale NUMBER(6,2) NOT NULL,
                        excentricite NUMBER(6,4) NOT NULL,
                        zone_couverture VARCHAR2(200),
                        CONSTRAINT uq_orbite UNIQUE (altitude, inclinaison),
                        CONSTRAINT chk_altitude CHECK (altitude > 0),
                        CONSTRAINT chk_inclinaison CHECK (inclinaison BETWEEN 0 AND 180),
                        CONSTRAINT chk_excentricite CHECK (excentricite BETWEEN 0 AND 1),
                        CONSTRAINT chk_periode CHECK (periode_orbitale > 0)
);

--------------------------------------------------
-- 2. TABLE SATELLITE
--------------------------------------------------
CREATE TABLE SATELLITE (
                           id_satellite VARCHAR2(20) PRIMARY KEY,
                           nom_satellite VARCHAR2(100) NOT NULL,
                           date_lancement DATE NOT NULL,
                           masse NUMBER(5,2) NOT NULL,
                           format_cubesat VARCHAR2(5) NOT NULL,
                           statut VARCHAR2(30) NOT NULL,
                           duree_vie_prevue NUMBER(4) NOT NULL,
                           capacite_batterie NUMBER(6,1) NOT NULL,
                           id_orbite VARCHAR2(20),
                           CONSTRAINT fk_sat_orbite FOREIGN KEY (id_orbite) REFERENCES ORBITE(id_orbite),
                           CONSTRAINT chk_sat_masse CHECK (masse > 0),
                           CONSTRAINT chk_sat_duree CHECK (duree_vie_prevue > 0),
                           CONSTRAINT chk_sat_batterie CHECK (capacite_batterie > 0),
                           CONSTRAINT chk_sat_statut CHECK (statut IN ('Opérationnel','En veille','Défaillant','Désorbité')),
                           CONSTRAINT chk_format_cubesat CHECK (format_cubesat IN ('1U','3U','6U','12U'))
);

--------------------------------------------------
-- 3. TABLE INSTRUMENT
--------------------------------------------------
CREATE TABLE INSTRUMENT (
                            ref_instrument VARCHAR2(20) PRIMARY KEY,
                            type_instrument VARCHAR2(50) NOT NULL,
                            modele VARCHAR2(100) NOT NULL,
                            resolution NUMBER(6,1),
                            consommation NUMBER(5,2) NOT NULL,
                            masse NUMBER(5,3) NOT NULL,
                            CONSTRAINT chk_instr_consommation CHECK (consommation > 0),
                            CONSTRAINT chk_instr_masse CHECK (masse > 0),
                            CONSTRAINT chk_instr_resolution CHECK (resolution IS NULL OR resolution > 0)
);

--------------------------------------------------
-- 4. TABLE EMBARQUEMENT
--------------------------------------------------
CREATE TABLE EMBARQUEMENT (
                            id_satellite VARCHAR2(20),
                            ref_instrument VARCHAR2(20),
                            date_integration DATE NOT NULL,
                            etat_fonctionnement VARCHAR2(20) NOT NULL,
                            CONSTRAINT chk_emb_etat CHECK (etat_fonctionnement IN ('Nominal','Dégradé','Hors service')),
                            PRIMARY KEY (id_satellite, ref_instrument),
                            FOREIGN KEY (id_satellite) REFERENCES SATELLITE(id_satellite),
                            FOREIGN KEY (ref_instrument) REFERENCES INSTRUMENT(ref_instrument)
);

--------------------------------------------------
-- 5. TABLE CENTRE_CONTROLE
--------------------------------------------------
CREATE TABLE CENTRE_CONTROLE (
                            id_centre VARCHAR2(20) PRIMARY KEY,
                            nom_centre VARCHAR2(100) NOT NULL,
                            ville VARCHAR2(50) NOT NULL,
                            region_geo VARCHAR2(50) NOT NULL,
                            fuseau_horaire VARCHAR2(50) NOT NULL,
                            statut VARCHAR2(20) NOT NULL,
                            CONSTRAINT chk_centre_statut CHECK (statut IN ('Actif','Inactif'))
);

--------------------------------------------------
-- 6. TABLE STATION_SOL
--------------------------------------------------
CREATE TABLE STATION_SOL (
                            code_station VARCHAR2(20) PRIMARY KEY,
                            nom_station VARCHAR2(100) NOT NULL,
                            latitude NUMBER(9,6) NOT NULL,
                            longitude NUMBER(9,6) NOT NULL,
                            diametre_antenne NUMBER(4,1) NOT NULL,
                            bande_frequence VARCHAR2(10) NOT NULL,
                            debit_max NUMBER(6,1) NOT NULL,
                            statut VARCHAR2(20) NOT NULL,
                            CONSTRAINT chk_latitude CHECK (latitude BETWEEN -90 AND 90),
                            CONSTRAINT chk_longitude CHECK (longitude BETWEEN -180 AND 180),
                            CONSTRAINT chk_diametre CHECK (diametre_antenne > 0),
                            CONSTRAINT chk_debit CHECK (debit_max > 0),
                            CONSTRAINT chk_station_statut CHECK (statut IN ('Active','Maintenance'))
);

--------------------------------------------------
-- 7. TABLE AFFECTATION_STATION
--------------------------------------------------
CREATE TABLE AFFECTATION_STATION (
                            id_centre VARCHAR2(20),
                            code_station VARCHAR2(20),
                            date_affectation DATE NOT NULL,
                            PRIMARY KEY (id_centre, code_station),
                            FOREIGN KEY (id_centre) REFERENCES CENTRE_CONTROLE(id_centre),
                            FOREIGN KEY (code_station) REFERENCES STATION_SOL(code_station)
);

--------------------------------------------------
-- 8. TABLE MISSION
--------------------------------------------------
CREATE TABLE MISSION (
                            id_mission VARCHAR2(20) PRIMARY KEY,
                            nom_mission VARCHAR2(100) NOT NULL,
                            objectif VARCHAR2(500) NOT NULL,
                            zone_geo_cible VARCHAR2(200) NOT NULL,
                            date_debut DATE NOT NULL,
                            date_fin DATE,
                            statut_mission VARCHAR2(20) NOT NULL,
                            CONSTRAINT chk_mission_dates CHECK (date_fin IS NULL OR date_fin >= date_debut),
                            CONSTRAINT chk_mission_statut CHECK (statut_mission IN ('Planifiée','Active','Terminée'))
);

--------------------------------------------------
-- 9. TABLE FENETRE_COM
--------------------------------------------------
CREATE TABLE FENETRE_COM (
                            id_fenetre NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            datetime_debut TIMESTAMP NOT NULL,
                            duree NUMBER(4) NOT NULL,
                            elevation_max NUMBER(5,2) NOT NULL,
                            volume_donnees NUMBER(8,1),
                            statut VARCHAR2(20) NOT NULL,
                            id_satellite VARCHAR2(20) NOT NULL,
                            code_station VARCHAR2(20) NOT NULL,
                            FOREIGN KEY (id_satellite) REFERENCES SATELLITE(id_satellite),
                            FOREIGN KEY (code_station) REFERENCES STATION_SOL(code_station),
                            CONSTRAINT chk_duree CHECK (duree BETWEEN 1 AND 900),
                            CONSTRAINT chk_fenetre_statut CHECK (statut IN ('Planifiée','Réalisée','Annulée')),
                            CONSTRAINT chk_volume_statut CHECK (
                            (statut = 'Réalisée' AND volume_donnees IS NOT NULL)
                            OR
                            (statut <> 'Réalisée' AND volume_donnees IS NULL)
                            )
);

--------------------------------------------------
-- 10. TABLE PARTICIPATION
--------------------------------------------------
CREATE TABLE PARTICIPATION (
                               id_satellite VARCHAR2(20),
                               id_mission VARCHAR2(20),
                               role_satellite VARCHAR2(50) NOT NULL,
                               PRIMARY KEY (id_satellite, id_mission),
                               FOREIGN KEY (id_satellite) REFERENCES SATELLITE(id_satellite),
                               FOREIGN KEY (id_mission) REFERENCES MISSION(id_mission)
);

--------------------------------------------------
-- 11. TABLE HISTORIQUE_STATUT
--------------------------------------------------
CREATE TABLE HISTORIQUE_STATUT (
                                   id_hist NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   id_satellite VARCHAR2(20),
                                   ancien_statut VARCHAR2(30),
                                   nouveau_statut VARCHAR2(30),
                                   date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (id_satellite) REFERENCES SATELLITE(id_satellite)
);

--------------------------------------------------
COMMIT;
--------------------------------------------------