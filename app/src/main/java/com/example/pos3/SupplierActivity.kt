package com.example.pos3

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.service.controls.ControlsProviderService
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pos3.ui.theme.Pos3Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SupplierActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Supplier()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Supplier() {
    val suppliers = remember { mutableStateOf<List<Supplier>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        scope.launch {
            suppliers.value = fetchSuppliersFromFirestore()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Suppliers") },
            navigationIcon= {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Menu")
                }
            }, )
    }) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center){
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { showDialog.value = true }, shape = RoundedCornerShape(15.dp)) {
                        Text(text = "Add Supplier")
                    }
                }
                if (showDialog.value) {
                    AddSupplierDialog(onDismiss = { showDialog.value = false }) { newSupplier ->
                        scope.launch {
                            addSupplierToFirestore(newSupplier, context)
                            suppliers.value = fetchSuppliersFromFirestore()
                            showDialog.value = false
                        }
                    }
                }
                SuppliersList(suppliers = suppliers.value)

            }
        }
    }
}

@Composable
fun SuppliersList(suppliers: List<Supplier>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(suppliers) { supplier ->
            SupplierDetails(supplier = supplier)
        }
    }

}

@Composable
fun SupplierDetails(supplier: Supplier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Name: ${supplier.name}")
            Text(text = "Email: ${supplier.email}")
            Text(text = "Phone number: ${supplier.phone}")
            Text(text = "Product: ${supplier.product}")
        }
    }
}

@Composable
fun AddSupplierDialog(onDismiss: () -> Unit, onAddSupplier: (Supplier) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val list = listOf(
        "Admin",
        "Cashier"
    )
    var selectedItem by remember { mutableStateOf("") }

    val icon = if (expanded){
        Icons.Filled.KeyboardArrowUp
    }else{
        Icons.Filled.KeyboardArrowDown
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Add New Supplier", fontWeight = FontWeight.Bold)
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone number") })
                TextField(value = product, onValueChange = { product = it }, label = { Text("Product") })

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || product.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }else{
                            val newSupplier = Supplier(name, email, phone, product)
                            onAddSupplier(newSupplier)
                        }

                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

suspend fun addSupplierToFirestore(supplier: Supplier, context: Context) {
    val db = FirebaseFirestore.getInstance()

    val supplierData = hashMapOf(
        "Name" to supplier.name,
        "Email" to supplier.email,
        "Phone number" to supplier.phone,
        "Product" to supplier.product
    )

    db.collection("Suppliers")
        .add(supplierData)
        .addOnSuccessListener {
            Log.d("Firestore", "DocumentSnapshot successfully written!")
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing document", e)
            Toast.makeText(context, "Error adding supplier: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

data class Supplier(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val product: String = ""
)

suspend fun fetchSuppliersFromFirestore(): List<Supplier> {
    val db = FirebaseFirestore.getInstance()
    val suppliers = mutableListOf<Supplier>()

    try {
        val snapshot = db.collection("Suppliers").get().await()
        suppliers.addAll(snapshot.documents.map { document ->
            Supplier(
                name = document.getString("Name") ?: "",
                email = document.getString("Email") ?: "",
                phone = document.getString("Phone number") ?: "",
                product = document.getString("Product") ?: ""
            )
        })
    } catch (e: FirebaseFirestoreException) {
        Log.d(TAG, "Error fetching data from Firestore: ${e.message}")
    }

    return suppliers
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    Supplier()
}