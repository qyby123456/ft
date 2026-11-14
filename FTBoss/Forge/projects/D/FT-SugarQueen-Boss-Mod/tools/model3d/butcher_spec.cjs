const fs = require('node:fs');
const path = require('node:path');
const rules = fs.readFileSync(path.resolve(__dirname, '../../src/main/java/net/farmingtales/ftboss/boss/ai/ButcherCombatRules.java'), 'utf8');
const timing = Object.fromEntries([...rules.matchAll(/public static final int (\w+) = (\d+);/g)].map(match => [match[1], Number(match[2])]));
const box = (left, top, front, width, height, depth, material, detail = '') =>
    ({x:left, y:top, z:front, w:width, h:height, d:depth, mat:material, detail});
const SPEC = [
    {name:'root', parent:null, pivot:[0,0,0], boxes:[]},
    {name:'body', parent:'root', pivot:[0,3,0], boxes:[
        box(-9,-7,-6,18,12,12,'shirt'),
        box(-8.5,-1,-7,17,8,14,'skin'),
        box(-9.5,5,-7.5,19,2,15,'leather'),
        box(-2,5.3,-8.2,4,2,1,'brass'),
        box(-6,-5.5,-6.8,12,7,1,'apron'),
        box(-7,0,-7.9,14,6,1,'apron','stain'),
        box(-6.5,-7.6,-6.5,2,9,1,'leather'),
        box(4.5,-7.6,-6.5,2,9,1,'leather'),
        box(-5.7,-6.7,-7,1,1,1,'brass'),
        box(4.7,-6.7,-7,1,1,1,'brass'),
        box(-3.5,-8,-3.5,7,3,7,'skin'),
        box(-6,-6,6.4,2,12,1,'leather'),
        box(4,-6,6.4,2,12,1,'leather')
    ]},
    {name:'head', parent:'body', pivot:[0,-8,-0.7], boxes:[
        box(-5,-9,-5,10,10,10,'skin','face'),
        box(-4.5,-2.3,-5.8,9,3,3,'beard'),
        box(-1.5,-4.8,-6.3,3,3,2,'skin'),
        box(-5.8,-5,-1.5,2,3,3,'skin'),
        box(3.8,-5,-1.5,2,3,3,'skin'),
        box(-5.5,-9.8,-5.5,11,2,11,'cap'),
        box(-4.7,-13,-4.4,10,4,9,'cap'),
        box(-3.7,-14,-3.9,8,2,8,'cap'),
        box(-5.4,-8.8,-6.6,11,1,2,'cap')
    ]},
    {name:'apron_l', parent:'body', pivot:[3.4,6,-8.1], boxes:[box(-3,0,-0.6,6,7,1,'apron','stain')]},
    {name:'apron_r', parent:'body', pivot:[-3.4,6,-8.1], boxes:[box(-3,0,-0.6,6,7,1,'apron','stain')]},
    {name:'arm_l', parent:'body', pivot:[11.5,-6,0], boxes:[
        box(-2.6,-1.5,-3.5,6,8,7,'shirt'), box(-2.9,4,-3.8,6,2,8,'cap')
    ]},
    {name:'forearm_l', parent:'arm_l', pivot:[0,6,0], boxes:[
        box(-2.5,-1,-3,5,8,6,'skin'), box(-2.7,4.7,-3.2,5,2,7,'leather')
    ]},
    {name:'hand_l', parent:'forearm_l', pivot:[0,6,0], boxes:[
        box(-2.8,-0.4,-3.3,6,4,7,'skin'), box(-3.5,0.3,-2.4,2,3,3,'skin')
    ]},
    {name:'arm_r', parent:'body', pivot:[-11.5,-6,0], boxes:[
        box(-3.4,-1.5,-3.5,6,4,7,'shirt'), box(-3,1,-3.2,6,6,6,'skin')
    ]},
    {name:'forearm_r', parent:'arm_r', pivot:[0,6,0], boxes:[
        box(-3,-1,-3.4,6,8,7,'skin'), box(-3.3,3,-3.7,7,2,8,'apron'), box(-3.1,5.2,-3.5,6,1,7,'leather')
    ]},
    {name:'hand_r', parent:'forearm_r', pivot:[0,6,0], boxes:[
        box(-3.2,-0.4,-3.3,6,4,7,'skin'), box(1.5,0.3,-2.4,2,3,3,'skin')
    ]},
    {name:'leg_l', parent:'root', pivot:[4.8,10,0.7], boxes:[box(-3.5,-1,-3.5,7,8,7,'trouser')]},
    {name:'shin_l', parent:'leg_l', pivot:[0,6,0], boxes:[box(-3.2,-1,-3.2,6,6,6,'trouser')]},
    {name:'boot_l', parent:'shin_l', pivot:[0,5,0], boxes:[
        box(-3.8,-0.6,-4.8,8,3,9,'boot'), box(-4,2,-5,8,1,10,'leather'), box(-3.6,-0.4,-5,7,1,1,'metal')
    ]},
    {name:'leg_r', parent:'root', pivot:[-4.8,10,0.7], boxes:[box(-3.5,-1,-3.5,7,8,7,'trouser')]},
    {name:'shin_r', parent:'leg_r', pivot:[0,6,0], boxes:[box(-2.8,-1,-3.2,6,6,6,'trouser')]},
    {name:'boot_r', parent:'shin_r', pivot:[0,5,0], boxes:[
        box(-4.2,-0.6,-4.8,8,3,9,'boot'), box(-4,2,-5,8,1,10,'leather'), box(-3.4,-0.4,-5,7,1,1,'metal')
    ]}
];
const REST = {body:[4,0,0],head:[-4,0,0],arm_r:[-12,-8,8],forearm_r:[-18,0,0],hand_r:[0,0,0],arm_l:[4,0,-8],forearm_l:[-8,0,0]};
const POSES = {
    chop: [
        [timing.MELEE_IMPACT-1,{body:[-4,-12,0],head:[4,10,0],arm_r:[-145,-15,15],forearm_r:[-30,0,0],arm_l:[-35,0,-22]}],
        [timing.MELEE_IMPACT,{body:[16,10,0],head:[-12,0,0],arm_r:[-20,-10,12],forearm_r:[-15,0,0],hand_r:[70,0,0],arm_l:[-15,0,-20],apron_r:[-20,0,0],apron_l:[-20,0,0]}],
        [timing.MELEE_IMPACT+1,{body:[14,10,0],head:[-10,0,0],arm_r:[-20,-10,12],forearm_r:[-15,0,0],hand_r:[65,0,0]}],
        [timing.MELEE_DURATION,REST]
    ],
    backhand: [
        [timing.MELEE_IMPACT-1,{body:[4,-22,0],arm_r:[-62,-65,28],forearm_r:[-20,0,0],hand_r:[90,0,0],arm_l:[-18,0,-25],head:[-4,18,0]}],
        [timing.MELEE_IMPACT,{body:[8,25,0],arm_r:[-65,30,8],forearm_r:[-15,0,0],hand_r:[90,0,0],arm_l:[-15,0,-28],head:[-6,-12,0],apron_l:[-12,0,6],apron_r:[-12,0,6]}],
        [timing.MELEE_IMPACT+1,{body:[8,25,0],arm_r:[-65,30,8],forearm_r:[-15,0,0],hand_r:[90,0,0]}],
        [timing.MELEE_DURATION,REST]
    ]
};
module.exports = {SPEC, POSES, REST, timing};
