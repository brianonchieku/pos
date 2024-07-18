package com.example.pos3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SalesActivity : ComponentActivity() {
    private val salesViewModel: SalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalesScreen(salesViewModel)
        }
    }
}
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val products by viewModel.products.collectAsState()
    val productNames = products.map { it.name }
    Sales(items = productNames,products=products)
}
@Composable
fun Sales(products: List<ProductSale>,items: List<String>) {
    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            var query by remember { mutableStateOf("") }
            var filteredItems by remember { mutableStateOf(items) }
            var expanded by remember { mutableStateOf(false) }
            var selectedProducts by remember { mutableStateOf(listOf<ProductSale>()) }

            // Update the filtered list whenever the query changes
            LaunchedEffect(query) {
                filteredItems = if (query.isEmpty()) {
                    items
                } else {
                    items.filter { it.contains(query, ignoreCase = true) }
                }
            }

            Column {
                SearchBar(query, expanded, onQueryChanged = {
                    query = it
                    expanded = true
                }, onTextFieldFocused = { expanded = true })

                DropdownMenu(
                    expanded = expanded && filteredItems.isNotEmpty(),
                    onDismissRequest = { expanded = false }
                ) {
                    filteredItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                val selectedProduct = products.firstOrNull { it.name == item }
                                if (selectedProduct != null) {
                                    selectedProducts = selectedProducts + selectedProduct
                                }
                                query = item
                                expanded = false
                            }
                        )
                    }
                }
                ProductTable(products = selectedProducts, onQuantityChange = { product, newQuantity ->
                    selectedProducts = selectedProducts.map {
                        if (it.name == product.name) it.copy(quantity = newQuantity) else it
                    }.filter { it.quantity>0 } //remove item from table
                })
            }
        }
    }
}
@Composable
fun SearchBar(query: String,expanded: Boolean, onQueryChanged: (String) -> Unit,
              onTextFieldFocused: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            },

        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        }
    )
    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
}
@Composable
fun ProductTable(products: List<ProductSale>, onQuantityChange: (ProductSale, Int) -> Unit) {

    val tableHeaders = listOf("Name", "Quantity", "Price", "Total")
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
                Text(text = product.quantity.toString(), modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
                Text(text = product.price.toString(), modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
                Text(text = (product.price * product.quantity).toString(), modifier = Modifier
                    .weight(1f)
                    .padding(8.dp))
                Row(modifier = Modifier.weight(1f)) {
                    IconButton(onClick = {
                        if (product.quantity > 1) {
                            onQuantityChange(product, product.quantity - 1)
                        }else {
                            // Remove the item if quantity is 1
                            onQuantityChange(product, 0)
                        }
                    },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)) {
                        Icon(painter = painterResource(id = R.drawable.baseline_minimize_24), contentDescription = null)
                    }
                    IconButton(onClick = {
                        onQuantityChange(product, product.quantity + 1)
                    },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            }
            Divider(color = Color.Gray, thickness = 0.5.dp)
        }
    }
}
suspend fun fetchProducts(): List<ProductSale> {
    val db = FirebaseFirestore.getInstance()
    val products = mutableListOf<ProductSale>()
    try {
        val snapshot = db.collection("Products").get().await()
        products.addAll(snapshot.documents.map { document ->
            ProductSale(
                name = document.getString("Name") ?: "",
                price = document.getDouble("Price") ?: 0.0
            )
        })
    } catch (e: FirebaseFirestoreException) {
        Log.d("Firestore", "Error fetching data from Firestore: ${e.message}")
    }
    return products
}

class SalesViewModel : ViewModel() {
    private val _products = MutableStateFlow(listOf<ProductSale>())
    val products: StateFlow<List<ProductSale>> = _products

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = fetchProducts()
        }
    }


}

data class ProductSale(
    val name: String,
    val price: Double,
    val quantity: Int = 1)


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    SalesScreen(SalesViewModel())
}