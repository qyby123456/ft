import sys
from pathlib import Path

parent = Path(__file__).with_name("prepare_native_passive_root_diagnostic4.py")
source = parent.read_text(encoding="utf-8").replace("diagnostic4", "diagnostic5")
anchor = '                            } catch (Throwable failure) { incomplete.add(required[index][0] + ":" + failure); }'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                            } catch (Throwable failure) {
                                Throwable cause = failure instanceof java.lang.reflect.InvocationTargetException wrapped
                                    ? wrapped.getCause() : failure;
                                incomplete.add(required[index][0] + ":" + cause + ":" +
                                    java.util.Arrays.stream(cause.getStackTrace()).limit(5).map(Object::toString).toList());
                            }''')
anchor = 'extra = '\'\'\'                        Class<?> registry'
assert source.count(anchor) == 1
source = source.replace(anchor, '''extra = '\'\'\'                        ThreadLocal<?> passiveRootScope = (ThreadLocal<?>)readStatic(
                            Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority"), "PASSIVE_ROOT");
                        publicationRow.put("privatePassiveRootScopeCleared", passiveRootScope.get() == null);
                        require(passiveRootScope.get() == null, "Private original passive publication scope leaked");
                        Class<?> registry''')
source = source.replace('paths = [Path(__file__), parent_path,', 'paths = [Path(__file__), Path(__file__).with_name("prepare_native_passive_root_diagnostic4.py"), parent_path,')
exec(compile(source, str(Path(__file__)), "exec"), {"__file__": __file__, "__name__": "__main__"})
