param([Parameter(Mandatory=$true)][string]$Room)
$ErrorActionPreference = 'Stop'
$qa = Join-Path $Room 'qa'
$old = 'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685'
$classpath = (Get-Content -Raw -LiteralPath 'D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\S8-rebuild-single-20261104-09\qa\classpath.txt').Trim()
$classpath = "$old\classes;$classpath"
Copy-Item -LiteralPath "$old\vanilla-block-tags.json" -Destination "$qa\vanilla-block-tags.json"
$java = (Get-Command java.exe).Source
Push-Location $qa
try {
  & $java -Xmx1536m '-Dfile.encoding=UTF-8' -cp $classpath IndependentNativeAudit $qa frozen *> native.log
  if ($LASTEXITCODE -ne 0) { throw 'Native audit process failed; see native.log' }
  & $java -Xmx1536m '-Dfile.encoding=UTF-8' -cp $classpath IndependentRouteAudit $qa *> routes.log
  if ($LASTEXITCODE -ne 0) { throw 'Route audit process failed; see routes.log' }
} finally { Pop-Location }
$native = Get-Content -Raw "$qa\independent-native-frozen.json" | ConvertFrom-Json
$routes = Get-Content -Raw "$qa\independent-routes.json" | ConvertFrom-Json
if ($native[0].registry_properties -ne 'PASS' -or $native[0].template_roundtrip_block_count -ne 'PASS' -or $native[0].survival_failures.Count -gt 0 -or $native[0].survival_unsupported.Count -gt 0) { throw 'Native self-check findings' }
if (-not $routes[0].start_clear -or ($routes[0].targets | Where-Object { -not $_.reachable -or -not $_.standing_clear })) { throw 'Route self-check findings' }
Write-Output 'Vanilla native and route self-checks passed; independent review still required.'
