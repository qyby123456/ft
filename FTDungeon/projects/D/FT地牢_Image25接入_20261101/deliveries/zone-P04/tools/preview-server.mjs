import fs from 'node:fs/promises'
import path from 'node:path'
import {createRequire} from 'node:module'
import {fileURLToPath, pathToFileURL} from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const require = createRequire(path.join(skill, 'package.json'))
const {createServer} = await import(pathToFileURL(require.resolve('vite')))
const lodestoneEntry = require.resolve('@mattzh72/lodestone')
const packRoot = path.resolve(path.dirname(lodestoneEntry), '../assets/default-pack')
const mime = filename => filename.endsWith('.json') ? 'application/json' : filename.endsWith('.png') ? 'image/png' : 'application/octet-stream'
const server = await createServer({
  root: path.join(root, 'tools/viewer'),
  cacheDir: path.join(root, 'qa/vite-cache'),
  resolve: {alias: {'@mattzh72/lodestone': lodestoneEntry}},
  server: {host: '127.0.0.1', port: 0, open: false, fs: {strict: false}},
  plugins: [{name: 'p04-readonly-nbt-resources', configureServer(instance) {
    instance.middlewares.use(async (request, response, next) => {
      const url = new URL(request.url, 'http://localhost')
      let filename
      if (url.pathname === '/room.nbt') filename = path.join(root, 'room.nbt')
      else if (url.pathname.startsWith('/pack/')) {
        filename = path.resolve(packRoot, decodeURIComponent(url.pathname.slice(6)))
        if (!filename.startsWith(packRoot + path.sep)) {response.statusCode = 403; response.end(); return}
      } else {next(); return}
      try {response.setHeader('Content-Type', mime(filename)); response.end(await fs.readFile(filename))}
      catch {response.statusCode = 404; response.end('Not found')}
    })
  }}]
})
await server.listen()
const url = server.resolvedUrls.local[0]
await fs.writeFile(path.join(root, 'qa/preview-server.json'), JSON.stringify({url, pid: process.pid}, null, 2))
console.log(`P04_LODESTONE_PREVIEW ${url}`)
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, async () => {await server.close(); process.exit(0)})
