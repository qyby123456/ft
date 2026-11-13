import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const packRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/assets/default-pack'
const server = await createServer({
  root, appType: 'mpa', clearScreen: false,
  server: { host: '127.0.0.1', port: 0, strictPort: false, watch: null, hmr: false, fs: { strict: false } },
  plugins: [{
    name: 'c02-local-structure-and-default-pack',
    configureServer(server) {
      server.middlewares.use(async (request, response, next) => {
        const pathname = decodeURIComponent(new URL(request.url, 'http://127.0.0.1').pathname)
        let filename
        if (pathname === '/__minecraft_builder__/build.nbt') filename = path.join(root, 'room.nbt')
        if (pathname.startsWith('/__minecraft_builder__/default-pack/')) {
          filename = path.resolve(packRoot, pathname.slice('/__minecraft_builder__/default-pack/'.length))
          if (!filename.startsWith(path.resolve(packRoot) + path.sep)) {
            response.statusCode = 403
            response.end('Forbidden')
            return
          }
        }
        if (!filename) return next()
        try {
          const bytes = await fs.readFile(filename)
          const extension = path.extname(filename)
          response.setHeader('Content-Type', extension === '.json' ? 'application/json' : extension === '.png' ? 'image/png' : 'application/octet-stream')
          response.end(bytes)
        } catch {
          response.statusCode = 404
          response.end('Not found')
        }
      })
    },
  }],
})
await server.listen()
console.log('C02 actual-NBT viewer: ' + server.resolvedUrls.local[0] + 'views/index.html')
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, async () => { await server.close(); process.exit(0) })

