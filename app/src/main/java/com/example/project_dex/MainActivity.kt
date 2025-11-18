package com.example.project_dex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_dex.ui.theme.Project_dexTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_dexTheme {
                // --- START: SINGLE, CORRECTED SCAFFOLD LOGIC ---

                // State to decide which screen to show
                var currentScreen by remember { mutableStateOf("menu") }

                // Back navigation handler
                val navigateBackToMenu = { currentScreen = "menu" }

                // Use only ONE Scaffold as the root for the screen
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Conditionally show the TopAppBar only when NOT on the menu
                        if (currentScreen != "menu") {
                            TopAppBar(
                                title = { Text(currentScreen.replaceFirstChar { it.titlecase() } + " List") },
                                navigationIcon = {
                                    IconButton(onClick = navigateBackToMenu) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back to Menu"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // Apply the padding from the single Scaffold to your content
                    val screenModifier = Modifier.padding(innerPadding)

                    when (currentScreen) {
                        "menu" -> MainMenuScreen(
                            modifier = screenModifier,
                            onNavigate = { screen -> currentScreen = screen }
                        )
                        "pokemon" -> ListingScreen(
                            resourceType = "pokemon",
                            searchHint = "Search for a Pokémon...",
                            modifier = screenModifier
                        )
                        "move" -> MovesListScreen()
                        "ability" -> ListingScreen( // <-- The duplicate is now removed
                            resourceType = "ability",
                            searchHint = "Search for an Ability...",
                            modifier = screenModifier
                        )
                        "type" -> ListingScreen(
                            resourceType = "type",
                            searchHint = "Search for a Pokémon Type...",
                            modifier = screenModifier
                        )
                        "location" -> ListingScreen(
                            resourceType = "location",
                            searchHint = "Search for a Location...",
                            modifier = screenModifier
                        )
                    }
                }
                // --- END: SINGLE, CORRECTED SCAFFOLD LOGIC ---
            }
        }
    }
}

@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pokedex Main Menu", fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 32.dp))

        MenuButton(text = "Pokedex Listing", onClick = { onNavigate("pokemon") })
        MenuButton(text = "Moves Listing", onClick = { onNavigate("move") })
        MenuButton(text = "Abilities", onClick = { onNavigate("ability") })
        MenuButton(text = "Pokemon Types", onClick = { onNavigate("type") })
        MenuButton(text = "Pokemon Locations", onClick = { onNavigate("location") })
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = text, fontSize = 16.sp)
    }
}
