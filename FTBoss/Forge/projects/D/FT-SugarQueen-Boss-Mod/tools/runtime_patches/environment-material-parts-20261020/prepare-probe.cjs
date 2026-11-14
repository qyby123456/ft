const fs = require("node:fs");
const path = require("node:path");
const child = require("node:child_process");
const crypto = require("node:crypto");
const task = __dirname;
const root = path.resolve(task, "../..").replaceAll("\\", "/");
const java = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin";
const previous = root + "/_work/weapon-ui-open-20261019/client-smoke-02";
const flavor = process.env.PROBE_FLAVOR || "candidate";
const run = task + "/client-" + flavor;
const artifact = task + (flavor.startsWith("baseline") ? "/baseline.jar" : "/qys-bosses.jar");
const deps = fs.readFileSync(root + "/_work/four-weapon-ui-20261019/classpath.txt", "utf8").trim().split(";").slice(1);
function execute(command, arguments, label, cwd = root) {
  const result = child.spawnSync(command, arguments, { cwd, encoding: "utf8", maxBuffer: 16000000 });
  fs.writeFileSync(task + "/evidence/" + label + ".log", result.stdout + result.stderr);
  if (result.status) throw new Error(result.stdout + result.stderr);
  console.log(label + ": PASS");
}
for (const folder of ["probe-classes", "probe-overlay", "ui-overlay", "crossbow-overlay"]) fs.mkdirSync(task + "/" + folder, { recursive: true });
const arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", task + "/empty-sourcepath", "-cp", [task + "/candidate-mapped.jar", ...deps].join(";"), "-d", task + "/probe-classes", task + "/probe-src/net/farmingtales/validation/weaponui/EnvironmentMaterialProbe.java", task + "/probe-src/net/farmingtales/validation/weaponui/BowLoadProbe.java", task + "/probe-src/net/farmingtales/validation/weaponui/RuleCacheProbe.java", task + "/probe-src/net/farmingtales/validation/weaponui/WeaponUiProbe.java"];
fs.writeFileSync(task + "/probe.args", arguments.map(value => JSON.stringify(value.replaceAll("\\", "/"))).join("\n"));
execute(java + "/javac.exe", ["-J-Dfile.encoding=UTF-8", "@" + task + "/probe.args"], "probe-compile");
execute(java + "/jar.exe", ["cf", task + "/probe-named.jar", "-C", task + "/probe-classes", "."], "probe-jar");
execute(java + "/java.exe", ["-Xmx2G", "-jar", "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar", "--input", task + "/probe-named.jar", "--output", task + "/probe-srg.jar", "--map", "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg", "--reverse", "--lib", deps[0], "--lib", task + "/candidate-mapped.jar", "--threads", "2"], "probe-remap");
execute(java + "/jar.exe", ["xf", task + "/probe-srg.jar"], "probe-extract", task + "/probe-overlay");
if (process.env.PROBE_COMPILE_ONLY === "1") process.exit(0);
fs.cpSync(task + "/probe-overlay/net/farmingtales/validation", task + "/ui-overlay/net/farmingtales/validation", { recursive: true });
if (fs.existsSync(run)) throw new Error("Refusing to replace an existing test instance");
fs.mkdirSync(run + "/mods", { recursive: true });
fs.mkdirSync(run + "/evidence");

for (const name of fs.readdirSync(previous + "/mods")) {
  if (!name.endsWith(".jar") || name === "qys-bosses.jar" || name === "weapon-ui-probe.jar") continue;
  fs.linkSync(previous + "/mods/" + name, run + "/mods/" + name);
}
fs.copyFileSync(artifact, run + "/mods/qys-bosses.jar");
fs.copyFileSync(previous + "/mods/weapon-ui-probe.jar", run + "/mods/weapon-ui-probe.jar");
execute(java + "/jar.exe", ["uf", run + "/mods/weapon-ui-probe.jar", "-C", task + "/ui-overlay", "."], "probe-ui-overlay");
for (const folder of ["config", "defaultconfigs", "kubejs", "natives", "resourcepacks", "fancymenu_data", "patchouli_books", "moonlight-global-datapacks", "saves"]) {
  if (fs.existsSync(previous + "/" + folder)) fs.cpSync(previous + "/" + folder, run + "/" + folder, { recursive: true });
}
let options = fs.readFileSync(previous + "/options.txt", "utf8").replace("pauseOnLostFocus:true", "pauseOnLostFocus:false").replace("fullscreen:true", "fullscreen:false");
fs.writeFileSync(run + "/options.txt", options);
const spec = JSON.parse(fs.readFileSync(previous + "/evidence/LAUNCH.json", "utf8").replace(/^\uFEFF/, ""));
const sha = crypto.createHash("sha256").update(fs.readFileSync(artifact)).digest("hex");
spec.argv = spec.argv.filter(value => !value.startsWith("-javaagent")).map(value => value.replaceAll(previous, run).replaceAll(previous.replaceAll("/", "\\"), run.replaceAll("/", "\\")).replace(/^-Dweapon.ui.probe.sha256=.*/, "-Dweapon.ui.probe.sha256=" + sha));
spec.argv = spec.argv.map(value => value.replaceAll("\\", "/").replaceAll(previous, run)).map(value => value.startsWith("-Xmx") ? "-Xmx5G" : value);
spec.workingDirectory = run;
if (flavor.startsWith("baseline")) spec.argv.unshift("-Dweapon.ui.probe.cases=2");
fs.writeFileSync(run + "/evidence/LAUNCH.json", JSON.stringify(spec, null, 2) + "\n");
console.log(run);
