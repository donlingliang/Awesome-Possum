package codelabs.don.codelabs.room

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class WordViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val mRepository: WordRepository
    private var mAllWords: LiveData<List<Word>>? = null

    init {
        mRepository = WordRepository(application)
        mAllWords = mRepository.getAllWords()
    }

    fun getAllWords(): LiveData<List<Word>>? {
        return mAllWords
    }

    fun insert(word: Word) {
        mRepository.insert(word)
    }

}