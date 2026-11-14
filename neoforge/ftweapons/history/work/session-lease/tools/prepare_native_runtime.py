import difflib
import hashlib
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
WORK = Path(__file__).resolve().parents[1]
PATCHER = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'


def create(target, text):
    assert not target.exists(), str(target)
    completed = []
    remaining = text.splitlines()
    while remaining:
        chunk = []
        size = 0
        while remaining and (not chunk or size + len(remaining[0]) < 10000):
            line = remaining.pop(0)
            chunk.append(line)
            size += len(line) + 3
        if completed:
            patch = '*** Begin Patch\n*** Update File: ' + target.relative_to(ROOT).as_posix() + '\n@@\n'
            patch += ''.join(' ' + line + '\n' for line in completed[-10:])
            patch += ''.join('+' + line + '\n' for line in chunk) + '*** End of File\n*** End Patch\n'
        else:
            patch = '*** Begin Patch\n*** Add File: ' + target.relative_to(ROOT).as_posix() + '\n' + ''.join('+' + line + '\n' for line in chunk) + '*** End Patch\n'
        subprocess.run([PATCHER, '--codex-run-as-apply-patch', patch], cwd=ROOT, check=True)
        completed.extend(chunk)


text = (ROOT / 'work/session-projection/tests/PositiveWeapon436.java').read_text(encoding='utf-8-sig')
text = text.replace('                storage.save(original);', '''                event.getServer().getPlayerList().getPlayers().add(original);
                net.farmingtales.ftboss.item.NativeFoodWeaponWear.manage(original, sword);
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(sword);
                sword.setDamageValue(10);
                storage.save(original);
                event.getServer().getPlayerList().getPlayers().remove(original);''')
text = text.replace('                for (int hit = 0; hit < (sample == 1 ? 1 : 3); hit++) {', '''                runNativeRepair(event, restored, actual, row);
                input = ((OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                for (int hit = 0; hit < 1; hit++) {''')
text = text.replace('                        flight.put("damagePurpose", image.getString("DamagePurpose"));', '''                        flight.put("damagePurpose", image.getString("DamagePurpose"));
                        flight.put("serverSignaturePresent", image.getString("ServerSignature").length() == 64);
                        flight.put("repairEpochPresent", image.hasUUID("OwnerRepairAuthorityEpoch"));
                        if (!image.hasUUID("OwnerRepairAuthorityEpoch") || !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, restored)) throw new AssertionError("Repaired native signed snapshot missing");''')
method = '''
    private static void runNativeRepair(ServerStartedEvent event, ServerPlayer player, ItemStack weapon, Map<String, Object> row) throws Exception {
        var server = event.getServer();
        player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        server.getPlayerList().getPlayers().add(player);
        ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(player.getUUID(), player);
        row.put("scratchRegistrationNotAuthenticated", true);
        var statuePos = new net.minecraft.core.BlockPos(1, -59, 0);
        net.farmingtales.ftboss.block.CandyStatueBlock statue = null;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (block instanceof net.farmingtales.ftboss.block.CandyStatueBlock candidate && candidate.ownTint() == 2) { statue = candidate; break; }
        }
        if (statue == null) throw new AssertionError("Native repair statue missing");
        var half = net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
        server.overworld().setBlock(statuePos, statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER), 3);
        server.overworld().setBlock(statuePos.above(), statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
        var authority = (net.farmingtales.ftboss.transaction.FourWeaponTransactions)readStatic(net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime.class, "authority");
        if (authority == null) throw new AssertionError("Original runtime transaction authority absent");
        var boundary = new net.farmingtales.ftboss.transaction.ExistingTransactionBoundary(java.util.Set.of(), new net.farmingtales.ftboss.transaction.FoundationApprovedRules());
        var binding = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding(player.getUUID(), net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(weapon),
            net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.registryId(weapon), 0, 0, player.level().dimension().location().toString(), statuePos.asLong(), boundary.statueIdentity(player, statuePos));
        List<Object> transactions = new ArrayList<>();
        row.put("repairTransactions", transactions);
        for (int action = 0; action < 5; action++) {
            var operation = action == 0 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.START_REPAIR
                : action == 4 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR
                : net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            int index = action - 1;
            int expectedPayment = 0;
            int supplySlot = 2;
            if (action > 0 && action < 4) {
                expectedPayment = net.farmingtales.ftboss.util.FoodWeaponLifecycleRules.repairRequired(net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(weapon), index);
                net.minecraft.world.item.Item supply = index == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI;
                if (index == 0) {
                    supply = null;
                    for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) if (net.farmingtales.ftboss.item.FoodWeaponEvolution.matchesStatueFood(new ItemStack(item), 2)) { supply = item; break; }
                }
                if (supply == null) throw new AssertionError("Native task food supply missing");
                player.getInventory().setItem(supplySlot, new ItemStack(supply, expectedPayment));
            }
            ((net.minecraft.world.level.storage.ServerLevelData)server.overworld().getLevelData()).setGameTime(server.overworld().getGameTime() + 5L);
            var opened = authority.open(player.getUUID(), new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open(UUID.randomUUID(), binding));
            if (!opened.accepted() || opened.snapshot() == null || !opened.snapshot().allowed().contains(operation)) throw new AssertionError("Original transaction open denied: " + opened);
            var snapshot = opened.snapshot();
            var request = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request(UUID.randomUUID(), snapshot.session(), snapshot.credential(), binding,
                snapshot.version(), operation, 0, action > 0 && action < 4 ? index : 0, "");
            var reply = authority.submit(player.getUUID(), request);
            Map<String, Object> transaction = new LinkedHashMap<>();
            transaction.put("operation", operation.name()); transaction.put("accepted", reply.accepted()); transaction.put("code", reply.code().name());
            transaction.put("expectedPayment", expectedPayment); transaction.put("remainingSupply", player.getInventory().getItem(supplySlot).getCount());
            transactions.add(transaction);
            if (!reply.accepted()) throw new AssertionError("Original native repair transaction failed: " + transaction);
            if (action > 0 && action < 4 && !player.getInventory().getItem(supplySlot).isEmpty()) throw new AssertionError("Actual native payment did not consume supplies");
            authority.close(player.getUUID(), snapshot.session());
        }
        Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
        java.lang.reflect.Method leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
        java.lang.reflect.Method generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
        row.put("nonNullLease", leaseMethod.invoke(null, weapon) != null);
        row.put("repairGenerationPresent", generationMethod.invoke(null, weapon) != null);
        row.put("damageAfterRepair", weapon.getDamageValue());
        if (!Boolean.TRUE.equals(row.get("nonNullLease")) || !Boolean.TRUE.equals(row.get("repairGenerationPresent")) || weapon.getDamageValue() != 0) throw new AssertionError("Native claim did not publish genuine lease/generation");
    }
'''
text = text.replace('    private static Object verifySixWeaponScope436()', method + '\n    private static Object verifySixWeaponScope436()')
create(WORK / 'tests/PositiveWeapon436.java', text)

launcher = (ROOT / 'work/session-projection/tools/run_positive_saved.py').read_text(encoding='utf-8-sig')
launcher = launcher.replace("binding = WORK.parent / 'session-continuity/reports/candidate-transactional.json'", "binding = WORK / 'reports/native-repair-candidate.json'")
receipt_hash = hashlib.sha256((WORK / 'reports/native-repair-candidate.json').read_bytes()).hexdigest()
launcher = launcher.replace('813f1156373e2c0aa68edb94ca3267ce623bfba98d7a343741d9a6250a71be5f', receipt_hash)
launcher = launcher.replace("('positive-' + uuid.uuid4().hex[:8])", "('repair-runtime-' + uuid.uuid4().hex[:8])")
launcher = launcher.replace("output = WORK / 'reports/positive-saved.json'", "output = WORK / 'reports/native-repair-runtime.json'")
create(WORK / 'tools/run_native_repair.py', launcher)
