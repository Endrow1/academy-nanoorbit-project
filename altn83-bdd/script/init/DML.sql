--------------------------------------------------
-- NANOORBIT - PHASE 2
-- INSERT DES DONNÉES (DML)
--------------------------------------------------

--------------------------------------------------
-- 1. ORBITE
--------------------------------------------------
INSERT INTO ORBITE VALUES ('1','SSO',550,97.6,95.5,0.001,'Polaire globale — Europe / Arctique');
INSERT INTO ORBITE VALUES ('2','SSO',700,98.2,98.8,0.0008,'Polaire globale — haute latitude');
INSERT INTO ORBITE VALUES ('3','LEO',400,51.6,92.6,0.002,'Équatoriale — zone tropicale');

--------------------------------------------------
-- 2. SATELLITE
--------------------------------------------------
INSERT INTO SATELLITE VALUES ('SAT-001','NanoOrbit-Alpha',DATE '2022-03-15',1.3,'3U','Opérationnel',60,20,'1');
INSERT INTO SATELLITE VALUES ('SAT-002','NanoOrbit-Beta',DATE '2022-03-15',1.3,'3U','Opérationnel',60,20,'1');
INSERT INTO SATELLITE VALUES ('SAT-003','NanoOrbit-Gamma',DATE '2023-06-10',2.0,'6U','Opérationnel',84,40,'2');
INSERT INTO SATELLITE VALUES ('SAT-004','NanoOrbit-Delta',DATE '2023-06-10',2.0,'6U','En veille',84,40,'2');
INSERT INTO SATELLITE VALUES ('SAT-005','NanoOrbit-Epsilon',DATE '2021-11-20',4.5,'12U','Désorbité',36,80,'3');

--------------------------------------------------
-- 3. INSTRUMENT
--------------------------------------------------
INSERT INTO INSTRUMENT VALUES ('INS-CAM-01','Caméra optique','PlanetScope-Mini',3,2.5,0.4);
INSERT INTO INSTRUMENT VALUES ('INS-IR-01','Infrarouge','FLIR-Lepton-3',160,1.2,0.15);
INSERT INTO INSTRUMENT VALUES ('INS-AIS-01','Récepteur AIS','ShipTrack-V2',NULL,0.8,0.12);
INSERT INTO INSTRUMENT VALUES ('INS-SPEC-01','Spectromètre','HyperSpec-Nano',30,3.1,0.6);

--------------------------------------------------
-- 4. EMBARQUEMENT
--------------------------------------------------
INSERT INTO EMBARQUEMENT VALUES ('SAT-001','INS-CAM-01',DATE '2022-03-15','Nominal');
INSERT INTO EMBARQUEMENT VALUES ('SAT-001','INS-IR-01',DATE '2022-03-15','Nominal');
INSERT INTO EMBARQUEMENT VALUES ('SAT-002','INS-CAM-01',DATE '2022-03-15','Nominal');
INSERT INTO EMBARQUEMENT VALUES ('SAT-003','INS-CAM-01',DATE '2023-06-10','Nominal');
INSERT INTO EMBARQUEMENT VALUES ('SAT-003','INS-SPEC-01',DATE '2023-06-10','Nominal');
INSERT INTO EMBARQUEMENT VALUES ('SAT-004','INS-IR-01',DATE '2023-06-10','Dégradé');
INSERT INTO EMBARQUEMENT VALUES ('SAT-005','INS-AIS-01',DATE '2021-11-20','Hors service');

--------------------------------------------------
-- 5. CENTRE_CONTROLE
--------------------------------------------------
INSERT INTO CENTRE_CONTROLE VALUES ('1','NanoOrbit Paris HQ','Paris','Europe','Europe/Paris','Actif');
INSERT INTO CENTRE_CONTROLE VALUES ('2','NanoOrbit Houston','Houston','Amériques','America/Chicago','Actif');
INSERT INTO CENTRE_CONTROLE VALUES ('3','NanoOrbit Singapore','Singapour','Asie-Pacifique','Asia/Singapore','Actif');

--------------------------------------------------
-- 6. STATION_SOL
--------------------------------------------------
INSERT INTO STATION_SOL VALUES ('GS-TLS-01','Toulouse Ground Station',43.6047,1.4442,3.5,'S',150,'Active');
INSERT INTO STATION_SOL VALUES ('GS-KIR-01','Kiruna Arctic Station',67.8557,20.2253,5.4,'X',400,'Active');
INSERT INTO STATION_SOL VALUES ('GS-SGP-01','Singapore Station',1.3521,103.8198,3.0,'S',120,'Maintenance');

--------------------------------------------------
-- 7. AFFECTATION_STATION
--------------------------------------------------
INSERT INTO AFFECTATION_STATION VALUES ('1','GS-TLS-01',DATE '2022-01-10');
INSERT INTO AFFECTATION_STATION VALUES ('1','GS-KIR-01',DATE '2022-01-10');
INSERT INTO AFFECTATION_STATION VALUES ('3','GS-SGP-01',DATE '2022-01-10');

--------------------------------------------------
-- 8. MISSION
--------------------------------------------------
INSERT INTO MISSION VALUES (
                               'MSN-ARC-2023',
                               'ArcticWatch 2023',
                               'Surveillance de la fonte des glaces et dynamique des banquises arctiques',
                               'Arctique / Groenland',
                               DATE '2023-01-01',
                               NULL,
                               'Active'
                           );

INSERT INTO MISSION VALUES (
                               'MSN-DEF-2022',
                               'DeforestAlert',
                               'Détection et cartographie de la déforestation en temps quasi-réel',
                               'Amazonie / Congo',
                               DATE '2022-06-01',
                               DATE '2023-05-31',
                               'Terminée'
                           );

INSERT INTO MISSION VALUES (
                               'MSN-COAST-2024',
                               'CoastGuard 2024',
                               'Surveillance de l''évolution du trait de côte et détection d''érosion côtière',
                               'Méditerranée / Atlantique',
                               DATE '2024-03-01',
                               NULL,
                               'Active'
                           );

--------------------------------------------------
-- 9. FENETRE_COM
--------------------------------------------------
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station) VALUES (TIMESTAMP '2024-01-15 09:14:00',420,82.3,1250,'Réalisée','SAT-001','GS-KIR-01');
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station) VALUES (TIMESTAMP '2024-01-15 11:52:00',310,67.1,890,'Réalisée','SAT-002','GS-TLS-01');
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station) VALUES (TIMESTAMP '2024-01-16 08:30:00',540,88.9,1680,'Réalisée','SAT-003','GS-KIR-01');
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station) VALUES (TIMESTAMP '2024-01-20 14:22:00',380,71.4,NULL,'Planifiée','SAT-001','GS-TLS-01');
INSERT INTO FENETRE_COM (datetime_debut,duree,elevation_max,volume_donnees,statut,id_satellite,code_station) VALUES (TIMESTAMP '2024-01-21 07:45:00',290,59.8,NULL,'Planifiée','SAT-003','GS-TLS-01');
--------------------------------------------------
-- 10. PARTICIPATION
--------------------------------------------------
INSERT INTO PARTICIPATION VALUES ('SAT-001','MSN-ARC-2023','Imageur principal');
INSERT INTO PARTICIPATION VALUES ('SAT-002','MSN-ARC-2023','Imageur secondaire');
INSERT INTO PARTICIPATION VALUES ('SAT-003','MSN-ARC-2023','Satellite de relais');
INSERT INTO PARTICIPATION VALUES ('SAT-001','MSN-DEF-2022','Imageur principal');
INSERT INTO PARTICIPATION VALUES ('SAT-005','MSN-DEF-2022','Imageur secondaire');
INSERT INTO PARTICIPATION VALUES ('SAT-003','MSN-COAST-2024','Imageur principal');
INSERT INTO PARTICIPATION VALUES ('SAT-004','MSN-COAST-2024','Satellite de secours');

--------------------------------------------------
COMMIT;
--------------------------------------------------