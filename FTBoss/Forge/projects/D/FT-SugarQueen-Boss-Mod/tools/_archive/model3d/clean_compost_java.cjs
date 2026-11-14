/* 清掉 CompostBossEntity.java 里的乱码(安全做法):
 *  ① 整体剔除所有非 ASCII 字符 —— 只会影响注释与字符串字面量, 语法绝不会坏
 *  ② 再把几条功能提示改成英文
 */
const fs = require('fs');
const path = require('path');
const p = path.join(process.argv[2], 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java');
let s = fs.readFileSync(p, 'utf8');
const before = (s.match(/[^\x00-\x7F]/g) || []).length;
s = s.replace(/[^\x00-\x7F]/g, '');
console.log(`剔除非 ASCII 字符: ${before} 个`);

/* ② 功能提示改成英文(锚点此时已是纯 ASCII) */
const fixes = [
    [/LOGGER\.info\("\[FTBoss\]\s*",\s*want\);/, 'LOGGER.info("[FTBoss] Compost Superman entered phase {}", want);'],
    [/LOGGER\.warn\("\[FTBoss\]\[\s*\]\s*",\s*this\.cfgCd = -1;/, 'LOGGER.warn("[FTBoss][CompostMan] create:potato_cannon not found, empty hand", this.cfgCd = -1;'],
    [/LOGGER\.info\("\[FTBoss\]\[\s*\]\s*"\);/, 'LOGGER.info("[FTBoss][CompostMan] holding create:potato_cannon");'],
    [/Component\.literal\(\s*"\\u00a7a\\u3010\\u70ab\\u5f69\\u7b54\\u8fa9\\u8d85\\u4eba\\u3011\\u00a77 \\u8fdb\\u5165\\u7b2c " \+ want \+ " \\u9636\\u6bb5"\s*\)/, 'Component.literal("[Compost Superman] Phase " + want + "!")']
];
for (const [re, to] of fixes) {
    if (re.test(s)) { s = s.replace(re, to); console.log('  已替换: ' + to.slice(0, 48) + '...'); }
    else console.log('  未命中(跳过): ' + String(re).slice(0, 42));
}
/* 兜底: 任何残留的空字符串提示, 给个英文 */
s = s.replace(/Component\.literal\(""\)/g, 'Component.literal("[Compost Superman] Phase changed!")');
s = s.replace(/LOGGER\.(info|warn)\(""\)/g, 'LOGGER.$1("[FTBoss] Compost Superman event")');
fs.writeFileSync(p, s);
const left = (s.match(/[^\x00-\x7F]/g) || []).length;
console.log(`写入完成, 剩余非 ASCII: ${left}`);
/* 快速自检: 括号配平 */
const bal = (a, b) => (s.split(a).length - s.split(b).length);
console.log(`括号配平: {} = ${bal('{', '}')}, () = ${bal('(', ')')}`);
