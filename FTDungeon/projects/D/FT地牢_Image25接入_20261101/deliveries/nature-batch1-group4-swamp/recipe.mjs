import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.dirname(fileURLToPath(import.meta.url))
const variants = [
  ['large-chamber', '湿地古树大厅', [19, 16, 19]],
  ['medium-room', '沉水石台室', [13, 8, 13]],
  ['small-room', '湿地藏宝小室', [7, 8, 7]],
  ['great-hall', '双列苔柱长厅', [19, 8, 19]],
  ['corridor', '湿地直通廊', [9, 8, 19]],
  ['corner', '湿地直角转廊', [13, 8, 13]],
  ['entrance', '泥石守卫前厅', [13, 8, 13]],
  ['courtyard', '古树天井水院', [19, 16, 19]],
  ['treasure', '蛙灯祭坛宝库', [19, 16, 19]],
]

export default function build(api) {
  const index = Number(process.env.SWAMP_ROOM || 1) - 1
  if (!variants[index]) throw new Error('SWAMP_ROOM must be 1..9')
  const [kind, title] = variants[index]
  const evidence = JSON.parse(fs.readFileSync(path.join(root, 'source-evidence.json'), 'utf8')).modules[index]
  const size = evidence.output_size
  const [width, height, depth] = size
  const middle = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const id = `NAT-B1-G4-${String(index + 1).padStart(2, '0')}`
  const structure = api.createStructure(size)
  const cells = new Set()
  const key = (blockX, blockZ) => `${blockX},${blockZ}`
  const inside = (blockX, blockZ) => cells.has(key(blockX, blockZ))
  for (let blockX = 0; blockX < width; blockX++) {
    for (let blockZ = 0; blockZ < depth; blockZ++) {
      if (kind !== 'corner' || (Math.abs(blockX - middle) <= 3 && blockZ <= centerZ + 3) || (Math.abs(blockZ - centerZ) <= 3 && blockX >= middle - 3)) cells.add(key(blockX, blockZ))
    }
  }
  const put = (blockX, blockY, blockZ, name, properties, nbt) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties, nbt)
  const fill = (start, end, name, properties) => api.fill(structure, start, end, `minecraft:${name}`, properties)
  const boundary = (blockX, blockZ) => [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([deltaX, deltaZ]) => !inside(blockX + deltaX, blockZ + deltaZ))
  const exits = []
  const entrance = [middle, 2, 0]
  if (!['small-room', 'medium-room', 'corner', 'treasure'].includes(kind)) exits.push({ face: 'south', position: [middle, 2, depth - 1] })
  if (['large-chamber', 'courtyard', 'corner'].includes(kind)) exits.push({ face: 'east', position: [width - 1, 2, centerZ] })
  for (const coordinate of cells) {
    const [blockX, blockZ] = coordinate.split(',').map(Number)
    put(blockX, 0, blockZ, 'stone_bricks')
    put(blockX, 1, blockZ, (blockX * 19 + blockZ * 7) % 5 ? 'mossy_stone_bricks' : 'mud_bricks')
    if (boundary(blockX, blockZ)) {
      for (let blockY = 2; blockY < height - 1; blockY++) put(blockX, blockY, blockZ, (blockX + blockY + blockZ) % 7 ? 'mossy_stone_bricks' : 'cracked_stone_bricks')
    }
    if (kind !== 'courtyard' || boundary(blockX, blockZ)) put(blockX, height - 1, blockZ, 'dark_oak_planks')
  }
  for (let blockZ = 0; blockZ < depth; blockZ++) {
    for (let blockX = middle - 1; blockX <= middle + 1; blockX++) {
      if (inside(blockX, blockZ) && (kind !== 'corner' || blockZ <= centerZ + 1)) put(blockX, 1, blockZ, 'stone_bricks')
    }
  }
  if (exits.some(exit => exit.face === 'east')) {
    for (let blockX = middle - 1; blockX < width; blockX++) {
      for (let blockZ = centerZ - 1; blockZ <= centerZ + 1; blockZ++) put(blockX, 1, blockZ, 'stone_bricks')
    }
  }
  const openPortal = (position, face) => {
    const [blockX, , blockZ] = position
    for (let offset = -1; offset <= 1; offset++) {
      for (let blockY = 2; blockY <= 4; blockY++) put(blockX + (face === 'east' ? 0 : offset), blockY, blockZ + (face === 'east' ? offset : 0), 'air')
    }
    for (let offset = -2; offset <= 2; offset++) put(blockX + (face === 'east' ? 0 : offset), 5, blockZ + (face === 'east' ? offset : 0), 'mud_bricks')
  }
  openPortal(entrance, 'north')
  for (const exit of exits) openPortal(exit.position, exit.face)
  return finish(api, { root, structure, index, kind, title, size, middle, centerZ, id, cells, put, fill, entrance, exits })
}

function finish(api, context) {
  const { structure, index, kind, title, size, middle, centerZ, id, cells, put, fill, entrance, exits } = context
  const [width, height, depth] = size
  const pillar = (blockX, blockZ) => {
    put(blockX, 1, blockZ, 'mud_bricks')
    fill([blockX, 2, blockZ], [blockX, height - 2, blockZ], 'dark_oak_log', { axis: 'y' })
    put(blockX, 2, blockZ, 'mud_bricks')
    put(blockX, height - 2, blockZ, 'mud_bricks')
  }
  const lamp = (blockX, blockZ) => {
    put(blockX, 2, blockZ, 'mossy_cobblestone')
    put(blockX, 3, blockZ, 'ochre_froglight', { axis: 'y' })
  }
  const pool = (minimumX, maximumX, minimumZ, maximumZ) => {
    for (let blockX = minimumX; blockX <= maximumX; blockX++) {
      for (let blockZ = minimumZ; blockZ <= maximumZ; blockZ++) {
        if (structure.getBlock([blockX, 2, blockZ])) continue
        put(blockX, 1, blockZ, 'water', { level: '0' })
        if ((blockX + blockZ) % 5 === 0) put(blockX, 2, blockZ, 'lily_pad')
      }
    }
  }
  const chest = (blockX, blockZ) => {
    put(blockX, 2, blockZ, 'mud_bricks')
    put(blockX, 3, blockZ, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  }
  if (kind !== 'corner') {
    for (const blockX of [1, width - 2]) {
      for (const blockZ of [2, depth - 3]) pillar(blockX, blockZ)
    }
    for (const blockZ of [2, depth - 3]) fill([1, height - 2, blockZ], [width - 2, height - 2, blockZ], 'dark_oak_log', { axis: 'x' })
    for (const blockX of [2, width - 3]) {
      for (const blockZ of [3, depth - 4]) lamp(blockX, blockZ)
    }
  }
  if (['large-chamber', 'medium-room', 'courtyard', 'great-hall'].includes(kind)) {
    pool(3, middle - 3, 4, centerZ - 2)
    pool(middle + 3, width - 4, centerZ + 2, depth - 5)
  }
  if (kind === 'large-chamber' || kind === 'courtyard') {
    const treeX = middle
    const treeZ = kind === 'large-chamber' ? centerZ : centerZ + 1
    fill([treeX - 1, 1, treeZ - 1], [treeX + 1, 2, treeZ + 1], 'mossy_cobblestone')
    fill([treeX, 3, treeZ], [treeX, height - 3, treeZ], 'dark_oak_log', { axis: 'y' })
    fill([treeX - 2, height - 3, treeZ], [treeX + 2, height - 3, treeZ], 'dark_oak_log', { axis: 'x' })
    for (let blockX = treeX - 2; blockX <= treeX + 2; blockX++) {
      for (let blockZ = treeZ - 2; blockZ <= treeZ + 2; blockZ++) put(blockX, height - 2, blockZ, 'oak_leaves', { persistent: 'true', distance: '1', waterlogged: 'false' })
    }
  }
  if (kind === 'great-hall') {
    for (const blockZ of [4, 8, 12]) {
      for (const blockX of [3, width - 4]) pillar(blockX, blockZ)
      fill([3, height - 2, blockZ], [width - 4, height - 2, blockZ], 'dark_oak_log', { axis: 'x' })
    }
  }
  if (kind === 'corridor') {
    pool(1, 2, 6, 7)
    pool(width - 3, width - 2, 6, 7)
    pool(1, 2, 10, 11)
    pool(width - 3, width - 2, 10, 11)
    for (const blockZ of [5, 9, 13]) {
      for (const blockX of [1, width - 2]) { pillar(blockX, blockZ); lamp(blockX + (blockX === 1 ? 1 : -1), blockZ) }
      fill([1, height - 2, blockZ], [width - 2, height - 2, blockZ], 'dark_oak_log', { axis: 'x' })
    }
  }
  if (kind === 'corner') {
    for (const [blockX, blockZ] of [[middle - 2, 2], [middle + 2, 2], [middle - 2, centerZ + 2], [width - 3, centerZ + 2]]) lamp(blockX, blockZ)
    pillar(middle - 3, centerZ + 3)
    fill([middle - 3, height - 2, 1], [middle - 3, height - 2, centerZ + 3], 'dark_oak_log', { axis: 'z' })
    fill([middle - 3, height - 2, centerZ + 3], [width - 2, height - 2, centerZ + 3], 'dark_oak_log', { axis: 'x' })
  }
  if (kind === 'medium-room') {
    fill([middle + 3, 2, centerZ - 1], [middle + 4, 2, centerZ + 1], 'mossy_stone_bricks')
    chest(2, centerZ)
    chest(width - 3, centerZ)
  }
  if (kind === 'small-room') chest(middle, depth - 3)
  if (kind === 'entrance') {
    for (const blockX of [3, width - 4]) {
      fill([blockX, 2, centerZ], [blockX, 4, centerZ], 'mud_bricks')
      put(blockX, 5, centerZ, 'ochre_froglight', { axis: 'y' })
      chest(blockX, depth - 4)
    }
  }
  if (kind === 'treasure') {
    fill([middle - 2, 1, depth - 5], [middle + 2, 2, depth - 3], 'mud_bricks')
    chest(middle, depth - 4)
    for (const blockX of [middle - 2, middle + 2]) lamp(blockX, depth - 5)
    fill([2, 2, depth - 2], [3, 4, depth - 2], 'bookshelf')
    fill([width - 4, 2, depth - 2], [width - 3, 4, depth - 2], 'bookshelf')
  }
  const routes = {
    'great-hall': [[9, 0], [9, 3], [6, 3], [6, 8], [12, 8], [12, 13], [9, 13], [9, 18]],
    courtyard: [[9, 0], [9, 3], [13, 3], [13, 8], [5, 8], [5, 13], [9, 13], [9, 18]],
  }
  const route = routes[kind]
  if (route) {
    const dry = new Set()
    const segment = (start, end) => {
      const distance = Math.abs(end[0] - start[0]) + Math.abs(end[1] - start[1])
      for (let step = 0; step <= distance; step++) {
        const blockX = start[0] + Math.sign(end[0] - start[0]) * step
        const blockZ = start[1] + Math.sign(end[1] - start[1]) * step
        for (let offsetX = -1; offsetX <= 1; offsetX++) {
          for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
            const targetX = blockX + offsetX
            const targetZ = blockZ + offsetZ
            if (targetX >= 0 && targetX < width && targetZ >= 0 && targetZ < depth) dry.add(`${targetX},${targetZ}`)
          }
        }
      }
    }
    for (let routeIndex = 1; routeIndex < route.length; routeIndex++) segment(route[routeIndex - 1], route[routeIndex])
    if (kind === 'large-chamber') segment([14, 9], [18, 9])
    if (kind === 'courtyard') { segment([13, 8], [13, 9]); segment([13, 9], [18, 9]) }
    for (let blockX = 1; blockX < width - 1; blockX++) {
      for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
        const upper = structure.getBlock([blockX, 2, blockZ])
        const upperName = upper ? String(upper.state.getName()) : null
        const occupied = upper && upperName !== 'minecraft:lily_pad'
        if (dry.has(`${blockX},${blockZ}`)) {
          put(blockX, 1, blockZ, 'dark_oak_planks')
          if (upperName === 'minecraft:lily_pad') put(blockX, 2, blockZ, 'air')
        } else if (!occupied) {
          put(blockX, 1, blockZ, 'water', { level: '0' })
          if ((blockX + blockZ) % 7 === 0) put(blockX, 2, blockZ, 'lily_pad')
        }
      }
    }
  }
  if (kind === 'courtyard') {
    fill([2, 1, 12], [3, 2, 14], 'mud_bricks')
    put(4, 1, 13, 'mud_bricks')
    put(4, 2, 13, 'stone_brick_stairs', { facing: 'west', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  if (kind === 'large-chamber') {
    for (let blockX = 1; blockX < width - 1; blockX++) {
      for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
        if (!structure.getBlock([blockX, 2, blockZ])) put(blockX, 1, blockZ, 'water', { level: '0' })
      }
    }
    fill([middle - 3, 1, centerZ - 3], [middle + 3, 2, centerZ + 3], 'mossy_stone_bricks')
    fill([middle - 1, 2, centerZ - 1], [middle + 1, 2, centerZ + 1], 'mossy_cobblestone')
    for (let blockZ = 0; blockZ < depth; blockZ++) {
      for (let blockX = middle - 1; blockX <= middle + 1; blockX++) {
        if (blockZ < centerZ - 3 || blockZ > centerZ + 3) put(blockX, 1, blockZ, 'dark_oak_planks')
      }
    }
    for (let blockX = middle + 4; blockX < width; blockX++) {
      for (let blockZ = centerZ - 1; blockZ <= centerZ + 1; blockZ++) put(blockX, 1, blockZ, 'dark_oak_planks')
    }
    for (let offset = -1; offset <= 1; offset++) {
      put(middle + offset, 2, centerZ - 4, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(middle + offset, 2, centerZ + 4, 'stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(middle + 4, 2, centerZ + offset, 'stone_brick_stairs', { facing: 'west', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
    for (const blockX of [middle - 3, middle + 3]) {
      for (const blockZ of [centerZ - 3, centerZ + 3]) {
        put(blockX, 3, blockZ, 'mud_bricks')
        put(blockX, 4, blockZ, 'ochre_froglight', { axis: 'y' })
      }
    }
  }
  const metadata = { id, name: title, kind, size, entrance, interior: [middle, 2, 2], facing: 'north', exits,
    topology: kind === 'large-chamber' ? { pattern: 'raised_central_tree_island_three_short_bridges', island_bounds_xz: [6, 6, 12, 12], bridge_feet_y: 2, island_feet_y: 3, central_tree_occludes_direct_crossing: true } : route ? { pattern: 'wetland_meandering_islet_chain_no_ring', centerline_xz: route, turn_count: route.length - 2, dry_width: 3, branch: exits.some(exit => exit.face === 'east') ? 'east spur at island crossing' : null } : { pattern: 'supporting_' + kind },
    vertical_layers: { foundation: 0, sealed_water: 1, dry_walk_feet: 2, observation_feet: kind === 'courtyard' ? [3, 3, 13] : kind === 'large-chamber' ? [7, 3, 7] : null },
    mechanisms: { active: false, passive: 'water separates dry islets, enforcing dog-leg circulation; no reward, trap or redstone claims' },
    source_evidence: JSON.parse(fs.readFileSync(path.join(root, 'source-evidence.json'), 'utf8')).modules[index],
    connection_points: [{ face: 'north', position: entrance }, ...exits].map(point => ({ ...point, aperture: [3, 3], coordinates: 'relative XYZ player feet', clearance_required_outside: true })),
    open_sky: kind === 'courtyard', footprint: [...cells].map(coordinate => coordinate.split(',').map(Number)),
    reference: `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-${String(index + 1).padStart(2, '0')}-${kind}.png`,
    design_deviations: ['Concept cutaways are preview only; non-courtyard modules have complete roofs.', 'Flooded floors replaced by sealed shallow side pools and dry full-block circulation.', 'Vanilla 1.20.1 only. No entities, rewards or spawners.'] }
  fs.mkdirSync(path.join(root, 'incoming', id), { recursive: true })
  fs.writeFileSync(path.join(root, 'incoming', id, 'room.json'), JSON.stringify(metadata, null, 2))
  return { structure, metadata: { name: 'room' } }
}
