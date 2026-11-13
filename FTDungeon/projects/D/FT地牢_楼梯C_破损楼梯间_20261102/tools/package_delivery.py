import zipfile

from build_room import ROOT, fingerprint, save_json


def run():
    release = ROOT / 'release'
    release.mkdir(exist_ok=True)
    output = release / 'STAIR_C_RUINED-20261102.zip'
    files = [ROOT / name for name in ('README.md', 'build.gradle', 'settings.gradle', 'gradle.properties', '.gitignore')]
    for directory in ('out', 'preview', 'reference', 'src', 'tools'):
        files.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    files.extend(ROOT / 'qa' / name for name in ('static.json', 'native.json', 'reproducibility.json', 'render.json', 'visual-review.json', 'forge-gametest.log'))
    files.extend((ROOT / 'qa').glob('native-placed-*.nbt'))
    for path in files:
        assert path.resolve().is_relative_to(ROOT.resolve()) and path.is_file()
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in sorted(set(files)):
            archive.write(path, str(path.relative_to(ROOT)))
    with zipfile.ZipFile(output) as archive:
        assert archive.testzip() is None
        assert not any(name.startswith(('build/', '.gradle/', 'qa/forge-run/')) or name.endswith('.jar') for name in archive.namelist())
        count = len(archive.namelist())
    receipt = {'file': str(output), 'sha256': fingerprint(output), 'bytes': output.stat().st_size, 'files': count, 'no_test_world_or_binaries': True}
    save_json(release / 'receipt.json', receipt)
    print(receipt)


if __name__ == '__main__':
    run()
