-- Dump created by MySQL pump utility, version: 5.7.23, osx10.13 (x86_64)
-- Dump start time: Mon Sep  3 19:01:34 2018
-- Server version: 5.7.23

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE;
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET @@SESSION.SQL_LOG_BIN= 0;
SET @OLD_TIME_ZONE=@@TIME_ZONE;
SET TIME_ZONE='+00:00';
SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT;
SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS;
SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION;
SET NAMES utf8mb4;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `didekin` /*!40100 DEFAULT CHARACTER SET utf8 */;
DROP TABLE IF EXISTS `didekin`.`DATABASECHANGELOG`;
CREATE TABLE `didekin`.`DATABASECHANGELOG` (
`ID` varchar(255) NOT NULL,
`AUTHOR` varchar(255) NOT NULL,
`FILENAME` varchar(255) NOT NULL,
`DATEEXECUTED` datetime NOT NULL,
`ORDEREXECUTED` int(11) NOT NULL,
`EXECTYPE` varchar(10) NOT NULL,
`MD5SUM` varchar(35) DEFAULT NULL,
`DESCRIPTION` varchar(255) DEFAULT NULL,
`COMMENTS` varchar(255) DEFAULT NULL,
`TAG` varchar(255) DEFAULT NULL,
`LIQUIBASE` varchar(20) DEFAULT NULL,
`CONTEXTS` varchar(255) DEFAULT NULL,
`LABELS` varchar(255) DEFAULT NULL,
`DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
DROP TABLE IF EXISTS `didekin`.`DATABASECHANGELOGLOCK`;
CREATE TABLE `didekin`.`DATABASECHANGELOGLOCK` (
`ID` int(11) NOT NULL,
`LOCKED` bit(1) NOT NULL,
`LOCKGRANTED` datetime DEFAULT NULL,
`LOCKEDBY` varchar(255) DEFAULT NULL,
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
DROP TABLE IF EXISTS `didekin`.`ambito_incidencia`;
CREATE TABLE `didekin`.`ambito_incidencia` (
`ambito_id` smallint(5) unsigned NOT NULL,
`ambito_ES` varchar(100) NOT NULL,
PRIMARY KEY (`ambito_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
DROP TABLE IF EXISTS `didekin`.`comunidad`;
CREATE TABLE `didekin`.`comunidad` (
`c_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`tipo_via` varchar(25) NOT NULL,
`nombre_via` varchar(150) NOT NULL,
`numero` smallint(5) unsigned NOT NULL,
`sufijo_numero` char(10) DEFAULT NULL,
`m_id` int(10) unsigned NOT NULL,
`fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`c_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`comunidad` ADD UNIQUE KEY `tipo_via` (`tipo_via`,`nombre_via`,`numero`,`sufijo_numero`,`m_id`);
ALTER TABLE `didekin`.`comunidad` ADD KEY `id_parent_municipio` (`m_id`);
ALTER TABLE `didekin`.`comunidad` ADD CONSTRAINT `comunidad_ibfk_1` FOREIGN KEY (`m_id`) REFERENCES `municipio` (`m_id`) ON UPDATE CASCADE;
DROP TABLE IF EXISTS `didekin`.`comunidad_autonoma`;
CREATE TABLE `didekin`.`comunidad_autonoma` (
`ca_id` smallint(5) unsigned NOT NULL,
`nombre` varchar(100) NOT NULL,
PRIMARY KEY (`ca_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
DROP VIEW IF EXISTS `didekin`.`comunidades_municipio_view`;
CREATE VIEW `didekin`.`comunidades_municipio_view` AS SELECT
 1 AS `c_id`,
 1 AS `tipo_via`,
 1 AS `nombre_via`,
 1 AS `numero`,
 1 AS `sufijo_numero`,
 1 AS `m_id`,
 1 AS `fecha_alta`,
 1 AS `fecha_mod`,
 1 AS `pr_id`,
 1 AS `m_cd`,
 1 AS `m_nombre`,
 1 AS `pr_nombre`,
 1 AS `ca_id`
;
DROP VIEW IF EXISTS `didekin`.`incid_importancia_resolucion_view`;
CREATE VIEW `didekin`.`incid_importancia_resolucion_view` AS SELECT
 1 AS `incid_id`,
 1 AS `u_id`,
 1 AS `user_name`,
 1 AS `alias`,
 1 AS `roles`,
 1 AS `importancia`,
 1 AS `fecha_alta`,
 1 AS `descripcion`,
 1 AS `ambito`,
 1 AS `fecha_alta_incidencia`,
 1 AS `incid_user_initiator`,
 1 AS `c_id`,
 1 AS `comunidad_tipo_via`,
 1 AS `comunidad_nombre_via`,
 1 AS `comunidad_numero`,
 1 AS `comunidad_sufijo`,
 1 AS `fecha_alta_resolucion`
;
DROP VIEW IF EXISTS `didekin`.`incid_importancia_user_view`;
CREATE VIEW `didekin`.`incid_importancia_user_view` AS SELECT
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `u_id`,
 1 AS `user_name`,
 1 AS `alias`,
 1 AS `roles`
;
DROP TABLE IF EXISTS `didekin`.`incidencia`;
CREATE TABLE `didekin`.`incidencia` (
`incid_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`c_id` int(10) unsigned NOT NULL,
`user_name` varchar(60) NOT NULL,
`descripcion` varchar(300) NOT NULL,
`ambito` smallint(5) unsigned NOT NULL,
`fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
`fecha_cierre` timestamp(2) NULL DEFAULT NULL,
PRIMARY KEY (`incid_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`incidencia` ADD KEY `incid_id` (`incid_id`,`c_id`);
ALTER TABLE `didekin`.`incidencia` ADD KEY `id_parent_comunidad` (`c_id`);
ALTER TABLE `didekin`.`incidencia` ADD KEY `id_parent_ambito` (`ambito`);
ALTER TABLE `didekin`.`incidencia` ADD CONSTRAINT `incidencia_ibfk_1` FOREIGN KEY (`c_id`) REFERENCES `comunidad` (`c_id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `didekin`.`incidencia` ADD CONSTRAINT `incidencia_ibfk_2` FOREIGN KEY (`ambito`) REFERENCES `ambito_incidencia` (`ambito_id`) ON UPDATE CASCADE;
DROP VIEW IF EXISTS `didekin`.`incidencia_avg_view`;
CREATE VIEW `didekin`.`incidencia_avg_view` AS SELECT
 1 AS `incid_id`,
 1 AS `importancia_avg`
;
DROP TABLE IF EXISTS `didekin`.`incidencia_comment`;
CREATE TABLE `didekin`.`incidencia_comment` (
`comment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`incid_id` int(10) unsigned NOT NULL,
`c_id` int(10) unsigned NOT NULL,
`u_id` int(10) unsigned NOT NULL,
`descripcion` varchar(250) NOT NULL,
`fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`incidencia_comment` ADD KEY `id_parent_incidencia` (`incid_id`);
ALTER TABLE `didekin`.`incidencia_comment` ADD KEY `id_parent_usercomu` (`c_id`,`u_id`);
ALTER TABLE `didekin`.`incidencia_comment` ADD CONSTRAINT `incidencia_comment_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `didekin`.`incidencia_comment` ADD CONSTRAINT `incidencia_comment_ibfk_2` FOREIGN KEY (`c_id`, `u_id`) REFERENCES `usuario_comunidad` (`c_id`, `u_id`) ON DELETE CASCADE ON UPDATE CASCADE;
DROP VIEW IF EXISTS `didekin`.`incidencia_comment_view`;
CREATE VIEW `didekin`.`incidencia_comment_view` AS SELECT
 1 AS `comment_id`,
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `u_id`,
 1 AS `descripcion`,
 1 AS `fecha_alta`,
 1 AS `user_name`,
 1 AS `alias`
;
DROP TABLE IF EXISTS `didekin`.`incidencia_importancia`;
CREATE TABLE `didekin`.`incidencia_importancia` (
`incid_id` int(10) unsigned NOT NULL,
`c_id` int(10) unsigned NOT NULL,
`u_id` int(10) unsigned NOT NULL,
`importancia` smallint(5) unsigned NOT NULL,
`fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
PRIMARY KEY (`incid_id`,`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
DROP VIEW IF EXISTS `didekin`.`incidencia_comunidad_view`;
CREATE VIEW `didekin`.`incidencia_comunidad_view` AS SELECT
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `user_name`,
 1 AS `descripcion`,
 1 AS `ambito`,
 1 AS `fecha_alta`,
 1 AS `fecha_cierre`,
 1 AS `tipo_via`,
 1 AS `nombre_via`,
 1 AS `numero`,
 1 AS `sufijo_numero`
;
USE `didekin`;
ALTER TABLE `didekin`.`incidencia_importancia` ADD KEY `id_parent_incidencia_comunidad` (`incid_id`);
ALTER TABLE `didekin`.`incidencia_importancia` ADD KEY `id_parent_usercomu` (`c_id`,`u_id`);
ALTER TABLE `didekin`.`incidencia_importancia` ADD CONSTRAINT `incidencia_importancia_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `didekin`.`incidencia_importancia` ADD CONSTRAINT `incidencia_importancia_ibfk_2` FOREIGN KEY (`c_id`, `u_id`) REFERENCES `usuario_comunidad` (`c_id`, `u_id`) ON DELETE CASCADE ON UPDATE CASCADE;
DROP TABLE IF EXISTS `didekin`.`incidencia_res_avance`;
CREATE TABLE `didekin`.`incidencia_res_avance` (
`avance_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`incid_id` int(10) unsigned NOT NULL,
`user_name` varchar(60) NOT NULL,
`descripcion` varchar(250) NOT NULL,
`fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
PRIMARY KEY (`avance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`incidencia_res_avance` ADD KEY `user_name_author` (`user_name`);
DROP TABLE IF EXISTS `didekin`.`incidencia_resolucion`;
ALTER TABLE `didekin`.`incidencia_res_avance` ADD KEY `id_parent_incidencia` (`incid_id`);
CREATE TABLE `didekin`.`incidencia_resolucion` (
`incid_id` int(10) unsigned NOT NULL,
`user_name` varchar(60) NOT NULL,
`plan` varchar(300) NOT NULL,
`coste_estimado` int(11) DEFAULT NULL,
`fecha_prevista` timestamp NULL DEFAULT NULL,
`coste` int(11) DEFAULT NULL,
`moraleja` varchar(250) DEFAULT NULL,
`fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
PRIMARY KEY (`incid_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
ALTER TABLE `didekin`.`incidencia_res_avance` ADD CONSTRAINT `incidencia_res_avance_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE;
USE `didekin`;
ALTER TABLE `didekin`.`incidencia_resolucion` ADD KEY `id_parent_incidencia_comunidad` (`incid_id`);
ALTER TABLE `didekin`.`incidencia_resolucion` ADD CONSTRAINT `incidencia_resolucion_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE;
DROP VIEW IF EXISTS `didekin`.`incidencia_user_alta_view`;
CREATE VIEW `didekin`.`incidencia_user_alta_view` AS SELECT
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `descripcion`,
 1 AS `ambito`,
 1 AS `fecha_alta`,
 1 AS `fecha_cierre`,
 1 AS `user_name`,
 1 AS `alias`,
 1 AS `u_id`
;
DROP TABLE IF EXISTS `didekin`.`municipio`;
CREATE TABLE `didekin`.`municipio` (
`m_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`pr_id` smallint(5) unsigned NOT NULL,
`m_cd` smallint(5) unsigned NOT NULL,
`nombre` varchar(100) NOT NULL,
PRIMARY KEY (`m_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8120 DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`municipio` ADD UNIQUE KEY `pr_id` (`pr_id`,`m_cd`);
ALTER TABLE `didekin`.`municipio` ADD KEY `id_parent_provincia` (`pr_id`);
ALTER TABLE `didekin`.`municipio` ADD CONSTRAINT `municipio_ibfk_1` FOREIGN KEY (`pr_id`) REFERENCES `provincia` (`pr_id`) ON UPDATE CASCADE;
DROP TABLE IF EXISTS `didekin`.`provincia`;
CREATE TABLE `didekin`.`provincia` (
`pr_id` smallint(5) unsigned NOT NULL,
`ca_id` smallint(5) unsigned NOT NULL,
`nombre` varchar(100) NOT NULL,
PRIMARY KEY (`pr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`provincia` ADD KEY `id_parent_comunidad` (`ca_id`);
ALTER TABLE `didekin`.`provincia` ADD CONSTRAINT `provincia_ibfk_1` FOREIGN KEY (`ca_id`) REFERENCES `comunidad_autonoma` (`ca_id`) ON UPDATE CASCADE;
DROP TABLE IF EXISTS `didekin`.`usuario`;
CREATE TABLE `didekin`.`usuario` (
`u_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`alias` varchar(30) NOT NULL,
`password` varchar(125) NOT NULL,
`user_name` varchar(60) NOT NULL,
`gcm_token` varchar(175) DEFAULT NULL,
`token_auth` char(60) DEFAULT NULL,
`fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`usuario` ADD UNIQUE KEY `user_name` (`user_name`);
ALTER TABLE `didekin`.`usuario` ADD UNIQUE KEY `gcm_token` (`gcm_token`);
DROP TABLE IF EXISTS `didekin`.`usuario_comunidad`;
CREATE TABLE `didekin`.`usuario_comunidad` (
`c_id` int(10) unsigned NOT NULL,
`u_id` int(10) unsigned NOT NULL,
`portal` char(10) DEFAULT NULL,
`escalera` char(10) DEFAULT NULL,
`planta` char(10) DEFAULT NULL,
`puerta` char(10) DEFAULT NULL,
`roles` set('adm','pre','pro','inq') NOT NULL,
`fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`c_id`,`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;
USE `didekin`;
ALTER TABLE `didekin`.`usuario_comunidad` ADD KEY `id_parent_com` (`c_id`);
ALTER TABLE `didekin`.`usuario_comunidad` ADD KEY `id_parent_usu` (`u_id`);
ALTER TABLE `didekin`.`usuario_comunidad` ADD CONSTRAINT `usuario_comunidad_ibfk_1` FOREIGN KEY (`c_id`) REFERENCES `comunidad` (`c_id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `didekin`.`usuario_comunidad` ADD CONSTRAINT `usuario_comunidad_ibfk_2` FOREIGN KEY (`u_id`) REFERENCES `usuario` (`u_id`) ON DELETE CASCADE ON UPDATE CASCADE;
DROP VIEW IF EXISTS `didekin`.`usuarios_comunidades_view`;
CREATE VIEW `didekin`.`usuarios_comunidades_view` AS SELECT
 1 AS `user_name`,
 1 AS `u_id`,
 1 AS `alias`,
 1 AS `c_id`,
 1 AS `tipo_via`,
 1 AS `nombre_via`,
 1 AS `numero`,
 1 AS `sufijo_numero`,
 1 AS `fecha_alta`,
 1 AS `portal`,
 1 AS `escalera`,
 1 AS `planta`,
 1 AS `puerta`,
 1 AS `roles`,
 1 AS `pr_id`,
 1 AS `m_cd`,
 1 AS `m_nombre`,
 1 AS `pr_nombre`
;
DROP VIEW IF EXISTS `didekin`.`comunidades_municipio_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`comunidades_municipio_view` AS select `c`.`c_id` AS `c_id`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero`,`c`.`m_id` AS `m_id`,`c`.`fecha_alta` AS `fecha_alta`,`c`.`fecha_mod` AS `fecha_mod`,`m`.`pr_id` AS `pr_id`,`m`.`m_cd` AS `m_cd`,`m`.`nombre` AS `m_nombre`,`pr`.`nombre` AS `pr_nombre`,`ca`.`ca_id` AS `ca_id` from (((`didekin`.`comunidad` `c` join `didekin`.`municipio` `m`) join `didekin`.`provincia` `pr`) join `didekin`.`comunidad_autonoma` `ca` on(((`c`.`m_id` = `m`.`m_id`) and (`m`.`pr_id` = `pr`.`pr_id`) and (`pr`.`ca_id` = `ca`.`ca_id`))))
;
DROP VIEW IF EXISTS `didekin`.`incid_importancia_resolucion_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incid_importancia_resolucion_view` AS select distinct `im`.`incid_id` AS `incid_id`,`im`.`u_id` AS `u_id`,`iuv`.`user_name` AS `user_name`,`iuv`.`alias` AS `alias`,`iuv`.`roles` AS `roles`,`im`.`importancia` AS `importancia`,`im`.`fecha_alta` AS `fecha_alta`,`icv`.`descripcion` AS `descripcion`,`icv`.`ambito` AS `ambito`,`icv`.`fecha_alta` AS `fecha_alta_incidencia`,`icv`.`user_name` AS `incid_user_initiator`,`icv`.`c_id` AS `c_id`,`icv`.`tipo_via` AS `comunidad_tipo_via`,`icv`.`nombre_via` AS `comunidad_nombre_via`,`icv`.`numero` AS `comunidad_numero`,`icv`.`sufijo_numero` AS `comunidad_sufijo`,`re`.`fecha_alta` AS `fecha_alta_resolucion` from (((`didekin`.`incidencia_importancia` `im` join `didekin`.`incid_importancia_user_view` `iuv` on(((`im`.`incid_id` = `iuv`.`incid_id`) and (`im`.`u_id` = `iuv`.`u_id`)))) join `didekin`.`incidencia_comunidad_view` `icv` on((`iuv`.`incid_id` = `icv`.`incid_id`))) left join `didekin`.`incidencia_resolucion` `re` on((`icv`.`incid_id` = `re`.`incid_id`))) where isnull(`icv`.`fecha_cierre`)
;
DROP VIEW IF EXISTS `didekin`.`incid_importancia_user_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incid_importancia_user_view` AS select distinct `im`.`incid_id` AS `incid_id`,`im`.`c_id` AS `c_id`,`im`.`u_id` AS `u_id`,`u`.`user_name` AS `user_name`,`u`.`alias` AS `alias`,`uc`.`roles` AS `roles` from ((`didekin`.`incidencia_importancia` `im` join `didekin`.`usuario_comunidad` `uc` on(((`im`.`u_id` = `uc`.`u_id`) and (`im`.`c_id` = `uc`.`c_id`)))) join `didekin`.`usuario` `u` on((`im`.`u_id` = `u`.`u_id`)))
;
DROP VIEW IF EXISTS `didekin`.`incidencia_avg_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incidencia_avg_view` AS select `didekin`.`incidencia_importancia`.`incid_id` AS `incid_id`,avg(`didekin`.`incidencia_importancia`.`importancia`) AS `importancia_avg` from `didekin`.`incidencia_importancia` group by `didekin`.`incidencia_importancia`.`incid_id`
;
DROP VIEW IF EXISTS `didekin`.`incidencia_comment_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incidencia_comment_view` AS select `ic`.`comment_id` AS `comment_id`,`ic`.`incid_id` AS `incid_id`,`ic`.`c_id` AS `c_id`,`ic`.`u_id` AS `u_id`,`ic`.`descripcion` AS `descripcion`,`ic`.`fecha_alta` AS `fecha_alta`,`u`.`user_name` AS `user_name`,`u`.`alias` AS `alias` from (`didekin`.`incidencia_comment` `ic` join `didekin`.`usuario` `u` on((`ic`.`u_id` = `u`.`u_id`)))
;
DROP VIEW IF EXISTS `didekin`.`incidencia_comunidad_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incidencia_comunidad_view` AS select distinct `ic`.`incid_id` AS `incid_id`,`ic`.`c_id` AS `c_id`,`ic`.`user_name` AS `user_name`,`ic`.`descripcion` AS `descripcion`,`ic`.`ambito` AS `ambito`,`ic`.`fecha_alta` AS `fecha_alta`,`ic`.`fecha_cierre` AS `fecha_cierre`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero` from (`didekin`.`incidencia` `ic` join `didekin`.`comunidad` `c` on((`ic`.`c_id` = `c`.`c_id`)))
;
DROP VIEW IF EXISTS `didekin`.`incidencia_user_alta_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`incidencia_user_alta_view` AS select `i`.`incid_id` AS `incid_id`,`i`.`c_id` AS `c_id`,`i`.`descripcion` AS `descripcion`,`i`.`ambito` AS `ambito`,`i`.`fecha_alta` AS `fecha_alta`,`i`.`fecha_cierre` AS `fecha_cierre`,`i`.`user_name` AS `user_name`,`u`.`alias` AS `alias`,`u`.`u_id` AS `u_id` from (`didekin`.`incidencia` `i` left join `didekin`.`usuario` `u` on((`i`.`user_name` = `u`.`user_name`)))
;
DROP VIEW IF EXISTS `didekin`.`usuarios_comunidades_view`;
CREATE ALGORITHM=UNDEFINED VIEW `didekin`.`usuarios_comunidades_view` AS select `u`.`user_name` AS `user_name`,`u`.`u_id` AS `u_id`,`u`.`alias` AS `alias`,`c`.`c_id` AS `c_id`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero`,`c`.`fecha_alta` AS `fecha_alta`,`cu`.`portal` AS `portal`,`cu`.`escalera` AS `escalera`,`cu`.`planta` AS `planta`,`cu`.`puerta` AS `puerta`,`cu`.`roles` AS `roles`,`m`.`pr_id` AS `pr_id`,`m`.`m_cd` AS `m_cd`,`m`.`nombre` AS `m_nombre`,`pr`.`nombre` AS `pr_nombre` from ((((`didekin`.`usuario` `u` join `didekin`.`usuario_comunidad` `cu`) join `didekin`.`comunidad` `c`) join `didekin`.`municipio` `m`) join `didekin`.`provincia` `pr` on(((`u`.`u_id` = `cu`.`u_id`) and (`cu`.`c_id` = `c`.`c_id`) and (`c`.`m_id` = `m`.`m_id`) and (`m`.`pr_id` = `pr`.`pr_id`))))
;
SET TIME_ZONE=@OLD_TIME_ZONE;
SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT;
SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS;
SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_MODE=@OLD_SQL_MODE;
-- Dump end time: Mon Sep  3 19:01:35 2018
