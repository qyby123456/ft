const fs = require("node:fs");
const path = require("node:path");
const child = require("node:child_process");
const crypto = require("node:crypto");
const root = path.resolve(__dirname, "../../..").replaceAll("\\", "/");
const task = root + "/_work/legacy-accessory-fallback-20261020";
const flavor = process.argv[2] || "candidate01";
const run = task + "/client-" + flavor;
const artifact = process.argv[3] || task + "/candidate.jar";
const previous = root + "/_work/ingot-material-parts-20261020/client-rarity02";
const actual = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods";
const java = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin";
const dependencies = fs.readFileSync(root + "/_work/four-weapon-ui-20261019/classpath.txt", "utf8").trim().split(";").slice(1);
const fart = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar";
const mapping = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg";
const hash = file => crypto.createHash("sha256").update(fs.readFileSync(file)).digest("hex");
function execute(command, arguments, label, cwd = task) {
  const result = child.spawnSync(command, arguments, {cwd, encoding: "utf8", maxBuffer: 32000000, windowsHide: true});
  fs.writeFileSync(task + "/evidence/" + label + ".log", (result.stdout || "") + (result.stderr || ""));
  console.log(label + ": " + result.status);
  if (result.status !== 0) throw new Error((result.stdout + result.stderr).slice(-6000));
}
fs.mkdirSync(task + "/probe-classes", {recursive: true});
const arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", task + "/empty", "-cp", [task + "/baseline-named.jar", ...dependencies].join(";"), "-d", task + "/probe-classes", __dirname + "/probe-src/net/farmingtales/validation/legacy/LegacyAccessoryProbe.java"];
fs.writeFileSync(task + "/probe.args", arguments.map(value => JSON.stringify(value.replaceAll("\\", "/"))).join("\n"));
execute(java + "/javac.exe", ["-J-Dfile.encoding=UTF-8", "@" + task + "/probe.args"], "probe-compile");
execute(java + "/jar.exe", ["xf", previous + "/mods/weapon-ui-probe.jar", "META-INF/mods.toml"], "probe-metadata", task + "/probe-classes");
execute(java + "/jar.exe", ["cf", task + "/probe-named.jar", "-C", task + "/probe-classes", "."], "probe-pack");
execute(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/probe-named.jar", "--output", task + "/probe-srg.jar", "--map", mapping, "--reverse", "--lib", dependencies[0], "--lib", task + "/baseline-named.jar", "--threads", "2"], "probe-remap");
if (fs.existsSync(run)) throw new Error("Refusing to overwrite private instance");
fs.mkdirSync(run + "/mods", {recursive: true});
fs.mkdirSync(run + "/evidence");
const mods = [];
for (const name of fs.readdirSync(actual)) {
  if (!name.endsWith(".jar") || name === "qys-bosses.jar" || name === "weapon-ui-probe.jar") continue;
  const source = actual + "/" + name;
  const cached = previous + "/mods/" + name;
  const digest = hash(source);
  if (fs.existsSync(cached) && hash(cached) === digest) fs.linkSync(cached, run + "/mods/" + name);
  else fs.copyFileSync(source, run + "/mods/" + name);
  mods.push({name, sha256: digest});
}
fs.copyFileSync(artifact, run + "/mods/qys-bosses.jar");
fs.copyFileSync(task + "/probe-srg.jar", run + "/mods/weapon-ui-probe.jar");
for (const folder of ["config", "defaultconfigs", "kubejs", "natives", "resourcepacks", "fancymenu_data", "patchouli_books", "moonlight-global-datapacks", "saves"]) {
  if (fs.existsSync(previous + "/" + folder)) fs.cpSync(previous + "/" + folder, run + "/" + folder, {recursive: true});
}
fs.copyFileSync(previous + "/options.txt", run + "/options.txt");
const spec = JSON.parse(fs.readFileSync(previous + "/evidence/LAUNCH.json", "utf8").replace(/^\uFEFF/, ""));
spec.argv = spec.argv.filter(value => !value.startsWith("-javaagent")).map(value => value.replaceAll("\\", "/").replaceAll(previous, run).replace(/^-Dweapon.ui.probe.sha256=.*/, "-Dweapon.ui.probe.sha256=" + hash(artifact)));
spec.workingDirectory = run;
fs.writeFileSync(run + "/evidence/LAUNCH.json", JSON.stringify(spec, null, 2) + "\n");
fs.writeFileSync(run + "/evidence/MODSET.json", JSON.stringify(mods, null, 2) + "\n");
console.log(run);
