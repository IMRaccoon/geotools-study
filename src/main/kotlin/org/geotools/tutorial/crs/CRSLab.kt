package org.geotools.tutorial.crs

import org.geotools.api.data.FileDataStore
import org.geotools.api.data.FileDataStoreFinder
import org.geotools.api.data.SimpleFeatureSource
import org.geotools.api.feature.Feature
import org.geotools.api.feature.FeatureVisitor
import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.api.referencing.operation.MathTransform
import org.geotools.api.util.ProgressListener
import org.geotools.data.DefaultTransaction
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.feature.simple.SimpleFeatureTypeBuilder
import org.geotools.geometry.jts.JTS
import org.geotools.map.FeatureLayer
import org.geotools.map.MapContent
import org.geotools.referencing.CRS
import org.geotools.styling.SLD
import org.geotools.swing.JMapFrame
import org.geotools.swing.action.SafeAction
import org.geotools.swing.data.JFileDataStoreChooser
import org.locationtech.jts.geom.Geometry
import java.awt.event.ActionEvent
import java.io.File
import java.io.Serializable
import javax.swing.JButton
import javax.swing.JOptionPane
import kotlin.jvm.Throws

class CRSLab {

    private lateinit var sourceFile: File
    private lateinit var featureSource: SimpleFeatureSource
    private lateinit var map: MapContent

    init {
        this.displayShapefile()
    }

    @Throws(Exception::class)
    fun displayShapefile() {
        sourceFile = this::class.java.getResource("/tutorial/crs/bc_border.shp")?.file?.let(::File)
            ?: throw Exception("File not found")
        val store: FileDataStore = FileDataStoreFinder.getDataStore(sourceFile)
        featureSource = store.featureSource

        // Create a map context and add our shapefile to it
        map = MapContent()
        val style = SLD.createSimpleStyle(featureSource.schema)
        val layer = FeatureLayer(featureSource, style)
        map.layers().add(layer)

        // Create a JMapFrame
        val mapFrame = JMapFrame(map)
        mapFrame.enableToolBar(true)
        mapFrame.enableStatusBar(true)

        val toolBar = mapFrame.toolBar
        toolBar.addSeparator()
        toolBar.add(JButton(ValidateGeometryAction()))
        toolBar.add(JButton(ExportShapefileAction()))

        // Display the map frame. When it is closed the application will exit
        mapFrame.setSize(800, 600)
        mapFrame.isVisible = true
    }

    /**
     * checks the geometry associated with each feature for common problem (such as polygons not having closed boundaries)
     */
    private fun validateFeatureGeometry(progress: ProgressListener?): Int {
        val featureCollection = featureSource.features

        // Rather than use an iterator, create a FeatureVisitor to process the features
        val visitor = object : FeatureVisitor {
            var numInvalidGeometry = 0

            override fun visit(f: Feature) {
                val feature = f as SimpleFeature
                val geom = feature.defaultGeometry as Geometry?
                if (geom != null && !geom.isValid) {
                    numInvalidGeometry++
                    println("Invalid geometry: ${feature.id}")
                }
            }
        }

        featureCollection.accepts(visitor, progress)
        return visitor.numInvalidGeometry
    }


    @Throws(Exception::class)
    private fun exportToShapeFile() {
        val schema = featureSource.schema
        val chooser = JFileDataStoreChooser("shp")
        chooser.dialogTitle = "Save reprojected shapefile"
        chooser.setSaveFile(sourceFile)

        val returnVal = chooser.showSaveDialog(null)
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            return
        }

        val file = chooser.selectedFile
        if (file == sourceFile) {
            JOptionPane.showMessageDialog(null, "Cannot replace $file");
            return
        }

        // set up a math transform used to process the data
        val dataCRS = schema.coordinateReferenceSystem
        val userCRS = map.coordinateReferenceSystem
        val lenient = true // allow for some error due to different datums
        val transform: MathTransform = CRS.findMathTransform(dataCRS, userCRS, lenient)

        val featureCollection = featureSource.getFeatures()

        // To create a new shapefile we will need to produce a FeatureType that is similar to our original.
        // The only difference will be the CoordinateReferenceSystem of the geometry descriptor.
        val factory = ShapefileDataStoreFactory()
        val create = mapOf<String, Serializable>(
            "url" to file.toURI().toURL(),
            "create spatial index" to true
        )
        val dataStore = factory.createDataStore(create)
        val featureType = SimpleFeatureTypeBuilder.retype(schema, userCRS)
        dataStore.createSchema(featureType)

        // Get the name of the new Shapefile, which will be used to open the FeatureWriter
        val createdName = dataStore.typeNames[0]

        // Open an iterator to go through the contents, and a writer to write out the new Shapefile.
        val transaction = DefaultTransaction("Reproject")
        val writer = dataStore.getFeatureWriterAppend(createdName, transaction)
        try {
            writer.use {
                val iterator = featureCollection.features()
                iterator.use {
                    while (iterator.hasNext()) {
                        // copy the contents of each feature and transform the geometry
                        val originalFeature = iterator.next()
                        val feature = writer.next()
                        feature.attributes = originalFeature.attributes

                        val geometry = originalFeature.defaultGeometry as Geometry
                        val newGeometry = JTS.transform(geometry, transform)

                        feature.defaultGeometry = newGeometry
                        writer.write()
                    }
                }
            }
            transaction.commit()
            JOptionPane.showMessageDialog(null, "Export to shapefile complete")
        } catch (e: Exception) {
            e.printStackTrace()
            transaction.rollback()
            JOptionPane.showMessageDialog(null, "Export to shapefile failed")
        } finally {
            transaction.close()
        }
    }

    inner class ValidateGeometryAction : SafeAction("Validate Geometry") {
        init {
            putValue(SHORT_DESCRIPTION, "Check each geometry")
        }

        @Throws(Throwable::class)
        override fun action(e: ActionEvent?) {
            val numInValid = validateFeatureGeometry(null)
            val msg = if (numInValid == 0) {
                "All feature geometries are valid"
            } else {
                "Invalid features: $numInValid"
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results", JOptionPane.INFORMATION_MESSAGE)
        }
    }


    inner class ExportShapefileAction : SafeAction("Export...") {
        init {
            putValue(SHORT_DESCRIPTION, "Export using current crs")
        }

        @Throws(Throwable::class)
        override fun action(e: ActionEvent?) {
            exportToShapeFile()
        }
    }
}
