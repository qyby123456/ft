'use strict';

const http = require('http');
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '../..');
const art = path.join(root, 'art/blank-statue/refined');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const files = new Map([
  ['/', [path.join(art, 'viewer.html'), 'text/html; charset=utf-8']],
  ['/viewer.js', [path.join(art, 'viewer.js'), 'application/javascript; charset=utf-8']],
  ['/statue_blank.obj', [path.join(assets, 'models/block/statue_blank.obj'), 'text/plain']],
  ['/statue_blank.png', [path.join(assets, 'textures/block/statue_blank.png'), 'image/png']],
  ['/statue_blank.bbmodel', [path.join(art, 'statue_blank.bbmodel'), 'application/json']],
  ['/fourviews.png', [path.join(art, 'fourviews.png'), 'image/png']],
  ['/hero.png', [path.join(art, 'hero.png'), 'image/png']]
]);
http.createServer((request, response) => {
  const entry = files.get(new URL(request.url, 'http://localhost').pathname);
  if (!entry || request.method !== 'GET') { response.writeHead(404); response.end('Not found'); return; }
  response.writeHead(200, { 'Content-Type': entry[1], 'Cache-Control': 'no-store' });
  fs.createReadStream(entry[0]).pipe(response);
}).listen(18934, '127.0.0.1', () => console.log('Blank statue: http://127.0.0.1:18934'));
