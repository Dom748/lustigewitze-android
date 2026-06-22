import * as assert from "node:assert/strict";
import { test } from "node:test";
import { readFileSync } from "node:fs";
import path from "node:path";

const root = process.cwd();
const mainActivity = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt"), "utf8");
const apiClient = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/MobileApi.kt"), "utf8");
const sessionStore = readFileSync(path.join(root, "app/src/main/java/studio/broapp/lustigewitze/SessionStore.kt"), "utf8");

test("android ships a first-party mobile api client for auth profile and block flows", () => {
  assert.match(apiClient, /private const val MOBILE_API_BASE_URL = "https:\/\/api\.lustigewitze\.fun"/);
  assert.match(apiClient, /suspend fun login\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/login"/);
  assert.match(apiClient, /suspend fun register\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/register"/);
  assert.match(apiClient, /suspend fun getCurrentUser\(/);
  assert.match(apiClient, /"\/api\/mobile\/auth\/me"/);
  assert.match(apiClient, /suspend fun deleteAccount\(/);
  assert.match(apiClient, /request\("DELETE", "\/api\/mobile\/auth\/me"/);
  assert.match(apiClient, /suspend fun getProfile\(username: String/);
  assert.match(apiClient, /"\/api\/mobile\/profile\/\$\{encodedUsername\}"/);
  assert.match(apiClient, /suspend fun blockUser\(authorId: String, jokeId: String, accessToken: String\)/);
  assert.match(apiClient, /"\/api\/mobile\/users\/\$authorId\/block"/);
});

test("android persists session tokens and exposes async auth account helpers", () => {
  assert.match(sessionStore, /class SessionStore\(/);
  assert.match(sessionStore, /SharedPreferences/);
  assert.match(sessionStore, /var accessToken by mutableStateOf/);
  assert.match(sessionStore, /var refreshToken by mutableStateOf/);
  assert.match(sessionStore, /suspend fun login\(/);
  assert.match(sessionStore, /suspend fun register\(/);
  assert.match(sessionStore, /suspend fun loadOwnProfile\(/);
  assert.match(sessionStore, /suspend fun loadProfile\(/);
  assert.match(sessionStore, /suspend fun deleteAccount\(/);
  assert.match(sessionStore, /suspend fun blockAuthorAndReport\(/);
});

test("android ui is wired to the real session store instead of local demo-only account actions", () => {
  assert.match(mainActivity, /val sessionStore = remember \{ SessionStore\(/);
  assert.match(mainActivity, /AuthSheet\(sessionStore = sessionStore/);
  assert.match(mainActivity, /ProfileScreen\([\s\S]*sessionStore = sessionStore/);
  assert.match(mainActivity, /sessionStore\.blockAuthorAndReport\(/);
  assert.match(mainActivity, /sessionStore\.deleteAccount\(/);
  assert.match(mainActivity, /sessionStore\.loadProfile\(/);
  assert.match(mainActivity, /sessionStore\.loadOwnProfile\(/);
});
