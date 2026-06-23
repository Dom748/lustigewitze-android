package studio.broapp.lustigewitze

import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val MOBILE_API_BASE_URL = "https://api.lustigewitze.fun"

class MobileApiException(
    val code: String,
    override val message: String,
    val status: Int,
    val fields: Map<String, String> = emptyMap()
) : Exception(message)

data class MobileAuthUser(
    val id: String,
    val username: String,
    val email: String,
    val bio: String?,
    val isGuest: Boolean,
    val canChangeUsername: Boolean,
    val createdAt: String,
    val proSince: String?
)

data class MobileAuthResult(
    val accessToken: String,
    val refreshToken: String,
    val user: MobileAuthUser
)

data class MobileProfileStats(
    val jokeCount: Int,
    val totalScore: Int,
    val averageScore: Int,
    val favoriteCategory: String
)

data class MobileJokeAuthor(
    val id: String,
    val username: String
)

data class MobileJoke(
    val id: String,
    val content: String,
    val category: String,
    val author: MobileJokeAuthor,
    val score: Int,
    val favoriteCount: Int,
    val legendCount: Int,
    val viewerVote: Int?,
    val viewerFavorite: Boolean,
    val createdAt: String
)

data class MobileFeedResult(
    val items: List<MobileJoke>,
    val nextCursor: Int?
)

data class MobileProfileResult(
    val user: MobileAuthUser,
    val stats: MobileProfileStats,
    val jokes: List<MobileJoke>,
    val favorites: List<MobileJoke>
)

data class BlockUserResult(
    val ok: Boolean,
    val blockedUserId: String,
    val reportId: String?
)

data class BlockedUserSummary(
    val id: String,
    val username: String,
    val blockedAt: String
)

class MobileApiClient {
    suspend fun login(identifier: String, password: String): MobileAuthResult {
        val payload = JSONObject()
            .put("identifier", identifier)
            .put("password", password)
        val json = request("POST", "/api/mobile/auth/login", payload = payload)
        return parseAuthResult(json)
    }

    suspend fun register(username: String, email: String, password: String): MobileAuthResult {
        val payload = JSONObject()
            .put("username", username)
            .put("email", email)
            .put("password", password)
        val json = request("POST", "/api/mobile/auth/register", payload = payload)
        return parseAuthResult(json)
    }

    suspend fun getFeed(
        sort: String = "latest",
        category: String = "all",
        cursor: Int? = null,
        accessToken: String? = null
    ): MobileFeedResult {
        val query = buildList {
            add("sort=${URLEncoder.encode(sort, Charsets.UTF_8.name())}")
            add("category=${URLEncoder.encode(category, Charsets.UTF_8.name())}")
            cursor?.let { add("cursor=$it") }
        }.joinToString("&")
        val json = request("GET", "/api/mobile/feed?$query", accessToken = accessToken)
        return MobileFeedResult(
            items = parseJokes(json.optJSONArray("items")),
            nextCursor = json.optInt("nextCursor").takeIf { !json.isNull("nextCursor") }
        )
    }

    suspend fun getCurrentUser(accessToken: String): MobileAuthUser {
        val json = request("GET", "/api/mobile/auth/me", accessToken = accessToken)
        return parseUser(json.getJSONObject("user"))
    }

    suspend fun deleteAccount(accessToken: String) {
        request("DELETE", "/api/mobile/auth/me", accessToken = accessToken)
    }

    suspend fun updateAccount(
        accessToken: String,
        username: String,
        email: String,
        password: String? = null
    ): MobileAuthResult {
        val payload = JSONObject()
            .put("username", username)
            .put("email", email)
        if (!password.isNullOrBlank()) {
            payload.put("password", password)
        }
        val json = request("PATCH", "/api/mobile/auth/me", payload = payload, accessToken = accessToken)
        return parseAuthResult(json)
    }

    suspend fun getProfile(username: String, accessToken: String?): MobileProfileResult {
        val encodedUsername = URLEncoder.encode(username, Charsets.UTF_8.name())
        val json = request("GET", "/api/mobile/profile/${encodedUsername}", accessToken = accessToken)
        return MobileProfileResult(
            user = parseUser(json.getJSONObject("user")),
            stats = parseStats(json.getJSONObject("stats")),
            jokes = parseJokes(json.optJSONArray("jokes")),
            favorites = parseJokes(json.optJSONArray("favorites"))
        )
    }

    suspend fun blockUser(authorId: String, jokeId: String, accessToken: String): BlockUserResult {
        val payload = JSONObject()
            .put("jokeId", jokeId)
            .put("reason", "abusive_user")
            .put("details", "Android block flow meldet den Author und blendet Inhalte direkt aus.")
        val json = request("POST", "/api/mobile/users/$authorId/block", payload = payload, accessToken = accessToken)
        return BlockUserResult(
            ok = json.optBoolean("ok", false),
            blockedUserId = json.optString("blockedUserId"),
            reportId = json.optString("reportId").takeIf { !it.isNullOrBlank() }
        )
    }

    suspend fun getBlockedUsers(accessToken: String): List<BlockedUserSummary> {
        val json = request("GET", "/api/mobile/blocked-users", accessToken = accessToken)
        val items = json.optJSONArray("items")
        if (items == null) return emptyList()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    BlockedUserSummary(
                        id = item.getString("id"),
                        username = item.getString("username"),
                        blockedAt = item.optString("blockedAt")
                    )
                )
            }
        }
    }

    suspend fun unblockUser(userId: String, accessToken: String) {
        request("DELETE", "/api/mobile/users/$userId/block", accessToken = accessToken)
    }

    private suspend fun request(
        method: String,
        path: String,
        payload: JSONObject? = null,
        accessToken: String? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        val connection = (URL(MOBILE_API_BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            if (!accessToken.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $accessToken")
            }
            doInput = true
            if (payload != null) {
                doOutput = true
            }
        }

        try {
            if (payload != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            val json = if (body.isBlank()) JSONObject() else JSONObject(body)

            if (status !in 200..299) {
                val fieldsObject = json.optJSONObject("fields")
                val fields = buildMap {
                    if (fieldsObject != null) {
                        val keys = fieldsObject.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            put(key, fieldsObject.optString(key))
                        }
                    }
                }
                throw MobileApiException(
                    code = json.optString("error", "network_error"),
                    message = json.optString("message", "Request failed."),
                    status = status,
                    fields = fields
                )
            }

            json
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAuthResult(json: JSONObject): MobileAuthResult {
        return MobileAuthResult(
            accessToken = json.getString("accessToken"),
            refreshToken = json.getString("refreshToken"),
            user = parseUser(json.getJSONObject("user"))
        )
    }

    private fun parseUser(json: JSONObject): MobileAuthUser {
        return MobileAuthUser(
            id = json.getString("id"),
            username = json.getString("username"),
            email = json.optString("email"),
            bio = json.optString("bio").takeIf { it.isNotBlank() },
            isGuest = json.optBoolean("isGuest", false),
            canChangeUsername = json.optBoolean("canChangeUsername", true),
            createdAt = json.optString("createdAt"),
            proSince = json.optString("proSince").takeIf { it.isNotBlank() && it != "null" }
        )
    }

    private fun parseStats(json: JSONObject): MobileProfileStats {
        return MobileProfileStats(
            jokeCount = json.optInt("jokeCount", 0),
            totalScore = json.optInt("totalScore", 0),
            averageScore = json.optDouble("averageScore", 0.0).toInt(),
            favoriteCategory = json.optString("favoriteCategory", "Noch offen")
        )
    }

    private fun parseJokes(array: JSONArray?): List<MobileJoke> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val author = item.getJSONObject("author")
                add(
                    MobileJoke(
                        id = item.getString("id"),
                        content = item.getString("content"),
                        category = item.getString("category"),
                        author = MobileJokeAuthor(
                            id = author.getString("id"),
                            username = author.getString("username")
                        ),
                        score = item.optInt("score", 0),
                        favoriteCount = item.optInt("favoriteCount", 0),
                        legendCount = item.optInt("legendCount", 0),
                        viewerVote = item.optInt("viewerVote").takeIf { !item.isNull("viewerVote") },
                        viewerFavorite = item.optBoolean("viewerFavorite", false),
                        createdAt = item.optString("createdAt")
                    )
                )
            }
        }
    }
}
