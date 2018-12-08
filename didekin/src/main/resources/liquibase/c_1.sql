# noinspection SqlResolveForFile

--liquibase formatted sql

--changeset pedronevado:1 dbms:mysql
ALTER TABLE comunidad
  drop index tipo_via,
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:2 dbms:mysql
ALTER TABLE usuario
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:3 dbms:mysql
ALTER TABLE usuario_comunidad
  drop column roles,
  drop column fecha_alta,
  drop column fecha_mod;