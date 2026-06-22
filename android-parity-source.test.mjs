import * as assert from "node:assert/strict";
import { test } from "node:test";
import { readFileSync } from "node:fs";
import path from "node:path";

const source = readFileSync(
  path.join(process.cwd(), "app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt"),
  "utf8",
);
const sessionStore = readFileSync(
  path.join(process.cwd(), "app/src/main/java/studio/broapp/lustigewitze/SessionStore.kt"),
  "utf8",
);
const mobileApi = readFileSync(
  path.join(process.cwd(), "app/src/main/java/studio/broapp/lustigewitze/MobileApi.kt"),
  "utf8",
);

test("android auth sheet gates login and registration behind terms acceptance", () => {
  assert.equal(source.includes("var acceptedTerms by rememberSaveable"), true, "AuthSheet should track accepted terms state");
  assert.equal(source.includes("var confirmedAdult by rememberSaveable"), true, "AuthSheet should track a separate adulthood confirmation state");
  assert.equal(source.includes("Nutzungsbedingungen / EULA"), true, "AuthSheet should show a visible terms / EULA block");
  assert.equal(source.includes("Checkbox(checked = acceptedTerms"), true, "AuthSheet should require an explicit checkbox");
  assert.equal(source.includes("Checkbox(checked = confirmedAdult"), true, "AuthSheet should require an explicit legal-age checkbox");
  assert.equal(source.includes("Ich bestätige, dass ich volljährig bin."), true, "AuthSheet should visibly mention the legal-age confirmation");
  assert.equal(source.includes("enabled = acceptedTerms && confirmedAdult"), true, "Auth submit should stay disabled until terms and legal-age confirmation are accepted");
});

test("android profile screen exposes real profile stats and direct account deletion", () => {
  assert.equal(source.includes("Gastmodus"), false, "ProfileScreen should no longer be a guest-mode placeholder only");
  assert.equal(source.includes("Offene API-Abhaengigkeit"), false, "ProfileScreen should no longer advertise missing profile API wiring as the main state");
  assert.equal(source.includes("Lieblingskategorie"), true, "ProfileScreen should surface profile stats like iOS");
  assert.equal(source.includes("Ø Score / Joke"), true, "ProfileScreen should surface average score stats like iOS");
  assert.equal(source.includes("Konto löschen"), true, "ProfileScreen should expose direct account deletion");
  assert.equal(source.includes("Konto wirklich löschen?"), true, "Profile deletion should warn before destructive action");
});

test("android profile lets guests upgrade and regular users edit email with one-time username change", () => {
  assert.equal(source.includes("Gastkonto in normalen Account umwandeln"), true, "Profile should explain the guest-upgrade flow");
  assert.equal(source.includes("Gastkonto umwandeln"), true, "Profile should expose a clear guest-upgrade CTA");
  assert.equal(source.includes("Deinen Namen kannst du genau einmal ändern"), true, "Profile should explain the one-time username rule");
  assert.equal(source.includes("Beliebig oft") || source.includes("beliebig oft aktualisieren"), true, "Profile should explain the email can be changed repeatedly");
  assert.equal(source.includes("currentUser?.canChangeUsername"), true, "Profile should react to the server-side username-change allowance");
  assert.equal(sessionStore.includes("suspend fun updateAccount("), true, "SessionStore should expose an account-update flow");
  assert.equal(mobileApi.includes("suspend fun updateAccount("), true, "Mobile API client should call the PATCH /auth/me update route");
  assert.equal(mobileApi.includes("canChangeUsername"), true, "Mobile auth user parsing should retain the username-change capability flag");
});

test("android account flow shows visible success feedback after saving", () => {
  assert.equal(sessionStore.includes("var accountSuccessMessage by mutableStateOf<String?>(null)"), true, "SessionStore should keep a visible success state for account updates");
  assert.equal(sessionStore.includes('accountSuccessMessage = if (result.user.isGuest) "Gastkonto erfolgreich umgewandelt." else "Kontodaten gespeichert."'), true, "Successful account updates should set a user-facing success message");
  assert.equal(source.includes("sessionStore.accountSuccessMessage?.let"), true, "Profile screen should render the shared success feedback");
  assert.equal(source.includes("Zuletzt erfolgreich gespeichert."), true, "Profile screen should keep a quiet saved-state hint near the CTA");
});

test("android random screen uses the new swipe-forward helper copy", () => {
  assert.equal(source.includes("Zieh dir einen zufälligen Witz und swipe zum Nächsten."), true, "Random screen should show the requested new subtitle");
});

test("android random undo walks the local stack back instead of only resetting one stale index", () => {
  assert.equal(source.includes("var undoStack by rememberSaveable"), true, "Random screen should keep a stack of previous random positions");
  assert.equal(source.includes("fun advanceRandomStack()"), true, "Random screen should advance through one helper so swipe/buttons keep undo state in sync");
  assert.equal(source.includes("undoStack = (undoStack + currentIndex).takeLast(8)"), true, "Random advance should push the current index onto the undo stack");
  assert.equal(source.includes("val previous = undoStack.last()"), true, "Undo should restore the most recent stack entry");
  assert.equal(source.includes("undoStack = undoStack.dropLast(1)"), true, "Undo should pop the restored stack entry after going back");
});

test("android routes usernames into profile navigation from cards comments and leaderboard", () => {
  assert.equal(source.includes("var selectedProfileUsername by rememberSaveable"), true, "App shell should track the selected profile username");
  assert.equal(source.includes("onOpenProfile = { selectedProfileUsername = it }"), true, "App shell should wire profile navigation callbacks into child screens");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(joke.authorUsername) })'), true, "Joke cards should open a profile when the author name is tapped");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(comment.author) })'), true, "Detail comments should open a profile when the author name is tapped");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(name) })'), true, "Leaderboard rows should open a profile when the username is tapped");
});

test("android detail flow can block a user and surface blocked-user handling", () => {
  assert.equal(source.includes("var blockedAuthors by rememberSaveable"), true, "App shell should persist blocked authors");
  assert.equal(source.includes('ComicAction("User blockieren"'), true, "DetailScreen should expose a block-user action");
  assert.equal(sessionStore.includes('blockMessage = "blocked_user:${result.blockedUserId}"'), true, "Blocking should surface a blocked_user status message for parity with iOS/web handling");
  assert.equal(source.includes("filterNot { blockedAuthors.contains(it.authorId) || blockedAuthors.contains(it.authorUsername) }"), true, "Feed/random data should filter blocked authors out after blocking");
});
