const fs = require("node:fs");
const path = require("node:path");
const child = require("node:child_process");
const crypto = require("node:crypto");
const root = path.resolve(__dirname, "../../..").replaceAll("\\", "/");
const task = path.resolve(process.argv[2] || root + "/_work/ingot-material-parts-20261020").replaceAll("\\", "/");
const java = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin";
const baseline = task + "/baseline/qys-bosses.jar";
const artifact = task + "/qys-bosses.jar";
const asm = ["asm", "asm-tree", "asm-commons", "asm-util", "asm-analysis"].map(name => "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/libraries/org/ow2/asm/" + name + "/9.7.1/" + name + "-9.7.1.jar");
const dependencies = fs.readFileSync(root + "/_work/four-weapon-ui-20261019/classpath.txt", "utf8").trim().split(";").slice(1);
const fart = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar";
const mapping = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg";
for (const directory of ["tools", "classes", "payload-srg", "empty-sourcepath", "evidence"]) fs.mkdirSync(task + "/" + directory, { recursive: true });
function run(executable, arguments, label) {
  const result = child.spawnSync(executable, arguments, { cwd: root, encoding: "utf8", maxBuffer: 32000000 });
  fs.writeFileSync(task + "/evidence/" + label + ".log", (result.stdout || "") + (result.stderr || ""));
  console.log(label + ": " + result.status);
  if (result.status !== 0) throw new Error(result.stdout + result.stderr);
}
function compile(label, sources, destination, classpath) {
  const arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", task + "/empty-sourcepath", "-cp", classpath.join(";"), "-d", destination, ...sources];
  const argfile = task + "/" + label + ".args";
  fs.writeFileSync(argfile, arguments.map(value => JSON.stringify(value.replaceAll("\\", "/"))).join("\n"));
  run(java + "/javac.exe", ["-J-Dfile.encoding=UTF-8", "@" + argfile], label);
}
compile("compile-tools", [__dirname + "/SingleClassOverlay.java", root + "/tools/verification/WeaponIngotMaterialCheck.java", root + "/tools/verification/WeaponEnvironmentMaterialCheck.java", root + "/tools/verification/WeaponUiJarClosureCheck.java", root + "/tools/verification/ZipStreamCheck.java"], task + "/tools", asm);
compile("compile-bonus-check", [root + "/src/main/java/net/farmingtales/ftboss/util/WeaponMaterialBonusRules.java", root + "/tools/verification/WeaponMaterialBonusCheck.java"], task + "/tools", []);
run(java + "/java.exe", ["-Xverify:all", "-cp", task + "/tools", "WeaponMaterialBonusCheck", task + "/evidence/BONUS_PURE.json"], "bonus-pure");
compile("compile-materials", [root + "/src/main/java/net/farmingtales/ftboss/util/WeaponEnvironmentMaterials.java", root + "/src/main/java/net/farmingtales/ftboss/util/WeaponMaterialBonusRules.java", root + "/src/main/java/net/farmingtales/ftboss/item/WeaponMaterialBonuses.java"], task + "/classes", [task + "/candidate-mapped.jar", ...dependencies]);
run(java + "/jar.exe", ["cf", task + "/payload-named.jar", "-C", task + "/classes", "."], "pack");
run(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/payload-named.jar", "--output", task + "/payload-srg.jar", "--map", mapping, "--reverse", "--lib", dependencies[0], "--lib", task + "/candidate-mapped.jar", "--threads", "2"], "remap");
const unpack = child.spawnSync(java + "/jar.exe", ["xf", task + "/payload-srg.jar"], { cwd: task + "/payload-srg", encoding: "utf8" });
if (unpack.status !== 0) throw new Error(unpack.stdout + unpack.stderr);
run(java + "/java.exe", ["-Xverify:all", "-cp", task + "/tools", "SingleClassOverlay", "overlay", baseline, artifact, task + "/payload-srg"], "exact-delta");
run(java + "/java.exe", ["-Xverify:all", "-cp", task + "/tools", "WeaponIngotMaterialCheck", baseline, artifact, task + "/evidence/INGOT_RULES.json"], "ingot-rules");
run(java + "/java.exe", ["-Xverify:all", "-cp", task + "/tools", "WeaponEnvironmentMaterialCheck", baseline, artifact, task + "/evidence/PURE_RULES.json"], "environment-regression");
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "WeaponUiJarClosureCheck", artifact, "net/farmingtales/ftboss/util/WeaponEnvironmentMaterials", "net/farmingtales/ftboss/util/WeaponMaterialBonusRules", "net/farmingtales/ftboss/item/WeaponMaterialBonuses"], "closure");
run(java + "/java.exe", ["-cp", task + "/tools", "ZipStreamCheck", artifact], "zip-stream");
run("node", [root + "/tools/verification/check_jar_zip_rules.cjs", artifact], "zip-rules");
const sha = filename => crypto.createHash("sha256").update(fs.readFileSync(filename)).digest("hex");
const sourceHashes = Object.fromEntries(["util/WeaponEnvironmentMaterials", "util/WeaponMaterialBonusRules", "item/WeaponMaterialBonuses"].map(name => [name, sha(root + "/src/main/java/net/farmingtales/ftboss/" + name + ".java")]));
fs.writeFileSync(task + "/evidence/BUILD.json", JSON.stringify({ baseline: sha(baseline), candidate: sha(artifact), changedClasses: ["net/farmingtales/ftboss/util/WeaponEnvironmentMaterials.class"], addedClasses: ["WeaponMaterialBonusRules", "WeaponMaterialBonusRules$Input", "WeaponMaterialBonusRules$Bonus", "WeaponMaterialBonuses"], sourceHashes }, null, 2) + "\n");
