param([string]$Nbt='D:\ft工程文件\FT地牢_第5组逐图重做_20261104\01-coral-chamber\nbt\s5-01-coral-chamber-redo.nbt')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
  $dependencies=Get-Content 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object {$_ -and $_ -notmatch 'minecraft_user_repo'}
  $classpath=(@('D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar')+$dependencies)-join ';'
  New-Item -ItemType Directory -Path '.\classes' -Force | Out-Null
  & 'C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe' -encoding UTF-8 -cp $classpath -d .\classes .\ConnectionCheck.java 2>&1 | Tee-Object .\compile.log
  if($LASTEXITCODE -ne 0){throw 'Native compile failed'}
  & 'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe' '-Djava.awt.headless=true' -cp ".\classes;$classpath" ConnectionCheck $Nbt "$PSScriptRoot\connections.json" 2>&1 | Tee-Object .\native.log
  if($LASTEXITCODE -ne 0){throw 'Native route check failed'}
} finally {Pop-Location}
