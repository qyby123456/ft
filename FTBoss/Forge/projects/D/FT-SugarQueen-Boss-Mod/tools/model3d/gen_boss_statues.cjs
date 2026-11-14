'use strict';

const fs = require('fs');
const path = require('path');
const { listCubes, cubeCorners } = require('./cube_space.cjs');
const { decodePNG, encodePNG } = require('./png.cjs');

const { statues, sourceModel, faceMapping, assets } = require('./statue_catalog.cjs');
const output = path.resolve(process.argv[2] || assets);
const selected = process.argv.slice(3);
for (const id of selected) if (!statues.some(statue => statue.id === id)) throw new Error(`Unknown statue: ${id}`);
for (const statue of statues.filter(entry => !selected.length || selected.includes(entry.id))) generate(statue);

function generate(statue) {
    const identifier = `statue_${statue.id}`;
    const faceCorners = {
        north: [2, 6, 4, 0], south: [7, 3, 1, 5],
        east: [6, 7, 5, 4], west: [3, 2, 0, 1],
        up: [3, 7, 6, 2], down: [0, 4, 5, 1]
    };
    const round = value => Number(value.toFixed(7));
    const model = sourceModel(statue, path.join(require('os').tmpdir(), `ftboss-statue-source-${process.pid}`));
    const pose = statue.pose || {};
    const cubes = listCubes(model).map(entry => ({ ...entry, points: cubeCorners(model, entry, pose) }));
    const points = cubes.flatMap(entry => entry.points);
    const minimum = [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis])));
    const maximum = [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis])));
    const scale = Math.min(14.2 / (maximum[0] - minimum[0]), 14.2 / (maximum[2] - minimum[2]),
        26 / (maximum[1] - minimum[1]));
    const center = minimum.map((value, axis) => (value + maximum[axis]) / 2);
    const height = 4 + (maximum[1] - minimum[1]) * scale;
    const itemScale = Math.min(14 / height, 14 / 15);
    const normalize = point => [(point[0] - center[0]) * scale + 8,
        (point[1] - minimum[1]) * scale + 4, (point[2] - center[2]) * scale + 8];
    const texture = decodePNG(path.join(assets, `textures/entity/${statue.texture}.png`));
    const polygons = [];
    
    function addCube(name, corners, mapping, material) {
        for (const [face, indices] of Object.entries(faceCorners)) {
            const region = mapping[face];
            if (!region) continue;
            const start = region.uv;
            const size = region.uv_size;
            const coordinates = [[start[0], start[1]], [start[0] + size[0], start[1]],
                [start[0] + size[0], start[1] + size[1]], [start[0], start[1] + size[1]]];
            polygons.push({ name: `${name}_${face}`, material, vertices: indices.map((index, vertex) => ({
                position: corners[index], uv: coordinates[vertex]
            })) });
        }
    }
    
    for (const entry of cubes) {
        const mapping = Object.fromEntries(Object.entries(faceMapping(entry.cube)).map(([face, region]) => [face, {
            uv: [region.uv[0] / texture.w, region.uv[1] / texture.h],
            uv_size: [region.uv_size[0] / texture.w, region.uv_size[1] / texture.h]
        }]));
        addCube(entry.name.replace('#', '_'), entry.points.map(normalize), mapping, 'body');
    }
    
    function pedestal(name, from, to, material) {
        const corners = [];
        for (const horizontal of [from[0], to[0]]) {
            for (const vertical of [from[1], to[1]]) {
                for (const depth of [from[2], to[2]]) corners.push([horizontal, vertical, depth]);
            }
        }
        const mapping = Object.fromEntries(Object.keys(faceCorners).map(face => [face, { uv: [0, 0], uv_size: [1, 1] }]));
        addCube(name, corners, mapping, material);
    }
    
    pedestal('plinth_lower', [0.5, 0, 0.5], [15.5, 0.8, 15.5], 'trim');
    pedestal('plinth_body', [1.2, 0.8, 1.2], [14.8, 3.4, 14.8], 'base');
    pedestal('plinth_top', [0.7, 3.4, 0.7], [15.3, 4, 15.3], 'trim');
    for (const [row, pattern] of statue.badge.entries()) {
        for (const [column, pixel] of [...pattern].entries()) {
            if (pixel !== '1') continue;
            const horizontal = 6.35 + column * 0.66;
            const vertical = 2.65 - row * 0.65;
            pedestal(`badge_${row}_${column}`, [horizontal, vertical - 0.52, 0.96],
                [horizontal + 0.54, vertical, 1.22], 'badge');
        }
    }
    
    function clip(vertices, height, keepAbove) {
        const clipped = [];
        for (let index = 0; index < vertices.length; index++) {
            const current = vertices[index];
            const next = vertices[(index + 1) % vertices.length];
            const inside = keepAbove ? current.position[1] >= height : current.position[1] <= height;
            const nextInside = keepAbove ? next.position[1] >= height : next.position[1] <= height;
            if (inside) clipped.push(current);
            if (inside !== nextInside) {
                const amount = (height - current.position[1]) / (next.position[1] - current.position[1]);
                const lerp = (first, second) => first.map((value, axis) => value + (second[axis] - value) * amount);
                clipped.push({ position: lerp(current.position, next.position), uv: lerp(current.uv, next.uv) });
            }
        }
        return clipped;
    }
    
    function mesh(part) {
        const lines = [`mtllib ftboss:models/block/${identifier}.mtl`];
        let vertexCount = 0;
        let faceCount = 0;
        for (const polygon of polygons) {
            const vertices = part === 'item' ? polygon.vertices : clip(polygon.vertices, 16, part === 'upper');
            if (vertices.length < 3) continue;
            lines.push(`g ${polygon.name}`, `usemtl ${polygon.material}`);
            for (let index = 1; index < vertices.length - 1; index++) {
                const triangle = [vertices[0], vertices[index], vertices[index + 1]];
                const edges = triangle.slice(1).map(vertex => vertex.position.map((value, axis) => value - triangle[0].position[axis]));
                const normal = [edges[0][1] * edges[1][2] - edges[0][2] * edges[1][1],
                    edges[0][2] * edges[1][0] - edges[0][0] * edges[1][2],
                    edges[0][0] * edges[1][1] - edges[0][1] * edges[1][0]];
                const length = Math.hypot(...normal);
                if (length < 1e-8) continue;
                for (const vertex of triangle) {
                    const position = vertex.position.map((value, axis) => {
                        if (part === 'item') return (value - (axis === 1 ? height / 2 : 8)) * itemScale / 16 + 0.5;
                        return (value - (axis === 1 && part === 'upper' ? 16 : 0)) / 16;
                    });
                    lines.push(`v ${position.map(round).join(' ')}`, `vt ${vertex.uv.map(round).join(' ')}`,
                        `vn ${normal.map(value => round(value / length)).join(' ')}`);
                }
                const indices = [1, 2, 3].map(offset => vertexCount + offset);
                lines.push(`f ${indices.map(index => `${index}/${index}/${index}`).join(' ')}`);
                vertexCount += 3;
                faceCount++;
            }
        }
        console.log(`${part}: ${faceCount} triangles`);
        return lines.join('\n') + '\n';
    }
    
    function write(relative, contents) {
        const destination = path.join(output, relative);
        fs.mkdirSync(path.dirname(destination), { recursive: true });
        fs.writeFileSync(destination, contents);
    }
    
    function writeJson(relative, value) {
        write(relative, JSON.stringify(value, null, 2) + '\n');
    }
    
    write(`textures/block/${identifier}.png`, fs.readFileSync(path.join(assets, `textures/entity/${statue.texture}.png`)));
    const white = Buffer.from(texture.data);
    for (let index = 0; index < white.length; index += 4) {
        const shade = Math.round(196 + (white[index] * 0.2126 + white[index + 1] * 0.7152 + white[index + 2] * 0.0722) * 0.18);
        white[index] = shade;
        white[index + 1] = shade;
        white[index + 2] = shade;
    }
    write(`textures/block/${identifier}_white.png`, encodePNG(texture.w, texture.h, white));
    const gold = Buffer.from(texture.data);
    for (let index = 0; index < gold.length; index += 4) {
        const light = (gold[index] * 0.2126 + gold[index + 1] * 0.7152 + gold[index + 2] * 0.0722) / 255;
        const shine = Math.max(0, Math.min(1, Math.pow(light, 0.82)));
        gold[index] = Math.round(58 + shine * 197);
        gold[index + 1] = Math.round(31 + shine * 176);
        gold[index + 2] = Math.round(7 + shine * 74);
    }
    write(`textures/block/${identifier}_gold.png`, encodePNG(texture.w, texture.h, gold));
    write(`models/block/${identifier}.mtl`, ['body', 'base', 'trim', 'badge'].map(material =>
        `newmtl ${material}\nKa 0 0 0\nKd 1 1 1\nd 1\nmap_Kd #${material}\n`).join('\n'));
    
    function descriptor(part, white, gold = false) {
        const body = `ftboss:block/${identifier}${gold ? '_gold' : white ? '_white' : ''}`;
        return {
            parent: 'minecraft:block/block', loader: 'forge:obj',
            model: `ftboss:models/block/${identifier}_${part}.obj`,
            automatic_culling: false, shade_quads: true, flip_v: false, emissive_ambient: false,
            ambientocclusion: false, render_type: 'minecraft:cutout',
            textures: {
                body,
                base: gold ? 'minecraft:block/deepslate_gold_ore' : 'minecraft:block/smooth_stone',
                trim: gold ? 'minecraft:block/gold_block' : 'minecraft:block/polished_deepslate',
                badge: gold ? 'minecraft:block/raw_gold_block' : 'minecraft:block/gold_block', particle: body
            }
        };
    }
    
    for (const part of ['lower', 'upper', 'item']) write(`models/block/${identifier}_${part}.obj`, mesh(part));
    const variants = {};
    for (const half of ['lower', 'upper']) {
        for (const white of [true, false]) {
            const modelName = `${identifier}_${half}${white ? '' : '_painted'}`;
            writeJson(`models/block/${modelName}.json`, descriptor(half, white));
            variants[`half=${half},white=${white},gold=false`] = { model: `ftboss:block/${modelName}` };
        }
        const goldModel = `${identifier}_${half}_gold`;
        writeJson(`models/block/${goldModel}.json`, descriptor(half, false, true));
        variants[`half=${half},white=false,gold=true`] = { model: `ftboss:block/${goldModel}` };
        variants[`half=${half},white=true,gold=true`] = { model: `ftboss:block/${goldModel}` };
    }
    writeJson(`models/item/${identifier}.json`, descriptor('item', false));
    writeJson(`blockstates/${identifier}.json`, { variants });
    console.log(`${identifier}: ${cubes.length} source cubes preserved, height ${round(4 + (maximum[1] - minimum[1]) * scale)} / 32`);
}
