package by.nalivajr.yana.database

import android.content.*
import android.database.Cursor
import android.net.Uri
import by.nalivajr.yana.database.tools.AtomicBigInteger
import by.nalivajr.yana.exceptions.UnknownTableException
import by.nalivajr.yana.models.DatabaseSchema
import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
abstract class YanaContentProvider(
        private val authority : String
) : ContentProvider() {

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private val codeToTypeMap = HashMap<Int, String>()
    private val codeToTableName = HashMap<Int, String>()
    private val messageIdGenerator: AtomicBigInteger by lazy { maxIdInitializer() }
    private lateinit var dbHelper: YanaSQLiteOpenHelper;

    private fun addUri(tableName: String, code: Int, type: String) {
        uriMatcher.addURI(authority, tableName, code)
        codeToTypeMap.put(code, type)
        codeToTableName.put(code, tableName)
    }

    private fun getTableName(uri: Uri): String {
        val code = uriMatcher.match(uri)
        val table = codeToTableName[code] ?: throw UnknownTableException(uri)
        return table
    }

    override fun onCreate(): Boolean {
        dbHelper = YanaSQLiteOpenHelper(context)

        addUri(DatabaseSchema.Message.TABLE_NAME, DatabaseSchema.Message.CODE_ONE, DatabaseSchema.Message.TYPE_ONE)
        addUri(DatabaseSchema.Message.TABLE_NAME, DatabaseSchema.Message.CODE_MANY, DatabaseSchema.Message.TYPE_MANY)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val database = dbHelper.readableDatabase
        val tableName = getTableName(uri)
        return database.query(tableName, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun getType(uri: Uri): String? {
        val code = uriMatcher.match(uri)
        return codeToTypeMap[code]
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val nextId = messageIdGenerator.incrementAndGet();
        val tableName = getTableName(uri)

        if (tableName == DatabaseSchema.Message.TABLE_NAME) {
            values?.put(DatabaseSchema.Message.MESSAGE_ID, nextId.toString())
        }

        val database = dbHelper.writableDatabase
        val rowId = database.insert(tableName, null, values)
        val newUri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority).appendPath(tableName).build()
        notifyObservers(uri)
        return ContentUris.withAppendedId(newUri, rowId)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val tableName = getTableName(uri)
        val database = dbHelper.writableDatabase
        return database.delete(tableName, selection, selectionArgs)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val tableName = getTableName(uri)
        val database = dbHelper.writableDatabase
        return database.update(tableName, values, selection, selectionArgs)
    }

    private fun maxIdInitializer(): AtomicBigInteger {
        val readableDatabase = dbHelper.readableDatabase;
        var result : AtomicBigInteger;
        readableDatabase.beginTransaction();
        try {
            val maxIdColName = "maxId"
            val cursor = readableDatabase.query(DatabaseSchema.Message.TABLE_NAME,
                    arrayOf("MAX(${DatabaseSchema.Message.MESSAGE_ID}) as $maxIdColName "), null, null, null, null, null);

            if (cursor?.moveToFirst() == false) {
                result = AtomicBigInteger(BigInteger.ZERO)
            } else {
                val id = cursor.getString(cursor.getColumnIndex(maxIdColName))
                result = AtomicBigInteger(BigInteger(id?: "0"));
            }
            readableDatabase.setTransactionSuccessful();
        } finally {
            readableDatabase.endTransaction();
        }
        return result
    }

    private fun notifyObservers(uri: Uri) {
        context!!.contentResolver.notifyChange(uri, null)
    }
}