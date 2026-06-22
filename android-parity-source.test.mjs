import * as assert from "node:assert/strict";
import { test } from "node:test";
import { readFileSync } from "node:fs";
import path from "node:path";

const source = readFileSync(
  path.join(process.cwd(), "app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt"),
  "utf8",
);

test("android auth sheet gates login and registration behind terms acceptance", () => {
  assert.equal(source.includes("var acceptedTerms by rememberSaveable"), true, "AuthSheet should track accepted terms state");
  assert.equal(source.includes("Nutzungsbedingungen / EULA"), true, "AuthSheet should show a visible terms / EULA block");
  assert.equal(source.includes("Checkbox(checked = acceptedTerms"), true, "AuthSheet should require an explicit checkbox");
  assert.equal(source.includes("enabled = acceptedTerms"), true, "Auth submit should stay disabled until terms are accepted");
});

test("android profile screen exposes real profile stats and direct account deletion", () => {
  assert.equal(source.includes("Gastmodus"), false, "ProfileScreen should no longer be a guest-mode placeholder only");
  assert.equal(source.includes("Offene API-Abhaengigkeit"), false, "ProfileScreen should no longer advertise missing profile API wiring as the main state");
  assert.equal(source.includes("Lieblingskategorie"), true, "ProfileScreen should surface profile stats like iOS");
  assert.equal(source.includes("Ø Score / Joke"), true, "ProfileScreen should surface average score stats like iOS");
  assert.equal(source.includes("Konto löschen"), true, "ProfileScreen should expose direct account deletion");
  assert.equal(source.includes("Konto wirklich löschen?"), true, "Profile deletion should warn before destructive action");
});

test("android routes usernames into profile navigation from cards comments and leaderboard", () => {
  assert.equal(source.includes("var selectedProfileUsername by rememberSaveable"), true, "App shell should track the selected profile username");
  assert.equal(source.includes("onOpenProfile = { selectedProfileUsername = it }"), true, "App shell should wire profile navigation callbacks into child screens");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(joke.author) })'), true, "Joke cards should open a profile when the author name is tapped");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(comment.author) })'), true, "Detail comments should open a profile when the author name is tapped");
  assert.equal(source.includes('TextButton(onClick = { onOpenProfile(name) })'), true, "Leaderboard rows should open a profile when the username is tapped");
});

test("android detail flow can block a user and surface blocked-user handling", () => {
  assert.equal(source.includes("var blockedAuthors by rememberSaveable"), true, "App shell should persist blocked authors");
  assert.equal(source.includes('ComicAction("User blockieren"'), true, "DetailScreen should expose a block-user action");
  assert.equal(source.includes('blockedUserMessage = "blocked_user:'), true, "Blocking should surface a blocked_user status message for parity with iOS/web handling");
  assert.equal(source.includes("filterNot { blockedAuthors.contains(it.author) }"), true, "Feed/random data should filter blocked authors out after blocking");
});
