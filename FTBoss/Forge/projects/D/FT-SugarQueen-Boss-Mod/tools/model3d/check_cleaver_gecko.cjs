'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const space = require('./cube_space.cjs');
const { PREFIX, sampleAnimation } = require('./cleaver_reference_motion.cjs');
const { FACE_CORNERS } = require('./render_cleaver_reference.cjs');
const ROOT = path.resolve(__dirname,'../..');
const OUT = path.join(ROOT,'art/black_cleaver_reference');
const BUILD = path.join(ROOT,'build/cleaver-gecko-check');
const GAME = path.join(process.env.APPDATA,'.minecraft');
const JAVA = process.env.JAVA_HOME || path.join(GAME,'runtime/java-runtime-gamma-snapshot');
const configuration = JSON.parse(fs.readFileSync(path.join(GAME,'versions/FarmingTales Forge/FarmingTales Forge.json'),'utf8'));
const libraries = configuration.libraries.map(library => library.downloads?.artifact?.path)
  .filter(Boolean).map(relative => path.join(GAME,'libraries',relative)).filter(filename => fs.existsSync(filename));
const client = path.join(GAME,'libraries/net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar');
const gecko = path.join(ROOT,'libs',fs.readdirSync(path.join(ROOT,'libs')).find(name => name.includes('geckolib-forge-1.20.1-4.8.4')));
fs.mkdirSync(BUILD,{ recursive: true });
const stagedGecko = path.join(BUILD,'geckolib-runtime.jar');
fs.copyFileSync(gecko,stagedGecko);
const extracted = spawnSync(path.join(JAVA,'bin/jar.exe'),['xf',stagedGecko,'META-INF/jarjar/mclib-20.jar'],{ cwd: BUILD,windowsHide: true });
assert.equal(extracted.status,0,'GeckoLib nested math dependency extraction failed');
const classpath = [BUILD,stagedGecko,path.join(BUILD,'META-INF/jarjar/mclib-20.jar'),client,...libraries].join(path.delimiter);
const geometry = path.join(OUT,'black_cleaver.geo.json');
const model = space.loadGeo(geometry,{ geckolib: true });
const animations = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.animation.json'),'utf8')).animations;
const samples = [{ name: 'closed',pose: {} },{ name: 'nested_xyz',pose: {
  weapon: { rotation: [23,-7,19] }, axe_head: { rotation: [-9,13,4] }, blade: { rotation: [6,11,-18] }
}}];
for (const name of ['blood_enter','blood_idle','blood_slam','blood_exit']) {
  const clip = animations[PREFIX+name];
  for (let index = 0; index <= 8; index++) {
    const seconds = clip.animation_length*index/8;
    samples.push({ name: `${name}:${seconds}`,pose: sampleAnimation(clip,seconds) });
  }
}
for (const sample of samples) {
  sample.cubes = space.listCubes(model).map(info => ({ bone: info.bone,index: Number(info.name.split('#').at(-1)),
    corners: space.cubeCorners(model,info,sample.pose),
    faces: Object.entries(FACE_CORNERS).map(([direction,indices]) => {
      const face = info.cube.uv[direction];
      const startU = face.uv[0];
      const startV = face.uv[1];
      const endU = startU+face.uv_size[0];
      const endV = startV+face.uv_size[1];
      return { indices,uv: [[startU,startV],[endU,startV],[endU,endV],[startU,endV]] };
    }) }));
  const grip = space.transformByBoneChain(model,'handle',[0,0,0],sample.pose);
  assert.ok(Math.hypot(...grip) < 1e-8,`${sample.name}: animated grip drifts away from the origin`);
}
fs.mkdirSync(BUILD,{ recursive: true });
const expected = path.join(BUILD,'geometry-expected.json');
const itemArgument = process.argv.indexOf('--item');
const reportArgument = process.argv.indexOf('--report');
const itemPath = itemArgument < 0 ? path.join(OUT,'black_cleaver.item.json') : process.argv[itemArgument+1];
const reportPath = reportArgument < 0 ? path.join(OUT,'gecko-validation.json') : process.argv[reportArgument+1];
const display = JSON.parse(fs.readFileSync(itemPath,'utf8')).display;
fs.writeFileSync(expected,JSON.stringify({ samples,display })+'\n');
function run(binary,args) {
  const argumentFile = path.join(BUILD,binary+'.args');
  fs.writeFileSync(argumentFile,args.map(value => JSON.stringify(value.replaceAll('\\','/'))).join('\n')+'\n');
  const result = spawnSync(path.join(JAVA,'bin',binary+'.exe'),['@'+argumentFile],{
    cwd: ROOT,encoding: 'utf8',windowsHide: true,maxBuffer: 8*1024*1024
  });
  if (result.stdout) process.stdout.write(result.stdout);
  if (result.stderr) process.stderr.write(result.stderr);
  if (result.error) throw result.error;
  assert.equal(result.status,0,`${binary} failed`);
}
run('javac',['-J-Duser.language=en','-J-Duser.country=US','-proc:none','-encoding','UTF-8','-cp',classpath,'-d',BUILD,path.join(__dirname,'GeckoGeometryCheck.java')]);
run('java',['-Dfile.encoding=UTF-8','-cp',classpath,'GeckoGeometryCheck',geometry,expected,reportPath,
  ...(process.argv.includes('--dump-uv') ? ['dump-uv'] : process.argv.includes('--audit-hands') ? ['audit-hands']
    : process.argv.includes('--audit-display') ? ['audit-display'] : [])]);
