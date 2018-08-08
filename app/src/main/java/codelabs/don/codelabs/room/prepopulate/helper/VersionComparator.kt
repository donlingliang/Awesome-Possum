package codelabs.don.codelabs.room.prepopulate

import android.util.Log
import java.util.*
import java.util.regex.Pattern

/**
 * Created by don on 8/7/18
 */
class VersionComparator: Comparator<String> {

    private val TAG = CustomSQLiteHelper::class.java.simpleName
    private val pattern = Pattern.compile(".*_upgrade_([0-9]+)-([0-9]+).*")

    constructor()

    override fun compare(file0: String, file1: String): Int {
        val m0 = this.pattern.matcher(file0)
        val m1 = this.pattern.matcher(file1)
        if (!m0.matches()) {
            Log.w(TAG, "could not parse upgrade script file: $file0")
            throw CustomSQLiteHelper.SQLiteAssetException("Invalid upgrade script file")
        } else if (!m1.matches()) {
            Log.w(TAG, "could not parse upgrade script file: $file1")
            throw CustomSQLiteHelper.SQLiteAssetException("Invalid upgrade script file")
        } else {
            val v0_from = Integer.valueOf(m0.group(1))
            val v1_from = Integer.valueOf(m1.group(1))
            val v0_to = Integer.valueOf(m0.group(2))
            val v1_to = Integer.valueOf(m1.group(2))
            return if (v0_from == v1_from) {
                if (v0_to == v1_to) {
                    0
                } else {
                    if (v0_to < v1_to) -1 else 1
                }
            } else {
                if (v0_from < v1_from) -1 else 1
            }
        }
    }
}