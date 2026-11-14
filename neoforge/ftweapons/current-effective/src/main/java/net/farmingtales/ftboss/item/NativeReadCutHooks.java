package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public final class NativeReadCutHooks {
    private static final String MARKER = "ftboss:p10:read-cut:v1";
    private static final String FRAME = "net/farmingtales/ftboss/item/NativeObservationFrame";
    private static final String OBSERVER = "net/farmingtales/ftboss/item/NativeReadCutObserver";

    private NativeReadCutHooks() {
    }

    public static int apply(ClassNode var0, String var1) {
        String var2 = var1.substring(var1.lastIndexOf(46) + 1);

        String var3 = switch (var2) {
            case "NativeReadCutItemStackMixin" -> "net/minecraft/world/item/ItemStack";
            case "NativeReadCutCapabilityProviderMixin" -> "net/minecraftforge/common/capabilities/CapabilityProvider";
            case "NativeReadCutEntityMixin" -> "net/minecraft/world/entity/Entity";
            case "NativeReadCutLivingEntityMixin" -> "net/minecraft/world/entity/LivingEntity";
            case "NativeReadCutPlayerMixin" -> "net/minecraft/world/entity/player/Player";
            case "NativeReadCutServerPlayerMixin" -> "net/minecraft/server/level/ServerPlayer";
            case "NativeReadCutProjectileMixin" -> "net/minecraft/world/entity/projectile/Projectile";
            case "NativeReadCutMinecraftServerMixin" -> "net/minecraft/server/MinecraftServer";
            default -> null;
        };
        if (var3 == null) {
            return 0;
        } else if (!var3.equals(var0.name)) {
            throw new IllegalStateException("Read-cut target mismatch " + var0.name);
        } else {
            ArrayList<MethodNode> var4 = new ArrayList<>();

            for (NativeReadCutHooks.Rule var6 : rules(var3)) {
                List<MethodNode> var7 = var0.methods
                    .stream()
                    .filter(var1x -> var1x.desc.equals(var6.descriptor) && (var1x.name.equals(var6.official) || var1x.name.equals(var6.srg)))
                    .toList();
                if (var7.size() != 1) {
                    throw new IllegalStateException("Missing or duplicate read-cut mutator " + var3 + "." + var6);
                }

                MethodNode var8 = (MethodNode)var7.get(0);
                if ((var8.access & 1288) != 0) {
                    throw new IllegalStateException("Non-instance read-cut mutator " + var8.name);
                }

                for (AbstractInsnNode var10 : var8.instructions) {
                    if (var10 instanceof VarInsnNode var11 && var11.var == 0 && var11.getOpcode() == 58) {
                        throw new IllegalStateException("Read-cut receiver reassigned " + var8.name);
                    }
                }

                var4.add(var8);
            }

            int var13 = 0;
            String var14 = !var3.equals("net/minecraft/world/item/ItemStack") && !var3.equals("net/minecraftforge/common/capabilities/CapabilityProvider")
                ? "player"
                : "weapon";

            for (MethodNode var16 : var4) {
                String var17 = var3.equals("net/minecraft/server/MinecraftServer")
                    ? "server"
                    : (
                        var3.equals("net/minecraft/world/entity/projectile/Projectile")
                            ? "projectile"
                            : (
                                !var16.desc.equals("(Lnet/minecraft/world/entity/Entity$RemovalReason;)V")
                                        || !var16.name.equals("remove") && !var16.name.equals("m_142687_")
                                    ? var14
                                    : "removed"
                            )
                    );
                if (install(var0, var16, var17)) {
                    var13++;
                }
            }

            return var13;
        }
    }

    private static List<NativeReadCutHooks.Rule> rules(String var0) {
        return switch (var0) {
            case "net/minecraft/world/entity/projectile/Projectile" -> List.of(
            new NativeReadCutHooks.Rule("setOwner", "m_5602_", "(Lnet/minecraft/world/entity/Entity;)V")
        );
            case "net/minecraft/server/MinecraftServer" -> List.of(
            new NativeReadCutHooks.Rule("tickServer", "m_5705_", "(Ljava/util/function/BooleanSupplier;)V"),
            new NativeReadCutHooks.Rule("stopServer", "m_7041_", "()V"),
            new NativeReadCutHooks.Rule("reloadResources", "m_129861_", "(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;")
        );
            case "net/minecraft/world/item/ItemStack" -> List.of(
            new NativeReadCutHooks.Rule("setDamageValue", "m_41721_", "(I)V"),
            new NativeReadCutHooks.Rule("getOrCreateTag", "m_41784_", "()Lnet/minecraft/nbt/CompoundTag;"),
            new NativeReadCutHooks.Rule("getOrCreateTagElement", "m_41698_", "(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"),
            new NativeReadCutHooks.Rule("removeTagKey", "m_41749_", "(Ljava/lang/String;)V"),
            new NativeReadCutHooks.Rule("setTag", "m_41751_", "(Lnet/minecraft/nbt/CompoundTag;)V"),
            new NativeReadCutHooks.Rule("setHoverName", "m_41714_", "(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;"),
            new NativeReadCutHooks.Rule("resetHoverName", "m_41787_", "()V"),
            new NativeReadCutHooks.Rule("hideTooltipPart", "m_41654_", "(Lnet/minecraft/world/item/ItemStack$TooltipPart;)V"),
            new NativeReadCutHooks.Rule("enchant", "m_41663_", "(Lnet/minecraft/world/item/enchantment/Enchantment;I)V"),
            new NativeReadCutHooks.Rule("addTagElement", "m_41700_", "(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V"),
            new NativeReadCutHooks.Rule("setRepairCost", "m_41742_", "(I)V"),
            new NativeReadCutHooks.Rule("setCount", "m_41764_", "(I)V"),
            new NativeReadCutHooks.Rule("grow", "m_41769_", "(I)V"),
            new NativeReadCutHooks.Rule("shrink", "m_41774_", "(I)V")
        );
            case "net/minecraftforge/common/capabilities/CapabilityProvider" -> List.of(
            new NativeReadCutHooks.Rule("gatherCapabilities", "gatherCapabilities", "()V"),
            new NativeReadCutHooks.Rule("gatherCapabilities", "gatherCapabilities", "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)V"),
            new NativeReadCutHooks.Rule("gatherCapabilities", "gatherCapabilities", "(Ljava/util/function/Supplier;)V"),
            new NativeReadCutHooks.Rule("deserializeCaps", "deserializeCaps", "(Lnet/minecraft/nbt/CompoundTag;)V"),
            new NativeReadCutHooks.Rule("invalidateCaps", "invalidateCaps", "()V"),
            new NativeReadCutHooks.Rule("reviveCaps", "reviveCaps", "()V")
        );
            case "net/minecraft/world/entity/Entity" -> List.of(
            new NativeReadCutHooks.Rule("remove", "m_142687_", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            new NativeReadCutHooks.Rule("setPos", "m_146884_", "(Lnet/minecraft/world/phys/Vec3;)V"),
            new NativeReadCutHooks.Rule("setPos", "m_6034_", "(DDD)V"),
            new NativeReadCutHooks.Rule("changeDimension", "m_5489_", "(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"),
            new NativeReadCutHooks.Rule(
                "changeDimension",
                "changeDimension",
                "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;"
            ),
            new NativeReadCutHooks.Rule("setLevel", "m_284535_", "(Lnet/minecraft/world/level/Level;)V")
        );
            case "net/minecraft/world/entity/LivingEntity" -> List.of(
            new NativeReadCutHooks.Rule("setHealth", "m_21153_", "(F)V"),
            new NativeReadCutHooks.Rule("setItemInHand", "m_21008_", "(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V")
        );
            case "net/minecraft/world/entity/player/Player" -> List.of(
            new NativeReadCutHooks.Rule("die", "m_6667_", "(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            new NativeReadCutHooks.Rule("remove", "m_142687_", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            new NativeReadCutHooks.Rule("setItemSlot", "m_8061_", "(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V")
        );
            case "net/minecraft/server/level/ServerPlayer" -> List.of(
            new NativeReadCutHooks.Rule("die", "m_6667_", "(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            new NativeReadCutHooks.Rule(
                "changeDimension",
                "changeDimension",
                "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;"
            ),
            new NativeReadCutHooks.Rule("restoreFrom", "m_9015_", "(Lnet/minecraft/server/level/ServerPlayer;Z)V"),
            new NativeReadCutHooks.Rule("teleportTo", "m_6021_", "(DDD)V"),
            new NativeReadCutHooks.Rule("teleportRelative", "m_246847_", "(DDD)V"),
            new NativeReadCutHooks.Rule("teleportTo", "m_264318_", "(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"),
            new NativeReadCutHooks.Rule("setGameMode", "m_143403_", "(Lnet/minecraft/world/level/GameType;)Z"),
            new NativeReadCutHooks.Rule("teleportTo", "m_8999_", "(Lnet/minecraft/server/level/ServerLevel;DDDFF)V")
        );
            default -> throw new IllegalStateException("No read-cut rules " + var0);
        };
    }

    static boolean install(ClassNode var0, MethodNode var1, String var2) {
        for (AbstractInsnNode var4 : var1.instructions) {
            if (var4.getOpcode() >= 0) {
                if (var4 instanceof LdcInsnNode var5 && "ftboss:p10:read-cut:v1".equals(var5.cst)) {
                    return false;
                }

                if (var4 instanceof LdcInsnNode var33 && ("ftboss:p8:read-cut:v1".equals(var33.cst) || "ftboss:p9:read-cut:v1".equals(var33.cst))) {
                    throw new IllegalStateException("Stale transformed method is not an original P10 input " + var1.name);
                }
                break;
            }
        }

        Type var31 = Type.getReturnType(var1.desc);
        int var32 = var1.maxLocals;
        int var34 = var32 + var31.getSize();
        int var6 = var34 + 1;
        int var7 = var6 + 1;
        ArrayList var8 = new ArrayList();
        var8.add(var0.name);

        for (Type var12 : Type.getArgumentTypes(var1.desc)) {
            var8.add(frameType(var12));
        }

        Map<AbstractInsnNode, List<Integer>> var35 = returnRemainders(var0, var1, var31);
        carryObservationFrame(var1, var8, var34);
        LabelNode var36 = new LabelNode();
        LabelNode var37 = new LabelNode();
        LabelNode var38 = new LabelNode();
        LabelNode var13 = new LabelNode();
        LabelNode var14 = new LabelNode();
        LabelNode var15 = new LabelNode();
        LabelNode var16 = new LabelNode();
        LabelNode var17 = new LabelNode();
        LabelNode var18 = new LabelNode();
        LabelNode var19 = new LabelNode();
        LabelNode var20 = new LabelNode();
        LabelNode var21 = new LabelNode();
        LabelNode var22 = new LabelNode();
        LabelNode var23 = new LabelNode();
        ArrayList<AbstractInsnNode> var24 = new ArrayList<>();

        for (AbstractInsnNode var26 : var1.instructions) {
            int var27 = var26.getOpcode();
            if (var27 >= 172 && var27 <= 177) {
                var24.add(var26);
            }
        }

        InsnList var39 = new InsnList();
        var39.add(new LdcInsnNode("ftboss:p10:read-cut:v1"));
        var39.add(new InsnNode(87));
        var39.add(new VarInsnNode(25, 0));
        var39.add(new LdcInsnNode(var0.name + "." + var1.name + var1.desc));
        var39.add(
            new MethodInsnNode(
                184,
                "net/farmingtales/ftboss/item/NativeObservationFrame",
                "open",
                "(Ljava/lang/Object;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/NativeObservationFrame;",
                false
            )
        );
        var39.add(new VarInsnNode(58, var34));
        var39.add(var36);
        observe(var39, var2);
        var39.add(var37);
        var39.add(new JumpInsnNode(167, var13));
        var39.add(var38);
        var39.add(observationFrame(var8, var34, -1, null, "java/lang/Throwable"));
        var39.add(new VarInsnNode(58, var7));
        record(var1, var39, var34, var7, 0, var8, -1, null);
        var39.add(var13);
        var39.add(observationFrame(var8, var34, -1, null, null));
        var1.instructions.insert(var39);

        for (AbstractInsnNode var42 : var24) {
            InsnList var28 = new InsnList();
            if (var31.getSort() != 0) {
                var28.add(new VarInsnNode(var31.getOpcode(54), var32));
            }

            for (int var30 : var35.get(var42)) {
                var28.add(new InsnNode(var30 == 2 ? 88 : 87));
            }

            var28.add(new JumpInsnNode(167, var15));
            var1.instructions.insertBefore(var42, var28);
            var1.instructions.remove(var42);
        }

        List var41 = List.of(var0.name);
        var1.instructions.add(var14);
        var1.instructions.add(var16);
        var1.instructions.add(observationFrame(var41, var34, -1, null, "java/lang/Throwable"));
        var1.instructions.add(new VarInsnNode(58, var6));
        var1.instructions.add(new VarInsnNode(25, var34));
        var1.instructions.add(new VarInsnNode(25, var6));
        var1.instructions
            .add(new MethodInsnNode(182, "net/farmingtales/ftboss/item/NativeObservationFrame", "originalFailed", "(Ljava/lang/Throwable;)V", false));
        var1.instructions.add(var17);
        observe(var1.instructions, var2);
        var1.instructions.add(var18);
        var1.instructions.add(new JumpInsnNode(167, var20));
        var1.instructions.add(var19);
        var1.instructions.add(observationFrame(var41, var34, var6, "java/lang/Throwable", "java/lang/Throwable"));
        var1.instructions.add(new VarInsnNode(58, var7));
        record(var1, var1.instructions, var34, var7, 2, var41, var6, "java/lang/Throwable");
        var1.instructions.add(var20);
        var1.instructions.add(observationFrame(var41, var34, var6, "java/lang/Throwable", null));
        var1.instructions.add(new VarInsnNode(25, var6));
        var1.instructions.add(new InsnNode(191));
        if (!var24.isEmpty()) {
            int var43 = var31.getSort() == 0 ? -1 : var32;
            var1.instructions.add(var15);
            var1.instructions.add(observationFrame(var41, var34, var43, frameType(var31), null));
            observe(var1.instructions, var2);
            var1.instructions.add(var21);
            var1.instructions.add(new JumpInsnNode(167, var23));
            var1.instructions.add(var22);
            var1.instructions.add(observationFrame(var41, var34, var43, frameType(var31), "java/lang/Throwable"));
            var1.instructions.add(new VarInsnNode(58, var7));
            record(var1, var1.instructions, var34, var7, 1, var41, var43, frameType(var31));
            var1.instructions.add(var23);
            var1.instructions.add(observationFrame(var41, var34, var43, frameType(var31), null));
            if (var31.getSort() != 0) {
                var1.instructions.add(new VarInsnNode(var31.getOpcode(21), var32));
            }

            var1.instructions.add(new InsnNode(var31.getOpcode(172)));
            var1.tryCatchBlocks.add(new TryCatchBlockNode(var15, var21, var22, "java/lang/Throwable"));
        }

        var1.tryCatchBlocks.add(new TryCatchBlockNode(var36, var37, var38, "java/lang/Throwable"));
        var1.tryCatchBlocks.add(new TryCatchBlockNode(var13, var14, var16, "java/lang/Throwable"));
        var1.tryCatchBlocks.add(new TryCatchBlockNode(var17, var18, var19, "java/lang/Throwable"));
        var1.maxLocals = var7 + 1;
        var1.maxStack = Math.max(var1.maxStack, 3);
        return true;
    }

    private static Map<AbstractInsnNode, List<Integer>> returnRemainders(ClassNode var0, MethodNode var1, Type var2) {
        Frame[] var3;
        try {
            var3 = new Analyzer(new BasicInterpreter()).analyze(var0.name, var1);
        } catch (AnalyzerException var12) {
            throw new IllegalStateException("Invalid original read-cut control flow " + var0.name + "." + var1.name + var1.desc, var12);
        }

        IdentityHashMap var4 = new IdentityHashMap();

        for (int var5 = 0; var5 < var1.instructions.size(); var5++) {
            AbstractInsnNode var6 = var1.instructions.get(var5);
            int var7 = var6.getOpcode();
            if (var7 >= 172 && var7 <= 177) {
                Frame var8 = var3[var5];
                if (var8 == null) {
                    var4.put(var6, List.of());
                } else {
                    int var9 = var8.getStackSize() - (var2.getSort() == 0 ? 0 : 1);
                    if (var9 < 0) {
                        throw new IllegalStateException("Missing original return value " + var1.name + var1.desc);
                    }

                    ArrayList var10 = new ArrayList();

                    for (int var11 = var9 - 1; var11 >= 0; var11--) {
                        var10.add(((BasicValue)var8.getStack(var11)).getSize());
                    }

                    var4.put(var6, var10);
                }
            }
        }

        return var4;
    }

    private static void record(MethodNode var0, InsnList var1, int var2, int var3, int var4, List<Object> var5, int var6, Object var7) {
        var1.add(new VarInsnNode(25, var2));
        var1.add(new InsnNode(3 + var4));
        var1.add(new VarInsnNode(25, var3));
        var1.add(new MethodInsnNode(182, "net/farmingtales/ftboss/item/NativeObservationFrame", "observationFailed", "(ILjava/lang/Throwable;)V", false));
        LabelNode var8 = new LabelNode();
        LabelNode var9 = new LabelNode();
        LabelNode var10 = new LabelNode();
        LabelNode var11 = new LabelNode();
        var1.add(var8);
        var1.add(new VarInsnNode(25, 0));
        var1.add(new VarInsnNode(25, var2));
        var1.add(
            new MethodInsnNode(
                184,
                "net/farmingtales/ftboss/item/NativeReadCutObserver",
                "failed",
                "(Ljava/lang/Object;Lnet/farmingtales/ftboss/item/NativeObservationFrame;)V",
                false
            )
        );
        var1.add(var9);
        var1.add(new JumpInsnNode(167, var11));
        var1.add(var10);
        var1.add(observationFrame(var5, var2, var6, var7, "java/lang/Throwable"));
        var1.add(new VarInsnNode(58, var3));
        var1.add(new VarInsnNode(25, var2));
        var1.add(new InsnNode(6));
        var1.add(new VarInsnNode(25, var3));
        var1.add(new MethodInsnNode(182, "net/farmingtales/ftboss/item/NativeObservationFrame", "observationFailed", "(ILjava/lang/Throwable;)V", false));
        var1.add(var11);
        var0.tryCatchBlocks.add(new TryCatchBlockNode(var8, var9, var10, "java/lang/Throwable"));
    }

    private static void carryObservationFrame(MethodNode var0, List<Object> var1, int var2) {
        ArrayList var3 = new ArrayList(var1);

        for (AbstractInsnNode var5 : var0.instructions) {
            if (var5 instanceof FrameNode) {
                FrameNode var6 = (FrameNode)var5;
                switch (var6.type) {
                    case -1:
                    case 0:
                        var3 = new ArrayList(var6.local);
                        break;
                    case 1:
                        var3.addAll(var6.local);
                        break;
                    case 2:
                        var3 = new ArrayList(var3.subList(0, var3.size() - var6.local.size()));
                    case 3:
                    case 4:
                        break;
                    default:
                        throw new IllegalStateException("Unknown original StackMap frame");
                }

                var6.type = 0;
                if (var6.stack == null) {
                    var6.stack = new ArrayList();
                }

                var6.local = new ArrayList(var3);
                appendLocal(var6.local, var2, "net/farmingtales/ftboss/item/NativeObservationFrame");
            }
        }
    }

    private static FrameNode observationFrame(List<Object> var0, int var1, int var2, Object var3, String var4) {
        ArrayList var5 = new ArrayList(var0);
        if (var2 >= 0 && var2 < var1) {
            appendLocal(var5, var2, var3);
        }

        appendLocal(var5, var1, "net/farmingtales/ftboss/item/NativeObservationFrame");
        if (var2 > var1) {
            appendLocal(var5, var2, var3);
        }

        return new FrameNode(0, var5.size(), var5.toArray(), var4 == null ? 0 : 1, var4 == null ? new Object[0] : new Object[]{var4});
    }

    private static void appendLocal(List<Object> var0, int var1, Object var2) {
        int var3 = 0;

        for (Object var5 : var0) {
            var3 += !var5.equals(Opcodes.LONG) && !var5.equals(Opcodes.DOUBLE) ? 1 : 2;
        }

        if (var3 > var1) {
            throw new IllegalStateException("Overlapping original StackMap local");
        } else {
            while (var3++ < var1) {
                var0.add(Opcodes.TOP);
            }

            var0.add(var2);
        }
    }

    private static void observe(InsnList var0, String var1) {
        var0.add(new VarInsnNode(25, 0));
        var0.add(new MethodInsnNode(184, "net/farmingtales/ftboss/item/NativeReadCutObserver", var1, "(Ljava/lang/Object;)V", false));
    }

    private static Object frameType(Type var0) {
        return switch (var0.getSort()) {
            case 0 -> null;
            default -> Opcodes.INTEGER;
            case 6 -> Opcodes.FLOAT;
            case 7 -> Opcodes.LONG;
            case 8 -> Opcodes.DOUBLE;
            case 9 -> var0.getDescriptor();
            case 10 -> var0.getInternalName();
        };
    }

    private static FrameNode frame(String var0, int var1, Object var2, String var3) {
        ArrayList var4 = new ArrayList();
        var4.add(var0);
        if (var1 >= 0) {
            while (var4.size() < var1) {
                var4.add(Opcodes.TOP);
            }

            var4.add(var2);
        }

        return new FrameNode(0, var4.size(), var4.toArray(), var3 == null ? 0 : 1, var3 == null ? new Object[0] : new Object[]{var3});
    }

    private static record Rule(String official, String srg, String descriptor) {
    }
}
