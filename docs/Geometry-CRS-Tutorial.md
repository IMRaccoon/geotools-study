> reference: https://docs.geotools.org/stable/userguide/tutorial/geometry/geometrycrs.html  
> study code: [CRSLab.kt](../src/main/kotlin/org/geotools/tutorial/crs/CRSLab.kt)

## Pre-requisites

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

## Other Things to try

### Easy way to fix invalid geometry

- easy place to start validate is to use `geometry.buffer(0)`
- In `export` method, Use a `Query` object to retrieve the features write them to a new shapefile.
    - Instead of transforming the features 'by hand' as we did in the previous example

---

# Geometry

- Geometry is literally the shape of the GIS (Geographic Information System).
- Usually, there is one geometry for a feature. (as a attribute)
- it will help you understand if you consider situations where there are multiple representations of the same thing.
    - e.g. Represent Sydney as a single location (i.e. a point)
    - e.g. Represent Sydney as a city limit (so you can tell when you are inside Sydney) (i.e. a polygon)

## Point

```kotlin
val geometryFactory = JTSFactoryFinder.getGeometryFactory(null)

// Create a point using Well-Known Text (WKT) format.
val reader = WKTReader(geometryFactory)
val pointFromWKT = reader.read("POINT(1 1)") as Point

// Create a `Point` by hand using the GeometryFactory
val coord = Coordinate(1, 1)
val pointFromFactory = geometryFactory.createPoint(coord)
```

## Line

- A `LineString` is a sequence of segments in the same manner.

```kotlin
val geometryFactory = JTSFactoryFinder.getGeometryFactory(null)

// Create a `LineString` using Well-Known Text (WKT) format.
val reader = WKTReader(geometryFactory)
val lineFromWKT = reader.read("LINESTRING(0 2, 2 0, 8 6)") as LineString

// Create a `LineString` by hand using the GeometryFactory
val coords = arrayOf(
    Coordinate(0, 2),
    Coordinate(2, 0),
    Coordinate(8, 6)
)
val lineFromFactory = geometryFactory.createLineString(coords)
```

## Polygon

```kotlin
val geometryFactory = JTSFactoryFinder.getGeometryFactory(null)

// Create a `Polygon` using Well-Known Text (WKT) format.
val reader = WKTReader(geometryFactory)
val polygonFromWKT = reader.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))") as Polygon
```

---

# Coordinate Reference System

- Geometry is a bunch of math (a set of points in teh mathematical sense).
- In order to provide a Geometry with meaning, you need to know what those individual points represent.
- The data structure that tells us where points are located is called a `Coordinate Reference System` (CRS).\
- The Coordinate Reference System defines a couple of concepts
    - It defines the axis used - along with the units of measure
        - latitude measured in degrees from the Equator
        - longitude measured in degrees from the Greenwich meridian
        - x, y can be measured in meters, feet, or any other unit of measure
    - It defines the shape of the world. (but not all CRS imagine the same shape)

## EPSG Codes

- The first group that cared about this was the European Petroleum Survey Group (EPSG).
- Database is distributed by Microsoft Access, and is ported into all kinds of other forms (include gt-hsql jar)
- Popular codes
    - EPSG:4326
        - EPSG Projection 4326 - WGS 84
        - Information measured by latitude/longitude using decimal degrees.
    - EPSG:3785
        - Popular Visualization CRS / Mercator
        - The official code for the “Google map” projection. (also used by a lot of web mapping services)
        - It is nice to pretend the world is a sphere.
    - EPSG:3857
        - Web Mercator
        - Used by Google Maps, Bing Maps, OpenStreetMap, MapQuest, and others.
        - It is a spherical projection, but it is not the same as EPSG:3785.
        - It is a bit more accurate, but it is still a spherical projection.

## Axis Order

- When 'Open Source Geospatial Foundation' (OSGeo) arrived on the scene, maps ware always recording position in
  latitude, longitude order.
    - that is, with the north-south axis first and the east-west axis second.
    - If draw that on a graph, it looks like the world is sideways as the coordinates are in "y/x" to their way of
      thinking
- So, they changed the order of the axis to be longitude, latitude. (x/y)
    - It still makes fight with mapmakers even today.
- the problem is when you see some data in "EPSG:4326", you have no idea order of x and y.
    - So, OSGeo supposed to use 'urn:ogc:def:crs:EPSG:6.6:4326'
    - In code, 'ogc' means Open Geospatial Consortium

### Workarounds

```kotlin
// Create CRS with OGC axis order
val factory = CRS.getAuthorityFactory(true) // true means to use OGC axis order (longitude first)
val crs = factory.createCoordinateReferenceSystem("EPSG:4326")

// Or Set Global hint to force to use x/y order
System.setProperty("org.geotools.referencing.forceXY", "true")
```
