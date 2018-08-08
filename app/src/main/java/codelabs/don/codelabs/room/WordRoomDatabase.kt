package codelabs.don.codelabs.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.AsyncTask
import android.arch.persistence.db.SupportSQLiteDatabase


@Database(entities = arrayOf(Word::class), version = 1, exportSchema = true)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {

        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                PopulateDbAsync(INSTANCE).execute()
            }
        }
        private var INSTANCE: WordRoomDatabase? = null

        // cannot find implementation for codelabs.don.codelabs.room.WordRoomDatabase. WordRoomDatabase_Impl does not exist
        fun getDatabase(context: Context): WordRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(WordRoomDatabase::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.applicationContext,
                                WordRoomDatabase::class.java,
                                "word_database")
                                .addCallback(sRoomDatabaseCallback)
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }

    class PopulateDbAsync : AsyncTask<Word, Void, Unit> {

        private var mDao: WordDao? = null

        constructor(database: WordRoomDatabase?) {
            mDao = database?.wordDao()
        }

        override fun doInBackground(vararg p0: Word?) {
            mDao?.deleteAll()
            var word = Word("Hello")
            mDao?.insert(word)
            word = Word("World")
            mDao?.insert(word)
        }
    }
}