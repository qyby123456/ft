$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath('D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S8')
$externalRoot=[IO.Path]::GetFullPath('C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s8-maker-access-isolated')
$transferRoot=Join-Path $externalRoot ('transfer-'+[guid]::NewGuid().ToString('N').Substring(0,12))
if(Test-Path -LiteralPath $transferRoot){throw 'Never overwrite an external transfer'}
$active=Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {$_.CommandLine -like '*runtime-install-*'}
if($active){throw 'Access test is still running; preserve process'}
$moves=@()
foreach($attempt in 1..5){
    foreach($suffix in @('server','native-test\build','native-test\.gradle')){
        $relative="runtime-install-r$attempt\$suffix"
        $source=[IO.Path]::GetFullPath((Join-Path $taskRoot $relative))
        $destination=[IO.Path]::GetFullPath((Join-Path $transferRoot $relative))
        if(!$source.StartsWith($taskRoot+'\') -or $source -notmatch '\\runtime-install-r[1-5]\\(server|native-test\\(build|\.gradle))$'){throw 'Unexpected source'}
        if(!$destination.StartsWith($externalRoot+'\') -or !$destination.StartsWith($transferRoot+'\')){throw 'Unexpected destination'}
        if(Test-Path -LiteralPath $source){$moves+= [pscustomobject]@{source=$source;destination=$destination}}
    }
}
$files=@()
foreach($move in $moves){
    foreach($file in Get-ChildItem -LiteralPath $move.source -Recurse -File){
        $relative=[IO.Path]::GetRelativePath($move.source,$file.FullName)
        $files+=[pscustomobject]@{original_path=$file.FullName;path=(Join-Path $move.destination $relative);sha256=(Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant();bytes=$file.Length}
    }
}
$report=[ordered]@{status='PREPARED_NOT_MOVED';source_sha256='4d768bcaa073ba63da090ea9b6f64068c474d8a4c4d627217a81803e0c83fd37';external_root=$transferRoot;moves=$moves;files=$files;original_installation_evidence_sha256=(Get-FileHash -LiteralPath "$PSScriptRoot\INSTALLATION-EVIDENCE.json" -Algorithm SHA256).Hash.ToLowerInvariant();world_path=(Join-Path $transferRoot 'runtime-install-r5\server\world');file_count=$files.Count;total_bytes=($files | Measure-Object bytes -Sum).Sum}
$report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath "$PSScriptRoot\STORAGE-MANIFEST.json" -Encoding utf8
foreach($move in $moves){
    if(Test-Path -LiteralPath $move.destination){throw 'Destination exists; stop rather than merge'}
    New-Item -ItemType Directory -Path (Split-Path -Parent $move.destination) -Force | Out-Null
    Move-Item -LiteralPath $move.source -Destination $move.destination
}
foreach($file in $files){
    $actual=Get-Item -LiteralPath $file.path
    if($actual.Length -ne $file.bytes -or (Get-FileHash -LiteralPath $file.path -Algorithm SHA256).Hash.ToLowerInvariant() -ne $file.sha256){throw "Relocated file mismatch: $($file.path)"}
}
$report.status='MOVED_AND_ALL_FILE_HASHES_VERIFIED'
$report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath "$PSScriptRoot\STORAGE-MANIFEST.json" -Encoding utf8
[pscustomobject]@{external_root=$transferRoot;files=$files.Count;bytes=$report.total_bytes;world_path=$report.world_path} | ConvertTo-Json
