param([Parameter(Mandatory=$true)][string]$PlanPath)
$ErrorActionPreference='Stop'
$plan=Get-Content -LiteralPath $PlanPath -Raw -Encoding utf8 | ConvertFrom-Json
$root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$repository=[IO.Path]::GetFullPath((Join-Path $root $plan.targetRelativePath))
if(-not $repository.StartsWith($root.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Target outside temporary workspace'}
$prefix=$repository.TrimEnd('\')+'\'
$busy=@(Get-CimInstance Win32_Process | Where-Object {$_.Name -match '^(javaw?|pythonw?|git.*|gradle.*)\.exe$' -and $_.CommandLine -and ($_.CommandLine.Replace('/','\').IndexOf($prefix,[StringComparison]::OrdinalIgnoreCase) -ge 0 -or $_.CommandLine.Replace('/','\').IndexOf('"'+$repository+'"',[StringComparison]::OrdinalIgnoreCase) -ge 0)} | ForEach-Object {[pscustomobject]@{pid=$_.ProcessId;name=$_.Name}})
if($busy.Count){$busy|ConvertTo-Json -Compress;throw 'Processes reference cleanup target'}
Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;
public static class SourceCacheLocks {
 [StructLayout(LayoutKind.Sequential)] public struct UniqueProcess { public uint ProcessId; public System.Runtime.InteropServices.ComTypes.FILETIME StartTime; }
 [StructLayout(LayoutKind.Sequential,CharSet=CharSet.Unicode)] public struct ProcessInfo { public UniqueProcess Process; [MarshalAs(UnmanagedType.ByValTStr,SizeConst=256)] public string AppName; [MarshalAs(UnmanagedType.ByValTStr,SizeConst=64)] public string Service; public uint Type; public uint Status; public uint Session; [MarshalAs(UnmanagedType.Bool)] public bool Restartable; }
 [DllImport("rstrtmgr.dll",CharSet=CharSet.Unicode)] public static extern int RmStartSession(out uint session,uint flags,[Out] System.Text.StringBuilder key);
 [DllImport("rstrtmgr.dll",CharSet=CharSet.Unicode)] public static extern int RmRegisterResources(uint session,uint fileCount,string[] names,uint appCount,IntPtr applications,uint serviceCount,IntPtr services);
 [DllImport("rstrtmgr.dll")] public static extern int RmGetList(uint session,out uint needed,ref uint count,[In,Out] ProcessInfo[] applications,ref uint reasons);
 [DllImport("rstrtmgr.dll")] public static extern int RmEndSession(uint session);
}
"@
$files=@($plan.deleteFiles.PSObject.Properties | ForEach-Object {[IO.Path]::GetFullPath((Join-Path $repository $_.Name))})
foreach($file in $files){if(-not $file.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){throw 'File outside target'}}
[uint32]$session=0
$key=New-Object System.Text.StringBuilder 33
$result=[SourceCacheLocks]::RmStartSession([ref]$session,0,$key)
if($result -ne 0){throw "RmStartSession failed: $result"}
try{
 $result=[SourceCacheLocks]::RmRegisterResources($session,[uint32]$files.Count,[string[]]$files,0,[IntPtr]::Zero,0,[IntPtr]::Zero)
 if($result -ne 0){throw "RmRegisterResources failed: $result"}
 [uint32]$needed=0;[uint32]$count=0;[uint32]$reasons=0
 $result=[SourceCacheLocks]::RmGetList($session,[ref]$needed,[ref]$count,$null,[ref]$reasons)
 if($result -eq 234){$count=$needed;$owners=New-Object 'SourceCacheLocks+ProcessInfo[]' $count;$result=[SourceCacheLocks]::RmGetList($session,[ref]$needed,[ref]$count,$owners,[ref]$reasons);if($result -ne 0){throw "RmGetList failed: $result"};if($count -gt 0){throw "Open file handles found: $count"}}
 elseif($result -ne 0){throw "RmGetList failed: $result"}
 if($needed -ne 0 -or $count -ne 0){throw 'Unexpected lock owners'}
 [pscustomobject]@{target=$repository;registeredFiles=$files.Count;referencingProcesses=0;restartManagerFileHandleOwners=0;shutdownRequested=$false}|ConvertTo-Json -Compress
}finally{[void][SourceCacheLocks]::RmEndSession($session)}
