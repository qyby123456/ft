'use strict';
const fs = require('node:fs');
const path = require('node:path');
const { root, raster, sheet } = require('./render.cjs');
const { loadGeo, listCubes, cubeCorners } = require('../../cube_space.cjs');
const { decodePNG } = require('../../png.cjs');

async function main() {
  const resources = path.join(root, 'src/main/resources/assets/ftboss');
  const model = loadGeo(path.join(resources, 'geo/defect_spire.geo.json'));
  for (const bone of model.bones) {
    if (bone.rotation) bone.rotation = bone.rotation.map((value, index) => index === 1 ? value : -value);
    for (const cube of bone.cubes || []) if (cube.rotation) cube.rotation = cube.rotation.map((value, index) => index === 1 ? value : -value);
  }
  const texture = decodePNG(path.join(resources, 'textures/entity/defect_spire_lightning.png'));
  const entries = listCubes(model).map(entry => ({ points: cubeCorners(model, entry), shade: true,
    faces: Object.fromEntries(Object.entries(entry.cube.uv).map(([side, face]) => [side, { uv: [face.uv[0], face.uv[1], face.uv[0] + face.uv_size[0], face.uv[1] + face.uv_size[1]] }])) }));
  const boss = raster(entries, texture, { width: 640, height: 840, center: [0, 24, 0], yaw: -20, pitch: 4, zoom: 15 });
  fs.writeFileSync(path.join(root, 'art/boss_materials/refine/capacitor/references/boss-runtime.png'), boss.png);
  await sheet('reference-study.png', [
    { png: boss.png, label: '实际 Boss · 尖塔机器人' },
    { path: path.join(resources, 'textures/item/charged_core_pendant.png'), label: '现有遗物 · 蓄能核心' },
    { path: path.join(root, 'art/boss_materials/refine/capacitor/references/assets__createaddition__textures__item__capacitor.png'), label: 'Create Addition · 电容参考' },
    { path: path.join(root, 'art/boss_materials/refine/capacitor/references/assets__createaddition__textures__item__copper_spool.png'), label: 'Create Addition · 铜线轴参考' }
  ], 4, 360, 600, '参考研究 · 铜黄护甲 / 铁件 / 青色电芯 / 绝缘端子');
  console.log('Reference study rendered using actual Boss geo and skin; external textures for study only.');
}
main().catch(error => { console.error(error); process.exitCode = 1; });
