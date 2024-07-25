package com.example.pos3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MpesaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mpesa()
        }
    }
}
@Composable
fun Mpesa() {

}

/*interface DarajaApiService {

    @FormUrlEncoded
    @POST("oauth/v1/generate")
    suspend fun generateToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): TokenResponse

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiatePayment(
        @Header("Authorization") authHeader: String,
        @Body paymentRequest: PaymentRequest
    ): PaymentResponse
}

class MpesaRepository(private val apiService: DarajaApiService) {

    suspend fun getAccessToken(consumerKey: String, consumerSecret: String): String {
        val credentials = "$consumerKey:$consumerSecret"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        val response = apiService.generateToken(authHeader)
        return response.accessToken
    }

    suspend fun initiateMpesaPayment(token: String, paymentRequest: PaymentRequest): PaymentResponse {
        val authHeader = "Bearer $token"
        return apiService.initiatePayment(authHeader, paymentRequest)
    }
}

class MpesaViewModel(private val repository: MpesaRepository) : ViewModel() {

    private val _paymentResponse = MutableLiveData<PaymentResponse>()
    val paymentResponse: LiveData<PaymentResponse> = _paymentResponse

    fun initiatePayment(phoneNumber: String, amount: Double, consumerKey: String, consumerSecret: String) {
        viewModelScope.launch {
            try {
                val token = repository.getAccessToken(consumerKey, consumerSecret)
                val timestamp = getCurrentTimestamp()
                val password = generatePassword(BUSINESS_SHORTCODE, PASSKEY, timestamp)

                val paymentRequest = PaymentRequest(
                    BusinessShortCode = BUSINESS_SHORTCODE,
                    Password = password,
                    Timestamp = timestamp,
                    TransactionType = "CustomerPayBillOnline",
                    Amount = amount,
                    PartyA = phoneNumber,
                    PartyB = BUSINESS_SHORTCODE,
                    PhoneNumber = phoneNumber,
                    CallBackURL = CALLBACK_URL,
                    AccountReference = "REF123",
                    TransactionDesc = "Payment for XYZ"
                )

                val response = repository.initiateMpesaPayment(token, paymentRequest)
                _paymentResponse.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun generatePassword(shortcode: String, passkey: String, timestamp: String): String {
        val password = "$shortcode$passkey$timestamp"
        return Base64.encodeToString(password.toByteArray(), Base64.NO_WRAP)
    }

    companion object {
        private const val BUSINESS_SHORTCODE = "YOUR_SHORTCODE"
        private const val PASSKEY = "YOUR_PASSKEY"
        private const val CALLBACK_URL = "https://your_callback_url.com/callback"
    }
}



data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class PaymentRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String,
    val Amount: Double,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

data class PaymentResponse(
    @SerializedName("MerchantRequestID") val merchantRequestID: String,
    @SerializedName("CheckoutRequestID") val checkoutRequestID: String,
    @SerializedName("ResponseCode") val responseCode: String,
    @SerializedName("ResponseDescription") val responseDescription: String,
    @SerializedName("CustomerMessage") val customerMessage: String
)
private lateinit var mpesaViewModel: MpesaViewModel
// Create the repository and factory
val retrofit = Retrofit.Builder()
    .baseUrl("https://sandbox.safaricom.co.ke/") // or your base URL
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(DarajaApiService::class.java)
val repository = MpesaRepository(apiService)
val factory = MpesaViewModelFactory(repository)

// Initialize the ViewModel using the factory
mpesaViewModel = ViewModelProvider(this, factory).get(MpesaViewModel::class.java)


@Preview(showBackground = true)
@Composable
fun GreetingPreview8() {
    Mpesa()
}*/