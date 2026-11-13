export default function build(api) {
  const structure = api.createStructure([49, 31, 49])
  const put = (x, y, z, block) => api.setBlock(structure, [x, y, z], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (x, y, z) => Math.abs(Math.sin(x * 127.1 + y * 311.7 + z * 74.7) * 43758.5453) % 1
  const masonry = (x, y, z) => {
    const choice = noise(x, y, z)
    return choice < 0.52 ? 'mossy_stone_bricks' : choice < 0.73 ? 'stone_bricks' : choice < 0.87 ? 'cracked_stone_bricks' : 'mossy_cobblestone'
  }
  const stoneBox = (from, to) => {
    for (let x = from[0]; x <= to[0]; x++) {
      for (let y = from[1]; y <= to[1]; y++) {
        for (let z = from[2]; z <= to[2]; z++) put(x, y, z, masonry(x, y, z))
      }
    }
  }
  box([0, 0, 0], [48, 0, 48], 'stone')
  box([0, 1, 0], [48, 2, 48], 'mud_bricks')
  for (let x = 3; x <= 45; x++) {
    for (let z = 3; z <= 45; z++) {
      put(x, 1, z, noise(x, 1, z) < 0.45 ? 'clay' : 'packed_mud')
      box([x, 2, z], [x, 3, z], 'water[level=0]')
      if (x >= 7 && x <= 41 && z >= 7 && z <= 41 && noise(x, 5, z) < 0.09) put(x, 4, z, 'lily_pad')
    }
  }
  stoneBox([0, 3, 0], [2, 5, 48])
  stoneBox([46, 3, 0], [48, 5, 48])
  stoneBox([0, 3, 0], [48, 5, 2])
  stoneBox([0, 3, 46], [48, 5, 48])
  for (let x = 0; x < 49; x++) {
    for (let z = 0; z < 49; z++) {
      if (x < 7 || x > 41 || z < 7 || z > 41) put(x, 4, z, (x + z) % 5 === 0 ? 'mud_bricks' : masonry(x, 4, z))
    }
  }
  const transform = (side, along, depth) => side === 'north' ? [along, depth] : side === 'west' ? [depth, along] : side === 'south' ? [along, 48 - depth] : [48 - depth, along]
  for (const side of ['north', 'west', 'south', 'east']) {
    const tall = side === 'north' || side === 'west'
    for (let along = 0; along < 49; along++) {
      const height = tall ? 17 + (Math.floor(along / 5) % 3 === 0 ? 1 : 0) : 6 + Math.floor(noise(along, 0, side === 'south' ? 5 : 9) * 3)
      for (let depth = 0; depth <= 2; depth++) {
        const [x, z] = transform(side, along, depth)
        for (let y = 5; y <= height; y++) put(x, y, z, y >= 9 && y <= 15 && noise(Math.floor(x / 3), Math.floor(y / 3), Math.floor(z / 3)) < 0.24 ? 'mud_bricks' : masonry(x, y, z))
        if (depth === 1 && along % 7 < 3 && (along < 17 || along > 31)) put(x, height + 1, z, 'moss_block')
      }
    }
    for (const along of [5, 15, 33, 43]) {
      const [x, z] = transform(side, along, 3)
      box([x, 5, z], [x + (side === 'north' || side === 'south' ? 1 : 0), tall ? 17 : 10, z + (side === 'east' || side === 'west' ? 1 : 0)], 'dark_oak_log[axis=y]')
      if (tall) {
        for (let delta = -1; delta <= 2; delta++) {
          const [beamX, beamZ] = transform(side, along + delta, 3)
          put(beamX, 15, beamZ, side === 'north' ? 'dark_oak_log[axis=x]' : 'dark_oak_log[axis=z]')
        }
      }
      const [lampX, lampZ] = transform(side, along, 4)
      put(lampX, 9, lampZ, 'ochre_froglight[axis=y]')
      put(lampX, 8, lampZ, 'dark_oak_log[axis=y]')
      if (tall) {
        for (let y = 9; y <= 15; y++) {
          const [vineX, vineZ] = transform(side, along + 1, 4)
          put(vineX, y, vineZ, side === 'north' ? 'vine[north=true]' : 'vine[west=true]')
        }
      }
    }
    for (let depth = 0; depth <= 3; depth++) {
      for (let along = 20; along <= 28; along++) {
        const [x, z] = transform(side, along, depth)
        const top = Math.abs(along - 24) <= 2 ? 12 : Math.abs(along - 24) === 3 ? 11 : 9
        for (let y = 5; y <= top; y++) put(x, y, z, 'air')
        put(x, top + 1, z, 'mossy_stone_bricks')
        if (!tall) put(x, top + 2, z, 'mossy_stone_bricks')
      }
    }
    for (const along of [18, 19, 29, 30]) {
      for (let depth = 0; depth <= 3; depth++) {
        const [x, z] = transform(side, along, depth)
        box([x, 5, z], [x, tall ? 16 : 12, z], masonry(x, 8, z))
      }
    }
    for (let along = 22; along <= 26; along++) {
      for (let depth = 0; depth <= 12; depth++) {
        const [x, z] = transform(side, along, depth)
        put(x, 4, z, 'dark_oak_planks')
        box([x, 5, z], [x, 7, z], 'air')
      }
    }
    for (const along of [22, 26]) {
      for (const depth of [7, 11]) {
        const [x, z] = transform(side, along, depth)
        box([x, 2, z], [x, 4, z], 'dark_oak_log[axis=y]')
      }
    }
  }
  stoneBox([13, 2, 13], [35, 4, 35])
  stoneBox([14, 5, 14], [34, 5, 34])
  stoneBox([15, 6, 15], [33, 6, 33])
  stoneBox([16, 7, 16], [32, 7, 32])
  for (let along = 21; along <= 27; along++) {
    for (const side of ['north', 'west', 'south', 'east']) {
      for (let step = 0; step < 3; step++) {
        const [x, z] = transform(side, along, 13 + step)
        const facing = { north: 'south', south: 'north', west: 'east', east: 'west' }[side]
        put(x, 5 + step, z, `stone_brick_stairs[facing=${facing},half=bottom,shape=straight]`)
      }
    }
  }
  box([20, 7, 20], [28, 7, 28], 'moss_block')
  for (const [x, z] of [[17, 17], [31, 17], [17, 31], [31, 31]]) {
    put(x, 8, z, 'mossy_stone_bricks')
    put(x, 9, z, 'ochre_froglight[axis=y]')
  }
  for (let y = 8; y <= 24; y++) {
    const bend = y > 16 ? 1 : 0
    const radius = y < 11 ? 3.4 : y < 19 ? 2.7 : 2
    for (let x = 20; x <= 29; x++) {
      for (let z = 20; z <= 28; z++) {
        if (Math.hypot(x - 24 - bend, z - 24) <= radius + noise(x, y, z) * 0.6) put(x, y, z, 'dark_oak_log[axis=y]')
      }
    }
  }
  for (const [deltaX, deltaZ, reach] of [[1, 0, 5], [-1, 0, 5], [0, 1, 5], [0, -1, 5], [1, 1, 4], [-1, -1, 4]]) {
    for (let step = 2; step <= reach; step++) {
      const x = 24 + deltaX * step
      const z = 24 + deltaZ * step
      const top = 8 + Math.max(0, 5 - step)
      box([x, 8, z], [x, top, z], deltaX === 0 ? 'dark_oak_log[axis=z]' : 'dark_oak_log[axis=x]')
    }
  }
  for (const [endX, endZ, branchY] of [[15, 22, 21], [32, 21, 22], [22, 15, 23], [27, 32, 22], [18, 30, 21]]) {
    api.line(structure, [25, branchY - 3, 24], [endX, branchY, endZ], 'minecraft:dark_oak_log', { axis: Math.abs(endX - 25) > Math.abs(endZ - 24) ? 'x' : 'z' })
    for (let x = endX - 5; x <= endX + 5; x++) {
      for (let z = endZ - 5; z <= endZ + 5; z++) {
        for (let y = branchY; y <= branchY + 5; y++) {
          if (((x - endX) / 5.8) ** 2 + ((z - endZ) / 5.2) ** 2 + ((y - branchY - 2) / 3.3) ** 2 < 1 && noise(x, y, z) > 0.07) put(x, y, z, 'oak_leaves[persistent=true,distance=1]')
        }
      }
    }
    put(endX, branchY, endZ, 'dark_oak_log[axis=y]')
    for (let y = branchY - 4; y < branchY; y++) put(endX, y, endZ, 'chain[axis=y]')
    put(endX, branchY - 5, endZ, 'ochre_froglight[axis=y]')
    for (const offset of [-3, 2]) {
      put(endX + offset, branchY + 1, endZ + 3, 'oak_leaves[persistent=true,distance=1]')
      put(endX + offset, branchY, endZ + 3, 'oak_leaves[persistent=true,distance=1]')
      for (let y = branchY - 5; y <= branchY; y++) put(endX + offset, y, endZ + 4, 'vine[north=true]')
    }
  }
  box([22, 25, 21], [28, 28, 27], 'oak_leaves[persistent=true,distance=1]')
  put(17, 20, 25, 'oak_leaves[persistent=true,distance=1]')
  return { structure, metadata: { name: 's4-01-swamp-heart-rebuild-v1' } }
}
