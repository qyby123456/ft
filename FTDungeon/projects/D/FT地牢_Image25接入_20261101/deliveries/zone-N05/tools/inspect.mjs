import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { specification } from '../recipe.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const { NbtFile } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const bytes = await fs.readFile(path.join(root, 'room.nbt'));
const digest = hash(bytes);
const referenceBytes = await fs.readFile(specification.reference);
assert.equal(hash(referenceBytes), specification.reference_sha256);
const batch = path.resolve(specification.reference, '../../../..');
const manifestPath = path.join(batch, 'dungeon-zone-batch-20261103/docs/manifest.json');
const manifest = JSON.parse(await fs.readFile(manifestPath, 'utf8'));
const verifyPath = path.join(batch, 'dungeon-zone-batch-20261103/docs/verify.json');
const verification = JSON.parse(await fs.readFile(verifyPath, 'utf8'));
const referenceRecord = manifest.files.find(record => record.file.endsWith('N05-hub-elevator-lift-hub.png'));
const verifyRecord = verification.records.find(record => record.file.endsWith('N05-hub-elevator-lift-hub.png'));
assert.equal(referenceRecord.sha256, specification.reference_sha256);
assert.equal(verifyRecord.sha256, specification.reference_sha256);
assert(referenceRecord.ok && verifyRecord.ok);
assert.equal(referenceRecord.bytes, referenceBytes.length);
const tag = NbtFile.read(new Uint8Array(bytes)).root.toSimplifiedJson();
assert.deepEqual(tag.size, specification.size);
assert.equal(tag.DataVersion, 3465);
assert.equal(tag.entities.length, 0);
assert.equal(tag.palettes, undefined);
assert.equal(tag.blocks.length, 29 * 24 * 29);
const positions = new Map();
const materials = {};
const forbidden = /:(?:.*command_block|structure_block|structure_void|jigsaw|spawner|tnt|chest|barrel|hopper|dropper|dispenser|water|lava|fire|soul_fire|powder_snow|magma_block|cactus|campfire|soul_campfire)$/;
for (const entry of tag.blocks) {
  const key = entry.pos.join(',');
  assert(!positions.has(key), 'Duplicate: ' + key);
  assert(entry.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < tag.size[axis]));
  assert.equal(entry.nbt, undefined);
  const state = tag.palette[entry.state];
  assert(state.Name.startsWith('minecraft:') && !forbidden.test(state.Name));
  assert(!state.Properties || !('waterlogged' in state.Properties) || state.Properties.waterlogged === 'false');
  positions.set(key, state);
  materials[state.Name] = (materials[state.Name] ?? 0) + 1;
}
const stateAt = (blockX, blockY, blockZ) => positions.get([blockX, blockY, blockZ].join(','));
const nameAt = (...position) => stateAt(...position)?.Name;
const solid = (blockX, blockY, blockZ) => {
  const name = nameAt(blockX, blockY, blockZ);
  return name && !['minecraft:air', 'minecraft:iron_bars', 'minecraft:chain', 'minecraft:ladder', 'minecraft:lantern', 'minecraft:lever'].includes(name);
};
const open = (blockX, blockY, blockZ) => ['minecraft:air', 'minecraft:ladder'].includes(nameAt(blockX, blockY, blockZ));
const body = (blockX, blockY, blockZ) => open(blockX, blockY, blockZ) && open(blockX, blockY + 1, blockZ);
const safe = (blockX, blockY, blockZ) => body(blockX, blockY, blockZ) && (solid(blockX, blockY - 1, blockZ) || nameAt(blockX, blockY, blockZ) === 'minecraft:ladder');
for (const port of specification.ports) {
  assert(safe(...port.feet), 'Unsafe feet: ' + port.id);
  assert(safe(...port.safe_landing), 'Unsafe landing: ' + port.id);
  if (!port.aperture) continue;
  for (let blockX = port.aperture[0][0]; blockX <= port.aperture[1][0]; blockX++) {
    for (let blockY = port.aperture[0][1]; blockY <= port.aperture[1][1]; blockY++) {
      for (let blockZ = port.aperture[0][2]; blockZ <= port.aperture[1][2]; blockZ++) assert.equal(nameAt(blockX, blockY, blockZ), 'minecraft:air');
    }
  }
}
const threeWideRoute = new Set();
const routeBox = (from, to) => {
  for (let blockX = from[0]; blockX <= to[0]; blockX++) {
    for (let blockZ = from[1]; blockZ <= to[1]; blockZ++) {
      assert(solid(blockX, 6, blockZ), 'Route floor');
      for (let height = 7; height <= 9; height++) assert.equal(nameAt(blockX, height, blockZ), 'minecraft:air', 'Route obstruction: ' + [blockX, height, blockZ]);
      threeWideRoute.add([blockX, 7, blockZ].join(','));
    }
  }
};
routeBox([6, 6], [8, 22]); routeBox([20, 6], [22, 22]);
routeBox([6, 6], [22, 8]); routeBox([6, 20], [22, 22]);
routeBox([13, 0], [15, 8]); routeBox([13, 20], [15, 28]);
routeBox([0, 13], [8, 15]); routeBox([20, 13], [28, 15]);
const reachable = new Set(['14,7,0']);
const pending = [[14, 7, 0]];
for (let index = 0; index < pending.length; index++) {
  const current = pending[index];
  const next = [[current[0] + 1, current[1], current[2]], [current[0] - 1, current[1], current[2]], [current[0], current[1], current[2] + 1], [current[0], current[1], current[2] - 1]];
  if (nameAt(...current) === 'minecraft:ladder') next.push([current[0], current[1] + 1, current[2]], [current[0], current[1] - 1, current[2]]);
  for (const candidate of next) {
    const key = candidate.join(',');
    if (!reachable.has(key) && safe(...candidate)) { reachable.add(key); pending.push(candidate); }
  }
}
for (const port of specification.ports) {
  assert(reachable.has(port.feet.join(',')), 'Unreachable port: ' + port.id);
  assert(reachable.has(port.safe_landing.join(',')), 'Unreachable landing: ' + port.id);
}
assert(reachable.has(specification.routes.lower_shaft_landing.join(',')));
for (const levelY of [6, 17]) {
  for (let along = 9; along <= 19; along++) {
    for (const border of [9, 19]) {
      for (const position of [[along, levelY + 1, border], [border, levelY + 1, along]]) assert.equal(nameAt(...position), 'minecraft:polished_blackstone_bricks');
      for (const position of [[along, levelY + 2, border], [border, levelY + 2, along]]) assert.equal(nameAt(...position), 'minecraft:iron_bars');
    }
  }
}
for (let blockX = 12; blockX <= 16; blockX++) for (let blockZ = 12; blockZ <= 16; blockZ++) {
  assert(solid(blockX, 10, blockZ));
  if (blockX === 12 || blockX === 16 || blockZ === 12 || blockZ === 16) {
    for (let cageY = 11; cageY <= 14; cageY++) assert.equal(nameAt(blockX, cageY, blockZ), 'minecraft:iron_bars');
    assert(solid(blockX, 15, blockZ));
  }
}
for (let blockX = 0; blockX < 29; blockX++) for (let blockZ = 0; blockZ < 29; blockZ++) {
  assert(solid(blockX, 0, blockZ), 'Bottom cap');
  assert(solid(blockX, 23, blockZ), 'Roof cap');
}
for (let ladderY = 1; ladderY <= 18; ladderY++) {
  assert.equal(nameAt(4, ladderY, 9), 'minecraft:ladder');
  assert.equal(stateAt(4, ladderY, 9).Properties.facing, 'east');
  assert(solid(3, ladderY, 9));
}
const metadata = {
  ...specification, sha256: digest, data_version: 3465, minecraft: '1.20.1', format: 'gzip vanilla structure, single palette',
  records: tag.blocks.length, non_air_blocks: tag.blocks.length - materials['minecraft:air'], palette_states: tag.palette.length,
  entrance: [14, 7, 0], safe_landing: [14, 7, 4], rotation_pivot: [0, 0, 0],
  vertical_interface_contract: 'V_LOWER/V_UPPER are internal ladder landings. Floor y=0 and roof y=23 remain fully capped. No open top/bottom exterior connector; a future stack adapter requires separate design and QA.',
  elevator_function: 'Static suspended cage and winch. Decorative lever may switch its lamp only. No platform movement or transport logic.',
  reference: { path: specification.reference, sha256: specification.reference_sha256, manifest_path: manifestPath, verify_path: verifyPath, width: 1536, height: 1024, origin: 'image2.5 concept, not a game screenshot' },
  integration: { deployed: false, core_catalog_modified: false, player_save_modified: false, receiver_acceptance: 'PENDING', ready_for_unified_integration: false },
};
const report = { id: 'zone-N05', status: 'PASS', sha256: digest, size: tag.size, records: tag.blocks.length, non_air: metadata.non_air_blocks, palette_states: tag.palette.length, materials,
  checks: { reference_batch_manifest_verify_hash: true, gzip: bytes[0] === 31 && bytes[1] === 139, data_version_3465: true, complete_unique_volume: true, vanilla_only: true, no_entities_block_entities_liquids_or_hazards: true, four_open_3_by_5_ports: true, three_wide_three_high_main_ring_and_arms: true, all_six_ports_and_landings_connected: true, continuous_supported_ladder: true, enclosed_two_high_well_guards_both_levels: true, closed_cage: true, complete_bottom_and_roof_caps: true },
  three_wide_main_route_cells: threeWideRoute.size, reachable_route_graph_cells: reachable.size, static_only: true,
  limits: ['Native registry, support and collisions require the separate Forge report.', 'The vertical docks are internal and capped at the structure boundary.', 'Static cage is not a functioning elevator.'],
};
await fs.mkdir(path.join(root, 'qa'), { recursive: true });
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n');
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n');
await fs.writeFile(path.join(root, 'qa/route-cells.json'), JSON.stringify({ three_wide_main: [...threeWideRoute].map(key => key.split(',').map(Number)), reachable: pending }) + '\n');
await fs.writeFile(path.join(root, 'qa/decoded-nbt.json'), JSON.stringify(tag) + '\n');
console.log(JSON.stringify({ status: report.status, sha256: digest, non_air: report.non_air, records: report.records, main_route: threeWideRoute.size, all_route: reachable.size }));
