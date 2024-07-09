package com.example.pos3

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos3.ui.theme.Pos3Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CashierActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cashier()

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cashier() {
    val drawerItem = listOf(
        DrawerItemss(Icons.Filled.Search, "barcode scanner", 2, true),
        DrawerItemss(Icons.Filled.ShoppingCart, "products", 2, true),
        DrawerItemss(Icons.Filled.Warning, "expired", 2, true),
        DrawerItemss(Icons.Filled.MoreVert, "reports", 2, true),
        DrawerItemss(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "logout", 2, true),
    )

    val context = LocalContext.current

    var selectedItem by remember{
        mutableStateOf(drawerItem[0])
    }

    val drawerState= rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = remember { mutableStateOf("Admin") }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get the name from the document and set it to the userName state
                        val name = document.getString("Name")
                        if (name != null) {
                            userName.value = name
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle the failure here if necessary
                }
        }
    }

    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f).padding(top = 90.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(modifier = Modifier.size(10.dp))
                drawerItem.forEach {
                    NavigationDrawerItem(label = { Text(text = it.text) }
                        , selected = it==selectedItem,
                        onClick = {
                            selectedItem=it

                            scope.launch{
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                        icon = {
                            Icon(imageVector = it.icon, contentDescription = it.text)
                        },
                        badge = {
                            if(it.hasBadge){
                                BadgedBox(badge = {}) {
                                    Badge{
                                        Text(text = it.badgeCount.toString())
                                    }
                                }
                            }
                        })
                }

            }
        }
    }, drawerState=drawerState) {

        Scaffold(topBar = {
            TopAppBar(title = { Text(text = "Cashier's Dashboard") },
                navigationIcon= {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                })
        }) { paddingValues ->

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)){
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

                    Text(text = "Welcome ${userName.value} ", fontSize = 20.sp)


                }

            }
        }
    }

}

data class DrawerItemss(
    val icon: ImageVector,
    val text: String,
    val badgeCount: Int,
    val hasBadge: Boolean
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    Cashier()

}