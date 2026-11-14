const path = require('path');
const { validate } = require('../../boss_materials_refine/tea/verify.cjs');
const { root } = require('../../boss_materials_refine/tea/render.cjs');
validate({
  modelFile: path.join(root, 'src/main/resources/assets/ftboss/models/item/boss_relics/tea_heart_pendant.json'),
  atlasFile: path.join(root, 'src/main/resources/assets/ftboss/textures/item/boss_relics/tea_heart_pendant.png'),
  bbFile: path.join(root, 'art/boss_relics/blockbench/tea_heart_pendant.bbmodel'),
  item: 'tea_heart_pendant',
  output: path.join(root, '_work/boss-relics-refine-20261021/tea/checks.json'),
  iconOptions: { width: 32, height: 32, yaw: -16, pitch: 9, scale: 1.85, center: [8, 8.2, 8] }
});
