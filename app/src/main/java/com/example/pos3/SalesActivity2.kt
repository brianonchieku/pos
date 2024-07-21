package com.example.pos3

import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            onProceedClick = { /* Handle proceed action */ },
            paymentMethod = paymentMethod
        )
        // Add more UI elements here
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
                }, trailingIcon = {
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
}

data class ProductSale2(
    val name: String,
    val price: Double,
    val quantity: Int = 1)

@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
   Sales2(viewModel = SalesViewModel2())
}