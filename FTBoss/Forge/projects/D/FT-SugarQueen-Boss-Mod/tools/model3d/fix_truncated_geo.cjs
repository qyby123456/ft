// 把附件里**被截断**的 code.json 补全: 只截掉残缺的 tentacle_right, 按 tentacle_left 做 x 镜像补上, 其余字节原样保留
const fs = require('fs');
const src = process.argv[2], dst = process.argv[3];
let t = fs.readFileSync(src, 'utf8');
const i = t.indexOf('"name": "tentacle_right"');
if (i < 0) { console.error('没找到 tentacle_right'); process.exit(1); }
// 回退到该骨骼对象的起始 '{'
let j = t.lastIndexOf('{', i);
if (j < 0) { console.error('找不到对象起始'); process.exit(1); }
const head = t.slice(0, j);
const tail = `{
          "name": "tentacle_right",
          "parent": "root",
          "pivot": [8.0, 8.0, 0.0],
          "cubes": [
            {
              "origin": [8.0, 2.0, -2.0],
              "size": [4.0, 12.0, 4.0],
              "uv": [80, 0]
            },
            {
              "origin": [11.0, -2.0, -1.5],
              "size": [3.0, 5.0, 3.0],
              "uv": [80, 16]
            }
          ]
        }
      ]
    }
  ]
}
`;
fs.writeFileSync(dst, head + tail, 'utf8');
JSON.parse(fs.readFileSync(dst, 'utf8'));       // 解析不过就报错
console.log(`已写出 ${dst}  (补全 tentacle_right: pivot [8,8,0] + 两个立方体按 tentacle_left 沿 X 镜像)`);
