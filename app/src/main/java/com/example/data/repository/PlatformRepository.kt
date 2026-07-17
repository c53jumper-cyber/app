package com.example.data.repository

import android.content.Context
import com.example.data.api.BinanceClient
import com.example.data.database.*
import com.example.data.network.NetworkModule
import com.example.data.network.RemoteApiService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class CoinPair(
    val name: String,         // E.g., "BTC/USDT"
    val symbol: String,       // E.g., "BTCUSDT" or "UNIUSDT"
    var price: Double,
    var change24hPercent: Double
)

class PlatformRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.platformDao()
    private val api: RemoteApiService = NetworkModule.apiService
    val onlineDbManager = OnlineDatabaseManager(context)
    val authManager = AuthManager(context)

    val userProfile: Flow<UserEntity?> = dao.getLoggedInUser()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getTransactions()
    val allInvestments: Flow<List<InvestmentEntity>> = dao.getInvestments()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allInvitationCodes: Flow<List<InvitationCodeEntity>> = dao.getAllInvitationCodes()

    // Initialize list of currency pairs with default/base prices
    private val _marketPrices = MutableStateFlow<List<CoinPair>>(
        listOf(
            CoinPair("BTC/USDT", "BTCUSDT", 67240.50, 2.45),
            CoinPair("ETH/USDT", "ETHUSDT", 3420.75, 1.82),
            CoinPair("DOGE/USDT", "DOGEUSDT", 0.1254, -3.12),
            CoinPair("EOS/USDT", "EOSUSDT", 0.654, 0.45),
            CoinPair("LTC/USDT", "LTCUSDT", 78.40, 1.15),
            CoinPair("BCH/USDT", "BCHUSDT", 385.20, -0.92),
            CoinPair("XRP/USDT", "XRPUSDT", 0.5842, 4.25),
            CoinPair("ETC/USDT", "ETCUSDT", 21.30, -1.80),
            CoinPair("BSV/USDT", "BSVUSDT", 45.10, 0.22), // Mock/Fallback since not on Binance
            CoinPair("ADA/USDT", "ADAUSDT", 0.4256, 3.82),
            CoinPair("FIL/USDT", "FILUSDT", 4.12, -2.15),
            CoinPair("TRX/USDT", "TRXUSDT", 0.1384, 0.85),
            CoinPair("UNI/USDT", "UNIUSDT", 7.92, -4.10),
            CoinPair("LINK/USDT", "LINKUSDT", 14.85, 2.74),
            CoinPair("SOL/USDT", "SOLUSDT", 168.42, 6.95),
            CoinPair("AAVE/USDT", "AAVEUSDT", 98.55, -1.25)
        )
    )
    val marketPrices: StateFlow<List<CoinPair>> = _marketPrices

    init {
        // Pre-populate database with default User and Admin if empty
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val users = dao.getAllUsersSync()
            
            // Check if Admin exists, if not create it
            val adminExists = users.any { it.role == "ADMIN" }
            if (!adminExists) {
                val adminUser = UserEntity(
                    username = "Admin",
                    password = "999", 
                    isLoggedIn = false,
                    role = "ADMIN",
                    balanceUsdt = 0.0,
                    invitationCode = "MASTER"
                )
                dao.insertUser(adminUser)
            }

            if (users.isEmpty()) {
                // Default User (only if DB is totally empty)
                val defaultUser = UserEntity(
                    username = "eth999",
                    password = "password",
                    isLoggedIn = false,
                    balanceUsdt = 35000000.0,
                    totalRecharged = 15000.0,
                    vipLevel = 2,
                    invitationCode = "ATOM999",
                    role = "USER"
                )
                dao.insertUser(defaultUser)
                
                // Pre-populate some historic transactions for fidelity
                dao.insertTransaction(TransactionEntity(type = "RECHARGE", amount = 250000.0, address = "TYv9zNf2K8W7ZgHjK9XsD1FpQ6L3Rm8VbA", username = "eth999"))
                dao.insertTransaction(TransactionEntity(type = "RECHARGE", amount = 3339.65, address = "TYv9zNf2K8W7ZgHjK9XsD1FpQ6L3Rm8VbA", username = "eth999"))
                
                // Add an initial welcome chat support message
                dao.insertMessage(MessageEntity(sender = "SUPPORT", text = "سلام! به پشتیبانی آنلاین خوش آمدید. چگونه می‌توانیم به شما کمک کنیم؟", username = "eth999"))
            }
        }
    }

    // Refresh market prices from Binance API
    suspend fun refreshPrices() {
        try {
            val apiPrices = BinanceClient.api.getTickerPrices()
            val apiMap = apiPrices.associateBy { it.symbol }
            val currentList = _marketPrices.value
            val updatedList = currentList.map { pair ->
                val match = apiMap[pair.symbol]
                if (match != null) {
                    val newPrice = match.price.toDoubleOrNull() ?: pair.price
                    val oldPrice = pair.price
                    val change = if (oldPrice > 0) ((newPrice - oldPrice) / oldPrice) * 100 else pair.change24hPercent
                    val finalChange = if (change == 0.0) pair.change24hPercent + Random.nextDouble(-0.1, 0.1) else change
                    CoinPair(pair.name, pair.symbol, newPrice, finalChange)
                } else {
                    val pct = Random.nextDouble(-0.005, 0.005)
                    val newPrice = pair.price * (1.0 + pct)
                    CoinPair(pair.name, pair.symbol, newPrice, pair.change24hPercent + pct * 100)
                }
            }
            _marketPrices.value = updatedList
        } catch (e: Exception) {
            val currentList = _marketPrices.value
            val updatedList = currentList.map { pair ->
                val pct = Random.nextDouble(-0.001, 0.001)
                val newPrice = pair.price * (1.0 + pct)
                CoinPair(pair.name, pair.symbol, newPrice, pair.change24hPercent + pct * 100)
            }
            _marketPrices.value = updatedList
        }
    }

    suspend fun recharge(amount: Double) {
        val user = dao.getLoggedInUserSync() ?: return
        val newBalance = user.balanceUsdt + amount
        val newTotalRecharged = user.totalRecharged + amount
        val updatedUser = user.copy(balanceUsdt = newBalance, totalRecharged = newTotalRecharged)
        dao.updateUser(updatedUser)
        val tx = TransactionEntity(
            type = "RECHARGE",
            amount = amount,
            address = user.walletAddress,
            username = user.username
        )
        dao.insertTransaction(tx)
        
        // Online Sync
        try {
            api.addTransaction(tx)
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
        onlineDbManager.syncTransactionToFirebase(tx)
    }

    suspend fun withdraw(amount: Double, destAddress: String): WithdrawResult {
        val user = dao.getLoggedInUserSync() ?: return WithdrawResult.INSUFFICIENT_BALANCE
        if (amount > 1000000.0) {
            return WithdrawResult.LIMIT_EXCEEDED
        }
        val requiredRecharge = if (user.isIpSuspicious && user.hasReceivedBonus) 17500000.0 else 10000.0
        if (user.totalRecharged < requiredRecharge) {
            return WithdrawResult.VERIFICATION_REQUIRED
        }
        if (user.balanceUsdt < amount) {
            return WithdrawResult.INSUFFICIENT_BALANCE
        }
        val newBalance = user.balanceUsdt - amount
        val updatedUser = user.copy(balanceUsdt = newBalance)
        dao.updateUser(updatedUser)
        val tx = TransactionEntity(
            type = "WITHDRAW",
            amount = amount,
            address = destAddress,
            status = "PENDING",
            username = user.username
        )
        dao.insertTransaction(tx)

        // Online Sync
        try {
            api.addTransaction(tx)
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
        onlineDbManager.syncTransactionToFirebase(tx)
        return WithdrawResult.SUCCESS
    }

    suspend fun simulateIpChange(newIp: String) {
        val user = dao.getLoggedInUserSync() ?: return
        val updatedUser = user.copy(currentIp = newIp, isIpSuspicious = (newIp != user.registeredIp))
        dao.updateUser(updatedUser)
        onlineDbManager.syncUserToFirebase(updatedUser)
    }

    suspend fun transfer(amount: Double, recipient: String): Boolean {
        val user = dao.getLoggedInUserSync() ?: return false
        if (user.balanceUsdt < amount) return false
        val newBalance = user.balanceUsdt - amount
        val updatedUser = user.copy(balanceUsdt = newBalance)
        dao.updateUser(updatedUser)
        val tx = TransactionEntity(
            type = "TRANSFER",
            amount = amount,
            address = recipient,
            username = user.username
        )
        dao.insertTransaction(tx)
        onlineDbManager.syncUserToFirebase(updatedUser)
        onlineDbManager.syncTransactionToFirebase(tx)
        return true
    }

    suspend fun registerUser(username: String, password: String, inviteCode: String): Boolean {
        val trimmedCode = inviteCode.trim()
        if (trimmedCode.isEmpty()) return false

        // 1. Try Remote Registration
        try {
            val response = api.register(mapOf(
                "username" to username,
                "password" to password,
                "inviteCode" to trimmedCode
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteUser = response.body()?.data
                if (remoteUser != null) {
                    dao.logoutAllUsers()
                    dao.insertUser(remoteUser.copy(isLoggedIn = true))
                    return true
                }
            }
        } catch (e: Exception) {
            // Fallback to local or just log
        }

        // 2. Local Fallback (Original Logic)
        val systemCode = dao.getInvitationCode(trimmedCode)
        val users = dao.getAllUsersSync()
        val isValidUserCode = users.any { it.invitationCode == trimmedCode }

        if (systemCode == null && !isValidUserCode) return false

        dao.logoutAllUsers()
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val ownInviteCode = "ATOM" + (1..4).map { allowedChars.random() }.joinToString("")
        val user = UserEntity(
            username = username,
            password = password,
            isLoggedIn = true,
            invitationCode = ownInviteCode,
            balanceUsdt = 35000000.0,
            hasReceivedBonus = true,
            totalRecharged = 0.0,
            vipLevel = 1
        )
        dao.insertUser(user)
        val msg = MessageEntity(sender = "SUPPORT", text = "به پلتفرم خوش آمدید! چطور می‌توانم به شما کمک کنم؟", username = username)
        dao.insertMessage(msg)
        onlineDbManager.syncUserToFirebase(user)
        onlineDbManager.syncMessageToFirebase(msg)
        return true
    }

    suspend fun generateInvitationCode(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = "INV-" + (1..6).map { allowedChars.random() }.joinToString("")
        val entity = InvitationCodeEntity(code = code)
        dao.insertInvitationCode(entity)
        
        // Online Sync
        try {
            api.addInviteCode(entity)
        } catch (e: Exception) {}
        
        return code
    }

    suspend fun deleteInvitationCode(code: String) {
        dao.deleteInvitationCode(code)
        
        // Online Sync
        try {
            api.deleteInviteCode(code)
        } catch (e: Exception) {}
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        // 1. Try Remote Login
        try {
            val response = api.login(mapOf("username" to username, "password" to password))
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteUser = response.body()?.data
                if (remoteUser != null) {
                    dao.logoutAllUsers()
                    dao.insertUser(remoteUser.copy(isLoggedIn = true))
                    return true
                }
            }
        } catch (e: Exception) {
            // Fallback to local
        }

        // 2. Local Fallback
        val users = dao.getAllUsersSync()
        val match = users.find { it.username == username && it.password == password }
        if (match != null) {
            dao.logoutAllUsers()
            val updatedUser = match.copy(isLoggedIn = true)
            dao.updateUser(updatedUser)
            onlineDbManager.syncUserToFirebase(updatedUser)
            return true
        }
        return false
    }

    suspend fun logout() {
        val user = dao.getLoggedInUserSync()
        dao.logoutAllUsers()
        if (user != null) {
            onlineDbManager.syncUserToFirebase(user.copy(isLoggedIn = false))
        }
    }

    suspend fun upgradeVip(newLevel: Int, cost: Double): Boolean {
        val user = dao.getLoggedInUserSync() ?: return false
        if (user.balanceUsdt < cost) return false
        val newBalance = user.balanceUsdt - cost
        val updatedUser = user.copy(vipLevel = newLevel, balanceUsdt = newBalance)
        dao.updateUser(updatedUser)
        val tx = TransactionEntity(
            type = "VIP_UPGRADE",
            amount = cost,
            address = "VIP Level $newLevel Upgrade",
            username = user.username
        )
        dao.insertTransaction(tx)
        onlineDbManager.syncUserToFirebase(updatedUser)
        onlineDbManager.syncTransactionToFirebase(tx)
        return true
    }

    suspend fun purchaseInvestment(planTitle: String, apr: Double, durationDays: Int, amount: Double): Boolean {
        val user = dao.getLoggedInUserSync() ?: return false
        if (user.balanceUsdt < amount) return false
        val newBalance = user.balanceUsdt - amount
        val updatedUser = user.copy(balanceUsdt = newBalance)
        dao.updateUser(updatedUser)

        val interest = amount * (apr / 100.0) * (durationDays / 365.0)
        val maturity = System.currentTimeMillis() + (durationDays * 24L * 60L * 60L * 1000L)

        val inv = InvestmentEntity(
            planTitle = planTitle,
            apr = apr,
            durationDays = durationDays,
            amountInvested = amount,
            maturityDate = maturity,
            interestEarned = interest,
            status = "ACTIVE",
            username = user.username
        )
        dao.insertInvestment(inv)

        val tx = TransactionEntity(
            type = "INVESTMENT",
            amount = amount,
            address = "$planTitle ($durationDays Days)",
            username = user.username
        )
        dao.insertTransaction(tx)

        // Online Sync
        try {
            api.addInvestment(inv)
            api.addTransaction(tx)
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
        onlineDbManager.syncInvestmentToFirebase(inv)
        onlineDbManager.syncTransactionToFirebase(tx)
        return true
    }

    suspend fun updateUserData(
        username: String,
        vipLevel: Int,
        balanceUsdt: Double,
        walletAddress: String,
        totalRecharged: Double,
        invitationCode: String,
        registeredIp: String,
        currentIp: String,
        isIpSuspicious: Boolean
    ) {
        val user = dao.getLoggedInUserSync() ?: return
        val updatedUser = user.copy(
            username = username,
            vipLevel = vipLevel,
            balanceUsdt = balanceUsdt,
            walletAddress = walletAddress,
            totalRecharged = totalRecharged,
            invitationCode = invitationCode,
            registeredIp = registeredIp,
            currentIp = currentIp,
            isIpSuspicious = isIpSuspicious
        )
        dao.updateUser(updatedUser)

        // Online Sync
        try {
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
    }

    suspend fun linkGoogleAccount(googleEmail: String) {
        val user = dao.getLoggedInUserSync() ?: return
        val updatedUser = user.copy(
            googleEmail = googleEmail,
            isGoogleVerified = true
        )
        dao.updateUser(updatedUser)

        // Online Sync
        try {
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
    }

    suspend fun updateInviteCode(inviteCode: String): Boolean {
        val user = dao.getLoggedInUserSync() ?: return false
        if (user.invitationCode.isNotEmpty() && user.invitationCode != "None" && user.invitationCode != "N/A") return false 
        val updatedUser = user.copy(invitationCode = inviteCode)
        dao.updateUser(updatedUser)

        // Online Sync
        try {
            api.updateUser(updatedUser)
        } catch (e: Exception) {}

        onlineDbManager.syncUserToFirebase(updatedUser)
        return true
    }

    suspend fun adminUpdateAnyUser(user: UserEntity) {
        dao.updateUser(user)
        onlineDbManager.syncUserToFirebase(user)
    }

    suspend fun addTransaction(type: String, amount: Double, network: String, address: String, status: String) {
        val activeUser = dao.getLoggedInUserSync()
        val tx = TransactionEntity(
            type = type,
            amount = amount,
            network = network,
            address = address,
            status = status,
            timestamp = System.currentTimeMillis(),
            username = activeUser?.username ?: "eth999"
        )
        dao.insertTransaction(tx)
        onlineDbManager.syncTransactionToFirebase(tx)
    }

    suspend fun clearTransactions() {
        dao.deleteAllTransactions()
    }

    suspend fun clearInvestments() {
        dao.deleteAllInvestments()
    }

    // Chat operations
    fun getMessagesForUser(username: String): Flow<List<MessageEntity>> = dao.getMessagesForUser(username)
    fun getAllMessages(): Flow<List<MessageEntity>> = dao.getAllMessages()
    
    suspend fun sendMessage(sender: String, text: String, username: String) {
        val msg = MessageEntity(
            sender = sender,
            text = text,
            username = username,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)

        // Online Sync
        try {
            api.sendMessage(msg)
        } catch (e: Exception) {}

        onlineDbManager.syncMessageToFirebase(msg)
    }

    suspend fun clearAllMessages() {
        dao.deleteAllMessages()
    }

    // Admin updates status of specific withdrawal request
    suspend fun updateTransactionStatus(txId: Int, newStatus: String, newAmount: Double? = null): Boolean {
        val tx = dao.getTransactionById(txId) ?: return false
        val finalAmount = newAmount ?: tx.amount
        val oldAmount = tx.amount

        // Load the target user of this transaction
        val users = dao.getAllUsersSync()
        val targetUser = users.find { it.username == tx.username }
        if (targetUser != null) {
            var updatedBalance = targetUser.balanceUsdt
            if (newStatus == "REJECTED" && tx.status == "PENDING") {
                // Refund the original requested amount if rejected
                updatedBalance += oldAmount
            } else if (tx.status == "PENDING" && newStatus == "COMPLETED" && newAmount != null) {
                // If amount is edited and completed, refund or deduct the difference
                val diff = oldAmount - newAmount
                updatedBalance += diff
            }
            val updatedUser = targetUser.copy(balanceUsdt = updatedBalance)
            dao.updateUser(updatedUser)
            onlineDbManager.syncUserToFirebase(updatedUser)
        }

        val updatedTx = tx.copy(status = newStatus, amount = finalAmount)
        dao.insertTransaction(updatedTx)
        onlineDbManager.syncTransactionToFirebase(updatedTx)
        return true
    }
}

enum class WithdrawResult {
    SUCCESS,
    INSUFFICIENT_BALANCE,
    VERIFICATION_REQUIRED,
    LIMIT_EXCEEDED
}
