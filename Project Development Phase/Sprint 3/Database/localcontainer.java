package com.example.containmentzonealertingapplication

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.containmentzonealertingapplication.extras.LogTags
import com.example.containmentzonealertingapplication.VisitedLocations
import com.example.containmentzonealertingapplication.VisitedLocationsDao
import com.example.containmentzonealertingapplication.VisitedLocationsDatabase

object LocalDBContainer {
    /*
    fit location in container
    insert to local DB
     */
    private var database: VisitedLocationsDatabase? = null
    private var visitedLocationsDao: VisitedLocationsDao? = null

    // container based on current position
    private var diagonalRangePoint: MutableList<String> = ArrayList()
    fun addToLocalDB(location: Location, dateTime: String, context: Context?) {

        // get the current container
        calculateContainer(location.latitude, location.longitude, "Bangladesh")

        // now send container and dateTime to RoomDB

        // get the database config stuff
        database = VisitedLocationsDatabase.getDatabase(context)
        visitedLocationsDao = database.visitedLocationsDao()
        val visitedLocationList: MutableList<VisitedLocations> = ArrayList<VisitedLocations>()
        for (drp in diagonalRangePoint) {

            // format = "lat1,lon1,lat2,lon2_dateTime"
            val conatainerDateTimeComposite = drp + "_" + dateTime
            visitedLocationList.add(
                VisitedLocations(conatainerDateTimeComposite, 1)
            )
        }
        Log.d(
            LogTags.LocalDBContainer_TAG,
            """
                addToLocalDB: db entry list size = ${visitedLocationList.size}
                
                
                """.trimIndent()
        )

        // insert to db in a separate thread
        database.databaseWriteExecutor.execute(Runnable {
            for (entry in visitedLocationList) {
                // insert/update for each entry
                try {
                    // try to insert to db
                    visitedLocationsDao.insertLocations(entry)
                    Log.d(LogTags.LocalDBContainer_TAG, "run: room entry created")
                } catch (e: Exception) {
                    // entry already exists, update count
                    visitedLocationsDao.update(entry.getConatainerDateTimeComposite())
                    Log.d(LogTags.LocalDBContainer_TAG, "run: room entry updated")
                }
            }
        })
    }

    fun calculateContainer(lat: Double, lon: Double, country: String): List<String> {
        var latDevider = 0.000000
        var lonDevider = 0.000000
        val latX: Double
        val lony: Double

        // reset the previous list
        diagonalRangePoint = ArrayList()

        // this is so nice
        if (country == "Bangladesh") {
            latDevider = .0002000
            lonDevider = .0002000
        }
        latX = Math.floor(lat / latDevider) * latDevider
        lony = Math.floor(lon / lonDevider) * lonDevider
        //upper left            upper right
        val boxA_X: Double
        val boxA_Y: Double
        val boxC_X: Double
        val boxC_Y: Double //upper box
        boxA_X = latX //#### C
        boxA_Y = lony //left           // #  #   right box(x,y)
        boxC_X = latX + latDevider //  #  #
        boxC_Y = lony + lonDevider //(A)####
        //    #  #   lower
        diagonalRangePoint.add(
            checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                java.lang.Double.toString(boxA_Y)
            ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                java.lang.Double.toString(boxC_Y)
            )
        )
        if (lat - boxA_X < latDevider / 4) {
            //left box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxA_X - latDevider)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y)
                )
            )
        } else if (boxC_X - lat < latDevider / 4) {
            //right box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X + latDevider)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y)
                )
            )
        }
        if (lon - boxA_Y < latDevider / 4) {
            //lower box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y - lonDevider)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y)
                )
            )
        } else if (boxC_Y - lon < lonDevider / 4) {
            //Upper box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y + lonDevider)
                )
            )
        }
        if (boxC_X - lat < latDevider / 4 && boxC_Y - lon < lonDevider / 4) {
            //Upper Right  box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X + latDevider)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y + lonDevider)
                )
            )
        } else if (lat - boxA_X < latDevider / 4 && lon - boxA_Y < lonDevider / 4) {
            //Lower left box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxA_X - latDevider)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y - lonDevider)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y)
                )
            )
        } else if (lat - boxA_X < latDevider / 4 && boxC_Y - lon < lonDevider / 4) {
            //Upper Left  box's diagonal points are to be inserted
            diagonalRangePoint.add(
                java.lang.Double.toString(boxA_X - latDevider) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxA_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxC_Y + latDevider)
                )
            )
        } else if (boxC_X - lat < latDevider / 4 && lon - boxA_Y < lonDevider / 4) {
            //Lower Right  box's diagonal points are to be inserted
            diagonalRangePoint.add(
                checkLatLongLength(java.lang.Double.toString(boxC_X)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y - lonDevider)
                ) + "," + checkLatLongLength(java.lang.Double.toString(boxC_X + latDevider)) + "," + checkLatLongLength(
                    java.lang.Double.toString(boxA_Y)
                )
            )
        }
        Log.d(
            LogTags.LocalDBContainer_TAG,
            "calculateContainer: diagonalPoints size = " + diagonalRangePoint.size
        )
        return diagonalRangePoint
    }

    //This method keeps the lenght of the String same all the time
    private fun checkLatLongLength(latLonDigits: String): String {
        var latLonDigits = latLonDigits
        var index: Int
        val len = latLonDigits.length
        val decimalPointIndex = latLonDigits.indexOf('.')
        val checkRequiredDigits = len - decimalPointIndex - 1
        if (checkRequiredDigits < 6) {
            index = checkRequiredDigits
            while (index < 6) {
                latLonDigits = latLonDigits + "0"
                index++
            }
        } else if (checkRequiredDigits > 6) {
            return latLonDigits.substring(0, len - checkRequiredDigits + 6)
        }
        return latLonDigits
    }
}