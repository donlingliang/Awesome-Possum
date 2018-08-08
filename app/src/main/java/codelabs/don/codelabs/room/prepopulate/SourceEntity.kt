package com.tactile.tact.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by don on 8/7/18
 */

@Entity(tableName = "table_name")
class SourceEntity {

    @PrimaryKey
    @ColumnInfo(name = "primary_key") lateinit var primaryKey: String

}