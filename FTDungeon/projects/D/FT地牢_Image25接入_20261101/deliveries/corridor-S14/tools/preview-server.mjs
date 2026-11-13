import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const pack = path.join(skill, 'node_modules/@mattzh72/lodestone/assets/default-pack')
const server = await createServer({
  root,
  resolve: { alias: { '@mattzh72/lodestone': path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js') } },
  server: { host: '127.0.0.1', port: Number(process.env.S14_PREVIEW_PORT || 5314), strictPort: true, fs: { strict: false } },
  plugins: [{ name: 's14-pack', configureServer(server) {
    server.middlewares.use('/pack/', (request, response) => {
      const target = path.resolve(pack, '.' + decodeURIComponent(new URL(request.url, 'http://local').pathname))
      if (!target.startsWith(pack + path.sep) || !fs.existsSync(target) || !fs.statSync(target).isFile()) { response.statusCode = 404; response.end(); return }
      response.setHeader('Content-Type', target.endsWith('.png') ? 'image/png' : target.endsWith('.json') ? 'application/json' : 'application/octet-stream')
      fs.createReadStream(target).pipe(response)
    })
  } }]
})
await server.listen()
console.log(server.resolvedUrls.local[0] + 'viewer.html')
