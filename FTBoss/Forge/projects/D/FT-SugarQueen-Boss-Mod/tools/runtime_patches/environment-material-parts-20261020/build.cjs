const fs = require("node:fs");
const path = require("node:path");
const child = require("node:child_process");
const crypto = require("node:crypto");
const root = path.resolve(__dirname, "../../..").replaceAll("\\", "/");
const task = path.resolve(process.argv[2] || root + "/_work/environment-material-parts-20261020").replaceAll("\\", "/");
const java = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin";
const asm = ["asm", "asm-tree", "asm-commons", "asm-util", "asm-analysis"].map(name => "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/libraries/org/ow2/asm/" + name + "/9.7.1/" + name + "-9.7.1.jar");
const dependencies = fs.readFileSync(root + "/_work/four-weapon-ui-20261019/classpath.txt", "utf8").trim().split(";").slice(1);
const baseline = task + "/baseline-mapped.jar";
const fart = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar";
const mapping = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg";
for (const directory of ["tools", "classes", "empty-sourcepath", "payload-mapped", "payload-srg", "evidence"]) fs.mkdirSync(task + "/" + directory, { recursive: true });
function run(executable, arguments, label, cwd = root) {
  const result = child.spawnSync(executable, arguments, { cwd, encoding: "utf8", maxBuffer: 32 * 1024 * 1024 });
  fs.writeFileSync(task + "/evidence/" + label + ".log", (result.stdout || "") + (result.stderr || ""));
  console.log(label + ": " + result.status);
  if (result.status !== 0) throw new Error(result.stdout + result.stderr);
}
function compile(name, sources, destination, classpath) {
  const arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", task + "/empty-sourcepath", "-cp", classpath.join(";"), "-d", destination, ...sources];
  const argfile = task + "/" + name + ".args";
  fs.writeFileSync(argfile, arguments.map(value => JSON.stringify(value.replaceAll("\\", "/"))).join("\n"));
  run(java + "/javac.exe", ["-J-Dfile.encoding=UTF-8", "@" + argfile], name);
}
run(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/baseline.jar", "--output", baseline, "--map", mapping, "--lib", "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar", "--threads", "2"], "map-baseline");
compile("tools", [task + "/MethodPatch.java", root + "/tools/verification/WeaponUiJarClosureCheck.java", root + "/tools/verification/ZipStreamCheck.java"], task + "/tools", asm);
compile("source", [task + "/src/net/farmingtales/ftboss/item/SwordCavityBuild.java", root + "/src/main/java/net/farmingtales/ftboss/util/SwordCompatibilityRules.java", root + "/src/main/java/net/farmingtales/ftboss/util/WeaponEnvironmentMaterials.java"], task + "/classes", [baseline, ...dependencies]);
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "MethodPatch", baseline, task + "/classes", task + "/candidate-mapped.jar"], "patch");
const entries = ["util/SwordCompatibilityRules", "util/WeaponEnvironmentMaterials", "item/SwordNativeMaterials", "item/SwordCavityBuild"].map(name => "net/farmingtales/ftboss/" + name + ".class");
run(java + "/jar.exe", ["xf", task + "/candidate-mapped.jar", ...entries], "extract", task + "/payload-mapped");
run(java + "/jar.exe", ["cf", task + "/payload-named.jar", "-C", task + "/payload-mapped", "."], "pack");
run(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/payload-named.jar", "--output", task + "/payload-remapped.jar", "--map", mapping, "--reverse", "--lib", dependencies[0], "--lib", baseline, "--threads", "2"], "remap");
run(java + "/jar.exe", ["xf", task + "/payload-remapped.jar", ...entries], "extract-srg", task + "/payload-srg");
run(java + "/java.exe", ["-cp", [task + "/tools", ...asm].join(";"), "MethodPatch", task + "/baseline.jar", task + "/payload-srg", task + "/qys-bosses.jar", "overlay"], "overlay");
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "MethodPatch", "check", task + "/baseline.jar", task + "/qys-bosses.jar"], "exact-delta");
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "WeaponUiJarClosureCheck", task + "/qys-bosses.jar", ...entries.map(name => name.slice(0, -6))], "closure");
run(java + "/java.exe", ["-cp", task + "/tools", "ZipStreamCheck", task + "/qys-bosses.jar"], "zip-stream");
run("node", [root + "/tools/verification/check_jar_zip_rules.cjs", task + "/qys-bosses.jar"], "zip-rules");
const sha = filename => crypto.createHash("sha256").update(fs.readFileSync(filename)).digest("hex");
fs.writeFileSync(task + "/evidence/BUILD.json", JSON.stringify({ baseline: sha(task + "/baseline.jar"), candidate: sha(task + "/qys-bosses.jar"), entries }, null, 2) + "\n");
