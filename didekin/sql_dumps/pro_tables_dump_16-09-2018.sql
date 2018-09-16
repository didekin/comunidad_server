-- MySQL dump 10.13  Distrib 5.7.23, for osx10.13 (x86_64)
--
-- Host: 127.0.0.1    Database: didekin
-- ------------------------------------------------------
-- Server version	5.7.17-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `didekin`
--

/*!40000 DROP DATABASE IF EXISTS `didekin`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `didekin` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `didekin`;

--
-- Table structure for table `ambito_incidencia`
--

DROP TABLE IF EXISTS `ambito_incidencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ambito_incidencia` (
  `ambito_id` smallint(5) unsigned NOT NULL,
  `ambito_ES` varchar(100) NOT NULL,
  PRIMARY KEY (`ambito_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comunidad`
--

DROP TABLE IF EXISTS `comunidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comunidad` (
  `c_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `tipo_via` varchar(25) NOT NULL,
  `nombre_via` varchar(150) NOT NULL,
  `numero` smallint(5) unsigned NOT NULL,
  `sufijo_numero` char(10) DEFAULT NULL,
  `m_id` int(10) unsigned NOT NULL,
  `fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`c_id`),
  UNIQUE KEY `tipo_via` (`tipo_via`,`nombre_via`,`numero`,`sufijo_numero`,`m_id`),
  KEY `id_parent_municipio` (`m_id`),
  CONSTRAINT `comunidad_ibfk_1` FOREIGN KEY (`m_id`) REFERENCES `municipio` (`m_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comunidad_autonoma`
--

DROP TABLE IF EXISTS `comunidad_autonoma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comunidad_autonoma` (
  `ca_id` smallint(5) unsigned NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`ca_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `comunidades_municipio_view`
--

DROP TABLE IF EXISTS `comunidades_municipio_view`;
/*!50001 DROP VIEW IF EXISTS `comunidades_municipio_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `comunidades_municipio_view` AS SELECT 
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
 1 AS `ca_id`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `incid_importancia_resolucion_view`
--

DROP TABLE IF EXISTS `incid_importancia_resolucion_view`;
/*!50001 DROP VIEW IF EXISTS `incid_importancia_resolucion_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incid_importancia_resolucion_view` AS SELECT 
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
 1 AS `fecha_alta_resolucion`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `incid_importancia_user_view`
--

DROP TABLE IF EXISTS `incid_importancia_user_view`;
/*!50001 DROP VIEW IF EXISTS `incid_importancia_user_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incid_importancia_user_view` AS SELECT 
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `u_id`,
 1 AS `user_name`,
 1 AS `alias`,
 1 AS `roles`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `incidencia`
--

DROP TABLE IF EXISTS `incidencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incidencia` (
  `incid_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `c_id` int(10) unsigned NOT NULL,
  `user_name` varchar(60) NOT NULL,
  `descripcion` varchar(300) NOT NULL,
  `ambito` smallint(5) unsigned NOT NULL,
  `fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  `fecha_cierre` timestamp(2) NULL DEFAULT NULL,
  PRIMARY KEY (`incid_id`),
  KEY `incid_id` (`incid_id`,`c_id`),
  KEY `id_parent_comunidad` (`c_id`),
  KEY `id_parent_ambito` (`ambito`),
  CONSTRAINT `incidencia_ibfk_1` FOREIGN KEY (`c_id`) REFERENCES `comunidad` (`c_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidencia_ibfk_2` FOREIGN KEY (`ambito`) REFERENCES `ambito_incidencia` (`ambito_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `incidencia_avg_view`
--

DROP TABLE IF EXISTS `incidencia_avg_view`;
/*!50001 DROP VIEW IF EXISTS `incidencia_avg_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incidencia_avg_view` AS SELECT 
 1 AS `incid_id`,
 1 AS `importancia_avg`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `incidencia_comment`
--

DROP TABLE IF EXISTS `incidencia_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incidencia_comment` (
  `comment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `incid_id` int(10) unsigned NOT NULL,
  `c_id` int(10) unsigned NOT NULL,
  `u_id` int(10) unsigned NOT NULL,
  `descripcion` varchar(250) NOT NULL,
  `fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (`comment_id`),
  KEY `id_parent_incidencia` (`incid_id`),
  KEY `id_parent_usercomu` (`c_id`,`u_id`),
  CONSTRAINT `incidencia_comment_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidencia_comment_ibfk_2` FOREIGN KEY (`c_id`, `u_id`) REFERENCES `usuario_comunidad` (`c_id`, `u_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `incidencia_comment_view`
--

DROP TABLE IF EXISTS `incidencia_comment_view`;
/*!50001 DROP VIEW IF EXISTS `incidencia_comment_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incidencia_comment_view` AS SELECT 
 1 AS `comment_id`,
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `u_id`,
 1 AS `descripcion`,
 1 AS `fecha_alta`,
 1 AS `user_name`,
 1 AS `alias`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `incidencia_comunidad_view`
--

DROP TABLE IF EXISTS `incidencia_comunidad_view`;
/*!50001 DROP VIEW IF EXISTS `incidencia_comunidad_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incidencia_comunidad_view` AS SELECT 
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
 1 AS `sufijo_numero`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `incidencia_importancia`
--

DROP TABLE IF EXISTS `incidencia_importancia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incidencia_importancia` (
  `incid_id` int(10) unsigned NOT NULL,
  `c_id` int(10) unsigned NOT NULL,
  `u_id` int(10) unsigned NOT NULL,
  `importancia` smallint(5) unsigned NOT NULL,
  `fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (`incid_id`,`u_id`),
  KEY `id_parent_incidencia_comunidad` (`incid_id`),
  KEY `id_parent_usercomu` (`c_id`,`u_id`),
  CONSTRAINT `incidencia_importancia_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidencia_importancia_ibfk_2` FOREIGN KEY (`c_id`, `u_id`) REFERENCES `usuario_comunidad` (`c_id`, `u_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidencia_res_avance`
--

DROP TABLE IF EXISTS `incidencia_res_avance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incidencia_res_avance` (
  `avance_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `incid_id` int(10) unsigned NOT NULL,
  `user_name` varchar(60) NOT NULL,
  `descripcion` varchar(250) NOT NULL,
  `fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (`avance_id`),
  KEY `user_name_author` (`user_name`),
  KEY `id_parent_incidencia` (`incid_id`),
  CONSTRAINT `incidencia_res_avance_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidencia_resolucion`
--

DROP TABLE IF EXISTS `incidencia_resolucion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incidencia_resolucion` (
  `incid_id` int(10) unsigned NOT NULL,
  `user_name` varchar(60) NOT NULL,
  `plan` varchar(300) NOT NULL,
  `coste_estimado` int(11) DEFAULT NULL,
  `fecha_prevista` timestamp NULL DEFAULT NULL,
  `coste` int(11) DEFAULT NULL,
  `moraleja` varchar(250) DEFAULT NULL,
  `fecha_alta` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (`incid_id`),
  KEY `id_parent_incidencia_comunidad` (`incid_id`),
  CONSTRAINT `incidencia_resolucion_ibfk_1` FOREIGN KEY (`incid_id`) REFERENCES `incidencia` (`incid_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `incidencia_user_alta_view`
--

DROP TABLE IF EXISTS `incidencia_user_alta_view`;
/*!50001 DROP VIEW IF EXISTS `incidencia_user_alta_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `incidencia_user_alta_view` AS SELECT 
 1 AS `incid_id`,
 1 AS `c_id`,
 1 AS `descripcion`,
 1 AS `ambito`,
 1 AS `fecha_alta`,
 1 AS `fecha_cierre`,
 1 AS `user_name`,
 1 AS `alias`,
 1 AS `u_id`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `municipio`
--

DROP TABLE IF EXISTS `municipio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `municipio` (
  `m_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `pr_id` smallint(5) unsigned NOT NULL,
  `m_cd` smallint(5) unsigned NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`m_id`),
  UNIQUE KEY `pr_id` (`pr_id`,`m_cd`),
  KEY `id_parent_provincia` (`pr_id`),
  CONSTRAINT `municipio_ibfk_1` FOREIGN KEY (`pr_id`) REFERENCES `provincia` (`pr_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8120 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_access_token`
--

DROP TABLE IF EXISTS `oauth_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_access_token` (
  `token_id` varchar(100) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(100) NOT NULL,
  `user_name` varchar(100) NOT NULL,
  `client_id` varchar(100) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(100) NOT NULL,
  `fecha_alta` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`authentication_id`),
  UNIQUE KEY `user_name` (`user_name`),
  KEY `refresh_token_index` (`refresh_token`),
  CONSTRAINT `oauth_access_token_ibfk_1` FOREIGN KEY (`user_name`) REFERENCES `usuario` (`user_name`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_refresh_token`
--

DROP TABLE IF EXISTS `oauth_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(100) NOT NULL,
  `token` blob,
  `authentication` blob,
  PRIMARY KEY (`token_id`),
  CONSTRAINT `oauth_refresh_token_ibfk_1` FOREIGN KEY (`token_id`) REFERENCES `oauth_access_token` (`refresh_token`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provincia`
--

DROP TABLE IF EXISTS `provincia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provincia` (
  `pr_id` smallint(5) unsigned NOT NULL,
  `ca_id` smallint(5) unsigned NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`pr_id`),
  KEY `id_parent_comunidad` (`ca_id`),
  CONSTRAINT `provincia_ibfk_1` FOREIGN KEY (`ca_id`) REFERENCES `comunidad_autonoma` (`ca_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuario` (
  `u_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alias` varchar(30) NOT NULL,
  `password` varchar(125) NOT NULL,
  `user_name` varchar(60) NOT NULL,
  `gcm_token` varchar(175) DEFAULT NULL,
  `fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`u_id`),
  UNIQUE KEY `user_name` (`user_name`),
  UNIQUE KEY `gcm_token` (`gcm_token`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario_comunidad`
--

DROP TABLE IF EXISTS `usuario_comunidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuario_comunidad` (
  `c_id` int(10) unsigned NOT NULL,
  `u_id` int(10) unsigned NOT NULL,
  `portal` char(10) DEFAULT NULL,
  `escalera` char(10) DEFAULT NULL,
  `planta` char(10) DEFAULT NULL,
  `puerta` char(10) DEFAULT NULL,
  `roles` set('adm','pre','pro','inq') NOT NULL,
  `fecha_alta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_mod` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`c_id`,`u_id`),
  KEY `id_parent_com` (`c_id`),
  KEY `id_parent_usu` (`u_id`),
  CONSTRAINT `usuario_comunidad_ibfk_1` FOREIGN KEY (`c_id`) REFERENCES `comunidad` (`c_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `usuario_comunidad_ibfk_2` FOREIGN KEY (`u_id`) REFERENCES `usuario` (`u_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `usuarios_comunidades_view`
--

DROP TABLE IF EXISTS `usuarios_comunidades_view`;
/*!50001 DROP VIEW IF EXISTS `usuarios_comunidades_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `usuarios_comunidades_view` AS SELECT 
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
 1 AS `pr_nombre`*/;
SET character_set_client = @saved_cs_client;

--
-- Current Database: `didekin`
--

USE `didekin`;

--
-- Final view structure for view `comunidades_municipio_view`
--

/*!50001 DROP VIEW IF EXISTS `comunidades_municipio_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `comunidades_municipio_view` AS select `c`.`c_id` AS `c_id`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero`,`c`.`m_id` AS `m_id`,`c`.`fecha_alta` AS `fecha_alta`,`c`.`fecha_mod` AS `fecha_mod`,`m`.`pr_id` AS `pr_id`,`m`.`m_cd` AS `m_cd`,`m`.`nombre` AS `m_nombre`,`pr`.`nombre` AS `pr_nombre`,`ca`.`ca_id` AS `ca_id` from (((`comunidad` `c` join `municipio` `m`) join `provincia` `pr`) join `comunidad_autonoma` `ca` on(((`c`.`m_id` = `m`.`m_id`) and (`m`.`pr_id` = `pr`.`pr_id`) and (`pr`.`ca_id` = `ca`.`ca_id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incid_importancia_resolucion_view`
--

/*!50001 DROP VIEW IF EXISTS `incid_importancia_resolucion_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incid_importancia_resolucion_view` AS select distinct `im`.`incid_id` AS `incid_id`,`im`.`u_id` AS `u_id`,`iuv`.`user_name` AS `user_name`,`iuv`.`alias` AS `alias`,`iuv`.`roles` AS `roles`,`im`.`importancia` AS `importancia`,`im`.`fecha_alta` AS `fecha_alta`,`icv`.`descripcion` AS `descripcion`,`icv`.`ambito` AS `ambito`,`icv`.`fecha_alta` AS `fecha_alta_incidencia`,`icv`.`user_name` AS `incid_user_initiator`,`icv`.`c_id` AS `c_id`,`icv`.`tipo_via` AS `comunidad_tipo_via`,`icv`.`nombre_via` AS `comunidad_nombre_via`,`icv`.`numero` AS `comunidad_numero`,`icv`.`sufijo_numero` AS `comunidad_sufijo`,`re`.`fecha_alta` AS `fecha_alta_resolucion` from (((`incidencia_importancia` `im` join `incid_importancia_user_view` `iuv` on(((`im`.`incid_id` = `iuv`.`incid_id`) and (`im`.`u_id` = `iuv`.`u_id`)))) join `incidencia_comunidad_view` `icv` on((`iuv`.`incid_id` = `icv`.`incid_id`))) left join `incidencia_resolucion` `re` on((`icv`.`incid_id` = `re`.`incid_id`))) where isnull(`icv`.`fecha_cierre`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incid_importancia_user_view`
--

/*!50001 DROP VIEW IF EXISTS `incid_importancia_user_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incid_importancia_user_view` AS select distinct `im`.`incid_id` AS `incid_id`,`im`.`c_id` AS `c_id`,`im`.`u_id` AS `u_id`,`u`.`user_name` AS `user_name`,`u`.`alias` AS `alias`,`uc`.`roles` AS `roles` from ((`incidencia_importancia` `im` join `usuario_comunidad` `uc` on(((`im`.`u_id` = `uc`.`u_id`) and (`im`.`c_id` = `uc`.`c_id`)))) join `usuario` `u` on((`im`.`u_id` = `u`.`u_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incidencia_avg_view`
--

/*!50001 DROP VIEW IF EXISTS `incidencia_avg_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incidencia_avg_view` AS select `incidencia_importancia`.`incid_id` AS `incid_id`,avg(`incidencia_importancia`.`importancia`) AS `importancia_avg` from `incidencia_importancia` group by `incidencia_importancia`.`incid_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incidencia_comment_view`
--

/*!50001 DROP VIEW IF EXISTS `incidencia_comment_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incidencia_comment_view` AS select `ic`.`comment_id` AS `comment_id`,`ic`.`incid_id` AS `incid_id`,`ic`.`c_id` AS `c_id`,`ic`.`u_id` AS `u_id`,`ic`.`descripcion` AS `descripcion`,`ic`.`fecha_alta` AS `fecha_alta`,`u`.`user_name` AS `user_name`,`u`.`alias` AS `alias` from (`incidencia_comment` `ic` join `usuario` `u` on((`ic`.`u_id` = `u`.`u_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incidencia_comunidad_view`
--

/*!50001 DROP VIEW IF EXISTS `incidencia_comunidad_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incidencia_comunidad_view` AS select distinct `ic`.`incid_id` AS `incid_id`,`ic`.`c_id` AS `c_id`,`ic`.`user_name` AS `user_name`,`ic`.`descripcion` AS `descripcion`,`ic`.`ambito` AS `ambito`,`ic`.`fecha_alta` AS `fecha_alta`,`ic`.`fecha_cierre` AS `fecha_cierre`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero` from (`incidencia` `ic` join `comunidad` `c` on((`ic`.`c_id` = `c`.`c_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `incidencia_user_alta_view`
--

/*!50001 DROP VIEW IF EXISTS `incidencia_user_alta_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `incidencia_user_alta_view` AS select `i`.`incid_id` AS `incid_id`,`i`.`c_id` AS `c_id`,`i`.`descripcion` AS `descripcion`,`i`.`ambito` AS `ambito`,`i`.`fecha_alta` AS `fecha_alta`,`i`.`fecha_cierre` AS `fecha_cierre`,`i`.`user_name` AS `user_name`,`u`.`alias` AS `alias`,`u`.`u_id` AS `u_id` from (`incidencia` `i` left join `usuario` `u` on((`i`.`user_name` = `u`.`user_name`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `usuarios_comunidades_view`
--

/*!50001 DROP VIEW IF EXISTS `usuarios_comunidades_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `usuarios_comunidades_view` AS select `u`.`user_name` AS `user_name`,`u`.`u_id` AS `u_id`,`u`.`alias` AS `alias`,`c`.`c_id` AS `c_id`,`c`.`tipo_via` AS `tipo_via`,`c`.`nombre_via` AS `nombre_via`,`c`.`numero` AS `numero`,`c`.`sufijo_numero` AS `sufijo_numero`,`c`.`fecha_alta` AS `fecha_alta`,`cu`.`portal` AS `portal`,`cu`.`escalera` AS `escalera`,`cu`.`planta` AS `planta`,`cu`.`puerta` AS `puerta`,`cu`.`roles` AS `roles`,`m`.`pr_id` AS `pr_id`,`m`.`m_cd` AS `m_cd`,`m`.`nombre` AS `m_nombre`,`pr`.`nombre` AS `pr_nombre` from ((((`usuario` `u` join `usuario_comunidad` `cu`) join `comunidad` `c`) join `municipio` `m`) join `provincia` `pr` on(((`u`.`u_id` = `cu`.`u_id`) and (`cu`.`c_id` = `c`.`c_id`) and (`c`.`m_id` = `m`.`m_id`) and (`m`.`pr_id` = `pr`.`pr_id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-09-16 19:35:34
