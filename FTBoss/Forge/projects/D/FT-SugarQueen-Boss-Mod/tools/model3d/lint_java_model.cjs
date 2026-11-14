// lint_java_model.cjs -- 按 Minecraft Java 版方块/物品模型的硬性规则检查模型 JSON
// 用法: node tools/model3d/lint_java_model.cjs <a.json> [b.json ...]
const fs = require('fs');
const JAVA_TOP = ['parent', 'ambientocclusion', 'display', 'textures', 'elements', 'gui_light', 'overrides'];
// Java 会忽略、但 Blockbench/本工程常见、留着无害的元数据键(只提示不判错)
const IGNORED_TOP = ['credit', 'texture_size', 'format_version', 'groups'];
const JAVA_DISPLAY = ['thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand', 'gui', 'ground', 'fixed', 'head'];
const OK_ANGLES = new Set([-45, -22.5, 0, 22.5, 45]);
const OK_FACE_ROT = new Set([0, 90, 180, 270]);
const AXES = new Set(['x', 'y', 'z']);
const SHADES = new Set([true, false]);

let bad = 0;
for (const f of process.argv.slice(2)) {
  const errs = [], warns = [];
  let j;
  try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { console.log('FAIL ' + f + ' :: ' + e.message); bad++; continue; }
  for (const k of Object.keys(j)) {
    if (JAVA_TOP.includes(k)) continue;
    if (IGNORED_TOP.includes(k)) { warns.push('元数据键 Java 不解析(无害): ' + k); continue; }
    errs.push('顶层键 Java 不识别: ' + k);
  }
  if (j.gui_light !== undefined && !['front', 'side'].includes(j.gui_light)) errs.push('gui_light 非法: ' + j.gui_light);
  if (j.ambientocclusion !== undefined && !SHADES.has(j.ambientocclusion)) errs.push('ambientocclusion 必须是布尔');
  if (!j.parent && !j.elements) errs.push('既没有 parent 也没有 elements');
  if (j.display) {
    for (const [k, v] of Object.entries(j.display)) {
      if (!JAVA_DISPLAY.includes(k)) errs.push('display 槽位 Java 不识别: ' + k);
      if (v.rotation && (v.rotation.length !== 3 || v.rotation.some(n => typeof n !== 'number'))) errs.push('display.' + k + '.rotation 非法');
      if (v.translation && (v.translation.length !== 3 || v.translation.some(n => typeof n !== 'number'))) errs.push('display.' + k + '.translation 非法');
      if (v.scale && (v.scale.length !== 3 || v.scale.some(n => typeof n !== 'number' || n <= 0))) errs.push('display.' + k + '.scale 非法');
    }
  }
  if (j.textures) {
    for (const [k, v] of Object.entries(j.textures)) {
      if (typeof v !== 'string') { errs.push('textures.' + k + ' 不是字符串'); continue; }
      if (v.startsWith('#')) continue;
      if (!v.includes(':')) errs.push('textures.' + k + ' 缺少命名空间(会渲染成紫黑): ' + v);
    }
  }
  if (Array.isArray(j.elements)) {
    j.elements.forEach((el, i) => {
      const tag = 'elements[' + i + ']';
      if (!Array.isArray(el.from) || el.from.length !== 3) errs.push(tag + '.from 非法');
      if (!Array.isArray(el.to) || el.to.length !== 3) errs.push(tag + '.to 非法');
      if (Array.isArray(el.from) && Array.isArray(el.to)) {
        for (let a = 0; a < 3; a++) if (el.to[a] < el.from[a]) errs.push(tag + ' to < from (' + 'xyz'[a] + ')');
        const span = Math.max(...el.to.map((v, a) => v - el.from[a]));
        if (span <= 0) errs.push(tag + ' 尺寸为 0');
        for (const c of el.from.concat(el.to)) if (c < -16.01 || c > 32.01) warns.push(tag + ' 坐标超出 -16..32: ' + c);
      }
      if (el.rotation) {
        if (!AXES.has(el.rotation.axis)) errs.push(tag + '.rotation.axis 非法: ' + el.rotation.axis);
        if (!OK_ANGLES.has(el.rotation.angle)) errs.push(tag + '.rotation.angle 必须是 -45/-22.5/0/22.5/45 之一: ' + el.rotation.angle);
        if (!Array.isArray(el.rotation.origin) || el.rotation.origin.length !== 3) errs.push(tag + '.rotation.origin 非法');
        if (el.rotation.rescale !== undefined && typeof el.rotation.rescale !== 'boolean') errs.push(tag + '.rotation.rescale 必须是布尔');
      }
      if (!el.faces || typeof el.faces !== 'object') { errs.push(tag + ' 缺少 faces'); return; }
      for (const [fk, fc] of Object.entries(el.faces)) {
        if (!JAVA_DISPLAY.includes(fk) && !['north', 'south', 'east', 'west', 'up', 'down'].includes(fk)) errs.push(tag + ' 面名非法: ' + fk);
        if (!fc.texture) errs.push(tag + '.' + fk + ' 缺少 texture');
        else if (!String(fc.texture).startsWith('#')) errs.push(tag + '.' + fk + ' texture 必须以 # 开头: ' + fc.texture);
        else if (j.textures && !(String(fc.texture).slice(1) in j.textures)) errs.push(tag + '.' + fk + ' 引用了未定义的贴图键 ' + fc.texture);
        if (!Array.isArray(fc.uv) || fc.uv.length !== 4 || fc.uv.some(n => typeof n !== 'number')) errs.push(tag + '.' + fk + ' uv 必须是 4 个数字');
        else if (fc.uv.some(n => n < -0.01 || n > 16.01)) errs.push(tag + '.' + fk + ' uv 超出 0..16: ' + JSON.stringify(fc.uv));
        if (fc.rotation !== undefined && !OK_FACE_ROT.has(fc.rotation)) errs.push(tag + '.' + fk + ' uv 旋转非法: ' + fc.rotation);
        if (fc.tintindex !== undefined && !Number.isInteger(fc.tintindex)) errs.push(tag + '.' + fk + ' tintindex 必须是整数');
      }
    });
  }
  const name = f.split(/[\\/]/).pop();
  if (errs.length) { bad++; console.log('FAIL ' + name); errs.forEach(e => console.log('   x ' + e)); }
  else console.log('OK   ' + name + '   (elements ' + (j.elements ? j.elements.length : 0) + ', parent ' + (j.parent || '-') + ', display ' + (j.display ? Object.keys(j.display).length : 0) + ' 槽)');
  warns.slice(0, 6).forEach(w => console.log('   ! ' + w));
}
process.exit(bad ? 1 : 0);
