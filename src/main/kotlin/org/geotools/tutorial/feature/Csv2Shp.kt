package com.naver.map.org.geotools.tutorial.feature

import org.geotools.api.data.SimpleFeatureSource
import org.geotools.api.data.SimpleFeatureStore
import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.api.feature.simple.SimpleFeatureType
import org.geotools.data.DataUtilities
import org.geotools.data.DefaultTransaction
import org.geotools.data.collection.ListFeatureCollection
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.feature.simple.SimpleFeatureBuilder
import org.geotools.geometry.jts.JTSFactoryFinder
import org.geotools.swing.data.JFileDataStoreChooser
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.swing.UIManager
import kotlin.system.exitProcess

class Csv2Shp {
    companion object {
        /**
         * We use the DataUtilities class to create a FeatureType that will describe the data in our shapefile.
         * See also the createFeatureType method below for another, more flexible approach.
         */
        val TYPE: SimpleFeatureType = DataUtilities.createType(
            "Location",
            "the_geom:Point:srid=4326,"
                + // <- the geometry attribute: Point type
                "name:String,"
                + // <- a String attribute
                "number:Integer" // a number attribute
        )
    }

    init {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
        val file = JFileDataStoreChooser.showOpenFile("csv", null)
        requireNotNull(file) { "No file chosen" }

        println("TYPE: $TYPE")

        /**
         * A list to collect features as we create them.
         */
        val features: MutableList<SimpleFeature> = ArrayList()

        /**
         * GeometryFactory will be used to create the geometry attribute of each feature,
         * using a Point object for the location.
         */
        val geometryFactory: GeometryFactory = JTSFactoryFinder.getGeometryFactory()
        val featureBuilder: SimpleFeatureBuilder = SimpleFeatureBuilder(TYPE)

        val reader = BufferedReader(FileReader(file))
        reader.use { reader ->
            /* First line of the data file is the header */
            var line = reader.readLine()
            println("Header: $line")

            while (reader.readLine().also { line = it } != null) {
                if (line.trim().isNotEmpty()) { // skip blank lines
                    val tokens: List<String> = line.split(",")

                    val latitude: Double = tokens[0].toDouble()
                    val longitude: Double = tokens[1].toDouble()
                    val name: String = tokens[2].trim()
                    val number: Int = tokens[3].trim().toInt()

                    /* Longitude (= x coord) first ! */
                    val point = geometryFactory.createPoint(Coordinate(longitude, latitude))

                    featureBuilder.add(point)
                    featureBuilder.add(name)
                    featureBuilder.add(number)
                    val feature = featureBuilder.buildFeature(null)
                    features.add(feature)
                }
            }
        }


        val newFile = getNewShapeFile(file)
        val dataStoreFactory = ShapefileDataStoreFactory()
        val params: MutableMap<String, Any> = mutableMapOf(
            "url" to newFile.toURI().toURL(),
            "create spatial index" to true
        )
        val newDataStore: ShapefileDataStore = dataStoreFactory.createDataStore(params) as ShapefileDataStore
        /**
         * TYPE is used as a template to describe the file contents
         */
        newDataStore.createSchema(TYPE)


        /**
         * Write the features to the shapefile
         */
        val transaction = DefaultTransaction("create")
        val typeName = newDataStore.typeNames[0]
        val featureSource: SimpleFeatureSource = newDataStore.getFeatureSource(typeName)
        val SHAPE_TYPE: SimpleFeatureType = featureSource.schema

        /**
         * The Shapefile format has a couple limitations:
         * - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
         * - Attribute names are limited in length
         * - Not all data types are supported (example Timestamp represented as Date)
         *
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */
        println("SHAPE: $SHAPE_TYPE")

        if (featureSource is SimpleFeatureStore) {
            /**
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */
            val featureStore = featureSource
            val collection = ListFeatureCollection(TYPE, features)
            featureStore.transaction = transaction

            try {
                featureStore.addFeatures(collection)
                transaction.commit()
            } catch (problem: Exception) {
                problem.printStackTrace()
                transaction.rollback()
            } finally {
                transaction.close()
            }
            exitProcess(0) // success
        } else {
            println("$typeName does not support read/write access")
            exitProcess(1) // fail
        }
    }


    /**
     * Prompt the user for the name and path to use for the output shapefile
     *
     * @param csvFile the input csv file used to create a default shapefile name
     * @return name and path for the shapefile as a new File object
     */
    fun getNewShapeFile(csvFile: File): File {
        val path = csvFile.absolutePath
        val newPath = path.substring(0, path.length - 4) + ".shp"

        val chooser = JFileDataStoreChooser("shp")
        chooser.dialogTitle = "Save shapefile"
        chooser.selectedFile = File(newPath)

        val returnVal = chooser.showSaveDialog(null)
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            // the user cancelled the dialog
            exitProcess(0)
        }

        val newFile = chooser.selectedFile
        if (newFile == csvFile) {
            println("Error: cannot replace $csvFile")
            exitProcess(0)
        }

        return newFile
    }
}

fun main() {
    Csv2Shp()
}

