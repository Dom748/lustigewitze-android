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
    var blockMessage by mutableStateOf<String?>(null)
        private set
    var isSubmittingAuth by mutableStateOf(false)
        private set
    var isLoadingProfile by mutableStateOf(false)
        private set
    var isDeletingAccount by mutableStateOf(false)
        private set
    var isBlockingUser by mutableStateOf(false)
        private set

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
        }
    }

    suspend fun loadProfile(username: String) {
        isLoadingProfile = true
        profileError = null
        try {
            loadedProfile = apiClient.getProfile(username, accessToken)
        } catch (err: MobileApiException) {
            profileError = err.message
        } finally {
            isLoadingProfile = false
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

    suspend fun blockAuthorAndReport(authorId: String, jokeId: String): Boolean {
        val token = accessToken ?: run {
            blockMessage = "Bitte logge dich ein, um User zu blockieren."
            return false
        }
        isBlockingUser = true
        blockMessage = null
        try {
            val result = apiClient.blockUser(authorId = authorId, jokeId = jokeId, accessToken = token)
            blockMessage = "blocked_user:${result.blockedUserId}"
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
        blockMessage = null
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
        prefs.edit().remove("accessToken").remove("refreshToken").apply()
    }
}
