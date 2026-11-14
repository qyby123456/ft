#!/usr/bin/env node
'use strict';

/*
 * check_shadow_struct.cjs
 *
 * 暗影剑结构体检器。
 *
 * 检查：
 *   A. 静态 AABB 重叠嫌疑
 *   B. 动画极值下的新增重叠和脱离
 *   C. UV 完全复用与区域重叠
 *   D. 握柄和主体骨骼稳定性
 *   E. 模型、骨骼、动画格式问题
 *
 * 注意：
 *   AABB 重叠只是几何嫌疑，不能等同于实际三角面穿模。
 *   悬浮检查也是近似检查，需要结合 Blockbench 和游戏实测。
 *
 * 用法：
 *   node tools/model3d/check_shadow_struct.cjs
 *
 * 可选：
 *   node tools/model3d/check_shadow_struct.cjs \
 *     --geo path/to/shadow_sword.geo.json \
 *     --anim path/to/shadow_sword.animation.json \
 *     --report art/shadow_structure_report.json
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');

const DEFAULT_GEO = path.join(
    ROOT,
    'src',
    'main',
    'resources',
    'assets',
    'ftboss',
    'geo',
    'shadow_sword.geo.json'
);

const DEFAULT_ANIM = path.join(
    ROOT,
    'src',
    'main',
    'resources',
    'assets',
    'ftboss',
    'animations',
    'shadow_sword.animation.json'
);

const DEFAULT_REPORT = path.join(
    ROOT,
    'art',
    'shadow_structure_report.json'
);

const EPS = 1e-6;
const CONTACT_EPS = 0.08;
const OVERLAP_EPS = 0.05;
const NEW_OVERLAP_VOLUME = 0.30;

const INTENTIONAL_INDEPENDENT_BONES = new Set([
    'charge_orb',
    'charge_core',
    'crystal'
]);

const BODY_BONES = new Set([
    'root',
    'grip_anchor',
    'weapon',
    'handle',
    'blade',
    'guard'
]);

const FACE_NAMES = [
    'north',
    'south',
    'west',
    'east',
    'up',
    'down'
];

// ============================================================
// 基础工具
// ============================================================

function fail(message) {
    throw new Error(message);
}

function assert(condition, message) {
    if (!condition) fail(message);
}

function readJSON(file) {
    assert(fs.existsSync(file), `文件不存在：${file}`);

    try {
        return JSON.parse(fs.readFileSync(file, 'utf8'));
    } catch (error) {
        fail(`JSON 读取失败：${file}\n${error.message}`);
    }
}

function finite(value, label) {
    assert(
        typeof value === 'number' && Number.isFinite(value),
        `${label} 必须是有限数字`
    );

    return value;
}

function vec3(value, label, fallback = [0, 0, 0]) {
    const actual = value === undefined ? fallback : value;

    assert(
        Array.isArray(actual) && actual.length === 3,
        `${label} 必须是长度为 3 的数组`
    );

    return actual.map((v, i) =>
        finite(v, `${label}[${i}]`)
    );
}

function cloneVec(value) {
    return [value[0], value[1], value[2]];
}

function add(a, b) {
    return [
        a[0] + b[0],
        a[1] + b[1],
        a[2] + b[2]
    ];
}

function sub(a, b) {
    return [
        a[0] - b[0],
        a[1] - b[1],
        a[2] - b[2]
    ];
}

function mul(a, b) {
    return [
        a[0] * b[0],
        a[1] * b[1],
        a[2] * b[2]
    ];
}

function rad(degrees) {
    return degrees * Math.PI / 180;
}

function uniqueSorted(values) {
    return [...new Set(values)]
        .filter(Number.isFinite)
        .sort((a, b) => a - b);
}

function shortAnimationName(name) {
    const parts = name.split('.');
    return parts[parts.length - 1];
}

// ============================================================
// 几何变换
// ============================================================

function rotateXYZ(point, rotation, pivot = [0, 0, 0]) {
    let q = sub(point, pivot);

    let c = Math.cos(rad(rotation[0]));
    let s = Math.sin(rad(rotation[0]));

    q = [
        q[0],
        q[1] * c - q[2] * s,
        q[1] * s + q[2] * c
    ];

    c = Math.cos(rad(rotation[1]));
    s = Math.sin(rad(rotation[1]));

    q = [
        q[0] * c + q[2] * s,
        q[1],
        -q[0] * s + q[2] * c
    ];

    c = Math.cos(rad(rotation[2]));
    s = Math.sin(rad(rotation[2]));

    q = [
        q[0] * c - q[1] * s,
        q[0] * s + q[1] * c,
        q[2]
    ];

    return add(q, pivot);
}

function transformPoint(point, bone, pose) {
    const state = pose[bone.name] || {};

    const pivot = vec3(
        bone.pivot,
        `${bone.name}.pivot`
    );

    const baseRotation = vec3(
        bone.rotation,
        `${bone.name}.rotation`
    );

    const animationRotation = vec3(
        state.rotation,
        `${bone.name}.animation.rotation`
    );

    const rotation = [
        baseRotation[0] + animationRotation[0],
        baseRotation[1] + animationRotation[1],
        baseRotation[2] + animationRotation[2]
    ];

    const scale = vec3(
        state.scale,
        `${bone.name}.animation.scale`,
        [1, 1, 1]
    );

    const position = vec3(
        state.position,
        `${bone.name}.animation.position`
    );

    let result = mul(sub(point, pivot), scale);
    result = add(result, pivot);
    result = rotateXYZ(result, rotation, pivot);
    result = add(result, position);

    return result;
}

function getBoneChain(model, boneName) {
    const chain = [];
    const visited = new Set();

    let current = model.byName.get(boneName);

    while (current) {
        assert(
            !visited.has(current.name),
            `骨骼父级循环：${boneName}`
        );

        visited.add(current.name);
        chain.unshift(current);

        if (
            current.parent === undefined ||
            current.parent === null
        ) {
            break;
        }

        current = model.byName.get(current.parent);

        assert(
            current,
            `骨骼 ${boneName} 引用了不存在的父级`
        );
    }

    return chain;
}

function transformByBoneChain(model, boneName, point, pose) {
    const chain = getBoneChain(model, boneName);

    let result = point;

    /*
     * chain 由 getBoneChain() 用 unshift() 构建为 root → parent → child，
     * 而点位于**子骨骼**的局部空间，必须先被最近的骨骼变换，再逐级回父级。
     * 因此这里必须从末尾往前遍历。
     *
     * 之前写成 for (const bone of chain)（root 先作用），
     * 会把子骨骼的偏移再被祖先骨骼变换一次，护爪/晶核/瞳孔这类
     * 有父级偏移的骨骼位置会算错。
     */
    for (let i = chain.length - 1; i >= 0; i--) {
        result = transformPoint(result, chain[i], pose);
    }

    return result;
}

function cubeCorners(model, cubeInfo, pose) {
    const cube = cubeInfo.cube;

    const origin = vec3(
        cube.origin,
        `${cubeInfo.name}.origin`
    );

    const size = vec3(
        cube.size,
        `${cubeInfo.name}.size`
    );

    const inflate = cube.inflate === undefined
        ? 0
        : finite(cube.inflate, `${cubeInfo.name}.inflate`);

    const from = [
        origin[0] - inflate,
        origin[1] - inflate,
        origin[2] - inflate
    ];

    const to = [
        origin[0] + size[0] + inflate,
        origin[1] + size[1] + inflate,
        origin[2] + size[2] + inflate
    ];

    const points = [];

    for (const x of [from[0], to[0]]) {
        for (const y of [from[1], to[1]]) {
            for (const z of [from[2], to[2]]) {
                let point = [x, y, z];

                if (cube.rotation !== undefined) {
                    point = rotateXYZ(
                        point,
                        vec3(
                            cube.rotation,
                            `${cubeInfo.name}.rotation`
                        ),
                        vec3(
                            cube.pivot,
                            `${cubeInfo.name}.pivot`
                        )
                    );
                }

                point = transformByBoneChain(
                    model,
                    cubeInfo.bone,
                    point,
                    pose
                );

                points.push(point);
            }
        }
    }

    return points;
}

function boundsFromPoints(points) {
    assert(points.length > 0, '不能从空点集计算包围盒');

    return [
        Math.min(...points.map(p => p[0])),
        Math.min(...points.map(p => p[1])),
        Math.min(...points.map(p => p[2])),
        Math.max(...points.map(p => p[0])),
        Math.max(...points.map(p => p[1])),
        Math.max(...points.map(p => p[2]))
    ];
}

function volume(box) {
    return Math.max(0, box[3] - box[0]) *
        Math.max(0, box[4] - box[1]) *
        Math.max(0, box[5] - box[2]);
}

function intersection(boxA, boxB) {
    const size = [
        Math.min(boxA[3], boxB[3]) -
            Math.max(boxA[0], boxB[0]),

        Math.min(boxA[4], boxB[4]) -
            Math.max(boxA[1], boxB[1]),

        Math.min(boxA[5], boxB[5]) -
            Math.max(boxA[2], boxB[2])
    ];

    if (size.some(v => v <= OVERLAP_EPS)) {
        return {
            size,
            volume: 0
        };
    }

    return {
        size,
        volume: size[0] * size[1] * size[2]
    };
}

function touches(boxA, boxB) {
    for (let axis = 0; axis < 3; axis++) {
        const overlap = Math.min(
            boxA[axis + 3],
            boxB[axis + 3]
        ) - Math.max(boxA[axis], boxB[axis]);

        if (overlap < -CONTACT_EPS) {
            return false;
        }
    }

    return true;
}

// ============================================================
// Geo 校验与读取
// ============================================================

function loadModel(file) {
    const data = readJSON(file);
    const geometries = data['minecraft:geometry'];

    assert(
        Array.isArray(geometries) &&
        geometries.length === 1,
        '模型必须包含恰好一个 minecraft:geometry'
    );

    const geometry = geometries[0];
    const bones = geometry.bones || [];

    assert(bones.length > 0, '模型没有 bones');

    const byName = new Map();

    for (const bone of bones) {
        assert(
            typeof bone.name === 'string' &&
            bone.name.length > 0,
            '存在无效骨骼名称'
        );

        assert(
            !byName.has(bone.name),
            `重复骨骼名称：${bone.name}`
        );

        byName.set(bone.name, bone);
    }

    for (const bone of bones) {
        getBoneChain(
            { byName },
            bone.name
        );

        vec3(
            bone.pivot,
            `${bone.name}.pivot`
        );

        vec3(
            bone.rotation,
            `${bone.name}.rotation`
        );

        for (const [index, cube] of
            (bone.cubes || []).entries()) {
            validateCube(
                cube,
                `${bone.name}#${index}`,
                geometry.description
            );
        }
    }

    const cubes = [];

    for (const bone of bones) {
        for (const [index, cube] of
            (bone.cubes || []).entries()) {
            cubes.push({
                name: `${bone.name}#${index}`,
                bone: bone.name,
                cube
            });
        }
    }

    return {
        file,
        geometry,
        bones,
        byName,
        cubes
    };
}

function validateCube(cube, label, description = {}) {
    assert(
        Array.isArray(cube.origin) &&
        cube.origin.length === 3,
        `${label}.origin 无效`
    );

    assert(
        Array.isArray(cube.size) &&
        cube.size.length === 3,
        `${label}.size 无效`
    );

    vec3(cube.origin, `${label}.origin`);
    const size = vec3(cube.size, `${label}.size`);

    assert(
        size.every(v => v >= 0),
        `${label}.size 不能为负`
    );

    if (cube.inflate !== undefined) {
        const inflate = finite(
            cube.inflate,
            `${label}.inflate`
        );

        assert(
            size.every(v => v + inflate * 2 >= 0),
            `${label}.inflate 导致几何反转`
        );
    }

    if (cube.rotation !== undefined) {
        vec3(cube.rotation, `${label}.rotation`);
    }

    if (cube.pivot !== undefined) {
        vec3(cube.pivot, `${label}.pivot`);
    }

    assert(
        cube.uv &&
        !Array.isArray(cube.uv) &&
        typeof cube.uv === 'object',
        `${label} 使用了 Box UV；当前检查器要求逐面 UV`
    );

    const textureWidth =
        Number(description.texture_width || Infinity);

    const textureHeight =
        Number(description.texture_height || Infinity);

    for (const face of FACE_NAMES) {
        const uv = cube.uv[face];

        assert(
            uv && typeof uv === 'object',
            `${label}.${face} 缺少 UV`
        );

        assert(
            Array.isArray(uv.uv) &&
            uv.uv.length === 2,
            `${label}.${face}.uv 无效`
        );

        assert(
            Array.isArray(uv.uv_size) &&
            uv.uv_size.length === 2,
            `${label}.${face}.uv_size 无效`
        );

        const [u, v] = vec2(
            uv.uv,
            `${label}.${face}.uv`
        );

        const [width, height] = vec2(
            uv.uv_size,
            `${label}.${face}.uv_size`
        );

        assert(
            Math.min(u, u + width) >= -EPS &&
            Math.max(u, u + width) <= textureWidth + EPS &&
            Math.min(v, v + height) >= -EPS &&
            Math.max(v, v + height) <= textureHeight + EPS,
            `${label}.${face} UV 越界`
        );
    }
}

function vec2(value, label) {
    assert(
        Array.isArray(value) && value.length === 2,
        `${label} 必须是长度为 2 的数组`
    );

    return value.map((v, i) =>
        finite(v, `${label}[${i}]`)
    );
}

function collectCubes(model) {
    return model.cubes;
}

// ============================================================
// 动画采样
// ============================================================

function normaliseAnimationValue(value, label) {
    if (typeof value === 'number') {
        return [value, value, value];
    }

    assert(
        Array.isArray(value) &&
        value.length === 3,
        `${label} 必须是数字或三个数字的数组`
    );

    return value.map((v, i) =>
        finite(v, `${label}[${i}]`)
    );
}

function sampleChannel(channel, time, fallback, label) {
    if (channel === undefined) {
        return cloneVec(fallback);
    }

    if (typeof channel === 'number' ||
        Array.isArray(channel)) {
        return normaliseAnimationValue(channel, label);
    }

    assert(
        channel &&
        typeof channel === 'object',
        `${label} 使用了不支持的格式`
    );

    const keys = Object.entries(channel)
        .map(([key, value]) => {
            const numericTime = Number(key);

            assert(
                Number.isFinite(numericTime),
                `${label} 存在非法关键帧时间：${key}`
            );

            return {
                time: numericTime,
                value: normaliseAnimationValue(
                    value,
                    `${label}@${key}`
                )
            };
        })
        .sort((a, b) => a.time - b.time);

    assert(
        keys.length > 0,
        `${label} 没有关键帧`
    );

    for (let i = 1; i < keys.length; i++) {
        assert(
            keys[i].time !== keys[i - 1].time,
            `${label} 存在重复数值时间`
        );
    }

    if (time <= keys[0].time) {
        return cloneVec(keys[0].value);
    }

    const last = keys[keys.length - 1];

    if (time >= last.time) {
        return cloneVec(last.value);
    }

    for (let i = 1; i < keys.length; i++) {
        const left = keys[i - 1];
        const right = keys[i];

        if (time <= right.time) {
            const factor =
                (time - left.time) /
                (right.time - left.time);

            return left.value.map((value, axis) =>
                value + (
                    right.value[axis] - value
                ) * factor
            );
        }
    }

    fail(`${label} 无法完成关键帧采样`);
}

function samplePose(animation, time, model) {
    const pose = {};

    for (const [boneName, channels] of
        Object.entries(animation.bones || {})) {
        assert(
            model.byName.has(boneName),
            `${animation.name || '动画'} 引用了不存在的骨骼：${boneName}`
        );

        pose[boneName] = {
            rotation: sampleChannel(
                channels.rotation,
                time,
                [0, 0, 0],
                `${boneName}.rotation`
            ),
            position: sampleChannel(
                channels.position,
                time,
                [0, 0, 0],
                `${boneName}.position`
            ),
            scale: sampleChannel(
                channels.scale,
                time,
                [1, 1, 1],
                `${boneName}.scale`
            )
        };
    }

    return pose;
}

function collectAnimationTimes(animation) {
    const times = new Set([
        0,
        Number(animation.animation_length)
    ]);

    for (const channels of
        Object.values(animation.bones || {})) {
        for (const key of [
            'rotation',
            'position',
            'scale'
        ]) {
            const channel = channels[key];

            if (!channel ||
                typeof channel !== 'object' ||
                Array.isArray(channel)) {
                continue;
            }

            const keys = Object.keys(channel)
                .map(Number)
                .filter(Number.isFinite)
                .sort((a, b) => a - b);

            for (const time of keys) {
                times.add(time);
            }

            // 检查插值中点，补充捕捉旋转或缩放过程中的问题。
            for (let i = 1; i < keys.length; i++) {
                times.add(
                    (keys[i - 1] + keys[i]) / 2
                );
            }
        }
    }

    return uniqueSorted([...times]);
}

function loadAnimations(file) {
    const data = readJSON(file);
    const animations = data.animations || {};

    assert(
        animations &&
        typeof animations === 'object',
        '动画文件缺少 animations'
    );

    for (const [name, animation] of
        Object.entries(animations)) {
        assert(
            Number.isFinite(
                Number(animation.animation_length)
            ),
            `${name} 缺少有效 animation_length`
        );

        assert(
            Number(animation.animation_length) >= 0,
            `${name}.animation_length 不能为负`
        );

        for (const [boneName, channels] of
            Object.entries(animation.bones || {})) {
            for (const key of [
                'rotation',
                'position',
                'scale'
            ]) {
                if (channels[key] === undefined) continue;

                // 提前验证，避免检查进行到一半才发现 Molang。
                sampleChannel(
                    channels[key],
                    0,
                    key === 'scale'
                        ? [1, 1, 1]
                        : [0, 0, 0],
                    `${name}.${boneName}.${key}`
                );
            }
        }
    }

    return animations;
}

// ============================================================
// A. 静态 AABB 重叠
// ============================================================

function staticOverlapCheck(model) {
    const pose = {};
    const boxes = model.cubes.map(info => ({
        ...info,
        box: boundsFromPoints(
            cubeCorners(model, info, pose)
        )
    }));

    const results = [];

    for (let i = 0; i < boxes.length; i++) {
        for (let j = i + 1; j < boxes.length; j++) {
            const first = boxes[i];
            const second = boxes[j];

            if (first.bone === second.bone) {
                continue;
            }

            const result = intersection(
                first.box,
                second.box
            );

            if (result.volume > NEW_OVERLAP_VOLUME) {
                results.push({
                    type: 'static_aabb_overlap',
                    severity: 'review',
                    first: first.name,
                    second: second.name,
                    firstBone: first.bone,
                    secondBone: second.bone,
                    volume: Number(result.volume.toFixed(4)),
                    note: '这是 AABB 重叠嫌疑，不等同于实际三角面穿模'
                });
            }
        }
    }

    return results;
}

// ============================================================
// B. 动画检查
// ============================================================

function animatedBoxState(model, animation, time) {
    const pose = samplePose(
        {
            ...animation,
            name: animation.name
        },
        time,
        model
    );

    const boxes = model.cubes.map(info => ({
        ...info,
        box: boundsFromPoints(
            cubeCorners(model, info, pose)
        )
    }));

    return {
        pose,
        boxes
    };
}

function animationOverlapCheck(
    model,
    animationName,
    animation
) {
    const times = collectAnimationTimes(animation);
    const restPose = {};
    const restBoxes = model.cubes.map(info => ({
        ...info,
        box: boundsFromPoints(
            cubeCorners(model, info, restPose)
        )
    }));

    const restByName = new Map(
        restBoxes.map(info => [info.name, info])
    );

    const overlaps = [];
    const detached = [];
    const states = [];

    for (const time of times) {
        const current = animatedBoxState(
            model,
            animation,
            time
        );

        states.push({
            time,
            boxes: current.boxes
        });

        for (let i = 0; i < current.boxes.length; i++) {
            for (let j = i + 1; j < current.boxes.length; j++) {
                const first = current.boxes[i];
                const second = current.boxes[j];

                if (first.bone === second.bone) {
                    continue;
                }

                const currentIntersection = intersection(
                    first.box,
                    second.box
                );

                const firstRest = restByName.get(first.name);
                const secondRest = restByName.get(second.name);

                const restIntersection = intersection(
                    firstRest.box,
                    secondRest.box
                );

                const increase =
                    currentIntersection.volume -
                    restIntersection.volume;

                if (increase > NEW_OVERLAP_VOLUME) {
                    overlaps.push({
                        type: 'animation_aabb_overlap',
                        severity: 'review',
                        animation: animationName,
                        time,
                        first: first.name,
                        second: second.name,
                        volume: Number(
                            currentIntersection.volume.toFixed(4)
                        ),
                        restVolume: Number(
                            restIntersection.volume.toFixed(4)
                        ),
                        increasedBy: Number(
                            increase.toFixed(4)
                        ),
                        note: '这是动画姿态 AABB 重叠嫌疑'
                    });
                }
            }
        }

        for (const currentBox of current.boxes) {
            if (INTENTIONAL_INDEPENDENT_BONES.has(
                currentBox.bone
            )) {
                continue;
            }

            const hasNeighbour = current.boxes.some(
                other => {
                    if (other.name === currentBox.name) {
                        return false;
                    }

                    if (
                        INTENTIONAL_INDEPENDENT_BONES.has(
                            other.bone
                        )
                    ) {
                        return false;
                    }

                    return touches(
                        currentBox.box,
                        other.box
                    );
                }
            );

            if (!hasNeighbour) {
                detached.push({
                    type: 'possible_detachment',
                    severity: 'review',
                    animation: animationName,
                    time,
                    part: currentBox.name,
                    bone: currentBox.bone,
                    note: '这是 AABB 接触检查，末端零件可能是有意独立结构'
                });
            }
        }
    }

    return {
        times,
        overlaps: deduplicateIssues(overlaps),
        detached: deduplicateIssues(detached),
        states
    };
}

function deduplicateIssues(items) {
    const seen = new Set();
    const result = [];

    for (const item of items) {
        const key = JSON.stringify([
            item.type,
            item.animation,
            item.first,
            item.second,
            item.part,
            item.bone,
            item.time
        ]);

        if (seen.has(key)) continue;

        seen.add(key);
        result.push(item);
    }

    return result;
}

// ============================================================
// C. UV 检查
// ============================================================

function uvRectangle(face) {
    const [u, v] = face.uv;
    const [width, height] = face.uv_size;

    return {
        u0: u,
        v0: v,
        u1: u + width,
        v1: v + height,
        minU: Math.min(u, u + width),
        minV: Math.min(v, v + height),
        maxU: Math.max(u, u + width),
        maxV: Math.max(v, v + height),
        mirrorU: width < 0,
        mirrorV: height < 0
    };
}

function uvRectangleKey(cube) {
    return FACE_NAMES.map(face => {
        const uv = uvRectangle(cube.uv[face]);

        return [
            face,
            uv.u0,
            uv.v0,
            uv.u1,
            uv.v1
        ].join(':');
    }).join('|');
}

function uvAreaOverlap(a, b) {
    const width = Math.min(a.maxU, b.maxU) -
        Math.max(a.minU, b.minU);

    const height = Math.min(a.maxV, b.maxV) -
        Math.max(a.minV, b.minV);

    if (width <= 0 || height <= 0) {
        return 0;
    }

    return width * height;
}

function uvCheck(model) {
    const exactGroups = new Map();
    const regionOverlaps = [];

    for (const info of model.cubes) {
        const key = uvRectangleKey(info.cube);

        if (!exactGroups.has(key)) {
            exactGroups.set(key, []);
        }

        exactGroups.get(key).push(info.name);
    }

    const regions = [];

    for (const info of model.cubes) {
        for (const face of FACE_NAMES) {
            regions.push({
                part: info.name,
                face,
                rectangle: uvRectangle(
                    info.cube.uv[face]
                )
            });
        }
    }

    for (let i = 0; i < regions.length; i++) {
        for (let j = i + 1; j < regions.length; j++) {
            const first = regions[i];
            const second = regions[j];

            if (first.part === second.part) {
                continue;
            }

            const area = uvAreaOverlap(
                first.rectangle,
                second.rectangle
            );

            if (area > 0) {
                regionOverlaps.push({
                    first: `${first.part}.${first.face}`,
                    second: `${second.part}.${second.face}`,
                    area,
                    note: 'UV 复用可能是有意设计，需结合贴图和材质判断'
                });
            }
        }
    }

    return {
        exactGroups: [...exactGroups.entries()]
            .filter(([, names]) => names.length > 1)
            .map(([key, names]) => ({
                key,
                parts: names
            })),
        regionOverlaps
    };
}

// ============================================================
// D. 主体和握柄稳定性
// ============================================================

function stabilityCheck(animations) {
    const result = [];

    for (const [name, animation] of
        Object.entries(animations)) {
        const animatedBones = Object.keys(
            animation.bones || {}
        );

        const touchedBody = animatedBones.filter(
            bone => BODY_BONES.has(bone)
        );

        result.push({
            animation: name,
            animatedBones,
            touchedBody,
            stable: touchedBody.length === 0,
            note: touchedBody.length === 0
                ? '未直接修改主体/握柄骨骼'
                : '动画直接修改主体/握柄骨骼，需要检查是否脱手'
        });
    }

    return result;
}

// ============================================================
// 模型质量检查
// ============================================================

function modelWarnings(model) {
    const warnings = [];

    for (const info of model.cubes) {
        const cube = info.cube;

        if (cube.size.some(v => v === 0)) {
            warnings.push({
                type: 'zero_size_cube',
                part: info.name,
                severity: 'warning',
                note: '零尺寸立方体可能只用于挂点或特效，请确认是否有意'
            });
        }

        if (
            cube.inflate !== undefined &&
            cube.inflate < 0
        ) {
            warnings.push({
                type: 'negative_inflate',
                part: info.name,
                severity: 'review',
                inflate: cube.inflate,
                note: '负 inflate 会缩小几何，需要确认不是误填'
            });
        }

        if (
            cube.rotation &&
            cube.rotation.some(v => Math.abs(v) > 45)
        ) {
            warnings.push({
                type: 'large_cube_rotation',
                part: info.name,
                severity: 'review',
                rotation: cube.rotation,
                note: '较大 cube 旋转可能导致预览和实机轮廓差异'
            });
        }
    }

    return warnings;
}

// ============================================================
// CLI
// ============================================================

function parseArgs(argv) {
    const options = {
        geo: DEFAULT_GEO,
        anim: DEFAULT_ANIM,
        report: DEFAULT_REPORT
    };

    for (let i = 0; i < argv.length; i++) {
        const argument = argv[i];

        if (argument === '--geo') {
            options.geo = argv[++i];
        } else if (argument === '--anim') {
            options.anim = argv[++i];
        } else if (argument === '--report') {
            options.report = argv[++i];
        } else if (argument === '--help') {
            printHelp();
            process.exit(0);
        } else {
            fail(`未知参数：${argument}`);
        }
    }

    return options;
}

function printHelp() {
    console.log(`
暗影剑结构检查器

用法：
 node tools/model3d/check_shadow_struct.cjs

参数：
  --geo <file>       Geo 模型文件
 --anim <file>      动画文件
  --report <file>    JSON 报告输出路径
  --help             显示帮助

说明：
  检查结果中的 AABB overlap、detachment、
  z-fighting 嫌疑都需要结合 Blockbench 和游戏实测。
`);
}

function main() {
    const options = parseArgs(
        process.argv.slice(2)
    );

    const model = loadModel(options.geo);
    const animations = loadAnimations(options.anim);

    for (const [name, animation] of
        Object.entries(animations)) {
        animation.name = name;
    }

    const staticOverlaps = staticOverlapCheck(model);
    const animationReports = {};

    for (const [name, animation] of
        Object.entries(animations)) {
        animationReports[name] =
            animationOverlapCheck(
                model,
                name,
                animation
            );
    }

    const uv = uvCheck(model);
    const stability = stabilityCheck(animations);
    const warnings = modelWarnings(model);

    const report = {
        version: 2,
        generatedAt: new Date().toISOString(),
        files: {
            geo: path.resolve(options.geo),
            animation: path.resolve(options.anim)
        },
        model: {
            bones: model.bones.length,
            cubes: model.cubes.length,
            textureWidth:
                model.geometry.description.texture_width,
            textureHeight:
                model.geometry.description.texture_height,
            intentionalIndependentBones:
                [...INTENTIONAL_INDEPENDENT_BONES]
        },
        static: {
            aabbOverlapSuspects: staticOverlaps
        },
        animations: animationReports,
        uv,
        stability,
        warnings,
        limitations: [
            'ABB 重叠不等于实际三角面穿模',
            'AABB 接触不等于实际几何连接',
            'UV 重叠可能是有意复用',
            '未检查 Blockbench 控制器混合',
            '未检查第一人称/第三人称显示变换',
            '未检查 Minecraft 实机透明排序和自发光',
            '未检查 GeckoLib 运行时特有的 Molang'
        ]
    };

    fs.mkdirSync(path.dirname(
        path.resolve(options.report)
    ), { recursive: true });

    fs.writeFileSync(
        options.report,
        JSON.stringify(report, null, 2),
        'utf8'
    );

    console.log('=== 暗影剑结构体检 ===');
    console.log(`骨骼：${model.bones.length}`);
    console.log(`立方体：${model.cubes.length}`);
    console.log(
        `静态 AABB 重叠嫌疑：${staticOverlaps.length}`
    );

    for (const [name, result] of
        Object.entries(animationReports)) {
        console.log(
            `动画 ${shortAnimationName(name)}：` +
            `检查 ${result.times.length} 个姿态，` +
            `新增重叠 ${result.overlaps.length}，` +
            `疑似脱离 ${result.detached.length}`
        );
    }

    console.log(
        `UV 完全复用组：${uv.exactGroups.length}`
    );
    console.log(
        `UV 区域重叠：${uv.regionOverlaps.length}`
    );

    for (const item of stability) {
        console.log(
            `稳定性 ${shortAnimationName(item.animation)}：` +
            ` ${item.stable ? '通过' : '需要检查'}`
        );

        if (!item.stable) {
            console.log(
                `  修改主体骨骼：${item.touchedBody.join(', ')}`
            );
        }
    }

    console.log(`报告：${options.report}`);

    /*
     * 结构检查发现问题时不自动返回失败。
     * 因为 AABB 重叠和悬浮只是嫌疑，不应直接阻断模型修改。
     *
     * 真正的格式错误已在前面抛出并返回退出码 2。
     */
    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
