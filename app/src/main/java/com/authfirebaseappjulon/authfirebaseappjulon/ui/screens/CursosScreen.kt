package com.authfirebaseappjulon.authfirebaseappjulon.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.authfirebaseappjulon.authfirebaseappjulon.data.Curso
import com.authfirebaseappjulon.authfirebaseappjulon.ui.components.ModernBackground
import com.authfirebaseappjulon.authfirebaseappjulon.ui.components.StatusMessage
import com.authfirebaseappjulon.authfirebaseappjulon.ui.theme.AquaAccent
import com.authfirebaseappjulon.authfirebaseappjulon.ui.theme.CoralAccent
import com.authfirebaseappjulon.authfirebaseappjulon.ui.theme.IndigoPrimary
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
    val fabScale by animateFloatAsState(
        targetValue = if (mostrarFormulario) 0.9f else 1f,
        animationSpec = tween(220),
        label = "fabScale"
    )

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
                .addOnFailureListener { mensaje = "Error al agregar curso: ${it.message}" }
        } else {
            db.collection("cursos")
                .document(editandoId!!)
                .set(datos)
                .addOnSuccessListener {
                    cerrarFormulario()
                    mensaje = "Curso actualizado correctamente"
                }
                .addOnFailureListener { mensaje = "Error al actualizar curso: ${it.message}" }
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

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Mis Cursos", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Atrás", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { abrirNuevoCurso() },
                    modifier = Modifier.scale(fabScale),
                    shape = CircleShape,
                    containerColor = CoralAccent,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Tus cursos guardados",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(IndigoPrimary, AquaAccent)),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total registrados", color = Color.White.copy(alpha = 0.85f))
                                Text(
                                    text = cursos.size.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text("Pulsa + para crear", color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                StatusMessage(text = mensaje, isError = mensaje?.startsWith("Error") == true)
                Spacer(Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = cursos.isEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f))
                        ) {
                            Text(
                                text = "Aún no tienes cursos. Toca el botón + para registrar el primero.",
                                modifier = Modifier.padding(22.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                if (cursos.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cursos, key = { it.id }) { curso ->
                            CursoCard(curso = curso, onEditar = { cargarCurso(curso) }, onEliminar = { eliminarCurso(curso) })
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { cerrarFormulario() },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = { Text(if (editandoId == null) "Nuevo curso" else "Editar curso", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del curso *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = profesor,
                        onValueChange = { profesor = it },
                        label = { Text("Profesor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = creditos,
                        onValueChange = { creditos = it },
                        label = { Text("Créditos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { guardarCurso() }, shape = RoundedCornerShape(16.dp)) {
                    Text(if (editandoId == null) "Agregar" else "Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cerrarFormulario() }, shape = RoundedCornerShape(16.dp)) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CursoCard(curso: Curso, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Brush.linearGradient(listOf(IndigoPrimary, AquaAccent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = curso.nombre.take(1).uppercase().ifBlank { "C" },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(curso.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (curso.profesor.isNotBlank()) Text("Profesor: ${curso.profesor}", color = MaterialTheme.colorScheme.outline)
                if (curso.creditos.isNotBlank()) Text("Créditos: ${curso.creditos}", color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onEliminar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
