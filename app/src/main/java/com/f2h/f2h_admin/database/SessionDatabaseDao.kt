package com.f2h.f2h_admin.database

import androidx.room.*

@Dao
interface SessionDatabaseDao {

    @Insert
    fun insert(sessionEntity: SessionEntity)

    @Update
    fun update(sessionEntity: SessionEntity)

    @Query("SELECT * FROM session_table WHERE id = :id")
    fun get(id: Long): SessionEntity

    @Query("SELECT * FROM session_table")
    fun getAll(): List<SessionEntity>

    @Query("DELETE FROM session_table")
    fun clearSessions()

    @Query("UPDATE session_table SET group_id = :groupId, group_description = :groupDescription, group_name = :groupName, group_currency = :groupCurrency WHERE id is not null")
    fun updateActiveGroup(groupId: Long, groupDescription: String, groupName: String, groupCurrency: String)

}