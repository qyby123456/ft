export const specification = {
  id: 'N05', delivery_id: 'zone-N05', title: '升降枢纽 / Lift Hub', size: [29, 24, 29],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N05-hub-elevator-lift-hub.png',
  reference_sha256: '4838644367efccc2356e4c2aa59b1a0726670e19a75b8dacc07dea99320f6c00',
  ports: [
    { id: 'H_N', type: 'horizontal', facing: 'north', feet: [14, 7, 0], safe_landing: [14, 7, 4], aperture: [[13, 7, 0], [15, 11, 1]] },
    { id: 'H_E', type: 'horizontal', facing: 'east', feet: [28, 7, 14], safe_landing: [24, 7, 14], aperture: [[27, 7, 13], [28, 11, 15]] },
    { id: 'H_S', type: 'horizontal', facing: 'south', feet: [14, 7, 28], safe_landing: [14, 7, 24], aperture: [[13, 7, 27], [15, 11, 28]] },
    { id: 'H_W', type: 'horizontal', facing: 'west', feet: [0, 7, 14], safe_landing: [4, 7, 14], aperture: [[0, 7, 13], [1, 11, 15]] },
    { id: 'V_LOWER', type: 'internal_vertical_dock', facing: 'up', feet: [5, 1, 9], safe_landing: [6, 1, 9], boundary_open: false, capped_surface_y: 0 },
    { id: 'V_UPPER', type: 'internal_vertical_dock', facing: 'down', feet: [5, 18, 9], safe_landing: [6, 18, 9], boundary_open: false, capped_surface_y: 23 },
  ],
  routes: { main_floor_y: 6, lower_floor_y: 0, upper_floor_y: 17, ladder: { column: [4, 9], min_y: 1, max_y: 18, facing: 'east', support_x: 3 }, main_landing: [5, 7, 9], lower_shaft_landing: [14, 1, 14] },
  shaft: { opening: [[10, 6, 10], [18, 6, 18]], main_guard: { min: 9, max: 19, bottom_y: 7, top_y: 8 }, upper_guard: { min: 9, max: 19, bottom_y: 18, top_y: 19 }, bottom_floor_y: 0, cage: [[12, 10, 12], [16, 15, 16]], functional_lift: false },
};

export default function build(api) {
  const structure = api.createStructure(specification.size);
  const put = (blockX, blockY, blockZ, material, properties) => api.setBlock(structure, [blockX, blockY, blockZ], 'minecraft:' + material, properties);
  const box = (start, finish, material, properties) => api.fill(structure, start, finish, 'minecraft:' + material, properties);
  const brick = (blockX, blockY, blockZ) => {
    const value = (blockX * 17 + blockY * 11 + blockZ * 23) % 31;
    return value < 2 ? 'cracked_stone_bricks' : value === 2 ? 'mossy_stone_bricks' : 'stone_bricks';
  };
  box([0, 0, 0], [28, 23, 28], 'air');
  for (const floorY of [0, 6, 17, 23]) {
    for (let blockX = 0; blockX < 29; blockX++) {
      for (let blockZ = 0; blockZ < 29; blockZ++) {
        const shaftHole = (floorY === 6 || floorY === 17) && blockX >= 10 && blockX <= 18 && blockZ >= 10 && blockZ <= 18;
        if (!shaftHole) put(blockX, floorY, blockZ, (blockX + blockZ) % 5 === 0 ? 'polished_andesite' : brick(blockX, floorY, blockZ));
      }
    }
  }
  for (let blockY = 1; blockY < 23; blockY++) {
    for (let along = 0; along < 29; along++) {
      put(along, blockY, 0, brick(along, blockY, 0));
      put(along, blockY, 28, brick(along, blockY, 28));
      put(0, blockY, along, brick(0, blockY, along));
      put(28, blockY, along, brick(28, blockY, along));
    }
  }
  for (const pillarX of [4, 24]) {
    for (const pillarZ of [4, 24]) {
      box([pillarX - 1, 1, pillarZ - 1], [pillarX + 1, 5, pillarZ + 1], 'deepslate_bricks');
      box([pillarX - 1, 7, pillarZ - 1], [pillarX + 1, 8, pillarZ + 1], 'polished_blackstone_bricks');
      box([pillarX, 9, pillarZ], [pillarX, 20, pillarZ], 'polished_andesite');
      box([pillarX - 1, 21, pillarZ - 1], [pillarX + 1, 22, pillarZ + 1], 'polished_blackstone_bricks');
      put(pillarX, 15, pillarZ, 'chiseled_stone_bricks');
    }
  }
  for (const beamZ of [4, 24]) box([4, 22, beamZ], [24, 22, beamZ], 'polished_blackstone_bricks');
  for (const beamX of [4, 24]) box([beamX, 22, 4], [beamX, 22, 24], 'polished_blackstone_bricks');
  for (const port of specification.ports.filter(port => port.type === 'horizontal')) {
    const northSouth = port.facing === 'north' || port.facing === 'south';
    const border = port.facing === 'north' || port.facing === 'west' ? 0 : 28;
    for (const side of [11, 17]) {
      if (northSouth) box([side, 7, border], [side, 12, border], 'polished_andesite');
      else box([border, 7, side], [border, 12, side], 'polished_andesite');
    }
    if (northSouth) {
      box([11, 12, border], [17, 12, border], 'chiseled_stone_bricks');
      box([12, 7, border], [16, 10, border], 'air');
    } else {
      box([border, 12, 11], [border, 12, 17], 'chiseled_stone_bricks');
      box([border, 7, 12], [border, 10, 16], 'air');
    }
    box(port.aperture[0], port.aperture[1], 'air');
  }
  for (let along = 9; along <= 19; along++) {
    for (const border of [9, 19]) {
      box([along, 1, border], [along, 5, border], brick(along, 2, border));
      box([border, 1, along], [border, 5, along], brick(border, 2, along));
    }
  }
  box([9, 1, 13], [9, 3, 15], 'air');
  box([9, 4, 12], [9, 4, 16], 'chiseled_stone_bricks');
  for (const level of [6, 17]) {
    for (let along = 8; along <= 20; along++) {
      for (const border of [8, 20]) {
        put(along, level, border, along % 2 ? 'yellow_terracotta' : 'black_concrete');
        put(border, level, along, along % 2 ? 'yellow_terracotta' : 'black_concrete');
      }
    }
    for (let along = 9; along <= 19; along++) {
      for (const border of [9, 19]) {
        put(along, level + 1, border, 'polished_blackstone_bricks');
        put(border, level + 1, along, 'polished_blackstone_bricks');
        put(along, level + 2, border, 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' });
        put(border, level + 2, along, 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' });
      }
    }
  }
  for (let blockX = 12; blockX <= 16; blockX++) {
    for (let blockZ = 12; blockZ <= 16; blockZ++) {
      const edge = blockX === 12 || blockX === 16 || blockZ === 12 || blockZ === 16;
      put(blockX, 10, blockZ, edge ? ((blockX + blockZ) % 2 ? 'yellow_terracotta' : 'polished_blackstone_bricks') : 'iron_block');
      if (!edge) continue;
      box([blockX, 11, blockZ], [blockX, 14, blockZ], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' });
      put(blockX, 15, blockZ, 'polished_blackstone_bricks');
    }
  }
  for (const chainX of [12, 16]) {
    for (const chainZ of [12, 16]) box([chainX, 16, chainZ], [chainX, 21, chainZ], 'chain', { axis: 'y', waterlogged: 'false' });
    box([chainX, 22, 11], [chainX, 22, 17], 'polished_blackstone_bricks');
    box([chainX - 1, 20, 13], [chainX + 1, 20, 15], 'waxed_cut_copper');
  }
  box([11, 21, 13], [17, 21, 15], 'polished_blackstone_bricks');
  box([12, 21, 14], [16, 21, 14], 'stripped_spruce_log', { axis: 'x' });
  box([13, 20, 14], [15, 20, 14], 'spruce_planks');
  box([3, 1, 9], [3, 20, 9], 'deepslate_tiles');
  for (let ladderY = 1; ladderY <= 18; ladderY++) put(4, ladderY, 9, 'ladder', { facing: 'east', waterlogged: 'false' });
  for (const floorY of [0, 6, 17]) {
    for (const edgeZ of [8, 10]) {
      box([3, floorY + 1, edgeZ], [5, floorY + 2, edgeZ], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' });
    }
    put(6, floorY, 9, 'yellow_terracotta');
    put(7, floorY, 9, 'sea_lantern');
  }
  const suspendedLamp = (lampX, lampZ) => {
    box([lampX, 15, lampZ], [lampX, 16, lampZ], 'chain', { axis: 'y', waterlogged: 'false' });
    put(lampX, 14, lampZ, 'lantern', { hanging: 'true', waterlogged: 'false' });
  };
  for (const lampX of [4, 24]) for (const lampZ of [10, 18]) suspendedLamp(lampX, lampZ);
  for (const lampX of [10, 18]) for (const lampZ of [4, 24]) suspendedLamp(lampX, lampZ);
  for (const levelY of [0, 17]) for (const lampX of [8, 20]) for (const lampZ of [8, 20]) put(lampX, levelY, lampZ, 'sea_lantern');
  for (const wallX of [1, 27]) for (const wallZ of [7, 21]) {
    box([wallX, 7, wallZ], [wallX, 8, wallZ], 'stripped_spruce_log', { axis: 'y' });
    put(wallX, 9, wallZ, 'spruce_planks');
    put(wallX, 10, wallZ, 'lantern', { hanging: 'false', waterlogged: 'false' });
  }
  for (const stack of [[2, 7, 23], [2, 7, 24], [25, 7, 3], [26, 7, 3], [25, 7, 25]]) {
    put(...stack, 'stripped_spruce_log', { axis: 'y' });
    put(stack[0], 8, stack[2], 'spruce_planks');
  }
  box([24, 7, 9], [25, 8, 10], 'polished_andesite');
  put(24, 9, 9, 'chiseled_polished_blackstone');
  put(25, 9, 9, 'redstone_lamp', { lit: 'false' });
  put(25, 10, 9, 'lever', { face: 'floor', facing: 'west', powered: 'false' });
  for (const plaqueX of [1, 27]) {
    box([plaqueX, 10, 17], [plaqueX, 12, 19], 'polished_blackstone_bricks');
    put(plaqueX, 11, 18, 'chiseled_polished_blackstone');
  }
  return { structure, metadata: { name: 'room', ...specification, elevator_function: 'Static caged lift exhibit. No moving platform, redstone elevator, plugin, command or transport logic.' } };
}
