param([Parameter(Mandatory=$true)][string]$OutputDirectory)
$ErrorActionPreference='Stop'
$python='C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python "$PSScriptRoot\build.py" $OutputDirectory
if($LASTEXITCODE -ne 0){throw 'Assembly build failed'}
& $python "$PSScriptRoot\verify.py" $OutputDirectory
if($LASTEXITCODE -ne 0){throw 'Assembly verification failed'}
Write-Output "Assembly reproduced and verified in $OutputDirectory"
