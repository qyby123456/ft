(() => {
    const fs = require('fs');
    const root = 'D:/ft工程文件/原创火盆_血棘祭炉_整数方块_20261101';
    const job = JSON.parse(fs.readFileSync(root + '/work/render_job.json', 'utf8'));
    const state = globalThis.__bloodthornInteger20261101;
    fs.mkdirSync(root + '/work/renders', {recursive: true});
    if (!state || !state[job.kind] || !ModelProject.all.includes(state[job.kind])) {
        throw new Error('Requested owned project is not open');
    }
    const previous = Project;
    const project = state[job.kind];
    const results = [];
    const cameras = {
        front: [8, 8, -48], back: [8, 8, 64],
        left: [-48, 8, 8], right: [64, 8, 8],
        top: [8, 64, 8], underside: [8, -48, 8],
        hero: [37, 29, -35], rear_hero: [-25, 26, 40],
    };
    project.select();
    try {
        Texture.all.forEach(texture => { texture.currentFrame = 0; });
        Canvas.updateAllFaces();
        Canvas.updateAllUVs();
        if (job.native_save) {
            fs.writeFileSync(root + '/' + job.native_save, Codecs.project.compile());
            project.saved = true;
        }
        if (job.runtime_save) {
            fs.writeFileSync(root + '/' + job.runtime_save, Codecs.java_block.compile());
        }
        const textureStatus = Texture.all.map(texture => ({name: texture.name, error: texture.error, width: texture.width, height: texture.height, frames: texture.frameCount || 1}));
        if (textureStatus.some(texture => texture.error || !texture.width)) {
            throw new Error('Textures not ready: ' + JSON.stringify(textureStatus));
        }
        const fireCubes = new Set(Cube.all.filter(cube => Object.values(cube.faces).some(face => face.getTexture()?.name.includes('_fire'))).map(cube => cube.uuid));
        const materialCache = new Map();
        const isolatedTextures = [];
        function isolateMaterial(source) {
            if (materialCache.has(source)) return materialCache.get(source);
            const image = source.uniforms?.map?.value || source.map;
            const texture = image?.clone();
            if (texture) {
                texture.encoding = THREE.sRGBEncoding;
                texture.minFilter = THREE.NearestFilter;
                texture.magFilter = THREE.NearestFilter;
                texture.generateMipmaps = false;
                texture.needsUpdate = true;
                isolatedTextures.push(texture);
            }
            const material = new THREE.MeshBasicMaterial({map: texture, color: 0xffffff, vertexColors: true, side: THREE.FrontSide, alphaTest: 0.01});
            materialCache.set(source, material);
            return material;
        }
        const renderer = new THREE.WebGLRenderer({antialias: false, alpha: false, preserveDrawingBuffer: true});
        renderer.setSize(job.size || 640, job.size || 640, false);
        renderer.outputEncoding = THREE.sRGBEncoding;
        renderer.setClearColor(job.background || '#252630', 1);
        for (const frame of job.frames || [0]) {
            for (const view of job.views || ['hero']) {
                const clone = project.model_3d.clone(true);
                clone.visible = true;
                const toRemove = [];
                const geometries = [];
                clone.traverse(node => {
                    if (node.isLine || node.isPoints) {
                        toRemove.push(node);
                    } else if (node.isMesh) {
                        node.visible = true;
                        node.geometry = node.geometry.clone();
                        geometries.push(node.geometry);
                        const isFire = fireCubes.has(node.name);
                        const normals = node.geometry.attributes.normal;
                        const colors = [];
                        for (let index = 0; index < normals.count; index++) {
                            const brightness = isFire ? 1 : 0.75 + normals.getY(index) * 0.25 - Math.abs(normals.getX(index)) * 0.15 + Math.abs(normals.getZ(index)) * 0.05;
                            const linear = Math.pow(brightness, 2.2);
                            colors.push(linear, linear, linear);
                        }
                        node.geometry.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3));
                        node.material = Array.isArray(node.material) ? node.material.map(isolateMaterial) : isolateMaterial(node.material);
                        if (fireCubes.has(node.name) && node.geometry.attributes.uv) {
                            const coordinates = node.geometry.attributes.uv;
                            for (let index = 0; index < coordinates.count; index++) {
                                coordinates.setY(index, coordinates.getY(index) - frame / 16);
                            }
                            coordinates.needsUpdate = true;
                        }
                    }
                });
                toRemove.forEach(node => node.removeFromParent());
                const scene = new THREE.Scene();
                scene.background = new THREE.Color(job.background || '#252630');
                scene.add(clone);
                const span = job.span || (view.includes('hero') ? 13 : 10);
                const camera = new THREE.OrthographicCamera(-span, span, span, -span, 0.1, 350);
                camera.position.fromArray(cameras[view]);
                if (view === 'top' || view === 'underside') camera.up.set(0, 0, -1);
                camera.lookAt(new THREE.Vector3(8, 8, 8));
                camera.updateMatrixWorld(true);
                scene.updateMatrixWorld(true);
                renderer.render(scene, camera);
                const filename = job.prefix + '_' + view + '_f' + String(frame).padStart(2, '0') + '.png';
                const output = root + '/work/renders/' + filename;
                fs.writeFileSync(output, Buffer.from(renderer.domElement.toDataURL('image/png').split(',')[1], 'base64'));
                geometries.forEach(geometry => geometry.dispose());
                results.push(filename);
            }
        }
        renderer.dispose();
        renderer.forceContextLoss();
        materialCache.forEach(material => material.dispose());
        isolatedTextures.forEach(texture => texture.dispose());
        const evidence = {project: project.name, uuid: project.uuid, kind: job.kind, cube_count: Cube.all.length, group_count: Group.all.length, textures: textureStatus, rendered: results, source: 'Blockbench native imported geometry and UV / isolated THREE standard cuboid shading; not Minecraft client'};
        fs.writeFileSync(root + '/work/render_' + job.prefix + '.json', JSON.stringify(evidence, null, 2));
        return evidence;
    } finally {
        if (previous && previous !== project && ModelProject.all.includes(previous)) previous.select();
    }
})()
