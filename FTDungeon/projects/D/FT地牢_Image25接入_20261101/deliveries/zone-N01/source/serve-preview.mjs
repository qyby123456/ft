import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/vite/dist/node/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const pack = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/assets/default-pack'
const server = await createServer({
  root, clearScreen: false,
  server: { host: '127.0.0.1', port: 53901, strictPort: false, fs: { strict: false } },
  plugins: [{ name: 'n01-actual-nbt', configureServer(instance) {
    instance.middlewares.use(async (request, response, next) => {
      try {
        const url = decodeURIComponent((request.url ?? '/').split('?')[0])
        const resourcePrefix = '/__minecraft_builder__/default-pack/'
        let target
        if (url === '/__minecraft_builder__/build.nbt') target = path.join(root, 'room.nbt')
        else if (url.startsWith(resourcePrefix)) {
          target = path.resolve(pack, url.slice(resourcePrefix.length))
          if (!target.startsWith(path.resolve(pack) + path.sep)) { response.statusCode = 403; response.end(); return }
        } else return next()
        const bytes = await fs.readFile(target)
        response.setHeader('Content-Type', target.endsWith('.json') ? 'application/json' : target.endsWith('.png') ? 'image/png' : 'application/octet-stream')
        response.end(bytes)
      } catch (error) { response.statusCode = 404; response.end(error.message) }
    })
  } }],
})
await server.listen()
console.log(`N01 interactive actual-NBT preview: ${server.resolvedUrls.local[0]}preview/interactive.html`)
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, async () => { await server.close(); process.exit(0) })
