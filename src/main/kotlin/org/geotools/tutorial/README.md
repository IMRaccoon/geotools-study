reference: https://docs.geotools.org/stable/userguide/tutorial/feature/csv2shp.html

## 0. Pre-requisites

- Add geotools dependencies to your project
    - gt-shapefile
    - gt-swing
    - gt-epsg-hsql
- need csv file to convert to shapefile

## 1. Define Schema (also called feature type)

- defined Schema for the shapefile

## 2. Read CSV file

- read csv file
- generate feature from single row
    - to create point, need to get latitude and longitude and create point by `GeometryFactory`
    - other properties will be also save into feature
    - add all to `FeatureBuilder` and build feature

## 3. Write to Shapefile

- create `ShapefileDataStore` to get feature source
    - create by `ShapefileDataStoreFactory.createDataStore` with `shp` file path
    - have to create schema
    - get `getFeatureSource` with it's type name
- if `featureSource` is `SimpleFeatureStore`, then we can write to it
    - create `Transaction` and set to `FeatureStore`
    - create `FeatureCollection` and add to `FeatureStore`
    - commit transaction will write to shapefile
