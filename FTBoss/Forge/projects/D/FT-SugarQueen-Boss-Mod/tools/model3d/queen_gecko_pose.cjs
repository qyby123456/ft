'use strict';

const rotationInModelSpace = vector => [-vector[0], vector[1], -vector[2]];

function geckoModel(model) {
    const bones = structuredClone(model.bones);
    for (const bone of bones) {
        if (bone.rotation) bone.rotation = rotationInModelSpace(bone.rotation);
        for (const cube of bone.cubes || []) {
            if (cube.rotation) cube.rotation = rotationInModelSpace(cube.rotation);
        }
    }
    return { ...model, bones, byName: new Map(bones.map(bone => [bone.name, bone])) };
}

function geckoPose(pose) {
    return Object.fromEntries(Object.entries(pose).map(([owner, tracks]) => [owner, {
        ...tracks,
        ...(tracks.rotation ? { rotation: rotationInModelSpace(tracks.rotation) } : {}),
    }]));
}

function samplePose(animation, time) {
    return Object.fromEntries(Object.entries(animation.bones).map(([owner, tracks]) => [owner,
        Object.fromEntries(Object.entries(tracks).map(([channel, frames]) => {
            const ordered = Object.entries(frames).map(([stamp, vector]) => [Number(stamp), vector]).sort((first, second) => first[0] - second[0]);
            if (time <= ordered[0][0]) return [channel, ordered[0][1]];
            for (let index = 1; index < ordered.length; index++) {
                const previous = ordered[index - 1], next = ordered[index];
                if (time > next[0]) continue;
                const progress = (time - previous[0]) / (next[0] - previous[0]);
                return [channel, previous[1].map((value, axis) => value + (next[1][axis] - value) * progress)];
            }
            return [channel, ordered[ordered.length - 1][1]];
        })),
    ]));
}

function blendPoses(first, second, progress) {
    return Object.fromEntries([...new Set([...Object.keys(first), ...Object.keys(second)])].map(owner => [owner,
        Object.fromEntries([...new Set([...Object.keys(first[owner] || {}), ...Object.keys(second[owner] || {})])].map(channel => {
            const neutral = channel === 'scale' ? [1, 1, 1] : [0, 0, 0];
            const start = first[owner]?.[channel] || neutral, end = second[owner]?.[channel] || neutral;
            return [channel, start.map((value, axis) => value + (end[axis] - value) * progress)];
        })),
    ]));
}

module.exports = { geckoModel, geckoPose, samplePose, blendPoses };
