'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { PREFIX, CLIPS, sampleAnimation, sampleEffects } = require('./cleaver_reference_motion.cjs');
const ROOT = path.resolve(__dirname,'../..');
const OUT = path.join(ROOT,'art/black_cleaver_reference');
const BUILD = path.join(ROOT,'build/cleaver-runtime-check');
const JAVA = process.env.JAVA_HOME || 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot';
const gsonRoot = path.join(process.env.USERPROFILE,'.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson');
function jars(folder) {
  return fs.readdirSync(folder,{ withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? jars(path.join(folder,entry.name)) : entry.name.endsWith('.jar') ? [path.join(folder,entry.name)] : []);
}
const gson = jars(gsonRoot)[0];
assert.ok(gson,'Gson dependency not found');
fs.mkdirSync(BUILD,{ recursive: true });
const animations = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.animation.json'),'utf8')).animations;
const effects = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.effects.json'),'utf8')).clips;
const samples = [];
for (const [name,definition] of Object.entries(CLIPS)) {
  for (let frame = 0; frame <= Math.ceil(definition.duration*60); frame++) {
    const time = Math.min(definition.duration,frame/60);
    const sampled = definition.loop ? time%definition.duration : time;
    samples.push({ clip: name,time,pose: sampleAnimation(animations[PREFIX+name],sampled),effects: sampleEffects(effects[PREFIX+name],sampled) });
  }
}
const expected = path.join(BUILD,'expected.json');
fs.writeFileSync(expected,JSON.stringify({ samples }));
function run(binary,args) {
  const result = spawnSync(path.join(JAVA,'bin',binary+'.exe'),args,{ cwd: ROOT,encoding: 'utf8',windowsHide: true,maxBuffer: 8*1024*1024 });
  if (result.stdout) process.stdout.write(result.stdout);
  if (result.stderr) process.stderr.write(result.stderr);
  if (result.error) throw result.error;
  assert.equal(result.status,0,`${binary} failed`);
}
const source = path.join(ROOT,'src/main/java/net/farmingtales/ftboss/client/render');
run('javac',['-encoding','UTF-8','-cp',gson,'-d',BUILD,path.join(source,'CleaverMotionData.java'),path.join(source,'CleaverMotionPlayback.java'),path.join(__dirname,'CleaverRuntimeCheck.java')]);
run('java',['-cp',BUILD+path.delimiter+gson,'CleaverRuntimeCheck',OUT,expected,path.join(OUT,'runtime-validation.json')]);
