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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    private val viewModel: SalesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val salesViewModel = SalesViewModel()
            SalesScreen(salesViewModel)
        }
    }
}
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val products by viewModel.products
    Sales(products)
}
@Composable
fun Sales(products: List<ProductSale>) {
    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            var query by remember { mutableStateOf("") }
            var filteredItems by remember { mutableStateOf(products) }
            var expanded by remember { mutableStateOf(false) }


            // Update the filtered list whenever the query changes
            LaunchedEffect(query) {
                filteredItems = if (query.isEmpty()) {
                    products
                } else {
                    products.filter { it.name.contains(query, ignoreCase = true) }
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
                    filteredItems.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Text(text = product.name)
                                    Text(text = " - $${product.price}")
                                }
                            },
                            onClick = {
                                query = product.name
                                expanded = false
                            }
                        )
                    }
                }
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

class SalesViewModel : ViewModel() {
    var products = mutableStateOf<List<ProductSale>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            products.value = fetchProducts()
        }
    }

    private suspend fun fetchProducts(): List<ProductSale> {
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
}

data class ProductSale(val name: String, val price: Double)


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    SalesScreen(SalesViewModel())
}