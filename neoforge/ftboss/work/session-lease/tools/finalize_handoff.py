import hashlib
import json
import os
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
WORK = Path(__file__).resolve().parents[1]
THREAD = os.environ['CODEX_THREAD_ID']


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save_new(path, data):
    with path.open('x', encoding='utf-8') as stream:
        stream.write(json.dumps(data, ensure_ascii=False, indent=2) + '\n')


owner_path = ROOT / 'WRITE_OWNER.json'
owner = load(owner_path)
claim_path = ROOT / 'WRITE_OWNER.claim'
assert owner['activeWriterThreadId'] == THREAD == load(claim_path)['threadId']
assert THREAD not in owner['revokedWriterThreadIds']
current = ROOT / 'work/session-chain/reports/actual-current.json'
assert digest(current) == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
assert not current.with_suffix('.promoting.json').exists()
candidate = load(current)
assert not candidate['installable'] and not candidate['deployed']
assert not candidate['nonNullLeaseAccepted'] and not candidate['repairPaymentAccepted']
processes = load(WORK / 'reports/final-process-check.json')
assert not processes['ownedProcessesRemaining']
assert digest(WORK / 'HANDOFF_PREDECESSOR.md') == '198c414252ea7a3f61f58347506aa4a2d66968226c9b142bbe3f59e4461644c3'
for path, expected in candidate['inputHashes'].items():
    assert digest(Path(path)) == expected, path
status_path = WORK / 'reports/handoff-status.json'
files = [current, ROOT / 'work/session-projection/reports/promoted-current.json',
         ROOT / 'work/session-projection/reports/predecessor-current.json',
         ROOT / 'work/session-projection/reports/accepted-runtime.json',
         ROOT / 'work/session-continuity/reports/candidate-transactional.json',
         ROOT / 'work/session-continuity/reports/checked-transactional.json',
         WORK / 'HANDOFF_PREDECESSOR.md', WORK / 'IMPLEMENTATION.md',
         WORK / 'tools/promote_current.py', Path(__file__), WORK / 'reports/final-process-check.json']
status = {
    'threadId': THREAD,
    'createdAt': datetime.now().astimezone().isoformat(),
    'reason': 'Context safety after excessive diagnostic output; no guessed signed/native repair integration.',
    'candidatePromoted': True,
    'candidateReceiptSha256': digest(current),
    'productSourcesChangedThisSession': False,
    'newRuntimeStarted': False,
    'readOnlyTakeoverReceiptInputsChecked': 2802,
    'takeoverInputsIncludingAllRuntimeReportTerminalPairs': 2808,
    'resolvedHistoricalCheckpoint': 'work/session-projection/HANDOFF_PREDECESSOR.md',
    'hashMismatchesAfterExplicitHistoricalResolution': [],
    'focusedRuntimeEvidenceReused': True,
    'focusedClassInspectionExitCode': 0,
    'broadReadOnlySearchTerminal': {'session': 76017, 'exitCode': 0, 'consumed': True},
    'failedPatchInvocationsBeforeWrite': 2,
    'failedPythonInvocationsMissingTarget': 2,
    'powerShellParseFailuresBeforeExecution': 1,
    'ownedProcessesRemaining': False,
    'promotionTemporaryRemaining': False,
    'newNativePartialMutation': False,
    'predecessorWatchdogFailuresPreserved': True,
    'globalRollbackClaimed': False,
    'nonNullLeaseAccepted': False,
    'repairPaymentAccepted': False,
    'fullSignedSessionAccepted': False,
    'authenticatedMultiplayerAccepted': False,
    'installable': False,
    'deployed': False,
    'inputHashes': {str(path.relative_to(ROOT)).replace(os.sep, '/'): digest(path) for path in files},
}
save_new(status_path, status)
files.append(status_path)
lines = [
    '# NeoForge cumulative adoption handoff, 2026-11-10', '',
    '本线程因过量诊断输出触及上下文安全边界收尾，未在剩余上下文内猜测性修改签名链。迁移未完成，NOT-INSTALLABLE，未部署。', '',
    '唯一可写工程仍为本C目录；D历史与正式实例只读。所有Python必须使用bundled解释器并带-B。旧线程及本线程均已撤销写入权，不得自动恢复。', '',
    '## 本次累计候选已提升', '',
    'actual-current.json现为SHA256 `1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243`。构建为work/session-continuity/build/candidate-f42bcb60，1381类、716有效源码、208守卫。',
    '原ccf1096fe2503d2385f3ee34e000981b85d09e9bf649c9b40cb11cab30aae6bc收据保存于work/session-projection/reports/predecessor-current.json。提升工具仅适配新线程认领并把包装工具哈希写入收据；冻结原工具未修改。产品源码与类本轮没有新改动。', '',
    '已复用的通过范围仍仅为无测试Mixin三次连续弓命中、同次正向及四项活动scope扰动拒绝和原异常身份保留，聚合0失败，两个saved运行正常保存退出。早期两次watchdog失败保留。14类链接/ASM和6项外部拒绝输入未漂移，直接引用；未重跑连续命中/扰动、全量守卫、ZIP/CRC或195/196/197。', '',
    '## 优先解决的真实功能断点', '',
    '详见work/session-lease/IMPLEMENTATION.md。NativeFoodWeaponWear.eligible路径在StatueRepairMutation.commit绕过OwnerStatueTaskService；其原生支付SavedData不等于旧签名lease。旧wear admission拒绝eligible，旧wear plan及task origin又拒绝Foundation/SwordCavity。现有通过的bow夹具初始化了Foundation，不能证明非空lease。',
    '旧prepareStart外层要求Request为空；任务origin和Committed也绑定原始legacy UI。knownStackPolicy仅四种精确原版类，斧/法杖还有缺口。不能通过直接删除这些条件、手造lease/authority/签名、重置retired或临时未认证玩家来填验收。应把真实原生START/实际支付/CLAIM按独立计划接入现有签名存储与原始提交链。', '',
    '## 接手规则与余项', '',
    '先只读核验AGENTS、WRITE_OWNER、以下哈希、最新status、前任失败与进程，再用FileMode.CreateNew独占创建WRITE_OWNER.claim。已有claim保持只读。成功后更新WRITE_OWNER并继续，不等待催办。',
    '不要再次提升旧事务提案覆盖新累计收据；不要重跑冻结通过项。继续非空lease、repair/payment、完整signed Session，然后provider内部修改撤销、十项外部配方、旧协议/目录/翻译、六类变体、真实客户端/专服/认证多人/战斗、四向实际GL及实例闭环。保留剑弓弩斧三叉戟法杖，删除其他旧适配及FTBoss食物附魔，保留原版全局附魔与Independent Event。完整验收前不部署。',
    '本轮无新游戏运行、无游戏半写入；promotion临时文件不存在、无项目残留进程。工具调用失败及误触广搜已记录，session76017退出0已消费；失败patch未写入。前任全部交接保存在work/session-lease/HANDOFF_PREDECESSOR.md及其引用。', '',
    '| 文件 | SHA256 |', '| --- | --- |',
]
for path in files:
    lines.append('| ' + str(path.relative_to(ROOT)).replace(os.sep, '/') + ' | ' + digest(path) + ' |')
checkpoint = ROOT / 'HANDOFF_SESSION_CHAIN.md'
checkpoint.write_text('\n'.join(lines) + '\n', encoding='utf-8')
owner['activeWriterThreadId'] = None
owner['status'] = 'awaiting_successor_read_only_audit'
owner['revokedWriterThreadIds'].append(THREAD)
owner['relinquishedAt'] = datetime.now().astimezone().isoformat()
owner['latestCheckpointSha256'] = digest(checkpoint)
owner['latestHandoffStatus'] = str(status_path.relative_to(ROOT)).replace(os.sep, '/')
owner['candidateReceiptSha256'] = digest(current)
owner['ownedJavaOrPythonProcessesRemaining'] = False
temporary = owner_path.with_suffix('.releasing.json')
with temporary.open('x', encoding='utf-8') as stream:
    stream.write(json.dumps(owner, ensure_ascii=False, indent=2) + '\n')
    stream.flush()
    os.fsync(stream.fileno())
os.replace(temporary, owner_path)
claim_path.unlink()
print(json.dumps({'candidateSha256': digest(current), 'checkpointSha256': digest(checkpoint), 'writerRevoked': True, 'claimReleased': not claim_path.exists(), 'installable': False, 'deployed': False}))
