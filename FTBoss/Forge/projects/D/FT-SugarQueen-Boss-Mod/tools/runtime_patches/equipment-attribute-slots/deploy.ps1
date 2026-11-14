param([Parameter(Mandatory = $true)][string]$ReceiptPath)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
$receiptFile = (Resolve-Path -LiteralPath $ReceiptPath).Path
if (-not $receiptFile.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Build receipt must be inside the project task workspace.'
}
$receipt = Get-Content -LiteralPath $receiptFile -Raw -Encoding UTF8 | ConvertFrom-Json
$task = Split-Path -Parent $receiptFile
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
if ($receipt.checks -ne 'passed' -or -not $receipt.bossAndDamageLimitBytesUnchanged) { throw 'Build checks have not passed.' }
if ((Get-FileHash -LiteralPath $receipt.candidate -Algorithm SHA256).Hash -ne $receipt.candidateSha256) { throw 'Candidate checksum changed.' }
if ((Get-FileHash -LiteralPath $receipt.backup -Algorithm SHA256).Hash -ne $receipt.baselineSha256) { throw 'Backup checksum changed.' }
foreach ($source in $receipt.sourceHashes.PSObject.Properties) {
    if ((Get-FileHash -LiteralPath $source.Name -Algorithm SHA256).Hash -ne $source.Value) {
        throw "Source changed after verification: $($source.Name)"
    }
}
$locks = [System.Collections.Generic.List[System.IO.FileStream]]::new()
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'))) {
        $locks.Add([System.IO.File]::Open($path, [System.IO.FileMode]::OpenOrCreate,
                [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None))
    }
    & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') -NoBuild `
            -ArtifactPath $receipt.candidate -TargetPath $target -ExpectedTargetHash $receipt.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Deployment did not complete: $LASTEXITCODE" }
    $installed = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
    if ($installed -ne $receipt.candidateSha256) { throw 'Installed checksum mismatch.' }
    $state = "pid=$PID`noperation=equipment-attribute-slots-deployed`ninstalled=$installed`nbackup=$($receipt.backup)`n"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($state)
    foreach ($stream in $locks) {
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush()
    }
    $zone = [System.TimeZoneInfo]::FindSystemTimeZoneById('China Standard Time')
    [ordered]@{
        status = 'DEPLOYED'
        target = $target
        installedSha256 = $installed
        baselineSha256 = $receipt.baselineSha256
        backup = $receipt.backup
        assertions = $receipt.assertions
        changedEntries = $receipt.changedEntries
        addedEntries = $receipt.addedEntries
        bossAndDamageLimitBytesUnchanged = $true
        gameTested = $false
        docsTouched = $false
        docsProposal = $receipt.docsProposal
        deployedAt = [System.TimeZoneInfo]::ConvertTime([System.DateTimeOffset]::UtcNow, $zone).ToString('o')
    } | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding UTF8
    Write-Host "DEPLOYED $target"
    Write-Host "SHA256 $installed"
    Write-Host "Backup $($receipt.backup)"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
}
