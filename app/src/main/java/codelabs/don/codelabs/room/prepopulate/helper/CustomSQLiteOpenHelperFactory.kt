package codelabs.don.codelabs.room.prepopulate

import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.db.SupportSQLiteOpenHelper.Factory
import android.database.DatabaseErrorHandler

/**
 * Created by don on 8/7/18
 */
class CustomSQLiteOpenHelperFactory : Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return CustomSQLiteOpenHelper(
                configuration.context,
                configuration.name!!,
                null,
                configuration.callback.version,
                DatabaseErrorHandler { },
                configuration.callback)
    }
}