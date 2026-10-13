import test from 'node:test';
import assert from 'node:assert/strict';
import { findSpot, parseSpots } from './differenceGame.js';

const spots = [{ x: .5, y: .5, radius: .05 }];
test('hit detection follows circular regions on wide images at any display size', () => {
  assert.equal(findSpot(spots, .5, .58, 1000, 500), 0);
  assert.equal(findSpot(spots, .5, .58, 400, 200), 0);
  assert.equal(findSpot(spots, .5, .62, 1000, 500), -1);
  assert.equal(findSpot(spots, .56, .5, 1000, 500), -1);
  assert.equal(findSpot(spots, .5, .53, 500, 1000), -1);
});
test('missing or invalid stored spots cannot create a completed game', () => {
  for (const value of [null, 'broken', '{}', '[null]', '[{"x":2,"y":0.5,"radius":0.05}]']) {
    assert.deepEqual(parseSpots(value), []);
  }
  assert.deepEqual(parseSpots(JSON.stringify(spots)), spots);
  assert.equal(findSpot([], .5, .5, 100, 100), -1);
});
