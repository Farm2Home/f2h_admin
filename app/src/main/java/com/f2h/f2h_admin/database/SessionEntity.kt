package com.f2h.f2h_admin.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_table")
data class SessionEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    var userId: Long = 0L,

    @ColumnInfo(name = "user_name")
    var userName: String = "",

    @ColumnInfo(name = "address")
    var address: String = "",

    @ColumnInfo(name = "email")
    var email: String = "",

    @ColumnInfo(name = "mobile")
    var mobile: String = "",

    @ColumnInfo(name = "password")
    var password: String = "",

    @ColumnInfo(name = "group_id")
    var groupId: Long = 0L,

    @ColumnInfo(name = "group_name")
    var groupName: String = "",

    @ColumnInfo(name = "group_description")
    var groupDescription: String = ""

)