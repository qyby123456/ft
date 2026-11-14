import hashlib
import json
from datetime import datetime
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
THREAD = '01a1c40b-b9ff-7f71-8abb-dcecbb37929f'
NL = chr(10)


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value, exclusive=False):
    with path.open('x' if exclusive else 'w', encoding='utf-8') as stream:
        stream.write(json.dumps(value, ensure_ascii=False, indent=2) + NL)


owner_path = ROOT / 'WRITE_OWNER.json'
claim_path = ROOT / 'WRITE_OWNER.claim'
owner = load(owner_path)
assert owner['activeWriterThreadId'] == THREAD and load(claim_path)['threadId'] == THREAD
assert THREAD not in owner['revokedWriterThreadIds']
process_check = load(WORK / 'reports/final-process-check.json')
assert not process_check['ownedProcessesRemaining']
current = ROOT / 'work/session-chain/reports/actual-current.json'
assert digest(current) == 'ccf1096fe2503d2385f3ee34e000981b85d09e9bf649c9b40cb11cab30aae6bc'
accepted = load(WORK / 'reports/accepted-runtime.json')
assert accepted['failures'] == 0 and accepted['activeCoveredNegativesAccepted']
attempts = []
for name in ['positive', 'active', 'positive-saved', 'active-saved']:
    report_path = WORK / f'reports/{name}.json'
    report = load(report_path)
    attempt = Path(report['attempt'])
    terminal = load(attempt / 'terminal308.json')
    attempts.append({'name': name, 'attempt': str(attempt), 'exitCode': terminal['exitCode'],
                     'stopSent': terminal['stopSent'], 'completeSave': terminal['completeSave'],
                     'probeFailures': report['probe']['failures'], 'reportSha256': digest(report_path),
                     'terminalSha256': digest(attempt / 'terminal308.json')})
status = {'threadId': THREAD, 'createdAt': datetime.now().astimezone().isoformat(),
          'reason': 'Context safety handoff; no new implementation after compaction.',
          'attempts': attempts, 'focusedAcceptanceFailures': accepted['failures'],
          'activeCoveredNegativesAccepted': True, 'candidatePromoted': False,
          'candidateReceiptSha256': digest(current), 'ownedProcessesRemaining': False,
          'fullSignedSessionAccepted': False, 'authenticatedMultiplayerAccepted': False,
          'installable': False, 'deployed': False, 'globalRollbackClaimed': False,
          'next': 'Audit focused evidence, then review and promote transactional candidate; genuine non-null lease, repair/payment, full signed Session remain.'}
save(WORK / 'reports/handoff-status.json', status, True)
previous = WORK / 'HANDOFF_PREDECESSOR.md'
with previous.open('xb') as stream:
    stream.write((ROOT / 'HANDOFF_SESSION_CHAIN.md').read_bytes())
notes = ['', '', '## Final context-safety handoff', '',
         'Both saved runs finished: native exit 0, stdin stop sent, complete save true; focused acceptance failures 0.',
         'Independent no-test-Mixin control and same-run control each completed three native hits. All four active-scope faults were rejected; original exception identity was preserved. Both native damage read phases retained a valid projection. Repair generation remains absent.',
         'No product implementation or candidate promotion occurred after the context boundary. tools/promote_current.py is prepared but UNEXECUTED. The cumulative candidate is unchanged and NOT-INSTALLABLE.',
         'The two earlier watchdog crashes remain failed evidence without completed save. No rollback is claimed. All frozen runtime inputs remained unchanged. No owned Java/Python/jcmd remains after this recorder exits.',
         'One handoff-script patch failed parsing before any write; absence of the target file was verified before correcting it. Two transport JSON parse failures never executed commands.',
         'This thread is revoked. Successor must audit then claim exclusively. See reports/handoff-status.json and the new root handoff.', '']
with (WORK / 'IMPLEMENTATION.md').open('a', encoding='utf-8') as stream:
    stream.write(NL.join(notes))
checkpoints = [current, ROOT / 'work/session-continuity/reports/candidate-transactional.json',
               ROOT / 'work/session-continuity/reports/checked-transactional.json',
               WORK / 'reports/positive-saved.json', WORK / 'reports/active-saved.json',
               WORK / 'reports/accepted-runtime.json', WORK / 'reports/handoff-status.json',
               WORK / 'reports/final-process-check.json', WORK / 'reports/takeover-audit.json',
               WORK / 'tools/accept_runtime.py', WORK / 'tools/promote_current.py',
               WORK / 'IMPLEMENTATION.md', previous, Path(__file__)]
lines = ['# NeoForge session projection handoff, 2026-11-10', '',
         '本线程因上下文安全边界停止新实现并交接。迁移未完成，NOT-INSTALLABLE，未部署。仅C工程可写；D历史和两个正式实例只读。Python必须用Bundled解释器并带-B。', '',
         '原累计候选actual-current保持ccf1096fe2503d2385f3ee34e000981b85d09e9bf649c9b40cb11cab30aae6bc；事务提案仍未提升。原14类链接/ASM及6项外部拒绝证据复用，不重跑冻结195/196/197、全量守卫或ZIP/CRC。', '',
         '## 本次结果', '',
         '活NBT通过原生二进制编码后读取脱离副本，避免导出活getAllKeys别名；JDI在原putLong返回处注入四项活动scope扰动，无测试Mixin、无产品证明变更、无清retired或伪造签名。', '',
         '独立无测试Mixin正向、active同次正向各完成同玩家同弓三次连续原生命中，traceComplete均true。错key、错receiver、可变keyset改写恢复、原异常四项均在coverageInstalled=true、traceRevokedBefore=false下实际注入并被拒绝，异常同身份保留。accepted-runtime.json聚合failures=0。两个saved运行exit0、stopSent=true、completeSave=true；debugger正常退出。', '',
         '伤害前beforeMitigation及beforeHealth均有有效read projection，但repair generation为空；非空lease、repair/payment、完整signed Session、认证多人均未通过。早期两次watchdog崩溃exit1且未完整保存，不算通过；保留失败和原生半写入事实，不声称全局回滚。进程与终端见handoff-status。', '',
         '## 下一线程', '',
         '先只读核验AGENTS、WRITE_OWNER、下列哈希、失败/终端和进程，再以FileMode.CreateNew创建WRITE_OWNER.claim；已有claim时保持只读报告冲突。旧线程已撤销，不得自动恢复权限。', '',
         '本次因上下文安全未提升候选。下一步核验accepted-runtime与事务提案，再审查尚未执行的work/session-projection/tools/promote_current.py后提升，并只做必要定向检查。不要盲重跑冻结生成器。随后通过真实签名交易贯通非空lease、repair/payment和完整signed Session。未注册临时ServerPlayer不能证明认证多人，禁止造authority/签名或放松coverage。', '',
         '继续处理provider内部修改撤销、十项外部配方缺口、旧协议/目录/翻译清理、六类变体、真实客户端/专服/认证多人/战斗、四向GL和实例闭环。保留剑弓弩斧三叉戟法杖，去除其他旧武器适配及FTBoss食物附魔；不改原版全局附魔、Independent Event或玩法值。', '',
         '旧失败、D缓存边界事故和历史依赖见work/session-projection/HANDOFF_PREDECESSOR.md及其引用。所有Python使用-B，本线程未新增D写入。', '',
         '| 文件 | SHA256 |', '| --- | --- |']
lines.extend('| ' + path.relative_to(ROOT).as_posix() + ' | ' + digest(path) + ' |' for path in checkpoints)
(ROOT / 'HANDOFF_SESSION_CHAIN.md').write_text(NL.join(lines) + NL, encoding='utf-8')
owner['revokedWriterThreadIds'].append(THREAD)
owner.update({'status': 'awaiting_successor_read_only_audit', 'activeWriterThreadId': None,
              'candidateReceiptSha256': digest(current), 'lastRuntimePid': 49876, 'lastRuntimeTerminal': True,
              'ownedJavaOrPythonProcessesRemaining': False,
              'latestHandoffStatus': 'work/session-projection/reports/handoff-status.json',
              'latestFocusedRuntimeEvidence': 'work/session-projection/reports/accepted-runtime.json',
              'latestCheckpointSha256': digest(ROOT / 'HANDOFF_SESSION_CHAIN.md'),
              'relinquishedAt': datetime.now().astimezone().isoformat(), 'installable': False, 'deployed': False})
save(owner_path, owner)
with (WORK / 'reports/claim-relinquished.json').open('xb') as stream:
    stream.write(claim_path.read_bytes())
assert load(claim_path)['threadId'] == THREAD
claim_path.unlink()
print(json.dumps({'revoked': THREAD, 'focusedFailures': accepted['failures'], 'candidatePromoted': False,
                  'checkpointSha256': digest(ROOT / 'HANDOFF_SESSION_CHAIN.md')}, ensure_ascii=False))
