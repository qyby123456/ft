from pathlib import Path

parent = Path(__file__).with_name("prepare_native_passive_root_diagnostic7.py")
source = parent.read_text(encoding="utf-8").replace("diagnostic7", "diagnostic8")
anchor = 'source = source.replace(anchor,'
position = source.index(anchor)
source = source[:position] + '''source = source.replace("if (!installed[index] || targets[index] == null || observations[index] == null) continue;",
    "if ((!installed[index] && !required[index][0].equals(\\"net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin\\")) || targets[index] == null || observations[index] == null) continue;")
source = source.replace('publicationRow.put("diagnostic_registryInvalid",',
    'publicationRow.put("diagnostic_lateStages", ((int[])readStatic(registry, "lateStages")).clone()); publicationRow.put("diagnostic_registryInvalid",')
''' + source[position:]
source = source.replace('paths = [Path(__file__),', 'paths = [Path(__file__), Path(__file__).with_name("prepare_native_passive_root_diagnostic7.py"),')
exec(compile(source, str(Path(__file__)), "exec"), {"__file__": __file__, "__name__": "__main__"})
