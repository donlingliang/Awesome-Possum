package codelabs.don.codelabs.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "word_table")
class Word(@PrimaryKey
           @ColumnInfo(name = "word")
           var mWord: String) {

    init {
        this.mWord = mWord
    }
}