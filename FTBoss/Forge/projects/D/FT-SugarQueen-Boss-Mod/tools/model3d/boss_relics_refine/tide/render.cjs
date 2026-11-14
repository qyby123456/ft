const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG, encodePNG } = require('../../png.cjs');

const { render, stamp, label } = require('../../boss_materials_refine/tide/render.cjs');
if (require.main === module) {
  const root = path.resolve(__dirname, '../../../..');
  const assets = path.join(root, 'src/main/resources/assets/ftboss');
  const output = path.join(root, 'art/boss_relics/refine/tide');
  const model = JSON.parse(fs.readFileSync(path.join(assets, 'models/item/boss_relics/tide_pearl_pendant.json'), 'utf8'));
  const texture = decodePNG(path.join(assets, 'textures/item/boss_relics/tide_pearl_pendant.png'));
  fs.mkdirSync(output, { recursive: true });
  const save = (file, canvas) => fs.writeFileSync(path.join(output, file), encodePNG(canvas.w, canvas.h, canvas.d));
  const sheet = new Canvas(1040, 1100);
  sheet.rect(0, 0, sheet.w, sheet.h, 14, 23, 33);
  label(sheet, 'TIDE PEARL PENDANT  FINAL RUNTIME', 32, 22, 3);
  const views = [['FRONT', 0], ['BACK', 180], ['LEFT', 90], ['RIGHT', -90]];
  for (let index = 0; index < views.length; index++) {
    const [name, yaw] = views[index];
    const image = render(model, texture, { size: 480, yaw, pitch: 0 });
    save(name.toLowerCase() + '.png', image);
    const left = 24 + index % 2 * 516;
    const top = 60 + Math.floor(index / 2) * 510;
    stamp(sheet, { w: image.w, h: image.h, data: image.d }, left, top);
    label(sheet, name, left + 16, top + 446, 3);
  }
  save('four_views.png', sheet);
  const detail = new Canvas(1300, 760);
  detail.rect(0, 0, detail.w, detail.h, 14, 23, 33);
  label(detail, 'TIDE PEARL  ENAMEL  BAIL  REVERSE', 30, 22, 3);
  for (const [left, yaw, name] of [[18, -28, 'FRONT DETAIL'], [656, 145, 'REAR DETAIL']]) {
    const image = render(model, texture, { size: 620, yaw, pitch: 16, scale: 39 });
    stamp(detail, { w: image.w, h: image.h, data: image.d }, left, 66);
    label(detail, name, left + 24, 700, 3);
  }
  save('oblique_details.png', detail);
  const icon = decodePNG(path.join(assets, 'textures/item/tide_pearl_pendant.png'));
  const iconSheet = new Canvas(1000, 600);
  iconSheet.rect(0, 0, iconSheet.w, iconSheet.h, 14, 23, 33);
  label(iconSheet, 'RUNTIME ICON AND PEARL PENDANT', 32, 24, 3);
  const modelView = render(model, texture, { size: 480, yaw: -12, pitch: 6 });
  stamp(iconSheet, { w: 480, h: 480, data: modelView.d }, 20, 72);
  stamp(iconSheet, icon, 560, 112, 10);
  stamp(iconSheet, icon, 562, 486);
  const inventory = { w:16,h:16,data:new Uint8Array(16*16*4) };
  for(let pixelY=0;pixelY<16;pixelY++) for(let pixelX=0;pixelX<16;pixelX++) {
    const source=((pixelY*2+1)*icon.w+pixelX*2+1)*4;
    inventory.data.set(icon.data.slice(source,source+4),(pixelY*16+pixelX)*4);
  }
  stamp(iconSheet,inventory,884,488);
  label(iconSheet,'16 PX',884,520,2);
  label(iconSheet, '3D MODEL', 64, 552, 3);
  label(iconSheet, '32 PX', 618, 492, 2);
  label(iconSheet, 'ICON 10X', 560, 552, 3);
  save('icon_comparison.png', iconSheet);
  const materialModel = JSON.parse(fs.readFileSync(path.join(assets,'models/item/boss_materials/tide_material.json'),'utf8'));
  const materialTexture = decodePNG(path.join(assets,'textures/item/boss_materials/tide.png'));
  const paired = new Canvas(1100,650);
  paired.rect(0,0,1100,650,14,23,33);
  label(paired,'SAME TIDE  RAW CRYSTAL TO FINISHED RELIC',26,22,3);
  const rawView=render(materialModel,materialTexture,{size:500,yaw:-20,pitch:12,scale:30});
  const finishedView=render(model,texture,{size:500,yaw:-20,pitch:12,scale:30});
  stamp(paired,{w:500,h:500,data:rawView.d},24,65);
  stamp(paired,{w:500,h:500,data:finishedView.d},574,65);
  label(paired,'TIDE MATERIAL',50,600,3);
  label(paired,'PEARL PENDANT',610,600,3);
  save('material_relic_pair.png',paired);
  console.log(JSON.stringify({ output, views: views.map(view => view[0]), source: 'final runtime JSON and PNG', rotation: true, rescale: true, uvRotation: true, zBuffer: true, nearestSampling: true }));
}
