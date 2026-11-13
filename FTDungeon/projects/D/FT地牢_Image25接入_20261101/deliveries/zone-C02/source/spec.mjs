export const specification = {
  id: 'C02',
  name: '淹水拐弯·苔石蓄水柱厅',
  size: [19, 11, 19],
  entrance: [9, 2, 0],
  interior: [9, 2, 9],
  exit: [18, 2, 9],
  facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C02-corners-flooded-corner-bend.png',
  reference_sha256: '4b61ed5b869a18e1ca6c8ccd51de3972b17b7a4e2998d20b2a8429f5420f12d8',
  pillars: [[4, 5], [14, 5], [4, 14], [14, 14]],
  hanging_lights: [[4, 9], [14, 9], [9, 4], [9, 14]],
  barrels: [[3, 2, 16], [5, 2, 16]],
}

export function onRoute(blockX, blockZ) {
  return (blockX >= 8 && blockX <= 10 && blockZ <= 10)
    || (blockX >= 8 && blockZ >= 8 && blockZ <= 10)
}

export function onGuard(blockX, blockZ) {
  if (onRoute(blockX, blockZ) || blockX <= 0 || blockX >= 18 || blockZ <= 0 || blockZ >= 18) return false
  for (const deltaX of [-1, 0, 1]) {
    for (const deltaZ of [-1, 0, 1]) {
      if (onRoute(blockX + deltaX, blockZ + deltaZ)) return true
    }
  }
  return false
}
