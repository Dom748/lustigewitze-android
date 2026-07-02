import * as assert from "node:assert/strict";
import { test } from "node:test";
import { readFileSync } from "node:fs";
import path from "node:path";

const root = process.cwd();
const mainActivity = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt"), "utf8");
const apiClient = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/MobileApi.kt"), "utf8");
const sessionStore = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/SessionStore.kt"), "utf8");

test("android ships a first-party mobile api client for auth profile feed and block flows", () => {
  assert.match(apiClient, /private const val MOBILE_API_BASE_URL = "https:\/\/api\.lustigewitze\.fun"/);
  assert.match(apiClient, /suspend fun login\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/login"/);
  assert.match(apiClient, /suspend fun register\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/register"/);
  assert.match(apiClient, /suspend fun getFeed\(/);
  assert.match(apiClient, /request\("GET", "\/api\/mobile\/feed\?\$query"/);
  assert.match(apiClient, /MobileFeedResult\(/);
  assert.match(apiClient, /suspend fun getCurrentUser\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/me"/);
  assert.match(apiClient, /suspend fun deleteAccount\(/);
  assert.match(apiClient, /request\("DELETE", "\/api\/mobile\/auth\/me"/);
  assert.match(apiClient, /suspend fun getProfile\(username: String/);
  assert.match(apiClient, /"\/api\/mobile\/profile\/\$\{encodedUsername\}"/);
  assert.match(apiClient, /suspend fun blockUser\(authorId: String, jokeId: String, accessToken: String\)/);
  assert.match(apiClient, /"\/api\/mobile\/users\/\$authorId\/block"/);
  assert.match(apiClient, /data class BlockedUserSummary\(/);
  assert.match(apiClient, /suspend fun getBlockedUsers\(accessToken: String\)/);
  assert.match(apiClient, /"\/api\/mobile\/blocked-users"/);
  assert.match(apiClient, /suspend fun unblockUser\(userId: String, accessToken: String\)/);
  assert.match(apiClient, /request\("DELETE", "\/api\/mobile\/users\/\$userId\/block"/);
});

test("android persists session tokens and exposes async auth account helpers", () => {
  assert.match(sessionStore, /class SessionStore\(/);
  assert.match(sessionStore, /SharedPreferences/);
  assert.match(sessionStore, /var accessToken by mutableStateOf/);
  assert.match(sessionStore, /var refreshToken by mutableStateOf/);
  assert.match(sessionStore, /var feedItems by mutableStateOf<List<MobileJoke>>\(emptyList\(\)\)/);
  assert.match(sessionStore, /var canLoadMoreFeed by mutableStateOf\(false\)/);
  assert.match(sessionStore, /suspend fun login\(/);
  assert.match(sessionStore, /suspend fun register\(/);
  assert.match(sessionStore, /suspend fun loadOwnProfile\(/);
  assert.match(sessionStore, /suspend fun loadProfile\(/);
  assert.match(sessionStore, /suspend fun loadFeed\(sort: String, category: String\)/);
  assert.match(sessionStore, /suspend fun loadMoreFeed\(sort: String, category: String\)/);
  assert.match(sessionStore, /apiClient\.getFeed\(sort = sort, category = category/);
  assert.match(sessionStore, /suspend fun deleteAccount\(/);
  assert.match(sessionStore, /suspend fun blockAuthorAndReport\(/);
  assert.match(sessionStore, /var blockedUsers by mutableStateOf<List<BlockedUserSummary>>\(emptyList\(\)\)/);
  assert.match(sessionStore, /suspend fun loadBlockedUsers\(\)/);
  assert.match(sessionStore, /suspend fun unblockUser\(user: BlockedUserSummary\): Boolean/);
});

test("android ui is wired to the real session store instead of local demo-only account actions", () => {
  assert.match(mainActivity, /val sessionStore = remember \{ SessionStore\(/);
  assert.match(mainActivity, /LaunchedEffect\(feedSort, feedCategory, sessionStore\.accessToken\) \{[\s\S]*sessionStore\.loadFeed\(sort = feedSort, category = feedCategory\)/);
  assert.match(mainActivity, /AuthSheet\(sessionStore = sessionStore/);
  assert.match(mainActivity, /ProfileScreen\([\s\S]*sessionStore = sessionStore/);
  assert.match(mainActivity, /sessionStore\.blockAuthorAndReport\(/);
  assert.match(mainActivity, /sessionStore\.deleteAccount\(/);
  assert.match(mainActivity, /sessionStore\.loadProfile\(/);
  assert.match(mainActivity, /sessionStore\.loadOwnProfile\(/);
  assert.match(mainActivity, /sessionStore\.loadMoreFeed\(sort = feedSort, category = feedCategory\)/);
});

test("android guest profile no longer fakes a logged-in pointenpaule account", () => {
  assert.match(mainActivity, /Tab\.Profile -> ProfileScreen\([\s\S]*username = sessionStore\.currentUser\?\.username,/);
  assert.match(mainActivity, /if \(username == null\) \{[\s\S]*badge = "Gast"/);
  assert.match(mainActivity, /Text\("Gastkonto aktiv"/);
  assert.match(mainActivity, /PrimaryButton\("Login \/ Register", Icons\.AutoMirrored\.Filled\.Login, onClick = onAuthRequired\)/);
});

test("android feed screen now uses the live mobile feed instead of the local demo pagination placeholder", () => {
  assert.match(mainActivity, /val visibleJokes = \(if \(sessionStore\.hasLoadedFeed\) \{/);
  assert.match(mainActivity, /sessionStore\.feedItems\.map \{ it\.toAppJoke\(\) \}/);
  assert.match(mainActivity, /FeedScreen\([\s\S]*selectedSort = feedSort,[\s\S]*selectedCategory = feedCategory,[\s\S]*feedError = sessionStore\.feedError,[\s\S]*canLoadMoreFeed = sessionStore\.canLoadMoreFeed/);
  assert.match(mainActivity, /StatusPanel\("Feed lädt", "Android zieht jetzt echte Witze von \/api\/mobile\/feed\."\)/);
  assert.match(mainActivity, /StatusPanel\("Feed live", "Android nutzt \/api\/mobile\/feed ohne lokale Demo-Pagination\."\)/);
  assert.match(mainActivity, /PrimaryButton\("Mehr laden", Icons\.Filled\.Refresh, onClick = onLoadMore\)/);
  assert.doesNotMatch(mainActivity, /Naechster Schritt: API-Client an \/api\/mobile\/feed mit cursor anbinden\./);
});

test("android account deletion copy reflects the real mobile api flow", () => {
  assert.match(mainActivity, /StatusPanel\("Konto gelöscht", "Dein Account wurde über die Mobile API gelöscht und lokal aus der Session entfernt\."\)/);
  assert.match(mainActivity, /Text\("Dieser Schritt löscht dein Konto über die Mobile API und entfernt deine Session auf diesem Gerät\."/);
  assert.doesNotMatch(mainActivity, /Demo-State: Account wurde lokal als gelöscht markiert\./);
  assert.doesNotMatch(mainActivity, /Dieser Demo-Flow markiert den Account lokal als gelöscht\./);
});

test("android detail screen mirrors ios comment api wiring instead of a read-only placeholder", () => {
  assert.match(apiClient, /data class MobileCommentResponse\(/);
  assert.match(apiClient, /data class MobileComment\(/);
  assert.match(apiClient, /suspend fun getComments\(jokeId: String, accessToken: String\? = null\)/);
  assert.match(apiClient, /"\/api\/mobile\/jokes\/\$jokeId\/comments"/);
  assert.match(apiClient, /suspend fun addComment\(jokeId: String, content: String, accessToken: String\)/);
  assert.match(apiClient, /request\("POST", "\/api\/mobile\/jokes\/\$jokeId\/comments"/);
  assert.match(sessionStore, /var detailComments by mutableStateOf<List<MobileComment>>\(emptyList\(\)\)/);
  assert.match(sessionStore, /var isPostingComment by mutableStateOf\(false\)/);
  assert.match(sessionStore, /suspend fun loadComments\(jokeId: String\)/);
  assert.match(sessionStore, /suspend fun addComment\(jokeId: String, content: String\): Boolean/);
  assert.match(mainActivity, /sessionStore\.loadComments\(joke\.id\)/);
  assert.match(mainActivity, /CommentThreadPanel\([\s\S]*comments = sessionStore\.detailComments/);
  assert.match(mainActivity, /CommentComposerCard\(sessionStore = sessionStore, jokeId = joke\.id, onAuthRequired = onAuthRequired\)/);
  assert.doesNotMatch(mainActivity, /readOnly = true/);
  assert.match(mainActivity, /placeholder = \{ Text\("Kommentar schreiben\.\.\."\) \}/);
  assert.match(mainActivity, /PrimaryButton\("Senden", Icons\.Filled\.Share/);
});
