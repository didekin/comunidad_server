INSERT INTO didekin.comunidad VALUES (1,'Ronda','de la Plazuela',10,'bis',103,NULL,NULL);
INSERT INTO didekin.comunidad VALUES (2,'Calle','de la Fuente',11,'',204,NULL,NULL);
INSERT INTO didekin.comunidad VALUES (3,'Calle','de El Escorial',2,'',305,NULL,NULL);
INSERT INTO didekin.comunidad VALUES (4,'Calle','de la Plazuela',23,'',103,NULL,NULL);
INSERT INTO didekin.comunidad VALUES (6, 'Calle', 'del Olmo', 55, '', 105, NULL, NULL);

INSERT INTO didekin.usuario VALUES (3,'pedronevado','password3','pedro@pedro.com',NULL);
INSERT INTO didekin.usuario VALUES (5,'luis_gomez','password5','luis@luis.com', NULL );
INSERT INTO didekin.usuario VALUES (7,'juan_no_auth','password7','juan@noauth.com',NULL );
INSERT INTO didekin.usuario VALUES (9,'pepe_no_auth','password9','pepe@noauth.com', NULL );
INSERT INTO didekin.usuario VALUES (11,'paco','password11','paco@paco.com', NULL);

INSERT INTO didekin.usuario_comunidad VALUES (1,3,'Centro',NULL,3,'J','adm',DATE_SUB(NOW(), INTERVAL 20 SECOND),NULL);
INSERT INTO didekin.usuario_comunidad VALUES (1,5,NULL,NULL,NULL,NULL,'adm,pro',NULL,NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2,3,'A',NULL,NULL,NULL,'adm,inq',NULL,NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2,7,'A',NULL,NULL,NULL,'inq',DATE_SUB(NOW(), INTERVAL 20 SECOND),NULL);
INSERT INTO didekin.usuario_comunidad VALUES (3,3,'A',NULL,NULL,NULL,'adm,inq',NULL,NULL);
INSERT INTO didekin.usuario_comunidad VALUES (4,11,'BC',NULL,NULL,NULL,'adm,pro',NULL,NULL);
INSERT INTO didekin.usuario_comunidad VALUES (4, 7, 'JJ', NULL, NULL, NULL, 'inq', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (6, 11, 'B', NULL, 'Planta 0', '11', 'pro', NULL, NULL);





