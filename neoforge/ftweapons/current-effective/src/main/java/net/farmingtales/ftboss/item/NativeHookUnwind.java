package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeHookUnwind {
    private NativeHookUnwind() {
    }

    public static int apply(ClassNode var0, String var1) {
        String var2;
        List<String> var3;
        if (var1.endsWith("NativeUseTickIngressMixin")) {
            var2 = "ftboss$v9$nativeUnwind$useTick";
            var3 = List.of("onUseTick");
        } else if (var1.endsWith("NativeReleaseIngressMixin")) {
            var2 = "ftboss$v9$nativeUnwind$release";
            var3 = List.of("releaseUsing");
        } else if (var1.endsWith("NativeLeftClickIngressMixin")) {
            var2 = "ftboss$v9$nativeUnwind$leftClick";
            var3 = List.of("onLeftClick");
        } else if (var1.endsWith("NativeAncientMeleeIngressMixin")) {
            var2 = "ftboss$v9$nativeUnwind$melee";
            var3 = List.of("hurtEnemy");
        } else if (var1.endsWith("NativeWearTokenMixin")) {
            return install(var0, "hurtAndBreak", "ftboss$v9$wearUnwind", "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V");
        } else {
            if (var1.endsWith("NativeIronSelectionMixin")) {
                return installIronUse(var0);
            }

            if (!var1.endsWith("NativeRecastTokenCleanupMixin")) {
                if (var1.endsWith("FoodWeaponStaffCastMixin")) {
                    return install(var0, "attemptInitiateCast", "ftboss$v9$ironAttemptUnwind") + install(var0, "castSpell", "ftboss$v9$ironCastUnwind");
                }

                return 0;
            }

            var2 = "ftboss$v9$recastUnwind";
            var3 = List.of("removeRecast");
        }

        int var4 = 0;

        for (String var6 : var3) {
            var4 += install(var0, var6, var2);
        }

        return var4;
    }

    private static int installIronUse(ClassNode var0) {
        if (!var0.name.equals("io/redspace/ironsspellbooks/player/ServerPlayerEvents")) {
            throw new IllegalStateException("Unexpected Iron use host " + var0.name);
        } else {
            String var1 = "net/farmingtales/ftboss/integ/NativeIronExecution$UseFrame";
            String var2 = "(L" + var1 + ";)V";
            String var3 = "(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;)V";
            String var4 = "(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;L" + var1 + ";)V";
            MethodNode var5 = ironUseHook(var0, "ftboss$v9$ironUseBegin", var4, true);
            MethodNode var6 = ironUseHook(var0, "ftboss$v9$ironUseReturn", var4, true);
            MethodNode var7 = ironUseHook(var0, "ftboss$v9$ironUseUnwind", var2, true);
            String var8 = "onUseItem";
            if (!var8.equals("onUseItem")) {
                throw new IllegalStateException("Unsupported Iron use namespace " + var8);
            } else {
                List var9 = var0.methods.stream().filter(var2x -> var2x.name.equals(var8) && var2x.desc.equals(var3) && (var2x.access & 1280) == 0 && (var2x.access & 8) != 0).toList();
                if (var9.size() != 1) {
                    throw new IllegalStateException("Missing unique actual Iron use method " + var0.name);
                } else {
                    MethodNode var10 = (MethodNode)var9.get(0);
                    String var11 = "ftboss$v9$unwindGuard$" + var7.name;

                    for (AbstractInsnNode var13 : var10.instructions) {
                        if (var13 instanceof MethodInsnNode var14 && var14.owner.equals(var0.name) && var14.name.equals(var11) && var14.desc.equals(var2)) {
                            return 0;
                        }
                    }

                    ArrayList<AbstractInsnNode> var23 = new ArrayList<>();
                    if (!var10.tryCatchBlocks.isEmpty()) {
                        throw new IllegalStateException("Conflicting Iron use exception regions " + var0.name);
                    } else {
                        for (AbstractInsnNode var27 : var10.instructions) {
                            if (var27.getOpcode() == 177) {
                                var23.add(var27);
                            }

                            if (var27 instanceof VarInsnNode var28 && var28.var < 1 && var28.getOpcode() >= 54 && var28.getOpcode() <= 58
                                || var27 instanceof IincInsnNode var29 && var29.var < 1) {
                                throw new IllegalStateException("Overwritten Iron use argument slot " + var0.name);
                            }
                        }

                        if (var23.size() != 4) {
                            throw new IllegalStateException("Expected four actual Iron event return sites " + var0.name);
                        } else {
                            int var25 = var10.maxLocals;
                            int var26 = var25 + 1;
                            int frameType = ironUseFrames(var10, var25, var1);
                            LabelNode var15 = new LabelNode();
                            LabelNode var16 = new LabelNode();
                            LabelNode var17 = new LabelNode();
                            InsnList var18 = new InsnList();
                            var18.add(new TypeInsnNode(187, var1));
                            var18.add(new InsnNode(89));
                            var18.add(new MethodInsnNode(183, var1, "<init>", "()V", false));
                            var18.add(new VarInsnNode(58, var25));
                            var18.add(var15);
                            var18.add(ironUseCall(var0, var5, var25));
                            var10.instructions.insert(var18);

                            for (AbstractInsnNode var20 : var23) {
                                var10.instructions.insertBefore(var20, ironUseCall(var0, var6, var25));
                            }

                            MethodNode var30 = ironUseGuard(var0, var7, var2, var1);
                            InsnList var31 = new InsnList();
                            var31.add(var16);
                            var31.add(var17);
                            ArrayList var21 = new ArrayList();

                            for (int var22 = 0; var22 < var25; var22++) {
                                var21.add(Opcodes.TOP);
                            }

                            var21.add(var1);
                            var31.add(new FrameNode(frameType, var21.size(), var21.toArray(), 1, new Object[]{"java/lang/Throwable"}));
                            var31.add(new VarInsnNode(58, var26));
                            var31.add(new VarInsnNode(25, var25));
                            var31.add(new MethodInsnNode(184, var0.name, var30.name, var2, false));
                            var31.add(new VarInsnNode(25, var26));
                            var31.add(new InsnNode(191));
                            var10.instructions.add(var31);
                            var10.tryCatchBlocks.add(new TryCatchBlockNode(var15, var16, var17, "java/lang/Throwable"));
                            var10.maxLocals = var26 + 1;
                            var10.maxStack = Math.max(6, var10.maxStack + 5);
                            return 1;
                        }
                    }
                }
            }
        }
    }

    private static MethodNode ironUseHook(ClassNode var0, String var1, String var2, boolean var3) {
        List var4 = var0.methods
            .stream()
            .filter(
                var3x -> (
                            var3x.name.equals(var1)
                                || var3x.name.startsWith(var1 + "_$md$")
                                || var3x.name.matches("md[^$]+\\$" + Pattern.quote(var1) + "\\$[0-9a-f]+")
                        )
                        && var3x.desc.equals(var2)
                        && (var3x.access & 8) != 0 == var3
            )
            .toList();
        if (var4.size() != 1) {
            throw new IllegalStateException("Missing unique actual merged Iron use hook " + var0.name + "." + var1);
        } else {
            return (MethodNode)var4.get(0);
        }
    }

    private static InsnList ironUseCall(ClassNode owner, MethodNode hook, int frameSlot) {
        InsnList call = new InsnList();
        call.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call.add(new VarInsnNode(Opcodes.ALOAD, frameSlot));
        call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner.name, hook.name, hook.desc, false));
        return call;
    }

    private static int ironUseFrames(MethodNode var0, int var1, String var2) {
        int frameType = Opcodes.F_FULL;
        for (AbstractInsnNode instruction : var0.instructions) {
            if (instruction instanceof FrameNode frame && frame.type == Opcodes.F_NEW) {
                frameType = Opcodes.F_NEW;
                break;
            }
        }
        ArrayList var3 = new ArrayList();

        for (Type var7 : Type.getArgumentTypes(var0.desc)) {
            var3.add(var7.getInternalName());
        }

        for (AbstractInsnNode var12 : var0.instructions) {
            if (var12 instanceof FrameNode) {
                FrameNode var13 = (FrameNode)var12;
                switch (var13.type) {
                    case -1:
                    case 0:
                        var3 = new ArrayList(var13.local);
                        break;
                    case 1:
                        var3.addAll(var13.local);
                        break;
                    case 2:
                        for (int var14 = 0; var14 < var13.local.size(); var14++) {
                            var3.remove(var3.size() - 1);
                        }
                    case 3:
                    case 4:
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Iron use frame " + var13.type);
                }

                ArrayList var15 = new ArrayList(var3);
                int var8 = 0;

                for (Object var10 : var3) {
                    var8 += !Opcodes.LONG.equals(var10) && !Opcodes.DOUBLE.equals(var10) ? 1 : 2;
                }

                if (var8 > var1) {
                    throw new IllegalStateException("Iron use original frame exceeds maxLocals");
                }

                while (var8++ < var1) {
                    var15.add(Opcodes.TOP);
                }

                var15.add(var2);
                if (var13.stack == null && (var13.type == 1 || var13.type == 2 || var13.type == 3)) {
                    var13.stack = new ArrayList();
                }

                var13.type = frameType;
                var13.local = var15;
            }
        }
        return frameType;
    }

    private static MethodNode ironUseGuard(ClassNode var0, MethodNode var1, String var2, String var3) {
        String var4 = "ftboss$v9$unwindGuard$" + var1.name;
        if (var0.methods.stream().anyMatch(var1x -> var1x.name.equals(var4))) {
            throw new IllegalStateException("Conflicting Iron use guard " + var4);
        } else {
            MethodNode var5 = new MethodNode(4106, var4, var2, null, null);
            LabelNode var6 = new LabelNode();
            LabelNode var7 = new LabelNode();
            LabelNode var8 = new LabelNode();
            var5.instructions.add(var6);
            var5.instructions.add(new VarInsnNode(25, 0));
            var5.instructions.add(new MethodInsnNode(184, var0.name, var1.name, var2, false));
            var5.instructions.add(var7);
            var5.instructions.add(new InsnNode(177));
            var5.instructions.add(var8);
            var5.instructions.add(new FrameNode(0, 1, new Object[]{var3}, 1, new Object[]{"java/lang/Throwable"}));
            var5.instructions.add(new InsnNode(87));
            var5.instructions.add(new InsnNode(177));
            var5.tryCatchBlocks.add(new TryCatchBlockNode(var6, var7, var8, "java/lang/Throwable"));
            var5.maxLocals = 1;
            var5.maxStack = 1;
            var0.methods.add(var5);
            return var5;
        }
    }

    private static int install(ClassNode var0, String var1, String var2) {
        return install(var0, var1, var2, null);
    }

    private static int install(ClassNode var0, String var1, String var2, String descriptor) {
        MethodNode var3 = var0.methods
            .stream()
            .filter(
                var1x -> (
                            var1x.name.equals(var2)
                                || var1x.name.startsWith(var2 + "_$md$")
                                || var1x.name.matches("md[^$]+\\$" + Pattern.quote(var2) + "\\$[0-9a-f]+")
                        )
                        && var1x.desc.equals("()V")
            )
            .findFirst()
            .orElse(null);
        if (var3 == null) {
            throw new IllegalStateException("Missing actual merged Native unwind handler " + var0.name + "." + var2);
        } else {
            List<MethodNode> nativeTargets = var0.methods.stream()
                .filter(method -> method.name.equals(var1) && (descriptor == null || method.desc.equals(descriptor)) && (method.access & 1280) == 0).toList();
            if (nativeTargets.isEmpty()) {
                throw new IllegalStateException("Missing actual native unwind target " + var0.name + "." + var1);
            }
            MethodNode var4 = guard(var0, var3);
            int var5 = 0;

            for (MethodNode var7 : nativeTargets) {
                if (var7.name.equals(var1) && (var7.access & 1280) == 0) {
                    boolean var8 = false;

                    for (AbstractInsnNode var10 : var7.instructions) {
                        if (var10 instanceof MethodInsnNode) {
                            MethodInsnNode var11 = (MethodInsnNode)var10;
                            if (var11.owner.equals(var0.name) && var11.name.equals(var4.name) && var11.desc.equals("()V")) {
                                var8 = true;
                            }
                        }
                    }

                    if (!var8) {
                        int var19 = var7.maxLocals;
                        LabelNode var20 = new LabelNode();
                        LabelNode var21 = new LabelNode();
                        LabelNode var12 = new LabelNode();
                        var7.instructions.insert(var20);
                        InsnList var13 = new InsnList();
                        var13.add(var21);
                        var13.add(var12);
                        ArrayList var14 = new ArrayList();
                        if ((var7.access & 8) == 0) {
                            var14.add(var0.name);
                        }

                        for (Type var18 : Type.getArgumentTypes(var7.desc)) {
                            var14.add(switch (var18.getSort()) {
                                case 1, 2, 3, 4, 5 -> Opcodes.INTEGER;
                                case 6 -> Opcodes.FLOAT;
                                case 7 -> Opcodes.LONG;
                                case 8 -> Opcodes.DOUBLE;
                                default -> var18.getInternalName();
                            });
                        }

                        int frameType = Opcodes.F_FULL;
                        for (AbstractInsnNode originalInstruction : var7.instructions) {
                            if (originalInstruction instanceof FrameNode originalFrame && originalFrame.type == Opcodes.F_NEW) {
                                frameType = Opcodes.F_NEW;
                                break;
                            }
                        }
                        var13.add(new FrameNode(frameType, var14.size(), var14.toArray(), 1, new Object[]{"java/lang/Throwable"}));
                        var13.add(new VarInsnNode(58, var19));
                        boolean var22 = (var4.access & 8) != 0;
                        if (!var22) {
                            var13.add(new VarInsnNode(25, 0));
                        }

                        var13.add(new MethodInsnNode(var22 ? 184 : 183, var0.name, var4.name, "()V", false));
                        var13.add(new VarInsnNode(25, var19));
                        var13.add(new InsnNode(191));
                        var7.instructions.add(var13);
                        var7.tryCatchBlocks.add(new TryCatchBlockNode(var20, var21, var12, "java/lang/Throwable"));
                        var7.maxLocals = var19 + 1;
                        var7.maxStack = Math.max(2, var7.maxStack);
                        var5++;
                    }
                }
            }

            return var5;
        }
    }

    private static MethodNode guard(ClassNode var0, MethodNode var1) {
        String var2 = "ftboss$v9$unwindGuard$" + var1.name;
        MethodNode var3 = var0.methods.stream().filter(var1x -> var1x.name.equals(var2) && var1x.desc.equals("()V")).findFirst().orElse(null);
        if (var3 != null) {
            return var3;
        } else {
            boolean var4 = (var1.access & 8) != 0;
            MethodNode var5 = new MethodNode(4098 | (var4 ? 8 : 0), var2, "()V", null, null);
            LabelNode var6 = new LabelNode();
            LabelNode var7 = new LabelNode();
            LabelNode var8 = new LabelNode();
            var5.instructions.add(var6);
            if (!var4) {
                var5.instructions.add(new VarInsnNode(25, 0));
            }

            var5.instructions.add(new MethodInsnNode(var4 ? 184 : 183, var0.name, var1.name, "()V", false));
            var5.instructions.add(var7);
            var5.instructions.add(new InsnNode(177));
            var5.instructions.add(var8);
            var5.instructions.add(new FrameNode(0, var4 ? 0 : 1, var4 ? new Object[0] : new Object[]{var0.name}, 1, new Object[]{"java/lang/Throwable"}));
            var5.instructions.add(new InsnNode(87));
            var5.instructions.add(new InsnNode(177));
            var5.tryCatchBlocks.add(new TryCatchBlockNode(var6, var7, var8, "java/lang/Throwable"));
            var5.maxLocals = var4 ? 0 : 1;
            var5.maxStack = 1;
            var0.methods.add(var5);
            return var5;
        }
    }
}
