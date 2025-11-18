package com.example.project_dex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// This is the main Composable for the entire moves screen.
@Composable
fun MovesListScreen(
    movesViewModel: MovesViewModel = viewModel() // This creates and connects the ViewModel.
) {
    // Get the current search text and the filtered list of moves from the ViewModel.
    val searchText by movesViewModel.searchText
    val filteredMoves by movesViewModel.movesList

    Column(modifier = Modifier.fillMaxSize()) {
        // The Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { movesViewModel.onSearchTextChanged(it) },
            label = { Text("Search for a Move...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // The scrollable list of move items.
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredMoves) { move ->
                // Use a dedicated Composable to display each move.
                MoveItem(move = move)
                HorizontalDivider() // Adds a separator line.
            }
        }
    }
}

// This is a smaller, reusable Composable for displaying a single move's details.
@Composable
fun MoveItem(move: Move, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column for Name and Type on the left.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = move.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${move.type}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Column for Power and Accuracy on the right.
        Column(horizontalAlignment = Alignment.End) {
            // Use the 'elvis operator' (?:) to show "--" if power or accuracy is null.
            Text(
                text = "Pwr: ${move.power ?: "--"}",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Acc: ${move.accuracy ?: "--"}",
                fontSize = 14.sp
            )
        }
    }
}
