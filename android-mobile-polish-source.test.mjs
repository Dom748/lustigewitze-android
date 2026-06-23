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
  assert.match(mainActivity, /ProfileStatCard\("Lieblingskategorie", resolvedProfile\.favoriteCategory, Comic\.YellowSoft\)/, 'Profile should use dedicated stitched stat cards');
  assert.match(mainActivity, /ProfileStatCard\("Ø Score \/ Joke", resolvedProfile\.averageScore\.toString\(\), Comic\.BlueSoft\)/, 'Profile stats should be visually grouped instead of isolated plain cards');
  assert.match(mainActivity, /StatusPanel\(\s*title = "Random Flow"/, 'Random screen should use a cleaner stitched status panel title');
  assert.match(mainActivity, /SafetyPanel\(/, 'Detail screen should move report and block controls into a dedicated safety panel');
  assert.match(mainActivity, /private fun ScreenHeader\(title: String, subtitle: String, badge: String\) \{[\s\S]*Surface\([\s\S]*color = Comic\.YellowSoft/, 'Screen headers should render inside a stitched hero surface');
  assert.match(mainActivity, /private fun ScoreBadge\(score: Int\) \{[\s\S]*RoundedCornerShape\(18\.dp\)/, 'Score badges should use a rounded stitched badge instead of a plain circle');
  assert.match(mainActivity, /private fun ReactionTile\([\s\S]*Surface\([\s\S]*BorderStroke\(2\.dp, Comic\.Ink\)/, 'Reaction tiles should render as bordered stitched controls');
});

test('android manifest wires a first-party launcher icon resource', () => {
  assert.match(manifest, /android:icon="@mipmap\/ic_launcher"/, 'App should declare its own launcher icon');
  assert.match(manifest, /android:roundIcon="@mipmap\/ic_launcher_round"/, 'App should declare its own round launcher icon');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml')), true, 'Adaptive launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml')), true, 'Adaptive round launcher icon resource should exist');
  assert.equal(existsSync(resolve(root, 'app/src/main/res/drawable/ic_launcher_foreground.xml')), true, 'Launcher foreground drawable should exist');
});
