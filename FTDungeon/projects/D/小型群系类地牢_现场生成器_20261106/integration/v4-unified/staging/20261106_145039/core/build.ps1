$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$java = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
Push-Location -LiteralPath $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build\main,build\test,build\baseline-test,logs,results | Out-Null
    "cwd=$PSScriptRoot`ncompiler=javac --release 17 -encoding UTF-8 -Xlint:all -Werror`njava=$java`npython=$python -B`nclasspath=frozen ../build/ft-biomedungeon-core-v1.jar and local build/main" | Set-Content -LiteralPath logs\commands.log -Encoding utf8
    & $python -B prepare.py 2>&1 | Tee-Object -FilePath logs\prepare.log
    if ($LASTEXITCODE -ne 0) { throw 'Input locks failed' }
    $main = @(Get-ChildItem -LiteralPath src\main\java -Recurse -Filter '*.java' | ForEach-Object FullName)
    $tests = @(Get-ChildItem -LiteralPath src\test\java -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build\main $main 2>&1 | Tee-Object -FilePath logs\compile.log
    if ($LASTEXITCODE -ne 0) { throw 'Production compile failed' }
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp '..\build\ft-biomedungeon-core-v1.jar;build\main' -d build\test $tests 2>&1 | Tee-Object -FilePath logs\compile.log -Append
    if ($LASTEXITCODE -ne 0) { throw 'Test compile failed' }
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build\baseline-test ..\src\test\java\cn\ft\biomedungeon\core\CoreTests.java 2>&1 | Tee-Object -FilePath logs\compile.log -Append
    if ($LASTEXITCODE -ne 0) { throw 'Baseline test compile failed' }
    & $java -ea -cp '..\build\ft-biomedungeon-core-v1.jar;build\baseline-test' cn.ft.biomedungeon.core.CoreTests 2>&1 | Tee-Object -FilePath logs\baseline-regression-java17.log
    if ($LASTEXITCODE -ne 0) { throw 'Baseline regression failed' }
    & $java -ea -cp '..\build\ft-biomedungeon-core-v1.jar;build\main;build\test' PlannerYTests 2>&1 | Tee-Object -FilePath logs\planner-y-java17.log
    if ($LASTEXITCODE -ne 0) { throw 'New planner tests failed' }
    & $python -B verify_outputs.py 2>&1 | Tee-Object -FilePath logs\independent-verification.log
    if ($LASTEXITCODE -ne 0) { throw 'Independent output verification failed' }
    & jar --create --file build\ft-biomedungeon-planner-y-v2-experimental.jar --date=2026-11-06T00:00:00Z -C build\main .
    if ($LASTEXITCODE -ne 0) { throw 'Jar packaging failed' }
} finally { Pop-Location }
