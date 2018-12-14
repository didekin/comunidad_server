INSERT INTO didekin.entidad
VALUES (1, 'H00012674', 'Ronda', 'de la Plazuela', 10, 'bis', 103, 'comunidad', 'op');
INSERT INTO didekin.entidad
VALUES (2, 'H76543214', 'Calle', 'de la Fuente', 11, '', 204, 'comunidad', 'op');
INSERT INTO didekin.entidad
VALUES (3, 'H86543212', 'Calle', 'de El Escorial', 2, '', 305, 'comunidad', 'op');
INSERT INTO didekin.entidad
VALUES (4, 'H96543210', 'Calle', 'de la Plazuela', 23, '', 103, 'comunidad', 'op');
INSERT INTO didekin.entidad
VALUES (6, 'H76543297', 'Calle', 'del Olmo', 55, '', 105, 'comunidad', 'op');

# didekin.usuario ============================================================================================
INSERT INTO didekin.usuario
VALUES (3,
        'pedronevado',
        '$2a$10$9Gg4THybJVAAOY3yfzDY1uaTEhajatpX.WC056Ibd.fQJMG9QyZ3S',
        'pedro@didekin.es',
        NULL,
        'op'); # password3
INSERT INTO didekin.usuario
VALUES (5,
        'luis_gomez',
        '$2a$10$km0D4Uc5cFV1Gv6aAnoeeu03XNk1i686uqlB2A0BClNtB5A8LucLK',
        'luis@luis.com',
        'luis_token_auth',
        'op'); # password5
INSERT INTO didekin.usuario
VALUES (7,
        'juan_no_auth',
        '$2a$10$Gp06vc0EB.s8gkmulvfzouhCdkcaa5mvhlJsIf4XnrCYsJF/UwG56',
        'juan@noauth.com',
        NULL,
        'op'); # password7
INSERT INTO didekin.usuario
VALUES (11, 'paco', '$2a$10$cdOjupfvgP/zpTOd5yyGROQVjjUhU18J/kg/04ZoUwLAUoeIo5xbK', 'paco@paco.com', NULL, 'op'); # password11

# didekin.usuario_appinstance ============================================================================================
INSERT INTO didekin.usuario_appinstance
VALUES (3,
        '5755099286786375742977069411837913043687552329990879963599357261164258192822',
        '113508458922942625762303045849511115644149177067166179100542202199973362908704',
        NULL,
        'op');
INSERT INTO didekin.usuario_appinstance
VALUES (5,
        '14363705882766059861269980297772312796972996763835838973258301123617792691903',
        '8548796226027671542656434456983491211235689364124249696020996388434697910695',
        'luis_gcm_token',
        'op');
INSERT INTO didekin.usuario_appinstance
VALUES (7,
        '60516652787459742767729316113553183033590402337129336603609306259764050394247',
        '23438094196253963580466696420413957191774181758952894869561728098531469146056',
        NULL,
        'op');
INSERT INTO didekin.usuario_appinstance
VALUES (11,
        '90539592815903960196404796086847013657091594983797510255921369380347085239401',
        '67416172060429196669029199061605428267755702429247982587206641144020390989225',
        NULL,
        'op');

# didekin.comunidad_miembro ============================================================================================
INSERT INTO didekin.comunidad_miembro
VALUES (1, 1, 3, 'Centro', NULL, 3, 'J', TRUE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (2, 1, 5, NULL, NULL, NULL, NULL, FALSE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (3, 2, 3, 'A', NULL, NULL, NULL, FALSE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (4, 2, 7, 'A', NULL, NULL, NULL, TRUE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (5, 3, 3, 'A', NULL, NULL, NULL, FALSE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (6, 4, 11, 'BC', NULL, NULL, NULL, FALSE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (7, 4, 7, 'JJ', NULL, NULL, NULL, TRUE, 'op');
INSERT INTO didekin.comunidad_miembro
VALUES (8, 6, 11, 'B', NULL, 'Planta 0', '11', FALSE, 'op');

# DATE_SUB(NOW(), INTERVAL 20 SECOND)


