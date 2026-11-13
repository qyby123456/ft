$ErrorActionPreference='Stop'
$python='C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$java='C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
Push-Location -LiteralPath $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build\main,build\test,build\baseline-test,build\previous-test,build\previous-run\inputs,logs | Out-Null
    "cwd=$PSScriptRoot`njava=$java`njavac=--release17 -encoding UTF-8 -Xlint:all -Werror`npython=$python -B`nworld=NOT_RUN"|Set-Content -LiteralPath logs\commands.log -Encoding utf8
    & $python -B prepare.py 2>&1|Tee-Object -FilePath logs\prepare.log
    if($LASTEXITCODE -ne 0){throw 'Frozen input verification failed'}
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build\main src\DungeonEnvelopePlanner.java 2>&1|Tee-Object -FilePath logs\compile.log
    if($LASTEXITCODE -ne 0){throw 'New production compile failed'}
    $classpath='..\build\ft-biomedungeon-core-v1.jar;..\next-planner-y\build\ft-biomedungeon-planner-y-v2-experimental.jar;build\main'
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp $classpath -d build\test src\EnvelopeTests.java 2>&1|Tee-Object -FilePath logs\compile.log -Append
    if($LASTEXITCODE -ne 0){throw 'Envelope test compilation failed'}
    & $java -ea -cp "$classpath;build\test" EnvelopeTests 2>&1|Tee-Object -FilePath logs\java17-envelope.log
    if($LASTEXITCODE -ne 0){throw 'Envelope Java17 tests failed'}
    & $python -B verify.py 2>&1|Tee-Object -FilePath logs\independent-verification.log
    if($LASTEXITCODE -ne 0){throw 'Independent geometry verification failed'}
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build\baseline-test ..\src\test\java\cn\ft\biomedungeon\core\CoreTests.java 2>&1|Tee-Object -FilePath logs\compile.log -Append
    if($LASTEXITCODE -ne 0){throw 'Baseline regression compile failed'}
    & $java -ea -cp '..\build\ft-biomedungeon-core-v1.jar;build\baseline-test' cn.ft.biomedungeon.core.CoreTests 2>&1|Tee-Object -FilePath logs\baseline-regression.log
    if($LASTEXITCODE -ne 0){throw 'Baseline regression failed'}
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp $classpath -d build\previous-test ..\next-planner-y\src\test\java\PlannerYTests.java 2>&1|Tee-Object -FilePath logs\compile.log -Append
    if($LASTEXITCODE -ne 0){throw 'V2 regression compilation failed'}
    Copy-Item -LiteralPath inputs\real-nine-ports.tsv -Destination build\previous-run\inputs\real-nine-ports.tsv
    $previousClasspath="$PSScriptRoot\..\build\ft-biomedungeon-core-v1.jar;$PSScriptRoot\..\next-planner-y\build\ft-biomedungeon-planner-y-v2-experimental.jar;$PSScriptRoot\build\previous-test"
    Push-Location -LiteralPath build\previous-run
    try {
        & $java -ea -cp $previousClasspath PlannerYTests 2>&1|Tee-Object -FilePath "$PSScriptRoot\logs\previous-y-regression.log"
        if($LASTEXITCODE -ne 0){throw 'V2 regression failed'}
    } finally {Pop-Location}
    & jar --create --file build\ft-biomedungeon-envelope-planner-v3-experimental.jar --date=2026-11-06T00:00:00Z -C build\main .
    if($LASTEXITCODE -ne 0){throw 'JAR packaging failed'}
    & javap -classpath 'build\ft-biomedungeon-envelope-planner-v3-experimental.jar;..\build\ft-biomedungeon-core-v1.jar' -verbose cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner|Select-String 'major version|minor version'|Set-Content -LiteralPath logs\classfile.log
    & jdeps --multi-release 17 --class-path ..\build\ft-biomedungeon-core-v1.jar --print-module-deps build\ft-biomedungeon-envelope-planner-v3-experimental.jar 2>&1|Tee-Object -FilePath logs\dependencies.log
    if($LASTEXITCODE -ne 0){throw 'Dependency inspection failed'}
} finally {Pop-Location}
