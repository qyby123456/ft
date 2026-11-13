import fs from 'node:fs/promises'
import path from 'node:path'
import { pathToFileURL } from 'node:url'
import { root, skillRoot } from './common.mjs'

const { createServer } = await import(pathToFileURL(path.join(skillRoot, 'node_modules/vite/dist/node/index.js')))
const packRoot = path.join(skillRoot, 'node_modules/@mattzh72/lodestone/assets/default-pack')
const server = await createServer({
  root: path.join(root, 'tools/viewer'),
  clearScreen: false,
  resolve: { alias: { '@mattzh72/lodestone': path.join(skillRoot, 'node_modules/@mattzh72/lodestone/lib/index.js') } },
  plugins: [{
    name: 'm09-local-assets',
    configureServer(server) {
      server.middlewares.use(async (request, response, next) => {
        const pathname = decodeURIComponent(new URL(request.url, 'http://127.0.0.1').pathname)
        let candidate
        if (pathname.startsWith('/pack/')) candidate = path.resolve(packRoot, pathname.slice(6))
        else if (pathname === '/room.nbt') candidate = path.join(root, 'room.nbt')
        else if (pathname === '/native.nbt') candidate = path.join(root, 'qa/native-placed-none.nbt')
        else return next()
        if (pathname.startsWith('/pack/') && !candidate.startsWith(packRoot + path.sep)) {
          response.statusCode = 403
          return response.end('Forbidden')
        }
        try {
          const bytes = await fs.readFile(candidate)
          response.setHeader('Content-Type', candidate.endsWith('.json') ? 'application/json' : candidate.endsWith('.png') ? 'image/png' : 'application/octet-stream')
          response.end(bytes)
        } catch {
          response.statusCode = 404
          response.end('Missing local asset')
        }
      })
    },
  }],
  server: { host: '127.0.0.1', port: 0, fs: { strict: false } },
})
await server.listen()
const url = server.resolvedUrls.local[0]
await fs.writeFile(path.join(root, 'qa/preview-server.json'), JSON.stringify({ url, pid: process.pid, source: 'room.nbt', local_only: true }, null, 2))
console.log(`M09_PREVIEW ${url}`)
process.on('SIGINT', async () => { await server.close(); process.exit(0) })
