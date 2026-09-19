package com.example.ui.forum

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ForumPostEntity
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    repository: AnatomyRepository
) {
    val scope = rememberCoroutineScope()
    val posts by repository.allForumPosts.collectAsStateWithLifecycle(initialValue = emptyList())

    val categories = listOf("Todos", "Casos Clínicos", "Mnemotecnias", "Debate Quirúrgico", "Dudas de Cátedra")
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showNewPostDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, selectedCategory) {
        if (selectedCategory == "Todos") posts
        else posts.filter { it.category == selectedCategory }
    }

    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Comunidad Médica & Foros",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Debate clínico entre estudiantes y residentes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncing = true
                                repository.syncAllWithFirebase()
                                isSyncing = false
                            }
                        }
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MedicalTealLight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sincronizar foros con Firebase",
                                tint = MedicalTealLight
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavySurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPostDialog = true },
                containerColor = MedicalTealPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AddComment, contentDescription = "Crear tema de discusión")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground)
                .padding(innerPadding)
        ) {
            // Category filter row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalTealPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkNavySurfaceVariant,
                            labelColor = Color(0xFFECEFF4)
                        )
                    )
                }
            }

            // Posts list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    ForumPostCard(
                        post = post,
                        onUpvote = { scope.launch { repository.upvotePost(post.id) } },
                        onAddComment = { commentText ->
                            scope.launch { repository.addCommentToPost(post.id, commentText) }
                        }
                    )
                }
            }
        }
    }

    // New Discussion Post Dialog
    if (showNewPostDialog) {
        var postTitle by remember { mutableStateOf("") }
        var postCategory by remember { mutableStateOf("Casos Clínicos") }
        var postContent by remember { mutableStateOf("") }
        var postTags by remember { mutableStateOf("Anatomia,Clinica") }

        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            containerColor = DarkNavySurface,
            title = {
                Text(
                    text = "Publicar Caso o Duda Médica",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Título de la discusión") },
                        placeholder = { Text("Ej. Duda con las ramas de la carótida externa") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category picker
                    Text("Categoría:", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories.filter { it != "Todos" }) { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (postCategory == cat) MedicalTealPrimary else DarkNavySurfaceVariant,
                                modifier = Modifier.clickable { postCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("Descripción o planteamiento clínico") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 5
                    )

                    OutlinedTextField(
                        value = postTags,
                        onValueChange = { postTags = it },
                        label = { Text("Etiquetas (separadas por comas)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postContent.isNotBlank()) {
                            val tagsList = postTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            scope.launch {
                                repository.createForumPost(
                                    title = postTitle,
                                    category = postCategory,
                                    content = postContent,
                                    tags = tagsList
                                )
                            }
                            showNewPostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    enabled = postTitle.isNotBlank() && postContent.isNotBlank()
                ) {
                    Text("Publicar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPostDialog = false }) {
                    Text("Cancelar", color = Color(0xFFB0BEC5))
                }
            }
        )
    }
}

@Composable
fun ForumPostCard(
    post: ForumPostEntity,
    onUpvote: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var isCommentsExpanded by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }

    val commentsList = remember(post.commentsJson) {
        val list = mutableListOf<Triple<String, String, String>>() // Author, text, time
        try {
            val arr = JSONArray(post.commentsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Triple(
                        obj.optString("authorName", "Estudiante"),
                        obj.optString("text", ""),
                        obj.optString("timestamp", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author, Role badge, Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (post.isVerifiedDoctor) Color(0x3300B4D8) else Color(0x333F51B5),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = if (post.isVerifiedDoctor) MedicalTealLight else Color(0xFF8C9EFF)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (post.isVerifiedDoctor) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Médico verificado",
                                    tint = MedicalTealLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "${post.authorRole} • ${post.university}",
                            fontSize = 11.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }

                Surface(
                    color = Color(0x22007A8C),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = post.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedicalTealLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Content
            Text(
                text = post.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                fontSize = 13.sp,
                color = Color(0xFFCFD8DC),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags
            if (post.tagsCsv.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.tagsCsv.split(",").forEach { tag ->
                        Surface(
                            color = DarkNavySurfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#${tag.trim()}",
                                fontSize = 10.sp,
                                color = Color(0xFF80DEEA),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Footer actions: Upvote & Expand Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkNavySurfaceVariant,
                        modifier = Modifier.clickable { onUpvote() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Votar a favor",
                                tint = NeuralGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.upvotes}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeuralGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = post.timestampFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFF78909C)
                    )
                }

                TextButton(onClick = { isCommentsExpanded = !isCommentsExpanded }) {
                    Icon(
                        imageVector = Icons.Outlined.Forum,
                        contentDescription = null,
                        tint = MedicalTealLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${commentsList.size} Respuestas",
                        fontSize = 12.sp,
                        color = MedicalTealLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expandable Comments section
            AnimatedVisibility(visible = isCommentsExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color(0x22FFFFFF))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Existing comments
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        commentsList.forEach { (author, text, time) ->
                            Surface(
                                color = Color(0xFF131D30),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(author, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedicalTealLight)
                                        Text(time, fontSize = 10.sp, color = Color(0xFF78909C))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text, fontSize = 12.sp, color = Color(0xFFECEFF4))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Comment input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Aportar respuesta o aclaración clínica...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            enabled = newCommentText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MedicalTealLight)
                        }
                    }
                }
            }
        }
    }
}
