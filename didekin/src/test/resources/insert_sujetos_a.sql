INSERT INTO didekin.comunidad
VALUES (1, 'Ronda', 'de la Plazuela', 10, 'bis', 103, 'H00012674', 'op');
INSERT INTO didekin.comunidad
VALUES (2, 'Calle', 'de la Fuente', 11, '', 204, 'H76543214', 'op');
INSERT INTO didekin.comunidad
VALUES (3, 'Calle', 'de El Escorial', 2, '', 305, 'H86543212', 'op');
INSERT INTO didekin.comunidad
VALUES (4, 'Calle', 'de la Plazuela', 23, '', 103, 'H96543210', 'op');
INSERT INTO didekin.comunidad
VALUES (6, 'Calle', 'del Olmo', 55, '', 105, 'H76543297', 'op');

INSERT INTO didekin.usuario
VALUES (3, 'pedronevado', 'password3', 'pedro@didekin.es', NULL, NULL, 'op');
INSERT INTO didekin.usuario
VALUES (5, 'luis_gomez', 'password5', 'luis@luis.com', 'luis_gcm_token', 'luis_token_auth', 'op');
INSERT INTO didekin.usuario
VALUES (7, 'juan_no_auth', 'password7', 'juan@noauth.com', NULL, NULL, 'op');
INSERT INTO didekin.usuario
VALUES (9, 'pepe_no_auth', 'password9', 'pepe@noauth.com', NULL, NULL, 'op');
INSERT INTO didekin.usuario
VALUES (11, 'paco', 'password11', 'paco@paco.com', NULL, NULL, 'op');

INSERT INTO didekin.comunidad_miembro
VALUES (1, 3, 'Centro', NULL, 3, 'J', 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (1, 5, NULL, NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (2, 3, 'A', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (2, 7, 'A', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (3, 3, 'A', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (4, 11, 'BC', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (4, 7, 'JJ', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (6, 11, 'B', NULL, 'Planta 0', '11', 'op');

# DATE_SUB(NOW(), INTERVAL 20 SECOND)


