'use strict';

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { statues, assets } = require('./statue_catalog.cjs');
const { createCanvas, loadImage, GlobalFonts } = require(require.resolve('@napi-rs/canvas', {
    paths: [process.env.FT_NODE_MODULES || path.join(__dirname, 'node_modules')]
}));
const output = path.resolve(process.argv[2] || 'art/boss-statues');
const source = path.resolve(process.argv[3] || assets);
const titles = ['糖霜女皇', '屠夫', '沙耶', '酒桶王', '鼠群', '故障机器人', '大便超人', '苹果', '清蒸鱼'];

async function main() {
    fs.mkdirSync(output, { recursive: true });
    GlobalFonts.registerFromPath(path.join(process.env.WINDIR || 'C:/Windows', 'Fonts/msyh.ttc'), 'StatueLabels');
    const sheet = createCanvas(1800, 1560);
    const context = sheet.getContext('2d');
    context.fillStyle = '#ebe8e1';
    context.fillRect(0, 0, sheet.width, sheet.height);
    context.imageSmoothingEnabled = false;
    for (const [index, statue] of statues.entries()) {
        const file = path.join(output, `${statue.id}.png`);
        const rendered = spawnSync(process.execPath, [path.join(__dirname, 'preview_boss_statue.cjs'), file, source, statue.id], { encoding: 'utf8' });
        if (rendered.status !== 0) throw new Error(rendered.stderr || rendered.stdout);
        const image = await loadImage(file);
        const left = index % 3 * 600;
        const top = Math.floor(index / 3) * 520;
        context.drawImage(image, 0, 0, 480, 700, left, top, 300, 437.5);
        context.drawImage(image, 960, 0, 480, 700, left + 300, top, 300, 437.5);
        context.fillStyle = '#303735';
        context.font = '24px StatueLabels';
        context.textAlign = 'center';
        context.fillText(titles[index], left + 300, top + 466);
        context.font = '16px StatueLabels';
        context.fillStyle = '#6b6c66';
        context.fillText('原色 / 白石 · 离线模型预览', left + 300, top + 495);
    }
    const file = path.join(output, 'overview.png');
    fs.writeFileSync(file, sheet.toBuffer('image/png'));
    console.log(file);
}

main().catch(error => { console.error(error); process.exitCode = 1; });
