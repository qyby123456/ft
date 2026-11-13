import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const root = path.dirname(fileURLToPath(import.meta.url))
const modules = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules'
const pack = path.join(modules, '@mattzh72/lodestone/assets/default-pack')
const server = await createServer({
  root: path.join(root, 'viewer'),
  appType: 'spa',
  clearScreen: false,
  resolve: { alias: { '@mattzh72/lodestone': path.join(modules, '@mattzh72/lodestone/lib/index.js'), three: path.join(modules, 'three'), 'gl-matrix': path.join(modules, 'gl-matrix') } },
  plugins: [{ name: 's16-evidence', configureServer(instance) {
    instance.middlewares.use(async (request, response, next) => {
      const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname)
      let file
      if (pathname === '/build.nbt') file = path.join(root, 'room.nbt')
      else if (pathname === '/metadata.json') file = path.join(root, 'room.json')
      else if (pathname.startsWith('/default-pack/')) {
        file = path.resolve(pack, pathname.slice('/default-pack/'.length))
        if (!file.startsWith(path.resolve(pack) + path.sep)) { response.writeHead(403); response.end(); return }
      } else return next()
      try {
        const bytes = await fs.readFile(file)
        response.setHeader('Content-Type', file.endsWith('.json') ? 'application/json' : file.endsWith('.png') ? 'image/png' : 'application/octet-stream')
        response.end(bytes)
      } catch (error) { response.writeHead(404); response.end(String(error)) }
    })
  } }],
  server: { host: '127.0.0.1', port: 53116, strictPort: true, open: false, fs: { strict: false } },
})
await server.listen()
console.log('S16 NBT review: http://127.0.0.1:53116/')
process.on('SIGINT', async () => { await server.close(); process.exit(0) })
