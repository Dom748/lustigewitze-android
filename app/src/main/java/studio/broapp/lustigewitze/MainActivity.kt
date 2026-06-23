package studio.broapp.lustigewitze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.graphics.Brush
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
    val authorId: String,
    val authorUsername: String,
    val score: Int,
    val favoriteCount: Int,
    val viewerVote: Int? = null,
    val viewerFavorite: Boolean = false
)

private data class FeedCategoryOption(val label: String, val apiValue: String)

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
private const val JOKE_CARD_PREVIEW_LIMIT = 400

private val demoJokes = listOf(
    Joke(
        id = "1",
        content = "Warum nehmen Entwickler nie die Treppe? Weil sie lieber den Stack benutzen.",
        category = "Tech",
        authorId = "pointenpaule",
        authorUsername = "pointenpaule",
        score = 128,
        favoriteCount = 24,
        viewerVote = 1,
        viewerFavorite = true
    ),
    Joke(
        id = "2",
        content = "Mein Kalender ist so voll, selbst meine Pausen brauchen jetzt Termine.",
        category = "Arbeit",
        authorId = "deadline_dieter",
        authorUsername = "deadline_dieter",
        score = 87,
        favoriteCount = 11
    ),
    Joke(
        id = "3",
        content = "Ich wollte heute produktiv sein. Dann hat mein Sofa 'nur fuenf Minuten' gesagt.",
        category = "Alltag",
        authorId = "sofaprofi",
        authorUsername = "sofaprofi",
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

private val feedCategoryOptions = listOf(
    FeedCategoryOption("Alle", "all"),
    FeedCategoryOption("Alltag", "alltag"),
    FeedCategoryOption("Tech", "tech"),
    FeedCategoryOption("Arbeit", "arbeit"),
    FeedCategoryOption(LONG_JOKE_CATEGORY, "lange-witze"),
    FeedCategoryOption("Familie", "familie")
)

private fun MobileJoke.toAppJoke(): Joke {
    return Joke(
        id = id,
        content = content,
        category = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        authorId = author.id,
        authorUsername = author.username,
        score = score,
        favoriteCount = favoriteCount,
        viewerVote = viewerVote,
        viewerFavorite = viewerFavorite
    )
}

private object Comic {
    val Ink = Color(0xFF1A1714)
    val Cream = Color(0xFFFFF5DB)
    val Paper = Color(0xFFFFFCF2)
    val Panel = Color(0xFFEAE0D6)
    val Yellow = Color(0xFFFFD43B)
    val YellowSoft = Color(0xFFFFE89A)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (darkMode) {
                            listOf(Comic.DarkBg, Comic.DarkPaper, Comic.DarkPanel)
                        } else {
                            listOf(Comic.Cream, Comic.YellowSoft.copy(alpha = 0.45f), Comic.Paper)
                        }
                    )
                )
        ) {
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
    var feedSort by rememberSaveable { mutableStateOf("latest") }
    var feedCategory by rememberSaveable { mutableStateOf("all") }

    LaunchedEffect(sessionStore.accessToken) {
        if (!sessionStore.accessToken.isNullOrBlank()) {
            sessionStore.loadOwnProfile()
        }
    }

    LaunchedEffect(feedSort, feedCategory, sessionStore.accessToken) {
        sessionStore.loadFeed(sort = feedSort, category = feedCategory)
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

    val visibleJokes = (if (sessionStore.hasLoadedFeed) {
        sessionStore.feedItems.map { it.toAppJoke() }
    } else {
        demoJokes
    }).filterNot { blockedAuthors.contains(it.authorId) || blockedAuthors.contains(it.authorUsername) }

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
        Box(
            Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .fillMaxSize()
        ) {
            when (selectedTab) {
                Tab.Feed -> FeedScreen(
                    jokes = visibleJokes,
                    blockedUserMessage = blockedUserMessage,
                    selectedSort = feedSort,
                    selectedCategory = feedCategory,
                    isLoadingFeed = sessionStore.isLoadingFeed,
                    isLoadingMoreFeed = sessionStore.isLoadingMoreFeed,
                    feedError = sessionStore.feedError,
                    canLoadMoreFeed = sessionStore.canLoadMoreFeed,
                    onSelectSort = { feedSort = it },
                    onSelectCategory = { feedCategory = it },
                    onRefresh = { scope.launch { sessionStore.loadFeed(sort = feedSort, category = feedCategory) } },
                    onLoadMore = { scope.launch { sessionStore.loadMoreFeed(sort = feedSort, category = feedCategory) } },
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
                onBlockAuthor = { authorId, authorUsername ->
                    scope.launch {
                        val blocked = sessionStore.blockAuthorAndReport(authorId = authorId, jokeId = joke.id)
                        if (blocked) {
                            blockedAuthors = (blockedAuthors + listOf(authorId, authorUsername)).distinct()
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
    selectedSort: String,
    selectedCategory: String,
    isLoadingFeed: Boolean,
    isLoadingMoreFeed: Boolean,
    feedError: String?,
    canLoadMoreFeed: Boolean,
    onSelectSort: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenJoke: (Joke) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit
) {
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
            feedError?.let {
                Text(it, color = Comic.Red, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            }
            ComicCard(modifier = Modifier.padding(top = 14.dp)) {
                Text("Filter", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Kategorien laufen jetzt horizontal, damit der Feed frei scrollt und das Stitch-Layout sauber bleibt.", color = Comic.Muted, modifier = Modifier.padding(top = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                    Segment("Neu", selected = selectedSort == "latest") { onSelectSort("latest") }
                    Segment("Top", selected = selectedSort == "top") { onSelectSort("top") }
                    Segment("Reload", selected = false, onClick = onRefresh)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    feedCategoryOptions.forEach { option ->
                        Segment(option.label, selected = selectedCategory == option.apiValue) { onSelectCategory(option.apiValue) }
                    }
                }
            }
            if (isLoadingFeed) {
                StatusPanel("Feed lädt", "Android zieht jetzt echte Witze von /api/mobile/feed.")
            }
        }

        if (!isLoadingFeed && jokes.isEmpty()) {
            item {
                StatusPanel("Feed leer", "Aktuell sind keine Witze für diesen Filter sichtbar.")
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
            when {
                isLoadingMoreFeed -> StatusPanel("Mehr Feed lädt", "Nächste Seite wird gerade über den Cursor geladen.")
                canLoadMoreFeed -> ComicCard {
                    Text("Mehr laden", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Cursor ist vorhanden — lade die nächste Feed-Seite.", color = Comic.Muted, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Mehr laden", Icons.Filled.Refresh, onClick = onLoadMore)
                }
                else -> StatusPanel("Feed live", "Android nutzt /api/mobile/feed ohne lokale Demo-Pagination.")
            }
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
    var undoStack by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var dragX by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    fun advanceRandomStack() {
        undoStack = (undoStack + currentIndex).takeLast(8)
        currentIndex += 1
    }

    if (jokes.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenHeader(title = "Zufallswitz", subtitle = "Zieh dir einen zufälligen Witz und swipe zum Nächsten.", badge = "Random")
            blockedUserMessage?.let {
                Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
            }
            StatusPanel("Random bereinigt", "Aktuell sind keine nicht blockierten Witze mehr sichtbar.")
        }
        return
    }

    val joke = jokes[currentIndex % jokes.size]

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenHeader(title = "Zufallswitz", subtitle = "Zieh dir einen zufälligen Witz und swipe zum Nächsten.", badge = "Random")
            blockedUserMessage?.let {
                Text(it, color = Comic.Red, fontWeight = FontWeight.Black)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(joke.id) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                dragX += dragAmount
                            },
                            onDragEnd = {
                                if (abs(dragX) > 120f) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    advanceRandomStack()
                                }
                                dragX = 0f
                            }
                        )
                    }
                    .rotate(dragX / 40f)
            ) {
                JokeCard(joke = joke, onOpen = { onOpenJoke(joke) }, onOpenProfile = onOpenProfile, onAuthRequired = onAuthRequired)
            }

            PrimaryButton("Neuen Random-Witz laden", Icons.Filled.Refresh) {
                advanceRandomStack()
            }

            StatusPanel(
                title = "Random kompakt",
                message = "Unten bleibt nur noch ein klarer CTA fürs Nachladen. Voting und Merken passieren direkt auf der Karte."
            )
        }

        if (undoStack.isNotEmpty()) {
            TextButton(
                onClick = {
                    val previous = undoStack.last()
                    currentIndex = previous
                    undoStack = undoStack.dropLast(1)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 18.dp)
            ) {
                Text("Undo: letzten Witz zurueckholen", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun DetailScreen(
    joke: Joke,
    blockedAuthors: List<String>,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit,
    onBlockAuthor: (String, String) -> Unit
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
        JokeCard(
            joke = joke,
            onOpen = {},
            onOpenProfile = onOpenProfile,
            onAuthRequired = onAuthRequired,
            truncatesLongContent = false
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ComicAction("Teilen", Icons.Filled.Share, Comic.Blue, Modifier.weight(1f)) {}
            ComicAction("Report", Icons.Filled.Flag, Comic.Pink, Modifier.weight(1f)) { onAuthRequired() }
            ComicAction("User blockieren", Icons.Filled.Person, Comic.Yellow, Modifier.weight(1f)) {
                onBlockAuthor(joke.authorId, joke.authorUsername)
            }
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
    val currentUser = sessionStore.currentUser
    var editUsername by rememberSaveable(currentUser?.id, currentUser?.username) { mutableStateOf(currentUser?.username.orEmpty()) }
    var editEmail by rememberSaveable(currentUser?.id, currentUser?.email) { mutableStateOf(currentUser?.email.orEmpty()) }
    var editPassword by rememberSaveable(currentUser?.id) { mutableStateOf("") }
    var accountFormError by rememberSaveable(currentUser?.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                    "Du kannst die App ohne Login nutzen. Für den Gastzugang musst du die Bedingungen akzeptieren und volljährig sein. Für Profil, Favoriten-Sync und Kontoverwaltung brauchst du außerdem einen Account.",
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
            ComicCard {
                Text(if (currentUser?.isGuest == true) "Gastkonto in normalen Account umwandeln" else "Kontodaten ändern", fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(
                    if (currentUser?.isGuest == true) {
                        "Hier machst du aus deinem Gastkonto einen normalen Account. Name, Mail und Passwort werden direkt übernommen."
                    } else {
                        "Deinen Namen kannst du genau einmal ändern. Deine Mailadresse kannst du beliebig oft aktualisieren."
                    },
                    color = Comic.Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = editUsername,
                    onValueChange = {
                        editUsername = it
                        accountFormError = null
                        sessionStore.clearAccountSuccessMessage()
                    },
                    label = { Text("Name") },
                    enabled = currentUser?.isGuest == true || currentUser?.canChangeUsername == true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                if (currentUser?.isGuest != true && currentUser?.canChangeUsername == false) {
                    Text("Namensänderung schon verbraucht — Mail kannst du weiter ändern.", color = Comic.Muted, modifier = Modifier.padding(top = 6.dp))
                }
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = {
                        editEmail = it
                        accountFormError = null
                        sessionStore.clearAccountSuccessMessage()
                    },
                    label = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                if (currentUser?.isGuest == true) {
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = {
                            editPassword = it
                            accountFormError = null
                            sessionStore.clearAccountSuccessMessage()
                        },
                        label = { Text("Passwort") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
                sessionStore.accountSuccessMessage?.let {
                    Text(it, color = Color(0xFF1B7F3B), modifier = Modifier.padding(top = 10.dp))
                }
                accountFormError?.let {
                    Text(it, color = Comic.Red, modifier = Modifier.padding(top = 10.dp))
                }
                PrimaryButton(
                    if (sessionStore.isUpdatingAccount) "Daten werden gespeichert..." else if (currentUser?.isGuest == true) "Gastkonto umwandeln" else "Daten speichern",
                    Icons.Filled.Edit,
                    onClick = {
                        val trimmedUsername = editUsername.trim()
                        val trimmedEmail = editEmail.trim()
                        val password = editPassword.trim().takeIf { currentUser?.isGuest == true && it.isNotEmpty() }
                        accountFormError = when {
                            trimmedUsername.length < 3 -> "Bitte gib einen Namen mit mindestens 3 Zeichen ein."
                            trimmedEmail.isBlank() -> "Bitte gib eine E-Mail ein."
                            currentUser?.isGuest == true && password == null -> "Bitte setze ein Passwort für den normalen Account."
                            else -> null
                        }
                        if (accountFormError == null) {
                            scope.launch {
                                sessionStore.updateAccount(
                                    username = trimmedUsername,
                                    email = trimmedEmail,
                                    password = password,
                                )
                                if (currentUser?.isGuest == true && sessionStore.profileError == null) {
                                    editPassword = ""
                                }
                            }
                        }
                    },
                    enabled = !sessionStore.isUpdatingAccount
                )
                Text(
                    if (sessionStore.accountSuccessMessage != null) "Zuletzt erfolgreich gespeichert." else if (currentUser?.isGuest == true) "Danach läuft dein Gastprofil als normaler Account weiter." else "Speichert Name und Mail direkt im Profil.",
                    color = Comic.Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
    var confirmedAdult by rememberSaveable { mutableStateOf(false) }
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nutzungsbedingungen / EULA", fontWeight = FontWeight.Black)
                Text("Vor Login oder Registrierung akzeptierst du die Regeln für UGC, Moderation und Account-Nutzung. Für den Gastzugang gilt zusätzlich: nur volljährig.", color = Comic.Muted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                    Text("Ich akzeptiere die Nutzungsbedingungen und Moderationsregeln.", color = Comic.Muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = confirmedAdult, onCheckedChange = { confirmedAdult = it })
                    Text("Ich bestätige, dass ich volljährig bin.", color = Comic.Muted)
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
            enabled = acceptedTerms && confirmedAdult && !sessionStore.isSubmittingAuth
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
    var viewerVote by rememberSaveable(joke.id) { mutableStateOf(joke.viewerVote) }
    var score by rememberSaveable(joke.id) { mutableStateOf(joke.score) }
    var feedbackLabel by rememberSaveable(joke.id) { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(joke.id, viewerVote, score) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        dragX += dragAmount
                    },
                    onDragEnd = {
                        val voteValue = if (dragX > 120f) 1 else if (dragX < -120f) -1 else null
                        if (abs(dragX) > 120f) {
                            voteValue?.let { value ->
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                val previousVote = viewerVote
                                score += value - (previousVote ?: 0)
                                viewerVote = value
                                feedbackLabel = when (value) {
                                    1 -> "Like registriert"
                                    else -> "Dislike registriert"
                                }
                                onAuthRequired()
                            }
                        }
                        dragX = 0f
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
private fun JokeCard(
    joke: Joke,
    onOpen: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onAuthRequired: () -> Unit,
    truncatesLongContent: Boolean = true
) {
    var isContentExpanded by rememberSaveable(joke.id, truncatesLongContent) { mutableStateOf(false) }
    val shouldShowContentDisclosure = truncatesLongContent && joke.content.length > JOKE_CARD_PREVIEW_LIMIT
    val visibleContent = if (shouldShowContentDisclosure && !isContentExpanded) {
        joke.content.take(JOKE_CARD_PREVIEW_LIMIT).trimEnd() + "…"
    } else {
        joke.content
    }

    ComicCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(joke.category, Comic.Yellow)
            Spacer(Modifier.weight(1f))
            ScoreBadge(joke.score)
        }
        Text(
            visibleContent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 28.sp,
            modifier = Modifier.padding(top = 14.dp)
        )
        if (shouldShowContentDisclosure) {
            JokeDisclosureButton(
                expanded = isContentExpanded,
                onClick = { isContentExpanded = !isContentExpanded }
            )
        }
        TextButton(onClick = { onOpenProfile(joke.authorUsername) }) {
            Text(
                "von @${joke.authorUsername}",
                color = Comic.Muted,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
            ReactionTile("Top", Icons.Filled.ThumbUp, joke.viewerVote == 1, Modifier.weight(1f), onAuthRequired, showTitle = false)
            ReactionTile("Runter", Icons.Filled.ThumbDown, joke.viewerVote == -1, Modifier.weight(1f), onAuthRequired, showTitle = false)
            ReactionTile("Superlike", Icons.Filled.Star, joke.viewerVote == 5, Modifier.weight(1f), onAuthRequired, showTitle = false)
            ReactionTile(if (joke.viewerFavorite) "Gemerkt" else "Merken", Icons.Filled.Bookmark, joke.viewerFavorite, Modifier.weight(1f), onAuthRequired)
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
            .background(Comic.Ink, RoundedCornerShape(24.dp))
            .padding(3.dp)
            .background(Comic.Paper, RoundedCornerShape(24.dp))
            .padding(18.dp),
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
private fun JokeDisclosureButton(expanded: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Comic.Yellow.copy(alpha = 0.88f),
        border = BorderStroke(1.5.dp, Comic.Ink),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                null,
                tint = Comic.Ink,
                modifier = Modifier.size(18.dp)
            )
            Text(
                if (expanded) "Weniger anzeigen" else "Mehr anzeigen",
                color = Comic.Ink,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ReactionTile(title: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit, showTitle: Boolean = true) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(if (active) Comic.Yellow else Comic.BlueSoft, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = if (showTitle) 10.dp else 12.dp)
    ) {
        Icon(
            icon,
            title,
            tint = if (active) Comic.Red else Comic.Ink,
            modifier = Modifier.size(if (showTitle) 24.dp else 32.dp)
        )
        if (showTitle) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun ComicAction(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
private fun PrimaryButton(title: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    ComicAction(title = title, icon = icon, color = Comic.Yellow, modifier = Modifier.fillMaxWidth(), enabled = enabled, onClick = onClick)
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
