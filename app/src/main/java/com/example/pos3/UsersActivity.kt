package com.example.pos3

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pos3.ui.theme.Pos3Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           UsersScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen() {
    val users = remember { mutableStateOf<List<User>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        scope.launch {
            users.value = fetchUsersFromFirestore()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Users") },
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
                        Text(text = "Add User")
                    }
                }
                if (showDialog.value) {
                    AddUserDialog(onDismiss = { showDialog.value = false }) { newUser ->
                        scope.launch {
                            addUserToFirestore(newUser, context)
                            users.value = fetchUsersFromFirestore()
                            showDialog.value = false
                        }
                    }
                }
                UsersList(users = users.value)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersList(users: List<User>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(users) { user ->
            UserDetails(user = user)
        }
    }
}

@Composable
fun UserDetails(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Name: ${user.name}")
                Text(text = "Email: ${user.email}")
                Text(text = "Phone number: ${user.phone}")
                Text(text = "Role: ${user.role}")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(painter = painterResource(id = R.drawable.baseline_delete_24), contentDescription =null )

            }

        }

    }
}

@Composable
fun AddUserDialog(onDismiss: () -> Unit, onAddUser: (User) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val role by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                Text(text = "Add New User", fontWeight = FontWeight.Bold)
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone number") })
                Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    TextField(value =selectedItem ,
                        modifier = Modifier.clickable { expanded=!expanded },
                        onValueChange = {selectedItem=it},
                        label = { Text(text = "Select Role")},
                        trailingIcon = {
                            Icon(icon, "", modifier = Modifier.clickable { expanded=!expanded } )
                        }, readOnly = true
                        )
                    DropdownMenu(expanded = expanded,
                        onDismissRequest = { expanded=false}
                    ) {

                        list.forEach { label ->
                            DropdownMenuItem(text = {Text(text = label) }, onClick = {
                                selectedItem=label
                                expanded=false
                            })
                        }

                    }

                }
                TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedItem.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }else{
                            val newUser = User(name, email, phone, selectedItem, password)
                            onAddUser(newUser)
                        }

                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

suspend fun addUserToFirestore(user: User, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    auth.createUserWithEmailAndPassword(user.email,user.password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val current =auth.currentUser
                val userId = current?.uid
                // Create a new document in Firestore with user data
                val userData = hashMapOf(
                    "Name" to user.name,
                    "Email" to user.email,
                    "Phone number" to user.phone,
                    "Role" to user.role
                )
                userId?.let {
                    db.collection("Users").document(it)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(ControlsProviderService.TAG, "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(ControlsProviderService.TAG, "Error writing document", e)
                        }
                }

                Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
            } else {
                // User registration failed, display error message
                Toast.makeText(context, "Try Again: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}



suspend fun deleteUserFromFirestore(userId: String, context: Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection("Users").document(userId)
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
fun ConfirmDeleteUserDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Confirm Deletion", fontWeight = FontWeight.Bold)
                Text(text = "Are you sure you want to delete this user?")
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

data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val password: String = ""
)

suspend fun fetchUsersFromFirestore(): List<User> {
    val db = FirebaseFirestore.getInstance()
    val users = mutableListOf<User>()

    try {
        val snapshot = db.collection("Users").get().await()
        users.addAll(snapshot.documents.map { document ->
            User(
                name = document.getString("Name") ?: "",
                email = document.getString("Email") ?: "",
                phone = document.getString("Phone number") ?: "",
                role = document.getString("Role") ?: ""
            )
        })
    } catch (e: FirebaseFirestoreException) {
        Log.d(TAG, "Error fetching data from Firestore: ${e.message}")
    }

    return users
}

@Preview(showBackground = true)
@Composable
fun UsersPreview() {
    Pos3Theme {
        UsersScreen()
    }
}