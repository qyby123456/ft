const fs = require("node:fs");
const path = require("node:path");
const child = require("node:child_process");
const crypto = require("node:crypto");
const root = path.resolve(__dirname, "../../..").replaceAll("\\", "/");
const task = root + "/_work/legacy-accessory-fallback-20261020";
const java = "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin";
const dependencies = fs.readFileSync(root + "/_work/four-weapon-ui-20261019/classpath.txt", "utf8").trim().split(";").slice(1);
const asm = ["asm", "asm-tree", "asm-commons", "asm-util", "asm-analysis"].map(name => "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/libraries/org/ow2/asm/" + name + "/9.7.1/" + name + "-9.7.1.jar");
const fart = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar";
const mapping = "C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg";
for (const directory of ["donor-src", "donor-classes", "payload", "tools", "empty", "evidence"]) fs.mkdirSync(task + "/" + directory, {recursive: true});
function run(executable, arguments, label, cwd = task) {
  const result = child.spawnSync(executable, arguments, {cwd, encoding: "utf8", maxBuffer: 32000000, windowsHide: true});
  fs.writeFileSync(task + "/evidence/" + label + ".log", (result.stdout || "") + (result.stderr || ""));
  console.log(label + ": " + result.status);
  if (result.status !== 0) throw new Error((result.stdout + result.stderr).slice(-6000));
}
function compile(sources, output, classpath, label) {
  const arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", task + "/empty", "-cp", classpath.join(";"), "-d", output, ...sources];
  const argfile = task + "/" + label + ".args";
  fs.writeFileSync(argfile, arguments.map(value => JSON.stringify(value.replaceAll("\\", "/"))).join("\n"));
  run(java + "/javac.exe", ["-J-Dfile.encoding=UTF-8", "@" + argfile], label);
}
function extract(source, signature) {
  const start = source.indexOf(signature);
  if (start < 0) throw new Error("Missing " + signature);
  let cursor = source.indexOf("{", start), depth = 1;
  while (depth && ++cursor < source.length) {
    if (source[cursor] === "{") depth++;
    else if (source[cursor] === "}") depth--;
  }
  return source.slice(start, cursor + 1);
}
const sourceFile = root + "/src/main/java/net/farmingtales/ftboss/client/StatueHubScreen.java";
const source = fs.readFileSync(sourceFile, "utf8");
fs.writeFileSync(task + "/StatueHubScreen.frozen.java", source);
const donor = "package net.farmingtales.ftboss.client;\n"
  + "import net.minecraft.world.item.ItemStack;\nimport net.minecraft.core.BlockPos;\n"
  + "import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;\nimport net.farmingtales.ftboss.transaction.FourWeaponClientApi;\nimport net.farmingtales.ftboss.network.StatueMenuNetwork;\n"
  + "public class StatueHubScreen { private boolean actionPending; private BlockPos statuePos; private Section section; private int materialSlot; private int offeringSlot; enum Section { DAILY, MATERIAL }\n"
  + "private ItemStack evolutionWeapon() { throw new UnsupportedOperationException(); } private void updateActionButtons() { throw new UnsupportedOperationException(); }\n"
  + extract(source, "private void openSwordCavity()") + "\n" + extract(source, "private void openBuildAdjustment()") + "\n}\n";
fs.writeFileSync(task + "/donor-src/StatueHubScreen.java", donor);
if (!fs.existsSync(task + "/baseline-named.jar")) run(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/baseline.jar", "--output", task + "/baseline-named.jar", "--map", mapping, "--lib", dependencies[0], "--threads", "2"], "baseline-remap");
compile([task + "/donor-src/StatueHubScreen.java"], task + "/donor-classes", [task + "/baseline-named.jar", ...dependencies], "donor-compile");
compile([__dirname + "/LegacyAccessoryPatch.java", root + "/tools/verification/WeaponUiJarClosureCheck.java", root + "/tools/verification/ZipStreamCheck.java"], task + "/tools", asm, "tools-compile");
run(java + "/jar.exe", ["cf", task + "/donor-named.jar", "-C", task + "/donor-classes", "."], "donor-pack");
run(java + "/java.exe", ["-Xmx2G", "-jar", fart, "--input", task + "/donor-named.jar", "--output", task + "/donor-srg.jar", "--map", mapping, "--reverse", "--lib", dependencies[0], "--lib", task + "/baseline-named.jar", "--threads", "2"], "donor-remap");
run(java + "/jar.exe", ["xf", task + "/donor-srg.jar"], "donor-extract", task + "/payload");
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "LegacyAccessoryPatch", task + "/baseline.jar", task + "/payload", task + "/candidate.jar"], "exact-delta");
run(java + "/java.exe", ["-Xverify:all", "-cp", [task + "/tools", ...asm].join(";"), "WeaponUiJarClosureCheck", task + "/candidate.jar", "net/farmingtales/ftboss/client/StatueHubScreen"], "closure");
run(java + "/java.exe", ["-cp", task + "/tools", "ZipStreamCheck", task + "/candidate.jar"], "zip-stream");
run("node", [root + "/tools/verification/check_jar_zip_rules.cjs", task + "/candidate.jar"], "zip-rules");
if (fs.readFileSync(sourceFile, "utf8") !== source) throw new Error("Concurrent source change");
const sha = file => crypto.createHash("sha256").update(fs.readFileSync(file)).digest("hex");
fs.writeFileSync(task + "/evidence/BUILD.json", JSON.stringify({baseline: sha(task + "/baseline.jar"), candidate: sha(task + "/candidate.jar"), source: sha(sourceFile), changedMethods: ["openSwordCavity", "openBuildAdjustment"], newEntries: 0}, null, 2) + "\n");
