$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$workspace = [IO.Path]::GetFullPath((Join-Path $root '..\..'))
if ($env:FT_EVENT_FORGE_WORKSPACE) { $workspace = [IO.Path]::GetFullPath($env:FT_EVENT_FORGE_WORKSPACE) }
$minecraftRoot = [IO.Path]::GetFullPath((Join-Path $workspace '..\..'))
$manifest = Get-Content -LiteralPath (Join-Path $workspace 'FarmingTales_ForgeⅡ v1.5.2.json') -Raw | ConvertFrom-Json
$selected = foreach ($library in $manifest.libraries) {
    if ($library.downloads.artifact.path) {
        $path = Join-Path (Join-Path $minecraftRoot 'libraries') $library.downloads.artifact.path
        if (Test-Path -LiteralPath $path) { $path.Replace('\', '/') }
    }
}
$forgeRuntime = Join-Path $env:USERPROFILE '.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-srg.jar'
$libraries = Get-ChildItem -LiteralPath (Join-Path $minecraftRoot 'libraries') -Recurse -Filter '*.jar' | Select-Object -ExpandProperty FullName
$asm = Get-ChildItem -LiteralPath (Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\org.ow2.asm') -Recurse -Filter '*9.8.jar' | Select-Object -ExpandProperty FullName
$requiredMods = @('qys-event-forge-1.20.1-1.0.6.jar', 'kubejs-forge-2001.6.5-build.16.jar', '[犀牛] rhino-forge-2001.2.3-build.10.jar') | ForEach-Object { Join-Path $workspace ('mods\' + $_) }
$classpath = ((@($forgeRuntime) + $selected + $asm + $requiredMods + $libraries) -join ';').Replace('\', '/')
$payload = Join-Path $root 'payload'
$classes = Join-Path $root 'classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$sources = Get-ChildItem -LiteralPath (Join-Path $root 'src') -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
$compile = @('-proc:none', '--release', '17', '-encoding', 'UTF-8', '-cp', ('"' + $classpath + '"'), '-d', ('"' + $payload.Replace('\', '/') + '"'))
$compile += $sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
$arg = Join-Path $root 'runtime-compile.args'
[IO.File]::WriteAllLines($arg, $compile, [Text.UTF8Encoding]::new($false))
javac ('@' + $arg)
if ($LASTEXITCODE -ne 0) { throw 'Runtime delta compilation failed' }
$classpath = $payload.Replace('\', '/') + ';' + $classes.Replace('\', '/') + ';' + $classpath
$sources = @('VerifyCoreFacing.java', 'VerifyForgeFacing.java', 'QysEventVerifier.java') | ForEach-Object { Join-Path $root $_ }
$compile = @('-proc:none', '--release', '17', '-encoding', 'UTF-8', '-cp', ('"' + $classpath + '"'), '-d', ('"' + $classes.Replace('\', '/') + '"'))
$compile += $sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
$arg = Join-Path $root 'verification-compile.args'
[IO.File]::WriteAllLines($arg, $compile, [Text.UTF8Encoding]::new($false))
javac ('@' + $arg)
if ($LASTEXITCODE -ne 0) { throw 'Verification compilation failed' }
$env:PYTHONIOENCODING = 'utf-8'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python (Join-Path $root 'release.py') stage
if ($LASTEXITCODE -ne 0) { throw 'Fresh baseline or entry whitelist check failed' }
$runtime = Join-Path $root 'candidate.jar'
$minecraft = Join-Path $env:USERPROFILE '.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-srg.jar'
$kubejs = Join-Path $workspace 'mods\kubejs-forge-2001.6.5-build.16.jar'
$baseline = Get-ChildItem -LiteralPath (Join-Path $workspace '_work\event-four-way-20261008\backups') -Filter 'qys-event-forge*.bak' | Sort-Object Name | Select-Object -First 1
$classpath = $classes.Replace('\', '/') + ';' + $runtime.Replace('\', '/') + ';' + $classpath
foreach ($verifier in @('VerifyCoreFacing', 'VerifyForgeFacing', 'cn.farmingtales.qysevent.QysEventVerifier')) {
    $run = @('-Xverify:all', '-cp', ('"' + $classpath + '"'), $verifier)
    if ($verifier -eq 'VerifyCoreFacing') {
        $run += @($runtime, $minecraft, $kubejs, (Join-Path $root 'core-runtime-verification.json')) | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
    } elseif ($verifier -eq 'VerifyForgeFacing') {
        $run += @($runtime, $baseline.FullName, $kubejs, (Join-Path $root 'forge-runtime-verification.json')) | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
    }
    $arg = Join-Path $root ($verifier.Split('.')[-1] + '.args')
    [IO.File]::WriteAllLines($arg, $run, [Text.Encoding]::GetEncoding(936))
    $result = java ('@' + $arg)
    $result
    if ($LASTEXITCODE -ne 0) { throw ($verifier + ' failed') }
    [IO.File]::WriteAllLines((Join-Path $root ($verifier.Split('.')[-1] + '.txt')), $result, [Text.UTF8Encoding]::new($false))
}
