package studio.broapp.lustigewitze

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SessionStore(
    context: Context,
    private val apiClient: MobileApiClient
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lustigewitze-mobile-session", Context.MODE_PRIVATE)

    var accessToken by mutableStateOf(prefs.getString("accessToken", null))
        private set
    var refreshToken by mutableStateOf(prefs.getString("refreshToken", null))
        private set
    var currentUser by mutableStateOf<MobileAuthUser?>(null)
        private set
    var loadedProfile by mutableStateOf<MobileProfileResult?>(null)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var profileError by mutableStateOf<String?>(null)
        private set
    var accountSuccessMessage by mutableStateOf<String?>(null)
        private set
    var feedItems by mutableStateOf<List<MobileJoke>>(emptyList())
        private set
    var detailComments by mutableStateOf<List<MobileComment>>(emptyList())
        private set
    var detailCommentsError by mutableStateOf<String?>(null)
        private set
    var feedError by mutableStateOf<String?>(null)
        private set
    var isLoadingFeed by mutableStateOf(false)
        private set
    var isLoadingComments by mutableStateOf(false)
        private set
    var isPostingComment by mutableStateOf(false)
        private set
    var hasLoadedFeed by mutableStateOf(false)
        private set
    var isLoadingMoreFeed by mutableStateOf(false)
        private set
    var canLoadMoreFeed by mutableStateOf(false)
        private set
    var blockMessage by mutableStateOf<String?>(null)
        private set
    var isSubmittingAuth by mutableStateOf(false)
        private set
    var isLoadingProfile by mutableStateOf(false)
        private set
    var isDeletingAccount by mutableStateOf(false)
        private set
    var isUpdatingAccount by mutableStateOf(false)
        private set
    var isBlockingUser by mutableStateOf(false)
        private set
    var blockedUsers by mutableStateOf<List<BlockedUserSummary>>(emptyList())
        private set
    var blockedUsersError by mutableStateOf<String?>(null)
        private set
    var blockedUsersSuccessMessage by mutableStateOf<String?>(null)
        private set
    var isLoadingBlockedUsers by mutableStateOf(false)
        private set
    var pendingBlockedUserId by mutableStateOf<String?>(null)
        private set
    private var nextFeedCursor: Int? = null

    suspend fun login(identifier: String, password: String) {
        isSubmittingAuth = true
        authError = null
        try {
            val result = apiClient.login(identifier = identifier, password = password)
            persistTokens(result.accessToken, result.refreshToken)
            currentUser = result.user
            loadProfile(result.user.username)
        } catch (err: MobileApiException) {
            authError = err.fields.values.firstOrNull() ?: err.message
        } catch (err: Exception) {
            authError = err.message ?: "Login konnte gerade nicht abgeschlossen werden."
        } finally {
            isSubmittingAuth = false
        }
    }

    suspend fun register(username: String, email: String, password: String) {
        isSubmittingAuth = true
        authError = null
        try {
            val result = apiClient.register(username = username, email = email, password = password)
            persistTokens(result.accessToken, result.refreshToken)
            currentUser = result.user
            loadProfile(result.user.username)
        } catch (err: MobileApiException) {
            authError = err.fields.values.firstOrNull() ?: err.message
        } catch (err: Exception) {
            authError = err.message ?: "Registrierung konnte gerade nicht abgeschlossen werden."
        } finally {
            isSubmittingAuth = false
        }
    }

    suspend fun ensureGuestSession() {
        if (currentUser != null || !accessToken.isNullOrBlank()) return
        isSubmittingAuth = true
        authError = null
        try {
            val result = apiClient.createGuestSession()
            persistTokens(result.accessToken, result.refreshToken)
            currentUser = result.user
            loadProfile(result.user.username)
        } catch (err: MobileApiException) {
            authError = "Gastmodus konnte gerade nicht gestartet werden. Versuch es gleich noch einmal."
        } catch (err: Exception) {
            authError = "Gastmodus konnte gerade nicht gestartet werden. Versuch es gleich noch einmal."
        } finally {
            isSubmittingAuth = false
        }
    }

    suspend fun loadOwnProfile() {
        val token = accessToken ?: return
        try {
            currentUser = apiClient.getCurrentUser(token)
            currentUser?.username?.let { username ->
                loadProfile(username)
            }
        } catch (err: MobileApiException) {
            profileError = err.message
        } catch (err: Exception) {
            profileError = err.message ?: "Sitzung konnte nicht wiederhergestellt werden."
            clear()
        }
    }

    suspend fun loadProfile(username: String) {
        isLoadingProfile = true
        profileError = null
        try {
            loadedProfile = apiClient.getProfile(username, accessToken)
        } catch (err: MobileApiException) {
            profileError = err.message
        } catch (err: Exception) {
            profileError = err.message ?: "Profil konnte nicht geladen werden."
            loadedProfile = null
        } finally {
            isLoadingProfile = false
        }
    }

    suspend fun loadFeed(sort: String, category: String) {
        isLoadingFeed = true
        feedError = null
        try {
            val result = apiClient.getFeed(sort = sort, category = category, accessToken = accessToken)
            feedItems = result.items
            nextFeedCursor = result.nextCursor
            canLoadMoreFeed = result.nextCursor != null
            hasLoadedFeed = true
        } catch (err: MobileApiException) {
            feedError = err.message
            feedItems = emptyList()
            nextFeedCursor = null
            canLoadMoreFeed = false
        } catch (err: Exception) {
            feedError = err.message ?: "Feed konnte nicht geladen werden."
            feedItems = emptyList()
            nextFeedCursor = null
            canLoadMoreFeed = false
        } finally {
            isLoadingFeed = false
        }
    }

    suspend fun loadComments(jokeId: String) {
        isLoadingComments = true
        detailCommentsError = null
        try {
            detailComments = apiClient.getComments(jokeId, accessToken).items
        } catch (err: MobileApiException) {
            detailCommentsError = err.message
            detailComments = emptyList()
        } catch (err: Exception) {
            detailCommentsError = err.message ?: "Kommentare konnten nicht geladen werden."
            detailComments = emptyList()
        } finally {
            isLoadingComments = false
        }
    }

    suspend fun addComment(jokeId: String, content: String): Boolean {
        val token = accessToken ?: run {
            detailCommentsError = "Bitte logge dich ein, um Kommentare zu schreiben."
            return false
        }
        val trimmed = content.trim()
        if (trimmed.isEmpty() || isPostingComment) {
            return false
        }
        isPostingComment = true
        detailCommentsError = null
        return try {
            val created = apiClient.addComment(jokeId = jokeId, content = trimmed, accessToken = token)
            detailComments = listOf(created) + detailComments
            true
        } catch (err: MobileApiException) {
            detailCommentsError = err.fields.values.firstOrNull() ?: err.message
            false
        } catch (err: Exception) {
            detailCommentsError = err.message ?: "Kommentar konnte nicht gesendet werden."
            false
        } finally {
            isPostingComment = false
        }
    }

    suspend fun loadMoreFeed(sort: String, category: String) {
        val cursor = nextFeedCursor ?: return
        if (isLoadingMoreFeed || isLoadingFeed) return
        isLoadingMoreFeed = true
        feedError = null
        try {
            val result = apiClient.getFeed(sort = sort, category = category, cursor = cursor, accessToken = accessToken)
            val existingIds = feedItems.map { it.id }.toSet()
            feedItems = feedItems + result.items.filterNot { existingIds.contains(it.id) }
            nextFeedCursor = result.nextCursor
            canLoadMoreFeed = result.nextCursor != null
        } catch (err: MobileApiException) {
            feedError = err.message
        } catch (err: Exception) {
            feedError = err.message ?: "Weitere Feed-Witze konnten nicht geladen werden."
        } finally {
            isLoadingMoreFeed = false
        }
    }

    suspend fun deleteAccount() {
        val token = accessToken ?: return
        isDeletingAccount = true
        profileError = null
        try {
            apiClient.deleteAccount(token)
            clear()
        } catch (err: MobileApiException) {
            profileError = err.message
        } finally {
            isDeletingAccount = false
        }
    }

    suspend fun updateAccount(username: String, email: String, password: String? = null) {
        val token = accessToken ?: return
        isUpdatingAccount = true
        profileError = null
        accountSuccessMessage = null
        try {
            val result = apiClient.updateAccount(
                accessToken = token,
                username = username,
                email = email,
                password = password,
            )
            persistTokens(result.accessToken, result.refreshToken)
            currentUser = result.user
            accountSuccessMessage = if (result.user.isGuest) "Gastkonto erfolgreich umgewandelt." else "Kontodaten gespeichert."
            loadProfile(result.user.username)
        } catch (err: MobileApiException) {
            profileError = err.fields.values.firstOrNull() ?: err.message
            accountSuccessMessage = null
        } finally {
            isUpdatingAccount = false
        }
    }

    fun clearAccountSuccessMessage() {
        accountSuccessMessage = null
    }

    suspend fun loadBlockedUsers() {
        val token = accessToken ?: return
        isLoadingBlockedUsers = true
        blockedUsersError = null
        try {
            blockedUsers = apiClient.getBlockedUsers(token)
        } catch (err: MobileApiException) {
            blockedUsersError = err.message
        } finally {
            isLoadingBlockedUsers = false
        }
    }

    suspend fun unblockUser(user: BlockedUserSummary): Boolean {
        val token = accessToken ?: run {
            blockedUsersError = "Bitte logge dich ein, um User zu entblocken."
            return false
        }
        pendingBlockedUserId = user.id
        blockedUsersError = null
        blockedUsersSuccessMessage = null
        return try {
            apiClient.unblockUser(user.id, token)
            blockedUsers = blockedUsers.filterNot { it.id == user.id }
            blockedUsersSuccessMessage = "@${user.username} entblockt"
            true
        } catch (err: MobileApiException) {
            blockedUsersError = err.message
            false
        } finally {
            pendingBlockedUserId = null
        }
    }

    suspend fun blockAuthorAndReport(authorId: String, authorUsername: String, jokeId: String): Boolean {
        val token = accessToken ?: run {
            blockMessage = "Bitte logge dich ein, um User zu blockieren."
            return false
        }
        isBlockingUser = true
        blockMessage = null
        try {
            val result = apiClient.blockUser(authorId = authorId, jokeId = jokeId, accessToken = token)
            blockMessage = "blocked_user:${result.blockedUserId}:$authorUsername"
            return result.ok
        } catch (err: MobileApiException) {
            blockMessage = err.message
            return false
        } finally {
            isBlockingUser = false
        }
    }

    fun clearErrors() {
        authError = null
        profileError = null
        feedError = null
        detailCommentsError = null
        blockMessage = null
        blockedUsersError = null
    }

    private fun persistTokens(nextAccessToken: String, nextRefreshToken: String) {
        accessToken = nextAccessToken
        refreshToken = nextRefreshToken
        prefs.edit()
            .putString("accessToken", nextAccessToken)
            .putString("refreshToken", nextRefreshToken)
            .apply()
    }

    private fun clear() {
        accessToken = null
        refreshToken = null
        currentUser = null
        loadedProfile = null
        blockedUsers = emptyList()
        blockedUsersError = null
        blockedUsersSuccessMessage = null
        detailComments = emptyList()
        detailCommentsError = null
        feedItems = emptyList()
        feedError = null
        nextFeedCursor = null
        canLoadMoreFeed = false
        hasLoadedFeed = false
        prefs.edit().remove("accessToken").remove("refreshToken").apply()
    }
}
