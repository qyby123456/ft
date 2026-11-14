'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { render } = require('./render_cleaver_reference.cjs');
const { encodePNG } = require('./png.cjs');
const { PREFIX, sampleAnimation, sampleEffects, SHOWCASE } = require('./cleaver_reference_motion.cjs');

const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const FRAMES = path.join(OUT,'motion-frames');
const FFMPEG = process.env.FFMPEG_PATH || 'C:/Users/YOUR_USERNAME/AppData/Local/oopz/ffmpeg.exe';
const FONT = process.env.CLEAVER_PREVIEW_FONT || 'C:/Windows/Fonts/msyh.ttc';
const animations = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.animation.json'),'utf8')).animations;
const effectData = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.effects.json'),'utf8'));
const clip = animations[PREFIX+'blood_showcase'];
const effectClip = effectData.clips[PREFIX+'blood_showcase'];
const FPS = 24;
const WIDTH = 1100;
const HEIGHT = 900;
const count = Math.round(clip.animation_length*FPS);
const frames = Array.from({ length: count },(_,index) => ({ time: index/FPS,
  pose: sampleAnimation(clip,index/FPS),effects: sampleEffects(effectClip,index/FPS) }));
const views = [
  { width: 670,height: 730,yaw: -38,pitch: 8,roll: -20,left: 12,top: 104 },
  { width: 392,height: 730,yaw: 0,pitch: 0,roll: 0,left: 695,top: 104 }
];

function framing(view) {
  const limits = [[Infinity,-Infinity],[Infinity,-Infinity]];
  for (const frame of frames) {
    const measured = render({ ...view,pose: frame.pose,measureOnly: true });
    limits.forEach((interval,axis) => {
      interval[0] = Math.min(interval[0],measured.bounds[axis][0]);
      interval[1] = Math.max(interval[1],measured.bounds[axis][1]);
    });
  }
  return { ...view,scale: Math.min(view.width*0.86/(limits[0][1]-limits[0][0]),view.height*0.89/(limits[1][1]-limits[1][0])),
    centerX: (limits[0][0]+limits[0][1])/2,centerY: (limits[1][0]+limits[1][1])/2 };
}

function run(args) {
  const result = spawnSync(FFMPEG,['-hide_banner','-loglevel','error','-y',...args],{ encoding: 'utf8',maxBuffer: 16*1024*1024,windowsHide: true });
  if (result.error) throw result.error;
  assert.equal(result.status,0,result.stderr || 'ffmpeg failed');
}

fs.mkdirSync(FRAMES,{ recursive: true });
assert.ok(fs.existsSync(FFMPEG),'Set FFMPEG_PATH to an ffmpeg executable');
assert.ok(fs.existsSync(FONT),'Set CLEAVER_PREVIEW_FONT to a CJK font');
const cameras = views.map(framing);
const stills = [
  { name: 'normal',time: 0 }, { name: 'unfold',time: 1.28 },
  { name: 'heartbeat',time: 2.30 }, { name: 'windup',time: 4.68 },
  { name: 'impact',time: 4.78 }, { name: 'recover',time: 5.20 },
  { name: 'cooldown',time: 8.50 }, { name: 'closed',time: 9.50 }
];
for (const still of stills) {
  render({ ...cameras[0],name: 'motion-'+still.name,pose: sampleAnimation(clip,still.time),effects: sampleEffects(effectClip,still.time) });
}
if (process.argv.includes('--stills')) process.exit(0);

if (!process.argv.includes('--encode-only')) {
  for (const [index,frame] of frames.entries()) {
    const output = Buffer.alloc(WIDTH*HEIGHT*4);
    for (let pixel = 0; pixel < WIDTH*HEIGHT; pixel++) {
      const offset = pixel*4;
      output[offset] = 24;
      output[offset+1] = 29;
      output[offset+2] = 39;
      output[offset+3] = 255;
    }
    for (const camera of cameras) {
      const image = render({ ...camera,pose: frame.pose,effects: frame.effects });
      for (const axis of [0,1]) {
        const dimension = axis === 0 ? camera.width : camera.height;
        const center = axis === 0 ? camera.centerX : camera.centerY;
        assert.ok(image.bounds[axis].every(value => Math.abs((value-center)*camera.scale) < dimension/2-2),'Animated geometry clipped by frame');
      }
      for (let row = 0; row < image.height; row++) {
        image.buffer.copy(output,((camera.top+row)*WIDTH+camera.left)*4,row*image.width*4,(row+1)*image.width*4);
      }
    }
    fs.writeFileSync(path.join(FRAMES,`frame-${String(index).padStart(4,'0')}.png`),encodePNG(WIDTH,HEIGHT,output));
    if (index % 24 === 0) console.log(`Rendered ${index}/${count} frames`);
  }
}

const font = FONT.replaceAll('\\','/').replaceAll(':','\\:');
const text = (label,left,top,size,color = '0xe6e9f0',enable = null) =>
  `drawtext=fontfile='${font}':text='${label}':x=${left}:y=${top}:fontsize=${size}:fontcolor=${color}`+(enable ? `:enable='${enable}'` : '');
const filters = [
  text('黑切 / 血怒武器动画',30,20,32),
  text('固定镜头 · 真实骨骼关键帧 · 无音轨',32,66,18,'0x9da4b3'),
  text('立体视角',300,106,20),text('正面校验',850,106,20),
  text('离线骨骼动画预览；发光为分层模拟，非游戏截图',30,868,16,'0x9da4b3')
];
const labels = ['常态 / 闭合','启怒 / 核心蓄能，四翼依次展开','血怒维持 / 双拍脉动，刃翼轻微回压',
  '血怒重劈 / 蓄势、释放、回震','回收 / 重心回稳','血怒维持 / 双拍脉动，刃翼轻微回压',
  '退怒 / 刃光熄灭，四翼缓慢合拢','常态 / 恢复闭合'];
let start = 0;
SHOWCASE.forEach((segment,index) => {
  filters.push(text(labels[index],30,834,23,'0xf3c9ce',`gte(t,${start.toFixed(3)})*lt(t,${(start+segment.duration).toFixed(3)})`));
  start += segment.duration;
});
const movie = path.join(OUT,'blood-rage-preview.mp4');
run(['-framerate',String(FPS),'-i',path.join(FRAMES,'frame-%04d.png'),'-frames:v',String(count),'-vf',filters.join(','),
  '-c:v','libx264','-preset','fast','-crf','18','-pix_fmt','yuv420p','-movflags','+faststart','-an',movie]);
run(['-i',movie,'-filter_complex',
  '[0:v]fps=12,scale=880:-1:flags=lanczos,split[original][colors];[colors]palettegen=stats_mode=diff[palette];[original][palette]paletteuse=dither=bayer:bayer_scale=4',
  '-loop','0',path.join(OUT,'blood-rage-preview.gif')]);
run(['-ss','2.30','-i',movie,'-frames:v','1',path.join(OUT,'blood-rage-poster.png')]);
const files = ['black_cleaver.geo.json','black_cleaver.animation.json','black_cleaver.effects.json','black_cleaver_geo.png',...Object.values(effectData.textures)];
const hashes = Object.fromEntries(files.map(name => [name,crypto.createHash('sha256').update(fs.readFileSync(path.join(OUT,name))).digest('hex')]));
const validation = { frames: count,fps: FPS,duration: count/FPS,width: WIDTH,height: HEIGHT,camera: 'fixed for whole sequence',
  clipping: 'all frames fit within both viewports',animation_source: 'sampled exported GeckoLib JSON, not a separate pose approximation',
  geometry_and_animation_sha256: hashes,stills,game_tested: false };
fs.writeFileSync(path.join(OUT,'film-validation.json'),JSON.stringify(validation,null,2)+'\n');
console.log(movie);
