package com.example.project_dex

// This data class defines the structure for holding detailed information for a single move.
data class Move(
    val name: String,
    val type: String,
    val power: Int?,      // Power can be null (e.g., status moves), so it's nullable (Int?)
    val accuracy: Int?    // Accuracy can also be null, so it's nullable (Int?)
)
