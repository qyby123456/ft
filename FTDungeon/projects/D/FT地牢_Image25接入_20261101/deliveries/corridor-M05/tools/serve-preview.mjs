import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const packRoot = path.join(skill, 'node_modules/@mattzh72/lodestone/assets/default-pack')
const server = await createServer({
  root: path.join(root, 'viewer'),
  appType: 'spa',
  resolve: { alias: { '@mattzh72/lodestone': path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js') } },
  cacheDir: path.join(root, 'qa/vite-cache'),
  plugins: [{
    name: 'm05-read-only-local-assets',
    configureServer(instance) {
      instance.middlewares.use((request, response, next) => {
        const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname)
        let target
        if (pathname === '/room.nbt') target = path.join(root, 'room.nbt')
        else if (pathname.startsWith('/pack/')) {
          target = path.resolve(packRoot, pathname.slice(6))
          if (!target.startsWith(packRoot + path.sep)) { response.statusCode = 403; response.end(); return }
        } else return next()
        if (!fs.existsSync(target)) { response.statusCode = 404; response.end(); return }
        const extension = path.extname(target)
        response.setHeader('Content-Type', extension === '.json' ? 'application/json' : extension === '.png' ? 'image/png' : 'application/octet-stream')
        fs.createReadStream(target).pipe(response)
      })
    }
  }],
  server: { host: '127.0.0.1', port: Number(process.argv[2] ?? 5145), strictPort: false, fs: { strict: false } }
})
await server.listen()
process.stdout.write(`M05_PREVIEW_URL=${server.resolvedUrls.local[0]}\n`)
