package com.example.data.network

import com.example.data.database.UserEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.InvestmentEntity
import com.example.data.database.MessageEntity
import com.example.data.database.InvitationCodeEntity
import retrofit2.Response
import retrofit2.http.*

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

interface RemoteApiService {

    // Users
    @GET("users.php")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserEntity>>>

    @POST("login.php")
    suspend fun login(@Body body: Map<String, String>): Response<ApiResponse<UserEntity>>

    @POST("register.php")
    suspend fun register(@Body body: Map<String, String>): Response<ApiResponse<UserEntity>>

    @POST("update_user.php")
    suspend fun updateUser(@Body user: UserEntity): Response<ApiResponse<Boolean>>

    // Transactions
    @GET("transactions.php")
    suspend fun getTransactions(@Query("username") username: String): Response<ApiResponse<List<TransactionEntity>>>

    @POST("add_transaction.php")
    suspend fun addTransaction(@Body transaction: TransactionEntity): Response<ApiResponse<Boolean>>

    // Investments
    @GET("investments.php")
    suspend fun getInvestments(@Query("username") username: String): Response<ApiResponse<List<InvestmentEntity>>>

    @POST("add_investment.php")
    suspend fun addInvestment(@Body investment: InvestmentEntity): Response<ApiResponse<Boolean>>

    // Messages (Support Chat)
    @GET("messages.php")
    suspend fun getMessages(@Query("username") username: String): Response<ApiResponse<List<MessageEntity>>>

    @POST("send_message.php")
    suspend fun sendMessage(@Body message: MessageEntity): Response<ApiResponse<Boolean>>

    // Invitation Codes
    @GET("invite_codes.php")
    suspend fun getInviteCodes(): Response<ApiResponse<List<InvitationCodeEntity>>>

    @POST("add_invite_code.php")
    suspend fun addInviteCode(@Body code: InvitationCodeEntity): Response<ApiResponse<Boolean>>

    @DELETE("delete_invite_code.php")
    suspend fun deleteInviteCode(@Query("code") code: String): Response<ApiResponse<Boolean>>
}
