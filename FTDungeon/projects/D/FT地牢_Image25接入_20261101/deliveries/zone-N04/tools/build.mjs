export default function build(api) {
  const structure = api.createStructure([35, 16, 35])
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const box = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  box([0, 0, 0], [34, 15, 34], "air")
  box([0, 0, 0], [34, 0, 34], "deepslate_tiles")
  box([0, 1, 0], [34, 1, 34], "stone_bricks")
  for (let blockX = 0; blockX < 35; blockX++) {
    for (let blockZ = 0; blockZ < 35; blockZ++) {
      const variation = (blockX * 29 + blockZ * 19) % 23
      put([blockX, 1, blockZ], variation < 3 ? "cracked_stone_bricks" : variation === 4 ? "andesite" : "stone_bricks")
      for (let blockY = 2; blockY <= 13; blockY++) {
        if (blockX < 2 || blockX > 32 || blockZ < 2 || blockZ > 32) {
          const darkBand = blockY === 2 || blockY === 9 || blockY === 13
          put([blockX, blockY, blockZ], darkBand ? "polished_deepslate" : (blockX * 3 + blockZ * 7 + blockY * 11) % 17 === 0 ? "cracked_stone_bricks" : "stone_bricks")
        }
      }
    }
  }
  box([0, 14, 0], [34, 15, 34], "deepslate_tiles")
  for (const beam of [8, 14, 20, 26]) {
    box([2, 13, beam], [32, 13, beam], "polished_deepslate")
    box([beam, 13, 2], [beam, 13, 32], "polished_deepslate")
  }
  for (const pillarX of [8, 26]) {
    for (const pillarZ of [8, 26]) {
      box([pillarX - 2, 2, pillarZ - 2], [pillarX + 2, 2, pillarZ + 2], "polished_blackstone_bricks")
      box([pillarX - 1, 3, pillarZ - 1], [pillarX + 1, 12, pillarZ + 1], "stone_bricks")
      box([pillarX - 1, 4, pillarZ - 1], [pillarX + 1, 4, pillarZ + 1], "chiseled_stone_bricks")
      box([pillarX - 1, 10, pillarZ - 1], [pillarX + 1, 10, pillarZ + 1], "polished_deepslate")
      box([pillarX - 2, 12, pillarZ - 2], [pillarX + 2, 13, pillarZ + 2], "stone_bricks")
    }
  }
  const transform = (position, turns) => {
    let [blockX, blockY, blockZ] = position
    for (let turn = 0; turn < turns; turn++) [blockX, blockZ] = [34 - blockZ, blockX]
    return [blockX, blockY, blockZ]
  }
  const arch = (turns, material, accent, jewel) => {
    const gateBox = (from, to, block) => box(transform(from, turns), transform(to, turns), block)
    gateBox([15, 2, 0], [19, 8, 5], "air")
    for (const sideX of [13, 21]) {
      gateBox([sideX, 2, 2], [sideX, 9, 4], material)
      gateBox([sideX - 1, 2, 3], [sideX + 1, 2, 4], accent)
      gateBox([sideX - 1, 8, 3], [sideX + 1, 9, 4], accent)
      put(transform([sideX, 5, 5], turns), jewel)
      put(transform([sideX, 7, 5], turns), jewel)
    }
    gateBox([13, 9, 2], [21, 10, 4], material)
    gateBox([14, 11, 2], [20, 11, 4], accent)
    gateBox([16, 12, 2], [18, 12, 4], material)
    gateBox([15, 8, 2], [19, 8, 2], jewel)
    put(transform([17, 11, 5], turns), jewel)
    for (let distance = 1; distance <= 9; distance++) {
      put(transform([17, 1, distance], turns), accent)
      if (distance > 3) {
        put(transform([16, 1, distance], turns), material)
        put(transform([18, 1, distance], turns), material)
      }
    }
    for (let arrow = 0; arrow <= 2; arrow++) {
      for (let spread = -arrow; spread <= arrow; spread++) put(transform([17 + spread, 1, 5 + arrow], turns), jewel)
    }
  }
  arch(0, "polished_blackstone_bricks", "amethyst_block", "purple_stained_glass")
  arch(1, "deepslate_bricks", "polished_blackstone_bricks", "chiseled_polished_blackstone")
  arch(2, "waxed_oxidized_cut_copper", "bricks", "waxed_oxidized_copper")
  arch(3, "sandstone", "smooth_sandstone", "chiseled_sandstone")
  for (let blockX = 10; blockX <= 24; blockX++) {
    for (let blockZ = 10; blockZ <= 24; blockZ++) {
      const offsetX = blockX - 17
      const offsetZ = blockZ - 17
      const radius = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ)
      if (radius > 6.8 && radius < 7.7) put([blockX, 1, blockZ], "polished_blackstone_bricks")
      if (radius > 5.4 && radius <= 6.8) put([blockX, 1, blockZ], "smooth_stone")
      if (radius > 3.6 && radius <= 5.4) put([blockX, 1, blockZ], "polished_deepslate")
      if (Math.abs(offsetX) + Math.abs(offsetZ) <= 2) put([blockX, 1, blockZ], "amethyst_block")
    }
  }
  put([17, 1, 17], "lodestone")
  for (const [offsetX, offsetZ] of [[0, -5], [5, 0], [0, 5], [-5, 0], [3, -3], [3, 3], [-3, -3], [-3, 3]]) put([17 + offsetX, 1, 17 + offsetZ], "amethyst_block")
  for (const position of [[17, 1, 14], [20, 1, 17], [17, 1, 20], [14, 1, 17]]) put(position, "sea_lantern")
  for (const [lampX, lampZ] of [[8, 14], [8, 20], [26, 14], [26, 20], [11, 5], [23, 5], [11, 29], [23, 29]]) {
    box([lampX, 13, lampZ], [lampX, 14, lampZ], "polished_deepslate")
    box([lampX, 9, lampZ], [lampX, 12, lampZ], "chain", { axis: "y", waterlogged: "false" })
    put([lampX, 8, lampZ], "lantern", { hanging: "true", waterlogged: "false" })
  }
  for (const [lampX, lampZ] of [[11, 11], [23, 11], [11, 23], [23, 23]]) {
    box([lampX, 2, lampZ], [lampX, 3, lampZ], "chiseled_stone_bricks")
    put([lampX, 4, lampZ], "lantern", { hanging: "false", waterlogged: "false" })
  }
  for (const turns of [0, 1, 2, 3]) {
    for (const crate of [[4, 2, 10], [5, 2, 10], [4, 3, 10], [4, 2, 11]]) put(transform(crate, turns), "stripped_spruce_log", { axis: "y" })
    for (let shelfZ = 5; shelfZ <= 8; shelfZ++) {
      put(transform([2, 2, shelfZ], turns), "bookshelf")
      put(transform([2, 3, shelfZ], turns), "dark_oak_planks")
    }
    for (const emblemX of [6, 28]) {
      for (let emblemY = 5; emblemY <= 8; emblemY++) put(transform([emblemX, emblemY, 2], turns), "purple_concrete")
      put(transform([emblemX, 7, 3], turns), "amethyst_block")
      put(transform([emblemX, 9, 2], turns), "polished_blackstone")
    }
  }
  return { structure, metadata: { name: "room" } }
}
