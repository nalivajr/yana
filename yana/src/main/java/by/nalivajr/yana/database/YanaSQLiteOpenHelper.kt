package by.nalivajr.yana.database

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import by.nalivajr.yana.models.DatabaseSchema as DBSchema;

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
open class YanaSQLiteOpenHelper constructor(
        context: Context,
        name : String,
        factory : SQLiteDatabase.CursorFactory?,
        version : Int,
        errorHandler : DatabaseErrorHandler?)
: SQLiteOpenHelper(context, name, factory, version, errorHandler) {

    companion object Constants {
        val DATABASE_NAME = "YanaMessageDB";
        private val CREATION_SQL = "CREATE TABLE " +
                "${DBSchema.Message.TABLE_NAME} (" +
                "${DBSchema.Message.MESSAGE_ID} ${DBSchema.DATA_TYPE_TEXT} UNIQUE, " +
                "${DBSchema.Message.GROUP_ID} ${DBSchema.DATA_TYPE_TEXT}, " +
                "${DBSchema.Message.SENDER_ID} ${DBSchema.DATA_TYPE_TEXT}, " +
                "${DBSchema.Message.RECIPIENT_ID} ${DBSchema.DATA_TYPE_TEXT}, " +
                "${DBSchema.Message.CREATION_DATE} ${DBSchema.DATA_TYPE_INTEGER}, " +
                "${DBSchema.Message.COMMAND} ${DBSchema.DATA_TYPE_TEXT}, " +
                "${DBSchema.Message.ORDER} ${DBSchema.DATA_TYPE_TEXT}, " +
                "${DBSchema.Message.ORDERED} ${DBSchema.DATA_TYPE_INTEGER}, " +
                "${DBSchema.Message.PAYLOAD} ${DBSchema.DATA_TYPE_TEXT} )";
    }

    constructor(context: Context, name : String, factory : SQLiteDatabase.CursorFactory?, version : Int)
        : this (context, name, factory, version, null) {}
    constructor(context: Context) : this (context, DATABASE_NAME, null, 1) {}

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATION_SQL);
    }

    override fun onUpgrade(db : SQLiteDatabase, oldVersion : Int, newVersion : Int) {
        Log.w(YanaSQLiteOpenHelper::class.simpleName, "OnUpgrade method is not implemented");
    }
}