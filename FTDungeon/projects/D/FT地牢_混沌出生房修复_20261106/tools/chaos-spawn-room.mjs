export default function build(api) {
  const structure = api.createStructure([97, 10, 17]);
  const fill = (from, to, block) => api.fill(structure, from, to, block);
  fill([0, 0, 0], [16, 9, 16], 'minecraft:deepslate_bricks');
  fill([1, 2, 1], [15, 7, 15], 'minecraft:air');
  fill([1, 1, 1], [15, 1, 15], 'minecraft:stone_bricks');
  fill([3, 1, 3], [13, 1, 13], 'minecraft:polished_andesite');
  fill([6, 1, 6], [10, 1, 10], 'minecraft:chiseled_stone_bricks');
  fill([7, 1, 7], [9, 1, 9], 'minecraft:sea_lantern');
  fill([8, 1, 8], [8, 1, 8], 'minecraft:lodestone');
  for (const cornerX of [1, 15]) {
    for (const cornerZ of [1, 15]) {
      fill([cornerX, 2, cornerZ], [cornerX, 7, cornerZ], 'minecraft:polished_basalt');
      fill([cornerX, 6, cornerZ], [cornerX, 6, cornerZ], 'minecraft:sea_lantern');
    }
  }
  fill([0, 4, 4], [0, 5, 12], 'minecraft:tinted_glass');
  fill([4, 4, 0], [12, 5, 0], 'minecraft:tinted_glass');
  fill([4, 4, 16], [12, 5, 16], 'minecraft:tinted_glass');
  fill([16, 2, 7], [16, 5, 9], 'minecraft:air');
  fill([16, 6, 6], [16, 6, 10], 'minecraft:chiseled_deepslate');
  fill([17, 0, 6], [96, 6, 10], 'minecraft:deepslate_bricks');
  fill([17, 2, 7], [96, 5, 9], 'minecraft:air');
  fill([17, 1, 7], [96, 1, 9], 'minecraft:stone_bricks');
  for (let distance = 18; distance < 97; distance += 6) {
    fill([distance, 5, 6], [distance, 5, 6], 'minecraft:sea_lantern');
    fill([distance, 5, 10], [distance, 5, 10], 'minecraft:sea_lantern');
    fill([distance, 1, 8], [distance, 1, 8], 'minecraft:chiseled_stone_bricks');
  }
  return { structure, metadata: { name: 'chaos-spawn-room' } };
}
