package codelabs.don.codelabs.room

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask

class WordRepository constructor(application: Application) {

    var mWordDao: WordDao? = null
    var mAllWords: LiveData<List<Word>>? = null

    init {
        var database: WordRoomDatabase? = WordRoomDatabase.getDatabase(application)
        mWordDao = database?.wordDao()
        mAllWords = mWordDao?.getAllWords()
    }

    fun getAllWords(): LiveData<List<Word>>? {
        return mAllWords
    }

    fun insert(word: Word) {
        insertAsyncTask(mWordDao).execute(word)
    }

    class insertAsyncTask : AsyncTask<Word, Void, Unit> {

        private var mAsyncTaskDao: WordDao? = null

        constructor(dao: WordDao?) {
            mAsyncTaskDao = dao
        }

        override fun doInBackground(vararg p0: Word?) {
            p0?.let {
                mAsyncTaskDao?.insert(p0[0])
            }
        }
    }
}