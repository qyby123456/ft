/* ============================================================================
 *  修 statue_youzi 的手持显示变换(2026-09-12 v55)
 *
 *  问题: 模型是 32 格高的人形, 却沿用了 item/handheld 那套给"扁平刀剑"的
 *        thirdperson rotation [0,-90,35] —— 人形被转了 90°, 手里只能看到它的**侧面**
 *        (4 格厚 × 32 格高的窄条), 看起来就像"贴图被摊开的一条"。
 *  修法: 三个显示上下文都改成**正朝向(rotation 0)**, 于是:
 *        · 人形正面对着持有者的正前方(和 GUI 里一致), 玩家在正前方能看到脸;
 *        · 宽度(含手臂 14 格)完整显示, 不再是一条窄片;
 *        · 尺寸重新标定: 32 格高的模型 -> 第三人称 0.28(≈9 格 / 0.56 方块), GUI 0.42。
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');

const modelPath = process.argv[2] || path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'assets', 'ftboss', 'models', 'item', 'statue_youzi.json');

const DISPLAY = {
    thirdperson_righthand: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.28, 0.28, 0.28] },
    thirdperson_lefthand: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.28, 0.28, 0.28] },
    firstperson_righthand: { rotation: [0, 0, 0], translation: [0.8, 1.0, 0.8], scale: [0.22, 0.22, 0.22] },
    firstperson_lefthand: { rotation: [0, 0, 0], translation: [-0.8, 1.0, 0.8], scale: [0.22, 0.22, 0.22] },
    gui: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.42, 0.42, 0.42] },
    ground: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.35, 0.35, 0.35] },
    fixed: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.45, 0.45, 0.45] },
};

const m = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
m.display = Object.assign({}, m.display, DISPLAY);
fs.writeFileSync(modelPath, JSON.stringify(m, null, 2) + '\n');
console.log('已更新 ' + modelPath);
for (const [k, v] of Object.entries(DISPLAY)) {
    console.log(`  ${k.padEnd(22)} rot=${v.rotation.join(',')} trans=${v.translation.join(',')} scale=${v.scale[0]}`);
}
