'use strict';

const { atlas } = require('./materials.cjs');

function build() {
  const texture = atlas(512, 1.5);
  const bones = [];
  const byName = new Map();
  function bone(name, parent, pivot) {
    const entry = { name, ...(parent ? { parent } : {}), pivot, cubes: [] };
    bones.push(entry); byName.set(name, entry);
  }
  function cube(owner, name, origin, size, material, rotation) {
    const entry = { origin, size, uv: texture.uv(size, material, name) };
    if (rotation) {
      entry.rotation = [-rotation[0], rotation[1], -rotation[2]];
      entry.pivot = origin.map((coordinate, axis) => coordinate + size[axis] / 2);
    }
    byName.get(owner).cubes.push(entry);
  }
  bone('root', null, [0, 0, 0]);
  bone('pelvis', 'root', [0, 18, 0]);
  bone('waist', 'pelvis', [0, 20, 0]);
  bone('torso', 'waist', [0, 24, 0]);
  bone('neck', 'torso', [0, 33, 0]);
  bone('head', 'neck', [0, 36, 0]);
  bone('jaw', 'head', [0, 37, -1]);
  bone('eye', 'head', [2.1, 41.4, -5]);
  bone('eye_small', 'head', [-3, 42.8, -4.8]);
  bone('crown', 'head', [0, 46, 0]);
  bone('core_ring', 'torso', [0, 28, -3.8]);
  bone('core', 'torso', [0, 28, -4.1]);
  for (const [side, sign] of [['l', 1], ['r', -1]]) {
    bone('shoulder_' + side, 'torso', [sign * 7, 32, 0]);
    bone('arm_' + side, 'shoulder_' + side, [sign * 9, 31, 0]);
    bone('forearm_' + side, 'arm_' + side, [sign * 9.8, 25, 0]);
    bone('hand_' + side, 'forearm_' + side, [sign * 10, 19, -0.4]);
    bone('fingers_' + side, 'hand_' + side, [sign * 10, 15.6, -0.4]);
    bone('thumb_' + side, 'hand_' + side, [sign * 7.9, 17.5, -0.5]);
    bone('leg_' + side, 'pelvis', [sign * 3.1, 18, 0]);
    bone('shin_' + side, 'leg_' + side, [sign * 3.5, 10.2, 0]);
    bone('foot_' + side, 'shin_' + side, [sign * 3.5, 3.5, 0]);
    const part = (owner, name, origin, dimensions, material, rotation) => cube(owner + '_' + side, name + '_' + side,
      sign > 0 ? origin : [-(origin[0] + dimensions[0]), origin[1], origin[2]], dimensions, material,
      rotation && [rotation[0], sign * rotation[1], sign * rotation[2]]);
    part('shoulder', 'shoulder_socket', [5.4, 30.5, -1.6], [3.5, 2.6, 3.2], 'joint');
    part('shoulder', 'shoulder_light', [7.2, 31, -1.9], [2.6, 1.1, 3.8], 'eye');
    part('arm', 'shoulder_shell', [7.1, 29, -2.9], [5.4, 4.2, 5.8], 'brass', [0, 0, -5]);
    part('arm', 'shoulder_cap', [7.8, 32.3, -2.5], [4.1, 1.3, 5], 'edge');
    part('arm', 'upper_arm', [8.7, 25, -1.3], [2.5, 4.7, 2.6], 'iron');
    part('arm', 'arm_conduit', [9.5, 25.1, -1.6], [0.9, 4.4, 0.6], 'eye');
    part('forearm', 'elbow', [8.1, 23.5, -1.7], [3.4, 2.8, 3.4], 'steel');
    part('forearm', 'forearm', [8.5, 19, -1.8], [2.9, 5.4, 3.6], 'iron');
    part('forearm', 'forearm_shell', [8.1, 19.6, -2.1], [3.8, 3.8, 1.2], 'bronze');
    part('forearm', 'wrist_seal', [8.3, 18.5, -1.7], [3.4, 1.3, 3.4], 'edge');
    part('hand', 'palm', [7.9, 15, -2.2], [4.5, 3.5, 3.5], 'brass');
    part('hand', 'palm_socket', [8.7, 15.8, -2.4], [2.8, 2, 0.5], 'joint');
    part('hand', 'palm_light', [9.1, 16.1, -2.7], [2, 1.3, 0.5], 'eye');
    part('hand', 'hand_plate', [8.3, 15.5, 0.5], [3.7, 2.6, 1.2], 'edge');
    for (let digit = 1; digit <= 3; digit++) {
      const digitX = 8.2 + (digit - 1) * 1.5;
      bone('claw_' + side + digit, 'fingers_' + side, [sign * (digitX + 0.5), 15.6, -0.4]);
      const fingerPart = (name, origin, dimensions, material) => cube('claw_' + side + digit, name,
        sign > 0 ? origin : [-(origin[0] + dimensions[0]), origin[1], origin[2]], dimensions, material);
      fingerPart('finger_hinge', [digitX, 14.6, -1.6], [1.05, 1.3, 2.2], 'steel');
      fingerPart('finger_plate', [digitX - 0.1, 12.6, -1.7], [1.25, 2.4, 1.7], 'brass');
      fingerPart('finger_tip', [digitX + 0.08, 11.6, -2.1], [0.9, 1.4, 1.5], 'edge');
    }
    part('thumb', 'thumb_hinge', [6.8, 16.1, -1], [1.4, 1.8, 2.3], 'steel');
    part('thumb', 'thumb_claw', [6.5, 14.7, -1.9], [1.3, 1.8, 1.8], 'brass');
    part('leg', 'hip_joint', [1.8, 16.5, -1.6], [2.8, 2.8, 3.2], 'steel');
    part('leg', 'thigh_piston', [2.3, 10.3, -1.1], [2.2, 7.1, 2.2], 'iron');
    part('leg', 'thigh_shell', [1.9, 12.8, -1.7], [3.1, 3.7, 1.1], 'bronze');
    part('shin', 'knee_joint', [1.8, 9.1, -1.7], [3.4, 2.5, 3.4], 'steel');
    part('shin', 'knee_plate', [2.2, 9.4, -2.2], [2.6, 2.1, 0.8], 'brass');
    part('shin', 'shin', [2.5, 3.2, -0.9], [2, 6.6, 2.1], 'iron');
    part('shin', 'shin_rib', [2.8, 4.7, -1.3], [1.3, 3.7, 0.7], 'brass');
    part('foot', 'ankle', [2.4, 2.4, -1.2], [2.2, 1.9, 2.7], 'steel');
    part('foot', 'foot', [1.8, 0.5, -3.9], [3.4, 2.4, 5.5], 'iron');
    part('foot', 'toe_shell', [2.1, 0.6, -4.3], [2.8, 1.5, 2], 'bronze');
    part('foot', 'thruster', [2.5, 0.3, 0], [2, 0.6, 1.3], 'eye');
  }
  cube('pelvis', 'pelvis', [-4.5, 16.5, -2.2], [9, 3.2, 4.4], 'iron');
  cube('pelvis', 'pelvis_front', [-3.1, 17.5, -2.6], [6.2, 1.5, 0.9], 'bronze');
  cube('waist', 'waist_piston', [-2.6, 19, -1.8], [5.2, 5.3, 3.6], 'joint');
  for (let rib = 0; rib < 3; rib++) cube('waist', 'rib_' + rib, [-3.1, 20 + rib * 1.3, -2.1], [6.2, 0.7, 4.2], 'steel');
  cube('torso', 'chest_frame', [-5.1, 23.2, -2.5], [10.2, 10.7, 5], 'joint');
  cube('torso', 'back_shell', [-4.5, 24.7, 1.5], [9, 8.5, 1.7], 'bronze');
  cube('torso', 'left_chest_shell', [1.7, 29.1, -3.3], [3.8, 4.1, 1.5], 'brass', [0, 0, -8]);
  cube('torso', 'right_chest_shell', [-5.3, 29.9, -3.2], [4, 3.4, 1.4], 'brass', [0, 0, 6]);
  cube('torso', 'left_rib_shell', [3.4, 24.4, -3.1], [2, 4.2, 1.3], 'bronze');
  cube('torso', 'right_rib_shell', [-5.2, 24.4, -3], [1.8, 4.4, 1.2], 'iron');
  cube('torso', 'sternum_crack', [-0.45, 30.9, -3.5], [0.9, 2.4, 0.8], 'eye');
  cube('core_ring', 'core_housing', [-3.1, 25, -3.9], [6.2, 6.2, 1.5], 'bronze');
  cube('core_ring', 'core_top', [-2.3, 30.4, -4.3], [4.6, 0.9, 0.8], 'edge');
  cube('core_ring', 'core_bottom', [-2.3, 24.8, -4.3], [4.6, 0.9, 0.8], 'brass');
  cube('core_ring', 'core_left', [2.3, 25.6, -4.3], [0.9, 4.9, 0.8], 'brass');
  cube('core_ring', 'core_right', [-3.2, 25.6, -4.3], [0.9, 4.9, 0.8], 'brass');
  cube('core', 'core_lens', [-2.2, 25.9, -4.6], [4.4, 4.4, 1.1], 'core');
  cube('core', 'core_hotspot', [-0.9, 27.2, -4.8], [1.8, 1.8, 0.4], 'white');
  cube('neck', 'neck', [-1.5, 33.3, -1.5], [3, 3.7, 3], 'joint');
  for (let rib = 0; rib < 3; rib++) cube('neck', 'neck_ring_' + rib, [-1.8, 33.9 + rib, -1.8], [3.6, 0.5, 3.6], 'steel');
  cube('head', 'skull_inner', [-4.3, 36.6, -3.2], [8.6, 9.6, 6.4], 'joint');
  cube('head', 'skull_back_shell', [-4.2, 37.5, 1.6], [8.4, 8.1, 2.2], 'brass');
  cube('head', 'skull_left_shell', [3.7, 38, -3.5], [1.7, 7.8, 6.8], 'brass');
  cube('head', 'skull_right_shell', [-5.2, 37.8, -3.3], [1.6, 6.6, 6.45], 'bronze');
  cube('head', 'face_shell', [-4.4, 37.7, -4.5], [8.8, 7.7, 1.7], 'brass');
  cube('head', 'brow_plate', [-4.1, 44.1, -4.8], [8.6, 1.3, 1.8], 'edge', [0, 0, -7]);
  cube('crown', 'broken_plate_left', [1.8, 44.9, -3.4], [3.3, 3.1, 6], 'brass', [0, 0, -9]);
  cube('crown', 'broken_plate_rear', [-3.3, 45.3, 0.2], [4.2, 1.7, 3.5], 'bronze');
  cube('crown', 'broken_plate_right', [-4.9, 44.1, -3.1], [1.9, 2.6, 4.5], 'brass', [0, 0, 11]);
  cube('head', 'head_crack_a', [-1.3, 43.8, -4.92], [0.9, 2.1, 0.5], 'joint');
  cube('head', 'head_crack_b', [-0.5, 44.6, -4.94], [1.2, 0.6, 0.5], 'joint');
  cube('head', 'cheek_damage', [-4.5, 37.8, -4.75], [1.8, 1.5, 0.7], 'joint');
  cube('jaw', 'jaw_shell', [-3.8, 35.9, -3.8], [7.6, 2.1, 6.2], 'bronze');
  cube('jaw', 'chin_plate', [-2.3, 35.5, -4.3], [4.6, 1.7, 2.1], 'brass');
  for (let tooth = 0; tooth < 4; tooth++) cube('jaw', 'jaw_vent_' + tooth, [-2.8 + tooth * 1.5, 36.7, -4.55], [0.7, 1, 0.5], 'joint');
  function lens(owner, center, radius) {
    const [centerX, centerY, centerZ] = center;
    cube(owner, owner + '_socket', [centerX - radius + 0.08, centerY - radius + 0.56, centerZ], [radius * 2 - 0.16, radius * 2 - 1.12, 1.45], 'joint');
    cube(owner, owner + '_rim_vertical', [centerX - radius + 0.45, centerY - radius, centerZ - 0.1], [radius * 2 - 0.9, radius * 2, 1.7], 'edge');
    cube(owner, owner + '_rim_horizontal', [centerX - radius, centerY - radius + 0.6, centerZ - 0.2], [radius * 2, radius * 2 - 1.2, 1.9], 'edge');
    cube(owner, owner + '_glass_vertical', [centerX - radius + 1, centerY - radius + 0.5, centerZ - 0.5], [radius * 2 - 2, radius * 2 - 1, 0.8], 'eye');
    cube(owner, owner + '_glass_horizontal', [centerX - radius + 0.55, centerY - radius + 1, centerZ - 0.6], [radius * 2 - 1.1, radius * 2 - 2, 0.8], 'eye');
    cube(owner, owner + '_glint', [centerX + 0.2, centerY + 0.4, centerZ - 0.8], [0.8, 0.8, 0.4], 'white');
  }
  lens('eye', [2, 41, -5.1], 2.9);
  lens('eye_small', [-3.15, 42.5, -4.8], 1.6);
  for (let segment = 1; segment <= 4; segment++) {
    const top = 33.5 - (segment - 1) * 5.4;
    const width = 12 + segment * 1.1;
    bone('cape_back' + segment, segment === 1 ? 'torso' : 'cape_back' + (segment - 1), [0, top, 3.9]);
    cube('cape_back' + segment, 'cape_fabric_' + segment, [-width / 2, top - 6.1, 3.4], [width, 6.5, 0.9], 'jade');
    cube('cape_back' + segment, 'cape_lining_' + segment, [-width / 2 + 0.3, top - 5.9, 3.1], [width - 0.6, 6.1, 0.45], 'lining');
    cube('cape_back' + segment, 'cape_border_left_' + segment, [-width / 2 - 0.12, top - 5.8, 3.45], [0.6, 6.1, 1], 'seam');
    cube('cape_back' + segment, 'cape_border_right_' + segment, [width / 2 - 0.48, top - 5.8, 3.45], [0.6, 6.1, 1], 'seam');
  }
  for (let tail = 0; tail < 4; tail++) cube('cape_back4', 'ragged_hem_' + tail, [-7.8 + tail * 4, 8.3 + tail % 2, 3.32], [3.1, 3.4 - tail % 2, 1.04], 'jade');
  cube('torso', 'mantle_left', [3.9, 32.6, -1.9], [3.3, 1.5, 5.8], 'jade', [0, 0, -12]);
  cube('torso', 'mantle_right', [-7.3, 32.6, -1.9], [3.3, 1.5, 5.8], 'jade', [0, 0, 12]);
  cube('torso', 'cloak_clasp', [-0.7, 32.4, -3.4], [1.4, 1.1, 0.8], 'edge');
  const geometry = { format_version: '1.12.0', 'minecraft:geometry': [{
    description: { identifier: 'geometry.defect_spire', texture_width: texture.size, texture_height: texture.size,
      visible_bounds_width: 6, visible_bounds_height: 6, visible_bounds_offset: [0, 1.8, 0] }, bones
  }] };
  return { texture, geometry };
}

module.exports = { build };
