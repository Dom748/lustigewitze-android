import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const mainActivity = readFileSync(resolve(process.cwd(), 'app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt'), 'utf8');

test('Android composer adds Lange Witze plus auto-selection and 600-char error handling', () => {
  assert.match(mainActivity, /"Lange Witze"/);
  assert.match(mainActivity, /text\.trim\(\)\.length > 500/);
  assert.match(mainActivity, /selectedCategory = "Lange Witze"/);
  assert.match(mainActivity, /Witze über 600 Zeichen brauchen die Kategorie Lange Witze\./);
});
