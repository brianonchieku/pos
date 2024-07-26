package com.example.pos3

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Admin()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Admin() {
    val drawerItem = listOf(
        DrawerItems(Icons.Default.Home, "stores", 0, false),
        DrawerItems(Icons.Filled.AccountCircle, "users", 2, true),
        DrawerItems(Icons.Filled.Place, "suppliers", 20, true),
        DrawerItems(Icons.Filled.ShoppingCart, "products", 2, true),
        DrawerItems(Icons.Filled.Search, "POS", 2, true),
        DrawerItems(Icons.Filled.MoreVert, "reports", 2, true),
        DrawerItems(Icons.Filled.Warning, "expired", 2, true),
        DrawerItems(Icons.Filled.KeyboardArrowLeft, "logout", 2, true),
    )

    val context = LocalContext.current


    var selectedItem by remember{
        mutableStateOf(drawerItem[0])
    }

    val drawerState= rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = remember { mutableStateOf("Admin") }
    val itemCounts = remember { mutableStateOf(mapOf<String, Int>()) }


    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("Name")
                        if (name != null) {
                            userName.value = name
                        }
                    }
                }
                .addOnFailureListener {}

            fetchCounts(db, itemCounts)
        }
    }

    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy   HH:mm")
    val formattedDateTime = currentDateTime.format(formatter)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentDateTime = LocalDateTime.now()
        }
    }


    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet(modifier = Modifier
            .fillMaxWidth(0.75f)
            .padding(top = 90.dp)) {
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
                            if(it.text=="users"){
                                val intent= Intent(context, UsersActivity::class.java)
                                context.startActivity(intent)
                            }
                            if(it.text=="suppliers"){
                                val intent= Intent(context, SupplierActivity::class.java)
                                context.startActivity(intent)
                            }
                            if(it.text=="products"){
                                val intent= Intent(context, ProductsActivity::class.java)
                                context.startActivity(intent)
                            }
                            if(it.text=="POS"){
                                val intent= Intent(context, SalesActivity2::class.java)
                                context.startActivity(intent)
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
            TopAppBar(title = {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Admin's Dashboard")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = formattedDateTime, fontSize = 18.sp)
                }
            },
                navigationIcon= {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                },  colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.purple_200)
                ))
        }) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(it), horizontalAlignment = Alignment.CenterHorizontally) {
                Box (modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = colorResource(id = R.color.purple_200),
                        shape = RoundedCornerShape(bottomEnd = 40.dp, bottomStart = 40.dp)
                    ), contentAlignment = Alignment.Center){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Welcome, ${userName.value} ", fontSize = 20.sp)
                    }

                }
                Spacer(modifier = Modifier.size(20.dp))
                Row (modifier = Modifier
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_stacked_bar_chart_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Today's sales", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))

                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_warning_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Expired Products", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))
                        Badge(count = itemCounts.value["expired"] ?: 0)
                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_shopping_cart_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Products", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))
                        Badge(count = itemCounts.value["products"] ?: 0)

                    }

                }
                Row (modifier = Modifier
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_people_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Suppliers", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))
                        Badge(count = itemCounts.value["suppliers"] ?: 0)

                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_person_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Users", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))
                        Badge(count = itemCounts.value["users"] ?: 0)

                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_stacked_bar_chart_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Week's sales", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))


                    }

                }
                Row (modifier = Modifier
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_stacked_bar_chart_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Month's sales", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))

                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_stacked_bar_chart_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Year's sales", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))

                    }
                    Column(modifier = Modifier
                        .weight(0.25f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.baseline_warehouse_24), contentDescription =null,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .padding(16.dp))
                        Text(text = "Stores", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp))
                        Badge(count = itemCounts.value["stores"] ?: 0)

                    }

                }

            }
        }
    }

}
fun fetchCounts(db: FirebaseFirestore, itemCounts: MutableState<Map<String, Int>>) {
    db.collection("Products").whereEqualTo("isExpired", true).get()
        .addOnSuccessListener { documents ->
            val expiredCount = documents.size()
            itemCounts.value = itemCounts.value.toMutableMap().apply { put("expired", expiredCount) }
        }

    db.collection("Products").get()
        .addOnSuccessListener { documents ->
            val productsCount = documents.size()
            itemCounts.value = itemCounts.value.toMutableMap().apply { put("products", productsCount) }
        }

    db.collection("Suppliers").get()
        .addOnSuccessListener { documents ->
            val suppliersCount = documents.size()
            itemCounts.value = itemCounts.value.toMutableMap().apply { put("suppliers", suppliersCount) }
        }

    db.collection("Users").get()
        .addOnSuccessListener { documents ->
            val usersCount = documents.size()
            itemCounts.value = itemCounts.value.toMutableMap().apply { put("users", usersCount) }
        }

    db.collection("Stores").get()
        .addOnSuccessListener { documents ->
            val storesCount = documents.size()
            itemCounts.value = itemCounts.value.toMutableMap().apply { put("stores", storesCount) }
        }
}

@Composable
fun Badge(count: Int) {
    if (count > 0) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.purple_200)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = count.toString(),
                color = Color.White,
                fontSize = 12.sp)
        }
    }
}

data class DrawerItems(
    val icon: ImageVector,
    val text: String,
    val badgeCount: Int,
    val hasBadge: Boolean
)


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Admin()
}