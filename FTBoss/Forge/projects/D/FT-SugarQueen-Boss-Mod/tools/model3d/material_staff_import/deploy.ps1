$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$work = Join-Path $project '_work\material-staff-model-import-20261026'
$report = Get-Content -LiteralPath (Join-Path $work 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$instance = Split-Path -Parent (Split-Path -Parent $report.runtime)
$sourceLock = $null
$instanceLock = $null
try {
    $sourceLock = [IO.File]::Open((Join-Path $project '.ftboss-source-deploy.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    $instanceLock = [IO.File]::Open((Join-Path $instance '.ftboss-152-deploy.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    if ((Get-FileHash -LiteralPath $report.runtime).Hash -ne $report.baselineSha256) {
        throw 'Runtime changed after packaging; rebuild against the latest runtime.'
    }
    if ((Get-FileHash -LiteralPath $report.artifact).Hash -ne $report.artifactSha256) {
        throw 'Verified artifact changed.'
    }
    if ((Get-FileHash -LiteralPath $report.baseline).Hash -ne $report.baselineSha256) {
        throw 'Baseline backup changed.'
    }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.artifact -TargetPath $report.runtime -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Deployment stopped with code $LASTEXITCODE" }
    $installedHash = (Get-FileHash -LiteralPath $report.runtime).Hash.ToLowerInvariant()
    if ($installedHash -ne $report.artifactSha256) { throw 'Installed hash differs from verified artifact.' }
    $receipt = [ordered]@{
        target = $report.runtime
        installedSha256 = $installedHash
        backup = $report.baseline
        backupSha256 = $report.baselineSha256
        resourceFilesAdded = $report.added.Count
        existingFilesChanged = $report.changed.Count
        registeredMaterialStaves = @($report.registeredMaterialStaves)
        scope = 'model and texture resources only'
        deployedAt = [TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTimeOffset]::UtcNow, 'China Standard Time').ToString('o')
    }
    $receipt | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $work 'deployment-receipt.json') -Encoding UTF8
    $lockText = "pid=$PID`noperation=material-staff-model-resources-deployed`ninstalled=$installedHash`nbackup=$($report.baseline)`n"
    $lockBytes = [Text.Encoding]::UTF8.GetBytes($lockText)
    foreach ($stream in @($sourceLock, $instanceLock)) {
        $stream.Position = 0
        $stream.Write($lockBytes, 0, $lockBytes.Length)
        $stream.SetLength($lockBytes.Length)
        $stream.Flush($true)
    }
    $receipt | ConvertTo-Json -Depth 8
} finally {
    if ($instanceLock) { $instanceLock.Dispose() }
    if ($sourceLock) { $sourceLock.Dispose() }
}
