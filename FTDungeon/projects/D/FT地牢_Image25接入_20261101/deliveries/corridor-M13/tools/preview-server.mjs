import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createServer} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const packRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/assets/default-pack'
const server = await createServer({
  root: path.join(root, 'viewer'),
  clearScreen: false,
  server: {host: '127.0.0.1', port: 51813, strictPort: true, fs: {strict: false}},
  plugins: [{
    name: 'm13-local-assets',
    configureServer(instance) {
      instance.middlewares.use(async (request, response, next) => {
        const pathname = decodeURIComponent(new URL(request.url, 'http://127.0.0.1').pathname)
        let target
        if (pathname === '/room.nbt') target = path.join(root, 'room.nbt')
        else if (pathname === '/room.json') target = path.join(root, 'room.json')
        else if (pathname.startsWith('/pack/')) {
          target = path.resolve(packRoot, pathname.slice(6))
          if (!target.startsWith(path.resolve(packRoot) + path.sep)) {response.statusCode = 403; response.end(); return}
        } else {next(); return}
        try {
          const bytes = await fs.readFile(target)
          response.setHeader('Content-Type', target.endsWith('.png') ? 'image/png' : target.endsWith('.json') ? 'application/json' : 'application/octet-stream')
          response.end(bytes)
        } catch {
          response.statusCode = 404
          response.end('Not found')
        }
      })
    }
  }]
})
await server.listen()
console.log('M13 NBT preview http://127.0.0.1:51813/')
process.on('SIGINT', async () => {await server.close(); process.exit(0)})
