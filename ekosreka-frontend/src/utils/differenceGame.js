export function findSpot(spots, x, y, width, height) {
  if (width <= 0 || height <= 0) return -1;
  return spots.findIndex(spot => Math.hypot(x - spot.x, (y - spot.y) * height / width) <= spot.radius);
}

export function parseSpots(value) {
  try {
    const spots = JSON.parse(value || '[]');
    return Array.isArray(spots) ? spots.filter(spot => spot && ['x', 'y', 'radius'].every(key => Number.isFinite(spot[key])) && spot.x >= 0 && spot.x <= 1 && spot.y >= 0 && spot.y <= 1 && spot.radius >= .005 && spot.radius <= .2) : [];
  } catch { return []; }
}
