package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback
import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.support.annotation.RequiresApi


/**
 * Created by don on 8/7/18
 */
class CustomSQLiteOpenHelper : SupportSQLiteOpenHelper {

    private val mDelegate: CustomSQLiteOpenHelper.OpenHelper

    constructor(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int, errorHandler: DatabaseErrorHandler, callback: Callback) {
        this.mDelegate = this.createDelegate(context, name, factory, version, errorHandler, callback)
    }

    private fun createDelegate(context: Context, name: String, factory: CursorFactory?, version: Int, errorHandler: DatabaseErrorHandler, callback: Callback):
            CustomSQLiteOpenHelper.OpenHelper {

        return object : CustomSQLiteOpenHelper.OpenHelper(context, name, null, factory, version, errorHandler, null) {

            override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
                if (sqLiteDatabase != null) {
                    this.mWrappedDb = FrameworkSQLiteDatabase(sqLiteDatabase)
                }
                callback.onCreate(this.mWrappedDb)
            }

            override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                callback.onUpgrade(this.getWrappedDb(sqLiteDatabase), oldVersion, newVersion)
            }

            override fun onConfigure(db: SQLiteDatabase) {
                callback.onConfigure(this.getWrappedDb(db))
            }

            override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                callback.onDowngrade(this.getWrappedDb(db), oldVersion, newVersion)
            }

            override fun onOpen(db: SQLiteDatabase) {
                callback.onOpen(this.getWrappedDb(db))
            }
        }
    }

    override fun getDatabaseName(): String {
        return this.mDelegate.getDatabaseName()
    }

    @RequiresApi(api = 16)
    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        this.mDelegate.setWriteAheadLoggingEnabled(enabled)
    }

    override fun getWritableDatabase(): SupportSQLiteDatabase {
        return this.mDelegate.writableSupportDatabase
    }

    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return this.mDelegate.readableSupportDatabase
    }

    override fun close() {
        this.mDelegate.close()
    }

    internal abstract class OpenHelper : CustomSQLiteHelper {

        var mWrappedDb: FrameworkSQLiteDatabase? = null

        constructor(context: Context, name: String?, storageDirectory: String?, factory: CursorFactory?,
                    version: Int, errorHandler: DatabaseErrorHandler, mWrappedDb: FrameworkSQLiteDatabase?)
                : super(context, name, storageDirectory, factory, version, errorHandler) {
            this.mWrappedDb = mWrappedDb
        }

        val writableSupportDatabase: SupportSQLiteDatabase
            get() {
                val db = super.getWritableDatabase()
                return this.getWrappedDb(db)
            }

        val readableSupportDatabase: SupportSQLiteDatabase
            get() {
                val db = super.getReadableDatabase()
                return this.getWrappedDb(db)
            }

        fun getWrappedDb(sqLiteDatabase: SQLiteDatabase): FrameworkSQLiteDatabase {
            if (this.mWrappedDb == null) {
                this.mWrappedDb = FrameworkSQLiteDatabase(sqLiteDatabase)
            }

            return this.mWrappedDb as FrameworkSQLiteDatabase
        }

        @Synchronized
        override fun close() {
            super.close()
            this.mWrappedDb = null
        }
    }
}