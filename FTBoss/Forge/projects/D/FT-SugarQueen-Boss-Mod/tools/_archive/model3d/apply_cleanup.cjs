// 一次性优化补丁: 精确删除无用 import + 删除确认无人引用的死贴图
// 用法: node apply_cleanup.cjs <工程根目录>
const fs = require('fs');
const path = require('path');

const root = process.argv[2] || '.';
const edits = {
  'src/main/java/net/farmingtales/ftboss/client/model/ButcherBossModel.java': [
    'import net.minecraft.client.model.HumanoidModel;'
  ],
  'src/main/java/net/farmingtales/ftboss/entity/SugarQueenEntity.java': [
    'import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;',
    'import net.minecraft.sounds.SoundSource;'
  ],
  'src/main/java/net/farmingtales/ftboss/entity/WineBarrelBossEntity.java': [
    'import net.minecraft.core.particles.DustParticleOptions;',
    'import org.joml.Vector3f;'
  ],
  'src/main/java/net/farmingtales/ftboss/trade/TradeHandler.java': [
    'import net.minecraft.core.registries.Registries;'
  ]
};

let removedLines = 0;
for (const [rel, lines] of Object.entries(edits)) {
  const f = path.join(root, rel);
  const src = fs.readFileSync(f, 'utf8');
  const keep = [];
  for (const line of src.split('\n')) {
    if (lines.includes(line.trim())) { removedLines++; continue; }
    keep.push(line);
  }
  fs.writeFileSync(f, keep.join('\n'));
  console.log(`  import 清理: ${rel} (-${lines.length})`);
}

/* 死贴图: audit.cjs 报告为"无任何引用", 且都是被新版本取代的旧素材 */
const deadTextures = [
  'src/main/resources/assets/ftboss/textures/block/candy_statue_b.png',
  'src/main/resources/assets/ftboss/textures/block/candy_statue_meat.png',
  'src/main/resources/assets/ftboss/textures/entity/rat_boss.png',
  'src/main/resources/assets/ftboss/textures/entity/sugar_queen.png',
  'src/main/resources/assets/ftboss/textures/entity/tea_tree_boss.png'
];
for (const rel of deadTextures) {
  const f = path.join(root, rel);
  if (fs.existsSync(f)) {
    const kb = fs.statSync(f).size;
    fs.unlinkSync(f);
    console.log(`  删除死贴图: ${rel} (${kb} B)`);
  }
}
console.log(`完成: 删除 import 行 ${removedLines} 条, 死贴图 ${deadTextures.length} 张`);
