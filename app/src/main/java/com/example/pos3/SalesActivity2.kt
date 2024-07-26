package com.example.pos3

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class SalesActivity2 : ComponentActivity() {
    private val salesViewModel: SalesViewModel2 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sales2(salesViewModel)
        }
    }
}

@Composable
fun Sales2(viewModel: SalesViewModel2) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedProducts by remember { mutableStateOf(products) }
    var paymentMethod by remember { mutableStateOf("") }
    var showCashDialog by remember { mutableStateOf(false) }
    var showMPesaDialog by remember { mutableStateOf(false) }
    val context= LocalContext.current
    val subtotal = selectedProducts.sumOf { it.price * it.quantity }

    fun proceedWithSale() {
        val sale = Sale(
            transactionId = UUID.randomUUID().toString(),
            date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            amount = subtotal,
            paymentMethod = paymentMethod,
            items = selectedProducts,
            employeeId = FirebaseAuth.getInstance().currentUser?.uid,
            status = "completed"
        )
        viewModel.submitSale(sale, context)
    }



    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box (modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = colorResource(id = R.color.purple_200),
                    shape = RoundedCornerShape(bottomEnd = 40.dp, bottomStart = 40.dp)
                ), contentAlignment = Alignment.Center){
                Text(text = "POS", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            SearchBar(
                query = searchQuery,
                items = products,
                onQueryChanged = { searchQuery = it },
                onProductSelected = { product ->
                    if (selectedProducts.none { it.name == product.name }) {
                        selectedProducts = selectedProducts + product
                    }
                }
            )
            ProductTable2(
                products = selectedProducts,
                onQuantityChange = { product, quantity ->
                    selectedProducts = selectedProducts.map {
                        if (it.name == product.name) it.copy(quantity = quantity) else it
                    }.filter { it.quantity > 0 }
                },
                onPaymentMethodChange = { paymentMethod = it },
                onProceedClick = {
                    if (selectedProducts.isNotEmpty() && paymentMethod.isNotEmpty()) {
                        when (paymentMethod) {
                            "Cash" -> showCashDialog = true
                            "Mobile Payment" -> showMPesaDialog = true
                            "" -> Toast.makeText(context, "Select Payment Method", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(context, "Please select products", Toast.LENGTH_SHORT).show()
                    }
                },
                paymentMethod = paymentMethod
            )
            // Add more UI elements here
        }
        if (showCashDialog) {
            CashDialog2(
                subtotal = subtotal,
                onDismiss = { showCashDialog = false },
                onPay = {
                    proceedWithSale()
                    showCashDialog = false
                }
            )
        }
        if (showMPesaDialog) {
            MPesaDialog(
                subtotal = subtotal,
                onDismiss = { showMPesaDialog = false }
            )
        }
    }



}
@Composable
fun SearchBar(
    query: String,
    items: List<ProductSale2>,
    onQueryChanged: (String) -> Unit,
    onProductSelected: (ProductSale2) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = query,
        onValueChange = {
            onQueryChanged(it)
            expanded = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        label = { Text("Search") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        }
    )
    if (expanded) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            items.filter { it.name.contains(query, ignoreCase = true) }.forEach { item ->
                Text(
                    text = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onProductSelected(item)
                            onQueryChanged("")
                            expanded = false
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTable2(
    products: List<ProductSale2>,
    onQuantityChange: (ProductSale2, Int) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onProceedClick: () -> Unit,
    paymentMethod: String
) {
    val tableHeaders = listOf("Name", "Quantity", "Price", "Total")
    val subtotal = products.sumOf { it.price * it.quantity }

    var expanded by remember { mutableStateOf(false) }
    val list = listOf(
        "Cash",
        "Mobile Payment"
    )
    val icon = if (expanded){
        Icons.Filled.KeyboardArrowUp
    }else{
        Icons.Filled.KeyboardArrowDown
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row {
            tableHeaders.forEachIndexed { index,header ->
                Text(text = header, modifier = Modifier
                    .weight(1f)
                    .padding(8.dp), fontWeight = FontWeight.Bold)
                if(index < tableHeaders.size-1){
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
        Divider(color = Color.Black, thickness = 1.dp)

        products.forEachIndexed { rowIndex,product ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = product.name, modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
                Row(modifier = Modifier.weight(1f),verticalAlignment = Alignment.CenterVertically) {
                    Text(text = product.quantity.toString(), modifier = Modifier
                        .padding(8.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 2.dp, end = 2.dp)
                    ) {
                        IconButton(onClick = {
                            onQuantityChange(product, product.quantity + 1)
                        },
                            modifier = Modifier
                                .size(10.dp)) {
                            Icon(painter = painterResource(id = R.drawable.baseline_keyboard_arrow_up_24), contentDescription =null )
                        }
                        IconButton(onClick = {
                            if (product.quantity > 1) {
                                onQuantityChange(product, product.quantity - 1)
                            }else {
                                // Remove the item if quantity is 1
                                onQuantityChange(product, 0)
                            }
                        },
                            modifier = Modifier
                                .size(10.dp)) {
                            Icon(painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24), contentDescription = null)
                        }
                    }
                }

                Text(text = product.price.toString(), modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
                Text(text = (product.price * product.quantity).toString(), modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
            }
            Divider(color = Color.Gray, thickness = 0.5.dp)
        }
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Spacer(modifier = Modifier.weight(2f))
            Text(
                text = "Subtotal: ",
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtotal.toString(),
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.size(20.dp))
        Box(modifier = Modifier
            .wrapContentSize()
            .background(colorResource(id = R.color.white)), contentAlignment = Alignment.Center) {
            TextField(value =paymentMethod , onValueChange = onPaymentMethodChange,
                label = {
                    Text(text = "select payment method")
                }, readOnly = true,
                trailingIcon = {
                    Icon(icon, "", modifier = Modifier.clickable { expanded=!expanded } )
                }, modifier = Modifier
                    .wrapContentSize()
                    .background(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent))
            DropdownMenu(expanded = expanded,
                onDismissRequest = { expanded=false}
            ) {
                list.forEach { label ->
                    DropdownMenuItem(text = {Text(text = label) }, onClick = {
                        onPaymentMethodChange(label)
                        expanded=false
                    })

                }
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        Button(onClick = onProceedClick, modifier = Modifier
            .padding(vertical = 16.dp)) {
            Text(text = "Proceed")
        }

    }
}

class SalesViewModel2 : ViewModel() {
    private val _products = MutableStateFlow(listOf<ProductSale2>())
    val products: StateFlow<List<ProductSale2>> = _products

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val productsList = mutableListOf<ProductSale2>()

            try {
                val snapshot = db.collection("Products").get().await()
                productsList.addAll(snapshot.documents.map { document ->
                    ProductSale2(
                        name = document.getString("Name") ?: "",
                        price = document.getDouble("Price") ?: 0.0
                    )
                })
                _products.value = productsList
            } catch (e: Exception) {
                // Handle the error
            }
        }
    }
    fun submitSale(sale: Sale, context: Context) {
        val db = FirebaseFirestore.getInstance()
        viewModelScope.launch {
            try {
                db.collection("Sales").add(sale).await()
                Toast.makeText(context, "Sale submitted successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error submitting sale: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CashDialog2(onDismiss: () -> Unit,  subtotal: Double,  onPay: () -> Unit){
    var cashGiven by remember {mutableStateOf("") }
    var change by remember {mutableStateOf("") }

    LaunchedEffect(cashGiven) {
        val cashGivenValue = cashGiven.toDoubleOrNull() ?: 0.0
        change = (cashGivenValue - subtotal).toString()
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Cash Payment", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(value = cashGiven,
                    onValueChange ={ cashGiven=it},
                    label = { Text("Cash Given")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = "Sub Total: $subtotal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = "Change: $change",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = colorResource(id = R.color.purple_200))
                    }
                    TextButton(onClick = onPay) {
                        Text("Pay", color = colorResource(id = R.color.purple_500))
                    }
                }
            }
        }
    }
}

@Composable
fun MPesaDialog(onDismiss: () -> Unit, subtotal: Double) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "M-Pesa Payment", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Sub Total: $subtotal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = colorResource(id = R.color.purple_200))
                    }
                    TextButton(onClick = { }) {
                        Text("Pay", color = colorResource(id = R.color.purple_500))
                    }
                }
            }
        }
    }
}

suspend fun addSaleToFirestore(sale: Sale, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val currentTimestamp = com.google.firebase.Timestamp(java.util.Date())


    val productData = hashMapOf(
        "transaction_id" to sale.transactionId,
        "date" to currentTimestamp,
        "amount" to sale.amount,
        "paymentMethod" to sale.paymentMethod,
        "items" to sale.items,
        "cashier_name" to sale.employeeId
    )

    db.collection("Sales")
        .add(productData)
        .addOnSuccessListener {
            Log.d("Firestore", "DocumentSnapshot successfully written!")
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing document", e)
            Toast.makeText(context, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}


data class ProductSale2(
    val name: String,
    val price: Double,
    val quantity: Int = 1)

data class Sale(
    val transactionId: String,
    val date: String,
    val amount: Double,
    val paymentMethod: String,
    val items: List<ProductSale2>,
    val employeeId: String?,
    val status: String
)



@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
  //Sales2(viewModel = SalesViewModel2())
}