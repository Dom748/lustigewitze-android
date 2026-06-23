import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const mainActivity = readFileSync(resolve(process.cwd(), 'app/src/main/java/studio/broapp/lustigewitze/MainActivity.kt'), 'utf8');

test('feed screen keeps vertical scrolling free and only uses horizontal swipe voting', () => {
  assert.match(mainActivity, /FeedScreen\(/);
  assert.match(mainActivity, /FeedSwipeCard/);
  assert.doesNotMatch(mainActivity, /var dragY by remember/);
  assert.match(mainActivity, /detectHorizontalDragGestures/);
  assert.doesNotMatch(mainActivity, /dragY < -120f/);
  assert.match(mainActivity, /val voteValue = if \(dragX > 120f\) 1 else if \(dragX < -120f\) -1 else null/);
  assert.match(mainActivity, /Like registriert/);
  assert.match(mainActivity, /Dislike registriert/);
  assert.doesNotMatch(mainActivity, /Stern registriert/);
  assert.doesNotMatch(mainActivity, /Swipe, Buttons, Undo und Haptik-Aequivalent sind als MVP-State vorhanden\./);
  assert.match(mainActivity, /showTitle = false/);
  assert.match(mainActivity, /Modifier\.size\(if \(showTitle\) 24\.dp else 32\.dp\)/);
  assert.match(mainActivity, /else "Merken"/);
  assert.match(mainActivity, /SwipeVoteBadge/);
  assert.match(mainActivity, /label = "TOP"/);
  assert.match(mainActivity, /Icons\.Filled\.ThumbUp/);
  assert.match(mainActivity, /label = "RUNTER"/);
  assert.match(mainActivity, /Icons\.Filled\.ThumbDown/);
  assert.match(mainActivity, /dragX > 24f/);
  assert.match(mainActivity, /dragX < -24f/);
});
