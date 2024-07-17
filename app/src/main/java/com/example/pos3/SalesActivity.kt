package com.example.pos3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

class SalesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalesScreen()
        }
    }
}
@Composable
fun SalesScreen() {
    val items = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig", "Grape")
    Sales(items = items)
}
@Composable
fun Sales(items: List<String>) {
    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            var query by remember { mutableStateOf("") }
            var filteredItems by remember { mutableStateOf(items) }
            var expanded by remember { mutableStateOf(false) }


            // Update the filtered list whenever the query changes
            LaunchedEffect(query) {
                filteredItems = if (query.isEmpty()) {
                    items
                } else {
                    items.filter { it.contains(query, ignoreCase = true) }
                }
            }

            Column {
                SearchBar(query, expanded) {
                    query = it
                    expanded = true
                }
                DropdownMenu(
                    expanded = expanded && filteredItems.isNotEmpty(),
                    onDismissRequest = { expanded = false }
                ) {
                    filteredItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                query = item
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
fun SearchBar(query: String,expanded: Boolean, onQueryChanged: (String) -> Unit) {
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
                onQueryChanged(query)
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    SalesScreen()
}