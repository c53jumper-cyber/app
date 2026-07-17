package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String = "eth999",
    val password: String = "",
    val vipLevel: Int = 1,
    val balanceUsdt: Double = 0.0,
    val walletAddress: String = "TYv9zNf2K8W7ZgHjK9XsD1FpQ6L3Rm8VbA",
    val isLoggedIn: Boolean = false,
    val totalRecharged: Double = 0.0,
    val invitationCode: String = "",
    val googleEmail: String? = null,
    val isGoogleVerified: Boolean = false,
    val hasReceivedBonus: Boolean = false,
    val registeredIp: String = "185.120.45.12",
    val currentIp: String = "185.120.45.12",
    val isIpSuspicious: Boolean = false,
    val role: String = "USER" // "USER" or "ADMIN"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "RECHARGE", "WITHDRAW", "TRANSFER"
    val amount: Double,
    val network: String = "TRC20",
    val address: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "REJECTED"
    val username: String = "eth999"
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planTitle: String,
    val apr: Double,
    val durationDays: Int,
    val amountInvested: Double,
    val startDate: Long = System.currentTimeMillis(),
    val maturityDate: Long,
    val interestEarned: Double,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED"
    val username: String = "eth999"
)

@Entity(tableName = "chat_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "SUPPORT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "eth999"
)

@Entity(tableName = "invitation_codes")
data class InvitationCodeEntity(
    @PrimaryKey val code: String,
    val createdBy: String = "ADMIN",
    val timestamp: Long = System.currentTimeMillis()
)
