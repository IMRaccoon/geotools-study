package org.geotools.tutorial.filter

import org.geotools.api.data.DataStore
import org.geotools.api.data.DataStoreFactorySpi
import org.geotools.api.data.DataStoreFinder
import org.geotools.api.data.Query
import org.geotools.data.postgis.PostgisNGDataStoreFactory
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.filter.text.cql2.CQL
import org.geotools.swing.action.SafeAction
import org.geotools.swing.data.JDataStoreWizard
import org.geotools.swing.table.FeatureCollectionTableModel
import org.geotools.swing.wizard.JWizard
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.DefaultTableModel
import kotlin.jvm.Throws
import kotlin.system.exitProcess

class QueryLab : JFrame() {
    private var dataStore: DataStore? = null
    private val featureTypeCBox: JComboBox<String>
    private val table: JTable
    private val text: JTextField

    init {
        this.defaultCloseOperation = EXIT_ON_CLOSE
        this.contentPane.layout = BorderLayout()

        text = JTextField()
        text.text = "include"       // include selects everything
        this.contentPane.add(text, BorderLayout.NORTH)

        table = JTable()
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.model = DefaultTableModel(5, 5)
        table.preferredScrollableViewportSize = Dimension(500, 200)

        val scrollPane = JScrollPane(table)
        contentPane.add(scrollPane, BorderLayout.CENTER)

        val menubar = JMenuBar()
        this.jMenuBar = menubar

        val fileMenu = JMenu("File")
        menubar.add(fileMenu)

        featureTypeCBox = JComboBox<String>()
        menubar.add(featureTypeCBox)

        val dataMenu = JMenu("Data")
        menubar.add(dataMenu)

        pack()

        addActionsOnFileMenu(fileMenu)
        addActionsOnDataMenu(dataMenu)

        this.isVisible = true
    }

    private fun addActionsOnFileMenu(fileMenu: JMenu) {
        fileMenu.apply {
            add(object : SafeAction("Open shapefile...") {
                override fun action(e: ActionEvent) {
                    connect(ShapefileDataStoreFactory())
                }
            })
            add(object : SafeAction("Connect to PostGIS database...") {
                override fun action(e: ActionEvent) {
                    connect(PostgisNGDataStoreFactory())
                }
            })
            add(object : SafeAction("Connect to DataStore...") {
                override fun action(e: ActionEvent) {
                    connect(null)
                }
            })
            addSeparator()
            add(object : SafeAction("Exit") {
                override fun action(e: ActionEvent) {
                    exitProcess(0)
                }
            })
        }
    }

    private fun addActionsOnDataMenu(dataMenu: JMenu) {
        dataMenu.add(object : SafeAction("Get features") {
            override fun action(e: ActionEvent) {
                filterFeatures()
            }
        })
        dataMenu.add(object : SafeAction("Count") {
            override fun action(e: ActionEvent) {
                countFeatures()
            }
        })
        dataMenu.add(object : SafeAction("Geometry") {
            override fun action(e: ActionEvent) {
                queryFeatures()
            }
        })
    }

    /**
     * File menu actions call this method to connect
     */
    @Throws(Exception::class)
    private fun connect(format: DataStoreFactorySpi?) {
        val wizard = JDataStoreWizard(format)
        val result = wizard.showModalDialog()
        if (result == JWizard.FINISH) {
            val connectionParameters = wizard.connectionParameters
            dataStore = DataStoreFinder.getDataStore(connectionParameters)
            if (dataStore == null) {
                JOptionPane.showMessageDialog(null, "Could not connect - check parameters");
            }
            updateUI()
        }
    }

    /**
     * Helper method to update the combo box used to choose a feature type
     */
    private fun updateUI() {
        val cbm = DefaultComboBoxModel(dataStore?.typeNames)
        featureTypeCBox.model = cbm
        table.model = DefaultTableModel(5, 5)
    }


    @Throws(Exception::class)
    private fun filterFeatures() {
        val typeName = featureTypeCBox.selectedItem as String
        val simpleFeatureSource = dataStore?.getFeatureSource(typeName) ?: throw error("No feature source")

        val filter = CQL.toFilter(text.getText())
        val simpleFeatureCollection = simpleFeatureSource.getFeatures(filter)
        val model = FeatureCollectionTableModel(simpleFeatureCollection)
        table.model = model
    }

    @Throws(Exception::class)
    private fun countFeatures() {
        val typeName = featureTypeCBox.selectedItem as String
        val simpleFeatureSource = dataStore?.getFeatureSource(typeName) ?: throw error("No feature source")

        val filter = CQL.toFilter(text.getText())
        val simpleFeatureCollection = simpleFeatureSource.getFeatures(filter)
        val count = simpleFeatureCollection.size()
        JOptionPane.showMessageDialog(text, "Number of selected features: $count")
    }

    @Throws(Exception::class)
    private fun queryFeatures() {
        val typeName = featureTypeCBox.selectedItem as String
        val simpleFeatureSource = dataStore?.getFeatureSource(typeName) ?: throw error("No feature source")

        val schema = simpleFeatureSource.schema
        val name = schema.geometryDescriptor.localName

        val filter = CQL.toFilter(text.getText())
        val query = Query(typeName, filter, name)

        val simpleFeatureCollection = simpleFeatureSource.getFeatures(query)
        val model = FeatureCollectionTableModel(simpleFeatureCollection)
        table.model = model
    }
}
