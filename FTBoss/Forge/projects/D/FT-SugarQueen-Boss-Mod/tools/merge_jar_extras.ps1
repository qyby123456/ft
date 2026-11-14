# merge_jar_extras.ps1 —— 把「没有源码、只存在于已部署 jar 里」的条目合并进新构建的 jar。
#
# 背景：部署版里保留着主源码尚未收回的运行时兼容实现，包括血腥契约、Punchy Inspect、
# 荆棘护甲客户端模型、庄园法术模型/贴图，以及较新的万味胚刃动画。用户口径是全部保留。
# 每次从源码重建后都必须用本脚本从当前稳定 jar 搬回这些显式白名单条目；新增白名单时，
# 先做旧包/新包逐条目对比，再把每项写进 $entries，禁止按目录通配覆盖新构建内容。
#
# 用法（PowerShell 5.1 及以上）：
#   powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools\merge_jar_extras.ps1 `
#       -Target build\libs\qys-bosses.jar -Source <旧 jar 路径>
#
# ★★★ 2026-09-24 血的教训：**绝对不要用 .NET 的 ZipArchive 去写这个 jar** ★★★
#   第一版就是用 `[System.IO.Compression.ZipFile]::Open(..., Update)` 改的，结果 .NET 把
#   jar 里 95 个**目录条目**重写成了 STORED + data descriptor，而 Forge 的 securejarhandler
#   只允许 DEFLATED 条目带这个标记 —— 游戏在 ModLauncher 扫 mods 目录时当场退出：
#       java.util.zip.ZipException: only DEFLATED entries can have EXT descriptor
#         at cpw.mods.jarhandling.impl.Jar.<init>
#         at net.minecraftforge.fml.loading.ModDirTransformerDiscoverer.visitFile
#   症状：**Forge 一行日志都不写**（latest.log 只有 884 字节）、没有 crash-report、没有 hs_err，
#   而且 `jar tf` / 7-zip / java.util.zip 都能正常读它 —— 极难联想。
#   所以现在改成：**解包 → 改文件 → 用 JDK 自带的 `jar` 重新打包**（Java 自己的 zip 写入器
#   不会产生那种条目），并在开头/结尾各跑一次 `tools/check_jar_zip_rules.cjs` 兜底。
#
# 会做的事：
#   ① 校验 Target / Source 的 zip 规则（有 STORED+descriptor 就直接拒绝）；
#   ② `jar xf` 解包 Target 到临时目录；
#   ③ 用 Source 里的内容覆盖 $entries 这些文件（类/贴图/mixins.json）；
#   ④ lang：**只补键**（交给 merge_lang_keys.cjs，Target 已有的值一律不动）；
#   ⑤ `jar --create --manifest` 重新打包回 Target；
#   ⑥ 复核：zip 规则 PASS + 新功能类在 + 保留条目在。

param(
    [Parameter(Mandatory = $true)][string]$Target,
    [Parameter(Mandatory = $true)][string]$Source,
    [string]$JarTool
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$entries = @(
    'ftboss.mixins.json',
    'net/farmingtales/ftboss/FTBossMod.class',
    'net/farmingtales/ftboss/util/SpellVisualRules.class',
    'net/farmingtales/ftboss/mixin/SanguineBondEntityAccess.class',
    'net/farmingtales/ftboss/mixin/SanguineBondTooltipMixin.class',
    'net/farmingtales/ftboss/integ/tridents/SanguineBondHooks.class',
    'net/farmingtales/ftboss/client/render/SanguineBondTrailVisual.class',
    'net/farmingtales/ftboss/client/render/SanguineBondTrailVisual$MultiBufferSourceHack.class',
    'assets/ftboss/textures/fx/sanguine_bond_trail.png',
    # Runtime-only compatibility additions that are newer than the source tree.,
    'META-INF/ftboss-punchy-inspect-compat.txt',
    'net/farmingtales/ftboss/client/inspect/PunchyInspectCompat.class',
    'net/farmingtales/ftboss/client/inspect/WeaponInspectClient.class',
    'net/farmingtales/ftboss/client/model/ThornArmorClientExtension.class',
    'net/farmingtales/ftboss/client/model/ThornArmorModel.class',
    'net/farmingtales/ftboss/item/ThornArmorItem.class',
    'net/farmingtales/ftboss/item/ThornArmorItem$Material.class',
    'assets/ftboss/textures/models/armor/thorn_armor_3d.png',
    'assets/ftboss/animations/gourmet_blank_blade.animation.json',
    'assets/ftboss/geo/gourmet_blank_blade.geo.json',
    'net/farmingtales/ftboss/client/compat/irons/IronNativeSpellModels.class',
    'net/farmingtales/ftboss/client/compat/irons/IronNativeSpellModels$Baked.class',
    'net/farmingtales/ftboss/client/compat/irons/IronNativeSpellTextures.class',
    'net/farmingtales/ftboss/client/render/ManorBlockbenchSpellModels.class',
    'net/farmingtales/ftboss/client/render/ManorBlockbenchSpellModels$Baked.class',
    'net/farmingtales/ftboss/client/render/ManorSpellMergeRules.class',
    'net/farmingtales/ftboss/client/render/ManorSpellMergeRules$1.class',
    'net/farmingtales/ftboss/client/render/SpellVisualRenderer.class',
    'net/farmingtales/ftboss/client/render/SpellVisualRenderer$Visual.class',
    'net/farmingtales/ftboss/util/RemainingSpellModels.class',
    'net/farmingtales/ftboss/util/RemainingSpellModels$1.class',
    'net/farmingtales/ftboss/util/RemainingSpellModels$Mesh.class',
    'assets/ftboss/textures/fx/irons/manor_crystal_coronation.png',
    'assets/ftboss/textures/fx/irons/manor_effigy_ward.png',
    'assets/ftboss/textures/fx/irons/manor_orchard_aegis.png',
    'assets/ftboss/textures/fx/irons/manor_wingstep_wings.png',
    'assets/ftboss/textures/fx/manor_all/arcane_fusillade.png',
    'assets/ftboss/textures/fx/manor_all/blood_verdict.png',
    'assets/ftboss/textures/fx/manor_all/breach_shell.png',
    'assets/ftboss/textures/fx/manor_all/candle_eclipse.png',
    'assets/ftboss/textures/fx/manor_all/candle_prison.png',
    'assets/ftboss/textures/fx/manor_all/candle_reap.png',
    'assets/ftboss/textures/fx/manor_all/citrus_nova.png',
    'assets/ftboss/textures/fx/manor_all/crimson_oath.png',
    'assets/ftboss/textures/fx/manor_all/effigy_procession.png',
    'assets/ftboss/textures/fx/manor_all/effigy_volley.png',
    'assets/ftboss/textures/fx/manor_all/effigy_ward.png',
    'assets/ftboss/textures/fx/manor_all/elixir_rain.png',
    'assets/ftboss/textures/fx/manor_all/feather_fan.png',
    'assets/ftboss/textures/fx/manor_all/feather_refuge.png',
    'assets/ftboss/textures/fx/manor_all/four_tides.png',
    'assets/ftboss/textures/fx/manor_all/frost_needles.png',
    'assets/ftboss/textures/fx/manor_all/halo_sanctuary.png',
    'assets/ftboss/textures/fx/manor_all/hearth_feast.png',
    'assets/ftboss/textures/fx/manor_all/luminous_rebuke.png',
    'assets/ftboss/textures/fx/manor_all/magnetic_storm.png',
    'assets/ftboss/textures/fx/manor_all/nightfall_blades.png',
    'assets/ftboss/textures/fx/manor_all/orbital_gears.png',
    'assets/ftboss/textures/fx/manor_all/pressure_lance.png',
    'assets/ftboss/textures/fx/manor_all/prism_battery.png',
    'assets/ftboss/textures/fx/manor_all/rending_cleave.png',
    'assets/ftboss/textures/fx/manor_all/rose_starfall.png',
    'assets/ftboss/textures/fx/manor_all/searing_pan.png',
    'assets/ftboss/textures/fx/manor_all/seraph_descent.png',
    'assets/ftboss/textures/fx/manor_all/shadow_brand.png',
    'assets/ftboss/textures/fx/manor_all/star_cross.png',
    'assets/ftboss/textures/fx/manor_all/tidal_lotus.png',
    'assets/ftboss/textures/fx/manor_all/void_echo.png',
    'assets/ftboss/textures/fx/manor_all/whiteout_edict.png',
    'assets/ftboss/textures/fx/manor_all/wingstep.png',
    'assets/ftboss/textures/fx/manor_all/winter_clock.png'
)
$langPaths = @('assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json')
$newClasses = @(
    'net/farmingtales/ftboss/util/ManorChaseRules.class',
    'net/farmingtales/ftboss/integ/spells/RegularBossCastHost.class',
    'net/farmingtales/ftboss/boss/ai/ManorFlyBan.class',
    'net/farmingtales/ftboss/boss/ai/ManorChaseTracker.class',
    'net/farmingtales/ftboss/boss/ai/NonManorLootCrateDrops.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$FoodType.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$SkillEffect.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$EvolutionSkill.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$DailyRewardType.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$Profile.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$Socket.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$SocketTier.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$SocketStats.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$FoodBlessingStats.class',
    'net/farmingtales/ftboss/item/weapons/GourmetEvolutionWeapon.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$WeaponRole.class',
    'net/farmingtales/ftboss/client/render/GourmetBlankBladeRenderer$Model.class',
    'net/farmingtales/ftboss/client/render/GourmetBlankBladeRenderer.class',
    'net/farmingtales/ftboss/item/weapons/GourmetBlankBladeItem$1.class',
    'net/farmingtales/ftboss/item/weapons/GourmetBlankBladeItem.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$EvolutionStat.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$AcquiredSkill.class',
    'net/farmingtales/ftboss/item/FoodWeaponEvolution$1.class',
    'net/farmingtales/ftboss/item/StatueFoodAura.class',
    'net/farmingtales/ftboss/item/StatueFoodAura$Aura.class',
    'net/farmingtales/ftboss/skill/WeaponEvolutionSkills.class',
    'net/farmingtales/ftboss/skill/WeaponEvolutionSkills$1.class',
    'net/farmingtales/ftboss/skill/WeaponEvolutionSkills$Outcome.class',
    'net/farmingtales/ftboss/skill/WeaponEvolutionSkills$Shape.class',
    'net/farmingtales/ftboss/skill/WeaponEvolutionSkills$State.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$1.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$Aspect.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$BaseStyle.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$Fusion.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$FusionStyle.class',
    'net/farmingtales/ftboss/util/WeaponEvolutionSkillPool$Pair.class'
)

if (-not (Test-Path -LiteralPath $Target)) { throw "目标 jar 不存在: $Target" }
if (-not (Test-Path -LiteralPath $Source)) { throw "来源 jar 不存在: $Source" }
$Target = (Get-Item -LiteralPath $Target).FullName
$Source = (Get-Item -LiteralPath $Source).FullName
$tools = $PSScriptRoot

function Resolve-JarTool {
    param([string]$Explicit)
    $candidates = @()
    if ($Explicit) { $candidates += $Explicit }
    if ($env:JAVA_HOME) { $candidates += (Join-Path $env:JAVA_HOME 'bin\jar.exe') }
    $candidates += 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\jar.exe'
    $candidates += 'C:\Program Files\Zulu\zulu-21\bin\jar.exe'
    foreach ($c in $candidates) { if ($c -and (Test-Path -LiteralPath $c)) { return $c } }
    throw '找不到 jar.exe（JDK 自带）。用 -JarTool 指定，或设好 JAVA_HOME。'
}

function Read-EntryText($zip, $name) {
    $e = $zip.Entries | Where-Object { $_.FullName -eq $name }
    if (-not $e) { return $null }
    $sr = New-Object System.IO.StreamReader($e.Open(), [System.Text.Encoding]::UTF8)
    $t = $sr.ReadToEnd(); $sr.Dispose(); return $t
}
function Read-EntryBytes($zip, $name) {
    $e = $zip.Entries | Where-Object { $_.FullName -eq $name }
    if (-not $e) { return $null }
    $ms = New-Object System.IO.MemoryStream
    $s = $e.Open(); $s.CopyTo($ms); $s.Dispose()
    return $ms.ToArray()
}
function Assert-ZipRules([string]$Path, [string]$Label) {
    $out = & node (Join-Path $tools 'verification\check_jar_zip_rules.cjs') $Path 2>&1
    $code = $LASTEXITCODE
    $out | ForEach-Object { Write-Host ("  " + $_) }
    if ($code -ne 0) { throw "$Label 的 zip 条目不合 Forge 规则（见上）—— 装进游戏会让 ModLauncher 直接退出" }
}

# ★ 与 Forge 完全同源的第二道闸：JDK 自己的 ZipInputStream 会逐条读本地文件头，
#   正是抛 "only DEFLATED entries can have EXT descriptor" 的那段代码。
function Assert-ReadableByJdk([string]$Path, [string]$Label) {
    $javaHome = $null
    if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        $javaHome = $env:JAVA_HOME
    } elseif (Test-Path -LiteralPath 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\javac.exe') {
        $javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    }
    if (-not $javaHome) { Write-Host '  [跳过] 没找到带 javac 的 JDK —— 只跑了 zip 规则检查'; return }
    $outDir = Join-Path ([System.IO.Path]::GetTempPath()) ('zipcheck_' + [System.Guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    try {
        & (Join-Path $javaHome 'bin\javac.exe') -encoding UTF-8 -d $outDir (Join-Path $tools 'verification\ZipStreamCheck.java') 2>&1 |
            Where-Object { $_ } | ForEach-Object { Write-Host ("  javac: " + $_) }
        $out = & (Join-Path $javaHome 'bin\java.exe') -cp $outDir ZipStreamCheck $Path 2>&1
        $code = $LASTEXITCODE
        $out | ForEach-Object { Write-Host ("  " + $_) }
        if ($code -ne 0) { throw "$Label 用 JDK 的 ZipInputStream 读不通（Forge 启动期会同样失败）" }
    } finally {
        Remove-Item -LiteralPath $outDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

$jarTool = Resolve-JarTool -Explicit $JarTool
Write-Host ("jar 工具: {0}" -f $jarTool)
Write-Host '--- ① 合并前校验 zip 规则 ---'
Assert-ZipRules $Source '来源 jar'
Assert-ZipRules $Target '目标 jar'

$work = Join-Path ([System.IO.Path]::GetTempPath()) ('ftmerge_' + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $work | Out-Null
try {
    Write-Host '--- ② 解包目标 jar ---'
    Push-Location $work
    try { & $jarTool xf $Target; if ($LASTEXITCODE -ne 0) { throw 'jar xf 失败' } } finally { Pop-Location }

    Write-Host '--- ③ 搬运保留条目 ---'
    $srcZip = [System.IO.Compression.ZipFile]::OpenRead($Source)
    $copied = @()
    foreach ($name in $entries) {
        $bytes = Read-EntryBytes $srcZip $name
        if ($null -eq $bytes) { Write-Host ("  [跳过] 来源里没有 {0}" -f $name); continue }
        $dest = Join-Path $work ($name -replace '/', '\')
        New-Item -ItemType Directory -Force -Path (Split-Path $dest -Parent) | Out-Null
        [System.IO.File]::WriteAllBytes($dest, $bytes)
        $copied += $name
    }
    Write-Host '--- ④ lang 只补键 ---'
    $nodeTool = Join-Path $tools 'merge_lang_keys.cjs'
    foreach ($langPath in $langPaths) {
        $srcText = Read-EntryText $srcZip $langPath
        if ($null -eq $srcText) { Write-Host ("  [跳过] 来源里没有 {0}" -f $langPath); continue }
        $srcTmp = Join-Path $work ('_src_' + (Split-Path $langPath -Leaf))
        [System.IO.File]::WriteAllText($srcTmp, $srcText, (New-Object System.Text.UTF8Encoding($false)))
        & node $nodeTool (Join-Path $work ($langPath -replace '/', '\')) $srcTmp
        if ($LASTEXITCODE -ne 0) { throw ("merge_lang_keys.cjs 失败: {0}" -f $langPath) }
        Remove-Item -LiteralPath $srcTmp -Force
    }
    $srcZip.Dispose()

    Write-Host '--- ⑤ 用 JDK jar 重新打包 ---'
    $tmpJar = Join-Path $work 'repacked.jar'
    $manifest = Join-Path $work 'META-INF\MANIFEST.MF'
    $jarArgs = @('--create', '--file', $tmpJar)
    if (Test-Path -LiteralPath $manifest) { $jarArgs += @('--manifest', $manifest) }
    $jarArgs += @('-C', $work, '.')
    & $jarTool @jarArgs
    if ($LASTEXITCODE -ne 0) { throw 'jar --create 失败' }
    Move-Item -LiteralPath $tmpJar -Destination $Target -Force
} finally {
    Remove-Item -LiteralPath $work -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host '--- ⑥ 复核 ---'
Assert-ZipRules $Target '合并后的 jar'
Assert-ReadableByJdk $Target '合并后的 jar'
$verify = [System.IO.Compression.ZipFile]::OpenRead($Target)
$names = $verify.Entries | ForEach-Object { $_.FullName }
$entryCount = $verify.Entries.Count
$zh = (Read-EntryText $verify 'assets/ftboss/lang/zh_cn.json') | ConvertFrom-Json
$verify.Dispose()
foreach ($n in $copied) { Write-Host ("  + {0}" -f $n) }
$want = $entries + $newClasses
$bad = @($want | Where-Object { $names -notcontains $_ })
if ($bad.Count -gt 0) {
    Write-Host '缺失条目:' -ForegroundColor Red
    $bad | ForEach-Object { Write-Host ("  ! {0}" -f $_) }
    exit 1
}
Write-Host ("合并完成: {0} 条目（搬运 {1} 项 + 新功能类 {2} 项都在）" -f $entryCount, $copied.Count, $newClasses.Count)
Write-Host ("头衔风味文本抽查: {0}" -f $zh.'title.titles.ftboss.memories_end.flavor')
