export default function build(api) {
  const structure = api.createStructure([19, 11, 19])
  const put = (east, up, south, block, properties, nbt) => api.setBlock(structure, [east, up, south], "minecraft:" + block, properties, nbt)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, "minecraft:" + block, properties)
  const masonry = (east, up, south) => {
    const value = (east * 37 + south * 71 + up * 19) % 23
    return value < 3 ? "mossy_stone_bricks" : value < 6 ? "cracked_stone_bricks" : value === 8 ? "andesite" : "stone_bricks"
  }
  fill([0, 0, 0], [18, 10, 18], "air")
  for (let east = 0; east < 19; east++) {
    for (let south = 0; south < 19; south++) {
      put(east, 0, south, masonry(east, 0, south))
      put(east, 10, south, masonry(east, 10, south))
      put(east, 9, south, "spruce_planks")
      if (east >= 7 && east <= 10 && south >= 6 && south <= 10) {
        put(east, 9, south, "cobbled_deepslate")
        put(east, 10, south, "cracked_stone_bricks")
      }
      if (east === 0 || east === 18 || south === 0 || south === 18) {
        for (let up = 1; up <= 9; up++) put(east, up, south, masonry(east, up, south))
      }
    }
  }
  for (const east of [1, 17]) {
    for (const south of [1, 6, 12, 17]) fill([east, 1, south], [east, 8, south], "stripped_spruce_log", { axis: "y" })
    fill([east, 8, 1], [east, 8, 17], "stripped_spruce_log", { axis: "z" })
  }
  for (const east of [4, 14]) {
    for (const south of [5, 13]) {
      put(east, 1, south, "chiseled_stone_bricks")
      for (let up = 2; up <= 6; up++) put(east, up, south, masonry(east, up, south))
      put(east, 7, south, "chiseled_stone_bricks")
    }
    fill([east, 8, 5], [east, 8, 13], "stripped_spruce_log", { axis: "z" })
  }
  for (const south of [5, 9, 13]) fill([4, 8, south], [14, 8, south], "stripped_spruce_log", { axis: "x" })
  for (const east of [6, 12]) {
    fill([east, 6, 9], [east, 7, 9], "chain", { axis: "y", waterlogged: "false" })
    put(east, 5, 9, "lantern", { hanging: "true", waterlogged: "false" })
  }
  fill([8, 1, 0], [10, 3, 0], "air")
  fill([7, 1, 0], [7, 4, 0], "polished_andesite")
  fill([11, 1, 0], [11, 4, 0], "polished_andesite")
  fill([7, 4, 0], [11, 4, 0], "chiseled_stone_bricks")
  fill([0, 3, 8], [0, 5, 10], "black_stained_glass")
  for (const south of [8, 9, 10]) {
    put(1, 3, south, "spruce_trapdoor", { facing: "east", half: "bottom", open: "true", powered: "false", waterlogged: "false" })
    put(1, 5, south, "spruce_trapdoor", { facing: "east", half: "bottom", open: "true", powered: "false", waterlogged: "false" })
  }
  fill([5, 1, 16], [12, 1, 17], "spruce_planks")
  put(6, 1, 16, "crafting_table")
  put(7, 1, 16, "smithing_table")
  put(8, 1, 16, "fletching_table")
  fill([5, 2, 16], [12, 2, 17], "spruce_slab", { type: "bottom", waterlogged: "false" })
  for (const east of [5, 12]) fill([east, 1, 17], [east, 5, 17], "stripped_spruce_log", { axis: "y" })
  fill([5, 5, 16], [12, 5, 17], "spruce_planks")
  for (const east of [6, 8, 10]) {
    fill([east, 3, 16], [east, 4, 16], "chain", { axis: "y", waterlogged: "false" })
    put(east, 3, 17, "stone_button", { face: "wall", facing: "north", powered: "false" })
  }
  put(11, 3, 17, "grindstone", { face: "wall", facing: "north" })
  for (const east of [5, 9, 11]) put(east, 1, 15, "spruce_trapdoor", { facing: "north", half: "bottom", open: "true", powered: "false", waterlogged: "false" })
  put(6, 1, 12, "grindstone", { face: "floor", facing: "west" })
  put(5, 1, 13, "stonecutter", { facing: "north" })
  put(13, 1, 7, "chipped_anvil", { facing: "east" })
  put(15, 1, 11, "furnace", { facing: "west", lit: "false" }, api.createBlockNbt({ id: "minecraft:furnace", Items: [], BurnTime: 0, CookTime: 0, CookTimeTotal: 200 }))
  fill([15, 2, 11], [15, 4, 11], "cobblestone")
  const barrel = (east, up, south) => put(east, up, south, "barrel", { facing: "up", open: "false" }, api.createBlockNbt({ id: "minecraft:barrel", Items: [] }))
  barrel(2, 1, 13)
  barrel(16, 1, 14)
  barrel(16, 2, 14)
  put(15, 1, 16, "chest", { facing: "north", type: "single", waterlogged: "false" }, api.createBlockNbt({ id: "minecraft:chest", Items: [] }))
  put(16, 1, 5, "spruce_stairs", { facing: "west", half: "bottom", shape: "straight", waterlogged: "false" })
  put(15, 1, 5, "spruce_trapdoor", { facing: "west", half: "bottom", open: "false", powered: "false", waterlogged: "false" })
  for (const position of [[2, 1, 4], [3, 1, 7], [3, 1, 12], [4, 1, 15], [13, 1, 15], [16, 1, 8], [15, 1, 13], [6, 1, 6]]) put(...position, "cobblestone_slab", { type: "bottom", waterlogged: "false" })
  for (const position of [[3, 1, 5], [5, 1, 7], [3, 1, 14], [14, 1, 16], [16, 1, 7]]) put(...position, "stone_button", { face: "floor", facing: "north", powered: "false" })
  for (const position of [[2, 1, 15], [16, 1, 6]]) put(...position, "spruce_slab", { type: "bottom", waterlogged: "false" })
  put(2, 1, 5, "lantern", { hanging: "false", waterlogged: "false" })
  put(3, 1, 11, "lantern", { hanging: "false", waterlogged: "false" })
  return { structure, metadata: { name: "room", id: "A04", title: "砸毁工坊", source: "A04-abandoned-smashed-workshop.png" } }
}
