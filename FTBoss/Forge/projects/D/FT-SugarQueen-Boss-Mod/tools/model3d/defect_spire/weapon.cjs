'use strict';

const { atlas } = require('./materials.cjs');
const { thirdPersonDisplay } = require('../defect_claw_fit.cjs');

function build() {
  const texture = atlas(256, 1.5);
  const elements = [];
  function part(name, origin, size, material, rotation) {
    const coordinates = texture.uv(size, material, name);
    const faces = Object.fromEntries(Object.entries(coordinates).map(([direction, rectangle]) => {
      const [left, top] = rectangle.uv;
      const [width, height] = rectangle.uv_size;
      return [direction, { uv: [left, top, left + width, top + height].map(value => value * 16 / texture.size), texture: '#atlas' }];
    }));
    elements.push({ name, from: origin, to: origin.map((value, axis) => value + size[axis]), faces,
      ...(rotation ? { rotation: { origin: origin.map((value, axis) => value + size[axis] / 2), axis: rotation[0], angle: rotation[1] } } : {}) });
  }
  part('wrist_back', [4.5, 1, 9.6], [7, 6.6, 1.6], 'bronze');
  part('wrist_left', [3.8, 1.3, 5.4], [1.7, 6.4, 5], 'brass');
  part('wrist_right', [10.5, 1.3, 5.4], [1.7, 6.4, 5], 'brass');
  part('wrist_front', [4.7, 1, 4.6], [6.6, 2, 1.65], 'brass');
  part('wrist_rim_front', [4.4, 0.7, 4.4], [7.2, 1, 1.9], 'edge');
  part('wrist_rim_back', [4.4, 0.7, 9.8], [7.2, 1, 1.8], 'edge');
  part('grip', [5.1, 5.3, 7], [5.8, 1.5, 1.8], 'joint');
  part('bracer_shell', [4.3, 3, 4.2], [7.4, 5.9, 1.8], 'brass');
  part('bracer_back', [4.6, 7.6, 8.7], [6.8, 5, 1.6], 'iron');
  part('palm_frame', [4.1, 8, 5.1], [7.8, 5.4, 4], 'bronze');
  part('palm_armor', [4.5, 8.4, 4.5], [7, 4.7, 1.3], 'brass');
  part('capacitor_socket', [5.5, 7.45, 3.8], [5, 4.8, 1.05], 'joint');
  part('capacitor_rim_vertical', [6.2, 6.9, 3.6], [3.6, 5.8, 1.3], 'edge');
  part('capacitor_rim_horizontal', [5.3, 7.9, 3.5], [5.4, 3.8, 1.5], 'edge');
  part('capacitor_glass_vertical', [6.7, 7.4, 3.1], [2.6, 4.8, 1.1], 'eye');
  part('capacitor_glass_horizontal', [5.9, 8.3, 3], [4.2, 3, 1.15], 'eye');
  part('capacitor_glint', [7, 10.1, 2.7], [1.2, 1.1, 0.5], 'white');
  part('bracer_conduit', [7.5, 3.8, 3.9], [1, 3.6, 0.7], 'eye');
  for (let digit = 0; digit < 3; digit++) {
    const centerX = 5 + digit * 2.3;
    const lift = digit === 1 ? 0.8 : 0;
    part('knuckle_' + digit, [centerX - 0.65, 12.4, 5.2], [1.8, 2, 3.1], 'steel');
    part('finger_plate_' + digit, [centerX - 0.8, 13.7, 5.4], [2.1, 3.8 + lift, 2.5], 'brass');
    part('finger_inlay_' + digit, [centerX - 0.1, 14.5, 5.05], [0.7, 2.4, 0.6], 'eye');
    part('finger_joint_' + digit, [centerX - 0.5, 16.8 + lift, 5.1], [1.5, 1.6, 2.6], 'joint');
    part('hook_' + digit, [centerX - 0.55, 17.6 + lift, 3.5], [1.6, 1.4, 3.7], 'edge');
    part('claw_tip_' + digit, [centerX - 0.35, 16.5 + lift, 2.9], [1.2, 2, 1.7], 'brass', ['x', 22.5]);
  }
  part('thumb_joint', [2.6, 9, 6.1], [2.1, 2.8, 2.7], 'steel');
  part('thumb_plate', [1.7, 10.7, 5.4], [2, 3.8, 2.8], 'brass', ['z', -22.5]);
  part('thumb_hook', [1.3, 13.2, 3.9], [1.8, 1.6, 3.4], 'edge');
  const display = {
    gui: { rotation: [18, -32, -18], translation: [0.5, -1.2, 0], scale: [0.73, 0.73, 0.73] },
    thirdperson_righthand: thirdPersonDisplay(),
    thirdperson_lefthand: thirdPersonDisplay(),
    firstperson_righthand: { rotation: [0, 145, -12], translation: [0.491695, 1.5, 0.406969], scale: [0.65, 0.65, 0.65] },
    firstperson_lefthand: { rotation: [0, 145, -12], translation: [0.491695, 1.5, 0.406969], scale: [0.65, 0.65, 0.65] },
    ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.45, 0.45, 0.45] },
    fixed: { rotation: [0, 180, 0], translation: [0, -1, 0], scale: [0.65, 0.65, 0.65] },
    head: { rotation: [0, 180, 0], translation: [0, 12, 6], scale: [0.65, 0.65, 0.65] }
  };
  return { texture, model: { ambientocclusion: false, gui_light: 'front',
    textures: { atlas: 'ftboss:item/defect_claw', particle: 'ftboss:item/defect_claw_particle' }, elements, display } };
}

module.exports = { build };
