package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformDao {
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUser(id: Int = 1): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUserSync(id: Int = 1): UserEntity?

    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserSync(): UserEntity?

    @Query("SELECT * FROM user_profile")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM user_profile")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("UPDATE user_profile SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE username = :username ORDER BY timestamp DESC")
    fun getTransactionsForUser(username: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)

    @Query("SELECT * FROM investments ORDER BY startDate DESC")
    fun getInvestments(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE username = :username ORDER BY startDate DESC")
    fun getInvestmentsForUser(username: String): Flow<List<InvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(inv: InvestmentEntity)

    @Update
    suspend fun updateInvestment(inv: InvestmentEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM investments")
    suspend fun deleteAllInvestments()

    // Persistent Chat Messages
    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY timestamp ASC")
    fun getMessagesForUser(username: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    // Invitation Codes
    @Query("SELECT * FROM invitation_codes ORDER BY timestamp DESC")
    fun getAllInvitationCodes(): Flow<List<InvitationCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitationCode(code: InvitationCodeEntity)

    @Query("DELETE FROM invitation_codes WHERE code = :code")
    suspend fun deleteInvitationCode(code: String)

    @Query("SELECT * FROM invitation_codes WHERE code = :code LIMIT 1")
    suspend fun getInvitationCode(code: String): InvitationCodeEntity?
}
