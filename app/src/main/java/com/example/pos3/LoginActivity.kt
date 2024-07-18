package com.example.pos3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos3.ui.theme.Pos3Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Login()

        }
    }
}

@Composable
fun Login() {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        var loading by remember { mutableStateOf(false) }
        Image(painter = painterResource(id = R.drawable.lavender3)
            , contentDescription =null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop )
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Welcome back")
            Card(modifier = Modifier
                .wrapContentSize()
                .padding(30.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    val context= LocalContext.current
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()



                    var expanded by remember { mutableStateOf(false) }
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


                    Spacer(modifier = Modifier.size(20.dp))
                    Text(text = "Login", fontWeight = FontWeight.Bold, fontSize = 30.sp)
                    Spacer(modifier = Modifier.size(20.dp))
                    OutlinedTextField(value = email, onValueChange ={ email = it},
                        label = { Text(text = "Email")},
                        leadingIcon = {
                            Icon(painterResource(id = R.drawable.baseline_email_24), contentDescription =null )
                        })
                    Spacer(modifier = Modifier.size(20.dp))
                    Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                        OutlinedTextField(value =selectedItem ,
                            onValueChange = {selectedItem=it},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            label = { Text(text = "Select Role")},
                            trailingIcon = {
                                Icon(icon, "", modifier = Modifier.clickable { expanded=!expanded } )
                            }, readOnly = true,
                            leadingIcon = {
                                Icon(painterResource(id = R.drawable.baseline_person_24), contentDescription =null )
                            })
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


                    Spacer(modifier = Modifier.size(20.dp))

                    OutlinedTextField(value = password, onValueChange ={ password = it},
                        label = { Text(text = "Password")},
                        leadingIcon = {
                            Icon(painterResource(id = R.drawable.baseline_lock_24), contentDescription =null )
                        }, visualTransformation = PasswordVisualTransformation())

                    Spacer(modifier = Modifier.size(20.dp))
                    Button(onClick ={
                        if (email.isNotEmpty() && password.isNotEmpty() && selectedItem.isNotEmpty()) {
                            loading = true
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val userId = user.uid

                                        db.collection("Users").document(userId).get()
                                            .addOnSuccessListener { document ->
                                                if (document != null && document.exists()) {
                                                    val userRole = document.getString("Role")
                                                    if (userRole == selectedItem) {
                                                        loading = false
                                                        if (selectedItem == "Admin") {
                                                            val intent = Intent(context, MainActivity::class.java)
                                                            context.startActivity(intent)
                                                        }else{
                                                            val intent = Intent(context, CashierActivity::class.java)
                                                            context.startActivity(intent)
                                                        }
                                                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                                        email = ""
                                                        password = ""
                                                        selectedItem = ""
                                                    } else {
                                                        loading = false
                                                        Toast.makeText(context, "Role mismatch", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    loading = false
                                                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        loading = false
                                        Toast.makeText(context, "Login Failed: User does not exist", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    loading = false
                                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {

                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }


                    }
                    , modifier = Modifier.width(200.dp) ) {
                        Text(text = "Login", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.size(20.dp))

                }



            }
            Column(modifier = Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (loading) {
                    CircularProgressIndicator()
                }

            }


        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
   Login()
}