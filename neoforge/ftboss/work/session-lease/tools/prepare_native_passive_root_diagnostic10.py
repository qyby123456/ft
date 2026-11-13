from pathlib import Path

parent = Path(__file__).with_name("prepare_native_passive_root_diagnostic9.py")
source = parent.read_text(encoding="utf-8").replace("diagnostic9", "diagnostic10")
anchor = '((int[])readStatic(registry, "lateStages"))'
assert source.count(anchor) == 1
source = source.replace(anchor, '((byte[])readStatic(registry, "lateStages"))')
source = source.replace('paths = [Path(__file__), parent_path,', 'paths = [Path(__file__), Path(__file__).with_name("prepare_native_passive_root_diagnostic9.py"), parent_path,')
exec(compile(source, str(Path(__file__)), "exec"), {"__file__": __file__, "__name__": "__main__"})
