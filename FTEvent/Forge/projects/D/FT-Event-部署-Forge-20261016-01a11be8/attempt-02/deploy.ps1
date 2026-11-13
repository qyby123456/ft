$ErrorActionPreference = 'Stop'
$instance = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$candidateRoot = 'D:\ft工程文件\FT-Event-金币锅奖池调整-Forge-20261016-01a11be8'
$expectedJar = 'B0D747554C43A2A7C4796DFE2B0CD480A90654624EE33328010274398A6B8C1C'
$expectedScript = '77F44ED9470D833FC682ADEFDB8AB9A82690A036E19268A19E4D1B269D3AF4A7'
$expectedPreviousScript = '3584FE6F9524427290B52E574A40BA2A508A843C90AEF75B588B0DD930BAE646'
$expectedDisabled = '262B32847663B253CE5F117ED6B6DE49817A0016AF6EE75F60149A08A4D27B06'
$sourceJar = Join-Path $candidateRoot 'deliverables\qys-event-forge-1.20.1-1.0.7.jar'
$sourceScript = Join-Path $candidateRoot 'integration\kubejs\server_scripts\block_events\wishing_well.js'
$targetJar = Join-Path $instance 'mods\qys-event-forge-1.20.1-1.0.7.jar'
$targetScript = Join-Path $instance 'kubejs\server_scripts\block_events\wishing_well.js'
$disabledJar = Join-Path $instance 'mods\qys-event-forge-1.20.1-1.0.6.jar.disabled'
$bossJar = Join-Path $instance 'mods\qys-bosses.jar'
$backupRoot = Join-Path $PSScriptRoot 'backups'
$receiptPath = Join-Path $PSScriptRoot 'deployment-receipt.json'
$token = [Guid]::NewGuid().ToString('N')
$stagedJar = Join-Path $instance ('mods\.qys-event-1.0.7.' + $token + '.pending')
$stagedScript = Join-Path $instance ('kubejs\server_scripts\block_events\.wishing_well.' + $token + '.pending')
$rollbackScript = Join-Path $instance ('kubejs\server_scripts\block_events\.wishing_well.' + $token + '.restore')
$failedJar = Join-Path $instance ('mods\.qys-event-1.0.7.' + $token + '.rolled-back')
$lockPaths = @(
    (Join-Path $instance '.ftboss-152-deploy.lock'),
    (Join-Path $instance 'mods\.qys-bosses-install.lock'),
    (Join-Path $instance 'mods\.qys-event-forge-1.20.1-1.0.6.jar.shared-install.lock'),
    (Join-Path $instance 'mods\.qys-event-forge-1.20.1-1.0.7.jar.shared-install.lock')
)
$locks = [Collections.Generic.List[IO.FileStream]]::new()
$scriptCommitted = $false
$jarCommitted = $false
$receipt = [ordered]@{
    schemaVersion = 1
    clientDate = '2026-10-16'
    timestampChina = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    status = 'PRECHECK'
    authorization = 'User explicitly requested deployment of Event 1.0.7, superseding the previous disabled-only state; no game launch or save access authorized.'
    instanceRoot = $instance
    candidateRoot = $candidateRoot
    locks = $lockPaths
    gameStartedByAgent = $false
    savesModified = $false
    neoForgeModified = $false
    resourcePacksModified = $false
    bossJarModified = $false
}

function Assert-InstancePath([string]$Path) {
    $absolute = [IO.Path]::GetFullPath($Path)
    if (!$absolute.StartsWith($instance + '\', [StringComparison]::OrdinalIgnoreCase)) {
        throw ('Deployment path escaped the intended Forge instance: ' + $absolute)
    }
}

function Get-Sha256([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

function Assert-Hash([string]$Path, [string]$Expected) {
    if (!(Test-Path -LiteralPath $Path -PathType Leaf) -or (Get-Sha256 $Path) -ne $Expected) {
        throw ('Missing file or unexpected SHA256: ' + $Path)
    }
}

function Assert-NoJavaProcess {
    $processes = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'")
    if ($processes.Count) {
        throw ('A Java/game process is running; do not replace mods: ' + ($processes.ProcessId -join ', '))
    }
}

function Get-ActiveEventJars {
    $result = @()
    foreach ($file in (Get-ChildItem -LiteralPath (Join-Path $instance 'mods') -File | Where-Object { $_.Extension -ieq '.jar' })) {
        $archive = [IO.Compression.ZipFile]::OpenRead($file.FullName)
        try {
            $entry = $archive.GetEntry('META-INF/mods.toml')
            if (!$entry) { continue }
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if ($metadata -match '(?m)^\s*modId\s*=\s*"qys_event"\s*$') {
                $result += $file.FullName
            }
        } finally { $archive.Dispose() }
    }
    return $result
}

function Save-Receipt {
    $receipt | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $receiptPath -Encoding utf8
}

foreach ($path in @($targetJar, $targetScript, $disabledJar, $bossJar, $stagedJar, $stagedScript, $rollbackScript, $failedJar) + $lockPaths) {
    Assert-InstancePath $path
}
if (Test-Path -LiteralPath $receiptPath) { throw 'This deployment already has a receipt; do not overwrite or redeploy blindly.' }
Add-Type -AssemblyName System.IO.Compression.FileSystem

try {
    foreach ($path in $lockPaths) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    Assert-NoJavaProcess
    Assert-Hash $sourceJar $expectedJar
    Assert-Hash $sourceScript $expectedScript
    Assert-Hash $targetScript $expectedPreviousScript
    Assert-Hash $disabledJar $expectedDisabled
    if (Test-Path -LiteralPath $targetJar) { throw 'The target Event 1.0.7 JAR already exists.' }
    $activeBefore = @(Get-ActiveEventJars)
    if ($activeBefore.Count) { throw ('An Event JAR is already enabled: ' + ($activeBefore -join ', ')) }
    $bossBefore = Get-Sha256 $bossJar
    $integrationBefore = [ordered]@{}
    foreach ($relative in @(
        'kubejs\startup_scripts\content\wishing_well.js',
        'kubejs\assets\farmingtales\blockstates\wishing_well_core.json',
        'kubejs\assets\farmingtales\models\block\wishing_well_core.json',
        'kubejs\server_scripts\item_events\economy\coins.js'
    )) {
        $path = Join-Path $instance $relative
        $hash = Get-Sha256 $path
        Assert-Hash (Join-Path (Join-Path $candidateRoot 'integration') $relative) $hash
        $integrationBefore[$relative] = $hash
    }
    New-Item -ItemType Directory -Path $backupRoot | Out-Null
    $backupScript = Join-Path $backupRoot 'wishing_well.js.before'
    $backupDisabled = Join-Path $backupRoot 'qys-event-forge-1.20.1-1.0.6.jar.disabled.before'
    [IO.File]::Copy($targetScript, $backupScript, $false)
    [IO.File]::Copy($disabledJar, $backupDisabled, $false)
    Assert-Hash $backupScript $expectedPreviousScript
    Assert-Hash $backupDisabled $expectedDisabled
    $receipt['before'] = [ordered]@{
        activeEventJars = $activeBefore
        scriptSha256 = $expectedPreviousScript
        disabledJarSha256 = $expectedDisabled
        bossJarSha256 = $bossBefore
        preservedIntegrationHashes = $integrationBefore
    }
    $receipt['backups'] = [ordered]@{ script = $backupScript; disabledJar = $backupDisabled }
    $receipt['status'] = 'BACKED_UP_STAGING'
    Save-Receipt
    [IO.File]::Copy($sourceJar, $stagedJar, $false)
    [IO.File]::Copy($sourceScript, $stagedScript, $false)
    Assert-Hash $stagedJar $expectedJar
    Assert-Hash $stagedScript $expectedScript
    Assert-Hash $targetScript $expectedPreviousScript
    Assert-Hash $disabledJar $expectedDisabled
    Assert-NoJavaProcess
    [IO.File]::Replace($stagedScript, $targetScript, [NullString]::Value)
    $scriptCommitted = $true
    Assert-Hash $targetScript $expectedScript
    Assert-NoJavaProcess
    [IO.File]::Move($stagedJar, $targetJar)
    $jarCommitted = $true
    Assert-Hash $targetJar $expectedJar
    Assert-Hash $targetScript $expectedScript
    Assert-Hash $disabledJar $expectedDisabled
    Assert-Hash $bossJar $bossBefore
    foreach ($relative in $integrationBefore.Keys) {
        Assert-Hash (Join-Path $instance $relative) $integrationBefore[$relative]
    }
    $activeAfter = @(Get-ActiveEventJars)
    if ($activeAfter.Count -ne 1 -or $activeAfter[0] -ne $targetJar) {
        throw ('Expected exactly the new Event JAR, found: ' + ($activeAfter -join ', '))
    }
    $receipt['after'] = [ordered]@{
        activeEventJars = $activeAfter
        enabledVersion = '1.0.7'
        jar = $targetJar
        jarSha256 = Get-Sha256 $targetJar
        jarBytes = (Get-Item -LiteralPath $targetJar).Length
        script = $targetScript
        scriptSha256 = Get-Sha256 $targetScript
        oldDisabledJarRetained = Test-Path -LiteralPath $disabledJar
        oldDisabledJarSha256 = Get-Sha256 $disabledJar
        bossJarSha256 = Get-Sha256 $bossJar
        javaProcessesAtPostcheck = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Select-Object ProcessId, Name)
    }
    $receipt['status'] = 'DEPLOYED_VERIFIED_EVENT_ENABLED'
    $receipt['completedChina'] = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    Save-Receipt
    $receipt | ConvertTo-Json -Depth 8
} catch {
    $originalError = $_.Exception.Message
    $rollbackErrors = [Collections.Generic.List[string]]::new()
    if ($jarCommitted) {
        try {
            Assert-Hash $targetJar $expectedJar
            [IO.File]::Move($targetJar, $failedJar)
        } catch { $rollbackErrors.Add('JAR rollback: ' + $_.Exception.Message) }
    }
    if ($scriptCommitted) {
        try {
            Assert-Hash $targetScript $expectedScript
            Assert-Hash $backupScript $expectedPreviousScript
            [IO.File]::Copy($backupScript, $rollbackScript, $false)
            [IO.File]::Replace($rollbackScript, $targetScript, [NullString]::Value)
            Assert-Hash $targetScript $expectedPreviousScript
        } catch { $rollbackErrors.Add('Script rollback: ' + $_.Exception.Message) }
    }
    $receipt['status'] = if ($rollbackErrors.Count) { 'FAILED_MANUAL_RECONCILIATION_REQUIRED' } elseif ($scriptCommitted -or $jarCommitted) { 'FAILED_ROLLED_BACK' } else { 'ABORTED_PRECHECK_OR_STAGING' }
    $receipt['error'] = $originalError
    $receipt['rollbackErrors'] = @($rollbackErrors)
    Save-Receipt
    throw ('Event deployment failed: ' + $originalError + '; rollback errors: ' + ($rollbackErrors -join '; '))
} finally {
    for ($index = $locks.Count - 1; $index -ge 0; $index--) { $locks[$index].Dispose() }
}
