begin;

-- beards
INSERT INTO layers
(
  id,
  "name",
  description,
  "type",
  source,
  path,
  extents,
  minlatitude,
  minlongitude,
  maxlatitude,
  maxlongitude,
  notes,
  enabled,
  displayname,
  displaypath,
  scale,
  environmentalvaluemin,
  environmentalvaluemax,
  environmentalvalueunits,
  lookuptablepath,
  metadatapath,
  classification1,
  classification2,
  uid,
  mddatest,
  citation_date,
  datalang,
  mdhrlv,
  respparty_role,
  licence_level,
  licence_link,
  licence_notes,
  source_link
)
VALUES (
932,
'world', 
'world', 
'Contextual', 
NULL, 
'/data/ala/data/shapefiles/gadm_v1_lev0_shp', 
NULL, 
-35.1, 
112.9, 
-13.7, 
129, 
NULL, 
true,
'world', 
'http://spatial-dev.ala.org.au/geoserver/gwc/service/wms?service=WMS&version=1.1.0&request=GetMap&layers=ALA:world&format=image/png&styles=', 
NULL, 
NULL, 
NULL, 
NULL, 
NULL, 
'http://www.fixme.com', 
'Political',
'', 
'932', 
'2011-08', 
'2011', 
'eng', 
NULL, 
NULL,
2,
NULL,
NULL,
'http://www.fixme.com'
);

commit;

