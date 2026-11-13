$ErrorActionPreference='Stop'
$classpath=(Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'S8-11\qa\classpath.txt')).Trim()
$classes=Join-Path $PSScriptRoot 'classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& javac -encoding UTF-8 -cp $classpath -d $classes (Join-Path $PSScriptRoot 'S8GeometryAudit.java')
if($LASTEXITCODE -ne 0){throw 'Compile geometry failed'}
foreach($number in 11..20){
 $qa=Join-Path $PSScriptRoot "S8-$number\qa"
 Push-Location $qa
 try{
  & java '-Xmx1536m' -cp ($classes+';'+$classpath) S8GeometryAudit $qa *> 'geometry.log'
  if($LASTEXITCODE -ne 0){throw "Geometry audit failed S8-$number"}
  & java '-Xmx1536m' -cp ('D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes;'+$classpath) IndependentAllSupportAudit $qa *> 'all-support.log'
  if($LASTEXITCODE -ne 0){throw "All support failed S8-$number"}
 }finally{Pop-Location}
}
