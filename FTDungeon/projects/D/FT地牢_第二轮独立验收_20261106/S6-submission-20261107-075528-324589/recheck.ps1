param([Parameter(Mandatory=$true)][string]$Room)
$ErrorActionPreference='Stop'
$groupRoot=$PSScriptRoot
$roomPath=(Resolve-Path -LiteralPath $Room).Path
if(-not $roomPath.StartsWith($groupRoot+[IO.Path]::DirectorySeparatorChar)){throw 'Only new S6 descendants may be checked'}
& node 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs' "$roomPath\recipe.mjs" --out-dir $roomPath --data-version 3465
if($LASTEXITCODE -ne 0){throw 'Build failed'}
& node "$groupRoot\audit.mjs" $roomPath
if($LASTEXITCODE -ne 0){throw 'Static failed'}
& "$groupRoot\native.ps1" -Room $roomPath
$classpath=(Get-Content -Raw -LiteralPath 'D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\S8-rebuild-single-20261104-09\qa\classpath.txt').Trim()
Push-Location "$roomPath\qa"
try {
  & java -Xmx1536m '-Dfile.encoding=UTF-8' -cp "D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes;$classpath" IndependentAllSupportAudit (Get-Location).Path *> all-support.log
  if($LASTEXITCODE -ne 0){throw 'Full support failed'}
} finally {Pop-Location}
$support=Get-Content -Raw -LiteralPath "$roomPath\qa\independent-all-support.json"|ConvertFrom-Json
if($support[0].failures.Count -or $support[0].unsupported.Count){throw 'Full support findings'}
& node "$groupRoot\render.cjs" $roomPath
if($LASTEXITCODE -ne 0){throw 'Render failed'}
