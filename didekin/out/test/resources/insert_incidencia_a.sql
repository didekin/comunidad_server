INSERT INTO didekin.comunidad VALUES (1, 'Ronda', 'de la Plazuela', 10, 'bis', 103, NULL, NULL);
INSERT INTO didekin.comunidad VALUES (2, 'Calle', 'de la Fuente', 11, '', 204, NULL, NULL);
INSERT INTO didekin.comunidad VALUES (4, 'Calle', 'de la Plazuela', 23, '', 103, NULL, NULL);
INSERT INTO didekin.comunidad VALUES (6, 'Calle', 'del Olmo', 55, '', 105, NULL, NULL);

INSERT INTO didekin.usuario VALUES (3, 'pedronevado', 'password3', 'pedro@pedro.com', NULL);
INSERT INTO didekin.usuario VALUES (5, 'luis_gomez', 'password5', 'luis@luis.com', NULL );
INSERT INTO didekin.usuario VALUES (7, 'juan_no_auth', 'password7', 'juan@noauth.com', NULL);
INSERT INTO didekin.usuario VALUES (11, 'paco', 'password11', 'paco@paco.com', NULL);

INSERT INTO didekin.usuario_comunidad VALUES (1, 3, 'Centro', NULL, 3, 'J', 'adm', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (1, 5, NULL, NULL, NULL, NULL, 'pro,pre', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2, 3, 'A', NULL, NULL, NULL, 'adm,inq', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2, 7, 'A', NULL, NULL, NULL, 'inq', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (4, 11, 'BC', NULL, NULL, NULL, 'adm,pro', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (4, 7, 'JJ', NULL, NULL, NULL, 'inq', NULL, NULL);
INSERT INTO didekin.usuario_comunidad VALUES (6, 11, 'B', NULL, 'Planta 0', '11', 'pro', NULL, NULL);


INSERT INTO didekin.incidencia
VALUES (1, 1, 'luis@luis.com', 'incidencia_1', 41, DATE_SUB(NOW(), INTERVAL 20 SECOND), NULL);
INSERT INTO didekin.incidencia
VALUES (2, 2, 'juan@noauth.com', 'incidencia_2', 22, DATE_SUB(NOW(), INTERVAL 16 SECOND), NULL);
INSERT INTO didekin.incidencia
VALUES (3, 1, 'pedro@pedro.com', 'incidencia_3', 46, DATE_SUB(NOW(), INTERVAL 10 SECOND), NULL);
INSERT INTO didekin.incidencia
VALUES (4, 4, 'paco@paco.com', 'incidencia_4', 37, DATE_SUB(NOW(), INTERVAL 8 SECOND), NULL);
INSERT INTO didekin.incidencia
VALUES (5, 4, 'paco@paco.com', 'incidencia_5', 3, DATE_SUB(NOW(), INTERVAL 2000 SECOND), DATE_SUB(NOW(), INTERVAL 1000 SECOND));
INSERT INTO didekin.incidencia
VALUES (6, 6, 'paco@paco.com', 'incidencia_6_6', 37, DATE_SUB(NOW(), INTERVAL 2000 SECOND), NULL);
INSERT INTO didekin.incidencia
VALUES (7, 6, 'paco@paco.com', 'incidencia_7_6', 1, DATE_SUB(NOW(), INTERVAL 25 MONTH), DATE_SUB(NOW(), INTERVAL 20 MONTH));   -- Control de antigüedad 2 años.

INSERT INTO didekin.incidencia_importancia VALUES (1, 1, 3, 2, DATE_SUB(NOW(), INTERVAL 20 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (1, 1, 5, 1, DATE_SUB(NOW(), INTERVAL 18 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (2, 2, 3, 3, DATE_SUB(NOW(), INTERVAL 16 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (2, 2, 7, 4, DATE_SUB(NOW(), INTERVAL 12 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (3, 1, 3, 1, DATE_SUB(NOW(), INTERVAL 10 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (4, 4, 11, 4, DATE_SUB(NOW(), INTERVAL 8 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (5, 4, 11, 2, DATE_SUB(NOW(), INTERVAL 2000 SECOND));
INSERT INTO didekin.incidencia_importancia VALUES (6, 6, 11, 2, DATE_SUB(NOW(), INTERVAL 2000 SECOND));

INSERT INTO didekin.incidencia_comment
VALUES (1, 1, 1, 3, 'comment_1_incid_1_comu_1_user_3', DATE_ADD(NOW(), INTERVAL 1 SECOND));
INSERT INTO didekin.incidencia_comment
VALUES (2, 1, 1, 3, 'comment_2_incid_1_comu_1_user_3', DATE_ADD(NOW(), INTERVAL 2 SECOND));
INSERT INTO didekin.incidencia_comment
VALUES (3, 1, 1, 5, 'comment_3_incid_1_comu_1_user_5', DATE_ADD(NOW(), INTERVAL 3 SECOND));
INSERT INTO didekin.incidencia_comment
VALUES (4, 4, 4, 11, 'comment_4_incid_4_comu_4_user_11', DATE_ADD(NOW(), INTERVAL 4 SECOND));

INSERT INTO didekin.incidencia_resolucion
VALUES (3, 'pedro@pedro.com', 'plan_resol_3', 11, DATE_ADD(NOW(), INTERVAL 100 SECOND), 11, 'moraleja_3', DATE_ADD(NOW(), INTERVAL 8 SECOND));
INSERT INTO didekin.incidencia_resolucion
VALUES (5, 'paco@paco.com', 'plan_resol_5', 22, DATE_ADD(NOW(), INTERVAL 118 SECOND), 23, 'moraleja_5', DATE_ADD(NOW(), INTERVAL 18 SECOND));

INSERT INTO didekin.incidencia_res_avance
VALUES (1, 3, 'pedro@pedro.com', 'descripcion_avance_1_3', DATE_ADD(NOW(), INTERVAL 18 SECOND));
INSERT INTO didekin.incidencia_res_avance
VALUES (2, 3, 'pedro@pedro.com', 'descripcion_avance_2_3', DATE_ADD(NOW(), INTERVAL 28 SECOND));
