package com.authfirebaseappjulon.authfirebaseappjulon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Modelo ───────────────────────────────────────────────────────────────────
data class Curso(
    val id: String = "",
    val nombre: String = "",
    val profesor: String = "",
    val creditos: String = "",
    val userId: String = ""
)

// ── Pantalla ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val auth   = FirebaseAuth.getInstance()
    val db     = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid   ?: ""
    val email  = auth.currentUser?.email ?: ""

    var nombre     by remember { mutableStateOf("") }
    var profesor   by remember { mutableStateOf("") }
    var creditos   by remember { mutableStateOf("") }
    var editandoId by remember { mutableStateOf<String?>(null) }
    var mensaje    by remember { mutableStateOf<String?>(null) }
    var cursos     by remember { mutableStateOf<List<Curso>>(emptyList()) }

    // ── LEER en tiempo real, filtrado por usuario autenticado ─────────────────
    DisposableEffect(userId) {
        val listener = db.collection("cursos")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    mensaje = "Error al cargar: ${e.message}"
                    return@addSnapshotListener
                }
                cursos = snapshot?.documents?.map { doc ->
                    Curso(
                        id       = doc.id,
                        nombre   = doc.getString("nombre")   ?: "",
                        profesor = doc.getString("profesor") ?: "",
                        creditos = doc.getString("creditos") ?: "",
                        userId   = doc.getString("userId")   ?: ""
                    )
                }?.sortedBy { it.nombre } ?: emptyList()
            }
        onDispose { listener.remove() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    fun limpiar() {
        nombre = ""; profesor = ""; creditos = ""; editandoId = null
    }

    fun guardar() {
        if (nombre.isBlank()) { mensaje = "El nombre del curso es obligatorio"; return }
        val datos = hashMapOf(
            "nombre"   to nombre,
            "profesor" to profesor,
            "creditos" to creditos,
            "userId"   to userId
        )
        if (editandoId == null) {
            // ── CREAR ─────────────────────────────────────────────────────────
            db.collection("cursos").add(datos)
                .addOnSuccessListener { limpiar(); mensaje = "Curso agregado correctamente" }
                .addOnFailureListener { mensaje = "Error al agregar: ${it.message}" }
        } else {
            // ── ACTUALIZAR ────────────────────────────────────────────────────
            db.collection("cursos").document(editandoId!!).set(datos)
                .addOnSuccessListener { limpiar(); mensaje = "Curso actualizado correctamente" }
                .addOnFailureListener { mensaje = "Error al actualizar: ${it.message}" }
        }
    }

    fun eliminar(curso: Curso) {
        // ── ELIMINAR ──────────────────────────────────────────────────────────
        db.collection("cursos").document(curso.id).delete()
            .addOnSuccessListener { mensaje = "Curso eliminado" }
            .addOnFailureListener { mensaje = "Error al eliminar: ${it.message}" }
    }

    fun cargarParaEditar(curso: Curso) {
        nombre     = curso.nombre
        profesor   = curso.profesor
        creditos   = curso.creditos
        editandoId = curso.id
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cursos") },
                actions = {
                    TextButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) { Text("Cerrar sesión") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Usuario: $email",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))

            // ── Formulario ─────────────────────────────────────────────────────
            Text(
                text = if (editandoId == null) "Agregar curso" else "Editar curso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del curso *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = profesor,
                onValueChange = { profesor = it },
                label = { Text("Profesor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = creditos,
                onValueChange = { creditos = it },
                label = { Text("Créditos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { guardar() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editandoId == null) "Agregar" else "Guardar cambios")
                }
                if (editandoId != null) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { limpiar() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            if (mensaje != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = mensaje!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── Lista de cursos ────────────────────────────────────────────────
            Text(
                text = "Mis cursos registrados (${cursos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (cursos.isEmpty()) {
                Text(
                    text = "Aún no tienes cursos. ¡Agrega uno arriba!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(cursos, key = { it.id }) { curso ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = curso.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (curso.profesor.isNotBlank())
                                    Text("Profesor: ${curso.profesor}")
                                if (curso.creditos.isNotBlank())
                                    Text("Créditos: ${curso.creditos}")
                                Row {
                                    TextButton(onClick = { cargarParaEditar(curso) }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { eliminar(curso) }) {
                                        Text(
                                            "Eliminar",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
