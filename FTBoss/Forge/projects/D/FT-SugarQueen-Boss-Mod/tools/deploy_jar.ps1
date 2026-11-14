param(
    [switch]$NoBuild,
    [string]$ArtifactPath,
    [string]$TargetPath,
    [string]$ExpectedTargetHash
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Split-Path -Parent $PSScriptRoot)).Path
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales Forge'
if ($ArtifactPath -and -not $NoBuild) { throw 'ArtifactPath requires -NoBuild.' }
if ($TargetPath -and -not $NoBuild) { throw 'TargetPath requires -NoBuild.' }
if ($ExpectedTargetHash -and -not $TargetPath) { throw 'ExpectedTargetHash requires TargetPath.' }
if (-not $ArtifactPath) { $ArtifactPath = Join-Path $project 'build\libs\qys-bosses.jar' }
$artifact = [IO.Path]::GetFullPath($ArtifactPath)

function Test-GameUsesInstance([string]$CommandLine, [string]$InstancePath) {
    $instanceRoot = [IO.Path]::GetFullPath($InstancePath).TrimEnd('\', '/')
    if ($CommandLine.IndexOf($instanceRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0) { return $true }
    $directoryMatch = [regex]::Match($CommandLine, '(?i)(?:^|\s)--gameDir(?:=|\s+)(?:"(?<path>[^"]+)"|(?<path>[^\s"]+))(?=\s|$)')
    if (-not $directoryMatch.Success) { return $true }
    $gameDirectory = $directoryMatch.Groups['path'].Value
    if (-not [IO.Path]::IsPathRooted($gameDirectory)) { return $true }
    try {
        $gameDirectory = [IO.Path]::GetFullPath($gameDirectory).TrimEnd('\', '/')
        return [string]::Equals($gameDirectory, $instanceRoot, [StringComparison]::OrdinalIgnoreCase)
    } catch {
        return $true
    }
}

function Assert-GameStopped {
    $game = Get-CimInstance Win32_Process -Filter "name='java.exe' OR name='javaw.exe'" -ErrorAction Stop |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($TargetPath) {
        $targetInstance = Split-Path -Parent (Split-Path -Parent ([IO.Path]::GetFullPath($TargetPath)))
        $game = @($game | Where-Object { Test-GameUsesInstance $_.CommandLine $targetInstance })
    }
    if ($game) {
        Write-Warning "Minecraft is running (PID $($game.ProcessId -join ',')). Deployment skipped; artifact retained."
        exit 2
    }
}

function Assert-Destination([string]$Path) {
    $absolute = [IO.Path]::GetFullPath($Path)
    $profileRoot = [IO.Path]::GetFullPath($env:USERPROFILE)
    if (-not $absolute.StartsWith($profileRoot + '\', [StringComparison]::OrdinalIgnoreCase)) {
        throw "Destination escapes user profile: $absolute"
    }
    $ancestor = $absolute
    while ($ancestor -and $ancestor -ne $profileRoot) {
        if ((Test-Path -LiteralPath $ancestor) -and ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
            $tagOutput = & fsutil reparsepoint query $ancestor
            if ($LASTEXITCODE -ne 0) { throw "Cannot verify reparse point: $ancestor" }
            $tag = [regex]::Match(($tagOutput -join ' '), '(?i)0x[0-9a-f]{8}').Value
            if ($tag -notmatch '(?i)^0x9000[0-9a-f]01a$') { throw "Refusing redirected path: $ancestor ($tag)" }
        }
        $ancestor = Split-Path -Parent $ancestor
    }
    return $absolute
}

function Read-ZipText($Zip, [string]$Name) {
    $entry = $Zip.GetEntry($Name)
    if (-not $entry) { return '' }
    $reader = New-Object IO.StreamReader($entry.Open())
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}

function Backup-BundledTridents([string]$Directory) {
    foreach ($candidate in Get-ChildItem -LiteralPath $Directory -Filter '*.jar' -File) {
        if ($candidate.Name -eq 'qys-bosses.jar') { continue }
        $zip = [IO.Compression.ZipFile]::OpenRead($candidate.FullName)
        try { $toml = Read-ZipText $zip 'META-INF/mods.toml' } finally { $zip.Dispose() }
        $declaresTridents = $false
        foreach ($section in [regex]::Matches($toml, '(?ms)^\s*\[\[mods\]\]\s*(.*?)(?=^\s*\[|\z)')) {
            if ($section.Groups[1].Value -match '(?m)^\s*modId\s*=\s*["'']tridents_n_stuff["'']\s*$') {
                $declaresTridents = $true
            }
        }
        if (-not $declaresTridents) { continue }
        $source = Assert-Destination $candidate.FullName
        $hash = (Get-FileHash -LiteralPath $source).Hash
        $backupRoot = $Directory
        if ((Split-Path -Leaf $Directory) -eq 'mods') { $backupRoot = Split-Path -Parent $Directory }
        $backup = Assert-Destination (Join-Path $backupRoot "ftboss-merged-mod-backups\$hash\$($candidate.Name)")
        New-Item -ItemType Directory -Path (Split-Path -Parent $backup) -Force | Out-Null
        if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $source -Destination $backup }
        if ((Get-FileHash -LiteralPath $backup).Hash -ne $hash) { throw "Tridents backup hash mismatch: $backup" }
        [PSCustomObject]@{ Source = $source; Backup = $backup; Hash = $hash }
    }
}

if (-not $NoBuild) {
    if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        $env:JAVA_HOME = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    }
    if (-not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) { throw 'JDK 17 not found.' }
    $gradle = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\bin\gradle.bat" |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $gradle) { throw 'Gradle 8.8 cache not found.' }
    $preference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $gradle -p $project clean build --console=plain --no-daemon
        $buildExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $preference
    }
    if ($buildExitCode -ne 0) { throw 'Build failed; refusing deployment.' }
}
if (-not (Test-Path -LiteralPath $artifact -PathType Leaf)) { throw "Artifact missing: $artifact" }
Assert-GameStopped
$sourceHash = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
Add-Type -AssemblyName System.IO.Compression.FileSystem
$packed = [IO.Compression.ZipFile]::OpenRead($artifact)
try {
    $metadataText = Read-ZipText $packed 'META-INF/jarjar/metadata.json'
    $bundlesTridents = $false
    if ($metadataText) {
        $metadata = $metadataText | ConvertFrom-Json
        foreach ($dependency in $metadata.jars) {
            if ($dependency.identifier.artifact -eq 'tridents_n_stuff') {
                if (-not $packed.GetEntry($dependency.path)) { throw 'Embedded Tridents jar missing; refusing deployment.' }
                $bundlesTridents = $true
            }
        }
    }
} finally { $packed.Dispose() }

$betaInstance = 'FarmingTales_Forge' + (-join ([char[]]@(0x2161))) + ' v1.5.2_beta3'
$currentInstance = 'FarmingTales_Forge' + (-join ([char[]]@(0x2161))) + ' v1.5.2'
$versions = Join-Path (Join-Path $env:APPDATA '.minecraft') 'versions'
$destinations = @(
    (Join-Path $versions "$betaInstance\mods\qys-bosses.jar"),
    (Join-Path $versions "$currentInstance\mods\qys-bosses.jar")
)
if ($TargetPath) {
    $selected = Assert-Destination $TargetPath
    if ((Split-Path -Leaf $selected) -ne 'qys-bosses.jar' -or
        (Split-Path -Leaf (Split-Path -Parent $selected)) -ne 'mods') {
        throw "TargetPath must name mods/qys-bosses.jar: $selected"
    }
    $destinations = @($selected)
}
foreach ($destination in $destinations) {
    if (Test-Path -LiteralPath (Split-Path -Parent $destination)) {
        $null = Assert-Destination $destination
        $null = Assert-Destination ($destination + '.deploy.tmp')
    }
}
$tridentsBackups = @{}
if ($bundlesTridents) {
    foreach ($destination in $destinations) {
        $directory = Split-Path -Parent $destination
        if (Test-Path -LiteralPath $directory) {
            Assert-GameStopped
            $tridentsBackups[$destination] = @(Backup-BundledTridents $directory)
        }
    }
}
foreach ($destination in $destinations) {
    if (-not (Test-Path -LiteralPath (Split-Path -Parent $destination))) {
        Write-Warning "Destination directory missing; skipped: $destination"
        continue
    }
    Assert-GameStopped
    $target = Assert-Destination $destination
    $temporary = Assert-Destination ($destination + '.deploy.tmp')
    Copy-Item -LiteralPath $artifact -Destination $temporary -Force
    if ((Get-FileHash -LiteralPath $temporary).Hash -ne $sourceHash) { throw "Temporary hash mismatch: $temporary" }
    Assert-GameStopped
    if ($ExpectedTargetHash -and (Get-FileHash -LiteralPath $target).Hash -ne $ExpectedTargetHash) {
        throw "Target changed during preparation; refusing overwrite: $target"
    }
    if ($TargetPath -and (Test-Path -LiteralPath $target)) {
        [IO.File]::Replace($temporary, $target, [System.Management.Automation.Language.NullString]::Value)
    } else {
        Move-Item -LiteralPath $temporary -Destination $target -Force
    }
    if ((Get-FileHash -LiteralPath $target).Hash -ne $sourceHash) { throw "Deployment hash mismatch: $target" }
    foreach ($backup in $tridentsBackups[$destination]) {
        Assert-GameStopped
        $standalone = Assert-Destination $backup.Source
        if ((Get-FileHash -LiteralPath $standalone).Hash -ne $backup.Hash -or
            (Get-FileHash -LiteralPath $backup.Backup).Hash -ne $backup.Hash) {
            throw "Tridents changed during deployment: $standalone"
        }
        Remove-Item -LiteralPath $standalone
        Write-Output "[merged] $standalone (backup: $($backup.Backup))"
    }
    Write-Output "[OK] $target"
}
if ((Get-FileHash -LiteralPath $artifact).Hash -ne $sourceHash) { throw 'Artifact changed during deployment.' }
Assert-GameStopped
<#
Lightspeed / ModernFix 的资源索引会把"同名同路径但内容变了"的资源继续读成旧的
（本工程反复踩过）。换了 jar 就顺手清掉写入目标的那个实例的缓存。

2026-09-18：以前只认主实例 `FarmingTales Forge`；现在写入目标是 152beta3，
所以清 152beta3 的缓存。主实例仍然清一次是**故意的**：它的 mods 里还留着上一版
`qys-bosses.jar`，用户哪天回主实例测试时不该再吃到旧索引。
#>
$instances = @(
    (Join-Path $versions $betaInstance),
    (Join-Path $versions $currentInstance),
    (Join-Path $versions 'FarmingTales Forge')
)
if ($TargetPath) { $instances = @((Split-Path -Parent (Split-Path -Parent ([IO.Path]::GetFullPath($TargetPath))))) }
foreach ($target in $instances) {
    if (-not (Test-Path -LiteralPath $target)) { Write-Warning "Instance missing; cache not cleaned: $target"; continue }
    $expectedCache = [IO.Path]::GetFullPath((Join-Path $target 'lightspeed-cache'))
    $cache = Assert-Destination $expectedCache
    if ($cache -ne $expectedCache -or (Split-Path -Parent $cache) -ne [IO.Path]::GetFullPath($target)) {
        throw "Unexpected cache directory: $cache"
    }
    if (Test-Path -LiteralPath $cache) {
        Remove-Item -LiteralPath $cache -Recurse -Force
        Write-Output "[cache] removed $cache"
    }
}
Write-Output "[done] SHA256=$sourceHash"
