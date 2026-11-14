from pathlib import Path

parent = Path(__file__).with_name("prepare_native_passive_root_diagnostic6.py")
source = parent.read_text(encoding="utf-8").replace("diagnostic6", "diagnostic7")
anchor = 'require(passiveRootScope.get() == null, "Private original passive publication scope leaked");'
assert source.count(anchor) == 1
source = source.replace(anchor, 'if (passiveRootScope.get() != null) throw new AssertionError("Private original passive publication scope leaked");')
source = source.replace('paths = [Path(__file__),', 'paths = [Path(__file__), Path(__file__).with_name("prepare_native_passive_root_diagnostic6.py"),')
exec(compile(source, str(Path(__file__)), "exec"), {"__file__": __file__, "__name__": "__main__"})
