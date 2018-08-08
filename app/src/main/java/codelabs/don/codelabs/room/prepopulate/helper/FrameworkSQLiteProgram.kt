package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.db.SupportSQLiteProgram
import android.database.sqlite.SQLiteProgram

/**
 * Created by don on 8/7/18
 */
class FrameworkSQLiteProgram: SupportSQLiteProgram {

    private lateinit var mDelegate: SQLiteProgram

    constructor(mDelegate: SQLiteProgram) {
        this.mDelegate = mDelegate
    }

    override fun bindNull(index: Int) {
        this.mDelegate.bindNull(index)
    }

    override fun bindLong(index: Int, value: Long) {
        this.mDelegate.bindLong(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        this.mDelegate.bindDouble(index, value)
    }

    override fun bindString(index: Int, value: String) {
        this.mDelegate.bindString(index, value)
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        this.mDelegate.bindBlob(index, value)
    }

    override fun clearBindings() {
        this.mDelegate.clearBindings()
    }

    @Throws(Exception::class)
    override fun close() {
        this.mDelegate.close()
    }
}