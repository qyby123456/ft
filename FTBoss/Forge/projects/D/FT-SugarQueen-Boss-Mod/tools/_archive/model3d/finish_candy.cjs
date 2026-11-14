/* 收尾: ① 创造栏移除糖果石像 ② 删除糖果石像的战利品表 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const p = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss/item/FTCreativeTabs.java');
const lines = fs.readFileSync(p, 'utf8').split('\n');
const kept = lines.filter(l => !(l.includes('FTBlocks.STATUE.get()') && l.includes('out.accept')));
fs.writeFileSync(p, kept.join('\n'));
console.log(`创造栏: 删除 ${lines.length - kept.length} 行(糖果石像)`);
const lt = path.join(ROOT, 'src/main/resources/data/ftboss/loot_tables/blocks/statue.json');
if (fs.existsSync(lt)) { fs.unlinkSync(lt); console.log('已删除战利品表 loot_tables/blocks/statue.json'); }
else console.log('战利品表不存在(已删或路径不同)');
/* 复核: 还有哪里引用通用 STATUE */
const J = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss');
function walk(d, out = []) { for (const e of fs.readdirSync(d, { withFileTypes: true })) { const q = path.join(d, e.name); if (e.isDirectory()) walk(q, out); else if (e.name.endsWith('.java')) out.push(q); } return out; }
for (const f of walk(J)) {
    const s = fs.readFileSync(f, 'utf8');
    s.split('\n').forEach((l, i) => {
        if (/FTBlocks\.STATUE\b/.test(l) && !/STATUE_/.test(l) && !/^\s*(\*|\/\/)/.test(l)) {
            console.log(`  仍引用通用 STATUE: ${path.basename(f)}:${i + 1}  ${l.trim().slice(0, 90)}`);
        }
    });
}
console.log('收尾完成');
