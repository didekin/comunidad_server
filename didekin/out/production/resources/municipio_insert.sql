/*To reset the auto-increment counter*/
ALTER TABLE didekin.municipio AUTO_INCREMENT = 1;

LOAD DATA INFILE
'/Users/pedro/Documents/git_projects/didekindroid/administracion/ficheros_auxiliares/municipio.csv'
  INTO TABLE didekin.municipio
  FIELDS TERMINATED BY ':'
  IGNORE 0 LINES
  (pr_id,m_cd,nombre);
