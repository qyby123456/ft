const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const project = path.resolve(__dirname, '../..');
const task = path.resolve(process.argv[2] || path.join(project, '_work/boss-vanilla-hurt-sounds-20261012'));
assert.ok(task.startsWith(path.join(project, '_work') + path.sep));
const javaHome = 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot';
const tool = name => path.join(javaHome, `bin/${name}.exe`);
const cache = 'C:/Users/YOUR_USERNAME/.gradle/caches';
const renamer = `${cache}/modules-2/files-2.1/net.neoforged/AutoRenamingTool/2.0.17/9388df4c3589faf10c35d9154cfb2451c83bd717/AutoRenamingTool-2.0.17-all.jar`;
const mapping = `${cache}/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg`;
const minecraftSrg = `${cache}/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar`;
const minecraftMapped = `${cache}/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar`;
const verification = path.join(task, 'verification');
let log = '';
function run(executable, args) {
  try {
    const output = execFileSync(executable, args, { encoding: 'utf8', maxBuffer: 32 * 1024 * 1024 });
    log += output;
    fs.writeFileSync(path.join(verification, 'incremental-build.log'), log);
    return output;
  } catch (error) {
    log += (error.stdout || '') + (error.stderr || '');
    fs.writeFileSync(path.join(verification, 'incremental-build.log'), log);
    throw error;
  }
}
const mappedBaseline = path.join(task, 'baseline-mapped.jar');
run(tool('java'), ['-jar', renamer, '--input', path.join(task, 'baseline.jar'), '--output', mappedBaseline,
  '--names', mapping, '--lib', minecraftSrg, '--threads', '2']);
const classpath = mappedBaseline + ';' + fs.readFileSync(path.join(verification, 'compile-classpath.txt'), 'utf8').trim();
const classes = path.join(task, 'after-classes');
const emptySourcepath = path.join(task, 'empty-sourcepath');
fs.mkdirSync(classes, { recursive: true });
fs.mkdirSync(emptySourcepath, { recursive: true });
const source = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const argumentsFile = path.join(task, 'compile.args');
const args = ['--release', '17', '-proc:none', '-encoding', 'UTF-8', '-implicit:none', '-sourcepath', emptySourcepath,
  '-cp', classpath, '-d', classes, path.join(source, 'sound/BossHurtProfile.java'),
  path.join(source, 'sound/BossHurtSounds.java'), path.join(source, 'mixin/BossHurtSoundMixin.java')];
fs.writeFileSync(argumentsFile, args.map(argument => `"${argument.replaceAll('\\', '/')}"`).join('\n') + '\n');
run(tool('javac'), ['-J-Dfile.encoding=UTF-8', `@${argumentsFile}`]);
const mappedDelta = path.join(task, 'after-mapped.jar');
run(tool('jar'), ['--create', '--file', mappedDelta, '--no-manifest', '-C', classes, '.']);
run(tool('java'), ['-jar', renamer, '--input', mappedDelta, '--output', path.join(task, 'after-srg.jar'),
  '--names', mapping, '--reverse', '--lib', minecraftMapped, '--lib', mappedBaseline, '--threads', '2']);
log += 'PASS: isolated incremental javac and official-to-SRG reobfuscation\n';
fs.writeFileSync(path.join(verification, 'incremental-build.log'), log);
console.log('PASS: isolated incremental javac and official-to-SRG reobfuscation');
