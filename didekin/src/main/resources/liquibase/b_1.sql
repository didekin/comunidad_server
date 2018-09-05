--liquibase formatted sql

/*--changeset pedronevado:1 dbms:mysql*/
ALTER TABLE usuario
  add column token_auth CHAR(60) NULL after gcm_token;

--rollback alter table usuario drop column TOKEN_AUTH;

--changeset pedronevado:2 dbms:mysql

DROP TABLE IF EXISTS oauth_refresh_token;

--changeset pedronevado:3 dbms:mysql

DROP TABLE IF EXISTS oauth_access_token;