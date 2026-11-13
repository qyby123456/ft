export const metadata = {
  id: 'N02',
  name: '石拱矿车站枢纽',
  size: [33, 15, 31],
  entrance: [16, 2, 0],
  interior: [16, 2, 3],
  facing: 'north',
  safe_points: [[16, 2, 3], [20, 2, 10], [20, 2, 20], [20, 2, 27], [29, 2, 23]],
  walking_ports: [
    { id: 'pedestrian-north', facing: 'north', feet: [16, 2, 0], width: 5, height: 4 },
    { id: 'pedestrian-south', facing: 'south', feet: [16, 2, 30], width: 5, height: 4 },
    { id: 'pedestrian-east', facing: 'east', feet: [32, 2, 23], width: 5, height: 4 }
  ],
  rail_ports: [
    { id: 'through-north', facing: 'north', rail: [5, 2, 0], shape: 'north_south', support: [5, 1, 0] },
    { id: 'through-south', facing: 'south', rail: [5, 2, 30], shape: 'north_south', support: [5, 1, 30] },
    { id: 'siding-north', facing: 'north', rail: [10, 2, 0], shape: 'north_south', support: [10, 1, 0] },
    { id: 'branch-north', facing: 'north', rail: [26, 2, 0], shape: 'north_south', support: [26, 1, 0] },
    { id: 'branch-east', facing: 'east', rail: [32, 2, 11], shape: 'east_west', support: [32, 1, 11] }
  ],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N02-hub-minecart-station.png',
  reference_sha256: 'e1d1ea587fe2cfd8299af84f4a372b5cc089d6e4247e51d8505dc02dad1800b3',
  platform: { floor_y: 1, feet_y: 2, safe_waiting_point: [20, 2, 20], pedestrian_spine: { x: [14, 18], z: [0, 30], clear_height: 3 }, east_concourse: { x: [16, 32], z: [21, 25], clear_height: 3 }, yellow_edges: [{ x: 13, z: [3, 27] }, { z: 14, x: [24, 30] }], public_paths_cross_rails: false },
  rail_networks: [
    { id: 'through', kind: 'independent-passive-line', start: [5, 2, 0], end: [5, 2, 30], cells: Array.from({ length: 31 }, (_, index) => [5, 2, index]) },
    { id: 'siding', kind: 'independent-passive-terminus', start: [10, 2, 0], end: [10, 2, 23], buffer: [10, 2, 24], cells: Array.from({ length: 24 }, (_, index) => [10, 2, index]) },
    { id: 'branch', kind: 'independent-passive-curve', start: [26, 2, 0], end: [32, 2, 11], curve: [26, 2, 11], cells: [...Array.from({ length: 12 }, (_, index) => [26, 2, index]), ...Array.from({ length: 6 }, (_, index) => [27 + index, 2, 11])] }
  ],
  lamps: [[16, 8, 6], [16, 8, 16], [16, 8, 26], [7, 8, 10], [7, 8, 21], [25, 8, 19], [25, 8, 27]],
  runtime: { powered_rails: false, automatic_switching: false, spawned_minecarts: false, decorative_levers_drive_tracks: false, required_mods: [], note: 'Three independent vanilla rail networks. External rail continuations, carts and operation/power controls require separate design and validation.' }
}

export default function build(api) {
  const structure = api.createStructure(metadata.size)
  const set = (position, block) => api.setBlock(structure, position, 'minecraft:' + block)
  const fill = (from, to, block) => api.fill(structure, from, to, 'minecraft:' + block)
  fill([0, 0, 0], [32, 14, 30], 'air')
  fill([0, 0, 0], [32, 0, 30], 'deepslate_bricks')
  for (let east = 0; east < 33; east++) {
    for (let south = 0; south < 31; south++) {
      const ballast = east <= 11 || east >= 24 && south <= 13
      const choice = (east * 13 + south * 7) % 17
      set([east, 1, south], ballast ? choice < 6 ? 'gravel' : choice < 10 ? 'andesite' : 'cobblestone' : choice < 3 ? 'cracked_stone_bricks' : 'stone_bricks')
    }
  }
  for (const east of [0, 32]) fill([east, 2, 0], [east, 12, 30], 'stone_bricks')
  for (const south of [0, 30]) fill([0, 2, south], [32, 12, south], 'stone_bricks')
  for (let east = 1; east < 32; east++) {
    for (let south = 1; south < 30; south++) {
      if ((east * 5 + south * 11) % 19 === 0 && (east === 1 || east === 31 || south === 1 || south === 29)) set([east === 1 ? 0 : east === 31 ? 32 : east, 2, south === 1 ? 0 : south === 29 ? 30 : south], 'mossy_stone_bricks')
    }
  }
  for (const up of [3, 10, 12]) {
    const stone = up === 3 ? 'polished_deepslate' : 'chiseled_stone_bricks'
    fill([0, up, 0], [32, up, 0], stone)
    fill([0, up, 30], [32, up, 30], stone)
    fill([0, up, 0], [0, up, 30], stone)
    fill([32, up, 0], [32, up, 30], stone)
  }
  fill([0, 13, 0], [32, 13, 30], 'stone_bricks')
  for (let east = 0; east < 33; east++) {
    for (let south = 0; south < 31; south++) set([east, 14, south], east % 8 === 0 || south % 8 === 0 || east === 32 || south === 30 ? 'deepslate_tiles' : 'stone_brick_slab[type=bottom,waterlogged=false]')
  }
  for (const east of [0, 32]) {
    for (const south of [3, 16, 27]) fill([east, 2, south], [east, 11, south + 1], 'polished_andesite')
  }
  for (const south of [6, 16, 27]) {
    for (const east of [11, 29]) {
      fill([east, 2, south - 1], [east + 1, 3, south], 'polished_deepslate')
      fill([east, 4, south - 1], [east + 1, 9, south], 'polished_andesite')
      fill([east, 5, south - 1], [east + 1, 5, south], 'dark_oak_planks')
      fill([east, 10, south - 1], [east + 1, 11, south], 'deepslate_bricks')
    }
    fill([1, 12, south], [31, 12, south], 'deepslate_bricks')
    for (const east of [1, 2, 3, 10, 13, 28, 31]) set([east, 11, south], 'stone_brick_stairs[facing=' + (east < 16 ? 'west' : 'east') + ',half=top,shape=straight,waterlogged=false]')
  }
  function portal(side, center, halfWidth) {
    const coordinates = (across, up) => side === 'east' ? [32, up, across] : [across, up, side === 'north' ? 0 : 30]
    for (let across = center - halfWidth; across <= center + halfWidth; across++) {
      for (let up = 2; up <= 6; up++) set(coordinates(across, up), 'air')
      set(coordinates(across, 7), 'stone_bricks')
    }
    for (const across of [center - halfWidth - 1, center + halfWidth + 1]) {
      for (let up = 2; up <= 6; up++) set(coordinates(across, up), up === 2 ? 'polished_deepslate' : 'polished_andesite')
    }
    set(coordinates(center, 7), 'air')
    for (const [across, facing] of [[center - halfWidth, side === 'east' ? 'north' : 'west'], [center + halfWidth, side === 'east' ? 'south' : 'east']]) set(coordinates(across, 6), 'stone_brick_stairs[facing=' + facing + ',half=top,shape=straight,waterlogged=false]')
    for (let across = center - halfWidth - 1; across <= center + halfWidth + 1; across++) set(coordinates(across, 8), across === center ? 'chiseled_stone_bricks' : 'stone_bricks')
  }
  for (const center of [5, 10, 26]) portal('north', center, 1)
  portal('north', 16, 2)
  portal('south', 5, 1)
  portal('south', 16, 2)
  portal('east', 11, 1)
  portal('east', 23, 2)
  for (const port of metadata.walking_ports) {
    const [east, up, south] = port.feet
    if (port.facing === 'east') fill([east, 1, south - 2], [east, 1, south + 2], 'polished_andesite')
    else fill([east - 2, 1, south], [east + 2, 1, south], 'polished_andesite')
  }
  for (const network of metadata.rail_networks) {
    for (const [east, up, south] of network.cells) {
      set([east, 1, south], 'polished_andesite')
      const shape = network.id === 'branch' ? south === 11 ? east === 26 ? 'north_east' : 'east_west' : 'north_south' : 'north_south'
      set([east, up, south], 'rail[shape=' + shape + ',waterlogged=false]')
    }
  }
  fill([9, 2, 24], [11, 2, 24], 'polished_deepslate')
  set([10, 3, 24], 'dark_oak_fence[east=true,west=true,north=false,south=false,waterlogged=false]')
  for (let south = 3; south <= 27; south++) set([13, 1, south], 'yellow_concrete')
  for (let east = 24; east <= 30; east++) set([east, 1, 14], 'yellow_concrete')
  for (let south = 8; south <= 23; south++) if (![15, 16].includes(south)) set([12, 2, south], 'iron_bars[north=true,south=true,east=false,west=false,waterlogged=false]')
  for (let east = 24; east <= 31; east++) set([east, 2, 13], 'iron_bars[east=true,west=true,north=false,south=false,waterlogged=false]')
  for (const south of [9, 12, 15, 18]) {
    set([2, 2, south], 'stripped_spruce_wood[axis=y]')
    set([2, 3, south], south % 2 ? 'spruce_planks' : 'stripped_spruce_wood[axis=x]')
    set([3, 2, south], 'spruce_planks')
  }
  for (const south of [21, 23]) set([3, 2, south], 'cauldron')
  for (const south of [18, 27]) {
    for (let east = 25; east <= 27; east++) set([east, 2, south], 'spruce_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
    for (const east of [24, 28]) set([east, 2, south], 'spruce_planks')
  }
  fill([25, 2, 16], [28, 2, 16], 'polished_deepslate')
  for (const east of [25, 27]) set([east, 3, 16], 'lever[face=floor,facing=north,powered=false]')
  fill([23, 2, 17], [23, 4, 17], 'stripped_spruce_log[axis=y]')
  fill([22, 4, 17], [24, 5, 17], 'spruce_planks')
  set([23, 5, 17], 'chiseled_stone_bricks')
  set([30, 2, 28], 'stripped_spruce_wood[axis=y]')
  set([30, 3, 28], 'spruce_planks')
  for (const [east, up, south] of metadata.lamps) {
    fill([east, up + 1, south], [east, 12, south], 'chain[axis=y,waterlogged=false]')
    set([east, up, south], 'lantern[hanging=true,waterlogged=false]')
  }
  for (const south of [2, 28]) {
    set([16, 1, south], 'chiseled_stone_bricks')
    for (const east of [15, 17]) set([east, 1, south], 'yellow_concrete')
  }
  return { structure, metadata: { ...metadata, name: 'room' } }
}
