'use strict';

const palette = {
    black: [30, 33, 38], black_soft: [43, 47, 51],
    white: [224, 225, 217], belly: [104, 109, 109],
    muzzle: [225, 226, 219], face: [29, 32, 37],
    nose: [19, 22, 24], mouth: [30, 28, 30],
    ear: [75, 75, 74], eye: [29, 31, 34],
    red: [130, 43, 44], brass: [151, 124, 70],
    tooth: [214, 210, 188], wood: [81, 61, 44], sock: [35, 38, 41]
};

function mix(first, second, amount) {
    const fraction = Math.max(0, Math.min(1, amount));
    return first.map((value, axis) => value + (second[axis] - value) * fraction);
}

function paint(part, face, column, row, extent, seed) {
    const across = (column + 0.5) / extent[0];
    const down = (row + 0.5) / extent[1];
    const horizontal = part.origin[0] + part.size[0] * (face === 'south' ? 1 - across : across);
    const vertical = part.origin[1] + part.size[1] * (1 - down);
    const grain = ((Math.floor(column / 2) * 13 + Math.floor(row / 3) * 7 + seed * 3) % 11) - 5;
    let material = part.material;
    if (material === 'sock') material = vertical < 9.4 + Math.sin(column * 1.8) * 0.65 ? 'white' : 'black';
    let color = [...palette[material]];
    const softEdge = Math.min(across, 1 - across, down, 1 - down);
    const pale = ['white', 'muzzle', 'tooth'].includes(material);
    const volume = pale ? 0.98 + (1 - down) * 0.025 : 0.93 + Math.min(0.08, softEdge * 0.5) + (1 - down) * 0.035;
    color = color.map(value => value * volume + grain * (material === 'white' || material === 'muzzle' ? 0.7 : 0.5));
    const strand = (column + Math.floor(row / 3) + seed * 2) % 9;
    if (strand === 0 && row % 7 > 1 && row % 7 < 5) color = color.map(value => value + (material === 'black' ? 8 : 3));

    if (material === 'face' && (face === 'north' || face === 'up')) {
        const offset = vertical > 46 ? 0.45 : -0.1;
        const halfWidth = vertical > 47 ? 1.85 : vertical > 44 ? 1.5 : 2.2 + (44 - vertical) * 0.2;
        const edge = halfWidth + ((Math.floor(vertical * 2) % 5) === 0 ? 0.35 : 0);
        if (Math.abs(horizontal - offset) < edge) color = mix([196, 201, 197], [236, 237, 229], 0.6 + grain * 0.035);
        if (Math.abs(horizontal - offset) >= edge && Math.abs(horizontal - offset) < edge + 0.4) color = [91, 98, 99];
    }

    if (material === 'muzzle') {
        if (down > 0.67) color = mix(color, [146, 156, 156], (down - 0.67) * 0.8);
        if ((face === 'east' || face === 'west') && down > 0.36 && down < 0.82 && across > 0.25) {
            if ((column + 2 * row) % 13 === 0) color = mix(color, [85, 94, 96], 0.6);
        }
        if (face === 'up') color = mix(color, [193, 201, 199], 0.2);
    }

    if (material === 'ear') {
        const center = 1 - Math.abs(across * 2 - 1);
        color = mix([42, 46, 49], [99, 100, 96], center * 0.75);
        if ((column + Math.floor(row / 2)) % 4 === 0) color = color.map(value => value - 12);
        if (down > 0.78) color = mix(color, [44, 47, 49], 0.35);
    }

    if (material === 'eye' && face === 'north') {
        const eyeHorizontal = (across - 0.5) / 0.48;
        const eyeVertical = (down - 0.48) / 0.49;
        const radius = eyeHorizontal * eyeHorizontal + eyeVertical * eyeVertical;
        color = [29, 32, 36];
        if (radius < 1 && down > 0.23) color = [134, 140, 134];
        if (radius < 0.65 && down > 0.24) color = [59, 63, 58];
        if (Math.abs(across - 0.51) < 0.22 && down > 0.25 && down < 0.72) color = [14, 18, 19];
        if (across > 0.26 && across < 0.45 && down > 0.3 && down < 0.5) color = [244, 246, 232];
        if (radius > 0.65 && radius < 1.07 && down > 0.53) color = [173, 180, 170];
        if (down < 0.25 || (down < 0.36 && Math.abs(across - 0.5) > 0.33)) color = [25, 28, 32];
    }

    if (material === 'nose') {
        if (face === 'up') color = [48 + grain, 54 + grain, 56 + grain];
        if (face === 'north') {
            color = mix([17, 21, 23], [44, 49, 50], (1 - down) * 0.75);
            if (down < 0.26 && across > 0.22 && across < 0.66) color = [82, 90, 89];
            if (down > 0.39 && down < 0.86 && ((across > 0.12 && across < 0.33) || (across > 0.7 && across < 0.9))) color = [8, 12, 13];
        }
    }
    if (material === 'wood') {
        const streak = (row * 7 + Math.floor(column / 5)) % 9;
        color = streak < 2 ? [51, 39, 30] : [87 + grain, 63 + grain, 43 + grain];
        if (face === 'east' || face === 'west') color = [107, 82, 52];
    }
    if (material === 'brass') color = mix([93, 73, 42], [188, 164, 102], 0.3 + (1 - down) * 0.65);
    if (material === 'red' && down < 0.3) color = [165, 64, 59];
    return color.map(value => Math.max(0, Math.min(255, Math.round(value))));
}

module.exports = { paint };
