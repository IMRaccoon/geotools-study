> reference: https://docs.geotools.org/stable/userguide/tutorial/geometry/geometrycrs.html  
> study code: [CRSLab.kt](../src/main/kotlin/org/geotools/tutorial/crs/CRSLab.kt)

## 0. Pre-requisites

- Add geotools dependencies to your project
    - gt-shapefile
    - gt-swing
    - gt-epsg-hsql
- need to [download gis data](http://udig.refractions.net/docs/data-v1_2.zip)

# Purpose: CRS Lab Application

## Get Resources from shapefile

- Get `FeatureSource` from FileDataStore by shapefile.
- To create `JMapFrame`, need to create `MapContent` and add `FeatureSource` to it.
    - `FeatureLayer` will display the feature (from shapefile) on the map.
    - `FeatureLayer` need `FeatureSource` (to get features) and `Style` (to render features)
    - `Style` created with schema (a.k.a. featureType) from `SLD`
    - finally, add `FeatureLayer` to `MapContent`
- Create `JMapFrame` with `MapContent` and add JButtons.
    - one is for Geometry Validator
    - another is for Export shapefile.

## Geometry Validator

- Get features from `FeatureStore`
- Validate each feature's geometry by `FeatureVisitor`
- If geometry is invalid, then count it and returns.

## Export shapefile

- (after change CRS by application) To export shapefile, first, need to `ShapefileDataStore`
    - create by `ShapefileDataStoreFactory.createDataStore` with `shp` file path
    - FeatureType should be same as original shapefile excepts CRS.
    - So, retype `FeatureType` with new CRS by `SimpleFeatureTypeBuilder` (and set createSchema in DataStore)
- Get `FeatureWriter` from `DataStore.getFeatureWriterAppend` method to write features to shapefile.
    - Before writing, need to create `Transaction`
    - Write features to shapefile by `FeatureWriter` and commit transaction.
    - During copy, transform original geometry to new CRS by `JTS.transform` method.
    - finally, close `FeatureWriter` and `Transaction`.
