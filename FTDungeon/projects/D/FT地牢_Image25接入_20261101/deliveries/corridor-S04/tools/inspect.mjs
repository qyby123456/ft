import fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { fileURLToPath, pathToFileURL } from 'node:url';
import assert from 'node:assert/strict';
import { specification } from '../recipe.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const { NbtFile } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const bytes = await fs.readFile(path.join(root, 'room.nbt'));
const digest = createHash('sha256').update(bytes).digest('hex');
const referenceDigest = createHash('sha256').update(await fs.readFile(specification.reference)).digest('hex');
assert.equal(referenceDigest, specification.reference_sha256);
const tag = NbtFile.read(new Uint8Array(bytes)).root.toSimplifiedJson();
assert.deepEqual(tag.size, specification.size);
assert.equal(tag.DataVersion, 3465);
assert.equal(tag.entities.length, 0);
assert.equal(tag.palettes, undefined);
assert.equal(tag.blocks.length, 11 * 9 * 16);
const forbidden = /:(?:command_block|chain_command_block|repeating_command_block|structure_block|structure_void|jigsaw|spawner|tnt|chest|barrel|hopper|dropper|dispenser|water|lava|fire|soul_fire|powder_snow|magma_block|cactus|campfire|soul_campfire)$/;
const positions = new Map();
const materials = {};
for (const entry of tag.blocks) {
  const key = entry.pos.join(',');
  assert(!positions.has(key), `Duplicate coordinate ${key}`);
  assert(entry.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < tag.size[axis]));
  assert.equal(entry.nbt, undefined);
  const state = tag.palette[entry.state];
  assert(state?.Name.startsWith('minecraft:'));
  assert(!forbidden.test(state.Name), `Forbidden material ${state.Name}`);
  positions.set(key, state);
  materials[state.Name] = (materials[state.Name] ?? 0) + 1;
}
const stateAt = (blockX, blockY, blockZ) => positions.get([blockX, blockY, blockZ].join(','));
for (let blockZ = 0; blockZ < 16; blockZ++) {
  for (let blockX = 4; blockX <= 6; blockX++) {
    for (let blockY = 1; blockY <= 3; blockY++) assert.equal(stateAt(blockX, blockY, blockZ).Name, 'minecraft:air');
    assert(!['minecraft:air', 'minecraft:cobweb'].includes(stateAt(blockX, 0, blockZ).Name));
  }
  for (let blockX = 0; blockX < 11; blockX++) assert.notEqual(stateAt(blockX, 8, blockZ).Name, 'minecraft:air');
  for (let blockY = 1; blockY < 8; blockY++) {
    assert.notEqual(stateAt(0, blockY, blockZ).Name, 'minecraft:air');
    assert.notEqual(stateAt(10, blockY, blockZ).Name, 'minecraft:air');
  }
}
for (const entry of tag.blocks) {
  const state = tag.palette[entry.state];
  if (state.Name === 'minecraft:hanging_roots') assert.notEqual(stateAt(entry.pos[0], entry.pos[1] + 1, entry.pos[2]).Name, 'minecraft:air');
  if (state.Name === 'minecraft:red_mushroom' || state.Name === 'minecraft:warped_fungus') assert.equal(stateAt(entry.pos[0], entry.pos[1] - 1, entry.pos[2]).Name, 'minecraft:mycelium');
  if (state.Name === 'minecraft:cobweb') assert(entry.pos[0] < 4 || entry.pos[0] > 6);
}
const metadata = {
  ...specification,
  canonical_id: 'corridor-S04',
  producer_metadata_id: 'S04',
  reserved_intake_slot: 64,
  sha256: digest,
  minecraft: '1.20.1',
  data_version: 3465,
  type: 'corridor',
  length_class: 'short',
  length_blocks: 16,
  axis: 'z',
  clear_path: { x: [4, 6], feet_y: 1, headroom_blocks: 3, z: [0, 15] },
  ports: [
    { id: 'north', feet: specification.entrance, facing: 'north', clear_width: 3, clear_height: 3, floor_y: 0 },
    { id: 'south', feet: specification.exit, facing: 'south', clear_width: 3, clear_height: 3, floor_y: 0 },
  ],
  block_records: tag.blocks.length,
  non_air_blocks: tag.blocks.length - (materials['minecraft:air'] ?? 0),
  palette_states: tag.palette.length,
  architecture: ['Full earth roof and parallel closed side walls', 'Five weathered stone arches; center arch has a chipped crown', 'Four giant red mushrooms, mycelium shelves, hanging roots and off-path cobwebs', 'Cyan glass/sea-lantern recesses and upright white end-rod strips'],
  reference_adaptations: ['The distant closed-looking panel is replaced with an open south arch to preserve a true through corridor', 'Cyan-white lighting uses vanilla sea lanterns, cyan glass and end rods; no custom glowing mushroom or shader'],
  placement_policy: 'Independent authoring asset only. Explicit air is present; integrator must reject occupied/fluid/entity volume before placement. Never blindly clear a world.',
  deployment: { deployed: false, player_save_modified: false, core_catalog_modified: false },
};
const report = {
  id: 'corridor-S04', producer_metadata_id: 'S04', status: 'PASS', sha256: digest, reference_sha256: referenceDigest,
  checks: { gzip_nbt: bytes[0] === 31 && bytes[1] === 139, single_palette: true, data_version_3465: true, reference_hash: true, size_11_9_16: true, complete_unique_volume: true, vanilla_namespaces: true, no_block_entity_payloads: true, no_entities: true, no_inventory_loot_commands_or_fluid: true, three_by_three_dry_straight_route: true, both_ports_open: true, sealed_sides: true, complete_roof: true, plant_and_root_supports: true, cobwebs_off_path: true },
  size: tag.size, records: tag.blocks.length, non_air: metadata.non_air_blocks, palette_states: tag.palette.length,
  materials, limits: ['Native registry/property validation and real-world collision are reported separately in qa/forge-native.json', 'Visual reviews are generated from the final NBT, not the reference image'],
};
await fs.mkdir(path.join(root, 'qa'), { recursive: true });
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n');
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n');
await fs.writeFile(path.join(root, 'qa/decoded-nbt.json'), JSON.stringify(tag, null, 2) + '\n');
console.log(JSON.stringify({ status: report.status, sha256: digest, records: report.records, non_air: report.non_air, palette_states: report.palette_states }));
