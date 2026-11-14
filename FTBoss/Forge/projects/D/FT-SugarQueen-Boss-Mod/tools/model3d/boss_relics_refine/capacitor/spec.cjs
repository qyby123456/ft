'use strict';

function specification() {
  const entries = [];
  const centerY = 7.1;
  function cuboid(group, name, center, size, material, options = {}) {
    entries.push({ group, name, from: center.map((coordinate, index) => coordinate - size[index] / 2), to: center.map((coordinate, index) => coordinate + size[index] / 2), material, ...options });
  }
  function ring(group, name, radius, radialThickness, depth, centerDepth, material, options = {}) {
    const tangent = radius * 2 * Math.tan(Math.PI / 8) - 0.02;
    for (const index of options.segments || [0, 1, 2, 3, 4, 5, 6, 7]) {
      const radians = index * Math.PI / 4, diagonal = index % 2;
      const center = [options.centerX || 8, options.centerY || centerY, centerDepth];
      center[0] += Math.sin(radians) * radius; center[1] += Math.cos(radians) * radius;
      if (options.plane === 'yz') {
        center[2] = centerDepth + Math.sin(radians) * radius;
        center[0] = options.centerX || 8;
      } else center[2] += diagonal * 0.012;
      const tangentVertical = index === 2 || index === 6;
      const size = options.plane === 'yz' ? (tangentVertical ? [depth, tangent, radialThickness] : [depth, radialThickness, tangent]) : (tangentVertical ? [radialThickness, tangent, depth] : [tangent, radialThickness, depth]);
      const axis = options.plane === 'yz' ? 'x' : 'z';
      const angle = (index === 1 || index === 5 ? -45 : 45) * (options.plane === 'yz' ? -1 : 1);
      cuboid(group, name + '_' + index, center, size, material, { pattern: options.pattern || 'machined', rotation: diagonal ? { axis, angle, origin: center, rescale: false } : undefined });
    }
  }
  function filledOctagon(group, name, radius, depth, centerDepth, material, pattern) {
    const inset = radius * Math.tan(Math.PI / 8), cornerSize = Math.SQRT2 * (radius - inset);
    cuboid(group, name + '_vertical_table', [8, centerY, centerDepth], [2 * inset, 2 * radius, depth], material, { pattern });
    cuboid(group, name + '_horizontal_table', [8, centerY, centerDepth + 0.015], [2 * radius, 2 * inset, depth], material, { pattern });
    for (const horizontal of [-1, 1]) for (const vertical of [-1, 1]) {
      const center = [8 + horizontal * inset, centerY + vertical * inset, centerDepth + 0.03];
      cuboid(group, name + '_cut_' + horizontal + '_' + vertical, center, [cornerSize, cornerSize, depth], material,
        { pattern: 'cut_facet', rotation: { axis: 'z', angle: horizontal === vertical ? 45 : -45, origin: center, rescale: false } });
    }
  }

  filledOctagon('housing', 'inner_iron_chassis', 3.5, 1.65, 8.0, 'iron', 'panel');
  ring('housing', 'brass_outer_band', 3.65, 0.6, 1.5, 8.0, 'brass', { pattern: 'milled' });
  ring('housing', 'front_beveled_bezel', 3.6, 0.65, 0.38, 6.98, 'brass');
  ring('housing', 'rear_steel_retaining_rim', 3.55, 0.55, 0.3, 9.02, 'iron');
  ring('housing', 'recessed_enamel_seal', 3.0, 0.4, 0.4, 6.8, 'jade', { pattern: 'enamel' });
  ring('core', 'inner_gold_claw_bezel', 2.7, 0.35, 0.4, 6.6, 'brass');
  cuboid('core', 'hidden_dielectric_lens_mount', [8, centerY, 7.15], [4.2, 4.2, 0.5], 'dark', { pattern: 'rubber' });
  filledOctagon('core', 'blue_energy_lens', 2.5, 0.8, 6.7, 'energy', 'prismatic');
  filledOctagon('core', 'raised_core_table', 2.05, 0.45, 6.07, 'energy', 'prismatic');
  for (const horizontal of [-1, 1]) for (const vertical of [-1, 1]) {
    const center = [8 + horizontal * 2.22, centerY + vertical * 2.22, 6.7];
    cuboid('housing', 'faceted_gold_claw_' + horizontal + '_' + vertical, center, [0.42, 1.15, 0.62], 'brass',
      { pattern: 'milled', rotation: { axis: 'z', angle: horizontal === vertical ? -45 : 45, origin: center, rescale: false } });
    cuboid('housing', 'front_captive_screw_' + horizontal + '_' + vertical, [8 + horizontal * 2.6, centerY + vertical * 2.6, 6.72], [0.45, 0.45, 0.25], 'iron', { pattern: 'screw' });
  }
  const boltUpper = [8.35, centerY + 0.82, 5.78], boltLower = [7.7, centerY - 0.83, 5.78];
  cuboid('core', 'lightning_upper', boltUpper, [0.45, 2.3, 0.25], 'ivory', { rotation: { axis: 'z', angle: -45, origin: boltUpper, rescale: false }, pattern: 'polished' });
  cuboid('core', 'lightning_bridge', [8.0, centerY, 5.768], [1.6, 0.45, 0.25], 'ivory', { pattern: 'polished' });
  cuboid('core', 'lightning_lower', boltLower, [0.45, 2.3, 0.25], 'ivory', { rotation: { axis: 'z', angle: -45, origin: boltLower, rescale: false }, pattern: 'polished' });
  ring('coil', 'lower_copper_filament_outer', 3.18, 0.16, 0.25, 6.52, 'copper', { segments: [2, 3, 4, 5, 6], pattern: 'drawn' });
  ring('coil', 'lower_copper_filament_inner', 2.98, 0.16, 0.25, 6.48, 'copper', { segments: [3, 4, 5], pattern: 'drawn' });
  for (const sign of [-1, 1]) {
    cuboid('electrodes', 'ceramic_side_seat_' + sign, [8 + sign * 3.95, centerY, 8], [0.75, 1.65, 1.3], 'ceramic', { pattern: 'ceramic' });
    for (let rib = 0; rib < 2; rib++) cuboid('electrodes', 'side_insulator_rib_' + sign + '_' + rib, [8 + sign * (3.85 + rib * 0.3), centerY, 8], [0.18, 1.85, 1.5], 'ceramic', { pattern: 'ceramic' });
    cuboid('electrodes', 'copper_side_electrode_' + sign, [8 + sign * 4.45, centerY, 8], [0.4, 1.1, 1.0], 'copper', { pattern: 'contact' });
    cuboid('electrodes', 'terminal_contact_cap_' + sign, [8 + sign * 4.65, centerY, 8], [0.2, 1.35, 1.25], 'iron', { pattern: 'screw' });
    cuboid('coil', 'filament_terminal_' + sign, [8 + sign * 3.35, centerY, 6.7], [0.7, 0.45, 0.45], 'copper', { pattern: 'drawn' });
  }

  filledOctagon('backplate', 'back_dark_recess', 2.95, 0.3, 8.95, 'dark', 'rubber');
  filledOctagon('backplate', 'rear_jade_enameled_cover', 2.6, 0.25, 9.25, 'jade', 'enamel');
  cuboid('backplate', 'rear_service_spine', [8, centerY, 9.48], [2.5, 4.2, 0.25], 'iron', { pattern: 'panel' });
  for (let index = 0; index < 4; index++) cuboid('backplate', 'rear_recessed_cooling_fin_' + index, [8, centerY - 1.2 + index * 0.8, 9.67], [2.0, 0.25, 0.25], 'iron', { pattern: 'milled' });
  for (const horizontal of [-1, 1]) for (const vertical of [-1, 1]) cuboid('backplate', 'rear_screw_' + horizontal + '_' + vertical, [8 + horizontal * 1.85, centerY + vertical * 1.85, 9.5], [0.45, 0.45, 0.25], 'brass', { pattern: 'screw' });
  for (const sign of [-1, 1]) {
    cuboid('backplate', 'rear_copper_trace_' + sign, [8 + sign * 1.7, centerY, 9.5], [0.25, 2.6, 0.2], 'copper', { pattern: 'drawn' });
    cuboid('backplate', 'rear_copper_trace_elbow_' + sign, [8 + sign * 1.35, centerY + sign * 1.2, 9.5], [0.9, 0.25, 0.2], 'copper', { pattern: 'drawn' });
  }

  cuboid('suspension', 'upper_jewelry_lug', [8, 11.0, 8], [1.45, 1.0, 1.35], 'brass', { pattern: 'milled' });
  cuboid('suspension', 'lug_throat_insulator', [8, 11.3, 8], [1.1, 0.3, 1.0], 'dark', { pattern: 'rubber' });
  ring('suspension', 'octagonal_bail', 1.2, 0.4, 0.55, 8.0, 'brass', { centerY: 12.2 });
  ring('suspension', 'perpendicular_chain_link', 0.68, 0.3, 0.4, 8.0, 'iron', { centerY: 13.9, plane: 'yz', pattern: 'polished' });
  for (const horizontal of [7.55, 8.45]) cuboid('suspension', 'lug_pivot_pin_' + horizontal, [horizontal, 11.05, 7.23], [0.35, 0.35, 0.3], 'iron', { pattern: 'screw' });
  return entries;
}

module.exports = { specification };
