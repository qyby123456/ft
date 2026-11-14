const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
for (const file of fs.readdirSync(path.join(assets, 'animations'))) {
  if (!/^(apple_boss|compost_superman|benger|defect_core|wine_barrel_boss|gingerbread_king|rat)\./.test(file)) continue;
  const data = JSON.parse(fs.readFileSync(path.join(assets, 'animations', file), 'utf8'));
  console.log(file);
  for (const [name, animation] of Object.entries(data.animations)) {
    console.log(`  ${name}: ${animation.animation_length * 20} ticks, loop=${animation.loop}`);
  }
}
