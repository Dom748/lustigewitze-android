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

test('mobile api tolerates missing nested authors so the Android app does not crash on sparse production payloads', () => {
  assert.match(mobileApi, /data class MobileJokeAuthor\([\s\S]*val id: String\? = null,[\s\S]*val username: String\? = null/, 'Joke author DTO should allow missing id/username values');
  assert.match(mobileApi, /data class MobileCommentAuthor\([\s\S]*val id: String\? = null,[\s\S]*val username: String\? = null/, 'Comment author DTO should allow missing id/username values');
  assert.match(mobileApi, /val author: MobileJokeAuthor\? = null/, 'Mobile jokes should allow a missing nested author object');
  assert.match(mobileApi, /val author: MobileCommentAuthor\? = null/, 'Mobile comments should allow a missing nested author object');
  assert.match(mobileApi, /val author = item\.optJSONObject\("author"\)/, 'Joke parsing should read authors with optJSONObject instead of crashing on missing objects');
  assert.match(mobileApi, /val author = json\.optJSONObject\("author"\)/, 'Comment parsing should read authors with optJSONObject instead of crashing on missing objects');
  assert.match(mainActivity, /val resolvedAuthorUsername = author\?\.username\?\.takeIf\(String::isNotBlank\) \?: "unbekannt"/, 'Joke mapping should fall back to a safe author username');
});

test('android feed matches the compact iOS Stitch filter board', () => {
  assert.match(mainActivity, /Brush\.verticalGradient/, 'Android shell should keep the warm stitched background');
  assert.match(mainActivity, /private fun FeedFilterBoard\(/, 'Feed filters should use a dedicated iOS-like filter board');
  assert.match(mainActivity, /Segment\("Neu", selected = selectedSort == "latest", modifier = Modifier\.weight\(1f\)/, 'Neu and Top should fill the first filter row evenly');
  assert.match(mainActivity, /Segment\("Top", selected = selectedSort == "top", modifier = Modifier\.weight\(1f\)/, 'Top should mirror the iOS segmented row');
  assert.match(mainActivity, /Text\("Alle Kategorien"/, 'Category row should use the iOS label');
  assert.match(mainActivity, /DropdownMenu\(/, 'Categories should open from one compact dropdown instead of a long chip rail');
  assert.doesNotMatch(mainActivity, /Segment\("Reload"/, 'Reload must not occupy a third primary filter segment');
  assert.doesNotMatch(mainActivity, /Text\(\s*"Kategorien ↔"/, 'Legacy horizontal-scroll helper must disappear');
  assert.doesNotMatch(mainActivity, /CompactSegment\(option\.label/, 'The old category chip rail must disappear');
});

test('android shell mirrors the iOS four-tab tray and header actions', () => {
  assert.match(mainActivity, /shape = RoundedCornerShape\(30\.dp\)/, 'Bottom navigation should use the rounded iOS tray shape');
  assert.match(mainActivity, /NavigationBar\([\s\S]*modifier = Modifier\.height\(64\.dp\)/, 'Bottom navigation should stay compact like iOS');
  assert.match(mainActivity, /NavigationBarItemDefaults\.colors\(/, 'Bottom navigation items should define comic selected/unselected colors');
  assert.doesNotMatch(mainActivity, /if \(tab == Tab\.Random\)/, 'Create must not appear as a fifth bottom navigation item');
  assert.match(mainActivity, /private fun FeedHeader\([\s\S]*onToggleTheme: \(\) -> Unit,[\s\S]*onCreateJoke: \(\) -> Unit/, 'Feed header should own theme and create actions like iOS');
  assert.match(mainActivity, /HeaderCircleAction\([\s\S]*Icons\.Filled\.LightMode/, 'Feed header should expose the circular theme action');
  assert.match(mainActivity, /HeaderCircleAction\([\s\S]*Icons\.Filled\.Add/, 'Feed header should expose the circular create action');
  assert.match(mainActivity, /private fun ProfileScreen\([\s\S]*darkMode: Boolean,[\s\S]*onToggleTheme: \(\) -> Unit/, 'Profile must retain theme settings access');
  assert.doesNotMatch(mainActivity, /floatingActionButton\s*=/, 'Global actions must not float over jokes or comment controls');
});

test('dark mode keeps ink text readable on light comic cards and inactive navigation visible', () => {
  assert.match(mainActivity, /import androidx\.compose\.material3\.LocalContentColor/, 'Comic cards should be able to override Material dark-mode content colors');
  assert.match(mainActivity, /import androidx\.compose\.runtime\.CompositionLocalProvider/, 'Comic cards should provide an explicit local ink color');
  assert.match(mainActivity, /private fun ComicCard\([\s\S]*CompositionLocalProvider\(LocalContentColor provides Comic\.Ink\)/, 'Generic light comic cards must keep dark ink text in dark mode');
  assert.match(mainActivity, /private fun ScreenHeader\([\s\S]*color = Comic\.Paper,[\s\S]*contentColor = Comic\.Ink/, 'Light screen headers must keep dark ink text in dark mode');
  assert.match(mainActivity, /private fun LeaderboardHeaderCard\([\s\S]*color = Comic\.Paper,[\s\S]*contentColor = Comic\.Ink/, 'Leaderboard header must keep dark ink text in dark mode');
  assert.match(mainActivity, /private fun LeaderboardUserRowCard\([\s\S]*color = if \(highlighted\) Comic\.YellowSoft else Comic\.Paper,[\s\S]*contentColor = Comic\.Ink/, 'Leaderboard rows must keep usernames and ranks readable in dark mode');
  assert.match(mainActivity, /unselectedIconColor = if \(darkMode\) Comic\.Cream\.copy\(alpha = 0\.78f\) else Comic\.Ink/, 'Inactive dark-mode navigation icons should use a light visible tint');
  assert.match(mainActivity, /unselectedTextColor = if \(darkMode\) Comic\.Cream\.copy\(alpha = 0\.78f\) else Comic\.Ink/, 'Inactive dark-mode navigation labels should use a light visible tint');
});

test('guest profile stays readable and compact in dark mode', () => {
  assert.match(mainActivity, /selectedTextColor = if \(darkMode\) Comic\.Yellow else Comic\.Ink/, 'Selected navigation labels need a bright dark-mode color');
  assert.match(mainActivity, /Feed bleibt ohne Login offen\. Dein Profil startet nach dem Einloggen\./, 'Guest profile subtitle should fit without ellipsis');
  assert.doesNotMatch(mainActivity, /Ohne Login bleibt dein Feed offen, aber dein Account-Bereich startet erst nach dem Einloggen\./, 'Overlong guest subtitle must not return');
  assert.match(mainActivity, /PrimaryButton\("Anmelden \/ Registrieren"/, 'Guest CTA should use consistent German wording');
  assert.doesNotMatch(mainActivity, /PrimaryButton\("Login \/ Register"/, 'Denglish guest CTA must not return');
  assert.match(mainActivity, /if \(darkMode\) "Dark Mode aktiv · Wechsel zu Light" else "Light Mode aktiv · Wechsel zu Dark"/, 'Theme card should explain the icon action');
  assert.match(mainActivity, /contentDescription = if \(darkMode\) "Zum Light Mode wechseln" else "Zum Dark Mode wechseln"/, 'Theme icon needs an explicit dynamic accessibility action');
  assert.match(mainActivity, /Spacer\(Modifier\.weight\(1f\)\)\s*PrimaryButton\("Anmelden \/ Registrieren"/, 'Guest CTA should absorb leftover height above itself instead of leaving dead space below');
});

test('android cards typography profile and detail surfaces move closer to stitch polish', () => {
  assert.match(mainActivity, /Text\(\s*visibleContent,[\s\S]*fontSize = 24\.sp,[\s\S]*lineHeight = 32\.sp/, 'Joke cards should keep the bold iOS reading rhythm');
  assert.match(mainActivity, /Die besten Witze der Community\./, 'Feed header subtitle should match the iOS reference exactly');
  assert.match(mainActivity, /private fun StitchedJokeSurface\(/, 'Joke cards should use a dedicated double-border stitched surface');
  assert.match(mainActivity, /JokeShareButton\(/, 'Feed cards should expose the circular iOS-like share action');
  assert.match(mainActivity, /ScoreBadge\(score = joke\.score,[\s\S]*Modifier\.align\(Alignment\.TopEnd\)/, 'Score should float over the upper card corner like iOS');
  assert.match(mainActivity, /color = Comic\.Yellow,[\s\S]*Text\("Melden"/, 'Report action should use the yellow iOS pill instead of pink');
  assert.match(mainActivity, /Icons\.Filled\.Favorite,[\s\S]*showTitle = true/, 'The save action should retain the visible Merken label like iOS');
  assert.doesNotMatch(mainActivity, /Pill\(if \(selectedSort == "latest"\) "Neu zuerst" else "Top zuerst", Comic\.Pink\)/, 'Feed filter should avoid duplicating the active sort state above the sort controls');
  assert.doesNotMatch(mainActivity, /Wie auf iOS: oben nur die wichtigsten Filter, direkt darunter die Kategorie-Leiste zum schnellen Durchscrollen\./, 'Feed filter should remove explanatory copy and start content sooner');
  assert.match(mainActivity, /CompactRandomStatusRow\(currentIndex = currentIndex, total = jokes\.size\)/, 'Random screen should show compact deck/status pills above the main joke card');
  assert.doesNotMatch(mainActivity, /private fun RandomQueueCard\(/, 'Random deck status must not use a bulky full-width card');
  assert.doesNotMatch(mainActivity, /Pill\(if \(undoAvailable\) "Undo"/, 'Undo must only appear in the dedicated button beneath the joke card');
  assert.match(mainActivity, /verticalScroll\(rememberScrollState\(\)\)/, 'Random screen should allow vertical scrolling when the active joke and comments exceed the viewport height');
  assert.match(mainActivity, /\.padding\(horizontal = 20\.dp\)[\s\S]*\.padding\(top = 6\.dp, bottom = 40\.dp\)/, 'Random content should reserve safe horizontal room and bottom clearance above navigation');
  assert.match(mainActivity, /\.rotate\(dragX \/ 80f\)/, 'Random swipe rotation should stay subtle enough to avoid edge clipping');
  assert.match(mainActivity, /RandomUndoButton\(/, 'Random screen should use a dedicated stitched undo control directly under the card');
  assert.match(mainActivity, /private fun JokeMetaStrip\(authorUsername: String, onOpenProfile: \(String\) -> Unit, onReport: \(\) -> Unit, modifier: Modifier = Modifier\)/, 'Joke cards should expose a reusable author/report strip');
  assert.match(mainActivity, /Icon\(Icons\.Filled\.Flag, contentDescription = "Melden"[\s\S]*Text\("Melden"/, 'The old Merker count position should contain the report action');
  assert.doesNotMatch(mainActivity, /Pill\("\$favoriteCount Merker"/, 'The unused Merker count must not return');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.ArrowBack/, 'Detail back action should use the auto-mirrored back icon');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.Login/, 'Login actions should use the auto-mirrored login icon');
  assert.match(mainActivity, /Icons\.AutoMirrored\.Filled\.List/, 'Feed tab should use the auto-mirrored list icon');
  assert.match(mainActivity, /ProfileHeroCard\(resolvedProfile = resolvedProfile, isOwnProfile = isOwnProfile\)/, 'Profile should render a dedicated editorial hero card');
  assert.match(mainActivity, /private fun LeaderboardHeaderCard\(modifier: Modifier = Modifier\)/, 'Leaderboard should use a dedicated flatter header card instead of the generic stitched hero');
  assert.match(mainActivity, /Text\("Top Witze und Top User", color = Comic\.Muted, fontWeight = FontWeight\.SemiBold/, 'Leaderboard header should match the short iOS subtitle');
  assert.equal(mainActivity.includes('private fun LeaderboardFilterCard('), true, 'Leaderboard filters should use a dedicated compact filter card');
  assert.equal(mainActivity.includes('LeaderboardSegment("User", selected = selectedMode == "User", icon = Icons.Filled.People'), true, 'Leaderboard filters should use the iOS-like two-row compact filter card');
  assert.equal(mainActivity.includes('LeaderboardSegment("Alle", selected = selectedPeriod == "Alle"'), true, 'Leaderboard period filters should include Alle');
  assert.equal(mainActivity.includes('LeaderboardSegment("Heute", selected = selectedPeriod == "Heute"'), true, 'Leaderboard period filters should include Heute');
  assert.equal(mainActivity.includes('LeaderboardSegment("Woche", selected = selectedPeriod == "Woche"'), true, 'Leaderboard period filters should include Woche');
  assert.equal(mainActivity.includes('LeaderboardSegment("Monat", selected = selectedPeriod == "Monat"'), true, 'Leaderboard period filters should include Monat');
  assert.match(mainActivity, /private fun LeaderboardUserRowCard\([\s\S]*Text\("Zum Profil", color = Comic\.Ink, fontWeight = FontWeight\.Black\)[\s\S]*LeaderboardScoreBadge\(score\)/, 'Leaderboard rows should use a compact profile pill and orange score badge like iOS');
  assert.doesNotMatch(mainActivity, /Live Ranking/, 'Leaderboard rows should drop the bulky Live Ranking chip for iOS parity');
  assert.doesNotMatch(mainActivity, /Top Witze und Top User direkt im stitched Feed-Look\./, 'Leaderboard should drop the longer Android-only subtitle copy');
  assert.doesNotMatch(mainActivity, /Wie auf iOS: oben nur die wichtigsten Modi, darunter direkt die stärksten Creator ohne Tabellen-Look\./, 'Leaderboard should drop the extra explainer paragraph and stay compact like iOS');
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
  assert.match(mainActivity, /private fun ScreenHeader\(title: String, subtitle: String, badge: String\) \{[\s\S]*\.padding\(horizontal = 14\.dp, vertical = 8\.dp\)[\s\S]*Row\(verticalAlignment = Alignment\.Top\)[\s\S]*Text\(\s*title,[\s\S]*fontSize = 26\.sp,[\s\S]*maxLines = 2,[\s\S]*overflow = TextOverflow\.Ellipsis[\s\S]*Text\(\s*subtitle,[\s\S]*maxLines = 2,[\s\S]*overflow = TextOverflow\.Ellipsis[\s\S]*Box\(modifier = Modifier\.padding\(top = 2\.dp\)\) \{[\s\S]*Pill\(badge, Comic\.Yellow\)/, 'Screen headers should stay compact, keep the badge top-aligned and allow two-line titles/subtitles');
  assert.match(mainActivity, /contentPadding = androidx\.compose\.foundation\.layout\.PaddingValues\(start = 18\.dp, top = 18\.dp, end = 18\.dp, bottom = 40\.dp\)/, 'Feed should keep extra scroll clearance above the fixed navigation');
  assert.match(mainActivity, /private val MOBILE_HEADER_TOP_INSET = 6\.dp/, 'Screen heroes should keep a compact visible gap below the system status area');
  assert.doesNotMatch(mainActivity, /Pill\("LW", Comic\.Yellow\)/, 'The removed utility banner must not leave an LW brand pill behind');
  assert.doesNotMatch(mainActivity, /Text\(\s*"LustigeWitze",/, 'The removed utility banner must not duplicate the Lustige Witze screen title');
  assert.match(mainActivity, /ReactionTile\([\s\S]*"Top",[\s\S]*Modifier\.weight\(1f\)[\s\S]*ReactionTile\([\s\S]*"Runter",[\s\S]*Modifier\.weight\(1f\)[\s\S]*ReactionTile\([\s\S]*"Superlike",[\s\S]*Modifier\.weight\(1f\)[\s\S]*ReactionTile\([\s\S]*Icons\.Filled\.Favorite,[\s\S]*Modifier\.weight\(1\.35f\),[\s\S]*showTitle = true/, 'The iOS-like action row should keep three icon buttons plus a wider labelled Merken button');
  assert.match(mainActivity, /private fun ScoreBadge\(score: Int, modifier: Modifier = Modifier\) \{[\s\S]*RoundedCornerShape\(999\.dp\)[\s\S]*color = Comic\.Orange/, 'Score badges should use the compact orange iOS badge treatment');
  assert.match(mainActivity, /StitchedJokeSurface\(onClick = onOpen, contentPadding = 12\.dp\)/, 'Joke cards should use the dedicated double-border Stitch surface');
  assert.match(mainActivity, /ScoreBadge\(score = joke\.score, modifier = Modifier\.align\(Alignment\.TopEnd\)\.offset/, 'Score badges should overlap the upper card corner like the iOS reference');
  assert.match(mainActivity, /Pill\("Kommentare \(\$\{comments\.size\}\)", Comic\.BlueSoft\)/, 'Random comments should use one compact count label');
  assert.equal(mainActivity.indexOf('PrimaryButton("Neuen Random-Witz laden"') < mainActivity.indexOf('RandomInlineCommentSection('), true, 'The next-joke CTA should stay above comments and visible earlier in the Random flow');
  assert.match(mainActivity, /if \(showComposer\) "Schließen" else "Kommentar"/, 'Random comment action should use a short label on narrow screens');
  assert.doesNotMatch(mainActivity, /\.padding\([^\n]*\(-\d+\)\.dp/, 'Compose padding must never receive negative values');
  assert.match(mainActivity, /private fun ReactionTile\([\s\S]*Surface\([\s\S]*BorderStroke\(2\.dp, Comic\.Ink\)/, 'Reaction tiles should render as bordered stitched controls');
});

test('joke cards use tighter iOS Stitch corner placement and vertical rhythm', () => {
  assert.match(mainActivity, /StitchedJokeSurface\(onClick = onOpen, contentPadding = 12\.dp\)/, 'Joke cards should use compact dedicated inner padding');
  assert.match(mainActivity, /Row\(verticalAlignment = Alignment\.Top\) \{\s*Pill\(joke\.category\.uppercase\(\), Comic\.Yellow\)/, 'Category should hug the upper left corner');
  assert.match(mainActivity, /ScoreBadge\(score = joke\.score, modifier = Modifier\.align\(Alignment\.TopEnd\)\.offset\(x = \(-6\)\.dp, y = \(-8\)\.dp\)\)/, 'Score should use the compact iOS overlap');
  assert.match(mainActivity, /visibleContent,[\s\S]*modifier = Modifier\.padding\(top = 12\.dp, end = 34\.dp\)/, 'Joke text should start close beneath the category row while clearing the share control');
  assert.match(mainActivity, /onReport = onReport,[\s\S]*modifier = Modifier\.padding\(top = 12\.dp\)/, 'Metadata should sit close beneath the joke text');
  assert.match(mainActivity, /\.fillMaxWidth\(\)\s*\.padding\(top = 12\.dp\)/, 'Reaction actions should use the iOS card rhythm');
  assert.match(mainActivity, /private fun ScoreBadge\([\s\S]*Modifier\.padding\(horizontal = 10\.dp, vertical = 6\.dp\)/, 'Score badge should stay compact in the top corner');
});

test('android manifest wires a first-party launcher icon resource', () => {
  assert.match(manifest, /android:icon="@mipmap\/ic_launcher"/, 'App should declare its own launcher icon');
  assert.match(manifest, /android:roundIcon="@mipmap\/ic_launcher_round"/, 'App should declare its own round launcher icon');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml')), true, 'Adaptive launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml')), true, 'Adaptive round launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground.xml')), true, 'Launcher foreground drawable should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground_inset.xml')), true, 'Launcher foreground inset drawable should exist for correct Android scaling');
  assert.match(readFileSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml'), 'utf8'), /@drawable\/ic_launcher_foreground_inset/, 'Adaptive launcher icon should route through the inset foreground wrapper');
  const foreground = readFileSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground.xml'), 'utf8');
  assert.match(foreground, /android:gravity="fill"/, 'Adaptive foreground must scale into its icon bounds instead of rendering the 1024px source at intrinsic size');
  assert.doesNotMatch(foreground, /android:gravity="center"/, 'Adaptive foreground must not center-crop the artwork down to an unrecognizable yellow/navy fragment');
  const foregroundInset = readFileSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground_inset.xml'), 'utf8');
  assert.match(foregroundInset, /android:insetLeft="4dp"[\s\S]*android:insetTop="4dp"[\s\S]*android:insetRight="4dp"[\s\S]*android:insetBottom="4dp"/, 'Adaptive artwork should retain a balanced safe-zone inset on every edge');
});
