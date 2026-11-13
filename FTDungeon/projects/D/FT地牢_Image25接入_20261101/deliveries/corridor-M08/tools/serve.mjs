import fs from 'node:fs/promises'
import { createReadStream } from 'node:fs'
import path from 'node:path'
import { pathToFileURL } from 'node:url'

const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { createServer } = await import(pathToFileURL(`${skill}/node_modules/vite/dist/node/index.js`))

export default async function startViewer(root) {
  const pack = `${skill}/node_modules/@mattzh72/lodestone/assets/default-pack`
  const server = await createServer({
    root: path.join(root, 'tools/viewer'),
    appType: 'spa',
    clearScreen: false,
    plugins: [{
      name: 'corridor-m08-evidence-resources',
      configureServer(server) {
        server.middlewares.use('/__minecraft_builder__/', async (request, response, next) => {
          const relative = decodeURIComponent(new URL(request.url, 'http://localhost').pathname)
          const filename = relative === '/build.nbt' ? path.join(root, 'room.nbt')
            : relative.startsWith('/default-pack/') ? path.resolve(pack, relative.slice('/default-pack/'.length)) : null
          if (!filename) return next()
          if (filename !== path.join(root, 'room.nbt') && !filename.startsWith(path.resolve(pack) + path.sep)) {
            response.statusCode = 403
            return response.end('Forbidden')
          }
          try {
            await fs.access(filename)
            response.setHeader('Content-Type', filename.endsWith('.png') ? 'image/png' : filename.endsWith('.json') ? 'application/json' : 'application/octet-stream')
            createReadStream(filename).pipe(response)
          } catch {
            response.statusCode = 404
            response.end('Not found')
          }
        })
      },
    }],
    server: { host: '127.0.0.1', port: 0, open: false, fs: { strict: false } },
  })
  await server.listen()
  return server
}
