import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';

const root = process.cwd();
const mainActivity = readFileSync(resolve(root, 'app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt'), 'utf8');
const mobileApi = readFileSync(resolve(root, 'app/src/main/java/studio/broapp/lustigewitze/MobileApi.kt'), 'utf8');
const manifest = readFileSync(resolve(root, 'app/src/main/AndroidManifest.xml'), 'utf8');

test('mobile api requests run off the main thread so feed loading does not fail on app start', () => {
  assert.match(mobileApi, /withContext\(Dispatchers\.IO\)/, 'Mobile API client should move HttpURLConnection work onto Dispatchers.IO');
  assert.match(mobileApi, /import kotlinx\.coroutines\.Dispatchers/, 'Mobile API client should import Dispatchers');
  assert.match(mobileApi, /import kotlinx\.coroutines\.withContext/, 'Mobile API client should import withContext');
});

test('android feed styling uses a more stitched sheet and horizontal category scroller', () => {
  assert.match(mainActivity, /horizontalScroll\(rememberScrollState\(\)\)/, 'Feed filters should scroll horizontally instead of wrapping into cramped rows');
  assert.match(mainActivity, /Brush\.verticalGradient/, 'Android shell should use a stronger stitched gradient background');
  assert.match(mainActivity, /RoundedCornerShape\(24\.dp\)/, 'Core comic cards should use softer stitched corners');
  assert.match(mainActivity, /Pill\("Feed Filter", Comic\.Yellow\)/, 'Feed filter card should lead with a stitched filter badge instead of a plain text block');
  assert.doesNotMatch(mainActivity, /Kategorien laufen jetzt horizontal, damit der Feed frei scrollt und das Stitch-Layout sauber bleibt\./, 'Feed filter card should drop the long helper paragraph in favor of a tighter stitched layout');
});

test('android shell uses stitched bottom navigation and comic floating actions', () => {
  assert.match(mainActivity, /Surface\(\s*color = if \(darkMode\) Comic\.DarkPaper else Comic\.Cream,[\s\S]*shape = RoundedCornerShape\(topStart = 28\.dp, topEnd = 28\.dp\),[\s\S]*border = BorderStroke\(3\.dp, Comic\.Ink\)/, 'Bottom navigation should sit inside a stitched shell');
  assert.match(mainActivity, /NavigationBarItemDefaults\.colors\(/, 'Bottom navigation items should define comic selected/unselected colors');
  assert.match(mainActivity, /private fun FloatingComicButton\(/, 'App shell should use a custom comic floating action button helper');
  assert.match(mainActivity, /shape = RoundedCornerShape\(18\.dp\)/, 'Comic floating action buttons should use rounded-rectangle corners instead of default circles');
  assert.match(mainActivity, /modifier = Modifier\.size\(58\.dp\)/, 'Comic floating action buttons should use a larger tap target inside the stitched shell');
});

test('android cards typography profile and detail surfaces move closer to stitch polish', () => {
  assert.match(mainActivity, /Text\(\s*visibleContent,[\s\S]*fontSize = 24\.sp,[\s\S]*lineHeight = 32\.sp/, 'Joke cards should upgrade body typography for a more premium stitched reading rhythm');
  assert.match(mainActivity, /Die besten Witze der Community\./, 'Feed header should adopt the more editorial iOS-style subtitle');
  assert.match(mainActivity, /Pill\(if \(selectedSort == "latest"\) "Neu zuerst" else "Top zuerst", Comic\.Pink\)/, 'Feed filter card should surface the active sort state in an editorial badge');
  assert.match(mainActivity, /Wie auf iOS: oben nur die wichtigsten Filter, direkt darunter die Kategorie-Leiste zum schnellen Durchscrollen\./, 'Feed filter card should explain the tighter iOS-like structure');
  assert.match(mainActivity, /RandomQueueCard\(currentIndex = currentIndex, total = jokes\.size, undoAvailable = undoStack\.isNotEmpty\(\)\)/, 'Random screen should show a dedicated deck/status card above the main joke card');
  assert.match(mainActivity, /verticalScroll\(rememberScrollState\(\)\)/, 'Random screen should allow vertical scrolling when the active joke and comments exceed the viewport height');
  assert.match(mainActivity, /RandomUndoButton\(/, 'Random screen should use a dedicated stitched undo control directly under the card');
  assert.match(mainActivity, /private fun JokeMetaStrip\(authorUsername: String, favoriteCount: Int, onOpenProfile: \(String\) -> Unit, modifier: Modifier = Modifier\)/, 'Joke cards should expose a reusable editorial author/meta strip');
  assert.match(mainActivity, /Pill\("\$favoriteCount Merker", Comic\.BlueSoft\)/, 'Joke cards should surface save count in the new meta strip');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.ArrowBack/, 'Detail back action should use the auto-mirrored back icon');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.Login/, 'Login actions should use the auto-mirrored login icon');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.List/, 'Feed tab should use the auto-mirrored list icon');
  assert.match(mainActivity, /ProfileHeroCard\(resolvedProfile = resolvedProfile, isOwnProfile = isOwnProfile\)/, 'Profile should render a dedicated editorial hero card');
  assert.match(mainActivity, /ScreenHeader\(title = "Rangliste", subtitle = "Top Witze und Top User direkt im stitched Feed-Look\.", badge = "Top"\)/, 'Leaderboard header should mirror the cross-platform stitched subtitle');
  assert.match(mainActivity, /Pill\("Live Ranking", Comic\.Pink\)/, 'Leaderboard rows should expose a compact live-ranking utility chip');
  assert.equal(mainActivity.includes("Wie auf iOS: oben nur die wichtigsten Modi, darunter direkt die stärksten Creator ohne Tabellen-Look."), true, 'Leaderboard should explain the tighter iOS-like ranking structure');
  assert.match(mainActivity, /ProfileStatCard\("Lieblingskategorie", resolvedProfile\.favoriteCategory, Comic\.YellowSoft, Modifier\.weight\(1f\)\)/, 'Profile stats should sit in a two-column stitched row');
  assert.match(mainActivity, /private fun CommentThreadPanel\(\s*comments: List<MobileComment>,\s*isLoading: Boolean,\s*errorMessage: String\?,\s*onOpenProfile: \(String\) -> Unit\s*\)/, 'Detail comments should move into a dedicated thread panel helper');
  assert.match(mainActivity, /private fun CommentComposerCard\(sessionStore: SessionStore, jokeId: String, onAuthRequired: \(\) -> Unit\)/, 'Detail composer should move into its own cleaner card');
  assert.match(mainActivity, /Text\("\$\{comments\.size\} Einträge", color = Comic\.Muted, fontWeight = FontWeight\.SemiBold\)/, 'Comment panel should summarize the visible thread size');
  assert.match(mainActivity, /Zum Schreiben brauchst du einen Account — lesen bleibt ohne Login offen\./, 'Comment composer card should explain the cleaner login gate');
  assert.match(mainActivity, /StatusPanel\(\"Random bereinigt\"/, 'Random screen should use a cleaner stitched status panel title');
  assert.match(mainActivity, /SafetyPanel\(/, 'Detail screen should move report and block controls into a dedicated safety panel');
  assert.match(mainActivity, /Text\("Zurück", color = Comic\.Ink, fontWeight = FontWeight\.Black\)/, 'Detail screen should wrap the back action into a compact stitched pill');
  assert.match(mainActivity, /Pill\("Aus Feed \+ Random versteckt", Comic\.BlueSoft\)/, 'Blocked-users rows should explain the cross-surface effect in a compact badge');
  assert.match(mainActivity, /Text\("Profil-Stats", fontWeight = FontWeight\.Black, fontSize = 20\.sp\)/, 'Profile should group key stats into a calmer stats card under the hero');
  assert.doesNotMatch(mainActivity, /"Navigation"/, 'Bottom navigation should not show the extra Navigation label above the tray');
  assert.doesNotMatch(mainActivity, /letterSpacing = 0\.6\.sp/, 'Bottom navigation label styling should disappear with the removed tray heading');
  assert.match(mainActivity, /private fun ScreenHeader\(title: String, subtitle: String, badge: String\) \{[\s\S]*Row\(verticalAlignment = Alignment\.Top\)[\s\S]*Text\(\s*title,[\s\S]*maxLines = 2,[\s\S]*overflow = TextOverflow\.Ellipsis[\s\S]*Text\(\s*subtitle,[\s\S]*maxLines = 2,[\s\S]*overflow = TextOverflow\.Ellipsis[\s\S]*Box\(modifier = Modifier\.padding\(top = 2\.dp\)\) \{[\s\S]*Pill\(badge, Comic\.Yellow\)/, 'Screen headers should keep badge top-aligned and allow two-line titles/subtitles');
  assert.match(mainActivity, /ReactionTile\([\s\S]*"Top",[\s\S]*Modifier\.width\(72\.dp\),[\s\S]*compactHorizontalPadding = 3\.dp,[\s\S]*compactVerticalPadding = 2\.dp[\s\S]*ReactionTile\([\s\S]*"Runter",[\s\S]*Modifier\.width\(72\.dp\),[\s\S]*compactHorizontalPadding = 3\.dp,[\s\S]*compactVerticalPadding = 2\.dp[\s\S]*ReactionTile\([\s\S]*"Superlike",[\s\S]*Modifier\.width\(72\.dp\),[\s\S]*compactHorizontalPadding = 3\.dp,[\s\S]*compactVerticalPadding = 2\.dp/, 'Android compact vote buttons should stay visibly broader than the default action tile');
  assert.match(mainActivity, /private fun ScoreBadge\(score: Int, modifier: Modifier = Modifier\) \{[\s\S]*RoundedCornerShape\(18\.dp\)/, 'Score badges should use a rounded stitched badge instead of a plain circle');
  assert.match(mainActivity, /ScoreBadge\([\s\S]*score = joke\.score,[\s\S]*\.align\(Alignment\.TopEnd\)[\s\S]*\.padding\(top = \(-6\)\.dp, end = \(-8\)\.dp\)/, 'Feed cards should float the score badge over the top-right corner like iOS');
  assert.match(mainActivity, /private fun ReactionTile\([\s\S]*Surface\([\s\S]*BorderStroke\(2\.dp, Comic\.Ink\)/, 'Reaction tiles should render as bordered stitched controls');
});

test('android manifest wires a first-party launcher icon resource', () => {
  assert.match(manifest, /android:icon="@mipmap\/ic_launcher"/, 'App should declare its own launcher icon');
  assert.match(manifest, /android:roundIcon="@mipmap\/ic_launcher_round"/, 'App should declare its own round launcher icon');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml')), true, 'Adaptive launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml')), true, 'Adaptive round launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground.xml')), true, 'Launcher foreground drawable should exist');
});
