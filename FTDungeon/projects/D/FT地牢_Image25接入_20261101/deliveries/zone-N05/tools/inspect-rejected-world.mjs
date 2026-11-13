import fs from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL, fileURLToPath } from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const { NbtFile } = await import(pathToFileURL(skill + '/node_modules/@mattzh72/lodestone/lib/index.js').href);
const directory = path.join(root, 'forge/run/n05-isolated-gametest/world/entities');
const items = [];
for (const name of await fs.readdir(directory)) {
  if (!name.endsWith('.mca')) continue;
  const bytes = await fs.readFile(path.join(directory, name));
  for (let index = 0; index < 1024; index++) {
    const sector = bytes.readUIntBE(index * 4, 3);
    if (!sector) continue;
    const offset = sector * 4096;
    const length = bytes.readUInt32BE(offset);
    const data = bytes.subarray(offset + 5, offset + 4 + length);
    const tag = NbtFile.read(new Uint8Array(data)).root.toSimplifiedJson();
    for (const entity of tag.Entities ?? []) if (entity.id === 'minecraft:item') items.push({ file: name, position: entity.Pos, item: entity.Item, age: entity.Age });
  }
}
const report = { purpose: 'Diagnose first rejected GameTest without opening any player world', world: directory, item_entities: items };
await fs.writeFile(path.join(root,'qa/rejected-world-items.json'),JSON.stringify(report,null,2)+'\n');
console.log(JSON.stringify(report,null,2));
