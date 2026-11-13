export default function build(api) {
  const structure = api.createStructure([25, 14, 25]);
  const put = (horizontal, vertical, depth, name, properties) => api.setBlock(structure, [horizontal, vertical, depth], `minecraft:${name}`, properties);
  const box = (from, to, name, properties) => api.fill(structure, from, to, `minecraft:${name}`, properties);
  const rocks = ['cobbled_deepslate', 'deepslate_bricks', 'tuff', 'deepslate_tiles', 'cracked_deepslate_bricks'];
  const route = (horizontal, depth) => horizontal >= 10 && horizontal <= 14 && depth <= 14 || horizontal >= 10 && depth >= 10 && depth <= 14;
  box([0, 0, 0], [24, 13, 24], 'air');
  for (let horizontal = 0; horizontal < 25; horizontal++) {
    for (let depth = 0; depth < 25; depth++) {
      put(horizontal, 0, depth, 'deepslate_bricks');
      put(horizontal, 1, depth, route(horizontal, depth) ? ((horizontal + depth) % 7 === 0 ? 'polished_deepslate' : 'deepslate_tiles') : rocks[(horizontal * 11 + depth * 7) % rocks.length]);
      put(horizontal, 13, depth, rocks[(horizontal * 3 + depth * 13) % rocks.length]);
      for (let vertical = 2; vertical <= 12; vertical++) {
        const border = horizontal <= 1 || horizontal >= 23 || depth <= 1 || depth >= 23;
        const vault = vertical >= 11 && (horizontal <= 3 || horizontal >= 21 || depth <= 3 || depth >= 21);
        if (border || vault) put(horizontal, vertical, depth, rocks[(horizontal * 5 + depth * 3 + vertical) % rocks.length]);
      }
    }
  }
  for (const [horizontal, depth] of [[3, 14], [18, 7], [11, 21]]) {
    box([horizontal, 2, depth], [horizontal + 1, 10, depth + 1], 'polished_deepslate');
    box([horizontal - 1, 2, depth - 1], [horizontal + 2, 2, depth + 2], 'deepslate_bricks');
    box([horizontal - 1, 10, depth - 1], [horizontal + 2, 10, depth + 2], 'deepslate_bricks');
    for (const vertical of [3, 6, 9]) box([horizontal, vertical, depth], [horizontal + 1, vertical, depth + 1], 'chiseled_deepslate');
  }
  for (const depth of [8, 21]) box([2, 11, depth], [22, 12, depth], 'deepslate_bricks');
  for (const horizontal of [4, 19]) box([horizontal, 11, 2], [horizontal, 12, 22], 'deepslate_bricks');
  box([10, 2, 0], [14, 6, 3], 'air');
  box([21, 2, 10], [24, 6, 14], 'air');
  for (const horizontal of [9, 15]) box([horizontal, 2, 0], [horizontal, 7, 2], 'polished_deepslate');
  box([9, 7, 0], [15, 7, 2], 'deepslate_bricks');
  for (const depth of [9, 15]) box([22, 2, depth], [24, 7, depth], 'polished_deepslate');
  box([22, 7, 9], [24, 7, 15], 'deepslate_bricks');
  box([3, 2, 17], [9, 2, 22], 'cobbled_deepslate');
  box([4, 3, 18], [8, 3, 21], 'calcite');
  box([5, 4, 18], [7, 5, 20], 'amethyst_block');
  box([6, 6, 19], [6, 9, 19], 'amethyst_block');
  put(6, 10, 19, 'amethyst_cluster', {facing: 'up', waterlogged: 'false'});
  for (const [horizontal, depth, height] of [[4, 19, 5], [8, 20, 6], [5, 21, 5], [8, 18, 5]]) {
    box([horizontal, 4, depth], [horizontal, height, depth], 'amethyst_block');
    put(horizontal, height + 1, depth, 'amethyst_cluster', {facing: 'up', waterlogged: 'false'});
  }
  for (const [horizontal, depth] of [[5, 19], [7, 19], [6, 18], [6, 20]]) put(horizontal, 4, depth, 'sea_lantern');
  for (const [left, front, right, back] of [[15, 17, 20, 20], [5, 10, 7, 12]]) {
    for (let horizontal = left - 1; horizontal <= right + 1; horizontal++) {
      for (let depth = front - 1; depth <= back + 1; depth++) {
        if (horizontal < left || horizontal > right || depth < front || depth > back) put(horizontal, 1, depth, 'polished_deepslate');
        else {
          put(horizontal, 0, depth, (horizontal + depth) % 4 === 0 ? 'sea_lantern' : 'prismarine');
          put(horizontal, 1, depth, 'water', {level: '0'});
        }
      }
    }
  }
  for (let horizontal = 15; horizontal <= 21; horizontal++) put(horizontal, 2, 21, 'spruce_fence', {waterlogged: 'false'});
  for (let depth = 17; depth <= 20; depth++) put(21, 2, depth, 'spruce_fence', {waterlogged: 'false'});
  for (const [horizontal, depth] of [[4, 4], [18, 22]]) {
    put(horizontal, 2, depth, 'spruce_planks');
    put(horizontal, 3, depth, 'stripped_spruce_wood', {axis: 'y'});
    put(horizontal, 4, depth, 'lantern', {hanging: 'false', waterlogged: 'false'});
  }
  for (let horizontal = 3; horizontal <= 8; horizontal++) put(horizontal, 2, 7, 'rail', {shape: 'east_west', waterlogged: 'false'});
  for (const [horizontal, depth] of [[8, 8], [17, 8], [8, 21], [20, 21]]) {
    put(horizontal, 8, depth, 'lantern', {hanging: 'true', waterlogged: 'false'});
    box([horizontal, 9, depth], [horizontal, 10, depth], 'chain', {axis: 'y', waterlogged: 'false'});
  }
  for (const [horizontal, depth, height] of [[3, 3, 4], [20, 4, 4], [21, 7, 5], [3, 20, 5], [21, 22, 5]]) {
    box([horizontal, 2, depth], [horizontal, height, depth], 'amethyst_block');
    put(horizontal, height + 1, depth, 'amethyst_cluster', {facing: 'up', waterlogged: 'false'});
    if (height >= 5) {
      put(horizontal, height - 1, depth, 'sea_lantern');
      put(horizontal - 1, 2, depth, 'calcite');
      put(horizontal - 1, 3, depth, 'large_amethyst_bud', {facing: 'up', waterlogged: 'false'});
    }
  }
  for (const [horizontal, vertical] of [[4, 6], [7, 8], [18, 5], [21, 8]]) {
    put(horizontal, vertical, 1, 'amethyst_block');
    put(horizontal, vertical, 2, 'large_amethyst_bud', {facing: 'south', waterlogged: 'false'});
  }
  for (const [depth, vertical] of [[4, 5], [10, 7], [18, 8], [21, 5]]) {
    put(1, vertical, depth, 'amethyst_block');
    put(2, vertical, depth, 'amethyst_cluster', {facing: 'east', waterlogged: 'false'});
  }
  for (const [horizontal, vertical] of [[4, 8], [9, 7], [17, 6], [21, 8]]) {
    put(horizontal, vertical, 23, 'amethyst_block');
    put(horizontal, vertical, 22, 'amethyst_cluster', {facing: 'north', waterlogged: 'false'});
  }
  for (const [depth, vertical] of [[4, 8], [7, 6], [18, 7], [21, 9]]) {
    put(23, vertical, depth, 'amethyst_block');
    put(22, vertical, depth, 'large_amethyst_bud', {facing: 'west', waterlogged: 'false'});
  }
  for (const [horizontal, depth] of [[9, 4], [15, 7], [9, 15], [18, 15], [22, 15]]) {
    put(horizontal, 0, depth, 'sea_lantern');
    put(horizontal, 1, depth, 'cyan_stained_glass');
  }
  return {structure, metadata: {name: 'room', id: 'zone-C05', theme: 'crystal corner cavern'}};
}
