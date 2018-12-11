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
VALUES (3, 'pedronevado', '$2a$10$9Gg4THybJVAAOY3yfzDY1uaTEhajatpX.WC056Ibd.fQJMG9QyZ3S', 'pedro@didekin.es', NULL, NULL, 'op');
INSERT INTO didekin.usuario
VALUES (5,
        'luis_gomez',
        '$2a$10$km0D4Uc5cFV1Gv6aAnoeeu03XNk1i686uqlB2A0BClNtB5A8LucLK',
        'luis@luis.com',
        'luis_gcm_token',
        'luis_token_auth',
        'op');
INSERT INTO didekin.usuario
VALUES (11, 'paco', '$2a$10$cdOjupfvgP/zpTOd5yyGROQVjjUhU18J/kg/04ZoUwLAUoeIo5xbK', 'paco@paco.com', NULL, NULL, 'op');
-- No tiene comunidad_miembro en DB, no roles por lo tanto y no autorización.
INSERT INTO didekin.usuario
VALUES (7, 'juan_no_auth', '$2a$10$Gp06vc0EB.s8gkmulvfzouhCdkcaa5mvhlJsIf4XnrCYsJF/UwG56', 'juan@noauth.com', NULL, NULL, 'op');

INSERT INTO didekin.comunidad_miembro
VALUES (1, 3, 'Centro', NULL, 3, 'J', 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (1, 5, NULL, NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (2, 3, 'A', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (2, 5, 'B', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (3, 3, 'A', NULL, NULL, NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (4, 5, '2', 'escalera 1', 'C', NULL, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (6, 11, 'B', NULL, 'Planta 0', '11', 'op');