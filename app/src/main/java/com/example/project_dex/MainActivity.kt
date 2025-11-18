package com.example.project_dex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.project_dex.ui.theme.Project_dexTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Headers

// Data class for the list items from PokeAPI
@Serializable
data class ApiResource(
    val name: String,
    val url: String
)

@Serializable
data class ApiResponse(
    val results: List<ApiResource>
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_dexTheme {
                var currentScreen by remember { mutableStateOf("menu") }
                var selectedPokemonUrl by remember { mutableStateOf<String?>(null) }

                // Navigation logic
                val navigateBack = {
                    if (selectedPokemonUrl != null) {
                        selectedPokemonUrl = null
                    } else if (currentScreen != "menu") {
                        currentScreen = "menu"
                    }
                }

                val topBarTitle = when {
                    selectedPokemonUrl != null -> "Pokemon Details"
                    currentScreen != "menu" -> currentScreen.replaceFirstChar { it.titlecase() } + " List"
                    else -> "PokéDex"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentScreen != "menu" || selectedPokemonUrl != null) {
                            TopAppBar(
                                title = { Text(topBarTitle) },
                                navigationIcon = {
                                    IconButton(onClick = navigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    if (selectedPokemonUrl != null) {
                        PokemonDetailScreen(
                            pokemonUrl = selectedPokemonUrl!!,
                            modifier = screenModifier
                        )
                    } else {
                        when (currentScreen) {
                            "menu" -> MainMenuScreen(
                                modifier = screenModifier,
                                onNavigate = { screen -> currentScreen = screen }
                            )
                            "pokemon" -> ListingScreen(
                                resourceType = "pokemon",
                                searchHint = "Search by name or Pokédex ID...",
                                modifier = screenModifier,
                                onPokemonSelected = { url -> selectedPokemonUrl = url }
                            )
                            // Add cases for the other menu options
                            "type", "ability", "item", "location", "move" -> ListingScreen(
                                resourceType = currentScreen,
                                searchHint = "Search for a(n) $currentScreen...",
                                modifier = screenModifier,
                                // For these lists, we don't have a detail view yet
                                onPokemonSelected = { /* Do nothing for now */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Main Menu Composable with all options restored
@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        MenuCard(title = "Pokémon", onClick = { onNavigate("pokemon") })
        MenuCard(title = "Types", onClick = { onNavigate("type") })
        MenuCard(title = "Abilities", onClick = { onNavigate("ability") })
        MenuCard(title = "Items", onClick = { onNavigate("item") })
        MenuCard(title = "Locations", onClick = { onNavigate("location") })
        MenuCard(title = "Moves", onClick = { onNavigate("move") })
    }
}

@Composable
fun MenuCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Reusable Listing Screen for any resource type
@Composable
fun ListingScreen(
    resourceType: String,
    searchHint: String,
    modifier: Modifier = Modifier,
    onPokemonSelected: (String) -> Unit
) {
    var allItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val json = Json { ignoreUnknownKeys = true }

    // Fetch data from the API
    LaunchedEffect(resourceType) {
        val client = AsyncHttpClient()
        // Using a high limit to get most/all entries for a given type
        val url = "https://pokeapi.co/api/v2/$resourceType?limit=2000"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val response = json.decodeFromString<ApiResponse>(jsonResponse.jsonObject.toString())
                    allItems = response.results
                    filteredItems = response.results
                } catch (e: Exception) {
                    // Handle parsing error if needed
                }
            }
            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                // Handle fetch error if needed
            }
        })
    }

    // Filter logic that works for all resource types
    LaunchedEffect(searchQuery, allItems) {
        filteredItems = if (searchQuery.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                val nameMatch = item.name.contains(searchQuery, ignoreCase = true)
                // Only search by ID if the resource is pokemon
                if (resourceType == "pokemon") {
                    val id = item.url.split("/").dropLast(1).last()
                    val idMatch = id.contains(searchQuery)
                    nameMatch || idMatch
                } else {
                    nameMatch
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(searchHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredItems) { item ->
                val id = item.url.split("/").dropLast(1).last()
                ListItem(
                    headlineContent = { Text(item.name.replaceFirstChar { it.titlecase() }) },
                    // Show the ID only for Pokémon
                    supportingContent = if (resourceType == "pokemon") {
                        { Text("ID: $id") }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable {
                        // Only trigger navigation for Pokémon, as requested
                        if (resourceType == "pokemon") {
                            onPokemonSelected(item.url)
                        }
                    }
                )
            }
        }
    }
}
