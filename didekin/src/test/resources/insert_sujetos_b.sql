INSERT INTO didekin.comunidad VALUES (1, 'Ronda', 'de la Plazuela', 10, 'bis', 103, NOW(), NULL);
INSERT INTO didekin.comunidad VALUES (2, 'Calle', 'de la Fuente', 11, '', 204, NOW(), NULL);
INSERT INTO didekin.comunidad VALUES (3, 'Calle', 'de El Escorial', 2, '', 305, NOW(), NULL);
INSERT INTO didekin.comunidad VALUES (4, 'Calle', 'de la Plazuela', 23, '', 103, NOW(), NULL);
INSERT INTO didekin.comunidad VALUES (6, 'Calle', 'del Olmo', 55, '', 105, NOW(), NULL);

INSERT INTO didekin.usuario
VALUES
  (3, 'pedronevado', '$2a$10$9Gg4THybJVAAOY3yfzDY1uaTEhajatpX.WC056Ibd.fQJMG9QyZ3S', 'pedro@didekin.es', NULL, NULL, NOW(), NULL);
INSERT INTO didekin.usuario
VALUES
  (5, 'luis_gomez', '$2a$10$km0D4Uc5cFV1Gv6aAnoeeu03XNk1i686uqlB2A0BClNtB5A8LucLK', 'luis@luis.com', 'luis_gcm_token', 'luis_token_auth',
   NOW(), NULL);
INSERT INTO didekin.usuario
VALUES (11, 'paco', '$2a$10$cdOjupfvgP/zpTOd5yyGROQVjjUhU18J/kg/04ZoUwLAUoeIo5xbK', 'paco@paco.com', NULL, NULL, NOW(), NULL);
-- No tiene usuario_comunidad en DB, no roles por lo tanto y no autorización.
INSERT INTO didekin.usuario
VALUES
  (7, 'juan_no_auth', '$2a$10$Gp06vc0EB.s8gkmulvfzouhCdkcaa5mvhlJsIf4XnrCYsJF/UwG56', 'juan@noauth.com', NULL, NULL, NOW(), NULL);

INSERT INTO didekin.usuario_comunidad VALUES (1, 3, 'Centro', NULL, 3, 'J', 'pro', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (1, 5, NULL, NULL, NULL, NULL, 'adm', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2, 3, 'A', NULL, NULL, NULL, 'adm', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (2, 5, 'B', NULL, NULL, NULL, 'pro', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (3, 3, 'A', NULL, NULL, NULL, 'adm', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (4, 5, '2', 'escalera 1', 'C', NULL, 'inq', NOW(), NULL);
INSERT INTO didekin.usuario_comunidad VALUES (6, 11, 'B', NULL, 'Planta 0', '11', 'pro', NOW(), NULL);