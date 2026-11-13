param(
    [string]$Instance = (Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'),
    [switch]$Deploy
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root = Split-Path -Parent $PSScriptRoot
$packageReport = Get-Content -LiteralPath (Join-Path $root 'build\reports\challenge-package.json') -Raw | ConvertFrom-Json
if ($packageReport.status -ne 'PASS') { throw 'Package verification must pass before deployment.' }
$source = [IO.Path]::GetFullPath((Join-Path $root 'build\libs\ft-dungeon-challenges-forge-1.20.1-1.0.0.jar'))
$sourceHash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
if ($sourceHash -ne $packageReport.sha256) { throw 'Built JAR changed after package verification.' }
$instanceRoot = (Resolve-Path -LiteralPath $Instance).Path
$mods = Join-Path $instanceRoot 'mods'
$target = [IO.Path]::GetFullPath((Join-Path $mods 'FT地牢挑战-Forge-1.20.1-1.0.0.jar'))
if (!$target.StartsWith([IO.Path]::GetFullPath($mods) + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Destination escaped the explicitly selected mods directory.'
}

function Get-ModIds([string]$Path) {
    $archive = [IO.Compression.ZipFile]::OpenRead($Path)
    try {
        $entry = $archive.GetEntry('META-INF/mods.toml')
        if (!$entry) { return @() }
        $reader = [IO.StreamReader]::new($entry.Open(), [Text.Encoding]::UTF8)
        try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
        return @([regex]::Matches($text, '(?ms)^\s*\[\[mods\]\]\s*\r?\n(.*?)(?=^\s*\[|\z)') | ForEach-Object {
            $match = [regex]::Match($_.Groups[1].Value, '(?m)^\s*modId\s*=\s*"([^"]+)"')
            if ($match.Success) { $match.Groups[1].Value }
        })
    } finally { $archive.Dispose() }
}

function Get-ConfigHashes {
    $values = [ordered]@{}
    foreach ($name in @('ftdungeonmobs-common.toml', 'ftdungeonmobs-challenges.toml')) {
        $file = Join-Path $instanceRoot ('config\' + $name)
        $values[$name] = if (Test-Path -LiteralPath $file) { (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash } else { $null }
    }
    return $values
}

function Get-ModInventory {
    return @(Get-ChildItem -LiteralPath $mods -File -Filter '*.jar' | Sort-Object Name | ForEach-Object {
        [pscustomobject]@{ name = $_.Name; bytes = $_.Length; modified = $_.LastWriteTimeUtc.Ticks }
    })
}

$baseJars = @()
$existing = @()
foreach ($file in Get-ChildItem -LiteralPath $mods -File -Filter '*.jar') {
    $ids = @(Get-ModIds $file.FullName)
    if ($ids -contains 'ftdungeonmobs') { $baseJars += $file.FullName }
    if ($ids -contains 'ftdungeonchallenges') { $existing += $file.FullName }
}
if ($baseJars.Count -ne 1) { throw 'Exactly one FT Dungeon Mobs base JAR is required.' }
$baseHash = (Get-FileHash -LiteralPath $baseJars[0] -Algorithm SHA256).Hash.ToLowerInvariant()
if ($baseHash -ne '877d795f9bf0e86803d8105035930b8ba0c012c6f49da2482b5553ca44e8891e') {
    throw 'The installed base JAR is not the tested 0.5.0 build. Revalidate compatibility before deploying.'
}
if ($existing.Count -gt 0 -or (Test-Path -LiteralPath $target)) { throw 'An existing challenge module must be handled explicitly; refusing to overwrite it.' }
$clients = @(Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^java(w)?\.exe$' -and $_.CommandLine -match 'net\.minecraft\.client\.main\.Main|--launchTarget\s+forgeclient'
})
if ($clients.Count -gt 0) { throw 'Close the Minecraft client before deploying.' }
$before = Get-ModInventory
$configuration = Get-ConfigHashes
if (!$Deploy) {
    [pscustomobject]@{ mode = 'DRY_RUN'; source = $source; target = $target; sha256 = $sourceHash; baseJar = $baseJars[0]; otherMods = $before.Count } | Format-List
    return
}
$lock = [IO.File]::Open((Join-Path $instanceRoot '.ftdungeonchallenges-deploy.lock'), [IO.FileMode]::OpenOrCreate,
        [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
$staging = $target + '.staging-' + [guid]::NewGuid().ToString('N')
$installed = $false
$backup = Join-Path $instanceRoot ('ft-mod-backups\ftdungeonchallenges\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
try {
    New-Item -ItemType Directory -Path $backup -Force | Out-Null
    $before | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath (Join-Path $backup 'mods-before.json') -Encoding utf8
    $configuration | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $backup 'config-hashes-before.json') -Encoding utf8
    foreach ($name in $configuration.Keys) {
        $file = Join-Path $instanceRoot ('config\' + $name)
        if (Test-Path -LiteralPath $file) { Copy-Item -LiteralPath $file -Destination (Join-Path $backup $name) }
    }
    Copy-Item -LiteralPath $source -Destination $staging
    if ((Get-FileHash -LiteralPath $staging -Algorithm SHA256).Hash.ToLowerInvariant() -ne $sourceHash) { throw 'Staging hash mismatch.' }
    Move-Item -LiteralPath $staging -Destination $target
    $installed = $true
    $unrelated = @(Get-ModInventory | Where-Object name -ne (Split-Path -Leaf $target))
    if (($before | ConvertTo-Json -Compress) -ne ($unrelated | ConvertTo-Json -Compress)) { throw 'Other mod inventory changed during deployment.' }
    if (($configuration | ConvertTo-Json -Compress) -ne ((Get-ConfigHashes) | ConvertTo-Json -Compress)) { throw 'Configuration changed during deployment.' }
    if ((Get-FileHash -LiteralPath $baseJars[0] -Algorithm SHA256).Hash.ToLowerInvariant() -ne $baseHash) { throw 'Base mod changed during deployment.' }
    $report = [ordered]@{ status = 'PASS'; time = (Get-Date).ToString('o'); installed = $target; sha256 = $sourceHash;
        baseJar = $baseJars[0]; baseSha256 = $baseHash; untouchedOtherMods = $before.Count; configurations = $configuration; backup = $backup;
        playerWorldsModified = $false; gameLaunched = $false }
    $report | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $root 'build\reports\challenge-deployment.json') -Encoding utf8
    $report | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $backup 'deployment.json') -Encoding utf8
    $report | Format-List
} catch {
    if ($installed -and (Test-Path -LiteralPath $target) -and
            (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -eq $sourceHash) {
        Remove-Item -LiteralPath $target
    }
    throw
} finally {
    if (Test-Path -LiteralPath $staging) { Remove-Item -LiteralPath $staging }
    $lock.Dispose()
}
