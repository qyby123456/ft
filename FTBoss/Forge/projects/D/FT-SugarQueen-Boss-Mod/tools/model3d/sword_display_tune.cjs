#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  sword_display_tune.cjs —— 暗影剑 display 快速试参（**不需要重新构建**）
 *
 *  为什么需要它：
 *    物品模型 JSON 支持游戏内 F3+T 重载资源。所以改 display 只要改这个 JSON、
 *    进游戏按 F3+T 就能看到结果 —— 比"改代码 → gradle build 40s → 重启游戏"快一个量级。
 *    握持位置这种要反复试的东西，必须走这条路。
 *
 *  用法：
 *    node tools/model3d/sword_display_tune.cjs show
 *        打印当前四个槽位的 display，以及相对默认值的差异
 *
 *    node tools/model3d/sword_display_tune.cjs set <槽位> key=value [key=value ...]
 *        改一个槽位。key ∈ rot / trans / scale，值是三个逗号分隔的数字
 *        例：set thirdperson_righthand trans=-1,3.5,0.2 rot=0,-90,45
 *
 *    node tools/model3d/sword_display_tune.cjs apply <预设名>
 *        套用一个预设（见 PRESETS），只动第三/第一人称四个槽位
 *
 *    node tools/model3d/sword_display_tune.cjs reset
 *        还原成备份里的值（第一次改动时自动生成 .bak）
 *
 *    node tools/model3d/sword_display_tune.cjs diff
 *        与备份逐字段对比
 *
 *  改完进游戏按 **F3+T** 重载资源即可看到效果，不用重启、不用重新构建。
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const MODEL = path.join(
    ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'models', 'item', 'shadow_sword.json'
);
const BACKUP = MODEL + '.tune.bak';

const SLOTS = [
    'thirdperson_righthand',
    'thirdperson_lefthand',
    'firstperson_righthand',
    'firstperson_lefthand'
];

/**
 * 预设：只动第三人称右手，其余槽位按镜像对称。
 *
 * 依据（2026-09-15 用户实机反馈"剑往外飘 + 角度不对"）：
 *   · trans.x 是"横move"那一路：把它从 +1 改成 -1，位移方向会翻转 0.125 格 —— 先靠这个
 *     把方向定下来（改一次、F3+T 就能看出来是往里还是往外）。
 *   · rot.z 是刃的倾角：28°（现在）偏立；前身静态版用的是 55°，明显更斜。
 *     先试 45 这一档，确认"变斜"的方向对不对，再往 55 或 35 细调。
 */
const PRESETS = {
    /** ★ 三个平移分量的分工（离线算的，rotation=[0,-90,-20] 时）：
     *    trans.x 增加 → 物品朝 Java [0,0,+1] 移动      = **纯前后**（沿角色朝向）
     *    trans.y 增加 → [0.34,0.94,0]                   = 上下（穿手深浅）
     *    trans.z 增加 → [-0.94,0.34,0]                  = **横向**（靠近/远离身体）
     *  位移量 = 单位数 × scale(0.85) ÷ 16 格 → 半格 = 9.4 单位。
     *  ⚠ 只有"哪个轴管哪件事"是确定的；**符号方向**受手部空间基影响，必须实机确认。 */
    'body-in': {
        thirdperson_righthand: { translation: [9.4, 8.67, 10] },
        thirdperson_lefthand: { translation: [9.4, 8.67, 10] }
    },
    'body-out': {
        thirdperson_righthand: { translation: [9.4, 8.67, -9] },
        thirdperson_lefthand: { translation: [9.4, 8.67, -9] }
    },
    /** "往身后半格"：trans.x。 */
    'back-half': {
        thirdperson_righthand: { translation: [9.4, 8.67, 0.5] },
        thirdperson_lefthand: { translation: [9.4, 8.67, 0.5] }
    },
    /** 反方向半格（如果 back-half 看着是往前）。 */
    'fwd-half': {
        thirdperson_righthand: { translation: [-9.4, 8.67, 0.5] },
        thirdperson_lefthand: { translation: [-9.4, 8.67, 0.5] }
    },
    /** 背后 1/4 格（微调档）。 */
    'back-quarter': {
        thirdperson_righthand: { translation: [4.7, 8.67, 0.5] },
        thirdperson_lefthand: { translation: [4.7, 8.67, 0.5] }
    },
    /** 背后 1 格。 */
    'back-full': {
        thirdperson_righthand: { translation: [18.8, 8.67, 0.5] },
        thirdperson_lefthand: { translation: [18.8, 8.67, 0.5] }
    },
    /** 刃朝上（已确认方向）：z 翻号，倾角 55°。 */
    'blade-up': {
        thirdperson_righthand: { rotation: [0, -90, -55], translation: [9.4, 8.67, 0.5] },
        thirdperson_lefthand: { rotation: [0, 90, 55], translation: [9.4, 8.67, 0.5] }
    },
    /** 刃朝上 + 倾角 20°（当前默认）。 */
    'blade-up20': {
        thirdperson_righthand: { rotation: [0, -90, -20], translation: [9.4, 8.67, 0.5] },
        thirdperson_lefthand: { rotation: [0, 90, 20], translation: [9.4, 8.67, 0.5] }
    },
    /** 刃朝上 + 倾角 35°。 */
    'blade-up35': {
        thirdperson_righthand: { rotation: [0, -90, -35], translation: [9.4, 8.67, 0.5] },
        thirdperson_lefthand: { rotation: [0, 90, 35], translation: [9.4, 8.67, 0.5] }
    },
    /** 柄再往上抬 1 单位（穿手更深）。 */
    'grip-deeper': {
        thirdperson_righthand: { translation: [9.4, 9.67, 0.5] },
        thirdperson_lefthand: { translation: [9.4, 9.67, 0.5] }
    },
    /** 柄往下 1 单位（穿手变浅）。 */
    'grip-shallower': {
        thirdperson_righthand: { translation: [9.4, 7.67, 0.5] },
        thirdperson_lefthand: { translation: [9.4, 7.67, 0.5] }
    }
};

function read() {
    return JSON.parse(fs.readFileSync(MODEL, 'utf8'));
}

function write(json) {
    fs.writeFileSync(MODEL, JSON.stringify(json, null, 2) + '\n', 'utf8');
}

function ensureBackup() {
    if (!fs.existsSync(BACKUP)) {
        fs.copyFileSync(MODEL, BACKUP);
        console.log(`已生成备份：${path.relative(ROOT, BACKUP)}`);
    }
}

function vec3(text, label) {
    const parts = String(text).split(',').map(s => Number(s.trim()));

    if (parts.length !== 3 || parts.some(v => !Number.isFinite(v))) {
        throw new Error(`${label} 必须是三个逗号分隔的有限数字，收到：${text}`);
    }

    return parts;
}

function show() {
    const json = read();

    console.log('=== 当前 display ===');

    for (const slot of SLOTS) {
        const d = json.display[slot] || {};
        console.log(`  ${slot}`);
        console.log(`      rotation    ${JSON.stringify(d.rotation)}`);
        console.log(`      translation ${JSON.stringify(d.translation)}`);
        console.log(`      scale       ${JSON.stringify(d.scale)}`);
    }

    if (fs.existsSync(BACKUP)) {
        const bak = JSON.parse(fs.readFileSync(BACKUP, 'utf8'));
        const diff = [];

        for (const slot of SLOTS) {
            for (const key of ['rotation', 'translation', 'scale']) {
                const a = JSON.stringify((json.display[slot] || {})[key]);
                const b = JSON.stringify((bak.display[slot] || {})[key]);

                if (a !== b) diff.push(`  ${slot}.${key}: ${b}  ->  ${a}`);
            }
        }

        console.log('');
        console.log(diff.length === 0
            ? '与备份一致（未改动）'
            : '与备份的差异：\n' + diff.join('\n'));
        console.log(`备份：${path.relative(ROOT, BACKUP)}`);
    } else {
        console.log('\n（还没有备份，第一次改动时会自动生成）');
    }
}

function setSlot(slot, assignments) {
    if (!SLOTS.includes(slot)) {
        throw new Error(`槽位必须是：${SLOTS.join(' / ')}`);
    }

    const json = read();
    ensureBackup();

    json.display[slot] = json.display[slot] || {};

    for (const pair of assignments) {
        const eq = pair.indexOf('=');

        if (eq < 0) throw new Error(`参数要写成 key=value，收到：${pair}`);

        const key = pair.slice(0, eq).trim();
        const value = pair.slice(eq + 1).trim();

        if (key === 'rot' || key === 'rotation') {
            json.display[slot].rotation = vec3(value, 'rotation');
        } else if (key === 'trans' || key === 'translation') {
            json.display[slot].translation = vec3(value, 'translation');
        } else if (key === 'scale') {
            json.display[slot].scale = vec3(value, 'scale');
        } else {
            throw new Error(`未知字段：${key}（可用 rot / trans / scale）`);
        }
    }

    write(json);
    console.log(`已改 ${slot}：`);
    console.log('  rotation    ' + JSON.stringify(json.display[slot].rotation));
    console.log('  translation ' + JSON.stringify(json.display[slot].translation));
    console.log('  scale       ' + JSON.stringify(json.display[slot].scale));
    console.log('\n进游戏按 F3+T 重载资源即可看到效果（不用重建、不用重启）。');
}

function applyPreset(name) {
    const preset = PRESETS[name];

    if (!preset) {
        throw new Error(`没有预设 ${name}；可用：${Object.keys(PRESETS).join(' / ')}`);
    }

    const json = read();
    ensureBackup();

    for (const [slot, patch] of Object.entries(preset)) {
        json.display[slot] = json.display[slot] || {};

        for (const [key, value] of Object.entries(patch)) {
            json.display[slot][key] = value;
        }
    }

    write(json);
    console.log(`已套用预设 ${name}：`);

    for (const slot of Object.keys(preset)) {
        const d = json.display[slot];
        console.log(`  ${slot}  rot ${JSON.stringify(d.rotation)}  trans ${JSON.stringify(d.translation)}`);
    }

    console.log('\n进游戏按 F3+T 重载资源即可看到效果。');
}

function reset() {
    if (!fs.existsSync(BACKUP)) {
        throw new Error('没有备份可还原');
    }

    fs.copyFileSync(BACKUP, MODEL);
    console.log('已还原成备份值。进游戏按 F3+T。');
}

function main() {
    const [cmd, ...args] = process.argv.slice(2);

    if (!cmd || cmd === 'show') return show(), 0;
    if (cmd === 'set') {
        if (args.length < 2) throw new Error('用法：set <槽位> key=value [key=value ...]');
        return setSlot(args[0], args.slice(1)), 0;
    }
    if (cmd === 'apply') {
        if (!args[0]) throw new Error('用法：apply <预设名>');
        return applyPreset(args[0]), 0;
    }
    if (cmd === 'reset') return reset(), 0;
    if (cmd === 'diff') return show(), 0;

    console.log('用法：');
    console.log('  sword_display_tune.cjs show');
    console.log('  sword_display_tune.cjs set <槽位> rot=0,-90,45 trans=-1,3.5,0.2');
    console.log('  sword_display_tune.cjs apply <' + Object.keys(PRESETS).join('|') + '>');
    console.log('  sword_display_tune.cjs reset');
    console.log('');
    console.log('槽位：' + SLOTS.join(' / '));
    console.log('改完进游戏按 F3+T 重载资源，不用重建不用重启。');

    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
