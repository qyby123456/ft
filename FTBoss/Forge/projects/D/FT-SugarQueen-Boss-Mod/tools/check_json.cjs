/* 语言文件与模型 JSON 的 UTF-8 解析校验(2026-09-12) —— PowerShell 读 UTF-8 会误判, 用 node 才准 */
const fs = require('fs');
const path = require('path');
const A = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'ftboss');

const checks = [
    ['lang/zh_cn.json', (o) => `键数=${Object.keys(o).length}  blooming_thorns="${o['item.ftboss.blooming_thorns']}"`],
    ['lang/en_us.json', (o) => `键数=${Object.keys(o).length}  blooming_thorns="${o['item.ftboss.blooming_thorns']}"`],
    ['models/item/blooming_thorns.json', (o) => `元素=${o.elements.length}  UV最大=${Math.max(...o.elements.flatMap((e) => Object.values(e.faces).map((f) => Math.max(...f.uv))))}  贴图=${o.textures['0']}`],
];

let bad = 0;
for (const [rel, describe] of checks) {
    const p = path.join(A, rel);
    try {
        const o = JSON.parse(fs.readFileSync(p, 'utf8'));
        console.log(`OK   ${rel.padEnd(34)} ${describe(o)}`);
    } catch (e) {
        bad++;
        console.log(`FAIL ${rel}: ${e.message}`);
    }
}
console.log(bad === 0 ? '全部通过' : `${bad} 个文件有问题`);
