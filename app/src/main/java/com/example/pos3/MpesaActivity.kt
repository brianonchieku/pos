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


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview8() {
    Mpesa()

}