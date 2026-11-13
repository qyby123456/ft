(() => {
    const filesystem = require('fs');
    const path = require('path');
    const root = 'D:/ft工程文件/FT-Dungeon-Loot-Stand-Forge-1.20.1';
    const specification = JSON.parse(filesystem.readFileSync(path.join(root, 'verification/render-request.json'), 'utf8'));
    if (!Project || Project.uuid !== specification.project || Cube.all.length !== 71) throw new Error('Unexpected candidate project');
    const output = path.join(root, 'preview', specification.phase);
    filesystem.mkdirSync(output, {recursive: true});
    const renderer = new THREE.WebGLRenderer({antialias: false, alpha: false, preserveDrawingBuffer: true});
    if ('outputColorSpace' in renderer) renderer.outputColorSpace = THREE.SRGBColorSpace;
    else renderer.outputEncoding = THREE.sRGBEncoding;
    const reports = [];
    const views = {
        front: [0, 0, -80], back: [0, 0, 80], left: [80, 0, 0], right: [-80, 0, 0],
        top: [0, 80, 0], bottom: [0, -80, 0], hero: [-38, 22, -42], rear: [38, 22, 42]
    };
    const render = (name, cameraPosition, display) => {
        const clone = Project.model_3d.clone(true);
        clone.visible = true;
        const helpers = [];
        clone.traverse(node => {if (node.isLine || node.isPoints) helpers.push(node);});
        helpers.forEach(node => node.removeFromParent());
        const scene = new THREE.Scene();
        scene.background = new THREE.Color('#303232');
        if (display) {
            clone.position.set(-8, -8, -8);
            const wrapper = new THREE.Group();
            wrapper.add(clone);
            wrapper.rotation.set(...(display.rotation || [0, 0, 0]).map(value => value * Math.PI / 180), 'XYZ');
            wrapper.position.fromArray(display.translation || [0, 0, 0]);
            wrapper.scale.fromArray(display.scale || [1, 1, 1]);
            scene.add(wrapper);
        } else scene.add(clone);
        scene.updateMatrixWorld(true);
        const center = new THREE.Box3().setFromObject(clone).getCenter(new THREE.Vector3());
        const span = display ? 10 : 15;
        const camera = new THREE.OrthographicCamera(-span, span, span, -span, .1, 400);
        camera.position.fromArray(cameraPosition);
        if (!display) camera.position.add(center);
        if (name === 'top') camera.up.set(0, 0, 1);
        if (name === 'bottom') camera.up.set(0, 0, -1);
        camera.lookAt(...(display ? [0, 0, 0] : center.toArray()));
        renderer.setSize(700, 700);
        renderer.render(scene, camera);
        const filename = path.join(output, name + '.png');
        filesystem.writeFileSync(filename, Buffer.from(renderer.domElement.toDataURL('image/png').split(',')[1], 'base64'));
        reports.push({name, filename, camera: cameraPosition, display: display || null});
    };
    try {
        for (const [name, camera] of Object.entries(views)) render(name, camera);
        for (const [slot, configuration] of Object.entries(Project.display_settings)) render('display_' + slot, [0, 0, 65], configuration.export());
        filesystem.writeFileSync(path.join(output, 'manifest.json'), JSON.stringify({project:Project.uuid, views:reports, background:'#303232', antialias:false, version:Blockbench.version}, null, 2));
        return {images:reports.length, output};
    } finally {
        renderer.dispose();
        renderer.forceContextLoss();
    }
})()
