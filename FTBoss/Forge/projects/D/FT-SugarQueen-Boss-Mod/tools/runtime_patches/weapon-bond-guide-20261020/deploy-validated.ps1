param(
    [Parameter(Mandatory = $true)][string]$ReportPath,
    [Parameter(Mandatory = $true)][string]$TargetPath
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
$reportFile = (Resolve-Path -LiteralPath $ReportPath).Path
$report = Get-Content -LiteralPath $reportFile -Raw -Encoding UTF8 | ConvertFrom-Json
if ($report.status -ne 'PASS_NOT_DEPLOYED') { throw 'Candidate has not passed validation.' }
$artifact = (Resolve-Path -LiteralPath $report.candidate).Path
if ((Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash -ne $report.candidateSHA256) { throw 'Candidate hash changed.' }
foreach ($source in $report.sourceHashes.PSObject.Properties) {
    $filename = Join-Path $project $source.Name
    if ((Get-FileHash -LiteralPath $filename -Algorithm SHA256).Hash -ne $source.Value) { throw "Source changed: $($source.Name)" }
}
if ((Get-FileHash -LiteralPath $TargetPath -Algorithm SHA256).Hash -ne $report.candidateSHA256) {
    $global:LASTEXITCODE = 0
    & (Join-Path $project 'tools/deploy_jar.ps1') -NoBuild -ArtifactPath $artifact -TargetPath $TargetPath -ExpectedTargetHash $report.baselineSHA256
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
if ((Get-FileHash -LiteralPath $TargetPath -Algorithm SHA256).Hash -ne $report.candidateSHA256) { throw 'Deployment hash mismatch.' }
@{
    status = 'DEPLOYED'
    target = $TargetPath
    sha256 = $report.candidateSHA256
    inGameTested = $false
} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path (Split-Path -Parent $reportFile) 'deployment.json') -Encoding UTF8
