package studio.broapp.lustigewitze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LustigeWitzeApp()
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Feed("Feed", Icons.Filled.List),
    Random("Zufall", Icons.Filled.Shuffle),
    Leaderboard("Rangliste", Icons.Filled.EmojiEvents),
    Profile("Profil", Icons.Filled.Person)
}

private data class Joke(
    val id: String,
    val content: String,
    val category: String,
    val author: String,
    val score: Int,
    val favoriteCount: Int,
    val viewerVote: Int? = null,
    val viewerFavorite: Boolean = false
)

private data class Comment(val author: String, val content: String)

private data class ProfileSummary(
    val username: String,
    val headline: String,
    val jokeCount: Int,
    val totalScore: Int,
    val favoriteCategory: String,
    val averageScore: Int,
    val favoriteCount: Int
)

private const val LONG_JOKE_CATEGORY = "Lange Witze"
private const val AUTO_LONG_JOKE_THRESHOLD = 420
private const val STANDARD_JOKE_MAX_CHARS = 420
private const val LONG_JOKE_MAX_CHARS = 2500
private const val LONG_JOKE_LENGTH_ERROR = "Witze über 420 Zeichen werden automatisch als Lange Witze gespeichert."

private val demoJokes = listOf(
    Joke(
        id = "1",
        content = "Warum nehmen Entwickler nie die Treppe? Weil sie lieber den Stack benutzen.",
        category = "Tech",
        author = "pointenpaule",
        score = 128,
        favoriteCount = 24,
        viewerVote = 1,
        viewerFavorite = true
    ),
    Joke(
        id = "2",
        content = "Mein Kalender ist so voll, selbst meine Pausen brauchen jetzt Termine.",
        category = "Arbeit",
        author = "deadline_dieter",
        score = 87,
        favoriteCount = 11
    ),
    Joke(
        id = "3",
        content = "Ich wollte heute produktiv sein. Dann hat mein Sofa 'nur fuenf Minuten' gesagt.",
        category = "Alltag",
        author = "sofaprofi",
        score = 64,
        favoriteCount = 9
    )
)

private val demoComments = listOf(
    Comment("ehrenotto", "Bro der war stark."),
    Comment("lachflash", "Der Stack-Witz landet direkt in Favoriten.")
)

private val demoProfiles = mapOf(
    "pointenpaule" to ProfileSummary(
        username = "pointenpaule",
        headline = "Feed lesen, liefern und Favoriten sammeln.",
        jokeCount = 34,
        totalScore = 421,
        favoriteCategory = "Tech",
        averageScore = 12,
        favoriteCount = 24
    ),
    "deadline_dieter" to ProfileSummary(
        username = "deadline_dieter",
        headline = "Arbeitswitze mit stabilem Punch.",
        jokeCount = 22,
        totalScore = 317,
        favoriteCategory = "Arbeit",
        averageScore = 14,
        favoriteCount = 11
    ),
    "sofaprofi" to ProfileSummary(
        username = "sofaprofi",
        headline = "Alltag, Sofa und maximal relatable.",
        jokeCount = 18,
        totalScore = 284,
        favoriteCategory = "Alltag",
        averageScore = 15,
        favoriteCount = 9
    ),
    "ehrenotto" to ProfileSummary(
        username = "ehrenotto",
        headline = "Kommentiert alles, was knallt.",
        jokeCount = 6,
        totalScore = 59,
        favoriteCategory = "Alltag",
        averageScore = 10,
        favoriteCount = 3
    ),
    "lachflash" to ProfileSummary(
        username = "lachflash",
        headline = "Favoritenjäger mit Tech-Faible.",
        jokeCount = 4,
        totalScore = 41,
        favoriteCategory = "Tech",
        averageScore = 10,
        favoriteCount = 5
    )
)

private object Comic {
    val Ink = Color(0xFF1A1714)
    val Cream = Color(0xFFFFF5DB)
    val Paper = Color(0xFFFFFCF2)
    val Panel = Color(0xFFEAE0D6)
    val Yellow = Color(0xFFFFD43B)
    val Blue = Color(0xFF5CB2FF)
    val BlueSoft = Color(0xFFC2E6FF)
    val Pink = Color(0xFFFF73AD)
    val Orange = Color(0xFFFF7833)
    val Red = Color(0xFFD6263D)
    val Muted = Color(0xFF554F47)
    val DarkBg = Color(0xFF140F22)
    val DarkPanel = Color(0xFF382642)
    val DarkPaper = Color(0xFF291A31)
}

@Composable
private fun LustigeWitzeApp() {
    var darkMode by rememberSaveable { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkMode) {
            darkColorScheme(
                background = Comic.DarkBg,
                surface = Comic.DarkPaper,
                primary = Comic.Yellow,
                onPrimary = Comic.Ink,
                onSurface = Color(0xFFFBEFBA)
            )
        } else {
            lightColorScheme(
                background = Comic.Cream,
                surface = Comic.Paper,
                primary = Comic.Yellow,
                onPrimary = Comic.Ink,
                onSurface = Comic.Ink
            )
        }
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppShell(darkMode = darkMode, onToggleTheme = { darkMode = !darkMode })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(darkMode: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current
    val sessionStore = remember { SessionStore(context = context, apiClient = MobileApiClient()) }
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Feed) }
    var selectedJoke by remember { mutableStateOf<Joke?>(null) }
    var selectedProfileUsername by rememberSaveable { mutableStateOf<String?>(null) }
    var blockedAuthors by rememberSaveable { mutableStateOf(listOf<String>()) }
    var blockedUserMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var showComposer by rememberSaveable { mutableStateOf(false) }
    var accountDeleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sessionStore.accessToken) {
        if (!sessionStore.accessToken.isNullOrBlank()) {
            sessionStore.loadOwnProfile()
        }
    }

    LaunchedEffect(sessionStore.blockMessage) {
        val message = sessionStore.blockMessage ?: return@LaunchedEffect
        blockedUserMessage = message
        if (message.startsWith("blocked_user:")) {
            val blockedKey = message.removePrefix("blocked_user:")
            blockedAuthors = (blockedAuthors + blockedKey).distinct()
            selectedJoke = null
            selectedProfileUsername = null
            selectedTab = Tab.Feed
        }
    }

    val visibleJokes = demoJokes.filterNot { blockedAuthors.contains(it.author) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = if (darkMode) Comic.DarkPaper else Comic.Cream,
                tonalElevation = 0.dp
            ) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontWeight = FontWeight.Black) }
                    )
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = onToggleTheme,
                    containerColor = if (darkMode) Comic.Blue else Comic.Paper,
                    contentColor = Comic.Ink,
                    shape = CircleShape
                ) {
                    Icon(if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode, "Theme wechseln")
                }
                FloatingActionButton(
                    onClick = { showComposer = true },
                    containerColor = Comic.Yellow,
                    contentColor = Comic.Ink,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, "Neuen Witz erstellen")
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                Tab.Feed -> FeedScreen(
                    jokes = visibleJokes,
                    blockedUserMessage = blockedUserMessage,
                    onOpenJoke = { selectedJoke = it },
                    onOpenProfile = { selectedProfileUsername = it },
                    onAuthRequired = { showAuth = true }
                )
                Tab.Random -> RandomScreen(
                    jokes = visibleJokes,
                    blockedUserMessage = blockedUserMessage,
                    onOpenJoke = { selectedJoke = it },
                    onOpenProfile = { selectedProfileUsername = it },
                    onAuthRequired = { showAuth = true }
                )
                Tab.Leaderboard -> LeaderboardScreen(
                    blockedAuthors = blockedAuthors,
                    onOpenProfile = { selectedProfileUsername = it }
                )
                Tab.Profile -> ProfileScreen(
                    username = sessionStore.currentUser?.username,
                    isOwnProfile = sessionStore.currentUser != null,
                    accountDeleted = accountDeleted,
                    sessionStore = sessionStore,
                    onAuthRequired = { showAuth = true },
                    onDeleteAccount = {
                        scope.launch {
                            sessionStore.deleteAccount()
                            accountDeleted = sessionStore.accessToken == null
                        }
                    }
                )
            }
        }
    }

    selectedJoke?.let { joke ->
        ModalBottomSheet(onDismissRequest = { selectedJoke = null }) {
            DetailScreen(
                joke = joke,
                blockedAuthors = blockedAuthors,
                onBack = { selectedJoke = null },
                onOpenProfile = { selectedProfileUsername = it },
                onAuthRequired = { showAuth = true },
                onBlockAuthor = { author ->
                    scope.launch {
                        val blocked = sessionStore.blockAuthorAndReport(authorId = author, jokeId = joke.id)
                        if (blocked) {
                            blockedAuthors = (blockedAuthors + author).distinct()
                        }
                    }
                }
            )
        }
    }

    selectedProfileUsername?.let { username ->
        ModalBottomSheet(onDismissRequest = { selectedProfileUsername = null }) {
            ProfileScreen(
                username = username,
                isOwnProfile = username == sessionStore.currentUser?.username,
                accountDeleted = accountDeleted,
                sessionStore = sessionStore,
                onAuthRequired = { showAuth = true },
                onDeleteAccount = {
                    scope.launch {
                        sessionStore.deleteAccount()
                        accountDeleted = sessionStore.accessToken == null
                        selectedProfileUsername = null
                    }
                }
            )
        }
    }

    if (showAuth) {
        ModalBottomSheet(onDismissRequest = { showAuth = false }) {
            AuthSheet(sessionStore = sessionStore, onDone = { showAuth = false })
        }
    }

    if (showComposer) {
        ModalBottomSheet(onDismissRequest = { showComposer = false }) {
            ComposerSheet(onDone = { showComposer = false }, onAuthRequired = { showAuth = true })
        }
    }
}

@Composable
private fun FeedScreen(
    jokes: List<Joke>,
    blockedUserMessage: String?,
    onOpenJoke: (Joke) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit
) {
    var sort by rememberSaveable { mutableStateOf("Neu") }
    var category by rememberSaveable { mutableStateOf("Alle") }
    val categories = listOf("Alle", "Alltag", "Tech", "Arbeit", LONG_JOKE_CATEGORY, "Familie")

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(title = "LustigeWitze", subtitle = "Feed lesen, bewerten und merken.", badge = "Feed")
            blockedUserMessage?.let {
                Text(it, color = Comic.Red, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                Segment("Neu", selected = sort == "Neu") { sort = "Neu" }
                Segment("Top", selected = sort == "Top") { sort = "Top" }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                categories.take(4).forEach { item ->
                    Segment(item, selected = category == item) { category = item }
                }
            }
        }

        if (jokes.isEmpty()) {
            item {
                StatusPanel("Feed bereinigt", "Alle aktuell sichtbaren Witze stammen von blockierten Usern.")
            }
        } else {
            items(jokes) { joke ->
                FeedSwipeCard(
                    joke = joke,
                    onOpen = { onOpenJoke(joke) },
                    onOpenProfile = onOpenProfile,
                    onAuthRequired = onAuthRequired
                )
            }
        }

        item {
            StatusPanel(
                title = "Pagination bereit",
                message = "Naechster Schritt: API-Client an /api/mobile/feed mit cursor anbinden."
            )
        }
    }
}

@Composable
private fun RandomScreen(
    jokes: List<Joke>,
    blockedUserMessage: String?,
    onOpenJoke: (Joke) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit
) {
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var undoIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    if (jokes.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenHeader(title = "Zufallswitz", subtitle = "Swipe oder nutze die Buttons.", badge = "Random")
            blockedUserMessage?.let {
                Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
            }
            StatusPanel("Random bereinigt", "Aktuell sind keine nicht blockierten Witze mehr sichtbar.")
        }
        return
    }

    val joke = jokes[currentIndex % jokes.size]

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(title = "Zufallswitz", subtitle = "Swipe oder nutze die Buttons.", badge = "Random")
        blockedUserMessage?.let {
            Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(joke.id) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(dragX) > 120f) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                undoIndex = currentIndex
                                currentIndex += 1
                            }
                            dragX = 0f
                        },
                        onDrag = { _, amount -> dragX += amount.x }
                    )
                }
                .rotate(dragX / 40f)
        ) {
            JokeCard(joke = joke, onOpen = { onOpenJoke(joke) }, onOpenProfile = onOpenProfile, onAuthRequired = onAuthRequired)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ComicAction("Nope", Icons.Filled.ThumbDown, Comic.Red, Modifier.weight(1f)) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                undoIndex = currentIndex
                currentIndex += 1
            }
            ComicAction("Top", Icons.Filled.ThumbUp, Comic.Yellow, Modifier.weight(1f)) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAuthRequired()
            }
            ComicAction("Neu", Icons.Filled.Refresh, Comic.Blue, Modifier.weight(1f)) {
                undoIndex = currentIndex
                currentIndex += 1
            }
        }

        undoIndex?.let { previous ->
            TextButton(onClick = {
                currentIndex = previous
                undoIndex = null
            }) {
                Text("Undo: letzten Witz zurueckholen", fontWeight = FontWeight.Black)
            }
        }

        StatusPanel(
            title = "Native Random-Flow",
            message = "Swipe, Buttons, Undo und Haptik-Aequivalent sind als MVP-State vorhanden."
        )
    }
}

@Composable
private fun DetailScreen(
    joke: Joke,
    blockedAuthors: List<String>,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit,
    onBlockAuthor: (String) -> Unit
) {
    val visibleComments = demoComments.filterNot { blockedAuthors.contains(it.author) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Zurueck")
            }
            Text("Witz Detail", fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        JokeCard(joke = joke, onOpen = {}, onOpenProfile = onOpenProfile, onAuthRequired = onAuthRequired)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ComicAction("Teilen", Icons.Filled.Share, Comic.Blue, Modifier.weight(1f)) {}
            ComicAction("Report", Icons.Filled.Flag, Comic.Pink, Modifier.weight(1f)) { onAuthRequired() }
            ComicAction("User blockieren", Icons.Filled.Person, Comic.Yellow, Modifier.weight(1f)) { onBlockAuthor(joke.author) }
        }
        Text("Kommentare", fontWeight = FontWeight.Black, fontSize = 20.sp)
        visibleComments.forEach { comment ->
            ComicCard {
                TextButton(onClick = { onOpenProfile(comment.author) }) {
                    Text("@${comment.author}", fontWeight = FontWeight.Black)
                }
                Text(comment.content, modifier = Modifier.padding(top = 4.dp))
            }
        }
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Kommentar schreiben...") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        PrimaryButton("Einloggen zum Kommentieren", Icons.Filled.Login, onClick = onAuthRequired)
    }
}

@Composable
private fun LeaderboardScreen(blockedAuthors: List<String>, onOpenProfile: (String) -> Unit) {
    val rows = listOf("pointenpaule" to 421, "deadline_dieter" to 317, "sofaprofi" to 284)
        .filterNot { blockedAuthors.contains(it.first) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(title = "Rangliste", subtitle = "Top User und Top Witze im MVP.", badge = "Top")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                Segment("User", selected = true) {}
                Segment("Witze", selected = false) {}
                Segment("Woche", selected = false) {}
            }
        }
        items(rows) { (name, score) ->
            ComicCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = Comic.Orange)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        TextButton(onClick = { onOpenProfile(name) }) {
                            Text("@$name", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Text("$score Punkte", color = Comic.Muted)
                    }
                    ScoreBadge(score)
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    username: String?,
    isOwnProfile: Boolean,
    accountDeleted: Boolean,
    sessionStore: SessionStore,
    onAuthRequired: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val profile = username?.let { requestedUsername ->
        sessionStore.loadedProfile?.takeIf { it.user.username == requestedUsername }?.let { loaded ->
            ProfileSummary(
                username = loaded.user.username,
                headline = loaded.user.bio ?: "Profil wird live aus dem Mobile Namespace geladen.",
                jokeCount = loaded.stats.jokeCount,
                totalScore = loaded.stats.totalScore,
                favoriteCategory = loaded.stats.favoriteCategory,
                averageScore = loaded.stats.averageScore,
                favoriteCount = loaded.favorites.size
            )
        } ?: demoProfiles[requestedUsername] ?: ProfileSummary(
            username = requestedUsername,
            headline = "Profil wird aus dem Mobile Namespace gespiegelt.",
            jokeCount = 0,
            totalScore = 0,
            favoriteCategory = "Noch offen",
            averageScore = 0,
            favoriteCount = 0
        )
    }
    var showDeleteWarning by rememberSaveable(username) { mutableStateOf(false) }

    LaunchedEffect(username, sessionStore.accessToken) {
        if (username == null) {
            return@LaunchedEffect
        }

        if (username == sessionStore.currentUser?.username) {
            sessionStore.loadOwnProfile()
        } else {
            sessionStore.loadProfile(username)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (username == null) {
            ScreenHeader(title = "Profil", subtitle = "Ohne Login bleibt dein Feed offen, aber dein Account-Bereich startet erst nach dem Einloggen.", badge = "Gast")
            ComicCard {
                Text("Gastkonto aktiv", fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(
                    "Du kannst die App ohne Login nutzen. Für Profil, Favoriten-Sync und Kontoverwaltung brauchst du aber einen Account.",
                    color = Comic.Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            ComicCard {
                Text("Warum einloggen?", fontWeight = FontWeight.Black)
                Text("- Profil und Stats sehen\n- Favoriten accountgebunden speichern\n- Konto später direkt wieder löschen", color = Comic.Muted, modifier = Modifier.padding(top = 8.dp))
            }
            PrimaryButton("Login / Register", Icons.Filled.Login, onClick = onAuthRequired)
            return@Column
        }

        val resolvedProfile = profile ?: return@Column
        ScreenHeader(title = "Profil", subtitle = resolvedProfile.headline, badge = if (isOwnProfile) "Account" else "Creator")
        ComicCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, null, modifier = Modifier.size(54.dp), tint = Comic.Blue)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("@${resolvedProfile.username}", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("${resolvedProfile.favoriteCount} Favoriten · ${resolvedProfile.jokeCount} Jokes", color = Comic.Muted)
                }
            }
        }
        ComicCard {
            Text("Lieblingskategorie", fontWeight = FontWeight.Black)
            Text(resolvedProfile.favoriteCategory, color = Comic.Muted, modifier = Modifier.padding(top = 4.dp))
        }
        ComicCard {
            Text("Ø Score / Joke", fontWeight = FontWeight.Black)
            Text(resolvedProfile.averageScore.toString(), color = Comic.Muted, modifier = Modifier.padding(top = 4.dp))
        }
        ComicCard {
            Text("Gesamt-Score", fontWeight = FontWeight.Black)
            Text(resolvedProfile.totalScore.toString(), color = Comic.Muted, modifier = Modifier.padding(top = 4.dp))
        }
        if (sessionStore.isLoadingProfile) {
            Text("Profil wird geladen...", color = Comic.Muted, fontWeight = FontWeight.Black)
        }
        sessionStore.profileError?.let {
            Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
        }
        if (isOwnProfile) {
            if (accountDeleted) {
                StatusPanel("Konto gelöscht", "Dein Account wurde über die Mobile API gelöscht und lokal aus der Session entfernt.")
            } else {
                PrimaryButton("Konto löschen", Icons.Filled.Flag, onClick = { showDeleteWarning = !showDeleteWarning })
                if (showDeleteWarning) {
                    ComicCard {
                        Text("Konto wirklich löschen?", fontWeight = FontWeight.Black)
                        Text("Dieser Schritt löscht dein Konto über die Mobile API und entfernt deine Session auf diesem Gerät.", color = Comic.Muted, modifier = Modifier.padding(top = 4.dp))
                        PrimaryButton("Löschung bestätigen", Icons.Filled.Flag, onClick = onDeleteAccount)
                    }
                }
            }
        } else {
            PrimaryButton("Login / Register", Icons.Filled.Login, onClick = onAuthRequired)
        }
    }
}

@Composable
private fun AuthSheet(sessionStore: SessionStore, onDone: () -> Unit) {
    var mode by rememberSaveable { mutableStateOf("Login") }
    var identifier by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionStore.currentUser?.id) {
        if (sessionStore.currentUser != null) {
            onDone()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp).windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(title = mode, subtitle = "Native Mobile Auth mit Access- und Refresh-Token.", badge = "Auth")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Segment("Login", selected = mode == "Login") { mode = "Login" }
            Segment("Register", selected = mode == "Register") { mode = "Register" }
        }
        OutlinedTextField(value = identifier, onValueChange = { identifier = it }, label = { Text("Username oder E-Mail") }, modifier = Modifier.fillMaxWidth())
        if (mode == "Register") {
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-Mail") }, modifier = Modifier.fillMaxWidth())
        }
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Passwort") }, modifier = Modifier.fillMaxWidth())
        ComicCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                Column {
                    Text("Nutzungsbedingungen / EULA", fontWeight = FontWeight.Black)
                    Text("Ich stimme den Regeln für UGC, Moderation und Account-Nutzung zu.", color = Comic.Muted)
                }
            }
        }
        sessionStore.authError?.let {
            Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
        }
        Button(
            onClick = {
                scope.launch {
                    if (mode == "Login") {
                        sessionStore.login(identifier = identifier, password = password)
                    } else {
                        sessionStore.register(username = username, email = email, password = password)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = acceptedTerms && !sessionStore.isSubmittingAuth
        ) {
            Text(mode, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ComposerSheet(onDone: () -> Unit, onAuthRequired: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Arbeit") }
    var hasAutoSelectedLongJokes by rememberSaveable { mutableStateOf(false) }
    val trimmedText = text.trim()
    val currentLimit = if (selectedCategory == LONG_JOKE_CATEGORY) LONG_JOKE_MAX_CHARS else STANDARD_JOKE_MAX_CHARS
    val lengthErrorMessage = when {
        selectedCategory != LONG_JOKE_CATEGORY && trimmedText.length > STANDARD_JOKE_MAX_CHARS -> LONG_JOKE_LENGTH_ERROR
        selectedCategory == LONG_JOKE_CATEGORY && trimmedText.length > LONG_JOKE_MAX_CHARS -> "Lange Witze dürfen höchstens 2500 Zeichen haben."
        else -> null
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp).windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(title = "Neuer Witz", subtitle = "Witze ab 421 Zeichen wandern automatisch in Lange Witze.", badge = "Neu")
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it.take(LONG_JOKE_MAX_CHARS)
                if (text.trim().length > AUTO_LONG_JOKE_THRESHOLD && !hasAutoSelectedLongJokes && selectedCategory != LONG_JOKE_CATEGORY) {
                    selectedCategory = "Lange Witze"
                    hasAutoSelectedLongJokes = true
                } else if (text.trim().length <= AUTO_LONG_JOKE_THRESHOLD) {
                    hasAutoSelectedLongJokes = false
                }
            },
            label = { Text("Dein Witz") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        Text("${trimmedText.length} / $currentLimit Zeichen", color = Comic.Muted, fontWeight = FontWeight.Bold)
        if (trimmedText.length > AUTO_LONG_JOKE_THRESHOLD && selectedCategory == LONG_JOKE_CATEGORY) {
            Text("Ab 421 Zeichen wird dein Witz automatisch als Lange Witze einsortiert.", color = Comic.Ink, fontWeight = FontWeight.Bold)
        }
        lengthErrorMessage?.let {
            Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Arbeit", "Tech", "Alltag", LONG_JOKE_CATEGORY).forEach { category ->
                Segment(category, selected = selectedCategory == category) { selectedCategory = category }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Entwurf schliessen") }
            Button(onClick = onAuthRequired, modifier = Modifier.weight(1f), enabled = lengthErrorMessage == null) { Text("Login fuer Publish") }
        }
    }
}

@Composable
private fun FeedSwipeCard(joke: Joke, onOpen: () -> Unit, onOpenProfile: (String) -> Unit, onAuthRequired: () -> Unit) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var viewerVote by rememberSaveable(joke.id) { mutableStateOf(joke.viewerVote) }
    var score by rememberSaveable(joke.id) { mutableStateOf(joke.score) }
    var feedbackLabel by rememberSaveable(joke.id) { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(joke.id, viewerVote, score) {
                detectDragGestures(
                    onDragEnd = {
                        val voteValue = if (dragY < -120f) 5 else if (dragX > 120f) 1 else if (dragX < -120f) -1 else null
                        if (abs(dragX) > 120f || dragY < -120f) {
                            voteValue?.let { value ->
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                val previousVote = viewerVote
                                score += value - (previousVote ?: 0)
                                viewerVote = value
                                feedbackLabel = when (value) {
                                    1 -> "Like registriert"
                                    -1 -> "Dislike registriert"
                                    else -> "Superlike!"
                                }
                                onAuthRequired()
                            }
                        }
                        dragX = 0f
                        dragY = 0f
                    },
                    onDrag = { _, amount ->
                        dragX += amount.x
                        dragY += amount.y
                    }
                )
            }
            .rotate(dragX / 70f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            JokeCard(
                joke = joke.copy(score = score, viewerVote = viewerVote),
                onOpen = onOpen,
                onOpenProfile = onOpenProfile,
                onAuthRequired = onAuthRequired
            )
            feedbackLabel?.let { label ->
                Pill(label, if (label == "Dislike registriert") Comic.BlueSoft else Comic.Yellow)
            }
        }

        if (dragX > 24f) {
            SwipeVoteBadge(
                label = "TOP",
                icon = Icons.Filled.ThumbUp,
                color = Comic.Yellow,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 18.dp, start = 18.dp)
                    .rotate(-12f)
            )
        }

        if (dragX < -24f) {
            SwipeVoteBadge(
                label = "RUNTER",
                icon = Icons.Filled.ThumbDown,
                color = Comic.Red,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
                    .rotate(12f)
            )
        }
    }
}

@Composable
private fun SwipeVoteBadge(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(2.dp, Comic.Ink),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Comic.Ink, modifier = Modifier.size(20.dp))
            Text(label, color = Comic.Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
private fun JokeCard(joke: Joke, onOpen: () -> Unit, onOpenProfile: (String) -> Unit, onAuthRequired: () -> Unit) {
    ComicCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(joke.category, Comic.Yellow)
            Spacer(Modifier.weight(1f))
            ScoreBadge(joke.score)
        }
        Text(
            joke.content,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 28.sp,
            modifier = Modifier.padding(top = 14.dp)
        )
        TextButton(onClick = { onOpenProfile(joke.author) }) {
            Text(
                "von @${joke.author}",
                color = Comic.Muted,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
            ReactionTile("Top", Icons.Filled.ThumbUp, joke.viewerVote == 1, Modifier.weight(1f), onAuthRequired)
            ReactionTile("Runter", Icons.Filled.ThumbDown, joke.viewerVote == -1, Modifier.weight(1f), onAuthRequired)
            ReactionTile("Super", Icons.Filled.Star, joke.viewerVote == 5, Modifier.weight(1f), onAuthRequired)
            ReactionTile(if (joke.viewerFavorite) "Gemerkt" else "${joke.favoriteCount}", Icons.Filled.Bookmark, joke.viewerFavorite, Modifier.weight(1f), onAuthRequired)
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, badge: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Pill(badge, Comic.Pink)
        Text(title, fontSize = 34.sp, fontWeight = FontWeight.Black, lineHeight = 38.sp)
        Text(subtitle, color = Comic.Muted, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ComicCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Comic.Panel, RoundedCornerShape(8.dp))
            .padding(2.dp)
            .background(Comic.Paper, RoundedCornerShape(7.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun Pill(title: String, fill: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = fill, border = BorderStroke(2.dp, Comic.Ink)) {
        Text(title, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}

@Composable
private fun Segment(title: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(title, fontWeight = FontWeight.Black) },
        border = BorderStroke(2.dp, Comic.Ink),
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Comic.Yellow else Comic.Paper,
            labelColor = Comic.Ink
        )
    )
}

@Composable
private fun ScoreBadge(score: Int) {
    Surface(shape = CircleShape, color = Comic.Blue, border = BorderStroke(2.dp, Comic.Ink)) {
        Text(score.toString(), fontWeight = FontWeight.Black, modifier = Modifier.padding(10.dp))
    }
}

@Composable
private fun ReactionTile(title: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(if (active) Comic.Yellow else Comic.BlueSoft, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(icon, null, tint = if (active) Comic.Red else Comic.Ink)
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun ComicAction(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Comic.Ink),
        border = BorderStroke(2.dp, Comic.Ink)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PrimaryButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    ComicAction(title = title, icon = icon, color = Comic.Yellow, modifier = Modifier.fillMaxWidth(), onClick = onClick)
}

@Composable
private fun StatusPanel(title: String, message: String) {
    ComicCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ChatBubble, null, tint = Comic.Orange)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Black)
                Text(message, color = Comic.Muted)
            }
        }
    }
}
