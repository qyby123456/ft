import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
relative = 'work/session-lease/proposal-native/net/farmingtales/ftboss/item/NativeItemComponentObservation.java'
target = root / relative
assert not target.exists()
source = root / 'work/session-continuity/proposal-transactional/net/farmingtales/ftboss/item/NativeItemComponentObservation.java'
text = source.read_text(encoding='utf-8-sig')
anchor = '    boolean nativeDamageOverride(int damage) {'
assert text.count(anchor) == 1
fragment = '''    boolean nativeMeleeDamageOverride(int damage) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerNativeSemanticAuthority.class || !caller.getMethodName().equals("nativeMeleeItemReturned")) throw new IllegalStateException("Native melee prototype observation denied");
        Integer original = this.prototype.get(DataComponents.DAMAGE);
        return original == null || damage != original.intValue();
    }

'''
text = text.replace(anchor, fragment + anchor)
lines = text.splitlines()
previous = []
while lines:
    chunk = []
    length = 0
    while lines and length + len(lines[0]) < 6000:
        line = lines.pop(0)
        chunk.append(line)
        length += len(line) + 1
    assert chunk
    if previous:
        body = '*** Update File: ' + relative + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
        suffix = '*** End of File\n'
    else:
        body = '*** Add File: ' + relative + '\n'
        suffix = ''
    patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
    result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
    assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
    previous.extend(chunk)
print('Prepared exact-caller melee physical prototype observation without changing original bow observer')
