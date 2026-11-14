param()
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$task = Join-Path $project '_work\common-material-bonds-20261025'
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$stage = Join-Path $instance 'mods\.qys-bosses-common-material-bonds.tmp'
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
$report = Get-Content -Raw -LiteralPath (Join-Path $task 'build-report.json') | ConvertFrom-Json
$locks = [Collections.Generic.List[IO.FileStream]]::new()
function Hash([string] $File) {
    return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant()
}
try {
    if (Get-Process java,javaw -ErrorAction SilentlyContinue) { throw 'Java is running. Exit Minecraft before deployment.' }
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'))) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    if (-not $report.passed) { throw 'Candidate validation is incomplete.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'The deployed JAR changed. Rebuild on the latest baseline.' }
    if ((Hash $candidate) -ne $report.candidateSha256) { throw 'Candidate hash does not match the verified report.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after validation: $($property.Name)" }
    }
    if ((Test-Path -LiteralPath $stage) -and (Hash $stage) -ne $report.candidateSha256) { throw "Unexpected staging file: $stage" }
    if (Test-Path -LiteralPath $backup) {
        if ((Hash $backup) -ne $report.baselineSha256) { throw "Unexpected deployment backup: $backup" }
    } else {
        Copy-Item -LiteralPath $target -Destination $backup
    }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup verification failed.' }
    if (-not (Test-Path -LiteralPath $stage)) { Copy-Item -LiteralPath $candidate -Destination $stage }
    if ((Hash $stage) -ne $report.candidateSha256) { throw 'Staged JAR verification failed.' }
    if (Get-Process java,javaw -ErrorAction SilentlyContinue) { throw 'Java started before replacement. Deployment cancelled.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Concurrent deployment detected before replacement.' }
    [IO.File]::Replace($stage, $target, [NullString]::Value)
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed JAR hash verification failed.' }
    $receipt = [ordered]@{
        installed = $true
        target = $target
        backup = $backup
        baselineSha256 = $report.baselineSha256
        installedSha256 = $report.candidateSha256
        originalMaterialBonds = 12
        addedBasicMaterialBonds = 12
        totalMaterialBonds = 24
        gameplayTested = $false
        docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    }
    $receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8NoBOM
    $message = "pid=$PID`noperation=common-material-bonds-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) {
        $stream.Position = 0
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush()
    }
    Write-Output "DEPLOYED: $target"
    Write-Output "SHA256: $($report.candidateSha256)"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
}
