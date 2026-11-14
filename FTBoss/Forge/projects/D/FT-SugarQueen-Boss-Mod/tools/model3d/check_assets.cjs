const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const root = path.resolve(__dirname, '../..');
const directory = path.resolve(process.argv[2] || path.join(root, 'src/main/resources'));
let failures = 0;
let parsed = 0;
function inspect(current) {
  for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
    const filename = path.join(current, entry.name);
    if (entry.isDirectory()) {
      inspect(filename);
      continue;
    }
    if (entry.isSymbolicLink()) {
      console.error('Unsupported resource link: ' + filename);
      failures++;
      continue;
    }
    try {
      if (fs.statSync(filename).size === 0) throw new Error('Empty resource');
      if (/\.(json|mcmeta)$/.test(filename)) {
        const resource = JSON.parse(fs.readFileSync(filename, 'utf8'));
        if (filename.endsWith('.json') && /\/assets\/[^/]+\/animations\//.test(filename.split(path.sep).join('/'))) {
          if (!resource || typeof resource.animations !== 'object' || resource.animations === null || Array.isArray(resource.animations)) {
            throw new Error('GeckoLib animation JSON requires an animations object; place custom motion data outside animations/');
          }
        }
        parsed++;
      }
    } catch (error) {
      console.error(`${filename}: ${error.message}`);
      failures++;
    }
  }
}
inspect(directory);
const jdk = process.env.JAVA_HOME || path.join(process.env.APPDATA, '.minecraft/runtime/java-runtime-gamma-snapshot');
const imageCheck = spawnSync(path.join(jdk, 'bin', process.platform === 'win32' ? 'java.exe' : 'java'),
  ['-Djava.awt.headless=true', path.join(root, 'tools/verification/ImageResourcesCheck.java'), directory], { stdio: 'inherit' });
if (imageCheck.error) console.error(imageCheck.error.message);
if (imageCheck.status !== 0) failures++;
console.log(`Parsed ${parsed} JSON files; ${failures} failing checks.`);
process.exitCode = failures === 0 ? 0 : 1;
