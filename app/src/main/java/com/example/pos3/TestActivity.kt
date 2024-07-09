package com.example.pos3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pos3.ui.theme.Pos3Theme

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Greeting()

        }
    }
}

@Composable
fun Greeting() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TableCell(text = "Name")
            Spacer(modifier = Modifier.size(20.dp))
            TableCell(text = "Email")
            Spacer(modifier = Modifier.size(20.dp))
            TableCell(text = "phone")
            Spacer(modifier = Modifier.size(20.dp))
            TableCell(text = "Role")
        }

        Divider()

        // Table Rows
        for (i in 1..5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TableCell(text = "Row $i")
                Spacer(modifier = Modifier.size(20.dp))
                TableCell(text = "Row $i")
                Spacer(modifier = Modifier.size(20.dp))
                TableCell(text = "Row $i")
            }
            Divider()
        }
    }

}
@Composable
fun TableCell(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
   Greeting()
}