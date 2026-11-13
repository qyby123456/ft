$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$qa=Join-Path $root 'qa\all-support-final'
New-Item -ItemType Directory -Path $qa -Force | Out-Null
$targets=@()
foreach($number in 11..20){
  $candidate=Join-Path $root "S5-$number\s5-$number.nbt"
  $targets+=@{group=(500+$number);candidate=$candidate;sha256=(Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLower()}
}
$candidate=Join-Path $root 'merged\merged.nbt'
$targets+=@{group=500;candidate=$candidate;sha256=(Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLower()}
ConvertTo-Json -InputObject $targets -Depth 6 | Set-Content -LiteralPath "$qa\route-input.json" -Encoding utf8NoBOM
Copy-Item -LiteralPath "$root\S5-11\qa\vanilla-block-tags.json" -Destination $qa
$classpath=Get-Content -Raw -LiteralPath 'D:\ft工程文件\FT地牢_第7组逐图重做_20261104\10-overview\qa\isolated-run-05\classpath.txt'
$classpath='D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes;'+(($classpath.Trim() -split '\r?\n') -join ';')
Push-Location $qa
try {
  & 'C:\Program Files\Java\jdk-25.0.2\bin\java.exe' '-Xmx1536m' '-cp' $classpath 'IndependentAllSupportAudit' $qa 2>&1 | Tee-Object -FilePath "$qa\all-support.log"
  if($LASTEXITCODE){throw 'All-support audit failed'}
} finally {Pop-Location}
