--------------------------------------------------
-- NANOORBIT - PHASE 2
-- SCHÉMA DDL COMPLET + TRIGGERS
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
                        CONSTRAINT uq_orbite UNIQUE (altitude, inclinaison)
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
                           CONSTRAINT fk_sat_orbite FOREIGN KEY (id_orbite)
                               REFERENCES ORBITE(id_orbite)
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
                            masse NUMBER(5,3) NOT NULL
);

--------------------------------------------------
-- 4. TABLE EMBARQUEMENT
--------------------------------------------------
CREATE TABLE EMBARQUEMENT (
                              id_satellite VARCHAR2(20),
                              ref_instrument VARCHAR2(20),
                              date_integration DATE NOT NULL,
                              etat_fonctionnement VARCHAR2(20) NOT NULL,
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
                                 statut VARCHAR2(20) NOT NULL
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
                             statut VARCHAR2(20) NOT NULL
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
                         statut_mission VARCHAR2(20) NOT NULL
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
                             CONSTRAINT chk_duree CHECK (duree BETWEEN 1 AND 900)
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