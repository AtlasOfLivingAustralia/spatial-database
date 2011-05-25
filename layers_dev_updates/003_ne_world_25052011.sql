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
922,
'ne_world', 
'Natural Earth World Boundaries', 
'Contextual', 
'Natural Earth', 
'/data/ala/data/shapefiles/10m-admin-0-countries', 
NULL, 
-90, 
-180, 
90, 
180, 
'Country boundary data provided by natural earth', 
true, 
'Country Boundaries', 
'http://spatial-dev.ala.org.au/geoserver/gwc/service/wms?service=WMS&version=1.1.0&request=GetMap&layers=ALA:ne_world&format=image/png&styles=', 
NULL, 
'', 
'', 
'', 
'', 
'http://www.naturalearthdata.com/downloads/10m-cultural-vectors/10m-admin-0-countries/', 
'Political', 
'', 
'922', 
'2010-05', 
'2010', 
'eng', 
NULL, 
NULL, 
2,
'http://creativecommons.org/publicdomain/',
'Made with Natural Earth. Free vector and raster map data @ naturalearthdata.com.',
'http://www.naturalearthdata.com'
);
