// 【打狗棒】/【牧羊哨】物品图标(16x16) + 模型 JSON —— benger(v127) 的两件物品
// 用法: node gen_benger_items.cjs <assets/ftboss 目录>
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const R = process.argv[2] || 'src/main/resources/assets/ftboss';
fs.mkdirSync(path.join(R, 'textures/item'), { recursive: true });
fs.mkdirSync(path.join(R, 'models/item'), { recursive: true });

const write = (rel, txt) => { fs.writeFileSync(path.join(R, rel), txt); console.log('wrote ' + rel); };

/* ---------------- 打狗棒: 竹棍(斜放) + 两道金箍 + 红穗 ---------------- */
{
  const cv = new Canvas(16, 16);
  const BAM = [96, 138, 62], BAM_L = [136, 178, 92], BAM_D = [58, 92, 40];
  const GOLD = [214, 176, 74], GOLD_D = [150, 118, 44];
  const RED = [178, 44, 40], RED_L = [214, 92, 80];
  // 棍身: 从 (2,13) 到 (12,3) 的斜线, 宽 2
  for (let i = 0; i <= 11; i++) {
    const x = 2 + i, y = 13 - i;
    for (let w = 0; w < 2; w++) {
      const px = x, py = y + w;
      if (px < 0 || px > 15 || py < 0 || py > 15) continue;
      cv.set(px, py, BAM[0], BAM[1], BAM[2], 1);
      cv.set(px + 1, py, BAM_L[0], BAM_L[1], BAM_L[2], 1);   // 高光
      cv.set(px, py + 1, BAM_D[0], BAM_D[1], BAM_D[2], 1);   // 暗边
    }
  }
  // 竹节(每 4 格一道深色横纹)
  for (let i = 3; i <= 9; i += 3) {
    const x = 2 + i, y = 13 - i;
    cv.set(x, y, BAM_D[0], BAM_D[1], BAM_D[2], 1);
    cv.set(x + 1, y, BAM_D[0], BAM_D[1], BAM_D[2], 1);
  }
  // 两道金箍
  for (const i of [2, 8]) {
    const x = 2 + i, y = 13 - i;
    for (let w = 0; w < 2; w++) {
      cv.set(x, y + w, GOLD[0], GOLD[1], GOLD[2], 1);
      cv.set(x + 1, y + w, GOLD_D[0], GOLD_D[1], GOLD_D[2], 1);
    }
  }
  // 红穗(棍尾)
  cv.set(1, 14, RED[0], RED[1], RED[2], 1);
  cv.set(2, 14, RED_L[0], RED_L[1], RED_L[2], 1);
  cv.set(1, 13, RED_L[0], RED_L[1], RED_L[2], 1);
  cv.set(3, 15, RED[0], RED[1], RED[2], 1);
  cv.save(path.join(R, 'textures/item/dog_beating_stick.png'));
  console.log('wrote textures/item/dog_beating_stick.png');
  write('models/item/dog_beating_stick.json', JSON.stringify({
    parent: 'minecraft:item/handheld',
    textures: { layer0: 'ftboss:item/dog_beating_stick' }
  }, null, 2) + '\n');
}

/* ---------------- 牧羊哨: 银哨 + 木哨嘴 + 挂环 ---------------- */
{
  const cv = new Canvas(16, 16);
  const SIL = [176, 182, 190], SIL_L = [226, 230, 236], SIL_D = [108, 114, 124];
  const WOOD = [124, 84, 50], WOOD_L = [160, 112, 68];
  const HOLE = [40, 40, 46];
  // 哨身: 椭圆(银)
  for (let y = 5; y <= 11; y++) {
    for (let x = 4; x <= 13; x++) {
      const d = Math.hypot((x - 9) / 4.6, (y - 8) / 2.9);
      if (d > 1.0) continue;
      const edge = d > 0.75;
      const c = edge ? SIL_D : (y <= 7 ? SIL_L : SIL);
      cv.set(x, y, c[0], c[1], c[2], 1);
    }
  }
  // 吹口(木): 左侧伸出一个短管
  for (let x = 1; x <= 4; x++) {
    cv.set(x, 7, WOOD[0], WOOD[1], WOOD[2], 1);
    cv.set(x, 8, WOOD_L[0], WOOD_L[1], WOOD_L[2], 1);
  }
  // 出气孔
  cv.set(11, 8, HOLE[0], HOLE[1], HOLE[2], 1);
  cv.set(12, 8, HOLE[0], HOLE[1], HOLE[2], 1);
  // 挂环(右上)
  cv.set(12, 4, SIL_D[0], SIL_D[1], SIL_D[2], 1);
  cv.set(13, 5, SIL[0], SIL[1], SIL[2], 1);
  cv.set(11, 4, SIL[0], SIL[1], SIL[2], 1);
  cv.save(path.join(R, 'textures/item/benger_whistle.png'));
  console.log('wrote textures/item/benger_whistle.png');
  write('models/item/benger_whistle.json', JSON.stringify({
    parent: 'minecraft:item/handheld',
    textures: { layer0: 'ftboss:item/benger_whistle' }
  }, null, 2) + '\n');
}

/* ---------------- 刷怪蛋: 直接复用原版模板(零新增贴图) ---------------- */
write('models/item/benger_spawn_egg.json', JSON.stringify({
  parent: 'minecraft:item/template_spawn_egg'
}, null, 2) + '\n');
