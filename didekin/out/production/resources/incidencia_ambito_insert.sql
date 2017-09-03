LOAD DATA INFILE
	'/Users/pedro/Documents/git_projects/didekindroid/administracion/ficheros_auxiliares/ambito_incidencia.csv'
INTO TABLE didekin.ambito_incidencia
FIELDS TERMINATED BY ':'
IGNORE 0 LINES
(ambito_id,ambito_ES);