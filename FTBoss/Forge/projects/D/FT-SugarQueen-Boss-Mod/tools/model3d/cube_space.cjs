#!/usr/bin/env node
'use strict';

/*
 * cube_space.cjs
 *
 * 几何空间的**唯一**实现：geo 立方体角点 → 立方体自身旋转 → 完整骨骼链。
 *
 * 之前 cubeCorners() 在 check_shadow_struct.cjs / sword_preview.cjs /
 * hand_check.cjs 里各写了一份，且遍历顺序不一致；现在统一到这里，
 * 其它工具只允许 require 本模块，不再复制变换函数。
 *
 * 坐标约定（与 blockbench / GeckoLib 的 geo 一致）：
 *   +X 右, +Y 上, +Z 后（Bedrock；Java 的 +Z 朝向观察者，渲染前整体取反）。
 *   骨骼链作用顺序：**先子骨骼，再逐级回父骨骼**
 *   （点位于子骨骼局部空间，必须先被最近的骨骼变换）。
 *
 *   与 GeckoLib 运行时的具体符号、欧拉组合顺序仍需实机校准。
 */

const fs = require('fs');

const EPS = 1e-9;

// ============================================================
// 基础向量 / 断言
// ============================================================

function fail(message) {
    throw new Error(message);
}

function assert(condition, message) {
    if (!condition) fail(message);
}

function finite(value, label) {
    assert(
        typeof value === 'number' && Number.isFinite(value),
        `${label} 必须是有限数字`
    );

    return value;
}

/** 读三元素向量，缺省时用 fallback；逐元素校验有限性。 */
function vec3(value, label, fallback = [0, 0, 0]) {
    const actual = value === undefined ? fallback : value;

    assert(
        Array.isArray(actual) && actual.length === 3,
        `${label} 必须是长度为 3 的数组`
    );

    return [
        finite(actual[0], `${label}[0]`),
        finite(actual[1], `${label}[1]`),
        finite(actual[2], `${label}[2]`)
    ];
}

function add(a, b) {
    return [a[0] + b[0], a[1] + b[1], a[2] + b[2]];
}

function sub(a, b) {
    return [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
}

function mul(a, b) {
    return [a[0] * b[0], a[1] * b[1], a[2] * b[2]];
}

function scaleVec(a, k) {
    return [a[0] * k, a[1] * k, a[2] * k];
}

function rad(degrees) {
    return degrees * Math.PI / 180;
}

function round(value, digits = 4) {
    const k = Math.pow(10, digits);
    return Math.round(value * k) / k;
}

/**
 * 依次绕 X、Y、Z 旋转（每步绕 pivot）。
 * 旋转符号沿用 Blockbench 的立方体/骨骼角点旋转。
 */
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

// ============================================================
// geo 读取
// ============================================================

function geckoRotation(rotation) {
    return [-rotation[0], rotation[1], -rotation[2]].map(value => value === 0 ? 0 : value);
}

function geckoNativeRotation(rotation) {
    return [-rotation[0], -rotation[1], rotation[2]].map(value => value === 0 ? 0 : value);
}

function geckoNativePosition(position) {
    return [-position[0], position[1], position[2]].map(value => value === 0 ? 0 : value);
}

function loadGeo(file, options = {}) {
    assert(fs.existsSync(file), `geo 文件不存在：${file}`);

    let json;

    try {
        json = JSON.parse(fs.readFileSync(file, 'utf8'));
    } catch (error) {
        fail(`geo JSON 解析失败：${file}\n${error.message}`);
    }

    const geometry = json['minecraft:geometry'];

    assert(
        Array.isArray(geometry) && geometry.length > 0,
        `${file} 缺少 minecraft:geometry`
    );

    const g = geometry[0];
    const bones = g.bones || [];

    assert(bones.length > 0, `${file} 没有骨骼`);

    const byName = new Map();

    for (const bone of bones) {
        assert(
            typeof bone.name === 'string' && bone.name.length > 0,
            `${file} 存在无名骨骼`
        );

        assert(
            !byName.has(bone.name),
            `${file} 存在重复骨骼：${bone.name}`
        );

        byName.set(bone.name, bone);
    }

    for (const bone of bones) {
        assert(
            bone.parent === undefined ||
            bone.parent === null ||
            byName.has(bone.parent),
            `${file} 骨骼 ${bone.name} 引用了不存在的父级 ${bone.parent}`
        );
    }

    return {
        file,
        geckolib: options.geckolib === true,
        json,
        g,
        bones,
        byName,
        textureWidth: g.description.texture_width,
        textureHeight: g.description.texture_height
    };
}

// ============================================================
// 骨骼链（root → … → child）
// ============================================================

/**
 * 返回 root → parent → child 顺序的骨骼数组。
 * 用 unshift 构建，因此**索引递增方向是父到子**。
 */
function getBoneChain(model, boneName) {
    const chain = [];
    const visited = new Set();

    let current = model.byName.get(boneName);

    assert(current, `骨骼不存在：${boneName}`);

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

        const parent = model.byName.get(current.parent);

        assert(
            parent,
            `骨骼 ${current.name} 引用了不存在的父级 ${current.parent}`
        );

        current = parent;
    }

    return chain;
}

/** 单个骨骼的静止变换（scale → 平移 → 旋转）。 */
function transformBone(point, bone, pose = {}, geckolib = false) {
    const state = pose[bone.name] || {};

    const pivot = vec3(bone.pivot, `${bone.name}.pivot`);

    const baseRotation = vec3(
        bone.rotation,
        `${bone.name}.rotation`
    );

    const animatedRotation = vec3(
        state.rotation,
        `${bone.name}.animation.rotation`
    );

    // 离线预览约定：基础欧拉与动画欧拉分量相加。
    const rotation = [
        baseRotation[0] + animatedRotation[0],
        baseRotation[1] + animatedRotation[1],
        baseRotation[2] + animatedRotation[2]
    ];

    const scaling = vec3(
        state.scale,
        `${bone.name}.animation.scale`,
        [1, 1, 1]
    );

    const position = vec3(
        state.position,
        `${bone.name}.animation.position`
    );

    let result = mul(sub(point, pivot), scaling);
    result = add(result, pivot);
    result = rotateXYZ(result, geckolib ? geckoRotation(rotation) : rotation, pivot);
    result = add(result, position);

    return result;
}

/**
 * 点位于 boneName 的局部空间，需要**先子后父**地逐级变换。
 *
 * chain 是 root → … → child，所以必须从末尾往前遍历。
 * 这一点曾写反（root 先作用），会造成子骨骼偏移被父级再变换一次。
 */
function transformByBoneChain(model, boneName, point, pose = {}) {
    const chain = getBoneChain(model, boneName);

    let result = point;

    for (let i = chain.length - 1; i >= 0; i--) {
        result = transformBone(result, chain[i], pose, model.geckolib);
    }

    return result;
}

// ============================================================
// 立方体角点
// ============================================================

/**
 * 返回一个立方体的 8 个角点（geo 单位，已应用立方体旋转与骨骼链）。
 *
 * 角点索引与 FACE_INDEX 对应：
 *   index = 4 * x + 2 * y + z
 */
function cubeCorners(model, cubeInfo, pose = {}) {
    const cube = cubeInfo.cube;

    const origin = vec3(cube.origin, `${cubeInfo.name}.origin`);
    const size = vec3(cube.size, `${cubeInfo.name}.size`);

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
                    const rotation = vec3(cube.rotation, `${cubeInfo.name}.rotation`);
                    point = rotateXYZ(
                        point,
                        model.geckolib ? geckoRotation(rotation) : rotation,
                        vec3(
                            cube.pivot,
                            `${cubeInfo.name}.pivot`
                        )
                    );
                }

                points.push(
                    transformByBoneChain(
                        model,
                        cubeInfo.bone,
                        point,
                        pose
                    )
                );
            }
        }
    }

    return points;
}

/** 把 geo 的骨骼/立方体展开成 {name, bone, cube} 列表。 */
function listCubes(model) {
    const cubes = [];

    for (const bone of model.bones) {
        for (const [index, cube] of (bone.cubes || []).entries()) {
            cubes.push({
                name: `${bone.name}#${index}`,
                bone: bone.name,
                cube
            });
        }
    }

    return cubes;
}

// ============================================================
// 隐藏骨骼
// ============================================================

/**
 * 骨骼是否属于某个隐藏根（含全部后代）。
 *
 * 名称里带 glow 并不等于该隐藏；必须显式配置。
 */
function isHiddenBone(model, name, hiddenRoots) {
    const visited = new Set();

    let current = name;

    while (current !== undefined && current !== null) {
        assert(!visited.has(current), `骨骼父级循环：${current}`);
        visited.add(current);

        if (hiddenRoots.has(current)) return true;

        const bone = model.byName.get(current);

        assert(bone, `骨骼不存在：${current}`);

        current = bone.parent;
    }

    return false;
}

/** 返回隐藏根及其全部后代的名字集合。 */
function hiddenBoneSet(model, hiddenRoots) {
    const hidden = new Set();

    for (const bone of model.bones) {
        if (isHiddenBone(model, bone.name, hiddenRoots)) {
            hidden.add(bone.name);
        }
    }

    return hidden;
}

// ============================================================
// 单位换算与物品 display
// ============================================================

const DISPLAY_TRANSLATION_UNIT = 1 / 16;

/**
 * 物品 display 变换：先缩放，再绕原点旋转，最后平移（Java 顺序）。
 * 输入输出都是**方块**单位。
 */
function applyDisplayTransform(point, display) {
    const rotation = vec3(
        display && display.rotation,
        'display.rotation'
    );

    const translation = vec3(
        display && display.translation,
        'display.translation'
    );

    const scaling = vec3(
        display && display.scale,
        'display.scale',
        [1, 1, 1]
    );

    let result = mul(point, scaling);
    result = rotateXYZ(result, rotation, [0, 0, 0]);

    return [
        result[0] + translation[0] * DISPLAY_TRANSLATION_UNIT,
        result[1] + translation[1] * DISPLAY_TRANSLATION_UNIT,
        result[2] + translation[2] * DISPLAY_TRANSLATION_UNIT
    ];
}

/**
 * geo 单位 → 方块单位。
 *
 * 这里**不做 Z 取反**：display 的旋转数值是在 geo 所属的模型坐标系里
 * 标定并核对过的，套一层 Z 取反等于额外插入 180° 绕 Y 旋转
 * （实测会把 [0,-90,28] 的刃向从 (0.469,0.883,0) 翻成 (-0.469,0.883,0)）。
 * 需要换到 Java 显示空间时，应当在渲染器里整体做，而不是在这里偷偷翻。
 */
function geoToItemBlocks(point) {
    return [
        point[0] / 16,
        point[1] / 16,
        point[2] / 16
    ];
}

/** Java 显示空间 → 画布空间（镜像 X，见 hand_check.cjs 顶部说明）。 */
function itemBlocksToView(point, mirrorX) {
    return [
        mirrorX ? -point[0] : point[0],
        point[1],
        point[2]
    ];
}

module.exports = {
    geckoRotation,
    geckoNativeRotation,
    geckoNativePosition,
    EPS,
    assert,
    fail,
    finite,
    vec3,
    add,
    sub,
    mul,
    scaleVec,
    rad,
    round,
    rotateXYZ,
    loadGeo,
    getBoneChain,
    transformBone,
    transformByBoneChain,
    cubeCorners,
    listCubes,
    isHiddenBone,
    hiddenBoneSet,
    applyDisplayTransform,
    geoToItemBlocks,
    itemBlocksToView,
    DISPLAY_TRANSLATION_UNIT
};
