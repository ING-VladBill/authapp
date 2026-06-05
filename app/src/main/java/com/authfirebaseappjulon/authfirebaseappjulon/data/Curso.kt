package com.authfirebaseappjulon.authfirebaseappjulon.data

// Entidad que representa un curso guardado en Cloud Firestore.
data class Curso(
    val id: String = "",
    val nombre: String = "",
    val profesor: String = "",
    val creditos: String = "",
    val userId: String = ""
)
