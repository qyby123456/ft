$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$java = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
Push-Location -LiteralPath $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build,logs,evidence | Out-Null
    "cwd=$PSScriptRoot`npython=$python -B review.py`ncompiler=javac --release 17 -encoding UTF-8 -Xlint:all -Werror`njava=$java`nMCWorld=NOT_RUN" | Set-Content -LiteralPath logs\commands.log -Encoding utf8
    & $python -B review.py 2>&1 | Tee-Object -FilePath logs\review.log
    if ($LASTEXITCODE -ne 0) { throw 'Python review failed' }
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build CoordinateContractTest.java 2>&1 | Tee-Object -FilePath logs\compile.log
    if ($LASTEXITCODE -ne 0) { throw 'Java compile failed' }
    & $java -ea -cp '..\build\ft-biomedungeon-core-v1.jar;build' CoordinateContractTest 2>&1 | Tee-Object -FilePath logs\java17.log
    if ($LASTEXITCODE -ne 0) { throw 'Java coordinate contract failed' }
    $jar = '..\..\runtime\next-unified\build\libs\ft-biome-dungeon-generator-forge-1.20.1-0.4.0-unified-candidate.jar'
    foreach ($class in @('NativeRouteAudit','Placement')) {
        & javap -p -c -classpath $jar "cn.ft.biomedungeon.runtime.$class" 2>&1 | Set-Content -LiteralPath "evidence\$class-javap.txt" -Encoding utf8
        if ($LASTEXITCODE -ne 0) { throw 'Runtime bytecode disassembly failed' }
    }
    & javap -p -c -classpath $jar cn.ft.biomedungeon.core.DungeonGeometry 2>&1 | Set-Content -LiteralPath evidence\DungeonGeometry-javap.txt -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Geometry bytecode disassembly failed' }
} finally { Pop-Location }
