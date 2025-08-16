package com.twojafirma.askier.ui.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(context: Context) {

    private val appDao = AppDatabase.getDatabase(context).appDao()

    fun getAllSessions(): Flow<List<GameSession>> {
        return appDao.getAllSessionsWithDeals().map { list ->
            list.map { it.toGameSession() }
        }
    }

    suspend fun createNewSession(session: GameSession): Long {
        val sessionEntity = session.toSessionEntity()
        return appDao.insertSession(sessionEntity)
    }

    suspend fun addDealToSession(sessionId: Long, deal: SessionDeal) {
        val dealEntity = deal.toDealEntity(sessionId)
        appDao.insertDeal(dealEntity)
    }

    suspend fun updatePlayersInSession(sessionId: Long, players: Map<Player, PlayerProfile?>) {
        appDao.updatePlayersInSession(sessionId, players)
    }

    suspend fun deleteSession(sessionId: Long) {
        appDao.deleteSession(sessionId)
    }

    // --- Funkcje konwertujące (mapujące) ---

    private fun SessionWithDeals.toGameSession(): GameSession {
        return GameSession(
            id = this.session.id,
            title = this.session.title,
            players = this.session.players,
            deals = this.deals.map { it.toSessionDeal() }
        )
    }

    // Konwersja z Bazy Danych -> do UI
    private fun DealEntity.toSessionDeal(): SessionDeal {
        return SessionDeal(
            baseDeal = Deal(
                id = this.id.toInt(),
                level = this.level,
                suit = this.suit,
                declarer = this.declarer,
                double = this.double,
                tricks = this.tricks,
                vulnerability = this.vulnerability,
                score = this.score
            ),
            nsPoints = this.nsHcp,
            ewPoints = this.ewHcp,
            imps = this.imps // Poprawione z nsImps na imps
        )
    }

    private fun GameSession.toSessionEntity(): SessionEntity {
        return SessionEntity(
            id = this.id,
            title = this.title,
            players = this.players,
            createdAt = this.id
        )
    }

    // Konwersja z UI -> do Bazy Danych
    private fun SessionDeal.toDealEntity(sessionId: Long): DealEntity {
        return DealEntity(
            sessionId = sessionId,
            level = this.baseDeal.level,
            suit = this.baseDeal.suit,
            declarer = this.baseDeal.declarer,
            double = this.baseDeal.double,
            tricks = this.baseDeal.tricks,
            vulnerability = this.baseDeal.vulnerability,
            score = this.baseDeal.score,
            nsHcp = this.nsPoints,
            ewHcp = this.ewPoints,
            imps = this.imps // Poprawione z nsImps na imps
        )
    }

    suspend fun deleteLastDeal(sessionId: Long) {
        appDao.deleteLastDeal(sessionId)
    }
}