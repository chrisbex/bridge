package com.twojafirma.askier.ui.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: DealEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("UPDATE sessions SET players = :players WHERE id = :sessionId")
    suspend fun updatePlayersInSession(sessionId: Long, players: Map<Player, PlayerProfile?>)

    @Query("DELETE FROM deals WHERE id = (SELECT MAX(id) FROM deals WHERE sessionId = :sessionId)")
    suspend fun deleteLastDeal(sessionId: Long)
    @Transaction
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessionsWithDeals(): Flow<List<SessionWithDeals>>
}

@Database(entities = [SessionEntity::class, DealEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "askier_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}