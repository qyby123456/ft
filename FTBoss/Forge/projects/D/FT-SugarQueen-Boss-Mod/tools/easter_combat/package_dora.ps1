param(
    [Parameter(Mandatory=$true)][string]$Baseline,
    [Parameter(Mandatory=$true)][string]$Overlay,
    [Parameter(Mandatory=$true)][string]$Output
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Read-Archive([string]$Path) {
    $entries = [Collections.Generic.Dictionary[string,byte[]]]::new([StringComparer]::Ordinal)
    $archive = [IO.Compression.ZipFile]::OpenRead($Path)
    try {
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName.EndsWith('/')) { continue }
            if ($entries.ContainsKey($entry.FullName)) { throw "Duplicate ZIP entry: $($entry.FullName)" }
            $stream = $entry.Open()
            $memory = [IO.MemoryStream]::new()
            try {
                $stream.CopyTo($memory)
                $entries.Add($entry.FullName, $memory.ToArray())
            } finally {
                $memory.Dispose()
                $stream.Dispose()
            }
        }
    } finally { $archive.Dispose() }
    return ,$entries
}

function Byte-Hash([byte[]]$Bytes) {
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($algorithm.ComputeHash($Bytes)) }
    finally { $algorithm.Dispose() }
}

$baselinePath = (Resolve-Path -LiteralPath $Baseline).Path
$overlayPath = (Resolve-Path -LiteralPath $Overlay).Path
$outputPath = [IO.Path]::GetFullPath($Output)
if ($outputPath -eq $baselinePath -or $outputPath -eq $overlayPath) { throw 'Output cannot replace an input' }
if (Test-Path -LiteralPath $outputPath) { throw 'Output already exists; use a fresh output path' }
$original = Read-Archive $baselinePath
$patch = Read-Archive $overlayPath
$allowedClasses = '^net/farmingtales/ftboss/(entity/EasterBossEntity|client/model/EasterBossModel|effect/FTEffects|effect/LethalTempoEffect|util/DoraCombatTempo|boss/ai/GongdiSkills|util/GongdiCombatRules|util/HiddenBossCastRules|client/render/EasterSkillVisual)(\$[^/]+)?\.class$'
$allowedResources = @(
    'assets/ftboss/lang/zh_cn.json',
    'assets/ftboss/lang/en_us.json',
    'assets/ftboss/textures/mob_effect/lethal_tempo.png',
    'data/ftboss/damage_type/gongdi_guillotine.json',
    'data/minecraft/tags/damage_type/bypasses_armor.json',
    'data/minecraft/tags/damage_type/bypasses_shield.json',
    'data/minecraft/tags/damage_type/bypasses_enchantments.json',
    'data/minecraft/tags/damage_type/bypasses_resistance.json'
)
$changed = [Collections.Generic.List[string]]::new()
foreach ($name in $patch.Keys) {
    if ($name -eq 'META-INF/MANIFEST.MF') { continue }
    if ($name -notmatch $allowedClasses -and $name -notin $allowedResources) { throw "Unexpected overlay entry: $name" }
    if ($name -like 'assets/ftboss/lang/*') {
        $before = [Text.Encoding]::UTF8.GetString($original[$name]) | ConvertFrom-Json
        $after = [Text.Encoding]::UTF8.GetString($patch[$name]) | ConvertFrom-Json
        foreach ($property in $before.PSObject.Properties) {
            if ($property.Name -match '^(effect\.ftboss\.lethal_tempo(\.desc)?|death\.attack\.ftboss\.gongdi_guillotine(\.player)?)$') { continue }
            if ($after.PSObject.Properties[$property.Name].Value -cne $property.Value) {
                throw "Overlay would lose a current translation: $($property.Name)"
            }
        }
    }
    if ($name -like 'data/minecraft/tags/*' -and $original.ContainsKey($name)) {
        $before = [Text.Encoding]::UTF8.GetString($original[$name]) | ConvertFrom-Json
        $after = [Text.Encoding]::UTF8.GetString($patch[$name]) | ConvertFrom-Json
        foreach ($value in $before.values) {
            if ($value -notin $after.values) { throw "Overlay would lose a current damage tag: $value" }
        }
    }
    $original[$name] = $patch[$name]
    $changed.Add($name)
}
foreach ($required in @(
    'net/farmingtales/ftboss/entity/EasterBossEntity.class',
    'net/farmingtales/ftboss/effect/FTEffects.class',
    'net/farmingtales/ftboss/effect/LethalTempoEffect.class',
    'net/farmingtales/ftboss/util/DoraCombatTempo.class',
    'assets/ftboss/textures/mob_effect/lethal_tempo.png'
)) {
    if (!$changed.Contains($required)) { throw "Missing Dora payload: $required" }
}
$file = [IO.File]::Open($outputPath, [IO.FileMode]::CreateNew)
$archive = [IO.Compression.ZipArchive]::new($file, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($name in ($original.Keys | Sort-Object)) {
        $entry = $archive.CreateEntry($name, [IO.Compression.CompressionLevel]::Optimal)
        $entry.LastWriteTime = [DateTimeOffset]::new(2026, 9, 17, 0, 0, 0, [TimeSpan]::Zero)
        $stream = $entry.Open()
        try { $stream.Write($original[$name], 0, $original[$name].Length) }
        finally { $stream.Dispose() }
    }
} finally {
    $archive.Dispose()
    $file.Dispose()
}
$packed = Read-Archive $outputPath
if ($packed.Count -ne $original.Count) { throw 'Merged archive entry count mismatch' }
foreach ($name in $original.Keys) {
    if (!$packed.ContainsKey($name) -or (Byte-Hash $packed[$name]) -cne (Byte-Hash $original[$name])) {
        throw "Merged archive byte verification failed: $name"
    }
}
Write-Host "Verified $($packed.Count) entries; applied $($changed.Count) whitelisted entries; all other entry bytes preserved."
Get-FileHash -LiteralPath $outputPath -Algorithm SHA256 | Format-List
