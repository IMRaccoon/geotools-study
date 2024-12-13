plugins {
    kotlin("jvm") version "1.9.20"
}

group = "org.geotools"
version = "1.0-SNAPSHOT"

val geoToolsVersion = "32.1"

repositories {
    maven("https://repo.osgeo.org/repository/release/")
    mavenCentral()
}


dependencies {
    implementation("org.geotools:gt-shapefile:$geoToolsVersion")
    implementation("org.geotools:gt-swing:$geoToolsVersion")
    implementation("org.geotools:gt-epsg-hsql:$geoToolsVersion")
    implementation("org.geotools:gt-xml:$geoToolsVersion")
    implementation("org.geotools.jdbc:gt-jdbc-postgis:$geoToolsVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
