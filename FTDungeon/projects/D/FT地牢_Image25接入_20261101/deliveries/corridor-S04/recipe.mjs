export const specification = {
  id: 'S04',
  name: '陈旧菌洞短廊',
  size: [11, 9, 16],
  entrance: [5, 1, 0],
  interior: [5, 1, 3],
  exit: [5, 1, 15],
  exitInterior: [5, 1, 12],
  facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S04-fungal-cave-aged.png',
  reference_sha256: '6160f7b8403ec6ee56e01a0e909423f918176c24b4a23981fa547597931c12e7',
};

export default function build(api) {
  const structure = api.createStructure(specification.size);
  const placed = new Map();
  const put = (blockX, blockY, blockZ, name, properties = {}) => {
    placed.set(`${blockX},${blockY},${blockZ}`, { position: [blockX, blockY, blockZ], name, properties });
  };
  const box = (minimum, maximum, name, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) {
          put(blockX, blockY, blockZ, name, properties);
        }
      }
    }
  };
  const choose = (blockX, blockY, blockZ, palette) => palette[Math.abs(blockX * 53 + blockY * 29 + blockZ * 37 + blockX * blockZ * 7) % palette.length];
  const masonry = ['stone_bricks', 'mossy_stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks'];
  const earth = ['rooted_dirt', 'dirt', 'rooted_dirt', 'coarse_dirt', 'brown_terracotta'];
  const floor = ['stone_bricks', 'mossy_stone_bricks', 'coarse_dirt', 'cracked_stone_bricks', 'mycelium', 'rooted_dirt'];
  box([0, 0, 0], [10, 8, 15], 'air');
  for (let blockX = 0; blockX < 11; blockX++) {
    for (let blockZ = 0; blockZ < 16; blockZ++) {
      put(blockX, 0, blockZ, choose(blockX, 0, blockZ, floor));
      put(blockX, 8, blockZ, choose(blockX, 8, blockZ, earth));
    }
  }
  for (const wallX of [0, 10]) {
    for (let blockY = 1; blockY < 8; blockY++) {
      for (let blockZ = 0; blockZ < 16; blockZ++) {
        put(wallX, blockY, blockZ, choose(wallX, blockY, blockZ, earth));
      }
    }
  }
  for (const ledge of [[1, 2], [8, 9]]) {
    for (let blockX = ledge[0]; blockX <= ledge[1]; blockX++) {
      for (let blockZ = 1; blockZ < 15; blockZ++) put(blockX, 1, blockZ, 'mycelium');
    }
  }
  for (const archZ of [0, 4, 8, 12, 15]) {
    for (const pillarX of [2, 8]) {
      for (let blockY = 1; blockY <= 5; blockY++) {
        put(pillarX, blockY, archZ, choose(pillarX, blockY, archZ, masonry));
      }
      put(pillarX, 1, archZ, 'chiseled_stone_bricks');
      put(pillarX, 5, archZ, 'mossy_stone_bricks');
    }
    put(3, 6, archZ, 'mossy_stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' });
    put(7, 6, archZ, 'stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' });
    put(2, 6, archZ, 'mossy_stone_bricks');
    put(8, 6, archZ, 'mossy_stone_bricks');
    for (let blockX = 2; blockX <= 8; blockX++) put(blockX, 7, archZ, choose(blockX, 7, archZ, masonry));
    if (archZ !== 8) {
      for (let blockX = 4; blockX <= 6; blockX++) {
        put(blockX, 6, archZ, 'stone_brick_slab', { type: 'top', waterlogged: 'false' });
      }
    }
  }
  for (const endZ of [0, 15]) {
    box([0, 1, endZ], [1, 7, endZ], 'mossy_stone_bricks');
    box([9, 1, endZ], [10, 7, endZ], 'mossy_stone_bricks');
    for (let blockX = 3; blockX <= 7; blockX++) put(blockX, 0, endZ, 'stone_bricks');
  }
  for (const lampZ of [2, 6, 10, 13]) {
    for (const side of [{ backing: 1, glass: 2, rod: 3 }, { backing: 9, glass: 8, rod: 7 }]) {
      put(side.glass, 2, lampZ, 'mossy_stone_bricks');
      put(side.rod, 2, lampZ, 'stone_brick_slab', { type: 'top', waterlogged: 'false' });
      for (const blockY of [3, 4]) {
        put(side.backing, blockY, lampZ, 'sea_lantern');
        put(side.glass, blockY, lampZ, 'cyan_stained_glass');
        put(side.rod, blockY, lampZ, 'end_rod', { facing: 'up' });
      }
      put(side.glass, 5, lampZ, 'dark_prismarine');
    }
  }
  for (const cluster of [{ stemX: 1, capX: [1, 2], stemZ: 3, height: 5 }, { stemX: 9, capX: [8, 9], stemZ: 5, height: 5 }, { stemX: 1, capX: [1, 2], stemZ: 11, height: 4 }, { stemX: 9, capX: [8, 9], stemZ: 11, height: 5 }]) {
    for (let blockY = 2; blockY < cluster.height; blockY++) put(cluster.stemX, blockY, cluster.stemZ, 'mushroom_stem');
    for (const blockX of cluster.capX) {
      for (let blockZ = cluster.stemZ - 1; blockZ <= cluster.stemZ + 1; blockZ++) {
        if ([0, 4, 8, 12, 15].includes(blockZ) && [2, 8].includes(blockX)) continue;
        put(blockX, cluster.height, blockZ, 'red_mushroom_block');
      }
    }
  }
  for (const blockX of [1, 9]) {
    for (const blockZ of [1, 7, 9, 14]) put(blockX, 2, blockZ, blockZ % 2 === 0 ? 'red_mushroom' : 'warped_fungus');
    for (const blockZ of [2, 5, 6, 10, 13, 14]) put(blockX, 7, blockZ, 'hanging_roots', { waterlogged: 'false' });
    for (const blockZ of [1, 7, 14]) put(blockX, 6, blockZ, 'cobweb');
  }
  for (const blockZ of [3, 7, 11]) {
    put(3, 1, blockZ, 'moss_carpet');
    put(7, 1, blockZ + 1, 'moss_carpet');
  }
  for (let blockZ = 0; blockZ < 16; blockZ++) {
    for (let blockX = 4; blockX <= 6; blockX++) {
      for (let blockY = 1; blockY <= 3; blockY++) put(blockX, blockY, blockZ, 'air');
    }
  }
  const directions = { east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0], south: [0, 0, 1], north: [0, 0, -1] };
  for (const block of placed.values()) {
    if (block.name === 'mycelium') {
      const above = [...block.position];
      above[1]++;
      const covering = placed.get(above.join(','))?.name;
      if (!['air', 'red_mushroom', 'warped_fungus', 'moss_carpet'].includes(covering)) block.name = 'rooted_dirt';
      else block.properties = { snowy: 'false' };
    }
    if (['red_mushroom_block', 'mushroom_stem'].includes(block.name)) {
      block.properties = Object.fromEntries(Object.entries(directions).map(([direction, offset]) => {
        const neighbor = block.position.map((coordinate, axis) => coordinate + offset[axis]);
        return [direction, String(placed.get(neighbor.join(','))?.name !== block.name)];
      }));
    }
    api.setBlock(structure, block.position, `minecraft:${block.name}`, block.properties);
  }
  return { structure, metadata: { name: 'room', ...specification, theme: 'aged-fungal-cave', length_class: 'short', axis: 'z', ports: ['north', 'south'] } };
}
