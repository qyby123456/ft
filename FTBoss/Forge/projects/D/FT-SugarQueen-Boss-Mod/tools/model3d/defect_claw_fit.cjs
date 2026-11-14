const assert = require('node:assert/strict');
const {armPoint} = require('./puffer_bat_hand.cjs');
const space = require('./cube_space.cjs');

const CUFF_CENTER = [8, 1.2, 7.95];
const CUFF_HEIGHT = 5.5;
const SCALE = [0.94, 0.65, 1.4];

function thirdPersonDisplay() {
    const display = {rotation:[-90,-90,0], translation:[0,0,0], scale:[...SCALE]};
    const current = armPoint(CUFF_CENTER, display, false);
    const correction = space.sub([-1,CUFF_HEIGHT,0],current);
    display.translation = [-correction[0],-correction[2],-correction[1]].map(value => Number(value.toFixed(6)));
    return display;
}

function audit(model) {
    const results = [];
    for (const left of [false,true]) {
        const slot = left ? 'thirdperson_lefthand' : 'thirdperson_righthand';
        assert.deepEqual(model.display[slot],thirdPersonDisplay(),slot + ': stale wear transform');
        for (const slim of [false,true]) {
            const display = model.display[slot];
            const center = armPoint(CUFF_CENTER,display,left,slim);
            const expected = [(left ? 1 : -1)*(slim ? 0.5 : 1),CUFF_HEIGHT,0];
            assert(Math.hypot(...space.sub(center,expected)) < 0.00001,'Cuff not centered on wrist');
            const finger = armPoint(space.add(CUFF_CENTER,[0,1,0]),display,left,slim);
            const direction = space.sub(finger,center);
            assert(Math.abs(direction[0]) < 0.00001 && Math.abs(direction[2]) < 0.00001 && direction[1] > 0,
                'Fingers must continue down the forearm, not stick forward');
            const outside = space.sub(armPoint(space.add(CUFF_CENTER,[0,0,-1]),display,left,slim),center);
            assert((left ? 1 : -1)*outside[0] > 0,'Capacitor must face outward');
            const opening = [[5.5,1.2,6.3],[10.5,1.2,9.6]].map(point => armPoint(point,display,left,slim));
            const clearance = [0,2].map(axis => Math.abs(opening[1][axis]-opening[0][axis]));
            assert(clearance.every(width => width >= 4.5),'Cuff must contain a classic arm including sleeve');
            for (const swing of [-80,-35,0,45,90]) {
                const cuff = space.rotateXYZ(center,[swing,0,0],[0,0,0]);
                const wrist = space.rotateXYZ(expected,[swing,0,0],[0,0,0]);
                assert(Math.hypot(...space.sub(cuff,wrist)) < 0.00001,'Swing detached glove');
            }
            results.push({slot,slim,center,clearance,direction});
        }
    }
    return results;
}

if (require.main === module) {
    const fs = require('node:fs');
    const path = require('node:path');
    const file = process.argv[2] || path.resolve(__dirname,'../../src/main/resources/assets/ftboss/models/item/defect_claw.json');
    console.log(JSON.stringify(audit(JSON.parse(fs.readFileSync(file,'utf8'))),null,2));
    console.log('PASS: both hands, classic/slim wrists, sleeve clearance, outward capacitor and five swing angles.');
}

module.exports = {CUFF_CENTER,CUFF_HEIGHT,thirdPersonDisplay,audit};
