package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.db.SupportSQLiteStatement
import android.database.sqlite.SQLiteStatement

/**
 * Created by don on 8/7/18
 */
class FrameworkSQLiteStatement : SupportSQLiteStatement {

    private lateinit var mDelegate: SQLiteStatement

    constructor(mDelegate: SQLiteStatement) {
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

    override fun execute() {
        this.mDelegate.execute()
    }

    override fun executeUpdateDelete(): Int {
        return this.mDelegate.executeUpdateDelete()
    }

    override fun executeInsert(): Long {
        return this.mDelegate.executeInsert()
    }

    override fun simpleQueryForLong(): Long {
        return this.mDelegate.simpleQueryForLong()
    }

    override fun simpleQueryForString(): String {
        return this.mDelegate.simpleQueryForString()
    }

    @Throws(Exception::class)
    override fun close() {
        this.mDelegate.close()
    }

}