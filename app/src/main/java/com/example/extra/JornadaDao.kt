package com.example.extra

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JornadaDao {
    @Insert
    suspend fun insertJornada(jornada: Jornada): Long

    @Query("SELECT * FROM jornadas ORDER BY date DESC")
    fun getAllJornadas(): Flow<List<Jornada>>

    @Query("SELECT * FROM jornadas WHERE id = :id")
    fun getJornadaById(id: Int): Flow<Jornada>

    @Query("DELETE FROM jornadas WHERE id = :id")
    suspend fun deleteJornada(id: Int)

    @Query("UPDATE expenses SET jornadaId = NULL WHERE jornadaId = :jornadaId")
    suspend fun detachExpensesFromJornada(jornadaId: Int)
}
