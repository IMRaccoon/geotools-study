> reference: https://docs.geotools.org/stable/userguide/tutorial/feature/csv2shp.html    
> study code: [Csv2Shp.kt](../src/main/kotlin/org/geotools/tutorial/feature/Csv2Shp.kt)

## Pre-requisites

- Add geotools dependencies to your project
    - gt-shapefile
    - gt-swing
    - gt-epsg-hsql
- need [csv file](../src/main/resources/tutorial/feature/locations.csv) to convert to shapefile

# Purpose: CSV to SHP

## Define Schema (also called feature type)

- defined Schema for the shapefile

## Read CSV file

- read csv file
- generate feature from single row
    - to create point, need to get latitude and longitude and create point by `GeometryFactory`
    - other properties will be also save into feature
    - add all to `FeatureBuilder` and build feature

## Write to Shapefile

- create `ShapefileDataStore` to get feature source
    - create by `ShapefileDataStoreFactory.createDataStore` with `shp` file path
    - have to create schema
    - get `getFeatureSource` with it's type name
- if `featureSource` is `SimpleFeatureStore`, then we can write to it
    - create `Transaction` and set to `FeatureStore`
    - create `FeatureCollection` and add to `FeatureStore`
    - commit transaction will write to shapefile

## Other Things to try

### Create FeatureType with builder

- create `SimpleFeatureTypeBuilder` and add attributes
    - default properties for featureType: name, crs ...
    - additional properties of feature: geometry, attributes ...
- `buildFeatureType` will create `SimpleFeatureType`

---

## Definitions

> cheat sheet for understanding concept (compares with Java)

| <strong>Java</strong> | <strong>Geospatial</strong> | 
|-----------------------|-----------------------------|
| Object                | Feature                     |
| Class                 | FeatureType                 |
| Field                 | Attribute                   |
| Method                | Operation                   |

### Feature

- Feature is something that can be drawn on a map.
- It's like an object in Java.
    - contains some information about the real world thing that they represent.
    - information is organized into attributes just as in Java information is slotted into fields.
- Feature model is actually considers both attribute and operation to be “properties” of a Feature.

### FeatureClass

- GeoTools has Interfaces for `Feature`, `FeatureType`, `Attribute` provided by the GeoAPI project.
    - It's very common for a Feature to have only simple Attributes (like a Java Primitive Types).
- `Feature` API is similar to how `java.util.Map` is used in Java.

### Geometry

- Biggest difference between a Feature and a Java Object is that a Feature can have a Geometry.
- Location information is stored in a `Geometry` (or shape) that is stored in an attribute.
- To represent a `Geometry`, GeoTools uses the `JTS`(JTS Topology Suite) library.
    - JTS library provide good implementation of `Geometry`.
    - JTS library does all hard graph theory and geometry math to let you work with `Geometry` in a simple and
      productive way.

### DataStore

- `DataStore` API is used to represent a `File`, `Database` or `Service` that has spatial data in it.
- `FeatureSource` is used to read features, the sub-class `FeatureStore` is used for read/write access.
    - Handle write access as a sub-class of `FeatureSource` is a good way to keep the API simple.  
