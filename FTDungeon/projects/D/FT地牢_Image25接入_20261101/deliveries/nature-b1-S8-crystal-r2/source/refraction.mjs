const names = ['双焦晶核分流殿', '错列透镜筛选室', '偏轴折返盲腔', '上下交叉晶桥厅', '双腔测光直廊', '遮视折角廊', '三路校光前厅', '抬升U形观晶庭', '折返聚焦宝库']
const signatures = ['two-core-ground-loops/east-raised-terminal', 'right-left-forced-chicane', 'offset-wall/east-side-exit', 'lower-west-bypass/upper-straight-crossing', 'paired-parallel-lanes/three-control-stations', 'L-shell/inner-offset-screen', 'single-right-detour/three-test-stations', 'elevated-U-return/ground-crystal-island', 'right-left-dais-terminal']

export default function refraction(api, structure, metadata) {
  const [width, height, depth] = metadata.size
  const center = Math.floor(width / 2)
  const number = Number(metadata.id.slice(-2))
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name = 'polished_deepslate') => api.fill(structure, first, last, `minecraft:${name}`)
  const inside = (blockX, blockZ) => metadata.kind !== 'corner' || (blockX >= 4 && blockX <= 10) || (blockZ >= 8 && blockX >= 4)
  const boundary = (blockX, blockZ) => [[-1, 0], [1, 0], [0, -1], [0, 1]].some(([deltaX, deltaZ]) => !inside(blockX + deltaX, blockZ + deltaZ))
  for (let blockX = 1; blockX < width - 1; blockX++) for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
    if (!inside(blockX, blockZ) || boundary(blockX, blockZ)) continue
    fill([blockX, 1, blockZ], [blockX, height - 2, blockZ], 'air')
  }
  metadata.name = names[number - 1]
  metadata.revision = 'r2-refraction-distinct-topology'
  metadata.route_signature = signatures[number - 1]
  if (number === 4) metadata.interior = [center, 1, 1]
  metadata.route_targets = [{ label: 'safe-point', position: metadata.interior }]
  metadata.controls = []
  metadata.mechanism_runtime = 'vanilla lever directly powers supporting redstone lamp; placement/tick test NOT_RUN; no lock or sequence controller'
  const target = (label, position) => metadata.route_targets.push({ label, position })
  const crystal = (blockX, blockZ, peak) => {
    fill([blockX, 1, blockZ], [blockX, peak, blockZ], 'amethyst_block')
    put(blockX, peak + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  const station = (blockX, blockZ, label) => {
    put(blockX, 1, blockZ, 'redstone_lamp', { lit: 'false' })
    put(blockX, 2, blockZ, 'lever', { face: 'floor', facing: 'north', powered: 'false' })
    metadata.controls.push({ label, input: [blockX, 2, blockZ], output: [blockX, 1, blockZ], kind: 'independent-lamp-test', locks_route: false })
  }
  const stairs = (minimumX, maximumX, blockZ, level, facing) => {
    for (let blockX = minimumX; blockX <= maximumX; blockX++) {
      if (level > 1) fill([blockX, 1, blockZ], [blockX, level - 1, blockZ])
      put(blockX, level, blockZ, 'polished_deepslate_stairs', { facing, half: 'bottom', shape: 'straight', waterlogged: 'false' })
    }
  }
  const pier = (blockX, blockZ) => fill([blockX, 1, blockZ], [blockX, 3, blockZ])
  const emptyChest = (blockX, blockY, blockZ) => api.setBlock(structure, [blockX, blockY, blockZ], 'minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  redesign()
  for (const connection of metadata.connections) {
    const [doorX, doorY, doorZ] = connection.position
    for (let offset = -1; offset <= 1; offset++) {
      const blockX = doorX + (connection.facing === 'east' ? 0 : offset)
      const blockZ = doorZ + (connection.facing === 'east' ? offset : 0)
      put(blockX, doorY - 1, blockZ, 'polished_deepslate')
      fill([blockX, doorY, blockZ], [blockX, doorY + 2, blockZ], 'air')
      put(blockX, doorY + 3, blockZ, 'chiseled_deepslate')
    }
    target(`${connection.role}-${connection.facing}`, connection.position)
  }
  function redesign() {
    for (const blockX of [1, width - 2]) for (const blockZ of [2, depth - 3]) {
      if (!inside(blockX, blockZ)) continue
      put(blockX, 1, blockZ, 'calcite')
      put(blockX, 2, blockZ, 'sea_lantern')
    }
    if (number === 1) {
      fill([5, 1, 6], [7, 7, 8], 'amethyst_block')
      fill([11, 1, 11], [13, 6, 13], 'amethyst_block')
      crystal(6, 7, 8)
      crystal(12, 12, 7)
      for (let step = 1; step <= 4; step++) {
        const blockX = 10 + step
        for (let blockZ = 8; blockZ <= 10; blockZ++) {
          if (step > 1) fill([blockX, 1, blockZ], [blockX, step - 1, blockZ])
          put(blockX, step, blockZ, 'polished_deepslate_stairs', { facing: 'east', half: 'bottom', shape: 'straight', waterlogged: 'false' })
        }
      }
      fill([15, 4, 8], [18, 4, 10], 'calcite')
      for (const blockZ of [8, 10]) pier(15, blockZ)
      fill([18, 1, 8], [18, 3, 10], 'deepslate_bricks')
      metadata.connections.find(connection => connection.facing === 'east').position = [18, 5, 9]
      target('east-upper-landing', [16, 5, 9])
      target('first-core-west-loop', [3, 1, 8])
      target('second-core-south-loop', [12, 1, 15])
      station(3, 5, 'near-focus')
      station(9, 14, 'far-focus')
    } else if (number === 2) {
      fill([1, 1, 4], [8, 3, 4], 'calcite')
      fill([4, 1, 8], [11, 3, 8], 'amethyst_block')
      fill([1, 1, 6], [1, 2, 7], 'bookshelf')
      station(10, 6, 'lens-test')
      target('right-gate', [10, 1, 4])
      target('left-gate', [2, 1, 8])
    } else if (number === 3) {
      fill([4, 1, 3], [4, 3, 6], 'calcite')
      crystal(2, 5, 3)
      emptyChest(6, 1, 6)
      target('offset-east-pocket', [6, 1, 3])
    } else if (number === 4) {
      fill([1, 1, 9], [17, 3, 9], 'amethyst_block')
      fill([3, 1, 9], [5, 3, 9], 'air')
      for (let step = 1; step <= 4; step++) stairs(8, 10, 1 + step, step, 'south')
      fill([8, 4, 6], [10, 4, 13], 'calcite')
      for (let step = 1; step <= 3; step++) stairs(8, 10, 13 + step, 4 - step, 'north')
      for (const blockX of [8, 10]) for (const blockZ of [6, 13]) pier(blockX, blockZ)
      target('upper-crossing', [9, 5, 9])
      target('lower-west-bypass', [4, 1, 9])
      crystal(14, 6, 4)
      crystal(4, 13, 3)
    } else if (number === 5) {
      fill([4, 1, 4], [4, 3, 16], 'amethyst_block')
      for (const blockZ of [5, 10, 15]) station(1, blockZ, `spectrum-${blockZ}`)
      target('west-lane', [2, 1, 10])
      target('east-lane', [6, 1, 10])
    } else if (number === 6) {
      fill([8, 1, 8], [8, 3, 11], 'calcite')
      crystal(5, 10, 2)
      station(12, 13, 'corner-test')
      target('rear-of-screen', [9, 1, 12])
      put(5, 2, 5, 'sea_lantern')
      put(5, 1, 5, 'calcite')
    } else if (number === 7) {
      fill([1, 1, 5], [8, 3, 5], 'calcite')
      station(2, 2, 'calibration-A')
      station(3, 8, 'calibration-B')
      station(10, 9, 'calibration-C')
      target('side-entry-gate', [10, 1, 5])
      emptyChest(2, 1, 10)
    } else if (number === 8) {
      fill([2, 4, 6], [6, 4, 16], 'calcite')
      fill([2, 4, 12], [16, 4, 16], 'calcite')
      fill([12, 4, 10], [16, 4, 16], 'calcite')
      for (let step = 1; step <= 4; step++) stairs(3, 5, 1 + step, step, 'south')
      for (let step = 1; step <= 4; step++) stairs(13, 15, 5 + step, step, 'south')
      for (const [blockX, blockZ] of [[2, 6], [6, 6], [2, 16], [6, 12], [9, 16], [12, 12], [16, 10], [16, 16]]) pier(blockX, blockZ)
      for (let blockZ = 6; blockZ <= 16; blockZ++) {
        put(2, 5, blockZ, 'calcite')
        if (blockZ < 12) put(6, 5, blockZ, 'calcite')
      }
      for (let blockX = 3; blockX <= 16; blockX++) put(blockX, 5, 16, 'calcite')
      for (let blockZ = 10; blockZ < 16; blockZ++) put(16, 5, blockZ, 'calcite')
      for (let blockX = 7; blockX <= 11; blockX++) put(blockX, 5, 12, 'calcite')
      crystal(9, 9, 6)
      crystal(8, 9, 3)
      crystal(10, 9, 4)
      target('west-observation-balcony', [4, 5, 9])
      target('rear-observation-balcony', [9, 5, 14])
      target('east-return-balcony', [14, 5, 11])
    } else if (number === 9) {
      fill([1, 1, 6], [13, 3, 6], 'calcite')
      fill([5, 1, 11], [17, 3, 11], 'amethyst_block')
      fill([7, 1, 13], [11, 1, 16], 'calcite')
      stairs(7, 11, 12, 1, 'south')
      emptyChest(9, 2, 15)
      crystal(6, 15, 5)
      crystal(12, 15, 4)
      target('right-detour', [15, 1, 6])
      target('left-detour', [3, 1, 11])
      target('treasure-dais', [9, 2, 14])
    }
  }
}
