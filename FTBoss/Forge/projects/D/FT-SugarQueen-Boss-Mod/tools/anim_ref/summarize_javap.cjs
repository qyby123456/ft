/* Summarize a javap -c -p dump: per method, list the interesting references
   (called methods / String / float / int / class constants), de-duplicated.
   Usage: node summarize_javap.cjs <javap.txt> */
const fs = require('fs');
const lines = fs.readFileSync(process.argv[2], 'utf8').split(/\r?\n/);

const interesting = (l) => {
  let m = l.match(/\/\/ Method ([^:]+):/);
  if (m) return 'M:' + m[1].replace(/^.*\//, '');
  m = l.match(/\/\/ String (.*)$/);
  if (m) return 'S:"' + m[1].replace(/\s+$/, '') + '"';
  m = l.match(/\/\/ float (-?[\d.]+)f/);
  if (m) return 'F:' + m[1];
  m = l.match(/\/\/ int (-?\d+)/);
  if (m) return 'I:' + m[1];
  m = l.match(/\/\/ class ([^ ]+)/);
  if (m) return 'C:' + m[1].replace(/^.*\//, '');
  m = l.match(/\/\/ Field ([^:]+):/);
  if (m) return 'D:' + m[1].replace(/^.*\//, '');
  return null;
};

let cur = '(init)';
const refs = new Map();          // Map: 方法名可能撞上 valueOf 这类原型属性
const order = [];
const bucket = (name) => {
  if (!refs.has(name)) { refs.set(name, new Set()); order.push(name); }
  return refs.get(name);
};
bucket(cur);
for (const l of lines) {
  const h = l.match(/^\s{2}(?:public|private|protected|static|final|synchronized)[\w\s\[\]<>,\$\.]*\s([A-Za-z_$][\w$]*)\(/);
  if (h && !l.includes('//') && cur !== h[1]) {
    cur = h[1];
    bucket(cur);
    continue;
  }
  const r = interesting(l);
  if (r) bucket(cur).add(r);
}
for (const k of order) {
  const v = [...refs.get(k)];
  if (!v.length) continue;
  console.log('== ' + k + ' ==');
  console.log('   ' + v.join('  '));
}
