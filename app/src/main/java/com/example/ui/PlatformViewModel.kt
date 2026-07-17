package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.InvestmentEntity
import com.example.data.database.MessageEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.example.data.database.InvitationCodeEntity
import com.example.data.repository.CoinPair
import com.example.data.repository.PlatformRepository
import com.example.data.repository.WithdrawResult
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

data class Ticket(
    val id: String,
    val subject: String,
    val category: String,
    val status: String, // "OPEN", "RESOLVED"
    val timestamp: Long = System.currentTimeMillis()
)

class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlatformRepository(application)

    private val _currentLanguage = MutableStateFlow("fa") // "fa", "en", "ar", "de"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    // Active logged-in user profile stream
    val userProfile: StateFlow<UserEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User-specific transactions (derived from combine)
    val transactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        userProfile
    ) { allTx, user ->
        if (user == null) emptyList()
        else allTx.filter { it.username == user.username }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User-specific investments (derived from combine)
    val investments: StateFlow<List<InvestmentEntity>> = combine(
        repository.allInvestments,
        userProfile
    ) { allInv, user ->
        if (user == null) emptyList()
        else allInv.filter { it.username == user.username }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin-specific flows to see all users and transactions
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvitationCodes: StateFlow<List<InvitationCodeEntity>> = repository.allInvitationCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time persistent support messages for the logged-in user
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<MessageEntity>> = userProfile
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getMessagesForUser(user.username)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketPrices: StateFlow<List<CoinPair>> = repository.marketPrices

    private val _tickets = MutableStateFlow<List<Ticket>>(
        listOf(
            Ticket("T-1082", "درخواست ارتقا به VIP2", "VIP Level", "OPEN"),
            Ticket("T-0925", "تایید واریز تتر TRC20", "Recharge", "RESOLVED")
        )
    )
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private var priceRefreshJob: Job? = null

    init {
        startPriceRefreshLoop()
    }

    private fun startPriceRefreshLoop() {
        priceRefreshJob?.cancel()
        priceRefreshJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                repository.refreshPrices()
                delay(30000) // update every 30 seconds
            }
        }
    }

    fun manualRefreshPrices() {
        viewModelScope.launch {
            repository.refreshPrices()
        }
    }

    val databaseSyncStatus: StateFlow<com.example.data.repository.DatabaseSyncStatus> = repository.onlineDbManager.syncStatus

    val authStatus: StateFlow<com.example.data.repository.AuthStatus> = repository.authManager.authStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.repository.AuthStatus())

    fun signInWithGoogle() {
        viewModelScope.launch {
            repository.authManager.signInWithGoogle()
        }
    }

    fun signOutGoogle() {
        repository.authManager.signOut()
    }

    fun linkGoogleAccount(email: String) {
        viewModelScope.launch {
            repository.linkGoogleAccount(email)
        }
    }

    fun updateInviteCode(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateInviteCode(code)
            onResult(success)
        }
    }

    fun setFirebaseSyncEnabled(enabled: Boolean) {
        repository.onlineDbManager.setFirebaseSyncEnabled(enabled)
    }

    fun setRestApiSyncEnabled(enabled: Boolean) {
        repository.onlineDbManager.setRestApiSyncEnabled(enabled)
    }

    fun updateRestSettings(url: String, key: String) {
        repository.onlineDbManager.updateRestSettings(url, key)
    }

    fun backupAllToCloud() {
        viewModelScope.launch {
            repository.onlineDbManager.addLog("در حال جمع‌آوری اطلاعات برای ارسال به دیتابیس ابری...")
            val users = allUsers.value
            val txs = allTransactions.value
            val invs = repository.allInvestments.firstOrNull() ?: emptyList()
            val messages = repository.getAllMessages().firstOrNull() ?: emptyList()
            repository.onlineDbManager.backupAllToCloud(users, txs, invs, messages)
        }
    }

    fun recharge(amount: Double) {
        viewModelScope.launch {
            repository.recharge(amount)
        }
    }

    fun withdraw(amount: Double, address: String, onResult: (WithdrawResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.withdraw(amount, address)
            onResult(result)
        }
    }

    fun register(username: String, password: String, inviteCode: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.registerUser(username, password, inviteCode)
            onResult(success)
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.loginUser(username, password)
            onResult(success)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun simulateIpChange(newIp: String) {
        viewModelScope.launch {
            repository.simulateIpChange(newIp)
        }
    }

    fun transfer(amount: Double, recipient: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.transfer(amount, recipient)
            onResult(success)
        }
    }

    fun purchaseInvestment(title: String, apr: Double, duration: Int, amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.purchaseInvestment(title, apr, duration, amount)
            onResult(success)
        }
    }

    fun upgradeVipLevel(targetLevel: Int, cost: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.upgradeVip(targetLevel, cost)
            onResult(success)
        }
    }

    fun generateInvitationCode() {
        viewModelScope.launch {
            repository.generateInvitationCode()
        }
    }

    fun deleteInvitationCode(code: String) {
        viewModelScope.launch {
            repository.deleteInvitationCode(code)
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = userProfile.value ?: return@launch
            repository.sendMessage("USER", text, user.username)

            // Dynamic typing feedback simulation
            delay(1000)
            val lowercaseText = text.lowercase()
            val replyText = when {
                lowercaseText.contains("سلام") || lowercaseText.contains("hello") || lowercaseText.contains("hi") -> {
                    "سلام! پیام شما به پشتیبان آنلاین پلتفرم اتم ارسال شد. مدیر سیستم به زودی پاسخ شما را در این بخش ارسال خواهد کرد."
                }
                lowercaseText.contains("واریز") || lowercaseText.contains("recharge") -> {
                    "جهت پردازش شارژ حساب، می‌توانید به بخش Recharge مراجعه کرده و آدرس TRC20 را کپی کنید. پس از واریز، موجودی شما بلافاصله شارژ می‌شود."
                }
                else -> null
            }
            if (replyText != null) {
                repository.sendMessage("SUPPORT", replyText, user.username)
            }
        }
    }

    fun createTicket(subject: String, category: String) {
        if (subject.isBlank()) return
        val newId = "T-${Random.nextInt(1000, 9999)}"
        val newTicket = Ticket(newId, subject, category, "OPEN")
        _tickets.value = listOf(newTicket) + _tickets.value
    }

    fun resolveTicket(ticketId: String) {
        _tickets.value = _tickets.value.map {
            if (it.id == ticketId) it.copy(status = "RESOLVED") else it
        }
    }

    fun adminUpdateUserData(
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
        viewModelScope.launch {
            repository.updateUserData(
                username,
                vipLevel,
                balanceUsdt,
                walletAddress,
                totalRecharged,
                invitationCode,
                registeredIp,
                currentIp,
                isIpSuspicious
            )
        }
    }

    fun adminUpdateAnyUser(user: UserEntity) {
        viewModelScope.launch {
            repository.adminUpdateAnyUser(user)
        }
    }

    fun adminApproveWithdrawal(txId: Int, finalAmount: Double) {
        viewModelScope.launch {
            repository.updateTransactionStatus(txId, "COMPLETED", finalAmount)
        }
    }

    fun adminRejectWithdrawal(txId: Int) {
        viewModelScope.launch {
            repository.updateTransactionStatus(txId, "REJECTED")
        }
    }

    fun adminAddTransaction(type: String, amount: Double, network: String, address: String, status: String) {
        viewModelScope.launch {
            repository.addTransaction(type, amount, network, address, status)
        }
    }

    fun adminClearTransactions() {
        viewModelScope.launch {
            repository.clearTransactions()
        }
    }

    fun adminClearInvestments() {
        viewModelScope.launch {
            repository.clearInvestments()
        }
    }

    // Support chat messaging for any selected user
    fun getMessagesForUser(username: String): Flow<List<MessageEntity>> {
        return repository.getMessagesForUser(username)
    }

    fun sendSupportReply(username: String, text: String) {
        if (text.isBlank() || username.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage("SUPPORT", text, username)
        }
    }

    override fun onCleared() {
        super.onCleared()
        priceRefreshJob?.cancel()
    }
}
