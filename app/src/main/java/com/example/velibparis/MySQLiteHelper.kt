package com.example.velibparis

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.example.velibparis.MainActivity.StationDeVelib


// source https://vogella.developpez.com/tutoriels/android/utilisation-base-donnees-sqlite/
class MySQLiteHelper(context: Context?) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    companion object {
        const val TABLE_NAME = "stations"
        const val STATION_ID = "id"
        const val STATION_NAME = "name"
        const val STATION_NBR_BIKE = "nbr_bike"
        const val STATION_COORDINATES = "coordinates"
        private const val DATABASE_NAME =  "${TABLE_NAME} .db"
        private const val DATABASE_VERSION = 1

        // Création strucutre bdd
        private const val DATABASE_CREATE = ("create table  ${TABLE_NAME} (" +
                " ${STATION_ID} varchar(255), " +
                " ${STATION_NAME} varchar(255) not null," +
                " ${STATION_NBR_BIKE} integer not null," +
                " ${STATION_COORDINATES} varchar(255) not null);")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DATABASE_CREATE)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        Log.w(
            MySQLiteHelper::class.java.name,
            "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data"
        )
        db.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
        onCreate(db)
    }

    fun getAllStations(): Cursor? {
        val db = this.readableDatabase
        //SELECT * FROM stations
        return db.query("stations", null, null, null, null, null, null)
    }

    fun insertStation(station : StationDeVelib) {
        val db = this.readableDatabase
        db.execSQL("INSERT or replace INTO stations(id, name, nbr_bike, coordinates) " +
                "VALUES( \"${station.stationId}\", \"${station.lieu}\", ${station.nbVelib}, \"${station.gps.toString()}\")")
    }


}
