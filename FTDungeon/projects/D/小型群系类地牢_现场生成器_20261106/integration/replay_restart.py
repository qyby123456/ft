import argparse
import os
from pathlib import Path

import integrate


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', required=True, type=Path)
    parser.add_argument('--gradle', required=True, type=Path)
    parser.add_argument('--java-home', required=True, type=Path)
    args = parser.parse_args()
    frozen = args.snapshot.resolve()
    if not frozen.is_relative_to(integrate.HOME / 'staging'):
        raise integrate.GateError('Integration snapshot required')
    project = frozen / 'assembled'
    run = integrate.HOME / 'run' / (frozen.name + '_restart')
    run.mkdir(parents=True, exist_ok=False)
    world = run / 'server'
    literal = str(world).replace('\\', '/')
    init = run / 'restart-isolation.gradle'
    init.write_text("gradle.projectsEvaluated { allprojects { project ->\n def minecraft = project.extensions.findByName('minecraft')\n if (minecraft != null) { minecraft.runs.configureEach { workingDirectory new File('" + literal + "') } }\n project.tasks.withType(JavaExec).configureEach { doFirst { workingDir new File('" + literal + "'); workingDir.mkdirs() } }\n} }", encoding='utf-8')
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(args.java_home.resolve())
    command = [os.environ['COMSPEC'], '/d', '/c', str(args.gradle.resolve()), '--no-daemon', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '--console=plain', '--project-cache-dir', str(run / 'gradle-project-cache'), '-I', str(init), '-PcoreSource=' + str(frozen / 'core/src/main/java')]
    stages = []
    for stage in ('prepare', 'resume'):
        result = integrate.execute(command + ['-PrestartStage=' + stage, 'runGameTestServer'], project, run / (stage + '.log'), 600, environment)
        stages.append(result)
        if result['status'] != 'PASS':
            break
    logs = '\n'.join((run / (stage + '.log')).read_text(encoding='utf-8', errors='replace') for stage in ('prepare', 'resume') if (run / (stage + '.log')).exists())
    markers = [line for line in logs.splitlines() if 'RUNTIME_RESTART_' in line]
    passed = len(stages) == 2 and all(item['status'] == 'PASS' for item in stages) and 'RUNTIME_RESTART_PASS' in logs
    evidence = dict(status='PASS_CLEAN_INTERRUPTED_RECOVERY' if passed else 'FAIL', scope='Independent replay of owner restart fixtures against exact frozen sources, distinct JVMs; NOT forced kill/powerloss or installed JAR', snapshotLockSha256=integrate.digest(frozen / 'snapshot-lock.json'), stages=stages, assertions=markers)
    integrate.save(run / 'result.json', evidence)
    integrate.save(integrate.HOME / 'restart-replay-result.json', evidence)
    print(evidence)
    return 0 if passed else 2


if __name__ == '__main__':
    raise SystemExit(main())
