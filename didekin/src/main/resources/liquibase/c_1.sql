# noinspection SqlResolveForFile

--liquibase formatted sql

--changeset pedronevado:4 dbms:mysql
ALTER TABLE comunidad
  drop index tipo_via,
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:5 dbms:mysql
ALTER TABLE usuario
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:6 dbms:mysql
ALTER TABLE usuario_comunidad
  drop column roles,
  drop column fecha_alta,
  drop column fecha_mod;