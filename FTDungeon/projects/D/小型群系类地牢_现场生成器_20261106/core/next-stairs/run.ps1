$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build,logs | Out-Null
    "cwd=$PSScriptRoot`npython=-B investigate.py`njavac=--release 17 -encoding UTF-8 -Xlint:all -Werror`njava=Microsoft OpenJDK17.0.8 -ea; classpath=../build/ft-biomedungeon-core-v1.jar;build" | Set-Content -LiteralPath logs\commands.log -Encoding utf8
    & 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' -B investigate.py 2>&1 | Tee-Object -FilePath logs\source-investigation.log
    if ($LASTEXITCODE -ne 0) { throw 'Source investigation failed' }
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp ..\build\ft-biomedungeon-core-v1.jar -d build src\RealPortStudy.java 2>&1 | Tee-Object -FilePath logs\compile.log
    if ($LASTEXITCODE -ne 0) { throw 'Java study compile failed' }
    & 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe' -ea -cp '..\build\ft-biomedungeon-core-v1.jar;build' RealPortStudy 2>&1 | Tee-Object -FilePath logs\real-port-java17.log
    if ($LASTEXITCODE -ne 0) { throw 'Real port study failed' }
} finally { Pop-Location }
