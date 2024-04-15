package com.fivempk.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPath = context.getDatabasePath(DATABASE_NAME).absolutePath

    init {
        if (!checkDatabase()) {
            copyDatabaseFromAssets(context)
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "public_transport.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Not used as the database is pre-created
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle any upgrades if your database version changes
    }

    fun getAllStops(): List<OfflineStop> {
        val stopsList = mutableListOf<OfflineStop>()
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery("SELECT stop_id, stop_name, latitude, longitude FROM nodes_table", null)
        if (cursor.moveToFirst()) {
            do {
                stopsList.add(OfflineStop(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getDouble(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return stopsList
    }

    private fun checkDatabase(): Boolean {
        val dbFile = File(dbPath)
        return dbFile.exists()
    }

    private fun copyDatabaseFromAssets(context: Context) {
        val myInput = context.assets.open(DATABASE_NAME)
        val outFileName = context.getDatabasePath(DATABASE_NAME).absolutePath

        val outFile = File(outFileName)
        if (!outFile.exists()) {
            outFile.parentFile?.mkdirs()
            val myOutput = FileOutputStream(outFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (myInput.read(buffer).also { length = it } > 0) {
                myOutput.write(buffer, 0, length)
            }

            myOutput.flush()
            myOutput.close()
            myInput.close()
        }
    }
}