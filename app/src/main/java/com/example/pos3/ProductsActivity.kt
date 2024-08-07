package com.example.pos3

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.breens.beetablescompose.BeeTablesCompose
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val currentUserRole = getCurrentUserRole()
            setContent {
                Products(userRole = currentUserRole)
            }
        }
    }
    private suspend fun getCurrentUserRole(): String {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Get the current logged-in user
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            val userId = it.uid
            try {
                // Fetch the user document from Firestore
                val userDoc = firestore.collection("Users").document(userId).get().await()
                if (userDoc.exists()) {
                    // Get the role field from the user document
                    val role = userDoc.getString("Role") ?: "unknown"
                    return role
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Return a default role if something goes wrong
        return "unknown"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Products(userRole: String) {
    val products = remember { mutableStateOf<List<Product>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val selectedProduct = remember { mutableStateOf<Product?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            products.value = fetchProductsFromFirestore()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Products") },
            navigationIcon= {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Menu")
                }
            }, )
    }) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center){
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if(userRole == "Admin"){
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showDialog.value = true }, shape = RoundedCornerShape(15.dp)) {
                            Text(text = "Add Product")
                        }
                    }
                }
                if (showDialog.value) {
                    AddProductDialog(onDismiss = { showDialog.value = false }) { newProduct ->
                        scope.launch {
                            addProductToFirestore(newProduct, context)
                            products.value = fetchProductsFromFirestore()
                            showDialog.value = false
                        }
                    }
                }
                if (showEditDialog.value) {
                    selectedProduct.value?.let { product ->
                        EditProductDialog(
                            product = product,
                            onDismiss = { showEditDialog.value = false },
                            onEditProduct = { updatedProduct ->
                                scope.launch {
                                    updateProductInFirestore(updatedProduct, context)
                                    products.value = fetchProductsFromFirestore()
                                    showEditDialog.value = false
                                }
                            },
                            onDeleteProduct = { productId ->
                                scope.launch {
                                    deleteProductFromFirestore(productId, context)
                                    products.value = fetchProductsFromFirestore()
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(15.dp))
                ProductsTable(products = products.value, onEdit = { product ->
                    selectedProduct.value = product
                    showEditDialog.value = true
                }, canEdit = userRole == "Admin")

            }
        }
    }
}

@Composable
fun ProductsTable(products: List<Product>, onEdit: (Product) -> Unit, canEdit: Boolean) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val productList = products.map { product ->
        listOf(
            product.name,
            product.category,
            product.price.toString(),
            product.quantity.toString(),
            product.update?.toDate()?.let { dateFormat.format(it) } ?: "N/A",
            product.expiryDate
        )
    }

    val tableHeaders = if (canEdit) {
        listOf("Name", "Category", "Price", "Quantity", "Updated on", "Expiry Date", "Edit")
    } else {
        listOf("Name", "Category", "Price", "Quantity", "Updated on", "Expiry Date")
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
                val row = productList[rowIndex]
                row.forEachIndexed { cellIndex,cell ->
                    Text(text = cell, modifier = Modifier
                        .weight(1f)
                        .padding(8.dp))
                    if (cellIndex < row.size - 1) {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }
                if (canEdit){
                    IconButton(onClick = {  onEdit(product) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            }
            Divider(color = Color.Gray, thickness = 0.5.dp)
        }
    }

}
suspend fun fetchProductsFromFirestore(): List<Product> {
    val db = FirebaseFirestore.getInstance()
    val products = mutableListOf<Product>()

    try {
        val snapshot = db.collection("Products").get().await()
        products.addAll(snapshot.documents.map { document ->
            Product(
                id = document.id,
                name = document.getString("Name") ?: "",
                category = document.getString("Category") ?: "",
                barcode = document.getString("Barcode") ?: "",
                price = document.getDouble("Price") ?: 0.0,
                create = document.getTimestamp("Create"),
                update = document.getTimestamp("Update"),
                expiryDate = document.getString("ExpiryDate")?: "",
                quantity = document.getLong("Quantity")?.toInt() ?: 0
            )
        })
    } catch (e: FirebaseFirestoreException) {
        Log.d("Firestore", "Error fetching data from Firestore: ${e.message}")
    }

    return products
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onAddProduct: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val context = LocalContext.current
    val categories = remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    val icon = if (expanded){
        Icons.Filled.KeyboardArrowUp
    }else{
        Icons.Filled.KeyboardArrowDown
    }

    LaunchedEffect(Unit) {
        scope.launch {
            categories.value = fetchCategoriesFromFirestore()
        }
    }

    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()

    val date = remember { mutableStateOf("") }
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            date.value = "$dayOfMonth/${month+1}/$year"
        }, year, month, day
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Add New Product", fontWeight = FontWeight.Bold)
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    TextField(value =selectedCategory ,
                        modifier = Modifier.clickable { expanded=!expanded },
                        onValueChange = {selectedCategory=it},
                        label = { Text(text = "Category")},
                        trailingIcon = {
                            Icon(icon, "", modifier = Modifier.clickable { expanded=!expanded } )
                        }, readOnly = true
                    )
                    DropdownMenu(expanded = expanded,
                        onDismissRequest = { expanded=false}
                    ) {
                        categories.value.forEach { cat ->
                            DropdownMenuItem(text = {Text(text = cat) }, onClick = {
                                selectedCategory=cat
                                expanded=false
                            })
                        }

                    }

                }
                TextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") })
                TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                TextField(value = date.value, onValueChange = { date.value = it }, label = { Text("Expiry Date") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(icon, "", modifier = Modifier.clickable {  datePickerDialog.show() } )
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        if (name.isEmpty() || selectedCategory.isEmpty() || barcode.isEmpty() || price.isEmpty()|| quantity.isEmpty() || date.value.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }else{

                            val newProduct = Product("",name, selectedCategory, barcode, price.toDouble(),null, null,  date.value, quantity.toInt())
                            onAddProduct(newProduct)
                        }

                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var selectedCategory by remember { mutableStateOf(product.category) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var expiryDate by remember { mutableStateOf(product.expiryDate) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    val categories = remember { mutableStateOf<List<String>>(emptyList()) }

    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded){
        Icons.Filled.KeyboardArrowUp
    }else{
        Icons.Filled.KeyboardArrowDown
    }

    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            expiryDate = "$dayOfMonth/${month+1}/$year"
        }, year, month, day
    )

    LaunchedEffect(Unit) {
        scope.launch {
            categories.value = fetchCategoriesFromFirestore()
        }
    }

    if (showConfirmDeleteDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showConfirmDeleteDialog = false },
            onConfirm = {
                onDeleteProduct(product.id)
                showConfirmDeleteDialog = false
                onDismiss()
            }
        )
    }


    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Edit Product", fontWeight = FontWeight.Bold)
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    TextField(value =selectedCategory ,
                        modifier = Modifier.clickable { expanded=!expanded },
                        onValueChange = {selectedCategory=it},
                        label = { Text(text = "Category")},
                        trailingIcon = {
                            Icon(icon, "", modifier = Modifier.clickable { expanded=!expanded } )
                        }, readOnly = true
                    )
                    DropdownMenu(expanded = expanded,
                        onDismissRequest = { expanded=false}
                    ) {
                        categories.value.forEach { cat ->
                            DropdownMenuItem(text = {Text(text = cat) }, onClick = {
                                selectedCategory=cat
                                expanded=false
                            })
                        }

                    }

                }
                TextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") })
                TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                TextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Expiry Date") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(icon, "", modifier = Modifier.clickable {  datePickerDialog.show() } )
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        showConfirmDeleteDialog = true
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                    Spacer(modifier = Modifier.size(65.dp))
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        if (name.isEmpty() || selectedCategory.isEmpty() || barcode.isEmpty() || price.isEmpty() || quantity.isEmpty() || expiryDate.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            val updatedProduct = product.copy(
                                name = name,
                                category = selectedCategory,
                                barcode = barcode,
                                price = price.toDouble(),
                                quantity = quantity.toInt(),
                                expiryDate = expiryDate
                            )
                            onEditProduct(updatedProduct)
                        }
                    }) {
                        Text("Edit")
                    }


                }
            }
        }
    }
}

suspend fun fetchCategoriesFromFirestore(): List<String> {
    val db = FirebaseFirestore.getInstance()
    val categories = mutableListOf<String>()

    try {
        val snapshot = db.collection("Categories").get().await()
        categories.addAll(snapshot.documents.map { document ->
            document.getString("category name") ?: ""
        })
    } catch (e: FirebaseFirestoreException) {
        Log.d("Firestore", "Error fetching data from Firestore: ${e.message}")
    }

    return categories
}


suspend fun addProductToFirestore(product: Product, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val currentTimestamp = com.google.firebase.Timestamp(java.util.Date())


    val productData = hashMapOf(
        "Name" to product.name,
        "Category" to product.category,
        "Barcode" to product.barcode,
        "Price" to product.price,
        "Create" to currentTimestamp,
        "Update" to currentTimestamp,
        "ExpiryDate" to product.expiryDate,
        "Quantity" to product.quantity
    )

    db.collection("Products")
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

suspend fun updateProductInFirestore(product: Product, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val productData = hashMapOf(
        "Name" to product.name,
        "Category" to product.category,
        "Barcode" to product.barcode,
        "Price" to product.price,
        "ExpiryDate" to product.expiryDate,
        "Quantity" to product.quantity,
        "Update" to com.google.firebase.Timestamp(java.util.Date())
    )

    db.collection("Products").document(product.id)
        .set(productData)
        .addOnSuccessListener {
            Log.d("Firestore", "DocumentSnapshot successfully updated!")
            Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error updating document", e)
            Toast.makeText(context, "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

suspend fun deleteProductFromFirestore(productId: String, context: Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection("Products").document(productId)
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "DocumentSnapshot successfully deleted!")
            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error deleting document", e)
            Toast.makeText(context, "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Confirm Deletion", fontWeight = FontWeight.Bold)
                Text(text = "Are you sure you want to delete this product?")
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onConfirm() }) {
                        Text("Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}
@Keep
data class Product(
    val id: String = "",
    val name: String,
    val category: String,
    val barcode: String,
    val price: Double,
    val create: com.google.firebase.Timestamp?,
    val update: com.google.firebase.Timestamp?,
    val expiryDate: String,
    val quantity: Int
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview6() {
    //Products()
}