import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const mainActivity = readFileSync(resolve(process.cwd(), 'app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt'), 'utf8');

test('Android composer adds Lange Witze plus auto-selection and 420/2500-char handling', () => {
  assert.match(mainActivity, /"Lange Witze"/);
  assert.match(mainActivity, /AUTO_LONG_JOKE_THRESHOLD = 420/);
  assert.match(mainActivity, /text\.trim\(\)\.length > AUTO_LONG_JOKE_THRESHOLD/);
  assert.match(mainActivity, /selectedCategory = "Lange Witze"/);
  assert.match(mainActivity, /LONG_JOKE_MAX_CHARS = 2500/);
  assert.match(mainActivity, /Witze über 420 Zeichen werden automatisch als Lange Witze gespeichert\./);
});
