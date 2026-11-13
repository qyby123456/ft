import fs from 'node:fs'
import path from 'node:path'

const rooms = [
  ['large', '晶核大殿', [19, 12, 19]], ['medium', '紫晶研究室', [15, 8, 15]],
  ['small', '晶簇密室', [9, 6, 9]], ['hall', '方解石柱廊大厅', [21, 10, 15]],
  ['corridor', '紫晶直廊', [9, 7, 21]], ['corner', '晶脉直角回廊', [15, 7, 15]],
  ['entrance', '晶洞入口前厅', [15, 9, 13]], ['courtyard', '露天天晶庭院', [19, 11, 19]],
  ['treasure', '晶核宝库祭坛', [15, 9, 17]],
]

export default function build(api) {
  const index = Number(process.env.S8_ROOM_INDEX ?? 1) - 1
  const [kind, title] = rooms[index]
  const authority = JSON.parse(fs.readFileSync(path.join(process.env.S8_METADATA_DIR, 'authority.json'), 'utf8'))[index]
  const size = authority.output_size
  const [width, height, depth] = size
  const center = Math.floor(width / 2)
  const structure = api.createStructure(size)
  const put = (blockX, blockY, blockZ, name, properties, nbt) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties, nbt)
  const fill = (first, last, name) => api.fill(structure, first, last, `minecraft:${name}`)
  const inside = (blockX, blockZ) => kind !== 'corner' || (blockX >= 4 && blockX <= 10) || (blockZ >= 8 && blockX >= 4)
  const boundary = (blockX, blockZ) => [[-1, 0], [1, 0], [0, -1], [0, 1]].some(([deltaX, deltaZ]) => blockX + deltaX < 0 || blockX + deltaX >= width || blockZ + deltaZ < 0 || blockZ + deltaZ >= depth || !inside(blockX + deltaX, blockZ + deltaZ))
  fill([0, 0, 0], [width - 1, height - 1, depth - 1], 'air')
  for (let blockX = 0; blockX < width; blockX++) for (let blockZ = 0; blockZ < depth; blockZ++) {
    if (!inside(blockX, blockZ)) continue
    const trim = blockX === 2 || blockX === width - 3 || blockZ === 2 || blockZ === depth - 3
    put(blockX, 0, blockZ, trim ? 'calcite' : (blockX + blockZ) % 7 === 0 ? 'amethyst_block' : 'deepslate_tiles')
    const openSky = kind === 'courtyard' && blockX >= 5 && blockX <= width - 6 && blockZ >= 5 && blockZ <= depth - 6
    if (!openSky) put(blockX, height - 1, blockZ, 'deepslate_tiles')
    if (boundary(blockX, blockZ)) for (let blockY = 1; blockY < height - 1; blockY++) put(blockX, blockY, blockZ, (blockX * 5 + blockZ * 3 + blockY) % 13 < 2 ? 'amethyst_block' : 'deepslate_bricks')
  }
  const pillar = (blockX, blockZ) => {
    fill([blockX, 1, blockZ], [blockX, height - 2, blockZ], 'polished_deepslate')
    put(blockX, 1, blockZ, 'calcite')
    put(blockX, height - 3, blockZ, 'calcite')
  }
  const crystal = (blockX, blockZ, towerHeight = 1) => {
    fill([blockX, 1, blockZ], [blockX, towerHeight, blockZ], 'amethyst_block')
    put(blockX, towerHeight + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  const lamp = (blockX, blockZ) => {
    put(blockX, 1, blockZ, 'polished_deepslate')
    put(blockX, 2, blockZ, 'sea_lantern')
  }
  const chest = (blockX, blockZ, blockY = 1) => put(blockX, blockY, blockZ, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  if (kind !== 'corner') for (let blockZ = 3; blockZ < depth - 2; blockZ += 4) {
    for (const blockX of [1, width - 2]) {
      pillar(blockX, blockZ)
      lamp(blockX, blockZ + 1)
      crystal(blockX, blockZ - 1)
    }
    for (let blockX = 2; blockX < width - 2; blockX++) {
      if (kind !== 'courtyard' || blockZ < 5 || blockZ > depth - 6) put(blockX, height - 2, blockZ, blockX % 4 === 0 ? 'calcite' : 'deepslate_bricks')
    }
  }
  const id = `N8_${String(index + 1).padStart(2, '0')}`
  const metadata = { id, name: title, kind, size, entrance: [center, 1, 0], interior: [center, 1, 2], facing: 'north', open_sky: kind === 'courtyard', authority }
  decorate()
  connect()
  fs.writeFileSync(path.join(process.env.S8_METADATA_DIR, `${id}.json`), JSON.stringify(metadata, null, 2))
  return { structure, metadata: { name: id } }

  function decorate() {
    if (kind === 'large' || kind === 'courtyard') {
      const middleZ = Math.floor(depth / 2)
      for (let blockX = center - 2; blockX <= center + 2; blockX++) for (let blockZ = middleZ - 2; blockZ <= middleZ + 2; blockZ++) {
        if (Math.abs(blockX - center) + Math.abs(blockZ - middleZ) <= 3) put(blockX, 1, blockZ, 'polished_deepslate')
      }
      fill([center, 2, middleZ], [center, 5, middleZ], 'amethyst_block')
      put(center, 6, middleZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      for (const [deltaX, deltaZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
        put(center + deltaX, 2, middleZ + deltaZ, 'amethyst_block')
        put(center + deltaX, 3, middleZ + deltaZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      }
      for (const blockX of [4, width - 5]) for (const blockZ of [4, depth - 5]) {
        pillar(blockX, blockZ)
        crystal(blockX + (blockX < center ? 1 : -1), blockZ, 2)
      }
    } else if (kind === 'medium') {
      crystal(center, 8)
      for (const blockX of [2, width - 3]) {
        fill([blockX, 1, 6], [blockX, 2, 8], 'bookshelf')
        chest(blockX, 10)
      }
    } else if (kind === 'small') {
      chest(center, depth - 3)
      crystal(2, depth - 3, 2)
      crystal(width - 3, depth - 3, 1)
    } else if (kind === 'hall') {
      for (const blockX of [5, width - 6]) for (const blockZ of [4, 8, 11]) {
        pillar(blockX, blockZ)
        crystal(blockX + (blockX < center ? -1 : 1), blockZ)
      }
      for (let blockZ = 1; blockZ < depth - 1; blockZ++) put(center, 0, blockZ, blockZ % 2 === 0 ? 'amethyst_block' : 'calcite')
    } else if (kind === 'corner') {
      for (const [blockX, blockZ] of [[4, 3], [10, 3], [4, 7], [10, 7], [4, 14], [11, 8], [13, 14]]) pillar(blockX, blockZ)
      crystal(5, 11, 2)
      lamp(5, 13)
      lamp(9, 5)
      crystal(12, 13)
    } else if (kind === 'entrance') {
      for (const blockX of [3, width - 4]) {
        pillar(blockX, 2)
        crystal(blockX, 3, 2)
        fill([blockX, 1, 7], [blockX, 1, 9], 'dark_oak_planks')
        chest(blockX, 10)
      }
    } else if (kind === 'treasure') {
      fill([center - 2, 1, depth - 6], [center + 2, 1, depth - 3], 'polished_deepslate')
      for (let blockX = center - 2; blockX <= center + 2; blockX++) put(blockX, 1, depth - 7, 'polished_deepslate_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      chest(center, depth - 4, 2)
      for (const blockX of [center - 2, center + 2]) {
        put(blockX, 2, depth - 4, 'amethyst_block')
        put(blockX, 3, depth - 4, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      }
    }
  }

  function connect() {
    const connections = [{ role: 'entrance', facing: 'north', position: [center, 1, 0], width: 3, height: 3 }]
    if (kind === 'corner') connections.push({ role: 'exit', facing: 'east', position: [width - 1, 1, 11], width: 3, height: 3 })
    else if (kind !== 'treasure' && kind !== 'small') connections.push({ role: 'exit', facing: 'south', position: [center, 1, depth - 1], width: 3, height: 3 })
    if (kind === 'large' || kind === 'small') connections.push({ role: 'exit', facing: 'east', position: [width - 1, 1, Math.floor(depth / 2)], width: 3, height: 3 })
    for (const connection of connections) {
      const [doorX, doorY, doorZ] = connection.position
      for (let offset = -1; offset <= 1; offset++) for (let blockY = doorY; blockY < doorY + 3; blockY++) put(doorX + (connection.facing === 'east' ? 0 : offset), blockY, doorZ + (connection.facing === 'east' ? offset : 0), 'air')
      for (let offset = -2; offset <= 2; offset++) put(doorX + (connection.facing === 'east' ? 0 : offset), 4, doorZ + (connection.facing === 'east' ? offset : 0), Math.abs(offset) === 2 ? 'calcite' : 'chiseled_deepslate')
    }
    metadata.connections = connections
  }
}
