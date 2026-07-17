package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.UserEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.InvestmentEntity
import com.example.data.database.MessageEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class DatabaseSyncStatus(
    val isLocalActive: Boolean = true,
    val isFirebaseConfigured: Boolean = false,
    val isFirebaseSyncEnabled: Boolean = false,
    val isRestApiSyncEnabled: Boolean = false,
    val restApiUrl: String = "https://api.jsonbin.io/v3/b",
    val restApiKey: String = "",
    val lastSyncTimestamp: Long = 0L,
    val syncLog: List<String> = emptyList(),
    val connectionMessage: String = "پایگاه داده محلی فعال است."
)

class OnlineDatabaseManager(private val context: Context) {
    private val _syncStatus = MutableStateFlow(DatabaseSyncStatus())
    val syncStatus: StateFlow<DatabaseSyncStatus> = _syncStatus.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    init {
        // Load settings from SharedPreferences
        val prefs = context.getSharedPreferences("online_db_prefs", Context.MODE_PRIVATE)
        val isFirebaseEnabled = prefs.getBoolean("firebase_sync_enabled", false)
        val isRestEnabled = prefs.getBoolean("rest_sync_enabled", false)
        val restUrl = prefs.getString("rest_api_url", "https://api.jsonbin.io/v3/b") ?: "https://api.jsonbin.io/v3/b"
        val restKey = prefs.getString("rest_api_key", "") ?: ""

        _syncStatus.value = DatabaseSyncStatus(
            isFirebaseSyncEnabled = isFirebaseEnabled,
            isRestApiSyncEnabled = isRestEnabled,
            restApiUrl = restUrl,
            restApiKey = restKey
        )

        checkFirebaseInitialization()
    }

    private fun checkFirebaseInitialization() {
        try {
            // Safer check: look for the generated resource ID that only exists if google-services.json was present
            val googleAppIdResId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            val isConfigured = googleAppIdResId != 0

            if (isConfigured) {
                try {
                    // Try to initialize FirebaseApp if not already done
                    try {
                        FirebaseApp.getInstance()
                    } catch (e: Exception) {
                        FirebaseApp.initializeApp(context)
                    }

                    firestore = FirebaseFirestore.getInstance()
                    auth = FirebaseAuth.getInstance()
                    _syncStatus.value = _syncStatus.value.copy(
                        isFirebaseConfigured = true,
                        connectionMessage = "پایگاه داده محلی و دیتابیس ابری فایربیس متصل هستند."
                    )
                    addLog("فایربیس با موفقیت راه‌اندازی و متصل شد.")
                } catch (e: Exception) {
                    Log.e("OnlineDatabaseManager", "Firebase services init failed", e)
                    _syncStatus.value = _syncStatus.value.copy(
                        isFirebaseConfigured = false,
                        connectionMessage = "خطا در دسترسی به سرویس‌های گوگل: ${e.localizedMessage}"
                    )
                }
            } else {
                _syncStatus.value = _syncStatus.value.copy(
                    isFirebaseConfigured = false,
                    isFirebaseSyncEnabled = false
                )
                addLog("پیکربندی فایربیس یافت نشد (نیاز به google-services.json).")
            }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "Firebase init error", e)
            _syncStatus.value = _syncStatus.value.copy(
                isFirebaseConfigured = false,
                isFirebaseSyncEnabled = false
            )
            addLog("خطا در راه‌اندازی فایربیس: ${e.localizedMessage}")
        }
    }

    fun addLog(message: String) {
        val currentLogs = _syncStatus.value.syncLog.toMutableList()
        val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        currentLogs.add(0, "[$timeString] $message")
        if (currentLogs.size > 30) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _syncStatus.value = _syncStatus.value.copy(syncLog = currentLogs)
    }

    fun setFirebaseSyncEnabled(enabled: Boolean) {
        if (enabled && !_syncStatus.value.isFirebaseConfigured) {
            addLog("خطا: فایربیس پیکربندی نشده است. ابتدا فایل google-services.json را قرار دهید.")
            return
        }
        val prefs = context.getSharedPreferences("online_db_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("firebase_sync_enabled", enabled).apply()
        _syncStatus.value = _syncStatus.value.copy(isFirebaseSyncEnabled = enabled)
        addLog("همگام‌سازی فایربیس: ${if (enabled) "فعال" else "غیرفعال"}")
    }

    fun setRestApiSyncEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("online_db_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("rest_sync_enabled", enabled).apply()
        _syncStatus.value = _syncStatus.value.copy(isRestApiSyncEnabled = enabled)
        addLog("همگام‌سازی REST API: ${if (enabled) "فعال" else "غیرفعال"}")
    }

    fun updateRestSettings(url: String, key: String) {
        val prefs = context.getSharedPreferences("online_db_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("rest_api_url", url)
            .putString("rest_api_key", key)
            .apply()
        _syncStatus.value = _syncStatus.value.copy(restApiUrl = url, restApiKey = key)
        addLog("تنظیمات وب دیتابیس به‌روزرسانی شد. آدرس: $url")
    }

    // --- Firebase Sync Helpers ---
    private fun getBaseCollection(): String {
        val uid = auth?.currentUser?.uid ?: "unauthenticated"
        return "user_data/$uid"
    }

    suspend fun syncUserToFirebase(user: UserEntity) = withContext(Dispatchers.IO) {
        if (!_syncStatus.value.isFirebaseSyncEnabled || firestore == null) return@withContext
        try {
            val userMap = hashMapOf(
                "username" to user.username,
                "password" to user.password,
                "isLoggedIn" to user.isLoggedIn,
                "balanceUsdt" to user.balanceUsdt,
                "totalRecharged" to user.totalRecharged,
                "vipLevel" to user.vipLevel,
                "walletAddress" to user.walletAddress,
                "invitationCode" to user.invitationCode,
                "registeredIp" to user.registeredIp,
                "currentIp" to user.currentIp,
                "isIpSuspicious" to user.isIpSuspicious,
                "hasReceivedBonus" to user.hasReceivedBonus,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore?.document(getBaseCollection())?.collection("profile")?.document("current")?.set(userMap)
                ?.addOnSuccessListener {
                    addLog("پروفایل کاربر با دیتابیس ابری گوگل همگام شد.")
                }
                ?.addOnFailureListener { e ->
                    addLog("خطا در همگام‌سازی پروفایل: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "Firebase sync user error", e)
        }
    }

    suspend fun syncTransactionToFirebase(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        if (!_syncStatus.value.isFirebaseSyncEnabled || firestore == null) return@withContext
        try {
            val txMap = hashMapOf(
                "id" to tx.id,
                "type" to tx.type,
                "amount" to tx.amount,
                "status" to tx.status,
                "network" to tx.network,
                "address" to tx.address,
                "timestamp" to tx.timestamp,
                "username" to tx.username
            )

            firestore?.document(getBaseCollection())?.collection("transactions")?.document(tx.id.toString())?.set(txMap)
                ?.addOnSuccessListener {
                    addLog("تراکنش ${tx.id} در فضای امن گوگل ذخیره شد.")
                }
                ?.addOnFailureListener { e ->
                    addLog("خطا در ذخیره تراکنش ابری: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "Firebase sync tx error", e)
        }
    }

    suspend fun syncInvestmentToFirebase(inv: InvestmentEntity) = withContext(Dispatchers.IO) {
        if (!_syncStatus.value.isFirebaseSyncEnabled || firestore == null) return@withContext
        try {
            val invMap = hashMapOf(
                "id" to inv.id,
                "planTitle" to inv.planTitle,
                "apr" to inv.apr,
                "durationDays" to inv.durationDays,
                "amountInvested" to inv.amountInvested,
                "maturityDate" to inv.maturityDate,
                "interestEarned" to inv.interestEarned,
                "status" to inv.status,
                "username" to inv.username,
                "startDate" to inv.startDate
            )

            firestore?.document(getBaseCollection())?.collection("investments")?.document(inv.id.toString())?.set(invMap)
                ?.addOnSuccessListener {
                    addLog("سرمایه‌گذاری در پنل مدیریت گوگل ثبت شد.")
                }
                ?.addOnFailureListener { e ->
                    addLog("خطا در ثبت سرمایه‌گذاری ابری: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "Firebase sync investment error", e)
        }
    }

    suspend fun syncMessageToFirebase(msg: MessageEntity) = withContext(Dispatchers.IO) {
        if (!_syncStatus.value.isFirebaseSyncEnabled || firestore == null) return@withContext
        try {
            val msgMap = hashMapOf(
                "id" to msg.id,
                "sender" to msg.sender,
                "text" to msg.text,
                "timestamp" to msg.timestamp,
                "username" to msg.username
            )

            firestore?.document(getBaseCollection())?.collection("messages")?.document(msg.id.toString())?.set(msgMap)
                ?.addOnSuccessListener {
                    addLog("پیام پشتیبانی در بایگانی امن گوگل ذخیره شد.")
                }
                ?.addOnFailureListener { e ->
                    addLog("خطا در ذخیره پیام در ابر: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "Firebase sync message error", e)
        }
    }

    // --- REST API Web Database Sync Helpers ---
    // This allows syncing data with any online REST server or Mock DB (e.g. JSONBin, mocky, local server etc.)
    suspend fun syncToRestApi(category: String, dataJson: String): Boolean = withContext(Dispatchers.IO) {
        if (!_syncStatus.value.isRestApiSyncEnabled) return@withContext false
        val url = _syncStatus.value.restApiUrl
        if (url.isBlank()) return@withContext false

        try {
            // Build real API request to send to cloud database
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = dataJson.toRequestBody(mediaType)
            
            val requestBuilder = Request.Builder()
                .url("$url/$category")
                .post(requestBody)

            if (_syncStatus.value.restApiKey.isNotBlank()) {
                requestBuilder.addHeader("X-Master-Key", _syncStatus.value.restApiKey)
                requestBuilder.addHeader("X-Access-Key", _syncStatus.value.restApiKey)
            }

            val request = requestBuilder.build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    addLog("اطلاعات $category با وب‌سرویس با موفقیت همگام‌سازی شد.")
                    return@withContext true
                } else {
                    addLog("کد خطای وب‌سرویس برای $category: ${response.code}")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.e("OnlineDatabaseManager", "REST sync error for $category", e)
            addLog("خطای شبکه وب‌سرویس برای $category: ${e.localizedMessage}")
            return@withContext false
        }
    }

    // Complete Database Backup / Export to Cloud Database
    suspend fun backupAllToCloud(
        users: List<UserEntity>,
        transactions: List<TransactionEntity>,
        investments: List<InvestmentEntity>,
        messages: List<MessageEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        addLog("شروع پشتیبان‌گیری سراسری به پایگاه داده ابری...")
        var success = false

        // 1. Firebase Backup
        if (_syncStatus.value.isFirebaseSyncEnabled && firestore != null) {
            try {
                users.forEach { syncUserToFirebase(it) }
                transactions.forEach { syncTransactionToFirebase(it) }
                investments.forEach { syncInvestmentToFirebase(it) }
                messages.forEach { syncMessageToFirebase(it) }
                addLog("پشتیبان‌گیری کامل در فایربیس فایرستور با موفقیت انجام شد.")
                success = true
            } catch (e: Exception) {
                addLog("خطا در پشتیبان‌گیری فایربیس: ${e.localizedMessage}")
            }
        }

        // 2. REST API Backup
        if (_syncStatus.value.isRestApiSyncEnabled) {
            try {
                val userAdapter = moshi.adapter(List::class.java)
                val usersJson = userAdapter.toJson(users.map { 
                    mapOf("username" to it.username, "balance" to it.balanceUsdt, "vip" to it.vipLevel) 
                })
                val txJson = userAdapter.toJson(transactions.map { 
                    mapOf("id" to it.id, "type" to it.type, "amount" to it.amount, "user" to it.username)
                })

                val uSync = syncToRestApi("users_sync", usersJson)
                val tSync = syncToRestApi("transactions_sync", txJson)

                if (uSync || tSync) {
                    addLog("پشتیبان‌گیری کامل در دیتابیس وب با موفقیت ارسال شد.")
                    success = true
                }
            } catch (e: Exception) {
                addLog("خطا در پشتیبان‌گیری وب دیتابیس: ${e.localizedMessage}")
            }
        }

        if (success) {
            _syncStatus.value = _syncStatus.value.copy(
                lastSyncTimestamp = System.currentTimeMillis()
            )
        } else {
            addLog("هیچ دیتابیس ابری فعالی برای پشتیبان‌گیری پیدا نشد.")
        }
        return@withContext success
    }
}
