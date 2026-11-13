param(
    [Parameter(Mandatory = $true)][string]$InstanceRoot
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = Split-Path -Parent $PSScriptRoot
$root = (Resolve-Path -LiteralPath $InstanceRoot).Path
$mods = Join-Path $root 'mods'
$live = Join-Path $mods 'qys-event-forge-1.20.1-1.0.6.jar'
$built = Join-Path $projectRoot 'build/libs/qys-event-forge-1.20.1-1.0.6.jar'
$output = Join-Path $projectRoot 'build/gold-pot-wishes'
$candidate = Join-Path $output 'qys-event-forge-1.20.1-1.0.6.jar'
$prefix = 'cn/farmingtales/qysevent/wishingwell/'

function Assert-GameClosed {
    $games = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {
        $_.CommandLine -match [regex]::Escape($root) -and $_.CommandLine -match 'BootstrapLauncher|--gameDir|net.minecraft'
    }
    if ($games) { throw 'Close this Minecraft instance before installing the Event update.' }
}

function Entry-Hash($entry) {
    $stream = $entry.Open()
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '') }
    finally { $stream.Dispose(); $sha.Dispose() }
}

Assert-GameClosed
New-Item -ItemType Directory -Path $output -Force | Out-Null
$lock = [IO.File]::Open((Join-Path $mods '.qys-event-forge-1.20.1-1.0.6.jar.shared-install.lock'),
    [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
try {
    $baselineHash = (Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash
    Copy-Item -LiteralPath $live -Destination $candidate -Force
    $source = [IO.Compression.ZipFile]::OpenRead($built)
    $target = [IO.Compression.ZipFile]::Open($candidate, [IO.Compression.ZipArchiveMode]::Update)
    $added = @()
    try {
        foreach ($entry in $source.Entries) {
            if (!$entry.FullName.StartsWith($prefix) -or !$entry.FullName.EndsWith('.class')) { continue }
            $existing = $target.GetEntry($entry.FullName)
            if ($existing) { $existing.Delete() }
            $created = $target.CreateEntry($entry.FullName, [IO.Compression.CompressionLevel]::Optimal)
            $created.LastWriteTime = [DateTimeOffset]::new(2026, 10, 12, 0, 0, 0, [TimeSpan]::Zero)
            $input = $entry.Open()
            $destination = $created.Open()
            try { $input.CopyTo($destination) }
            finally { $input.Dispose(); $destination.Dispose() }
            $added += $entry.FullName
        }
    } finally { $source.Dispose(); $target.Dispose() }
    if ($added.Count -ne 5) { throw "Expected 5 new wish classes, got $($added.Count)." }

    $baseline = [IO.Compression.ZipFile]::OpenRead($live)
    $updated = [IO.Compression.ZipFile]::OpenRead($candidate)
    $preserved = 0
    try {
        $seen = [Collections.Generic.HashSet[string]]::new()
        foreach ($entry in $updated.Entries) {
            if (!$seen.Add($entry.FullName)) { throw "Duplicate ZIP entry: $($entry.FullName)" }
            if (!$entry.FullName.StartsWith($prefix) -and !$baseline.GetEntry($entry.FullName)) {
                throw "Unexpected added ZIP entry: $($entry.FullName)"
            }
        }
        foreach ($entry in $baseline.Entries) {
            if ($entry.FullName.StartsWith($prefix)) { continue }
            $replacement = $updated.GetEntry($entry.FullName)
            if (!$replacement -or (Entry-Hash $entry) -ne (Entry-Hash $replacement)) {
                throw "Existing Event content changed: $($entry.FullName)"
            }
            $preserved++
        }
    } finally { $baseline.Dispose(); $updated.Dispose() }

    Assert-GameClosed
    if ((Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash -ne $baselineHash) {
        throw 'Event JAR changed concurrently; refusing to overwrite it.'
    }
    $pending = [IO.Path]::GetFullPath((Join-Path $mods '.gold-pot-wishes.pending'))
    if (!$pending.StartsWith([IO.Path]::GetFullPath($mods) + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase)) { throw 'Pending target escaped the mods directory.' }
    Copy-Item -LiteralPath $candidate -Destination $pending -Force
    $rollback = Join-Path $output ('event-before-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.jar')
    Copy-Item -LiteralPath $live -Destination $rollback
    [IO.File]::Replace($pending, $live, [System.Management.Automation.Language.NullString]::Value)
    $candidateHash = (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash
    if ((Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash -ne $candidateHash) {
        throw 'Installed Event JAR hash did not match the validated candidate.'
    }
    Copy-Item -LiteralPath (Join-Path $root 'kubejs/server_scripts/block_events/wishing_well.js') -Destination $output -Force
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'gold-pot-wishes.md') -Destination $output -Force
    [ordered]@{
        instance = $root; baselineSha256 = $baselineHash; installedSha256 = $candidateHash
        installed = $live; backup = $rollback; addedClasses = $added; preservedEntries = $preserved
        testedInGame = $false; otherInstancesModified = $false
    } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $output 'deployment.json') -Encoding utf8
    Write-Output "Installed gold-pot wishes. Preserved $preserved Event entries; added $($added.Count) classes. SHA256=$candidateHash"
} finally { $lock.Dispose() }
