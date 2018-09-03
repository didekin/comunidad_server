# --liquibase formatted sql

# --changeset pedronevado:1 dbms:mysql
ALTER TABLE usuario
  add column token_auth CHAR(60) NULL
  after gcm_token;
# --rollback alter table usuario drop column TOKEN_AUTH;