param([switch]$Rebalance, [switch]$ImpossibleHealth)
$ErrorActionPreference = 'Stop'
if ($Rebalance -and $ImpossibleHealth) { throw 'Choose only one deployment mode.' }
$project = Split-Path -Parent $PSScriptRoot
$task = Join-Path $project $(if ($Rebalance) { '_work\manor-health-3000-20261025' } else { '_work\manor-damage-limits-20261025' })
if ($ImpossibleHealth) { $task = Join-Path $project '_work\manor-impossible-health-20261025' }
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$baseline = Join-Path $task 'baseline.jar'
$receipt = Get-Content -LiteralPath (Join-Path $task 'build-receipt.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if ($receipt.checks -ne 'passed') { throw 'Build checks have not passed.' }
if ((Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash -ne $receipt.candidateSha256) { throw 'Candidate checksum changed.' }
if ((Get-FileHash -LiteralPath $baseline -Algorithm SHA256).Hash -ne $receipt.baselineSha256) { throw 'Backup checksum changed.' }
foreach ($source in $receipt.sourceHashes.PSObject.Properties) {
    if ((Get-FileHash -LiteralPath $source.Name -Algorithm SHA256).Hash -ne $source.Value) { throw "Source changed after verification: $($source.Name)" }
}
$locks = [System.Collections.Generic.List[System.IO.FileStream]]::new()
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'))) {
        $locks.Add([System.IO.File]::Open($path, [System.IO.FileMode]::OpenOrCreate,
                [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None))
    }
    $game = Get-CimInstance Win32_Process -Filter "name='java.exe' OR name='javaw.exe'" -ErrorAction Stop |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($game) { throw "Minecraft is running (PID $($game.ProcessId -join ',')); exit before deployment." }
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $receipt.baselineSha256) {
        throw 'The installed jar changed since this build; rebuild on the current baseline before deployment.'
    }
    Copy-Item -LiteralPath $candidate -Destination $target -Force
    $installed = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
    if ($installed -ne $receipt.candidateSha256) { throw 'Installed checksum mismatch.' }
    $state = "pid=$PID`noperation=manor-damage-limits-deployed`ninstalled=$installed`nbackup=$baseline`n"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($state)
    foreach ($stream in $locks) {
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush()
    }
    $zone = [System.TimeZoneInfo]::FindSystemTimeZoneById('China Standard Time')
    $deployedAt = [System.TimeZoneInfo]::ConvertTime([System.DateTimeOffset]::UtcNow, $zone).ToString('o')
    [ordered]@{
        status = 'DEPLOYED'
        target = $target
        installedSha256 = $installed
        baselineSha256 = $receipt.baselineSha256
        backup = $baseline
        assertions = $receipt.assertions
        physicalSingleHit = 50
        magicSingleHit = 50
        physicalRolling20Ticks = $receipt.physicalRolling20Ticks
        magicRolling20Ticks = $receipt.magicRolling20Ticks
        totalRolling20Ticks = $receipt.totalRolling20Ticks
        manorBaseHealth = $receipt.manorBaseHealth
        supportBaseHealth = $receipt.supportBaseHealth
        impossibleHealthMultiplier = $receipt.impossibleHealthMultiplier
        gameTested = $false
        docsProposal = $receipt.docsProposal
        deployedAt = $deployedAt
    } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding UTF8
    Write-Host "DEPLOYED $target"
    Write-Host "SHA256 $installed"
    Write-Host "Backup $baseline"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
}
