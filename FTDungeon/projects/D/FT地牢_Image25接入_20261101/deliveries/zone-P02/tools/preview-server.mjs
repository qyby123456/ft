import fs from 'node:fs'
import fsp from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { createServer } = await import(pathToFileURL(path.join(skill, 'node_modules/vite/dist/node/index.js')).href)
const pack = path.join(skill, 'node_modules/@mattzh72/lodestone/assets/default-pack')
const mime = name => name.endsWith('.png') ? 'image/png' : name.endsWith('.json') ? 'application/json' : 'application/octet-stream'
const server = await createServer({
  root: path.join(root, 'preview-app'),
  clearScreen: false,
  resolve: { alias: { '@mattzh72/lodestone': path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js') } },
  server: { host: '127.0.0.1', port: Number(process.env.P02_PREVIEW_PORT ?? 0), fs: { strict: false } },
  plugins: [{ name: 's02-readonly-assets', configureServer(server) {
    server.middlewares.use(async (request, response, next) => {
      const pathname = decodeURIComponent(new URL(request.url, 'http://127.0.0.1').pathname)
      let target
      if (pathname === '/room.nbt' || pathname === '/room.json') target = path.join(root, pathname.slice(1))
      if (/^\/qa\/native-placed-(none|clockwise_90|clockwise_180|counterclockwise_90)\.nbt$/.test(pathname)) target = path.join(root, pathname.slice(1))
      if (pathname.startsWith('/default-pack/')) {
        const candidate = path.resolve(pack, pathname.slice('/default-pack/'.length))
        if (candidate.startsWith(pack + path.sep)) target = candidate
      }
      if (!target) return next()
      try { await fsp.access(target) } catch { response.statusCode = 404; response.end('Not found'); return }
      response.setHeader('Content-Type', mime(target))
      fs.createReadStream(target).pipe(response)
    })
  } }],
})
await server.listen()
const address = server.httpServer.address()
const url = 'http://127.0.0.1:' + address.port + '/'
await fsp.mkdir(path.join(root, 'qa'), { recursive: true })
await fsp.writeFile(path.join(root, 'qa/preview-server.json'), JSON.stringify({ url, pid: process.pid }, null, 2))
console.log('P02_PREVIEW=' + url)
