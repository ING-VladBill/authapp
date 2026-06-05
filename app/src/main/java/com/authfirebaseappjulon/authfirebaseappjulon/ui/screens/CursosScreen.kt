package com.authfirebaseappjulon.authfirebaseappjulon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.authfirebaseappjulon.authfirebaseappjulon.data.Curso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursosScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val usuario = auth.currentUser
    val userId = usuario?.uid.orEmpty()
    val email = usuario?.email.orEmpty()

    var nombre by remember { mutableStateOf("") }
    var profesor by remember { mutableStateOf("") }
    var creditos by remember { mutableStateOf("") }
    var editandoId by remember { mutableStateOf<String?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cursos by remember { mutableStateOf<List<Curso>>(emptyList()) }

    LaunchedEffect(usuario) {
        if (usuario == null) {
            navController.navigate("login") {
                popUpTo("cursos") { inclusive = true }
            }
        }
    }

    DisposableEffect(userId) {
        if (userId.isBlank()) {
            onDispose { }
        } else {
            val listener = db.collection("cursos")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        mensaje = "Error al cargar cursos: ${e.message}"
                        return@addSnapshotListener
                    }

                    cursos = snapshot?.documents?.map { doc ->
                        Curso(
                            id = doc.id,
                            nombre = doc.getString("nombre").orEmpty(),
                            profesor = doc.getString("profesor").orEmpty(),
                            creditos = doc.getString("creditos").orEmpty(),
                            userId = doc.getString("userId").orEmpty()
                        )
                    }?.sortedBy { it.nombre.lowercase() }.orEmpty()
                }

            onDispose { listener.remove() }
        }
    }

    fun limpiarFormulario() {
        nombre = ""
        profesor = ""
        creditos = ""
        editandoId = null
    }

    fun abrirNuevoCurso() {
        limpiarFormulario()
        mensaje = null
        mostrarFormulario = true
    }

    fun cerrarFormulario() {
        limpiarFormulario()
        mostrarFormulario = false
    }

    fun guardarCurso() {
        mensaje = null

        if (userId.isBlank()) {
            mensaje = "Debes iniciar sesión para registrar cursos"
            return
        }

        if (nombre.isBlank()) {
            mensaje = "El nombre del curso es obligatorio"
            return
        }

        val datos = hashMapOf(
            "nombre" to nombre.trim(),
            "profesor" to profesor.trim(),
            "creditos" to creditos.trim(),
            "userId" to userId
        )

        if (editandoId == null) {
            db.collection("cursos")
                .add(datos)
                .addOnSuccessListener {
                    cerrarFormulario()
                    mensaje = "Curso agregado correctamente"
                }
                .addOnFailureListener {
                    mensaje = "Error al agregar curso: ${it.message}"
                }
        } else {
            db.collection("cursos")
                .document(editandoId!!)
                .set(datos)
                .addOnSuccessListener {
                    cerrarFormulario()
                    mensaje = "Curso actualizado correctamente"
                }
                .addOnFailureListener {
                    mensaje = "Error al actualizar curso: ${it.message}"
                }
        }
    }

    fun eliminarCurso(curso: Curso) {
        if (curso.userId != userId) {
            mensaje = "No puedes eliminar cursos de otro usuario"
            return
        }

        db.collection("cursos")
            .document(curso.id)
            .delete()
            .addOnSuccessListener { mensaje = "Curso eliminado" }
            .addOnFailureListener { mensaje = "Error al eliminar curso: ${it.message}" }
    }

    fun cargarCurso(curso: Curso) {
        nombre = curso.nombre
        profesor = curso.profesor
        creditos = curso.creditos
        editandoId = curso.id
        mensaje = null
        mostrarFormulario = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cursos") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { abrirNuevoCurso() }) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
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

            Text(
                text = "Mis cursos registrados (${cursos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (mensaje != null) {
                Text(
                    text = mensaje!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
            }

            if (cursos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no tienes cursos registrados. Pulsa + para agregar uno.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
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
                                if (curso.profesor.isNotBlank()) {
                                    Text("Profesor: ${curso.profesor}")
                                }
                                if (curso.creditos.isNotBlank()) {
                                    Text("Créditos: ${curso.creditos}")
                                }
                                Row {
                                    TextButton(onClick = { cargarCurso(curso) }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { eliminarCurso(curso) }) {
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

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { cerrarFormulario() },
            title = {
                Text(if (editandoId == null) "Agregar curso" else "Editar curso")
            },
            text = {
                Column {
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
                }
            },
            confirmButton = {
                Button(onClick = { guardarCurso() }) {
                    Text(if (editandoId == null) "Agregar" else "Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cerrarFormulario() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
