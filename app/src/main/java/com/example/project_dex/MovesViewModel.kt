package com.example.project_dex

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException

class MovesViewModel : ViewModel() {
    // This state holds the final list of detailed moves to be displayed by the UI.
    val movesList = mutableStateOf<List<Move>>(emptyList())
    // This state holds the text the user types into the search bar.
    val searchText = mutableStateOf("")

    // We keep a private, complete list of all moves to use for filtering.
    private var allMoves = listOf<Move>()

    init {
        // When the ViewModel is first created, start fetching the list of moves.
        fetchMoveList()
    }

    private fun fetchMoveList() {
        val client = AsyncHttpClient()
        // This first API call gets the names and URLs of all moves.
        val url = "https://pokeapi.co/api/v2/move?limit=1000" // Fetches up to 1000 moves

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d("MovesViewModel", "Initial move list fetched!")
                try {
                    val movesArray = json.jsonObject.getJSONArray("results")
                    for (i in 0 until movesArray.length()) {
                        // For each move, we get its unique URL.
                        val moveUrl = movesArray.getJSONObject(i).getString("url")
                        // Now, we fetch the details for that specific move.
                        fetchMoveDetails(moveUrl)
                    }
                } catch (e: JSONException) {
                    Log.e("MovesViewModel", "Failed to parse initial move list JSON.", e)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.e("MovesViewModel", "Failed to fetch initial move list: $response", throwable)
            }
        })
    }

    private fun fetchMoveDetails(moveUrl: String) {
        val client = AsyncHttpClient()
        // This second API call gets the details for one specific move.
        client.get(moveUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    // Parse the JSON for the details we want.
                    val moveName = json.jsonObject.getString("name").replaceFirstChar { it.titlecase() }
                    val moveType = json.jsonObject.getJSONObject("type").getString("name").replaceFirstChar { it.titlecase() }
                    // Power and accuracy can be null, so we must check for that before getting the Int.
                    val movePower = if (json.jsonObject.isNull("power")) null else json.jsonObject.getInt("power")
                    val moveAccuracy = if (json.jsonObject.isNull("accuracy")) null else json.jsonObject.getInt("accuracy")

                    // Create our detailed Move object.
                    val detailedMove = Move(
                        name = moveName,
                        type = moveType,
                        power = movePower,
                        accuracy = moveAccuracy
                    )

                    // Add the new detailed move to our private list and sort it alphabetically.
                    allMoves = (allMoves + detailedMove).sortedBy { it.name }
                    // Update the public list that the UI is watching.
                    movesList.value = allMoves

                } catch (e: JSONException) {
                    Log.e("MovesViewModel", "Failed to parse move details for $moveUrl", e)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.e("MovesViewModel", "Failed to fetch move details: $response", throwable)
            }
        })
    }

    // This function is called every time the user types in the search bar.
    fun onSearchTextChanged(newText: String) {
        searchText.value = newText
        if (newText.isBlank()) {
            // If the search bar is empty, show the full list.
            movesList.value = allMoves
        } else {
            // Otherwise, filter the full list based on the search text.
            movesList.value = allMoves.filter {
                it.name.contains(newText, ignoreCase = true)
            }
        }
    }
}
