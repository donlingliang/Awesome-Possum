package codelabs.don.codelabs.room.prepopulate
import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.*
import java.util.*

/**
 * Created by don on 8/7/18
 */
open class CustomSQLiteHelper : SQLiteOpenHelper {

    private val TAG = CustomSQLiteHelper::class.simpleName
    private var ASSET_DB_PATH = "databases"
    private var mContext: Context
    private var mName: String?
    private var mFactory: SQLiteDatabase.CursorFactory?
    private var mNewVersion: Int
    private var mDatabase: SQLiteDatabase?
    private var mIsInitializing: Boolean = false
    private var mDatabasePath: String? = null
    private var mAssetPath: String? = null
    private val mUpgradePathFormat: String
    private var mForcedUpgradeVersion: Int = 0


    constructor (context: Context, name: String?, storageDirectory: String?, factory: SQLiteDatabase.CursorFactory?, version: Int, errorHandler: DatabaseErrorHandler)
            : super(context, name, factory, version, errorHandler){
        this.mDatabase = null
        this.mIsInitializing = false
        this.mForcedUpgradeVersion = 0
        if (version < 1) {
            throw IllegalArgumentException("Version must be >= 1, was $version")
        } else if (name == null) {
            throw IllegalArgumentException("Database name cannot be null")
        } else {
            this.mContext = context
            this.mName = name
            this.mFactory = factory
            this.mNewVersion = version
            this.mAssetPath = "databases/$name"

//            this.mAssetPath = FileHelper.databaseFolder + "/$name"
            if (storageDirectory != null) {
                this.mDatabasePath = storageDirectory
            } else {
                this.mDatabasePath = context.applicationInfo.dataDir + "/databases"
            }

            this.mUpgradePathFormat = "databases/" + name + "_upgrade_%s-%s.sql"
        }
    }

    @Synchronized
    override fun getWritableDatabase(): SQLiteDatabase {
        if (this.mDatabase != null && this.mDatabase!!.isOpen && !this.mDatabase!!.isReadOnly) {
            return this.mDatabase as SQLiteDatabase
        } else if (this.mIsInitializing) {
            throw IllegalStateException("getWritableDatabase called recursively")
        } else {
            var success = false
            var db: SQLiteDatabase? = null

            val var4: SQLiteDatabase
            try {
                this.mIsInitializing = true
                db = this.createOrOpenDatabase(false)
                var version = db!!.version
                if (version != 0 && version < this.mForcedUpgradeVersion) {
                    db = this.createOrOpenDatabase(true)
                    db!!.version = this.mNewVersion
                    version = db.version
                }

                if (version != this.mNewVersion) {
                    db.beginTransaction()

                    try {
                        if (version == 0) {
                            this.onCreate(db)
                        } else {
                            if (version > this.mNewVersion) {
                                Log.w(TAG, "Can't downgrade read-only database from version " + version + " to " + this.mNewVersion + ": " + db.path)
                            }

                            this.onUpgrade(db, version, this.mNewVersion)
                        }

                        db.version = this.mNewVersion
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }

                this.onOpen(db)
                success = true
                var4 = db
            } finally {
                this.mIsInitializing = false
                if (success) {
                    if (this.mDatabase != null) {
                        try {
                            this.mDatabase!!.close()
                        } catch (var15: Exception) {
                        }

                    }

                    this.mDatabase = db
                } else if (db != null) {
                    db.close()
                }

            }

            return var4
        }
    }

    @Synchronized
    override fun getReadableDatabase(): SQLiteDatabase {
        return if (this.mDatabase != null && this.mDatabase!!.isOpen) {
            this.mDatabase as SQLiteDatabase
        } else if (this.mIsInitializing) {
            throw IllegalStateException("getReadableDatabase called recursively")
        } else {
            try {
                this.writableDatabase
            } catch (var8: SQLiteException) {
                if (this.mName == null) {
                    throw var8
                } else {
                    Log.e(TAG, "Couldn't open " + this.mName + " for writing (will try read-only):", var8)
                    var db: SQLiteDatabase? = null

                    val var3: SQLiteDatabase
                    try {
                        this.mIsInitializing = true
                        val path = this.mContext.getDatabasePath(this.mName).path
                        db = SQLiteDatabase.openDatabase(path, this.mFactory, 1)
                        if (db!!.version != this.mNewVersion) {
                            throw SQLiteException("Can't upgrade read-only database from version " + db.version + " to " + this.mNewVersion + ": " + path)
                        }

                        this.onOpen(db)
                        Log.w(TAG, "Opened " + this.mName + " in read-only mode")
                        this.mDatabase = db
                        var3 = this.mDatabase as SQLiteDatabase
                    } finally {
                        this.mIsInitializing = false
                        if (db != null && db != this.mDatabase) {
                            db.close()
                        }

                    }

                    var3
                }
            }

        }
    }

    @Synchronized
    override fun close() {
        if (this.mIsInitializing) {
            throw IllegalStateException("Closed during initialization")
        } else {
            if (this.mDatabase != null && this.mDatabase!!.isOpen) {
                this.mDatabase!!.close()
                this.mDatabase = null
            }

        }
    }

    override fun onConfigure(db: SQLiteDatabase) {}

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database " + this.mName + " from version " + oldVersion + " to " + newVersion + "...")
        val paths = arrayListOf<String>()
        this.getUpgradeFilePaths(oldVersion, newVersion - 1, newVersion, paths)
        if (paths.isEmpty()) {
            Log.e(TAG, "no upgrade script path from $oldVersion to $newVersion")
            throw CustomSQLiteHelper.SQLiteAssetException("no upgrade script path from $oldVersion to $newVersion")
        } else {
            Collections.sort(paths, VersionComparator())
            val var5 = paths.iterator()

            while (var5.hasNext()) {
                val path = var5.next() as String

                try {
                    Log.w(TAG, "processing upgrade: $path")
                    val `is` = this.mContext.assets.open(path)
                    val sql = RoomSQLiteUtils.convertStreamToString(`is`)
                    if (sql != null) {
                        val cmds = RoomSQLiteUtils.splitSqlScript(sql, ';')
                        val var10 = cmds.iterator()

                        while (var10.hasNext()) {
                            val cmd = var10.next() as String
                            if (cmd.trim { it <= ' ' }.length > 0) {
                                db.execSQL(cmd)
                            }
                        }
                    }
                } catch (var12: IOException) {
                    var12.printStackTrace()
                }

            }

            Log.w(TAG, "Successfully upgraded database " + this.mName + " from version " + oldVersion + " to " + newVersion)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}


    @Deprecated("")
    fun setForcedUpgradeVersion(version: Int) {
        this.setForcedUpgrade(version)
    }

    fun setForcedUpgrade(version: Int) {
        this.mForcedUpgradeVersion = version
    }

    fun setForcedUpgrade() {
        this.setForcedUpgrade(this.mNewVersion)
    }

    @Throws(CustomSQLiteHelper.SQLiteAssetException::class)
    private fun createOrOpenDatabase(force: Boolean): SQLiteDatabase? {
        var db: SQLiteDatabase? = null
        val file = File(this.mDatabasePath + "/" + this.mName)
        if (file.exists()) {
            db = this.returnDatabase()
        }

        if (db != null) {
            if (force) {
                Log.w(TAG, "forcing database upgrade!")
                this.copyDatabaseFromAssets()
                db = this.returnDatabase()
            }

            return db
        } else {
            this.copyDatabaseFromAssets()
            db = this.returnDatabase()
            return db
        }
    }

    private fun returnDatabase(): SQLiteDatabase? {
        try {
            val db = SQLiteDatabase.openDatabase(this.mDatabasePath + "/" + this.mName, this.mFactory, 0)
            Log.i(TAG, "successfully opened database " + this.mName!!)
            return db
        } catch (var2: SQLiteException) {
            Log.w(TAG, "could not open database " + this.mName + " - " + var2.message)
            return null
        }

    }

    @Throws(CustomSQLiteHelper.SQLiteAssetException::class)
    private fun copyDatabaseFromAssets() {
        Log.w(TAG, "copying database from assets...")
        val path = this.mAssetPath
        val dest = this.mDatabasePath + "/" + this.mName
        var isZip = false

        var `is`: InputStream
        try {
            `is` = this.mContext.assets.open(path)
//            val db = File(FileHelper.databaseFolder, "your.db")
//            `is` = FileInputStream(db)


        } catch (var12: IOException) {
            try {
                `is` = this.mContext.assets.open(path!! + ".zip")
                isZip = true
            } catch (var11: IOException) {
                try {
                    `is` = this.mContext.assets.open(path!! + ".gz")
                } catch (var10: IOException) {
                    val se = CustomSQLiteHelper.SQLiteAssetException("Missing " + this.mAssetPath + " file (or .zip, .gz archive) in assets, or target folder not writable")
                    se.setStackTrace(var10.stackTrace)
                    throw se
                }
            }
        }

        try {
            val f = File(this.mDatabasePath!! + "/")
            if (!f.exists()) {
                f.mkdir()
            }

            if (isZip) {
                val zis = RoomSQLiteUtils.getFileFromZip(`is`)
                        ?: throw CustomSQLiteHelper.SQLiteAssetException("Archive is missing a SQLite database file")

                RoomSQLiteUtils.writeExtractedFileToDisk(zis, FileOutputStream(dest))
            } else {
                RoomSQLiteUtils.writeExtractedFileToDisk(`is`, FileOutputStream(dest))
            }

            Log.w(TAG, "database copy complete")
        } catch (var9: IOException) {
            val se = CustomSQLiteHelper.SQLiteAssetException("Unable to write $dest to data directory")
            se.setStackTrace(var9.stackTrace)
            throw se
        }

    }

    private fun getUpgradeSQLStream(oldVersion: Int, newVersion: Int): InputStream? {

        if(this.mUpgradePathFormat != null) {
            val path = String.format(this.mUpgradePathFormat, oldVersion, newVersion)

            try {
                return this.mContext.assets.open(path)
            } catch (var5: IOException) {
                Log.w(TAG, "missing database upgrade script: $path")
                return null
            }
        }
        return null
    }

    private fun getUpgradeFilePaths(baseVersion: Int, start: Int, end: Int, paths: ArrayList<String>) {
        val `is` = this.getUpgradeSQLStream(start, end)
        val a: Int
        val b: Int
        if (`is` != null) {
            val path = String.format(this.mUpgradePathFormat, start, end)
            paths.add(path)
            a = start - 1
            b = start
        } else {
            a = start - 1
            b = end
        }

        if (a >= baseVersion) {
            this.getUpgradeFilePaths(baseVersion, a, b, paths)
        }
    }

    class SQLiteAssetException : SQLiteException {
        constructor(error: String) : super(error) {}
    }
}