import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const tools = path.dirname(fileURLToPath(import.meta.url))
const root = path.dirname(tools)
const packageRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone'
const pack = path.resolve(packageRoot, 'assets/default-pack')
const server = await createServer({
  root: tools,
  resolve: { alias: { '@mattzh72/lodestone': packageRoot + '/lib/index.js' } },
  plugins: [{
    name: 'm06-actual-nbt-and-default-pack',
    configureServer(server) {
      server.middlewares.use(async (request, response, next) => {
        const url = new URL(request.url ?? '/', 'http://localhost')
        let target
        if (url.pathname === '/__minecraft_builder__/build.nbt') target = path.join(root, 'room.nbt')
        else if (url.pathname.startsWith('/__minecraft_builder__/default-pack/')) {
          target = path.resolve(pack, decodeURIComponent(url.pathname.slice('/__minecraft_builder__/default-pack/'.length)))
          if (target !== pack && !target.startsWith(pack + path.sep)) { response.statusCode = 403; response.end(); return }
        } else { next(); return }
        try {
          const buffer = await fs.readFile(target)
          response.setHeader('Content-Type', target.endsWith('.json') ? 'application/json' : target.endsWith('.png') ? 'image/png' : 'application/octet-stream')
          response.end(buffer)
        } catch (error) { response.statusCode = 404; response.end(String(error)) }
      })
    },
  }],
  server: { host: '127.0.0.1', port: Number(process.argv[2] ?? 5308), strictPort: true, open: false, fs: { strict: false } },
})
await server.listen()
console.log('M06 actual NBT views: ' + server.resolvedUrls.local[0] + 'viewer.html')
process.on('SIGINT', async () => { await server.close(); process.exit(0) })
process.on('SIGTERM', async () => { await server.close(); process.exit(0) })
