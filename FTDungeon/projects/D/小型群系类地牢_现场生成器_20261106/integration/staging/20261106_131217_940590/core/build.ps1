param(
    [string]$Java = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe',
    [string]$Javac = 'javac',
    [string]$Jar = 'jar'
)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$build = [IO.Path]::GetFullPath((Join-Path $root 'build'))
if ($build -ne "$root\build") { throw 'Build output escaped component directory' }
Push-Location -LiteralPath $root
try {
    if (Test-Path -LiteralPath $build) { Remove-Item -LiteralPath $build -Recurse -Force }
    New-Item -ItemType Directory -Force -Path build\main,build\test,logs | Out-Null
    @("cwd=$root", "javac=$Javac --release 17 -encoding UTF-8 -Xlint:all -Werror", "java=$Java -ea", "jar=$Jar --date=2026-11-06T00:00:00Z") | Set-Content -LiteralPath logs\commands.log -Encoding utf8
    & $Javac -version 2>&1 | Tee-Object -FilePath logs\compile.log
    $main = @(Get-ChildItem -LiteralPath src\main\java -Filter '*.java' -Recurse | Sort-Object FullName | ForEach-Object FullName)
    $tests = @(Get-ChildItem -LiteralPath src\test\java -Filter '*.java' -Recurse | Sort-Object FullName | ForEach-Object FullName)
    & $Javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build\main $main 2>&1 | Tee-Object -FilePath logs\compile.log -Append
    if ($LASTEXITCODE -ne 0) { throw "Main compilation failed: $LASTEXITCODE" }
    & $Javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build\main -d build\test $tests 2>&1 | Tee-Object -FilePath logs\compile.log -Append
    if ($LASTEXITCODE -ne 0) { throw "Test compilation failed: $LASTEXITCODE" }
    & $Java -version 2>&1 | Tee-Object -FilePath logs\tests-java17.log
    if ($LASTEXITCODE -ne 0) { throw 'Java unavailable' }
    & $Java -ea -cp 'build\main;build\test' cn.ft.biomedungeon.core.CoreTests 2>&1 | Tee-Object -FilePath logs\tests-java17.log -Append
    if ($LASTEXITCODE -ne 0) { throw "Tests failed: $LASTEXITCODE" }
    & $Jar --create --file build\ft-biomedungeon-core-v1.jar --date=2026-11-06T00:00:00Z -C build\main . 2>&1 | Tee-Object -FilePath logs\jar.log
    if ($LASTEXITCODE -ne 0) { throw "JAR failed: $LASTEXITCODE" }
    & $Jar --list --file build\ft-biomedungeon-core-v1.jar 2>&1 | Tee-Object -FilePath logs\jar-entries.log
    if ($LASTEXITCODE -ne 0) { throw 'JAR inspection failed' }
    Get-FileHash -LiteralPath build\ft-biomedungeon-core-v1.jar -Algorithm SHA256 | Format-List | Out-String | Tee-Object -FilePath logs\artifact-sha256.log
} finally { Pop-Location }
