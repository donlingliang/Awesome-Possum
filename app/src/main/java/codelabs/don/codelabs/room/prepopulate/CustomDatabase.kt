package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * Created by don on 8/7/18
 */

//@Database(entities = arrayOf(Entity::class), version = 1, exportSchema = true)
abstract class CustomDatabase : RoomDatabase() {

    companion object {

        private val DATABASE_NAME = "db.name"
        private var INSTANCE: CustomDatabase? = null

        fun getInstance(context: Context): CustomDatabase? {
            if (INSTANCE == null) {
                synchronized(CustomDatabase::class) {
                    INSTANCE = createDatabase(context)
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        fun createDatabase(context: Context): CustomDatabase? {
            return Room.databaseBuilder<CustomDatabase>(
                    context.applicationContext, CustomDatabase::class.java, DATABASE_NAME)
                    .openHelperFactory(CustomSQLiteOpenHelperFactory())
                    .allowMainThreadQueries()
                    .build()
        }
    }

    abstract fun getSourceDao(): SourceDAO
}