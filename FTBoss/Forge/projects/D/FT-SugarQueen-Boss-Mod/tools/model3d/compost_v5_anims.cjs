'use strict';

module.exports = function makeAnimations() {
  const animations = {};
  const zero = [0,0,0];
  const round = value => Math.round(value*1000)/1000;
  const interpolate = (start,end,amount) => start.map((value,axis) => round(value+(end[axis]-value)*amount));
  function keyframes(stops) {
    const result = {};
    for (let index = 0; index < stops.length-1; index++) {
      const [startTime,start] = stops[index];
      const [endTime,end] = stops[index+1];
      const count = Math.max(1,Math.ceil((endTime-startTime)*24));
      for (let frame = 0; frame < count; frame++) {
        const fraction = frame/count;
        const amount = fraction*fraction*(3-2*fraction);
        result[(startTime+(endTime-startTime)*fraction).toFixed(4)] = interpolate(start,end,amount);
      }
    }
    const [lastTime,last] = stops.at(-1);
    result[lastTime.toFixed(4)] = last;
    return result;
  }
  function animation(name,duration,loop = false) {
    const entry = {loop,animation_length: duration,bones: {}};
    animations[`animation.compost_superman.${name}`] = entry;
    return entry;
  }
  function channel(entry,owner,type,stops) {
    (entry.bones[owner] ||= {})[type] = keyframes(stops);
  }
  function wave(entry,owner,type,base,amplitude,offset = 0,cycles = 1) {
    const duration = entry.animation_length;
    const stops = [];
    for (let sample = 0; sample <= 32; sample++) {
      const angle = sample/32*Math.PI*2*cycles+offset;
      stops.push([duration*sample/32,base.map((value,axis) => round(value+Math.sin(angle)*amplitude[axis]))]);
    }
    channel(entry,owner,type,stops);
  }
  function clothMotion(entry,strength = 1) {
    for (let index = 0; index < 4; index++) {
      wave(entry,`cape${index+1}`,'rotation',[-3,0,0],[strength*(1.6+index*0.8),0.5,0.6],-index*0.6);
    }
  }
  function flies(entry) {
    for (const [index,name] of ['fly1','fly2'].entries()) {
      wave(entry,name,'position',zero,[0.7,1.1,1.3],index*Math.PI);
      for (const [side,suffix] of [[-1,'l'],[1,'r']]) {
        wave(entry,`${name}_wing_${suffix}`,'rotation',zero,[0,0,side*45],0,4);
      }
    }
  }

  const idle = animation('idle',2.4,true);
  wave(idle,'torso','rotation',zero,[1.1,0,0.5]);
  wave(idle,'waist','rotation',zero,[0,0.7,0]);
  wave(idle,'head','rotation',zero,[-1.1,2,-0.5]);
  for (const [side,suffix] of [[-1,'l'],[1,'r']]) {
    wave(idle,`arm_${suffix}`,'rotation',[0,0,side*3],[1.4,0,side*0.7]);
    wave(idle,`forearm_${suffix}`,'rotation',[-5,0,0],[1.3,0,0],side);
  }
  clothMotion(idle);
  flies(idle);

  const walk = animation('walk',0.9,true);
  wave(walk,'pelvis','position',[0,0.4,0],[0,0.4,0],0,2);
  wave(walk,'waist','rotation',[2,0,0],[0,3,1.5]);
  wave(walk,'torso','rotation',[2,0,0],[0,-4,-1]);
  wave(walk,'head','rotation',[-2,0,0],[0,1.5,0]);
  for (const [side,suffix] of [[-1,'l'],[1,'r']]) {
    const phase = side < 0 ? 0 : Math.PI;
    wave(walk,`leg_${suffix}`,'rotation',[-3,0,0],[18,0,0],phase);
    wave(walk,`shin_${suffix}`,'rotation',[11,0,0],[-10,0,0],phase);
    wave(walk,`foot_${suffix}`,'rotation',[-5,0,0],[4,0,0],phase);
    wave(walk,`arm_${suffix}`,'rotation',[0,0,side*4],[-12,0,0],phase);
    wave(walk,`forearm_${suffix}`,'rotation',[-9,0,0],[5,0,0],phase);
  }
  clothMotion(walk,1.5);
  flies(walk);

  const flight = animation('float_move',1.4,true);
  wave(flight,'root','position',[0,1.2,0],[0,0.7,0]);
  wave(flight,'torso','rotation',[9,0,0],[1.5,0,0]);
  wave(flight,'head','rotation',[-7,0,0],[-1,0,0]);
  for (const [side,suffix] of [[-1,'l'],[1,'r']]) {
    wave(flight,`leg_${suffix}`,'rotation',[-15+side*4,0,side*3],[3,0,0],side);
    wave(flight,`shin_${suffix}`,'rotation',[36,0,0],[4,0,0],side);
    wave(flight,`foot_${suffix}`,'rotation',[-12,0,0],[-2,0,0],side);
    wave(flight,`arm_${suffix}`,'rotation',[-22,0,side*12],[2,0,1],side);
    wave(flight,`forearm_${suffix}`,'rotation',[-12,0,0],[2,0,0],side);
  }
  clothMotion(flight,2);
  flies(flight);

  for (const [side,name] of [[-1,'turn_l'],[1,'turn_r']]) {
    const turn = animation(name,0.7,true);
    wave(turn,'waist','rotation',[0,side*5,0],[0,side,0]);
    wave(turn,'torso','rotation',[2,side*7,side*2],[0,side*2,0]);
    wave(turn,'head','rotation',[0,side*10,0],[0,side,0]);
    clothMotion(turn);
    flies(turn);
  }
  function action(name,duration,poses,peak = 0.4) {
    const entry = animation(name,duration);
    for (const [owner,rotation] of Object.entries(poses)) {
      channel(entry,owner,'rotation',[[0,zero],[duration*peak,rotation],
        [duration*0.69,rotation.map(value => round(value*0.66))],[duration,zero]]);
    }
    for (let index = 0; index < 4; index++) {
      channel(entry,`cape${index+1}`,'rotation',[[0,[-3,0,0]],
        [duration*(0.42+index*0.045),[-8-index*2,0,0]],[duration,[-3,0,0]]]);
    }
    flies(entry);
    return entry;
  }
  const fire = action('cannon_fire',1.6,{
    torso: [3,-6,0],head: [-3,6,0],arm_r: [-72,-6,4],forearm_r: [-12,0,0],
    hand_r: [2,0,0],arm_l: [-32,8,-10],forearm_l: [-28,0,0]
  },0.3);
  channel(fire,'arm_r','rotation',[[0,zero],[0.42,[-72,-6,4]],
    [0.58,[-61,-6,4]],[0.78,[-72,-6,4]],[1.13,[-67,-6,4]],[1.6,zero]]);
  action('gas_charge',2.6,{waist: [5,0,0],torso: [4,0,0],head: [-9,0,0],
    arm_l: [-25,0,-34],arm_r: [-25,0,34],forearm_l: [-34,0,0],forearm_r: [-34,0,0]});
  action('phase_shift',2.2,{torso: [-3,0,0],head: [-7,0,0],
    arm_l: [-14,0,-45],arm_r: [-14,0,45],forearm_l: [-19,0,0],forearm_r: [-19,0,0]});
  const transform = action('transform',2.8,{waist: [0,-6,0],torso: [-4,6,0],head: [-10,0,0],
    arm_l: [-30,0,-57],arm_r: [-30,0,57],forearm_l: [-25,0,0],forearm_r: [-25,0,0]});
  channel(transform,'root','position',[[0,zero],[0.7,[0,1.2,0]],[1.5,[0,2.6,0]],[2.8,zero]]);
  action('hurt',0.45,{torso: [-5,0,-3],head: [5,0,3],arm_l: [4,0,-5],arm_r: [4,0,5]},0.28);
  const death = animation('death',2,'hold_on_last_frame');
  channel(death,'root','rotation',[[0,zero],[0.5,[-8,0,0]],[1.25,[-82,0,0]],[2,[-90,0,0]]]);
  channel(death,'root','position',[[0,zero],[0.5,[0,2,0]],[1.25,[0,10.5,0]],[2,[0,9.8,0]]]);
  channel(death,'arm_l','rotation',[[0,zero],[1.1,[0,0,-15]],[2,zero]]);
  channel(death,'arm_r','rotation',[[0,zero],[1.1,[0,0,15]],[2,zero]]);
  for (const entry of Object.values(animations)) {
    for (const channels of Object.values(entry.bones)) {
      for (const value of Object.values(channels.rotation || {})) value[2] *= -1;
    }
  }
  return animations;
};
