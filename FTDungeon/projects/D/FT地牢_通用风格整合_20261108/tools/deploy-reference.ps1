param(
    [string]$GameDirectory = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2',
    [switch]$Deploy
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$game = [IO.Path]::GetFullPath($GameDirectory).TrimEnd('\')
$mods = Join-Path $game 'mods'
$built = Join-Path $root 'build\libs\ft-dungeon-mobs-forge-1.20.1-0.3.2.jar'
$destination = Join-Path $mods 'FT地牢怪物-Forge-1.20.1-0.3.2.jar'
$expectedPrevious = '3DDB894DF1916BE228641A2D8234697C216B733EE7009153BED68B65E56444EF'
$package = Get-Content -LiteralPath (Join-Path $root 'build\reports\reference-0.3.2-package.json') -Raw | ConvertFrom-Json
$builtHash = (Get-FileHash -LiteralPath $built -Algorithm SHA256).Hash
if ($package.status -ne 'PASS' -or $builtHash -ne $package.sha256) { throw 'Package audit is missing or stale' }

function Assert-InGame([string]$Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith($game + '\', [StringComparison]::OrdinalIgnoreCase)) { throw "Unsafe destination: $resolved" }
}

function Find-OwnJars {
    foreach ($file in Get-ChildItem -LiteralPath $mods -Filter '*.jar' -File) {
        $archive = [IO.Compression.ZipFile]::OpenRead($file.FullName)
        try {
            $entry = $archive.GetEntry('META-INF/mods.toml')
            if (-not $entry) { continue }
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
            foreach ($section in [regex]::Matches($metadata, '(?ms)^\s*\[\[mods\]\][ \t]*\r?\n(?<body>.*?)(?=^\s*\[|\z)')) {
                if ($section.Groups['body'].Value -match '(?m)^\s*modId\s*=\s*"ftdungeonmobs"') { $file; break }
            }
        } finally { $archive.Dispose() }
    }
}

$clients = @(Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^(java|javaw)\.exe$' -and ($_.CommandLine -match '--launchTarget\s+forgeclient|net\.minecraft\.client\.main\.Main' -or $_.CommandLine.Contains($game))
})
if ($clients.Count) { throw 'Minecraft client/server is running; exit it before deployment' }
$owned = @(Find-OwnJars)
if ($owned.Count -ne 1) { throw "Expected exactly one installed FT Dungeon Mobs jar, found $($owned.Count)" }
$previous = $owned[0].FullName
if ((Get-FileHash -LiteralPath $previous -Algorithm SHA256).Hash -ne $expectedPrevious) { throw 'Installed jar drifted from the backed-up 0.3.1 baseline' }
if (Test-Path -LiteralPath $destination) { throw 'Destination already exists' }
if (-not $Deploy) { [pscustomobject]@{Mode='DRY_RUN';Previous=$previous;Destination=$destination;SHA256=$builtHash}; return }

$lockPath = Join-Path $game '.ftdungeonmobs-deploy.lock'
$lock = [IO.File]::Open($lockPath, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
$backup = Join-Path $game ('ft-mod-backups\ftdungeonmobs\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$previousBackup = Join-Path $backup ([IO.Path]::GetFileName($previous))
$pending = $destination + '.pending'
$config = Join-Path $game 'config\ftdungeonmobs-common.toml'
$configHash = (Get-FileHash -LiteralPath $config -Algorithm SHA256).Hash
$unrelatedBefore = @(Get-ChildItem -LiteralPath $mods -File | Where-Object FullName -ne $previous | ForEach-Object { $_.Name + '|' + $_.Length + '|' + $_.LastWriteTimeUtc.Ticks })
foreach ($path in @($previous, $destination, $backup, $previousBackup, $pending, $lockPath)) { Assert-InGame $path }
$movedPrevious = $false
$installedNew = $false
try {
    New-Item -ItemType Directory -Path $backup | Out-Null
    Copy-Item -LiteralPath $config -Destination (Join-Path $backup 'ftdungeonmobs-common.toml')
    Copy-Item -LiteralPath $built -Destination $pending
    if ((Get-FileHash -LiteralPath $pending -Algorithm SHA256).Hash -ne $builtHash) { throw 'Staged jar hash mismatch' }
    if ((Get-FileHash -LiteralPath $previous -Algorithm SHA256).Hash -ne $expectedPrevious) { throw 'Installed jar changed during staging' }
    Move-Item -LiteralPath $previous -Destination $previousBackup
    $movedPrevious = $true
    Move-Item -LiteralPath $pending -Destination $destination
    $installedNew = $true
    $installedHash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash
    if ($installedHash -ne $builtHash) { throw 'Installed jar hash mismatch' }
    if ((Get-FileHash -LiteralPath $config -Algorithm SHA256).Hash -ne $configHash) { throw 'User config changed' }
    $unrelatedAfter = @(Get-ChildItem -LiteralPath $mods -File | Where-Object FullName -ne $destination | ForEach-Object { $_.Name + '|' + $_.Length + '|' + $_.LastWriteTimeUtc.Ticks })
    if (@(Compare-Object $unrelatedBefore $unrelatedAfter).Count) { throw 'Unrelated mod files changed during deployment' }
    if (@(Find-OwnJars).Count -ne 1) { throw 'Installed mod count mismatch' }
    $report = [ordered]@{status='PASS';version='0.3.2';deployedAt=(Get-Date -Format o);built=$built;installed=$destination;
        bytes=(Get-Item -LiteralPath $destination).Length;sha256=$installedHash.ToLowerInvariant();previousSHA256=$expectedPrevious.ToLowerInvariant();
        backupDirectory=$backup;configSHA256=$configHash.ToLowerInvariant();configUnchanged=$true;unrelatedModFilesUnchanged=$unrelatedAfter.Count;
        ownJarCount=1;clientRunning=$false;fullClientTested=$false;playerSavesModified=$false}
    $report | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'build\reports\deployment-0.3.2.json') -Encoding utf8
    $report | ConvertTo-Json
} catch {
    if ($installedNew -and (Test-Path -LiteralPath $destination)) { Move-Item -LiteralPath $destination -Destination (Join-Path $backup 'failed-candidate.jar') }
    if ($movedPrevious -and (Test-Path -LiteralPath $previousBackup)) { Move-Item -LiteralPath $previousBackup -Destination $previous }
    throw
} finally {
    if (Test-Path -LiteralPath $pending) { Remove-Item -LiteralPath $pending }
    $lock.Dispose()
    Remove-Item -LiteralPath $lockPath
}
