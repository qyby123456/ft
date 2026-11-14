param([string]$Flavor='baseline')
$ErrorActionPreference = 'Stop'
$fixtures = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..')) + [IO.Path]::DirectorySeparatorChar
$running = Get-CimInstance Win32_Process | Where-Object {
    if ($_.Name -notmatch '^javaw?\.exe$' -or $_.CommandLine -notmatch 'forgeclient|--gameDir') { return $false }
    $directory = [regex]::Match($_.CommandLine, '--gameDir"?\s+"([^"]+)"').Groups[1].Value
    return -not $directory -or -not [IO.Path]::GetFullPath($directory).StartsWith($fixtures, [StringComparison]::OrdinalIgnoreCase)
}
if ($running) { throw 'A non-fixture Minecraft client is running; refusing parallel test launch.' }
$spec = Get-Content -LiteralPath (Join-Path $PSScriptRoot ('client-'+$Flavor+'/evidence/LAUNCH.json')) -Raw -Encoding UTF8 | ConvertFrom-Json
$arguments = ($spec.argv | ForEach-Object { '"' + $_.Replace('"', '\"') + '"' }) -join ' '
$process = Start-Process -FilePath $spec.executable -ArgumentList $arguments -WorkingDirectory $spec.workingDirectory -WindowStyle Hidden -RedirectStandardOutput (Join-Path $spec.workingDirectory 'evidence/stdout.log') -RedirectStandardError (Join-Path $spec.workingDirectory 'evidence/stderr.log') -PassThru
[ordered]@{pid=$process.Id;privateInstance=$spec.workingDirectory;started=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $spec.workingDirectory 'evidence/PROCESS.json') -Encoding UTF8
Write-Output ('Private client PID: ' + $process.Id)
