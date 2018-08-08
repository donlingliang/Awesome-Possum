package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.db.SupportSQLiteStatement
import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.util.Pair
import java.io.IOException
import java.util.*

/**
 * Created by don on 8/7/18
 */
class FrameworkSQLiteDatabase : SupportSQLiteDatabase {

    private val CONFLICT_VALUES = arrayOf("", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE ")
    private val EMPTY_STRING_ARRAY = arrayOfNulls<String>(0)
    private lateinit var mDelegate: SQLiteDatabase

    constructor(mDelegate: SQLiteDatabase) {
        this.mDelegate = mDelegate
    }

    override fun compileStatement(sql: String): SupportSQLiteStatement {
        return FrameworkSQLiteStatement(this.mDelegate.compileStatement(sql))
    }

    override fun beginTransaction() {
        this.mDelegate.beginTransaction()
    }

    override fun beginTransactionNonExclusive() {
        this.mDelegate.beginTransactionNonExclusive()
    }

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {
        this.mDelegate.beginTransactionWithListener(transactionListener)
    }

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {
        this.mDelegate.beginTransactionWithListenerNonExclusive(transactionListener)
    }

    override fun endTransaction() {
        this.mDelegate.endTransaction()
    }

    override fun setTransactionSuccessful() {
        this.mDelegate.setTransactionSuccessful()
    }

    override fun inTransaction(): Boolean {
        return this.mDelegate.inTransaction()
    }

    override fun isDbLockedByCurrentThread(): Boolean {
        return this.mDelegate.isDbLockedByCurrentThread
    }

    override fun yieldIfContendedSafely(): Boolean {
        return this.mDelegate.yieldIfContendedSafely()
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean {
        return this.mDelegate.yieldIfContendedSafely(sleepAfterYieldDelay)
    }

    override fun getVersion(): Int {
        return this.mDelegate.version
    }

    override fun setVersion(version: Int) {
        this.mDelegate.version = version
    }

    override fun getMaximumSize(): Long {
        return this.mDelegate.maximumSize
    }

    override fun setMaximumSize(numBytes: Long): Long {
        return this.mDelegate.setMaximumSize(numBytes)
    }

    override fun getPageSize(): Long {
        return this.mDelegate.pageSize
    }

    override fun setPageSize(numBytes: Long) {
        this.mDelegate.pageSize = numBytes
    }

    override fun query(query: String): Cursor {
        return this.query(SimpleSQLiteQuery(query) as SupportSQLiteQuery)
    }

    override fun query(query: String, bindArgs: Array<Any>): Cursor {
        return this.query(SimpleSQLiteQuery(query, bindArgs) as SupportSQLiteQuery)
    }

    override fun query(supportQuery: SupportSQLiteQuery): Cursor {
        return this.mDelegate.rawQueryWithFactory({ sqLiteDatabase, sqLiteCursorDriver, s, sqLiteQuery ->
            supportQuery.bindTo(FrameworkSQLiteProgram(sqLiteQuery))
            SQLiteCursor(sqLiteCursorDriver, s, sqLiteQuery)
        }, supportQuery.sql, EMPTY_STRING_ARRAY, null as String?)
    }

    @RequiresApi(api = 16)
    override fun query(supportQuery: SupportSQLiteQuery, cancellationSignal: CancellationSignal): Cursor {
        return this.mDelegate.rawQueryWithFactory({ sqLiteDatabase, sqLiteCursorDriver, s, sqLiteQuery ->
            supportQuery.bindTo(FrameworkSQLiteProgram(sqLiteQuery))
            SQLiteCursor(sqLiteCursorDriver, s, sqLiteQuery)
        }, supportQuery.sql, EMPTY_STRING_ARRAY, null as String?, cancellationSignal)
    }

    @Throws(SQLException::class)
    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long {
        return this.mDelegate.insertWithOnConflict(table, null as String?, values, conflictAlgorithm)
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<Any>): Int {
        val query = "DELETE FROM " + table + if (isEmpty(whereClause)) "" else " WHERE $whereClause"
        val statement = this.compileStatement(query)
        SimpleSQLiteQuery.bind(statement, whereArgs)
        return statement.executeUpdateDelete()
    }

    override fun update(table: String, conflictAlgorithm: Int, values: ContentValues?, whereClause: String, whereArgs: Array<Any>?): Int {
        if (values != null && values.size() != 0) {
            val sql = StringBuilder(120)
            sql.append("UPDATE ")
            sql.append(CONFLICT_VALUES[conflictAlgorithm])
            sql.append(table)
            sql.append(" SET ")
            val setValuesSize = values.size()
            val bindArgsSize = if (whereArgs == null) setValuesSize else setValuesSize + whereArgs.size
            val bindArgs = arrayOfNulls<Any>(bindArgsSize)
            var i = 0
            val var11 = values.keySet().iterator()

            while (var11.hasNext()) {
                val colName = var11.next() as String
                sql.append(if (i > 0) "," else "")
                sql.append(colName)
                bindArgs[i++] = values.get(colName)
                sql.append("=?")
            }

            if (whereArgs != null) {
                i = setValuesSize
                while (i < bindArgsSize) {
                    bindArgs[i] = whereArgs[i - setValuesSize]
                    ++i
                }
            }

            if (!isEmpty(whereClause)) {
                sql.append(" WHERE ")
                sql.append(whereClause)
            }

            val stmt = this.compileStatement(sql.toString())
            SimpleSQLiteQuery.bind(stmt, bindArgs)
            return stmt.executeUpdateDelete()
        } else {
            throw IllegalArgumentException("Empty values")
        }
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String) {
        this.mDelegate.execSQL(sql)
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String, bindArgs: Array<Any>) {
        this.mDelegate.execSQL(sql, bindArgs)
    }

    override fun isReadOnly(): Boolean {
        return this.mDelegate.isReadOnly
    }

    override fun isOpen(): Boolean {
        return this.mDelegate.isOpen
    }

    override fun needUpgrade(newVersion: Int): Boolean {
        return this.mDelegate.needUpgrade(newVersion)
    }

    override fun getPath(): String {
        return this.mDelegate.path
    }

    override fun setLocale(locale: Locale) {
        this.mDelegate.setLocale(locale)
    }

    override fun setMaxSqlCacheSize(cacheSize: Int) {
        this.mDelegate.setMaxSqlCacheSize(cacheSize)
    }

    @RequiresApi(api = 16)
    override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
        this.mDelegate.setForeignKeyConstraintsEnabled(enable)
    }

    override fun enableWriteAheadLogging(): Boolean {
        return this.mDelegate.enableWriteAheadLogging()
    }

    @RequiresApi(api = 16)
    override fun disableWriteAheadLogging() {
        this.mDelegate.disableWriteAheadLogging()
    }

    @RequiresApi(api = 16)
    override fun isWriteAheadLoggingEnabled(): Boolean {
        return this.mDelegate.isWriteAheadLoggingEnabled
    }

    override fun getAttachedDbs(): List<Pair<String, String>> {
        return this.mDelegate.attachedDbs
    }

    override fun isDatabaseIntegrityOk(): Boolean {
        return this.mDelegate.isDatabaseIntegrityOk
    }

    @Throws(IOException::class)
    override fun close() {
        this.mDelegate.close()
    }

    private fun isEmpty(input: String?): Boolean {
        return input == null || input.length == 0
    }
}