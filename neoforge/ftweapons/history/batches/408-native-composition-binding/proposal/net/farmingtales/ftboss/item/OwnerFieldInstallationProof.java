package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

final class OwnerFieldInstallationProof {
    private static final String TIMER_PARENT = "writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V";
    private static final List<String> RANGED_PARENTS = List.of(
        "prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V",
        "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V"
    );
    private static final String OWNER_QUOTE = "quoteFoodBlessingHits(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V";
    private static final String OWNER_FINAL = "quoteFinalWeaponHit(Lnet/farmingtales/ftboss/event/NativeWeaponDamageEvent$BeforeMitigation;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;";
    private static final String OWNER_APPLY = "apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V";

    static boolean nativeComponentLambdaBody(MethodNode original, MethodNode actual, ClassNode source, ClassNode host) {
        return sameLambdaBody(original, actual, source, host);
    }

    private OwnerFieldInstallationProof() {
    }

    private static Object value(AnnotationNode var0, String var1) {
        if (var0 != null && var0.values != null) {
            for (byte var2 = 0; var2 + 1 < var0.values.size(); var2 += 2) {
                if (var1.equals(var0.values.get(var2))) {
                    return var0.values.get(var2 + 1);
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private static List<AnnotationNode> annotations(MethodNode var0) {
        ArrayList var1 = new ArrayList();
        if (var0.visibleAnnotations != null) {
            var1.addAll(var0.visibleAnnotations);
        }

        if (var0.invisibleAnnotations != null) {
            var1.addAll(var0.invisibleAnnotations);
        }

        return var1;
    }

    private static boolean merged(MethodNode var0, String var1) {
        return annotations(var0)
            .stream()
            .anyMatch(var1x -> var1x.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") && var1.equals(value(var1x, "mixin")));
    }

    private static String member(String var0) {
        int var1 = var0.indexOf("ftboss$");
        return var1 < 0 ? var0 : var0.substring(var1);
    }

    private static String owner(String var0, String var1, String var2) {
        return var0.equals(var1) ? var2 : var0;
    }

    private static String constant(Object var0, String var1, String var2) {
        if (var0 instanceof Handle var4) {
            return "handle:"
                + var4.getTag()
                + ":"
                + owner(var4.getOwner(), var1, var2)
                + ":"
                + member(var4.getName())
                + ":"
                + var4.getDesc()
                + ":"
                + var4.isInterface();
        } else if (var0 instanceof Type var3) {
            return "type:" + var3.getDescriptor().replace(var1, var2);
        } else {
            return var0 == null ? "null" : var0.getClass().getName() + ":" + var0;
        }
    }

    private static int position(MethodNode var0, AbstractInsnNode var1) {
        int var2 = 0;

        for (AbstractInsnNode var3 = var0.instructions.getFirst(); var3 != null; var3 = var3.getNext()) {
            if (var3 == var1) {
                return var2;
            }

            if (var3.getOpcode() >= 0) {
                var2++;
            }
        }

        throw new IllegalArgumentException("Foreign instruction label");
    }

    private static List<String> body(MethodNode var0, String var1, String var2) {
        if (!var1.equals(var2)) {
            MethodNode original = var0;
            var0 = new MethodNode(original.access, original.name, original.desc, original.signature, original.exceptions == null ? null : original.exceptions.toArray(String[]::new));
            original.accept(var0);
            for (AbstractInsnNode instruction : var0.instructions.toArray()) {
                if (instruction instanceof TypeInsnNode cast && cast.getOpcode() == 192 && cast.desc.equals(var2)) {
                    AbstractInsnNode previous = instruction.getPrevious();
                    while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
                    if (previous instanceof VarInsnNode load && load.getOpcode() == 25 && load.var == 0 && (original.access & 8) == 0) var0.instructions.remove(instruction);
                }
            }
        }
        ArrayList var3 = new ArrayList();

        for (AbstractInsnNode var5 : var0.instructions) {
            if (var5.getOpcode() >= 0) {
                String var6 = "";
                if (var5 instanceof FieldInsnNode var7) {
                    var6 = owner(var7.owner, var1, var2) + ":" + member(var7.name) + ":" + var7.desc;
                } else if (var5 instanceof MethodInsnNode var8) {
                    var6 = owner(var8.owner, var1, var2) + ":" + member(var8.name) + ":" + var8.desc + ":" + var8.itf;
                } else if (var5 instanceof TypeInsnNode var9) {
                    var6 = owner(var9.desc, var1, var2);
                } else if (var5 instanceof VarInsnNode var10) {
                    var6 = Integer.toString(var10.var);
                } else if (var5 instanceof IntInsnNode var11) {
                    var6 = Integer.toString(var11.operand);
                } else if (var5 instanceof IincInsnNode var12) {
                    var6 = var12.var + ":" + var12.incr;
                } else if (var5 instanceof LdcInsnNode var13) {
                    var6 = constant(var13.cst, var1, var2);
                } else if (var5 instanceof JumpInsnNode var14) {
                    var6 = Integer.toString(position(var0, var14.label));
                } else if (var5 instanceof MultiANewArrayInsnNode var15) {
                    var6 = var15.desc + ":" + var15.dims;
                } else if (var5 instanceof InvokeDynamicInsnNode var16) {
                    StringBuilder var28 = new StringBuilder(var16.name).append(':').append(var16.desc).append(':').append(constant(var16.bsm, var1, var2));

                    for (Object var23 : var16.bsmArgs) {
                        var28.append(':').append(constant(var23, var1, var2));
                    }

                    var6 = var28.toString();
                } else if (var5 instanceof TableSwitchInsnNode var17) {
                    StringBuilder var27 = new StringBuilder().append(var17.min).append(':').append(var17.max).append(':').append(position(var0, var17.dflt));

                    for (LabelNode var31 : var17.labels) {
                        var27.append(':').append(position(var0, var31));
                    }

                    var6 = var27.toString();
                } else if (var5 instanceof LookupSwitchInsnNode var18) {
                    StringBuilder var19 = new StringBuilder(var18.keys.toString()).append(':').append(position(var0, var18.dflt));

                    for (LabelNode var21 : var18.labels) {
                        var19.append(':').append(position(var0, var21));
                    }

                    var6 = var19.toString();
                }

                var3.add(var5.getOpcode() + ":" + var6);
            }
        }

        ArrayList var24 = new ArrayList();

        for (TryCatchBlockNode var26 : var0.tryCatchBlocks) {
            var24.add(position(var0, var26.start) + ":" + position(var0, var26.end) + ":" + position(var0, var26.handler) + ":" + var26.type);
        }

        var3.addAll(var24);
        return var3;
    }

    private static MethodNode proofClone(MethodNode original) {
        MethodNode copy = new MethodNode(original.access, original.name, original.desc, original.signature, original.exceptions == null ? null : original.exceptions.toArray(String[]::new));
        original.accept(copy);
        for (AbstractInsnNode instruction : copy.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.bsmArgs = dynamic.bsmArgs.clone();
        return copy;
    }

    private static MethodNode exactMethod(ClassNode owner, String name, String descriptor) {
        MethodNode found = null;
        for (MethodNode method : owner.methods) {
            if (method.name.equals(name) && method.desc.equals(descriptor)) {
                if (found != null) return null;
                found = method;
            }
        }
        return found;
    }

    private static boolean sameLambdaBody(MethodNode original, MethodNode woven, ClassNode source, ClassNode actual, List<String> active) {
        if (active.size() >= 8 || original.access != woven.access || !original.desc.equals(woven.desc)) return false;
        String pair = original.name + original.desc + ":" + woven.name + woven.desc;
        if (active.contains(pair)) return false;
        active.add(pair);
        try {
            MethodNode normalized = proofClone(woven);
            List<InvokeDynamicInsnNode> expected = new ArrayList<>();
            List<InvokeDynamicInsnNode> observed = new ArrayList<>();
            for (AbstractInsnNode instruction : original.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) expected.add(dynamic);
            for (AbstractInsnNode instruction : normalized.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) observed.add(dynamic);
            if (expected.size() != observed.size()) return false;
            for (int index = 0; index < expected.size(); index++) {
                InvokeDynamicInsnNode before = expected.get(index);
                InvokeDynamicInsnNode after = observed.get(index);
                if (!before.name.equals(after.name) || !before.desc.equals(after.desc) || before.bsmArgs.length != after.bsmArgs.length || !constant(before.bsm, source.name, actual.name).equals(constant(after.bsm, actual.name, actual.name))) return false;
                for (int argument = 0; argument < before.bsmArgs.length; argument++) {
                    Object beforeArgument = before.bsmArgs[argument];
                    Object afterArgument = after.bsmArgs[argument];
                    if (beforeArgument instanceof Handle originalHandle && originalHandle.getOwner().equals(source.name) && originalHandle.getName().startsWith("lambda$")) {
                        if (!before.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") || !before.bsm.getName().equals("metafactory") || before.bsm.getTag() != 6 || before.bsm.isInterface() || before.bsmArgs.length != 3 || argument != 1 || !(afterArgument instanceof Handle actualHandle) || !actualHandle.getOwner().equals(actual.name) || actualHandle.getTag() != originalHandle.getTag() || actualHandle.isInterface() != originalHandle.isInterface() || !actualHandle.getDesc().equals(originalHandle.getDesc())) return false;
                        String renamed = member(actualHandle.getName());
                        String prefix = "ftboss$" + originalHandle.getName() + "$";
                        if (!renamed.startsWith(prefix) || renamed.length() == prefix.length()) return false;
                        for (int suffix = prefix.length(); suffix < renamed.length(); suffix++) if (renamed.charAt(suffix) < '0' || renamed.charAt(suffix) > '9') return false;
                        MethodNode beforeLambda = exactMethod(source, originalHandle.getName(), originalHandle.getDesc());
                        MethodNode afterLambda = exactMethod(actual, actualHandle.getName(), actualHandle.getDesc());
                        if (beforeLambda == null || afterLambda == null || (beforeLambda.access & 4096) == 0 || (afterLambda.access & 4096) == 0 || !merged(afterLambda, source.name.replace('/', '.')) || !sameLambdaBody(beforeLambda, afterLambda, source, actual, active)) return false;
                        AnnotationNode origin = visibleAnnotation(afterLambda, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
                        if (!annotationKeys(origin, List.of("mixin", "priority", "sessionId")) || !(value(origin, "priority") instanceof Integer) || !(value(origin, "sessionId") instanceof String session) || session.isEmpty() || !session.equals(value(visibleAnnotation(woven, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "sessionId")) || !value(origin, "priority").equals(value(visibleAnnotation(woven, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "priority"))) return false;
                        after.bsmArgs[argument] = new Handle(actualHandle.getTag(), actual.name, originalHandle.getName(), actualHandle.getDesc(), actualHandle.isInterface());
                    } else if (!constant(beforeArgument, source.name, actual.name).equals(constant(afterArgument, actual.name, actual.name))) {
                        return false;
                    }
                }
            }
            return body(original, source.name, actual.name).equals(body(normalized, actual.name, actual.name));
        } finally {
            active.remove(active.size() - 1);
        }
    }

    private static boolean sameLambdaBody(MethodNode original, MethodNode woven, ClassNode source, ClassNode actual) {
        return sameLambdaBody(original, woven, source, actual, new ArrayList<>());
    }

    private static boolean matchesSelector(MethodNode var0, String var1) {
        int var2 = var1.indexOf(40);
        return var2 < 0 ? var0.name.equals(var1) : var0.name.equals(var1.substring(0, var2)) && var0.desc.equals(var1.substring(var2));
    }

    private static boolean call(AbstractInsnNode var0, MethodNode var1, String var2) {
        if (var0 instanceof MethodInsnNode var3 && var3.owner.equals(var2) && var3.name.equals(var1.name) && var3.desc.equals(var1.desc)) {
            return true;
        }

        return false;
    }

    private static boolean straightReturnOrder(MethodNode var0, MethodNode var1, String var2) {
        boolean var3 = false;
        boolean var4 = false;

        for (AbstractInsnNode var6 : var0.instructions) {
            if (var6.getOpcode() < 0) {
                if (var6.getType() == 8) {
                    var3 = false;
                }
            } else {
                if (call(var6, var1, var2)) {
                    var3 = true;
                }

                if (var6 instanceof JumpInsnNode || var6 instanceof LookupSwitchInsnNode || var6 instanceof TableSwitchInsnNode) {
                    var3 = false;
                }

                if (var6.getOpcode() >= 172 && var6.getOpcode() <= 177) {
                    var4 = true;
                    if (!var3) {
                        return false;
                    }

                    var3 = false;
                }
            }
        }

        return var4;
    }

    private static boolean straightHeadOrder(MethodNode var0, MethodNode var1, String var2) {
        for (AbstractInsnNode var4 : var0.instructions) {
            if (call(var4, var1, var2)) {
                return true;
            }

            if (var4 instanceof JumpInsnNode
                || var4 instanceof LookupSwitchInsnNode
                || var4 instanceof TableSwitchInsnNode
                || var4 instanceof FieldInsnNode
                || var4.getOpcode() == 191
                || var4.getOpcode() >= 172 && var4.getOpcode() <= 177) {
                return false;
            }

            if (var4 instanceof MethodInsnNode var5 && !var5.owner.startsWith("org/spongepowered/asm/mixin/injection/callback/")) {
                return false;
            }
        }

        return false;
    }

    private static boolean siteAndOrder(ClassNode var0, MethodNode var1, MethodNode var2) {
        for (AnnotationNode var4 : annotations(var1)) {
            if (var4.desc.equals("Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;")) {
                return false;
            }

            if (var4.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) {
                Object var5 = value(var4, "method");
                Object var6 = value(var4, "at");
                if (var5 instanceof List var7 && var6 instanceof List var8 && !var7.isEmpty() && var8.size() == 1 && var8.get(0) instanceof AnnotationNode var9
                    )
                 {
                    String var17 = String.valueOf(value(var9, "value"));
                    if (!var17.equals("HEAD") && !var17.equals("RETURN")) {
                        return false;
                    }

                    boolean var11 = false;
                    Iterator var12 = var7.iterator();

                    while (var12.hasNext()) {
                        if (!(var12.next() instanceof String var14)) {
                            return false;
                        }

                        for (MethodNode var16 : var0.methods) {
                            if (matchesSelector(var16, var14)) {
                                var11 = true;
                                if (var17.equals("HEAD") ? !straightHeadOrder(var16, var2, var0.name) : !straightReturnOrder(var16, var2, var0.name)) {
                                    return false;
                                }
                            }
                        }
                    }

                    return var11;
                }

                return false;
            }
        }

        return annotations(var1).stream().anyMatch(var0x -> var0x.desc.equals("Lorg/spongepowered/asm/mixin/Unique;"));
    }

    private static AnnotationNode visibleAnnotation(MethodNode var0, String var1) {
        List var2 = annotations(var0).stream().filter(var1x -> var1.equals(var1x.desc)).toList();
        return var2.size() == 1 && var0.visibleAnnotations != null && var0.visibleAnnotations.contains(var2.get(0)) ? (AnnotationNode)var2.get(0) : null;
    }

    private static boolean annotationKeys(AnnotationNode var0, List<String> var1) {
        if (var0 != null && var0.values != null && var0.values.size() % 2 == 0) {
            ArrayList var2 = new ArrayList();

            for (byte var3 = 0; var3 < var0.values.size(); var3 += 2) {
                if (!(var0.values.get(var3) instanceof String var4) || !var1.contains(var4) || var2.contains(var4)) {
                    return false;
                }

                var2.add(var4);
            }

            return true;
        } else {
            return false;
        }
    }

    private static boolean accessorFieldName(ClassNode var0, ClassNode var1, MethodNode var2, AnnotationNode var3, String var4) {
        Object var5 = value(var3, "value");
        Object var6 = value(var3, "remap");
        if (!(var5 instanceof String var7) || var7.isEmpty() || var6 != null && !(var6 instanceof Boolean)) {
            return false;
        }

        return var7.equals(var4)
            ? true
            : !Boolean.FALSE.equals(var6)
                && var1.name.equals("net/farmingtales/ftboss/mixin/OwnerFieldSourceAccessMixin")
                && var0.name.equals("net/minecraft/world/damagesource/DamageSource")
                && var2.name.equals("ftboss$ownerFieldDirectEntity")
                && var2.desc.equals("()Lnet/minecraft/world/entity/Entity;")
                && var7.equals("directEntity")
                && var4.equals("f_268595_");
    }

    static boolean nativeTridentHost(ClassNode host) {
        return host != null && host.name.equals("net/minecraft/world/entity/projectile/AbstractArrow");
    }

    private static String nativeTridentConstructor() {
        return "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V";
    }

    private static String nativeTridentCopy() {
        return "182:net/minecraft/world/item/ItemStack:copy:()Lnet/minecraft/world/item/ItemStack;:false";
    }

    static List<String> nativeTridentSnapshot(ClassNode host) {
        if (!nativeTridentHost(host)) return null;
        host = nativeChainEvidence(host);MethodNode parent = ownerParent(host, nativeTridentConstructor(), 4);
        if (parent == null || !parent.tryCatchBlocks.isEmpty() || !parent.exceptions.isEmpty() || nativeChainFrameLocals(host, parent) != parent.maxLocals || body(parent, host.name, host.name).stream().filter(nativeTridentCopy()::equals).count() != 2) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static boolean nativeTridentSource(ClassNode source, String name) {
        if (source == null || !source.name.equals("net/farmingtales/ftboss/mixin/" + name)) return false;
        List<AnnotationNode> markers = new ArrayList<>();if (source.visibleAnnotations != null) markers.addAll(source.visibleAnnotations);if (source.invisibleAnnotations != null) markers.addAll(source.invisibleAnnotations);
        List<AnnotationNode> targets = markers.stream().filter(marker -> marker.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).toList();
        return targets.size() == 1 && annotationKeys(targets.getFirst(), List.of("value", "remap")) && List.of(Type.getObjectType("net/minecraft/world/entity/projectile/AbstractArrow")).equals(value(targets.getFirst(), "value")) && (name.equals("NativeTridentFieldCarrierMixin") ? Boolean.FALSE.equals(value(targets.getFirst(), "remap")) : value(targets.getFirst(), "remap") == null);
    }

    static boolean nativeTridentWrapper(ClassNode host, ClassNode source, ClassNode fields, List<String> baseline) {
        if (!nativeTridentHost(host) || !nativeTridentSource(source, "OwnerTridentOriginalCopyMixin") || !nativeTridentSource(fields, "NativeTridentFieldCarrierMixin") || baseline == null || baseline.isEmpty() || !baseline.getFirst().startsWith("locals:")) return false;
        host = nativeChainEvidence(host);source = nativeChainEvidence(source);fields = nativeChainEvidence(fields);int locals;
        try {locals = Integer.parseInt(baseline.getFirst().substring(7));} catch (NumberFormatException failure) {return false;}
        if (locals < 12 || !host.interfaces.containsAll(source.interfaces) || !host.interfaces.contains(fields.name)) return false;
        String copyName = "ftboss$ownerTridentOriginalCopy", getterName = "ftboss$ownerTridentLineage", installerName = "ftboss$ownerInstallTridentLineage";
        MethodNode callback = nativeChainHandler(host, source, copyName, null);
        if (callback == null || callback.signature != null || !callback.exceptions.isEmpty() || callback.maxLocals != exactMethod(source, copyName, callback.desc).maxLocals || nativeChainFrameLocals(host, callback) != callback.maxLocals || !nativeChainReferences(host, callback, 1, 0) || !Integer.valueOf(1000).equals(value(visibleAnnotation(callback, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "priority"))) return false;
        MethodNode authentic = exactMethod(source, copyName, callback.desc);AnnotationNode wrap = visibleAnnotation(authentic, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        String signature = "(Lnet/minecraft/world/item/ItemStack;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation<Lnet/minecraft/world/item/ItemStack;>;)Lnet/minecraft/world/item/ItemStack;";
        if (!signature.equals(authentic.signature) || !annotationKeys(wrap, List.of("method", "at", "require")) || !List.of(nativeTridentConstructor()).equals(value(wrap, "method")) || !Integer.valueOf(1).equals(value(wrap, "require")) || !(value(wrap, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point) || !annotationKeys(point, List.of("value", "target", "ordinal")) || !"INVOKE".equals(value(point, "value")) || !"Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;".equals(value(point, "target")) || !Integer.valueOf(0).equals(value(point, "ordinal"))) return false;
        List<MethodNode> handlers = new ArrayList<>();handlers.add(callback);
        for (String name : List.of(getterName, installerName)) {
            String descriptor = OwnerFieldInstallationCatalogue.descriptor(source.name.replace('/', '.'), name);MethodNode original = exactMethod(source, name, descriptor), woven = exactMethod(host, name, descriptor);
            if (original == null || woven == null || original.access != 17 || woven.access != 17 || !original.exceptions.isEmpty() || !woven.exceptions.isEmpty() || original.signature != null || woven.signature != null || !mergedSession(woven, source.name.replace('/', '.'), callback) || !sameLambdaBody(original, woven, source, host) || woven.maxLocals != original.maxLocals || nativeChainFrameLocals(host, woven) != original.maxLocals || annotations(original).size() != 1 || !annotations(original).getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;")) return false;
            handlers.add(woven);
        }
        if (!nativePutField(host, source, "ftboss$ownerTridentLineage", "Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$TridentCarrierLineage;", handlers)) return false;
        String accessName = "ftboss$originalTridentStack", accessDescriptor = "()Lnet/minecraft/world/item/ItemStack;";MethodNode originalAccess = exactMethod(fields, accessName, accessDescriptor), actualAccess = exactMethod(host, accessName, accessDescriptor);
        if (originalAccess == null || actualAccess == null || !accessorGetter(new String[]{fields.name.replace('/', '.'),host.name.replace('/', '.'),accessName}, host, fields, originalAccess, actualAccess) || !mergedSession(actualAccess, fields.name.replace('/', '.'), callback)) return false;
        MethodNode parent = ownerParent(host, nativeTridentConstructor(), 4);
        if (parent == null || parent.maxLocals != locals + 1 || nativeChainFrameLocals(host, parent) != parent.maxLocals || !parent.tryCatchBlocks.isEmpty() || !parent.exceptions.isEmpty()) return false;
        List<AbstractInsnNode> instructions = executable(parent);List<String> actual = body(parent, host.name, host.name);int position = -1;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), callback, host.name)) {if (position >= 0) return false;position = index;}
        int first = position - 4;
        if (first < 0 || instructions.get(position).getOpcode() != 183 || ((MethodInsnNode)instructions.get(position)).itf || !actual.subList(first, first + 3).equals(List.of("58:" + locals, "25:0", "25:" + locals)) || !(instructions.get(first + 3) instanceof InvokeDynamicInsnNode dynamic) || nativeChainBridge(host, dynamic, new String[]{"net/minecraft/world/item/ItemStack"}, "Lnet/minecraft/world/item/ItemStack;", nativeTridentCopy(), false) == null) return false;
        for (int index = 0; index < instructions.size(); index++) if ((index < first || index > position) && (instructions.get(index) instanceof VarInsnNode variable && variable.var >= locals || instructions.get(index) instanceof IincInsnNode increment && increment.var >= locals)) return false;
        return baseline.equals(ownerShape(host, parent, List.of(new int[]{first, position}), List.of(nativeTridentCopy()), locals));
    }

    static boolean nativeTickSite(String[] row) {
        return row != null && List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerInventoryTickContinuityMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerInventoryTickCallback"));
    }

    static boolean nativeTickHost(ClassNode host) {
        return host != null && host.name.equals("net/minecraft/world/item/ItemStack");
    }

    private static String nativeTickSelector() {
        return "inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V";
    }

    private static String nativeTickOperation() {
        return "182:net/minecraft/world/item/Item:inventoryTick:(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V:false";
    }

    static List<String> nativeTickSnapshot(ClassNode host) {
        if (!nativeTickHost(host)) return null;
        host = nativeChainEvidence(host);MethodNode parent = ownerParent(host, nativeTickSelector(), 1);
        if (parent == null || parent.maxLocals != 5 || body(parent, host.name, host.name).stream().filter(nativeTickOperation()::equals).count() != 1 || !parent.tryCatchBlocks.isEmpty()) return null;
        return ownerShape(host, parent, List.of(), List.of(), 5);
    }

    private static boolean nativeTickAnnotation(MethodNode handler, boolean anchor) {
        AnnotationNode annotation = visibleAnnotation(handler, anchor ? "Lorg/spongepowered/asm/mixin/injection/Inject;" : "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, List.of("method", "at", "require")) || !List.of(nativeTickSelector()).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        String[] target = nativeTickOperation().split(":", -1);
        return anchor ? annotationKeys(point, List.of("value")) && "HEAD".equals(value(point, "value")) : annotationKeys(point, List.of("value", "target")) && "INVOKE".equals(value(point, "value")) && ("L" + target[1] + ";" + target[2] + target[3]).equals(value(point, "target"));
    }

    private static MethodNode nativeTickHandler(ClassNode host, ClassNode source, boolean anchor, MethodNode reference) {
        String name = anchor ? "ftboss$statueWear$anchor13" : "ftboss$ownerInventoryTickCallback", origin = source.name.replace('/', '.');
        String descriptor = anchor ? "(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V" : OwnerFieldInstallationCatalogue.descriptor(origin, name);
        MethodNode original = exactMethod(source, name, descriptor), woven = null;
        for (MethodNode candidate : host.methods) if (member(candidate.name).equals(name) && candidate.desc.equals(descriptor) && merged(candidate, origin)) {if (woven != null) return null;woven = candidate;}
        if (original == null || woven == null || original.access != 2 || woven.access != 2 || !original.exceptions.equals(anchor ? List.of() : List.of("java/lang/Throwable")) || !original.exceptions.equals(woven.exceptions) || !nativeTickAnnotation(original, anchor) || !sameLambdaBody(original, woven, source, host) || !mergedSession(woven, origin, reference == null ? woven : reference) || !Integer.valueOf(1000).equals(value(visibleAnnotation(woven, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "priority")) || !nativeChainReferences(host, woven, 1, 0) || woven.signature != null || woven.maxLocals != original.maxLocals || nativeChainFrameLocals(host, woven) != original.maxLocals) return null;
        String signature = anchor ? null : descriptor.replace("Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;", "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation<Ljava/lang/Void;>;");
        return java.util.Objects.equals(signature, original.signature) ? woven : null;
    }

    private static boolean nativeTickGuard(ClassNode host, boolean enter) {
        String descriptor = enter ? "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;" : "(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;Ljava/lang/Throwable;)V";
        MethodNode guard = exactMethod(host, enter ? "ftboss$statueWearEnterGuard" : "ftboss$statueWearExitGuard", descriptor);
        if (guard == null || guard.access != 4106 || guard.signature != null || !guard.exceptions.isEmpty() || !annotations(guard).isEmpty() || guard.maxLocals != (enter ? 4 : 2) || nativeChainFrameLocals(host, guard) != (enter ? 4 : 2) || guard.tryCatchBlocks.size() != 1) return false;
        TryCatchBlockNode handler = guard.tryCatchBlocks.getFirst();
        if (!"java/lang/Throwable".equals(handler.type) || position(guard, handler.start) != 0 || position(guard, handler.end) != (enter ? 5 : 3) || position(guard, handler.handler) != (enter ? 6 : 4)) return false;
        List<String> expected = new ArrayList<>(List.of("25:0", "25:1"));if (enter) expected.addAll(List.of("21:2", "21:3"));
        expected.add("184:net/farmingtales/ftboss/item/OwnerStatueWearAdmission:" + (enter ? "entered" : "exited") + ":" + descriptor + ":false");expected.addAll(enter ? List.of("176:", "87:", "1:", "176:") : List.of("177:", "87:", "177:"));
        expected.add(enter ? "0:5:6:java/lang/Throwable" : "0:3:4:java/lang/Throwable");
        return body(guard, host.name, host.name).equals(expected);
    }

    private static MethodNode nativeTickBridge(ClassNode host, InvokeDynamicInsnNode dynamic) {
        String descriptor = "([Ljava/lang/Object;)Ljava/lang/Void;";
        if (!dynamic.name.equals("call") || !dynamic.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;") || dynamic.bsm.getTag() != 6 || dynamic.bsm.isInterface() || !dynamic.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") || !dynamic.bsm.getName().equals("metafactory") || !dynamic.bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;") || dynamic.bsmArgs.length != 3 || !(dynamic.bsmArgs[0] instanceof Type erased) || !erased.getDescriptor().equals("([Ljava/lang/Object;)Ljava/lang/Object;") || !(dynamic.bsmArgs[1] instanceof Handle handle) || handle.getTag() != 6 || handle.isInterface() || !handle.getOwner().equals(host.name) || !handle.getDesc().equals(descriptor) || !(dynamic.bsmArgs[2] instanceof Type typed) || !typed.getDescriptor().equals(descriptor)) return null;
        MethodNode bridge = exactMethod(host, handle.getName(), descriptor);
        if (bridge == null || bridge.access != 4106 || bridge.signature != null || !bridge.exceptions.isEmpty() || !bridge.tryCatchBlocks.isEmpty() || !annotations(bridge).isEmpty() || bridge.maxLocals != 0 && bridge.maxLocals != 1 || nativeChainFrameLocals(host, bridge) != 1 || !nativeChainReferences(host, bridge, 0, 1)) return null;
        String[] arguments = {"net/minecraft/world/item/Item", "net/minecraft/world/item/ItemStack", "net/minecraft/world/level/Level", "net/minecraft/world/entity/Entity", "int", "boolean"};
        List<String> expected = new ArrayList<>(List.of("25:0", "16:6", "18:java.lang.String:[net.minecraft.world.item.Item, net.minecraft.world.item.ItemStack, net.minecraft.world.level.Level, net.minecraft.world.entity.Entity, int, boolean]", "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false", "25:0"));
        for (int index = 0; index < arguments.length; index++) {
            String argument = arguments[index], boxed = argument.equals("int") ? "java/lang/Integer" : argument.equals("boolean") ? "java/lang/Boolean" : argument;
            expected.addAll(List.of("89:", "16:" + index, "50:", "192:" + boxed));
            if (argument.equals("int")) expected.add("182:java/lang/Integer:intValue:()I:false");
            if (argument.equals("boolean")) expected.add("182:java/lang/Boolean:booleanValue:()Z:false");
            expected.add("95:");
        }
        expected.addAll(List.of("87:", nativeTickOperation(), "1:", "192:java/lang/Void", "176:"));
        return body(bridge, host.name, host.name).equals(expected) ? bridge : null;
    }

    static boolean nativeTickWrapper(ClassNode host, ClassNode source, ClassNode wear, List<String> baseline) {
        if (!nativeTickHost(host) || source == null || wear == null || !source.name.equals("net/farmingtales/ftboss/mixin/OwnerInventoryTickContinuityMixin") || !wear.name.equals("net/farmingtales/ftboss/mixin/OwnerStatueWearItemStackMixin") || baseline == null || baseline.isEmpty() || !baseline.getFirst().equals("locals:5")) return false;
        host = nativeChainEvidence(host);source = nativeChainEvidence(source);wear = nativeChainEvidence(wear);
        MethodNode handler = nativeTickHandler(host, source, false, null), anchor = handler == null ? null : nativeTickHandler(host, wear, true, handler);
        if (handler == null || anchor == null || !nativeTickGuard(host, true) || !nativeTickGuard(host, false)) return false;
        MethodNode parent = ownerParent(host, nativeTickSelector(), 1);
        if (parent == null || parent.maxLocals != 13 || nativeChainFrameLocals(host, parent) != 13 || parent.tryCatchBlocks.size() != 1) return false;
        List<AbstractInsnNode> instructions = executable(parent);List<String> actual = body(parent, host.name, host.name);int length = instructions.size();
        String enter = "184:" + host.name + ":ftboss$statueWearEnterGuard:(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;II)Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;:false", exit = "184:" + host.name + ":ftboss$statueWearExitGuard:(Lnet/farmingtales/ftboss/item/OwnerStatueWearAdmission$Frame;Ljava/lang/Throwable;)V:false";
        if (length < 40 || !actual.subList(0, 9).equals(List.of("25:0", "25:2", "16:13", "3:", enter, "58:11", "25:0", "1:", "183:" + host.name + ":" + member(anchor.name) + ":" + anchor.desc + ":false")) || !actual.subList(length - 10, length).equals(List.of("25:11", "1:", exit, "177:", "58:12", "25:11", "25:12", exit, "25:12", "191:"))) return false;
        TryCatchBlockNode outer = parent.tryCatchBlocks.getFirst();
        if (!"java/lang/Throwable".equals(outer.type) || position(parent, outer.start) != 6 || position(parent, outer.end) != length - 6 || position(parent, outer.handler) != length - 6) return false;
        int position = -1;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) position = index;
        List<String> prefix = List.of("54:10", "54:9", "58:8", "58:7", "58:6", "58:5", "25:0", "25:5", "25:6", "25:7", "25:8", "21:9", "21:10");int first = position - 14;
        if (first < 9 || instructions.get(position).getOpcode() != 183 || ((MethodInsnNode)instructions.get(position)).itf || !actual.subList(first, position - 1).equals(prefix) || !(instructions.get(position - 1) instanceof InvokeDynamicInsnNode dynamic) || nativeTickBridge(host, dynamic) == null) return false;
        MethodNode restored = proofClone(parent);List<AbstractInsnNode> saved = executable(restored);restored.tryCatchBlocks.clear();
        restored.instructions.insertBefore(saved.get(first), new MethodInsnNode(182, "net/minecraft/world/item/Item", "inventoryTick", "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V", false));
        for (int index = first; index <= position; index++) restored.instructions.remove(saved.get(index));
        for (int index = 0; index < 9; index++) restored.instructions.remove(saved.get(index));
        for (int index = length - 10; index < length; index++) if (index != length - 7) restored.instructions.remove(saved.get(index));
        restored.maxLocals = 5;
        return nativeChainFrameLocals(host, restored) == 5 && baseline.equals(ownerShape(host, restored, List.of(), List.of(), 5));
    }

    static int nativePutHost(ClassNode host) {
        if (host == null) return -1;
        return host.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf") ? 0 : host.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan$OutcomeTagLeaf") ? 1 : -1;
    }

    static int nativePutSlot(String[] row) {
        if (row == null || row.length < 3) return -1;
        int family = row[1].equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf") ? 0 : row[1].equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf") ? 1 : -1;
        if (family < 0) return -1;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin", row[1], "ftboss$ownerNativePutQuote", "ftboss$ownerNativeOriginalValue", "ftboss$ownerNativePutQuoted"))) return family * 2;
        return List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin", row[1], "ftboss$ownerNativeCopy", "ftboss$ownerNativePut")) ? family * 2 + 1 : -1;
    }

    private static String nativePutConstructor() {
        return "<init>(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V";
    }

    private static String nativePutOperation(boolean copy) {
        return copy ? "185:net/minecraft/nbt/Tag:copy:()Lnet/minecraft/nbt/Tag;:true" : "182:net/minecraft/nbt/CompoundTag:put:(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;:false";
    }

    static List<String> nativePutSnapshot(ClassNode host, boolean constructor) {
        if (nativePutHost(host) < 0) return null;
        host = nativeChainEvidence(host);
        MethodNode parent = ownerParent(host, constructor ? nativePutConstructor() : OWNER_APPLY, constructor ? 2 : 0);
        if (parent == null || parent.maxLocals < (constructor ? 5 : 3)) return null;
        List<String> instructions = body(parent, host.name, host.name);
        if (instructions.stream().filter(nativePutOperation(true)::equals).count() != 1 || !constructor && instructions.stream().filter(nativePutOperation(false)::equals).count() != 1 || instructions.stream().anyMatch(value -> value.contains("ftboss$ownerNativePut") || value.contains("ftboss$ownerNativeCopy"))) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static boolean nativePutAnnotation(MethodNode handler, int kind) {
        if (kind < 2) {
            List<AnnotationNode> markers = annotations(handler);
            return markers.size() == 1 && markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;") && (markers.getFirst().values == null || markers.getFirst().values.isEmpty());
        }
        AnnotationNode annotation = visibleAnnotation(handler, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, List.of("method", "at", "require")) || !List.of(kind == 2 ? "<init>" : "apply").equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        String[] target = nativePutOperation(kind != 4).split(":", -1);
        return annotationKeys(point, List.of("value", "target", "remap")) && "INVOKE".equals(value(point, "value")) && ("L" + target[1] + ";" + target[2] + target[3]).equals(value(point, "target")) && Boolean.TRUE.equals(value(point, "remap"));
    }

    private static MethodNode nativePutHandler(ClassNode host, ClassNode source, String name, int kind, MethodNode anchor) {
        String origin = source.name.replace('/', '.'), descriptor = OwnerFieldInstallationCatalogue.descriptor(origin, name);
        MethodNode original = exactMethod(source, name, descriptor), woven = null;
        for (MethodNode method : host.methods) if (member(method.name).equals(name) && method.desc.equals(descriptor) && merged(method, origin)) {if (woven != null) return null;woven = method;}
        if (original == null || woven == null || original.access != (kind < 2 ? 17 : 2) || woven.access != original.access || !original.exceptions.equals(kind > 2 ? List.of("java/lang/Throwable") : List.of()) || !original.exceptions.equals(woven.exceptions) || !nativePutAnnotation(original, kind) || !mergedSession(woven, origin, anchor == null ? woven : anchor) || !Integer.valueOf(1000).equals(value(visibleAnnotation(woven, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "priority")) || !sameLambdaBody(original, woven, source, host) || woven.maxLocals != original.maxLocals || nativeChainFrameLocals(host, woven) != original.maxLocals || woven.signature != null) return null;
        String signature = kind < 2 ? null : descriptor.replace("Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;", "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation<Lnet/minecraft/nbt/Tag;>;");
        if (!java.util.Objects.equals(signature, original.signature)) return null;
        if (kind < 2) {
            String field = kind == 0 ? "ftboss$ownerNativePutQuote:Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$PutQuote;" : "value:Lnet/minecraft/nbt/Tag;";
            if (!body(woven, host.name, host.name).equals(List.of("25:0", "180:" + host.name + ":" + field, "176:"))) return null;
        }
        for (int index = 0; index < Type.getArgumentTypes(descriptor).length; index++) {
            List<AnnotationNode> markers = new ArrayList<>();
            if (original.visibleParameterAnnotations != null && original.visibleParameterAnnotations[index] != null) markers.addAll(original.visibleParameterAnnotations[index]);
            if (original.invisibleParameterAnnotations != null && original.invisibleParameterAnnotations[index] != null) markers.addAll(original.invisibleParameterAnnotations[index]);
            if (kind == 3 && index >= 2 ? markers.size() != 1 || !markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/injection/Coerce;") || markers.getFirst().values != null && !markers.getFirst().values.isEmpty() : !markers.isEmpty()) return null;
        }
        return woven;
    }

    private static boolean nativePutField(ClassNode host, ClassNode source, String name, String descriptor, List<MethodNode> handlers) {
        List<FieldNode> originals = source.fields.stream().filter(field -> field.name.equals(name)).toList(), actual = host.fields.stream().filter(field -> field.name.equals(name)).toList();
        if (originals.size() != 1 || actual.size() != 1 || originals.getFirst().access != 2 || actual.getFirst().access != 2 || !originals.getFirst().desc.equals(descriptor) || !actual.getFirst().desc.equals(descriptor)) return false;
        FieldNode original = originals.getFirst();List<AnnotationNode> markers = new ArrayList<>();if (original.visibleAnnotations != null) markers.addAll(original.visibleAnnotations);if (original.invisibleAnnotations != null) markers.addAll(original.invisibleAnnotations);
        if (markers.size() != 1 || !markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;") || markers.getFirst().values != null && !markers.getFirst().values.isEmpty()) return false;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof FieldInsnNode field && field.owner.equals(host.name) && field.name.equals(name) && (!field.desc.equals(descriptor) || !handlers.contains(method))) return false;
        return true;
    }

    private static boolean nativePutParent(ClassNode host, MethodNode[] handlers, List<String> baseline, boolean constructor) {
        MethodNode parent = ownerParent(host, constructor ? nativePutConstructor() : OWNER_APPLY, constructor ? 2 : 0);int locals;
        try {locals = Integer.parseInt(baseline.getFirst().substring(7));} catch (NumberFormatException | IndexOutOfBoundsException failure) {return false;}
        if (parent == null || locals < (constructor ? 5 : 3) || parent.maxLocals != locals + (constructor ? 1 : 3) || nativeChainFrameLocals(host, parent) != parent.maxLocals) return false;
        List<AbstractInsnNode> instructions = executable(parent);List<String> actual = body(parent, host.name, host.name);List<int[]> windows = new ArrayList<>();List<String> operations = new ArrayList<>();
        for (int operation = 0; operation < (constructor ? 1 : 2); operation++) {
            MethodNode handler = handlers[constructor ? 2 : operation + 3];int position = -1;
            if (!nativeChainReferences(host, handler, 1, 0)) return false;
            for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) position = index;
            boolean copy = operation == 0;List<String> prefix = copy ? List.of("58:" + locals, "25:0", "25:" + locals) : List.of("58:" + (locals + 2), "58:" + (locals + 1), "58:" + locals, "25:0", "25:" + locals, "25:" + (locals + 1), "25:" + (locals + 2));
            List<String> suffix = constructor ? List.of("25:1", "25:2", "25:3", "25:4") : copy ? List.of("25:1", "25:2") : List.of();int first = position - prefix.size() - suffix.size() - 1;
            if (first < 0 || instructions.get(position).getOpcode() != 183 || ((MethodInsnNode)instructions.get(position)).itf || !actual.subList(first, first + prefix.size()).equals(prefix) || !(instructions.get(first + prefix.size()) instanceof InvokeDynamicInsnNode dynamic) || !actual.subList(first + prefix.size() + 1, position).equals(suffix) || nativeChainBridge(host, dynamic, copy ? new String[]{"net/minecraft/nbt/Tag"} : new String[]{"net/minecraft/nbt/CompoundTag", "java/lang/String", "net/minecraft/nbt/Tag"}, "Lnet/minecraft/nbt/Tag;", nativePutOperation(copy), false) == null) return false;
            windows.add(new int[]{first, position});operations.add(nativePutOperation(copy));
        }
        for (int index = 0; index < instructions.size(); index++) {
            boolean inside = false;for (int[] window : windows) if (index >= window[0] && index <= window[1]) inside = true;
            if (!inside && (instructions.get(index) instanceof VarInsnNode variable && variable.var >= locals || instructions.get(index) instanceof IincInsnNode increment && increment.var >= locals)) return false;
        }
        return baseline.equals(ownerShape(host, parent, windows, operations, locals));
    }

    static boolean nativePutWrapper(ClassNode host, ClassNode quote, ClassNode consumer, List<List<String>> baselines) {
        if (nativePutHost(host) < 0 || quote == null || consumer == null || !quote.name.equals("net/farmingtales/ftboss/mixin/OwnerNativePutQuoteMixin") || !consumer.name.equals("net/farmingtales/ftboss/mixin/OwnerNativePutConsumerMixin") || baselines == null || baselines.size() != 2 || baselines.stream().anyMatch(value -> value == null || value.isEmpty() || !value.getFirst().startsWith("locals:")) || !host.interfaces.containsAll(quote.interfaces)) return false;
        host = nativeChainEvidence(host);quote = nativeChainEvidence(quote);consumer = nativeChainEvidence(consumer);
        MethodNode[] handlers = new MethodNode[5];String[] names = {"ftboss$ownerNativePutQuote", "ftboss$ownerNativeOriginalValue", "ftboss$ownerNativePutQuoted", "ftboss$ownerNativeCopy", "ftboss$ownerNativePut"};
        for (int index = 0; index < names.length; index++) {handlers[index] = nativePutHandler(host, index < 3 ? quote : consumer, names[index], index, index == 0 ? null : handlers[0]);if (handlers[index] == null) return false;}
        List<FieldNode> values = host.fields.stream().filter(field -> field.name.equals("value")).toList(), shadows = quote.fields.stream().filter(field -> field.name.equals("value")).toList();
        if (values.size() != 1 || values.getFirst().access != 18 || !values.getFirst().desc.equals("Lnet/minecraft/nbt/Tag;") || shadows.size() != 1 || shadows.getFirst().access != 2 || !shadows.getFirst().desc.equals("Lnet/minecraft/nbt/Tag;")) return false;
        List<AnnotationNode> shadowMarkers = new ArrayList<>();if (shadows.getFirst().visibleAnnotations != null) shadowMarkers.addAll(shadows.getFirst().visibleAnnotations);if (shadows.getFirst().invisibleAnnotations != null) shadowMarkers.addAll(shadows.getFirst().invisibleAnnotations);
        if (shadowMarkers.size() != 2 || shadowMarkers.stream().filter(marker -> marker.desc.equals("Lorg/spongepowered/asm/mixin/Final;") && (marker.values == null || marker.values.isEmpty())).count() != 1 || shadowMarkers.stream().filter(marker -> marker.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;") && annotationKeys(marker, List.of("remap")) && Boolean.FALSE.equals(value(marker, "remap"))).count() != 1) return false;
        return nativePutField(host, quote, "ftboss$ownerNativePutQuote", "Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$PutQuote;", List.of(handlers[0], handlers[2])) && nativePutField(host, consumer, "ftboss$ownerNativeAdmission", "Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$Admission;", List.of(handlers[3], handlers[4])) && nativePutParent(host, handlers, baselines.getFirst(), true) && nativePutParent(host, handlers, baselines.get(1), false);
    }

    static boolean nativeCaptureSite(String[] row) {
        return row != null && List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedCaptureBoundaryMixin", "net.farmingtales.ftboss.item.FoodWeaponUseBridge", "ftboss$originalCapture"));
    }

    static boolean nativeCaptureHost(ClassNode host) {
        return host != null && host.name.equals("net/farmingtales/ftboss/item/FoodWeaponUseBridge");
    }

    private static String nativeCaptureSelector() {
        return "invoke(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;";
    }

    private static String nativeCaptureOperation() {
        return "184:net/farmingtales/ftboss/item/FoodWeaponRanged:captureBound:(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/nbt/CompoundTag;:false";
    }

    static List<String> nativeCaptureSnapshot(ClassNode host) {
        if (!nativeCaptureHost(host)) return null;
        host = nativeChainEvidence(host);
        MethodNode parent = ownerParent(host, nativeCaptureSelector(), 10);
        if (parent == null || parent.maxLocals != 9 || body(parent, host.name, host.name).stream().filter(nativeCaptureOperation()::equals).count() != 1) return null;
        return ownerShape(host, parent, List.of(), List.of(), 9);
    }

    private static boolean nativeCaptureAnnotation(MethodNode original) {
        AnnotationNode annotation = visibleAnnotation(original, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, List.of("method", "at", "require", "expect", "allow", "remap")) || !List.of(nativeCaptureSelector()).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !Integer.valueOf(1).equals(value(annotation, "expect")) || !Integer.valueOf(1).equals(value(annotation, "allow")) || !Boolean.FALSE.equals(value(annotation, "remap")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        String[] target = nativeCaptureOperation().split(":", -1);
        return annotationKeys(point, List.of("value", "target", "remap")) && "INVOKE".equals(value(point, "value")) && ("L" + target[1] + ";" + target[2] + target[3]).equals(value(point, "target")) && Boolean.FALSE.equals(value(point, "remap"));
    }

    static boolean nativeCaptureWrapper(ClassNode host, ClassNode source, List<String> baseline) {
        String origin = "net.farmingtales.ftboss.mixin.NativeExpectedCaptureBoundaryMixin", name = "ftboss$originalCapture";
        if (!nativeCaptureHost(host) || source == null || !source.name.replace('/', '.').equals(origin) || baseline == null || baseline.isEmpty() || !baseline.getFirst().equals("locals:9") || !host.interfaces.containsAll(source.interfaces)) return false;
        host = nativeChainEvidence(host);source = nativeChainEvidence(source);
        String descriptor = OwnerFieldInstallationCatalogue.descriptor(origin, name);
        MethodNode original = exactMethod(source, name, descriptor), handler = null;
        for (MethodNode candidate : host.methods) if (member(candidate.name).equals(name) && candidate.desc.equals(descriptor) && merged(candidate, origin)) {if (handler != null) return false;handler = candidate;}
        if (original == null || handler == null || original.access != 10 || handler.access != 10 || !original.exceptions.isEmpty() || !handler.exceptions.isEmpty() || !nativeCaptureAnnotation(original) || handler.signature != null || !descriptor.replace("Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;", "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation<Lnet/minecraft/nbt/CompoundTag;>;").equals(original.signature) || !Integer.valueOf(1000).equals(value(visibleAnnotation(handler, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"), "priority")) || handler.maxLocals != original.maxLocals || nativeChainFrameLocals(host, handler) != original.maxLocals || !sameLambdaBody(original, handler, source, host) || !mergedSession(handler, origin, handler) || !nativeChainReferences(host, handler, 1, 0)) return false;
        MethodNode parent = ownerParent(host, nativeCaptureSelector(), 10);
        if (parent == null || parent.maxLocals != 13 || nativeChainFrameLocals(host, parent) != 13) return false;
        List<AbstractInsnNode> instructions = executable(parent);List<String> actual = body(parent, host.name, host.name);int position = -1;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) {if (position >= 0) return false;position = index;}
        List<String> prefix = List.of("58:12", "58:11", "58:10", "58:9", "25:9", "25:10", "25:11", "25:12");int first = position - prefix.size() - 1;
        if (first < 0 || instructions.get(position).getOpcode() != 184 || ((MethodInsnNode)instructions.get(position)).itf || !actual.subList(first, first + prefix.size()).equals(prefix) || !(instructions.get(position - 1) instanceof InvokeDynamicInsnNode operation) || nativeChainBridge(host, operation, new String[]{"net/minecraft/server/level/ServerPlayer", "net/minecraft/world/item/ItemStack", "net/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind", "net/minecraft/world/InteractionHand"}, "Lnet/minecraft/nbt/CompoundTag;", nativeCaptureOperation(), false) == null) return false;
        for (int index = 0; index < instructions.size(); index++) if (index < first || index > position) {
            AbstractInsnNode instruction = instructions.get(index);
            if (instruction instanceof VarInsnNode variable && variable.var >= 9 || instruction instanceof IincInsnNode increment && increment.var >= 9) return false;
        }
        return baseline.equals(ownerShape(host, parent, List.of(new int[]{first, position}), List.of(nativeCaptureOperation()), 9));
    }

    static int nativeChainSlot(String[] row) {
        if (row == null) return -1;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerSnapshotLeaseCopyMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerSnapshotCopy"))) return 0;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerOriginalRepairContinuationMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$RepairLeaf", "ftboss$ownerRepairGetter", "ftboss$ownerRepairSetter"))) return 1;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedRangedScopeMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$originalRangedScope"))) return 2;
        return List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedTimerScopeMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$originalTimerScope")) ? 3 : -1;
    }

    static int nativeChainHost(ClassNode host) {
        if (host == null) return -1;
        return switch (host.name) {
            case "net/minecraft/nbt/CompoundTag" -> 0;
            case "net/farmingtales/ftboss/item/FoodWeaponHitPlan$RepairLeaf" -> 1;
            case "net/farmingtales/ftboss/item/FoodWeaponHitPlan$TimerSaveLeaf" -> 2;
            default -> -1;
        };
    }

    static String nativeChainSource(int slot) {
        return "net.farmingtales.ftboss.mixin." + switch (slot) {
            case 0 -> "OwnerSnapshotLeaseCopyMixin";
            case 1 -> "OwnerOriginalRepairContinuationMixin";
            case 2 -> "NativeExpectedRangedScopeMixin";
            case 3 -> "NativeExpectedTimerScopeMixin";
            default -> "";
        };
    }

    private static String nativeChainOperation(int family, int operation) {
        if (family == 1) return operation == 0 ? "182:net/minecraft/world/item/ItemStack:getDamageValue:()I:false" : "182:net/minecraft/world/item/ItemStack:setDamageValue:(I)V:false";
        return operation == 0 ? "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:writePlanHitTimer:(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V:false" : "184:net/farmingtales/ftboss/item/FoodWeaponRanged:saveHitTimers:(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V:false";
    }

    static List<String> nativeChainSnapshot(ClassNode host) {
        int family = nativeChainHost(host);
        if (family < 0) return null;
        MethodNode parent = ownerParent(host, family == 0 ? "copy()Lnet/minecraft/nbt/CompoundTag;" : OWNER_APPLY, family == 0 ? 1 : 0);
        if (parent == null || parent.maxLocals < (family == 0 ? 1 : 3)) return null;
        List<String> instructions = body(parent, host.name, host.name);
        if (family == 0 ? instructions.stream().anyMatch(value -> value.contains("ftboss$ownerSnapshotCopy") || value.contains("mixinextras$wrapped")) : instructions.stream().filter(nativeChainOperation(family, 0)::equals).count() != 1 || instructions.stream().filter(nativeChainOperation(family, 1)::equals).count() != 1) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static MethodNode nativeChainHandler(ClassNode host, ClassNode source, String name, MethodNode anchor) {
        if (source == null) return null;
        String origin = source.name.replace('/', '.');String descriptor = OwnerFieldInstallationCatalogue.descriptor(origin, name);
        MethodNode original = exactMethod(source, name, descriptor), woven = null;
        for (MethodNode method : host.methods) if (member(method.name).equals(name) && method.desc.equals(descriptor) && merged(method, origin)) {if (woven != null) return null;woven = method;}
        if (original == null || woven == null || original.access != 2 || woven.access != 2 || !mergedSession(woven, origin, anchor == null ? woven : anchor) || !sameLambdaBody(original, woven, source, host) || !original.exceptions.equals(woven.exceptions)) return null;
        return woven;
    }

    private static boolean nativeChainAnnotation(MethodNode original, int family, int operation) {
        if (family == 0) {
            AnnotationNode annotation = visibleAnnotation(original, "Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;");
            return annotationKeys(annotation, List.of("method")) && List.of("copy()Lnet/minecraft/nbt/CompoundTag;").equals(value(annotation, "method"));
        }
        AnnotationNode annotation = visibleAnnotation(original, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, family == 1 ? List.of("method", "at", "require") : List.of("method", "at", "require", "expect", "allow", "remap")) || !List.of(family == 1 ? "apply" : OWNER_APPLY).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        if (family == 2 && (!Integer.valueOf(1).equals(value(annotation, "expect")) || !Integer.valueOf(1).equals(value(annotation, "allow")) || !Boolean.FALSE.equals(value(annotation, "remap")))) return false;
        String[] target = nativeChainOperation(family, operation).split(":", -1);
        return annotationKeys(point, List.of("value", "target", "remap")) && "INVOKE".equals(value(point, "value")) && ("L" + target[1] + ";" + target[2] + target[3]).equals(value(point, "target")) && Boolean.valueOf(family == 1).equals(value(point, "remap"));
    }

    private static boolean nativeChainReferences(ClassNode host, MethodNode target, int calls, int handles) {
        int actualCalls = 0, actualHandles = 0;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode invocation && invocation.owner.equals(host.name) && invocation.name.equals(target.name) && invocation.desc.equals(target.desc)) actualCalls++;
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Handle handle && handle.getOwner().equals(host.name) && handle.getName().equals(target.name)) return false;
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                if (dynamic.bsm.getOwner().equals(host.name) && dynamic.bsm.getName().equals(target.name)) return false;
                for (Object argument : dynamic.bsmArgs) if (argument instanceof Handle handle && handle.getOwner().equals(host.name) && handle.getName().equals(target.name)) {if (!handle.getDesc().equals(target.desc)) return false;actualHandles++;}
            }
        }
        return actualCalls == calls && actualHandles == handles;
    }

    private static int nativeChainFrameLocals(ClassNode host, MethodNode method) {
        try {
            int locals = (method.access & 8) == 0 ? 1 : 0;
            for (Type argument : Type.getArgumentTypes(method.desc)) locals += argument.getSize();
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof VarInsnNode variable) locals = Math.max(locals, variable.var + (variable.getOpcode() == 22 || variable.getOpcode() == 24 || variable.getOpcode() == 55 || variable.getOpcode() == 57 ? 2 : 1));
                if (instruction instanceof IincInsnNode increment) locals = Math.max(locals, increment.var + 1);
            }
            MethodNode copy = proofClone(method);copy.maxLocals = locals;copy.maxStack = Math.max(8, executable(copy).size() * 2 + 8);
            new org.objectweb.asm.tree.analysis.Analyzer<>(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(host.name, copy);
            return locals;
        } catch (RuntimeException | org.objectweb.asm.tree.analysis.AnalyzerException failure) {
            return -1;
        }
    }

    private static MethodNode nativeChainBridge(ClassNode host, InvokeDynamicInsnNode dynamic, String[] arguments, String result, String operation, boolean captured) {
        String descriptor = "([Ljava/lang/Object;)" + result;
        if (!dynamic.name.equals("call") || !dynamic.desc.equals(captured ? "(L" + host.name + ";)Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;" : "()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;") || dynamic.bsm.getTag() != 6 || dynamic.bsm.isInterface() || !dynamic.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") || !dynamic.bsm.getName().equals("metafactory") || !dynamic.bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;") || dynamic.bsmArgs.length != 3 || !(dynamic.bsmArgs[0] instanceof Type erased) || !erased.getDescriptor().equals("([Ljava/lang/Object;)Ljava/lang/Object;") || !(dynamic.bsmArgs[1] instanceof Handle handle) || handle.getTag() != (captured ? 7 : 6) || handle.isInterface() || !handle.getOwner().equals(host.name) || !handle.getDesc().equals(descriptor) || !(dynamic.bsmArgs[2] instanceof Type typed) || !typed.getDescriptor().equals(descriptor)) return null;
        MethodNode bridge = exactMethod(host, handle.getName(), descriptor);
        if (bridge == null || bridge.access != (captured ? 4098 : 4106) || bridge.signature != null || !bridge.exceptions.isEmpty() || !bridge.tryCatchBlocks.isEmpty() || !annotations(bridge).isEmpty() || bridge.maxLocals != 0 && bridge.maxLocals != (captured ? 2 : 1) || nativeChainFrameLocals(host, bridge) != (captured ? 2 : 1) || !nativeChainReferences(host, bridge, 0, 1)) return null;
        List<String> types = new ArrayList<>();for (String argument : arguments) types.add(argument.replace('/', '.'));
        List<String> expected = new ArrayList<>(List.of("25:" + (captured ? 1 : 0), "16:" + arguments.length, "18:java.lang.String:" + types, "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false"));
        if (captured) expected.addAll(List.of("25:0", "25:1", "87:"));
        else {
            expected.add("25:0");
            for (int index = 0; index < arguments.length; index++) {
                expected.addAll(List.of("89:", "16:" + index, "50:", "192:" + (arguments[index].equals("int") ? "java/lang/Integer" : arguments[index])));
                if (arguments[index].equals("int")) expected.add("182:java/lang/Integer:intValue:()I:false");
                expected.add("95:");
            }
            expected.add("87:");
        }
        expected.add(operation);
        if (result.equals("Ljava/lang/Void;")) expected.addAll(List.of("1:", "192:java/lang/Void"));
        if (result.equals("Ljava/lang/Integer;")) expected.add("184:java/lang/Integer:valueOf:(I)Ljava/lang/Integer;:false");
        expected.add("176:");
        return body(bridge, host.name, host.name).equals(expected) ? bridge : null;
    }

    private static boolean nativeSnapshotWrapper(ClassNode host, ClassNode source, List<String> baseline, MethodNode handler) {
        MethodNode parent = ownerParent(host, "copy()Lnet/minecraft/nbt/CompoundTag;", 1);
        if (parent == null || parent.maxLocals != 1 && !baseline.getFirst().equals("locals:" + parent.maxLocals) || nativeChainFrameLocals(host, parent) != 1 || !parent.tryCatchBlocks.isEmpty() || !parent.exceptions.isEmpty() || !handler.exceptions.isEmpty() || !nativeChainReferences(host, handler, 1, 0)) return false;
        List<AbstractInsnNode> instructions = executable(parent);
        if (instructions.size() != 5 || !(instructions.get(2) instanceof InvokeDynamicInsnNode dynamic) || !body(parent, host.name, host.name).subList(0, 2).equals(List.of("25:0", "25:0")) || !call(instructions.get(3), handler, host.name) || instructions.get(3).getOpcode() != 183 || instructions.get(4).getOpcode() != 176) return false;
        if (!(dynamic.bsmArgs.length == 3 && dynamic.bsmArgs[1] instanceof Handle handle)) return false;
        MethodNode bridge = exactMethod(host, handle.getName(), handle.getDesc());
        if (bridge == null) return false;
        List<AbstractInsnNode> bridgeInstructions = executable(bridge);
        if (bridgeInstructions.size() != 9 || !(bridgeInstructions.get(7) instanceof MethodInsnNode originalCall) || originalCall.getOpcode() != 183 || originalCall.itf || !originalCall.owner.equals(host.name) || !originalCall.desc.equals("()Lnet/minecraft/nbt/CompoundTag;")) return false;
        MethodNode original = exactMethod(host, originalCall.name, originalCall.desc);
        if (original == null || original.access != 2 || original.signature != null || !original.exceptions.isEmpty() || !annotations(original).isEmpty() || !original.name.startsWith("copy$mixinextras$wrapped$") || !nativeChainReferences(host, original, 1, 0) || nativeChainFrameLocals(host, original) < 1 || original.maxLocals != 0 && original.maxLocals != nativeChainFrameLocals(host, original) || !baseline.equals(ownerShape(host, original, List.of(), List.of(), nativeChainFrameLocals(host, original)))) return false;
        return nativeChainBridge(host, dynamic, new String[]{}, "Lnet/minecraft/nbt/CompoundTag;", "183:" + host.name + ":" + original.name + ":" + original.desc + ":false", true) == bridge;
    }

    private static ClassNode nativeChainEvidence(ClassNode original) {
        if (original == null) return null;
        ClassNode copy = new ClassNode();original.accept(copy);
        for (MethodNode method : copy.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.bsmArgs = dynamic.bsmArgs.clone();
        return copy;
    }

    static boolean nativeChainWrapper(int family, ClassNode host, ClassNode source, ClassNode companion, List<String> baseline) {
        if (family < 0 || family > 2 || nativeChainHost(host) != family || source == null || !source.name.replace('/', '.').equals(nativeChainSource(family)) || baseline == null || baseline.isEmpty() || !baseline.getFirst().startsWith("locals:") || !host.interfaces.containsAll(source.interfaces)) return false;
        host = nativeChainEvidence(host);source = nativeChainEvidence(source);companion = nativeChainEvidence(companion);
        String[] names = family == 0 ? new String[]{"ftboss$ownerSnapshotCopy"} : family == 1 ? new String[]{"ftboss$ownerRepairGetter", "ftboss$ownerRepairSetter"} : new String[]{"ftboss$originalTimerScope", "ftboss$originalRangedScope"};
        if (family == 2 && (companion == null || !companion.name.replace('/', '.').equals(nativeChainSource(3)) || !host.interfaces.containsAll(companion.interfaces))) return false;
        MethodNode[] handlers = new MethodNode[names.length];
        for (int index = 0; index < names.length; index++) {
            ClassNode originalSource = family == 2 && index == 0 ? companion : source;
            handlers[index] = nativeChainHandler(host, originalSource, names[index], index == 0 ? null : handlers[0]);
            if (handlers[index] == null || !nativeChainAnnotation(exactMethod(originalSource, names[index], handlers[index].desc), family, index) || !handlers[index].exceptions.equals(family == 1 ? List.of("java/lang/Throwable") : List.of())) return false;
        }
        if (family == 0) return nativeSnapshotWrapper(host, source, baseline, handlers[0]);
        if (family == 1) {
            String fieldName = "ftboss$ownerRepairCall", descriptor = "Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OriginalRepairCall;";
            List<FieldNode> originalFields = source.fields.stream().filter(field -> field.name.equals(fieldName)).toList(), actualFields = host.fields.stream().filter(field -> field.name.equals(fieldName)).toList();
            if (originalFields.size() != 1 || actualFields.size() != 1 || originalFields.getFirst().access != 2 || actualFields.getFirst().access != 2 || !originalFields.getFirst().desc.equals(descriptor) || !actualFields.getFirst().desc.equals(descriptor)) return false;
            List<AnnotationNode> markers = new ArrayList<>();if (originalFields.getFirst().visibleAnnotations != null) markers.addAll(originalFields.getFirst().visibleAnnotations);if (originalFields.getFirst().invisibleAnnotations != null) markers.addAll(originalFields.getFirst().invisibleAnnotations);
            if (markers.size() != 1 || !markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;") || markers.getFirst().values != null && !markers.getFirst().values.isEmpty()) return false;
            for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof FieldInsnNode field && field.owner.equals(host.name) && field.name.equals(fieldName) && method != handlers[0] && method != handlers[1]) return false;
        }
        MethodNode parent = ownerParent(host, OWNER_APPLY, 0);int locals;
        try {locals = Integer.parseInt(baseline.getFirst().substring(7));} catch (NumberFormatException failure) {return false;}
        if (parent == null || locals < 3 || parent.maxLocals != locals + (family == 1 ? 2 : 9)) return false;
        List<AbstractInsnNode> instructions = executable(parent);List<String> actual = body(parent, host.name, host.name);List<int[]> windows = new ArrayList<>();List<String> replacements = new ArrayList<>();
        for (int operation = 0; operation < 2; operation++) {
            MethodNode handler = handlers[operation];if (!nativeChainReferences(host, handler, 1, 0)) return false;int position = -1;
            for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) position = index;
            if (position < 0 || instructions.get(position).getOpcode() != 183 || ((MethodInsnNode)instructions.get(position)).itf) return false;
            List<String> prefix = new ArrayList<>(), suffix = family == 1 && operation == 0 || family == 2 && operation == 1 ? List.of("25:1", "25:2") : List.of();String[] arguments;
            if (family == 1) {
                arguments = operation == 0 ? new String[]{"net/minecraft/world/item/ItemStack"} : new String[]{"net/minecraft/world/item/ItemStack", "int"};
                if (operation == 0) prefix.addAll(List.of("58:" + locals, "25:0", "25:" + locals));
                else prefix.addAll(List.of("54:" + (locals + 1), "58:" + locals, "25:0", "25:" + locals, "21:" + (locals + 1)));
            } else {
                arguments = operation == 0 ? new String[]{"net/minecraft/world/item/ItemStack", "net/farmingtales/ftboss/item/FoodWeaponEvolution$Profile", "java/lang/String", "net/minecraft/nbt/Tag", "net/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep", "net/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit", "int", "net/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication", "java/lang/Object"} : new String[]{"net/minecraft/server/level/ServerPlayer", "net/minecraft/world/damagesource/DamageSource", "net/minecraft/nbt/CompoundTag"};
                for (int index = arguments.length - 1; index >= 0; index--) prefix.add((arguments[index].equals("int") ? "54:" : "58:") + (locals + index));
                prefix.add("25:0");
                for (int index = 0; index < arguments.length; index++) prefix.add((arguments[index].equals("int") ? "21:" : "25:") + (locals + index));
            }
            int first = position - prefix.size() - suffix.size() - 1;
            if (first < 0 || !actual.subList(first, first + prefix.size()).equals(prefix) || !(instructions.get(first + prefix.size()) instanceof InvokeDynamicInsnNode dynamic) || !actual.subList(first + prefix.size() + 1, position).equals(suffix) || nativeChainBridge(host, dynamic, arguments, family == 1 && operation == 0 ? "Ljava/lang/Integer;" : "Ljava/lang/Void;", nativeChainOperation(family, operation), false) == null) return false;
            windows.add(new int[]{first, position});replacements.add(nativeChainOperation(family, operation));
        }
        for (int index = 0; index < instructions.size(); index++) {
            boolean inside = false;for (int[] window : windows) if (index >= window[0] && index <= window[1]) inside = true;
            if (!inside && (instructions.get(index) instanceof VarInsnNode variable && variable.var >= locals || instructions.get(index) instanceof IincInsnNode increment && increment.var >= locals)) return false;
        }
        return baseline.equals(ownerShape(host, parent, windows, replacements, locals));
    }

    static int nativeLoadSlot(String[] row) {
        if (row == null) return -1;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerTimerQuoteMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$ownerTimerQuoted", "ftboss$ownerTimerQuote"))) return 0;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerOriginalPublicationAdmissionMixin", "net.farmingtales.ftboss.item.FoodWeaponFinalDamage", "ftboss$ownerOriginalPublicationAssigned"))) return 1;
        return List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerNativeWeaponInput", "ftboss$ownerInstallNativeWeaponInput", "ftboss$ownerNativeLoadedInput")) ? 2 : -1;
    }

    static int nativeLoadHost(ClassNode host) {
        if (host == null) return -1;
        return switch (host.name) {
            case "net/farmingtales/ftboss/item/FoodWeaponHitPlan$TimerSaveLeaf" -> 0;
            case "net/farmingtales/ftboss/item/FoodWeaponFinalDamage" -> 1;
            case "net/minecraft/world/item/ItemStack" -> 2;
            case "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent" -> 3;
            default -> -1;
        };
    }

    static String nativeLoadSource(int slot) {
        return "net.farmingtales.ftboss.mixin." + switch (slot) {
            case 0 -> "OwnerTimerQuoteMixin";
            case 1 -> "OwnerOriginalPublicationAdmissionMixin";
            case 2 -> "OwnerNativeWeaponInputMixin";
            case 3 -> "NativeExpectedProviderObservationMixin";
            default -> "";
        };
    }

    private static String nativeLoadSelector(int slot) {
        return switch (slot) {
            case 0 -> "<init>(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V";
            case 1 -> "reserveListedAction(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PreparedListedAction;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$GuardResult;)Z";
            case 2 -> "parse(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;";
            case 3 -> "registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V";
            default -> "";
        };
    }

    private static String nativeLoadOperation(int slot) {
        return switch (slot) {
            case 0 -> "182:net/minecraft/nbt/CompoundTag:copy:()Lnet/minecraft/nbt/CompoundTag;:false";
            case 1 -> "181:net/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication:writerPublication:Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;";
            case 2 -> "185:com/mojang/serialization/Codec:parse:(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;:true";
            default -> "";
        };
    }

    static List<String> nativeLoadSnapshot(ClassNode host) {
        int slot = nativeLoadHost(host);
        if (slot < 0) return null;
        MethodNode parent = ownerParent(host, nativeLoadSelector(slot), slot == 0 ? 2 : slot == 1 ? 10 : slot == 2 ? 9 : 129);
        if (parent == null || parent.maxLocals < (slot == 0 ? 7 : slot == 3 ? 4 : 2)) return null;
        List<String> instructions = body(parent, host.name, host.name);
        if (slot < 3 && instructions.stream().filter(nativeLoadOperation(slot)::equals).count() != 1 || slot == 3 && instructions.stream().filter("177:"::equals).count() != 1) return null;
        for (String instruction : instructions) if (instruction.contains("ftboss$ownerTimerQuoted") || instruction.contains("ftboss$ownerOriginalPublicationAssigned") || instruction.contains("ftboss$ownerNativeLoadedInput") || instruction.contains("ftboss$originalProviderBefore") || instruction.contains("ftboss$originalProviderAfter")) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static boolean nativeLoadAnnotation(MethodNode handler, int slot) {
        AnnotationNode annotation = visibleAnnotation(handler, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, slot == 2 ? List.of("method", "at", "require", "remap") : List.of("method", "at", "require")) || !List.of(slot == 0 ? "<init>" : slot == 1 ? "reserveListedAction" : nativeLoadSelector(slot)).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !java.util.Objects.equals(value(annotation, "remap"), slot == 2 ? Boolean.FALSE : null) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        String target = slot == 0 ? "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;" : slot == 1 ? "Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;writerPublication:Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;" : "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;";
        return annotationKeys(point, slot == 1 ? List.of("value", "opcode", "target") : List.of("value", "target", "remap")) && (slot == 1 ? "FIELD" : "INVOKE").equals(value(point, "value")) && target.equals(value(point, "target")) && java.util.Objects.equals(value(point, "remap"), slot == 0 ? Boolean.TRUE : slot == 2 ? Boolean.FALSE : null) && java.util.Objects.equals(value(point, "opcode"), slot == 1 ? 181 : null);
    }

    private static boolean nativeRegistrationAnnotation(MethodNode handler, boolean before) {
        AnnotationNode annotation = visibleAnnotation(handler, "Lorg/spongepowered/asm/mixin/injection/Inject;");
        if (!annotationKeys(annotation, List.of("method", "at", "require", "remap")) || !List.of(nativeLoadSelector(3)).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !Boolean.FALSE.equals(value(annotation, "remap")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        return annotationKeys(point, List.of("value")) && (before ? "HEAD" : "RETURN").equals(value(point, "value"));
    }

    private static boolean nativeUniqueField(ClassNode host, ClassNode source, MethodNode original, MethodNode woven, int slot) {
        List<AnnotationNode> markers = annotations(original);
        if (original.access != 17 || woven.access != 17 || markers.size() != 1 || !markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;") || markers.getFirst().values != null && !markers.getFirst().values.isEmpty()) return false;
        String name = slot == 0 ? "ftboss$ownerTimerQuote" : "ftboss$ownerNativeWeaponInput";
        if (!original.name.equals(name)) return original.name.equals("ftboss$ownerInstallNativeWeaponInput") && slot == 2;
        String descriptor = slot == 0 ? "Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$TimerQuote;" : "Lnet/farmingtales/ftboss/item/OwnerNativeSemanticAuthority$WeaponInput;";
        int fields = 0;
        for (FieldNode field : host.fields) if (field.name.equals(name)) {if (!field.desc.equals(descriptor) || field.access != 2) return false;fields++;}
        return fields == 1 && woven.maxLocals == 1 && woven.maxStack == 1 && body(woven, host.name, host.name).equals(List.of("25:0", "180:" + host.name + ":" + name + ":" + descriptor, "176:"));
    }

    static boolean nativeLoadWrapper(int slot, ClassNode host, ClassNode source, List<String> baseline) {
        if (slot < 0 || slot > 3 || nativeLoadHost(host) != slot || source == null || !source.name.replace('/', '.').equals(nativeLoadSource(slot)) || baseline == null || baseline.isEmpty() || !baseline.getFirst().startsWith("locals:") || !host.interfaces.containsAll(source.interfaces)) return false;
        String[] names = slot == 0 ? new String[]{"ftboss$ownerTimerQuoted", "ftboss$ownerTimerQuote"} : slot == 1 ? new String[]{"ftboss$ownerOriginalPublicationAssigned"} : slot == 2 ? new String[]{"ftboss$ownerNativeWeaponInput", "ftboss$ownerInstallNativeWeaponInput", "ftboss$ownerNativeLoadedInput"} : new String[]{"ftboss$originalProviderBefore", "ftboss$originalProviderAfter"};
        MethodNode handler = null, before = null, anchor = null;
        for (String name : names) {
            String descriptor = OwnerFieldInstallationCatalogue.descriptor(nativeLoadSource(slot), name);
            MethodNode original = exactMethod(source, name, descriptor), woven = null;
            for (MethodNode candidate : host.methods) if (member(candidate.name).equals(name) && candidate.desc.equals(descriptor) && merged(candidate, nativeLoadSource(slot))) {if (woven != null) return false;woven = candidate;}
            if (original == null || woven == null || !mergedSession(woven, nativeLoadSource(slot), anchor == null ? woven : anchor) || !sameLambdaBody(original, woven, source, host) || !original.exceptions.isEmpty() || !woven.exceptions.isEmpty()) return false;
            if (anchor == null) anchor = woven;
            if (name.equals("ftboss$ownerTimerQuote") || name.equals("ftboss$ownerNativeWeaponInput") || name.equals("ftboss$ownerInstallNativeWeaponInput")) {if (!nativeUniqueField(host, source, original, woven, slot)) return false;}
            else {
                if (original.access != (slot == 0 || slot == 3 ? 2 : 10) || woven.access != original.access || !(slot == 3 ? nativeRegistrationAnnotation(original, name.endsWith("Before")) : nativeLoadAnnotation(original, slot))) return false;
                if (name.endsWith("Before")) before = woven; else handler = woven;
            }
        }
        MethodNode parent = ownerParent(host, nativeLoadSelector(slot), slot == 0 ? 2 : slot == 1 ? 10 : slot == 2 ? 9 : 129);
        int locals;
        if (slot == 3 && parent != null) {
            parent = proofClone(parent);
            try { NativeCapabilityInstallationProof170.restorePreQuery199(host, parent); }
            catch (RuntimeException rejected) { return false; }
        }
        try {locals = Integer.parseInt(baseline.getFirst().substring(7));} catch (NumberFormatException failure) {return false;}
        if (parent == null || handler == null || locals < (slot == 0 ? 7 : slot == 3 ? 4 : 2) || parent.maxLocals != locals + (slot == 0 ? 1 : slot == 1 ? 2 : slot == 2 ? 3 : 0)) return false;
        List<AbstractInsnNode> instructions = executable(parent);
        int position = -1, handlerCalls = 0, beforeCalls = 0;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) {if (call(instruction, handler, host.name)) handlerCalls++;if (before != null && call(instruction, before, host.name)) beforeCalls++;}
        if (handlerCalls != 1 || slot == 3 && beforeCalls != 1) return false;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) position = index;
        List<String> actual = body(parent, host.name, host.name);
        if (slot == 3) {
            if (before == null || instructions.size() < 13 || !actual.subList(0, 6).equals(List.of("25:0", "25:1", "25:2", "25:3", "1:", "183:" + host.name + ":ftboss$originalProviderBefore:" + before.desc + ":false")) || !actual.subList(actual.size() - 7, actual.size()).equals(List.of("25:0", "25:1", "25:2", "25:3", "1:", "183:" + host.name + ":ftboss$originalProviderAfter:" + handler.desc + ":false", "177:"))) return false;
            MethodNode restored = proofClone(parent);List<AbstractInsnNode> copied = executable(restored);
            for (int index = 0; index < 6; index++) {restored.instructions.remove(copied.get(index));restored.instructions.remove(copied.get(copied.size() - 7 + index));}
            return baseline.equals(ownerShape(host, restored, List.of(), List.of(), locals));
        }
        int first = position - (slot == 0 ? 10 : slot == 1 ? 5 : 9);
        if (first < 0 || !(instructions.get(position) instanceof MethodInsnNode invocation) || invocation.getOpcode() != (slot == 0 ? 183 : 184) || invocation.itf) return false;
        List<String> prefix = slot == 0 ? List.of("58:" + locals, "25:0", "25:" + locals) : slot == 1 ? List.of("58:" + (locals + 1), "58:" + locals, "25:" + locals, "25:" + (locals + 1)) : List.of("58:" + (locals + 2), "58:" + (locals + 1), "58:" + locals, "25:" + locals, "25:" + (locals + 1), "25:" + (locals + 2));
        if (!actual.subList(first, first + prefix.size()).equals(prefix) || !(instructions.get(first + prefix.size()) instanceof InvokeDynamicInsnNode operation) || !ownerBridge(host, operation, slot == 0 ? 2 : slot == 1 ? 8 : 9, nativeLoadOperation(slot), new ArrayList<>())) return false;
        List<String> suffix = slot == 0 ? List.of("25:1", "25:2", "25:3", "25:4", "25:5", "25:6") : slot == 1 ? List.of() : List.of("25:0", "25:1");
        if (!actual.subList(first + prefix.size() + 1, position).equals(suffix)) return false;
        for (int index = 0; index < instructions.size(); index++) if (index < first || index > position) {AbstractInsnNode instruction = instructions.get(index);if (instruction instanceof VarInsnNode variable && variable.var >= locals || instruction instanceof IincInsnNode increment && increment.var >= locals) return false;}
        return baseline.equals(ownerShape(host, parent, List.of(new int[]{first, position}), List.of(nativeLoadOperation(slot)), locals));
    }

    static int nativeOperationSlot(String[] row) {
        if (row == null) return -1;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$ownerSemanticLeaf"))) return 0;
        if (List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$ownerMaterialGuard"))) return 1;
        return canonicalAliasSite(row) && row[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin") ? 1 : -1;
    }

    static List<String> semanticParentSnapshot(ClassNode host) {
        if (host == null || !host.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan")) return null;
        MethodNode parent = exactMethod(host, "applyFinalEffects", "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V");
        if (parent == null || parent.access != 8 || parent.maxLocals < 2) return null;
        String operation = semanticOperation(false);
        List<String> instructions = body(parent, host.name, host.name);
        if (instructions.stream().filter(operation::equals).count() != 1 || instructions.stream().anyMatch(value -> value.contains("ftboss$ownerSemanticLeaf"))) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static String semanticOperation(boolean ranged) {
        return ranged ? "182:net/minecraft/nbt/CompoundTag:isEmpty:()Z:false" : "182:net/farmingtales/ftboss/item/FoodWeaponHitPlan$TypedLeaf:apply:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V:false";
    }

    private static String[] rangedCompositionRow() {
        return new String[]{"net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$originalRangedPersistentData", "ftboss$originalRangedCompound", "ftboss$originalRangedCompoundSrg", "ftboss$originalRangedLong", "ftboss$originalRangedLongSrg", "ftboss$originalRangedRoot", "ftboss$originalRangedRootSrg"};
    }

    private static boolean semanticAnnotation(MethodNode handler, boolean ranged) {
        AnnotationNode annotation = visibleAnnotation(handler, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, List.of("method", "at", "require")) || !List.of(ranged ? "saveHitTimers" : "applyFinalEffects").equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        return annotationKeys(point, ranged ? List.of("value", "target", "remap") : List.of("value", "target")) && "INVOKE".equals(value(point, "value")) && (ranged ? "Lnet/minecraft/nbt/CompoundTag;isEmpty()Z" : "Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$TypedLeaf;apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V").equals(value(point, "target")) && java.util.Objects.equals(value(point, "remap"), ranged ? Boolean.TRUE : null);
    }

    static boolean nativeOperationWrapper(int slot, ClassNode host, ClassNode source, ClassNode rangedSource, List<List<String>> baselines) {
        if ((slot != 0 && slot != 1) || host == null || source == null || baselines == null || baselines.size() != (slot == 0 ? 1 : 2)) return false;
        boolean ranged = slot == 1;
        String expectedHost = "net/farmingtales/ftboss/item/" + (ranged ? "FoodWeaponRanged" : "FoodWeaponHitPlan");
        String expectedSource = "net/farmingtales/ftboss/mixin/" + (ranged ? "OwnerTimerRangedGuardMixin" : "OwnerTimerSemanticConsumerMixin");
        String handlerName = ranged ? "ftboss$ownerMaterialGuard" : "ftboss$ownerSemanticLeaf";
        if (!host.name.equals(expectedHost) || !source.name.equals(expectedSource)) return false;
        String descriptor = OwnerFieldInstallationCatalogue.descriptor(source.name.replace('/', '.'), handlerName);
        MethodNode original = exactMethod(source, handlerName, descriptor), handler = null;
        for (MethodNode method : host.methods) if (member(method.name).equals(handlerName) && method.desc.equals(descriptor) && merged(method, source.name.replace('/', '.'))) { if (handler != null) return false; handler = method; }
        if (original == null || handler == null || original.access != 10 || handler.access != 10 || !semanticAnnotation(original, ranged) || !mergedSession(handler, source.name.replace('/', '.'), handler) || !sameLambdaBody(original, handler, source, host) || !original.exceptions.equals(ranged ? List.of() : List.of("java/lang/Throwable")) || !handler.exceptions.equals(original.exceptions)) return false;
        MethodNode parent = exactMethod(host, ranged ? "saveHitTimers" : "applyFinalEffects", ranged ? "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V" : "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V");
        List<String> baseline = baselines.get(ranged ? 1 : 0);
        if (parent == null || parent.access != (ranged ? 9 : 8) || baseline == null || baseline.isEmpty() || !baseline.getFirst().startsWith("locals:")) return false;
        int locals;
        try { locals = Integer.parseInt(baseline.getFirst().substring(7)); } catch (NumberFormatException failure) { return false; }
        if (locals < (ranged ? 3 : 2) || parent.maxLocals != locals + (ranged ? 4 : 3)) return false;
        List<AbstractInsnNode> instructions = executable(parent);
        int position = -1, calls = 0;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) if (call(instruction, handler, host.name)) calls++;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), handler, host.name)) { if (position >= 0) return false; position = index; }
        int first = position - (ranged ? 6 : 7);
        if (calls != 1 || first < 0 || !(instructions.get(position) instanceof MethodInsnNode invoke) || invoke.getOpcode() != 184 || invoke.itf) return false;
        List<String> actual = body(parent, host.name, host.name);
        List<String> expected = ranged ? List.of("58:" + locals, "25:" + locals) : List.of("58:" + (locals + 2), "58:" + (locals + 1), "58:" + locals, "25:" + locals, "25:" + (locals + 1), "25:" + (locals + 2));
        if (!actual.subList(first, first + expected.size()).equals(expected)) return false;
        int dynamicIndex = first + expected.size();
        if (!(instructions.get(dynamicIndex) instanceof InvokeDynamicInsnNode operation) || !ownerBridge(host, operation, ranged ? 7 : 6, semanticOperation(ranged), new ArrayList<>())) return false;
        if (ranged && !actual.subList(dynamicIndex + 1, position).equals(List.of("25:0", "25:1", "25:2"))) return false;
        List<int[]> windows = List.of(new int[]{first, position});
        if (!ranged) {
            if (rangedSource == null || !rangedSource.name.equals("net/farmingtales/ftboss/mixin/NativeExpectedActualStepMixin")) return false;
            String anchorName = "ftboss$actualReturnedTimerStep";
            String anchorDescriptor = OwnerFieldInstallationCatalogue.descriptor(rangedSource.name.replace('/', '.'), anchorName);
            MethodNode originalAnchor = exactMethod(rangedSource, anchorName, anchorDescriptor), actualAnchor = null;
            for (MethodNode candidate : host.methods) if (member(candidate.name).equals(anchorName) && candidate.desc.equals(anchorDescriptor) && merged(candidate, rangedSource.name.replace('/', '.'))) { if (actualAnchor != null) return false; actualAnchor = candidate; }
            if (originalAnchor == null || actualAnchor == null || !sameLambdaBody(originalAnchor, actualAnchor, rangedSource, host) || !siteAndOrder(host, originalAnchor, actualAnchor) || !mergedSession(actualAnchor, rangedSource.name.replace('/', '.'), handler)) return false;
            for (int index = 0; index < instructions.size(); index++) if (index < first || index > position) { AbstractInsnNode instruction = instructions.get(index); if (instruction instanceof VarInsnNode variable && variable.var >= locals || instruction instanceof IincInsnNode increment && increment.var >= locals) return false; }
            return baseline.equals(ownerShape(host, parent, windows, List.of(semanticOperation(false)), locals));
        }
        if (rangedSource == null || !rangedSource.name.equals("net/farmingtales/ftboss/mixin/NativeExpectedRangedSitesMixin") || ownerShape(host, parent, windows, List.of(semanticOperation(true)), locals) == null) return false;
        MethodNode rangedAnchor = ownerHandler(host, rangedSource, "ftboss$originalRangedPersistentData", false);
        if (rangedAnchor == null || !mergedSession(handler, source.name.replace('/', '.'), rangedAnchor)) return false;
        for (String name : List.of("ftboss$originalRangedPersistentData", "ftboss$originalRangedCompoundSrg", "ftboss$originalRangedLongSrg", "ftboss$originalRangedRootSrg")) {
            MethodNode companion = ownerHandler(host, rangedSource, name, false);
            if (companion == null || !mergedSession(companion, rangedSource.name.replace('/', '.'), handler)) return false;
        }
        ClassNode restored = new ClassNode();host.accept(restored);
        MethodNode restoredParent = exactMethod(restored, parent.name, parent.desc);
        List<AbstractInsnNode> restoredInstructions = executable(restoredParent);
        restoredParent.instructions.insertBefore(restoredInstructions.get(first), new MethodInsnNode(182, "net/minecraft/nbt/CompoundTag", "isEmpty", "()Z", false));
        for (int index = first; index <= position; index++) restoredParent.instructions.remove(restoredInstructions.get(index));
        return canonicalRangedWrapper(rangedCompositionRow(), restored, rangedSource, baselines);
    }

    static boolean entityOwnerSite(String[] row) {
        return row != null && List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerFieldConstructed", "ftboss$ownerFieldPersistentExposed", "ftboss$ownerFieldRawLevel", "ftboss$ownerFieldDomain", "ftboss$ownerFieldInstallDomain", "ftboss$ownerFieldLoadQuoted", "ftboss$ownerFieldLoadAssigned"));
    }

    static List<String> entityLoadSnapshot(ClassNode host) {
        if (host == null || !host.name.equals("net/minecraft/world/entity/Entity")) return null;
        MethodNode parent = exactMethod(host, "load", "(Lnet/minecraft/nbt/CompoundTag;)V");
        if (parent == null || parent.access != 1 || parent.maxLocals < 2) return null;
        List<String> instructions = body(parent, host.name, host.name);
        String assignment = "181:" + host.name + ":persistentData:Lnet/minecraft/nbt/CompoundTag;";
        int assignments = 0;
        for (int index = 0; index < instructions.size(); index++) if (instructions.get(index).equals(assignment)) {
            if (index < 4 || !instructions.subList(index - 4, index).equals(List.of("25:0", "25:1", "18:java.lang.String:NeoForgeData", "182:net/minecraft/nbt/CompoundTag:getCompound:(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;:false"))) return null;
            assignments++;
        }
        if (assignments != 1 || instructions.stream().anyMatch(value -> value.contains("ftboss$ownerFieldLoadAssigned") || value.equals("18:java.lang.String:ForgeData"))) return null;
        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);
    }

    private static boolean entityWrapperAnnotation(MethodNode handler) {
        AnnotationNode annotation = visibleAnnotation(handler, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!annotationKeys(annotation, List.of("method", "at", "require")) || !List.of("load(Lnet/minecraft/nbt/CompoundTag;)V").equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        return annotationKeys(point, List.of("value", "target", "opcode", "remap")) && "FIELD".equals(value(point, "value")) && "Lnet/minecraft/world/entity/Entity;persistentData:Lnet/minecraft/nbt/CompoundTag;".equals(value(point, "target")) && Integer.valueOf(181).equals(value(point, "opcode")) && Boolean.FALSE.equals(value(point, "remap"));
    }

    private static boolean entityHandlerAnnotation(MethodNode handler) {
        if (List.of("ftboss$ownerFieldRawLevel", "ftboss$ownerFieldDomain", "ftboss$ownerFieldInstallDomain").contains(handler.name)) {
            List<AnnotationNode> markers = annotations(handler);
            return markers.size() == 1 && markers.getFirst().desc.equals("Lorg/spongepowered/asm/mixin/Unique;") && (markers.getFirst().values == null || markers.getFirst().values.isEmpty());
        }
        String selector = handler.name.equals("ftboss$ownerFieldConstructed") ? "<init>" : handler.name.equals("ftboss$ownerFieldPersistentExposed") ? "getPersistentData()Lnet/minecraft/nbt/CompoundTag;" : handler.name.equals("ftboss$ownerFieldLoadQuoted") ? "load(Lnet/minecraft/nbt/CompoundTag;)V" : "";
        AnnotationNode annotation = visibleAnnotation(handler, "Lorg/spongepowered/asm/mixin/injection/Inject;");
        if (selector.isEmpty() || !annotationKeys(annotation, List.of("method", "at", "require", "remap")) || !List.of(selector).equals(value(annotation, "method")) || !Integer.valueOf(1).equals(value(annotation, "require")) || !java.util.Objects.equals(value(annotation, "remap"), handler.name.equals("ftboss$ownerFieldPersistentExposed") ? Boolean.FALSE : null) || !(value(annotation, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        return annotationKeys(point, List.of("value")) && (handler.name.equals("ftboss$ownerFieldLoadQuoted") ? "HEAD" : "RETURN").equals(value(point, "value"));
    }

    static boolean entityLoadWrapper(String[] row, ClassNode host, ClassNode source, List<String> baseline) {
        if (!entityOwnerSite(row) || host == null || source == null || baseline == null || !host.name.replace('/', '.').equals(row[1]) || !source.name.replace('/', '.').equals(row[0]) || !host.interfaces.containsAll(source.interfaces) || baseline.isEmpty() || !baseline.getFirst().startsWith("locals:")) return false;
        MethodNode wrapper = null;
        MethodNode quote = null;
        MethodNode anchor = null;
        for (int index = 2; index < row.length; index++) {
            String descriptor = OwnerFieldInstallationCatalogue.descriptor(row[0], row[index]);
            MethodNode original = exactMethod(source, row[index], descriptor);
            MethodNode woven = null;
            for (MethodNode candidate : host.methods) if (member(candidate.name).equals(row[index]) && candidate.desc.equals(descriptor) && merged(candidate, row[0])) { if (woven != null) return false; woven = candidate; }
            if (original == null || woven == null || !mergedSession(woven, row[0], anchor == null ? woven : anchor) || !sameLambdaBody(original, woven, source, host)) return false;
            if (anchor == null) anchor = woven;
            if (row[index].equals("ftboss$ownerFieldLoadAssigned")) {
                if (original.access != 2 || !entityWrapperAnnotation(original)) return false;
                wrapper = woven;
            } else {
                if (!entityHandlerAnnotation(original) || !siteAndOrder(host, original, woven)) return false;
                if (row[index].equals("ftboss$ownerFieldLoadQuoted")) quote = woven;
            }
        }
        MethodNode parent = exactMethod(host, "load", "(Lnet/minecraft/nbt/CompoundTag;)V");
        if (wrapper == null || quote == null || parent == null || parent.access != 1 || !mergedSession(wrapper, row[0], quote)) return false;
        int locals;
        try { locals = Integer.parseInt(baseline.getFirst().substring(7)); } catch (NumberFormatException failure) { return false; }
        if (locals < 2 || parent.maxLocals != locals + 2) return false;
        MethodNode restored = nativeEntityExternalParent270(host, parent, quote); if (restored == null) return false;
        List<AbstractInsnNode> instructions = executable(restored);
        if (instructions.size() < 12 || !body(restored, host.name, host.name).subList(0, 4).equals(List.of("25:0", "25:1", "1:", "183:" + host.name + ":ftboss$ownerFieldLoadQuoted:(Lnet/minecraft/nbt/CompoundTag;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V:false"))) return false;
        for (int index = 0; index < 4; index++) restored.instructions.remove(instructions.get(index));
        instructions = executable(restored);
        int position = -1;
        int quoteCalls = 0;
        int wrapperCalls = 0;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof MethodInsnNode call && call.owner.equals(host.name)) {
            if (call.name.equals(quote.name) && call.desc.equals(quote.desc)) quoteCalls++;
            if (call.name.equals(wrapper.name) && call.desc.equals(wrapper.desc)) wrapperCalls++;
        }
        if (quoteCalls != 1 || wrapperCalls != 1) return false;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), wrapper, host.name)) { if (position >= 0) return false; position = index; }
        if (position < 6 || !(instructions.get(position) instanceof MethodInsnNode invocation) || invocation.getOpcode() != 183 || invocation.itf || !(instructions.get(position - 6) instanceof VarInsnNode valueStore) || valueStore.getOpcode() != 58 || valueStore.var != locals + 1 || !(instructions.get(position - 5) instanceof VarInsnNode hostStore) || hostStore.getOpcode() != 58 || hostStore.var != locals) return false;
        List<String> actual = body(restored, host.name, host.name);
        if (!actual.subList(position - 4, position - 1).equals(List.of("25:0", "25:" + locals, "25:" + (locals + 1))) || !(instructions.get(position - 1) instanceof InvokeDynamicInsnNode operation)) return false;
        String assignment = "181:" + host.name + ":persistentData:Lnet/minecraft/nbt/CompoundTag;";
        List<String> bridges = new ArrayList<>();
        if (!ownerBridge(host, operation, 5, assignment, bridges)) return false;
        for (int index = 0; index < instructions.size(); index++) if (index < position - 6 || index > position) {
            AbstractInsnNode instruction = instructions.get(index);
            if (instruction instanceof VarInsnNode variable && variable.var >= locals || instruction instanceof IincInsnNode increment && increment.var >= locals || call(instruction, wrapper, host.name)) return false;
        }
        List<String> shape = ownerShape(host, restored, List.of(new int[]{position - 6, position}), List.of(assignment), locals);
        return baseline.equals(shape);
    }

    private static boolean nativeEntityIdentity(String[] row, ClassNode host, ClassNode source) {
        if (!List.of(row).equals(List.of("net.farmingtales.ftboss.mixin.OwnerNativeMaterialEntityIdentityMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerMaterialEntityIdentity")) || !host.name.equals("net/minecraft/world/entity/Entity") || !source.name.replace('/', '.').equals(row[0]) || !host.interfaces.containsAll(source.interfaces)) return false;
        String descriptor = "()Ljava/util/UUID;";
        MethodNode original = exactMethod(source, row[2], descriptor), woven = exactMethod(host, row[2], descriptor);
        if (original == null || woven == null || original.access != 17 || woven.access != 17 || !original.tryCatchBlocks.isEmpty() || !woven.tryCatchBlocks.isEmpty() || !mergedSession(woven, row[0], woven) || !sameLambdaBody(original, woven, source, host) || woven.maxLocals != 1 || woven.maxStack != 1) return false;
        int count = 0;
        for (FieldNode field : host.fields) if (field.name.equals("uuid") && field.desc.equals("Ljava/util/UUID;")) { if (field.access != 4) return false; count++; }
        return count == 1 && body(woven, host.name, host.name).equals(List.of("25:0", "180:" + host.name + ":uuid:Ljava/util/UUID;", "176:"));
    }

    static String nativeAliasMixin(String host) {
        String simple = switch (host) {
            case "net.minecraft.nbt.CompoundTag" -> "CompoundTag";
            case "net.minecraft.nbt.ListTag" -> "ListTag";
            case "net.minecraft.nbt.ByteArrayTag" -> "ByteArrayTag";
            case "net.minecraft.nbt.IntArrayTag" -> "IntArrayTag";
            case "net.minecraft.nbt.LongArrayTag" -> "LongArrayTag";
            default -> null;
        };
        return simple == null ? null : "net.farmingtales.ftboss.mixin.Native" + simple + "MutationVersionMixin";
    }

    static boolean observedFieldHost(ClassNode host) {
        return host != null && (nativeAliasMixin(host.name.replace('/', '.')) != null || host.name.equals("net/minecraft/world/entity/Entity"));
    }

    static boolean observedFieldSite(String[] row) {
        if (row == null || row.length < 3) return false;
        String simple;
        List<String> methods;
        switch (row[1]) {
            case "net.minecraft.nbt.CompoundTag": simple = "CompoundTag"; methods = List.of("ftboss$rawCompoundFields"); break;
            case "net.minecraft.nbt.ListTag": simple = "ListTag"; methods = List.of("ftboss$rawListFields", "ftboss$rawListKind"); break;
            case "net.minecraft.nbt.ByteArrayTag": simple = "ByteArrayTag"; methods = List.of("ftboss$rawBytes"); break;
            case "net.minecraft.nbt.IntArrayTag": simple = "IntArrayTag"; methods = List.of("ftboss$rawInts"); break;
            case "net.minecraft.nbt.LongArrayTag": simple = "LongArrayTag"; methods = List.of("ftboss$rawLongs"); break;
            case "net.minecraft.world.entity.Entity": simple = "Actor"; methods = List.of("ftboss$rawPersistentData"); break;
            default: return false;
        }
        return row[0].equals("net.farmingtales.ftboss.mixin.NativeExpected" + simple + "FieldsMixin") && List.of(row).subList(2, row.length).equals(methods);
    }

    private static boolean mergedSession(MethodNode method, String origin, MethodNode anchor) {
        AnnotationNode metadata = visibleAnnotation(method, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
        AnnotationNode reference = visibleAnnotation(anchor, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
        return annotationKeys(metadata, List.of("mixin", "priority", "sessionId")) && origin.equals(value(metadata, "mixin")) && value(metadata, "priority") instanceof Integer && value(metadata, "sessionId") instanceof String session && !session.isEmpty() && session.equals(value(reference, "sessionId")) && value(metadata, "priority").equals(value(reference, "priority"));
    }

    private static boolean returnedFieldObserver(ClassNode actual, ClassNode source, MethodNode anchor, MethodNode handler, String name) {
        if (source == null || handler == null || handler.access != 2 || !handler.desc.equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V") || !mergedSession(handler, source.name.replace('/', '.'), anchor)) return false;
        MethodNode original = exactMethod(source, name, handler.desc);
        if (original == null || original.access != 2 || !sameLambdaBody(original, handler, source, actual)) return false;
        AnnotationNode inject = visibleAnnotation(original, "Lorg/spongepowered/asm/mixin/injection/Inject;");
        if (!annotationKeys(inject, List.of("method", "at", "require", "remap")) || !(value(inject, "require") instanceof Integer count) || count != 1 || Boolean.TRUE.equals(value(inject, "remap"))) return false;
        if (!(value(inject, "method") instanceof List<?> methods) || !(methods.contains(anchor.name) || methods.contains(anchor.name + anchor.desc))) return false;
        if (!(value(inject, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point)) return false;
        return annotationKeys(point, List.of("value")) && "RETURN".equals(value(point, "value"));
    }

    static boolean composedFieldView(String[] row, ClassNode actual, ClassNode source, List<ClassNode> observers) {
        if (!observedFieldSite(row) || actual == null || source == null || observers == null || observers.size() != (row[1].equals("net.minecraft.world.entity.Entity") ? 1 : 2) || !actual.name.replace('/', '.').equals(row[1]) || !source.name.replace('/', '.').equals(row[0]) || !actual.interfaces.containsAll(source.interfaces)) return false;
        for (int index = 2; index < row.length; index++) {
            String name = row[index];
            String descriptor = OwnerFieldInstallationCatalogue.descriptor(row[0], name);
            MethodNode original = exactMethod(source, name, descriptor);
            MethodNode woven = exactMethod(actual, name, descriptor);
            if (original == null || woven == null || original.access != 1 || woven.access != 1 || !original.tryCatchBlocks.isEmpty() || !woven.tryCatchBlocks.isEmpty() || !mergedSession(woven, row[0], woven)) return false;
            List<AbstractInsnNode> instructions = executable(original);
            if (instructions.size() != 3 || !(instructions.get(0) instanceof VarInsnNode load) || load.getOpcode() != 25 || load.var != 0 || !(instructions.get(1) instanceof FieldInsnNode field) || field.getOpcode() != 180 || !field.owner.equals(source.name) || !field.desc.equals(Type.getReturnType(descriptor).getDescriptor()) || instructions.get(2).getOpcode() != Type.getReturnType(descriptor).getOpcode(172)) return false;
            String expectedField = switch (name) {
                case "ftboss$rawCompoundFields" -> "tags:Ljava/util/Map;:" + (nativePublicCompoundMap(actual) ? 17 : 18);
                case "ftboss$rawListFields" -> "list:Ljava/util/List;:18";
                case "ftboss$rawListKind" -> "type:B:2";
                case "ftboss$rawBytes" -> "data:[B:2";
                case "ftboss$rawInts" -> "data:[I:2";
                case "ftboss$rawLongs" -> "data:[J:2";
                case "ftboss$rawPersistentData" -> "persistentData:Lnet/minecraft/nbt/CompoundTag;:2";
                default -> "";
            };
            int fields = 0;
            for (FieldNode actualField : actual.fields) if (actualField.name.equals(field.name) && actualField.desc.equals(field.desc)) {
                if (!(actualField.name + ":" + actualField.desc + ":" + actualField.access).equals(expectedField)) return false;
                fields++;
            }
            if (fields != 1) return false;
            List<String> expected = new ArrayList<>();
            expected.add("25:0"); expected.add("180:" + actual.name + ":" + field.name + ":" + field.desc);
            boolean primitive = name.equals("ftboss$rawListKind");
            if (!primitive) {
                for (int observer = 0; observer < observers.size(); observer++) {
                    ClassNode observerSource = observers.get(observer);
                    String expectedSource = observer == observers.size() - 1 ? "net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin" : nativeAliasMixin(row[1]);
                    String handlerName = observer == observers.size() - 1 ? "ftboss$ownerRawExposed" : "ftboss$nativeRawAlias";
                    if (observerSource == null || !observerSource.name.replace('/', '.').equals(expectedSource)) return false;
                    MethodNode handler = null;
                    for (MethodNode candidate : actual.methods) if (member(candidate.name).equals(handlerName) && merged(candidate, expectedSource)) { if (handler != null) return false; handler = candidate; }
                    if (!returnedFieldObserver(actual, observerSource, woven, handler, handlerName)) return false;
                    int local = observer + 1;
                    String callback = "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable";
                    expected.addAll(List.of("89:", "58:" + local, "25:0", "187:" + callback, "89:", "18:java.lang.String:" + name, "3:", "25:" + local, "183:" + callback + ":<init>:(Ljava/lang/String;ZLjava/lang/Object;)V:false", "183:" + actual.name + ":" + handlerName + ":(L" + callback + ";)V:false"));
                }
            }
            expected.add(primitive ? "172:" : "176:");
            if (!expected.equals(body(woven, actual.name, actual.name)) || woven.maxLocals != (primitive ? 1 : observers.size() + 1) || (primitive ? woven.maxStack != 1 : woven.maxStack != 7 && woven.maxStack != 9)) return false;
        }
        return true;
    }

    private static boolean nativeProtectedSourceField(String[] row, ClassNode host, FieldNode field) {
        return row.length == 3 && row[0].equals("net.farmingtales.ftboss.mixin.OwnerFieldSourceAccessMixin") && row[1].equals("net.minecraft.world.damagesource.DamageSource") && row[2].equals("ftboss$ownerFieldDirectEntity") && host.name.equals("net/minecraft/world/damagesource/DamageSource") && field.name.equals("directEntity") && field.desc.equals("Lnet/minecraft/world/entity/Entity;") && field.access == 20;
    }

    private static boolean accessorGetter(String[] var0, ClassNode var1, ClassNode var2, MethodNode var3, MethodNode var4) {
        String var5 = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
        AnnotationNode var6 = visibleAnnotation(var3, var5);
        AnnotationNode var7 = visibleAnnotation(var4, var5);
        AnnotationNode var8 = visibleAnnotation(var4, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
        List var9 = List.of("value", "remap", "target");
        if (!annotationKeys(var6, var9)
            || !annotationKeys(var7, var9)
            || !annotationKeys(var8, List.of("mixin", "priority", "sessionId"))
            || !var0[0].equals(value(var8, "mixin"))
            || !(value(var8, "priority") instanceof Integer)
            || !(value(var8, "sessionId") instanceof String var10)
            || var10.isEmpty()) {
            return false;
        } else if ((var2.access & 512) != 0
            && var1.interfaces != null
            && var1.interfaces.contains(var2.name)
            && var3.access == 1025
            && var4.access == 4097
            && var4.name.equals(var3.name)
            && var4.desc.equals(var3.desc)
            && var3.desc.startsWith("()")
            && !var3.desc.equals("()V")
            && var4.signature == null
            && var3.tryCatchBlocks.isEmpty()
            && var4.tryCatchBlocks.isEmpty()
            && (var4.exceptions == null || var4.exceptions.isEmpty())) {
            for (AbstractInsnNode var12 : var3.instructions) {
                if (var12.getOpcode() >= 0) {
                    return false;
                }
            }

            if (annotations(var3).size() == 1
                && annotations(var4).size() == 2
                && value(var6, "value") instanceof String var20
                && !var20.isEmpty()
                && var20.equals(value(var7, "value"))) {
                Object var22 = value(var6, "remap");
                if (var22 == null ? value(var7, "remap") == null : var22.equals(value(var7, "remap"))) {
                    ArrayList var13 = new ArrayList();

                    for (AbstractInsnNode var15 : var4.instructions) {
                        if (var15.getOpcode() >= 0) {
                            var13.add(var15);
                        }
                    }

                    if (var13.size() == 3
                        && var13.get(0) instanceof VarInsnNode var23
                        && var23.getOpcode() == 25
                        && var23.var == 0
                        && var13.get(1) instanceof FieldInsnNode var24
                        && var24.getOpcode() == 180
                        && var24.owner.equals(var1.name)
                        && accessorFieldName(var1, var2, var3, var6, var24.name)) {
                        Type var26 = Type.getReturnType(var3.desc);
                        if (var24.desc.equals(var26.getDescriptor())
                            && ((AbstractInsnNode)var13.get(2)).getType() == 0
                            && ((AbstractInsnNode)var13.get(2)).getOpcode() == var26.getOpcode(172)
                            && var4.maxLocals == var26.getSize()
                            && var4.maxStack == var26.getSize()) {
                            String var17 = var24.name + ":" + var24.desc;
                            if (var17.equals(value(var7, "target")) && (value(var6, "target") == null || var17.equals(value(var6, "target")))) {
                                List var18 = var1.fields.stream().filter(var1x -> var1x.name.equals(var24.name) && var1x.desc.equals(var24.desc)).toList();
                                return var18.size() == 1 && (((FieldNode)var18.get(0)).access == 2 || ((FieldNode)var18.get(0)).access == 18 || nativeProtectedSourceField(var0, var1, (FieldNode)var18.get(0)));
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    static boolean lateMapSite(String[] var0) {
        return var0 != null
            && var0.length == 3
            && var0[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin")
            && var0[1].equals("net.minecraft.nbt.CompoundTag")
            && var0[2].equals("ftboss$originalFieldMapPut");
    }

    private static List<AbstractInsnNode> executable(MethodNode var0) {
        ArrayList var1 = new ArrayList();

        for (AbstractInsnNode var3 : var0.instructions) {
            if (var3.getOpcode() >= 0) {
                var1.add(var3);
            }
        }

        return var1;
    }

    private static boolean mapBridge(ClassNode var0, InvokeDynamicInsnNode var1, List<String> var2) {
        String var3 = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String var4 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (var1.name.equals("call")
            && var1.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;")
            && var1.bsm.getTag() == 6
            && !var1.bsm.isInterface()
            && var1.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
            && var1.bsm.getName().equals("metafactory")
            && var1.bsm.getDesc().equals(var4)
            && var1.bsmArgs.length == 3
            && var1.bsmArgs[0] instanceof Type var5
            && var5.getDescriptor().equals(var3)
            && var1.bsmArgs[1] instanceof Handle var6
            && var6.getTag() == 6
            && !var6.isInterface()
            && var6.getOwner().equals(var0.name)
            && var6.getDesc().equals(var3)
            && var1.bsmArgs[2] instanceof Type var7
            && var7.getDescriptor().equals(var3)
            && !var2.contains(var6.getName())) {
            List var25 = var0.methods.stream().filter(var1x -> var1x.name.equals(var6.getName()) && var1x.desc.equals(var6.getDesc())).toList();
            if (var25.size() != 1) {
                return false;
            } else {
                MethodNode var9 = (MethodNode)var25.get(0);
                if (var9.access == 4106
                    && var9.signature == null
                    && var9.tryCatchBlocks.isEmpty()
                    && (var9.exceptions == null || var9.exceptions.isEmpty())
                    && annotations(var9).isEmpty()) {
                    ArrayList var10 = new ArrayList<>(
                        List.of(
                            "25:0",
                            "16:3",
                            "18:java.lang.String:[java.util.Map, java.lang.Object, java.lang.Object]",
                            "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false",
                            "25:0"
                        )
                    );
                    String[] var11 = new String[]{"java/util/Map", "java/lang/Object", "java/lang/Object"};

                    for (int var12 = 0; var12 < var11.length; var12++) {
                        var10.addAll(List.of("89:", "16:" + var12, "50:", "192:" + var11[var12], "95:"));
                    }

                    var10.addAll(List.of("87:", "185:java/util/Map:put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;:true", "176:"));
                    if (!body(var9, var0.name, var0.name).equals(var10)) {
                        return false;
                    } else {
                        int var26 = 0;

                        for (MethodNode var14 : var0.methods) {
                            for (AbstractInsnNode var16 : var14.instructions) {
                                if (var16 instanceof MethodInsnNode var17
                                    && var17.owner.equals(var0.name)
                                    && var17.name.equals(var6.getName())
                                    && var17.desc.equals(var3)) {
                                    return false;
                                }

                                if (var16 instanceof LdcInsnNode var27
                                    && var27.cst instanceof Handle var18
                                    && var18.getOwner().equals(var0.name)
                                    && var18.getName().equals(var6.getName())) {
                                    return false;
                                }

                                if (var16 instanceof InvokeDynamicInsnNode var28) {
                                    for (Object var22 : var28.bsmArgs) {
                                        if (var22 instanceof Handle) {
                                            Handle var29 = (Handle)var22;
                                            if (var29.getOwner().equals(var0.name) && var29.getName().equals(var6.getName()) && var29.getDesc().equals(var3)) {
                                                var26++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (var26 != 1) {
                            return false;
                        } else {
                            var2.add(var6.getName());
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean nativePublicCompoundMap(ClassNode actual) {
        if (actual == null || !actual.name.equals("net/minecraft/nbt/CompoundTag")) return false;
        try (var raw = OwnerFieldInstallationProof.class.getClassLoader().getResourceAsStream("net/minecraft/nbt/CompoundTag.class");
             var loader = OwnerFieldInstallationProof.class.getClassLoader().getResourceAsStream("net/minecraft/nbt/CompoundTag$1.class");
             var source = OwnerFieldInstallationProof.class.getClassLoader().getResourceAsStream("net/farmingtales/ftboss/mixin/NativeCompoundTagMutationVersionMixin.class")) {
            if (raw == null || loader == null || source == null) return false;
            NativeMapFactoryProof287.verify(raw.readAllBytes(), loader.readAllBytes(), source.readAllBytes(), actual);
            return true;
        } catch (Exception failure) { return false; }
    }

    private static MethodNode composedNativeMapSite(ClassNode actual, MethodNode original, MethodNode observer) {
        List<AbstractInsnNode> instructions = executable(original);
        int callIndex = -1;
        for (int index = 0; index < instructions.size(); index++) if (call(instructions.get(index), observer, actual.name)) {
            if (callIndex >= 0) return null;
            callIndex = index;
        }
        if (callIndex < 13 || !original.tryCatchBlocks.isEmpty()
            || !(instructions.get(callIndex - 13) instanceof InvokeDynamicInsnNode inner)
            || !(instructions.get(callIndex - 1) instanceof InvokeDynamicInsnNode outer)) return null;
        String operation = "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;";
        String sam = "([Ljava/lang/Object;)Ljava/lang/Object;";
        if (!inner.name.equals("call") || !inner.desc.equals("()" + operation)
            || !outer.name.equals("call") || !outer.desc.equals("(L" + actual.name + ";" + operation + ")" + operation)
            || !outer.bsm.equals(inner.bsm) || outer.bsmArgs.length != 3
            || !Type.getType(sam).equals(outer.bsmArgs[0]) || !Type.getType(sam).equals(outer.bsmArgs[2])
            || !(outer.bsmArgs[1] instanceof Handle bridgeHandle) || bridgeHandle.getTag() != 7 || bridgeHandle.isInterface()
            || !bridgeHandle.getOwner().equals(actual.name) || !bridgeHandle.getDesc().equals("(" + operation + "[Ljava/lang/Object;)Ljava/lang/Object;")) return null;
        MethodNode bridge = exactMethod(actual, bridgeHandle.getName(), bridgeHandle.getDesc());
        if (bridge == null || bridge.access != 4098 || bridge.signature != null || !bridge.tryCatchBlocks.isEmpty()
            || bridge.exceptions != null && !bridge.exceptions.isEmpty() || !annotations(bridge).isEmpty()) return null;
        List<MethodNode> scopes = actual.methods.stream().filter(method -> method.name.endsWith("$ftboss$nativeMapPutScope280") && method.desc.equals(observer.desc)).toList();
        if (scopes.size() != 1) return null;
        MethodNode scope = scopes.getFirst();
        List<String> expectedBridge = new ArrayList<>(List.of("25:2", "16:3", "18:java.lang.String:[java.util.Map, java.lang.Object, java.lang.Object]", "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false", "25:0", "25:2"));
        String[] types = {"java/util/Map", "java/lang/Object", "java/lang/Object"};
        for (int index = 0; index < types.length; index++) expectedBridge.addAll(List.of("89:", "16:" + index, "50:", "192:" + types[index], "95:"));
        expectedBridge.addAll(List.of("87:", "25:1", "183:" + actual.name + ":" + member(scope.name) + ":" + scope.desc + ":false", "176:"));
        if (!expectedBridge.equals(body(bridge, actual.name, actual.name))) return null;
        int references = 0;
        for (MethodNode method : actual.methods) for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode invoked && invoked.owner.equals(actual.name) && invoked.name.equals(bridge.name) && invoked.desc.equals(bridge.desc)) return null;
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Handle handle && handle.equals(bridgeHandle)) return null;
            if (instruction instanceof InvokeDynamicInsnNode dynamic) for (Object argument : dynamic.bsmArgs) if (argument instanceof Handle handle && handle.equals(bridgeHandle)) references++;
        }
        if (references != 1) return null;
        int[] locals = new int[4];
        for (int index = 0; index < 4; index++) {
            if (!(instructions.get(callIndex - 12 + index) instanceof VarInsnNode store) || store.getOpcode() != 58 || store.var < Type.getArgumentsAndReturnSizes(original.desc) / 4 || store.var >= original.maxLocals) return null;
            locals[index] = store.var;
            for (int previous = 0; previous < index; previous++) if (locals[previous] == store.var) return null;
        }
        List<String> expected = List.of("58:" + locals[0], "58:" + locals[1], "58:" + locals[2], "58:" + locals[3], "87:", "25:0", "25:" + locals[3], "25:" + locals[2], "25:" + locals[1], "25:0", "25:" + locals[0]);
        if (!body(original, actual.name, actual.name).subList(callIndex - 12, callIndex - 1).equals(expected)) return null;
        for (AbstractInsnNode instruction : instructions) {
            if (instruction instanceof JumpInsnNode jump) {
                int target = position(original, jump.label);
                if (target > callIndex - 13 && target <= callIndex) return null;
            }
            if (instruction instanceof LookupSwitchInsnNode || instruction instanceof TableSwitchInsnNode) return null;
        }
        if (!nativePublicCompoundMap(actual)) return null;
        MethodNode normalized = new MethodNode(original.access, original.name, original.desc, original.signature, original.exceptions == null ? null : original.exceptions.toArray(String[]::new)); original.accept(normalized);
        List<AbstractInsnNode> copied = executable(normalized);
        for (int index = callIndex - 12; index < callIndex; index++) normalized.instructions.remove(copied.get(index));
        return normalized;
    }

    private static boolean mapSite(ClassNode var0, MethodNode var1, MethodNode var2, int var3, List<String> var4) {
        MethodNode composed = composedNativeMapSite(var0, var1, var2);
        if (composed != null) var1 = composed;
        if (var1.access == 1 && var1.tryCatchBlocks.isEmpty()) {
            List var5 = executable(var1);
            int var6 = -1;

            for (int var7 = 0; var7 < var5.size(); var7++) {
                if (var5.get(var7) instanceof MethodInsnNode var19 && var19.owner.equals("java/util/Map") && var19.name.equals("put")) {
                    return false;
                }

                if (call((AbstractInsnNode)var5.get(var7), var2, var0.name)) {
                    if (var6 >= 0 || !(var5.get(var7) instanceof MethodInsnNode var20) || var20.getOpcode() != 183 || var20.itf) {
                        return false;
                    }

                    var6 = var7;
                }
            }

            if (var6 >= 8 && var6 + 3 == var5.size() && var5.get(var6 - 1) instanceof InvokeDynamicInsnNode var17 && mapBridge(var0, var17, var4)) {
                List var18 = body(var1, var0.name, var0.name);
                ArrayList var9 = new ArrayList<>(List.of("25:0", "180:" + var0.name + ":tags:Ljava/util/Map;", "25:1"));
                if (var3 == 1) {
                    var9.addAll(List.of("22:2", "184:net/minecraft/nbt/LongTag:valueOf:(J)Lnet/minecraft/nbt/LongTag;:false"));
                } else {
                    var9.add("25:2");
                    if (var3 == 2) {
                        var9.add("184:net/minecraft/nbt/StringTag:valueOf:(Ljava/lang/String;)Lnet/minecraft/nbt/StringTag;:false");
                    }
                }

                int var10 = var6 - 8 - var9.size();
                if (var10 >= 0 && var18.subList(var10, var10 + var9.size()).equals(var9)) {
                    int[] var11 = new int[3];
                    int var12 = var3 == 1 ? 4 : 3;

                    for (int var13 = 0; var13 < 3; var13++) {
                        if (!(var5.get(var6 - 8 + var13) instanceof VarInsnNode var14)
                            || var14.getOpcode() != 58
                            || var14.var < var12
                            || var14.var >= var1.maxLocals) {
                            return false;
                        }

                        var11[var13] = var14.var;

                        for (int var26 = 0; var26 < var13; var26++) {
                            if (var11[var26] == var14.var) {
                                return false;
                            }
                        }
                    }

                    if (!((String)var18.get(var6 - 5)).equals("25:0")) {
                        return false;
                    } else {
                        for (int var23 = 0; var23 < 3; var23++) {
                            if (!((String)var18.get(var6 - 4 + var23)).equals("25:" + var11[2 - var23])) {
                                return false;
                            }
                        }

                        if (!var18.subList(var6 + 1, var18.size()).equals(var3 == 0 ? List.of("192:net/minecraft/nbt/Tag", "176:") : List.of("87:", "177:"))) {
                            return false;
                        } else {
                            for (AbstractInsnNode var25 : var1.instructions) {
                                if (!(var25 instanceof JumpInsnNode var27)) {
                                    if (var25 instanceof LookupSwitchInsnNode || var25 instanceof TableSwitchInsnNode) {
                                        return false;
                                    }
                                    continue;
                                }

                                int var16 = position(var1, var27.label);
                                if (var16 > var10 && var16 < var5.size()) {
                                    return false;
                                }
                            }

                            return true;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean mapWrapper(ClassNode var0, ClassNode var1, MethodNode var2, MethodNode var3) {
        String var4 = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
        AnnotationNode var5 = visibleAnnotation(var2, var4);
        List var6 = List.of(
            "put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
            "putLong(Ljava/lang/String;J)V",
            "putString(Ljava/lang/String;Ljava/lang/String;)V"
        );
        if (annotationKeys(var5, List.of("method", "at", "require", "expect", "allow", "remap"))
            && var6.equals(value(var5, "method"))
            && Integer.valueOf(3).equals(value(var5, "require"))
            && Integer.valueOf(3).equals(value(var5, "expect"))
            && Integer.valueOf(3).equals(value(var5, "allow"))
            && Boolean.FALSE.equals(value(var5, "remap"))
            && var2.access == 2
            && var3.access == 2
            && var2.desc
                .equals("(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Ljava/lang/Object;")
            && body(var2, var1.name, var0.name).equals(body(var3, var0.name, var0.name))) {
            if (value(var5, "at") instanceof List var8
                && var8.size() == 1
                && var8.get(0) instanceof AnnotationNode var9
                && annotationKeys(var9, List.of("value", "target", "remap"))
                && "INVOKE".equals(value(var9, "value"))
                && "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;".equals(value(var9, "target"))
                && Boolean.FALSE.equals(value(var9, "remap"))) {
                List var18 = var0.fields.stream().filter(var0x -> var0x.name.equals("tags") && var0x.desc.equals("Ljava/util/Map;")).toList();
                if (var18.size() == 1 && (((FieldNode)var18.get(0)).access == 18 || ((FieldNode)var18.get(0)).access == 17 && nativePublicCompoundMap(var0))) {
                    ArrayList var11 = new ArrayList();
                    ArrayList var12 = new ArrayList();

                    for (int var13 = 0; var13 < var6.size(); var13++) {
                        String var14 = (String)var6.get(var13);
                        List var15 = var0.methods.stream().filter(var1x -> matchesSelector(var1x, var14)).toList();
                        if (var15.size() != 1 || !mapSite(var0, (MethodNode)var15.get(0), var3, var13, var11)) {
                            return false;
                        }

                        var12.add((MethodNode)var15.get(0));
                    }

                    int var19 = 0;

                    for (MethodNode var21 : var0.methods) {
                        for (AbstractInsnNode var17 : var21.instructions) {
                            if (call(var17, var3, var0.name)) {
                                if (!var12.contains(var21)) {
                                    return false;
                                }

                                var19++;
                            }
                        }
                    }

                    return var19 == 3 && var11.size() == 3;
                }

                return false;
            }

            return false;
        } else {
            return false;
        }
    }

    static boolean lateTimerSite(String[] var0) {
        return var0 != null
            && var0.length == 4
            && "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin".equals(var0[0])
            && "net.farmingtales.ftboss.item.FoodWeaponEvolution".equals(var0[1])
            && "ftboss$r2$timerPut".equals(var0[2])
            && "ftboss$r2$timerPutSrg".equals(var0[3]);
    }

    static int lateSlot(String[] var0) {
        return lateMapSite(var0) ? 0 : (lateTimerSite(var0) ? 1 : ownerSlot(var0));
    }

    private static MethodNode timerHandler(ClassNode var0, ClassNode var1, String var2) {
        if (var1 == null) {
            return null;
        } else {
            String var3 = OwnerFieldInstallationCatalogue.descriptor(var1.name.replace('/', '.'), var2);
            if (var3 == null) {
                return null;
            } else {
                List var4 = var1.methods.stream().filter(var2x -> var2x.name.equals(var2) && var2x.desc.equals(var3)).toList();
                List var5 = var0.methods
                    .stream()
                    .filter(var3x -> member(var3x.name).equals(var2) && var3x.desc.equals(var3) && merged(var3x, var1.name.replace('/', '.')))
                    .toList();
                return var4.size() == 1
                        && var5.size() == 1
                        && ((MethodNode)var4.get(0)).access == 10
                        && ((MethodNode)var5.get(0)).access == 10
                        && body((MethodNode)var4.get(0), var1.name, var0.name).equals(body((MethodNode)var5.get(0), var0.name, var0.name))
                    ? (MethodNode)var5.get(0)
                    : null;
            }
        }
    }

    private static boolean timerGroup(MethodNode var0, boolean var1) {
        List var2 = annotations(var0).stream().filter(var0x -> var0x.desc.equals("Lorg/spongepowered/asm/mixin/injection/Group;")).toList();
        if (var2.size() == 1
            && annotationKeys((AnnotationNode)var2.get(0), List.of("name", "min", "max"))
            && (var1 ? "ftboss$actualCopySite" : "ftboss$r2$timerPut").equals(value((AnnotationNode)var2.get(0), "name"))
            && Integer.valueOf(1).equals(value((AnnotationNode)var2.get(0), "min"))
            && Integer.valueOf(1).equals(value((AnnotationNode)var2.get(0), "max"))) {
            AnnotationNode var3 = visibleAnnotation(var0, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
            if (annotationKeys(var3, List.of("method", "at", "require", "allow", "remap"))
                && List.of(
                        "writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"
                    )
                    .equals(value(var3, "method"))
                && Integer.valueOf(0).equals(value(var3, "require"))
                && Integer.valueOf(1).equals(value(var3, "allow"))
                && Boolean.FALSE.equals(value(var3, "remap"))) {
                if (value(var3, "at") instanceof List var5 && var5.size() == 1 && var5.get(0) instanceof AnnotationNode var6) {
                    return annotationKeys(var6, var1 ? List.of("value", "target", "remap") : List.of("value", "target"))
                        && "INVOKE".equals(value(var6, "value"))
                        && (!var1 || Boolean.FALSE.equals(value(var6, "remap")))
                        && (var1
                                ? "Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;"
                                : "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")
                            .equals(value(var6, "target"));
                }

                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static String timerOperation(int var0, boolean var1) {
        if (var0 == 0) {
            return "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:writableData:(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;)Lnet/minecraft/nbt/CompoundTag;:false";
        } else if (var0 == 1) {
            return "185:net/minecraft/nbt/Tag:" + (var1 ? "m_6426_" : "copy") + ":()Lnet/minecraft/nbt/Tag;:true";
        } else if (var0 == 2) {
            return "182:net/minecraft/nbt/CompoundTag:"
                + (var1 ? "m_128365_" : "put")
                + ":(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;:false";
        } else if (var0 == 3) {
            return "182:net/minecraft/nbt/CompoundTag:" + (var1 ? "m_128469_" : "getCompound") + ":(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;:false";
        } else if (var0 == 4) {
            return "182:net/minecraft/nbt/CompoundTag:" + (var1 ? "m_128356_" : "putLong") + ":(Ljava/lang/String;J)V:false";
        } else {
            return var0 == 5 ? "182:net/minecraft/server/level/ServerPlayer:getPersistentData:()Lnet/minecraft/nbt/CompoundTag;:false" : null;
        }
    }

    private static boolean timerBridge(ClassNode var0, InvokeDynamicInsnNode var1, int var2, boolean var3, List<String> var4) {
        String var5 = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String var6 = var2 == 4
            ? "([Ljava/lang/Object;)Ljava/lang/Void;"
            : "([Ljava/lang/Object;)Lnet/minecraft/nbt/" + (var2 != 0 && var2 != 3 && var2 != 5 ? "Tag;" : "CompoundTag;");
        String var7 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (var1.name.equals("call")
            && var1.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;")
            && var1.bsm.getTag() == 6
            && !var1.bsm.isInterface()
            && var1.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
            && var1.bsm.getName().equals("metafactory")
            && var1.bsm.getDesc().equals(var7)
            && var1.bsmArgs.length == 3
            && var1.bsmArgs[0] instanceof Type var8
            && var8.getDescriptor().equals(var5)
            && var1.bsmArgs[1] instanceof Handle var9
            && var9.getTag() == 6
            && !var9.isInterface()
            && var9.getOwner().equals(var0.name)
            && var9.getDesc().equals(var6)
            && !var4.contains(var9.getName())
            && var1.bsmArgs[2] instanceof Type var10
            && var10.getDescriptor().equals(var6)) {
            List var29 = var0.methods.stream().filter(var2x -> var2x.name.equals(var9.getName()) && var2x.desc.equals(var6)).toList();
            if (var29.size() != 1) {
                return false;
            } else {
                MethodNode var12 = (MethodNode)var29.get(0);
                if (var12.access == 4106
                    && var12.signature == null
                    && var12.tryCatchBlocks.isEmpty()
                    && (var12.exceptions == null || var12.exceptions.isEmpty())
                    && annotations(var12).isEmpty()) {
                    String[] var13 = var2 == 0
                        ? new String[]{"net/minecraft/world/item/ItemStack", "net/farmingtales/ftboss/item/FoodWeaponEvolution$Profile"}
                        : (
                            var2 == 1
                                ? new String[]{"net/minecraft/nbt/Tag"}
                                : (
                                    var2 == 3
                                        ? new String[]{"net/minecraft/nbt/CompoundTag", "java/lang/String"}
                                        : (
                                            var2 == 4
                                                ? new String[]{"net/minecraft/nbt/CompoundTag", "java/lang/String", "long"}
                                                : (
                                                    var2 == 5
                                                        ? new String[]{"net/minecraft/server/level/ServerPlayer"}
                                                        : new String[]{"net/minecraft/nbt/CompoundTag", "java/lang/String", "net/minecraft/nbt/Tag"}
                                                )
                                        )
                                )
                        );
                    ArrayList var14 = new ArrayList();

                    for (String var18 : var13) {
                        var14.add(var18.replace('/', '.'));
                    }

                    ArrayList var30 = new ArrayList<>(
                        List.of(
                            "25:0",
                            "16:" + var13.length,
                            "18:java.lang.String:[" + String.join(", ", var14) + "]",
                            "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false",
                            "25:0"
                        )
                    );

                    for (int var31 = 0; var31 < var13.length; var31++) {
                        var30.addAll(List.of("89:", "16:" + var31, "50:"));
                        if (var13[var31].equals("long")) {
                            var30.addAll(List.of("192:java/lang/Long", "182:java/lang/Long:longValue:()J:false", "93:", "88:"));
                        } else {
                            var30.addAll(List.of("192:" + var13[var31], "95:"));
                        }
                    }

                    var30.addAll(List.of("87:", timerOperation(var2, var3)));
                    if (var2 == 4) {
                        var30.addAll(List.of("1:", "192:java/lang/Void"));
                    }

                    var30.add("176:");
                    if (!body(var12, var0.name, var0.name).equals(var30)) {
                        return false;
                    } else {
                        int var32 = 0;

                        for (MethodNode var34 : var0.methods) {
                            for (AbstractInsnNode var20 : var34.instructions) {
                                if (var20 instanceof MethodInsnNode var21
                                    && var21.owner.equals(var0.name)
                                    && var21.name.equals(var9.getName())
                                    && var21.desc.equals(var6)) {
                                    return false;
                                }

                                if (var20 instanceof LdcInsnNode var35
                                    && var35.cst instanceof Handle var22
                                    && var22.getOwner().equals(var0.name)
                                    && var22.getName().equals(var9.getName())) {
                                    return false;
                                }

                                if (var20 instanceof InvokeDynamicInsnNode var36) {
                                    for (Object var26 : var36.bsmArgs) {
                                        if (var26 instanceof Handle) {
                                            Handle var37 = (Handle)var26;
                                            if (var37.getOwner().equals(var0.name) && var37.getName().equals(var9.getName()) && var37.getDesc().equals(var6)) {
                                                var32++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (var32 != 1) {
                            return false;
                        } else {
                            var4.add(var9.getName());
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static int[] timerWindow(ClassNode var0, MethodNode var1, int var2, MethodNode var3, int var4, boolean var5, int var6, List<String> var7) {
        List var8 = executable(var1);
        int var9 = var4 == 0 ? 2 : (var4 == 1 ? 1 : 3);
        int var10 = var4 == 2 ? 9 : 0;
        int var11 = var2 - var10;
        int var12 = var11 - 1;
        int var13 = var12 - var9;
        int var14 = var13 - var9;
        if (var14 >= 0
            && var8.get(var2) instanceof MethodInsnNode var15
            && var15.getOpcode() == 184
            && !var15.itf
            && call((AbstractInsnNode)var8.get(var2), var3, var0.name)) {
            int[] var22 = new int[var9];

            for (int var17 = 0; var17 < var9; var17++) {
                if (!(var8.get(var14 + var17) instanceof VarInsnNode var18) || var18.getOpcode() != 58 || var18.var < var6 || var18.var >= var1.maxLocals) {
                    return null;
                }

                var22[var17] = var18.var;

                for (int var26 = 0; var26 < var17; var26++) {
                    if (var22[var26] == var18.var) {
                        return null;
                    }
                }
            }

            for (int var24 = 0; var24 < var9; var24++) {
                if (!(var8.get(var14 + var9 + var24) instanceof VarInsnNode var23) || var23.getOpcode() != 25 || var23.var != var22[var9 - 1 - var24]) {
                    return null;
                }
            }

            if (var8.get(var12) instanceof InvokeDynamicInsnNode var25 && timerBridge(var0, var25, var4, var5, var7)) {
                for (int var20 = 0; var20 < var10; var20++) {
                    if (!(var8.get(var11 + var20) instanceof VarInsnNode var29) || var29.var != var20 || var29.getOpcode() != (var20 == 6 ? 21 : 25)) {
                        return null;
                    }
                }

                return new int[]{var14, var2};
            }

            return null;
        } else {
            return null;
        }
    }

    private static List<String> timerShape(ClassNode var0, MethodNode var1, List<int[]> var2, List<String> var3, int var4) {
        List var5 = executable(var1);
        if (var2.size() != var3.size()) {
            return null;
        } else {
            int var6 = -1;

            for (int[] var8 : var2) {
                if (var8.length != 2 || var8[0] <= var6 || var8[0] < 0 || var8[1] < var8[0] || var8[1] >= var5.size()) {
                    return null;
                }

                var6 = var8[1];
            }

            int[] var17 = new int[var5.size() + 1];
            Arrays.fill(var17, -1);
            int var18 = 0;
            int var9 = 0;
            int var10 = 0;

            while (var10 < var5.size()) {
                var17[var10] = var18++;
                if (var9 < var2.size() && ((int[])var2.get(var9))[0] == var10) {
                    var10 = ((int[])var2.get(var9++))[1] + 1;
                } else {
                    var10++;
                }
            }

            var17[var5.size()] = var18;
            List var20 = body(var1, var0.name, var0.name);
            ArrayList var11 = new ArrayList<>(List.of("locals:" + var4));
            var9 = 0;

            for (int var12 = 0; var12 < var5.size(); var12++) {
                if (var9 < var2.size() && ((int[])var2.get(var9))[0] == var12) {
                    var11.add((String)var3.get(var9));
                    var12 = ((int[])var2.get(var9++))[1];
                } else if (var5.get(var12) instanceof JumpInsnNode var13) {
                    int var23 = var17[position(var1, var13.label)];
                    if (var23 < 0) {
                        return null;
                    }

                    var11.add(var13.getOpcode() + ":" + var23);
                } else {
                    if (var5.get(var12) instanceof TableSwitchInsnNode || var5.get(var12) instanceof LookupSwitchInsnNode) {
                        return null;
                    }

                    var11.add((String)var20.get(var12));
                }
            }

            for (TryCatchBlockNode var22 : var1.tryCatchBlocks) {
                int var24 = var17[position(var1, var22.start)];
                int var15 = var17[position(var1, var22.end)];
                int var16 = var17[position(var1, var22.handler)];
                if (var24 < 0 || var15 < 0 || var16 < 0) {
                    return null;
                }

                var11.add("try:" + var22.type + ":" + var24 + ":" + var15 + ":" + var16);
            }

            return List.copyOf(var11);
        }
    }

    static List<String> timerParentSnapshot(ClassNode var0) {
        List var1 = var0.methods
            .stream()
            .filter(
                var0x -> matchesSelector(
                        var0x,
                        "writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"
                    )
            )
            .toList();
        if (var1.size() == 1 && ((MethodNode)var1.get(0)).access == 8) {
            MethodNode var2 = (MethodNode)var1.get(0);
            List var3 = executable(var2);
            List<String> var4 = body(var2, var0.name, var0.name);
            if (var3.size() >= 20
                && var4.subList(0, 2)
                    .equals(
                        List.of(
                            "25:5",
                            "184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:originalSequence:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;:false"
                        )
                    )
                && var3.get(2) instanceof VarInsnNode var5
                && var5.getOpcode() == 58
                && var5.var >= 9
                && var4.subList(3, 9)
                    .equals(
                        List.of(
                            "25:5",
                            "25:8",
                            "25:2",
                            "25:1",
                            "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:planHitTimerProfile:(Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;)Ljava/lang/String;:false",
                            "184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:beginOriginalTimer:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;:false"
                        )
                    )
                && var3.get(9) instanceof VarInsnNode var6
                && var6.getOpcode() == 58
                && var6.var >= 9
                && var6.var != var5.var) {
                int var30 = var4.indexOf(timerOperation(0, false));
                int var8 = var4.indexOf(timerOperation(1, false));
                if (var8 < 0) {
                    var8 = var4.indexOf(timerOperation(1, true));
                }

                int var9 = var4.indexOf(timerOperation(2, false));
                if (var9 < 0) {
                    var9 = var4.indexOf(timerOperation(2, true));
                }

                String var10 = "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;)V:false";
                int var11 = var4.indexOf("184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:timerOriginalPutReturned:" + var10);
                int var12 = var4.indexOf(
                    "182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:originalReturned:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;Ljava/lang/Object;)V:false"
                );
                int var13 = var4.indexOf(
                    "182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:originalThrown:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;Ljava/lang/Throwable;)V:false"
                );
                int var14 = var4.indexOf(
                    "184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:retainOriginalTimerThrowable:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Throwable;)V:false"
                );
                if (var30 >= 2
                    && var8 > var30
                    && var9 > var8
                    && var11 == var9 + 8
                    && var12 > var11
                    && var13 > var12
                    && var14 > var13
                    && var14 + 2 < var3.size()
                    && var4.subList(var30 - 2, var30).equals(List.of("25:0", "25:1"))
                    && ((String)var4.get(var8 - 1)).equals("25:3")
                    && var3.get(var30 + 1) instanceof VarInsnNode var15
                    && var15.getOpcode() == 58
                    && var3.get(var30 + 4) instanceof VarInsnNode baselineStore
                    && baselineStore.getOpcode() == 58
                    && baselineStore.var != var15.var
                    && var4.subList(var30 + 2, var30 + 4).equals(List.of("25:0", "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:data:(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/nbt/CompoundTag;:false"))
                    && var3.get(var8 + 1) instanceof VarInsnNode var16
                    && var16.getOpcode() == 58
                    && var4.subList(var9 - 3, var9).equals(List.of("25:" + var15.var, "25:2", "25:" + var16.var))
                    && var3.get(var9 + 1) instanceof VarInsnNode var17
                    && var17.getOpcode() == 58
                    && baselineStore.var != var16.var
                    && var4.subList(var9 + 2, var11).equals(List.of("25:0", "18:java.lang.String:FTBossFoodEvolution", "25:" + baselineStore.var, "25:" + var15.var, "184:net/farmingtales/ftboss/item/NativeLifecycleItemData:commit:(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V:false", "25:5"))
                    && java.util.Collections.frequency(var4, "184:net/farmingtales/ftboss/item/NativeLifecycleItemData:commit:(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V:false") == 1
                    && var4.subList(var12 - 3, var12).equals(List.of("25:" + var5.var, "25:" + var6.var, "25:" + var17.var))
                    && var3.get(var14 - 1) instanceof VarInsnNode var18
                    && var18.getOpcode() == 25
                    && ((String)var4.get(var14 - 2)).equals("25:5")
                    && var4.subList(var13 - 3, var13).equals(List.of("25:" + var5.var, "25:" + var6.var, "25:" + var18.var))
                    && var3.get(var13 - 4) instanceof JumpInsnNode var19
                    && var19.getOpcode() == 198
                    && position(var2, var19.label) == var14 - 2
                    && ((String)var4.get(var13 - 5)).equals("25:" + var5.var)
                    && var4.subList(var14 + 1, var14 + 3).equals(List.of("25:" + var18.var, "191:"))) {
                    for (int var35 = 0; var35 < 3; var35++) {
                        int var21 = 0;

                        for (String var23 : var4) {
                            if (var23.equals(timerOperation(var35, false)) || var23.equals(timerOperation(var35, true))) {
                                var21++;
                            }
                        }

                        if (var21 != 1) {
                            return null;
                        }
                    }

                    boolean var36 = false;
                    boolean var37 = false;

                    for (TryCatchBlockNode var39 : var2.tryCatchBlocks) {
                        int var24 = position(var2, var39.start);
                        int var25 = position(var2, var39.end);
                        int var26 = position(var2, var39.handler);
                        if (var24 <= var9 && var9 < var25 && var39.type == null) {
                            var37 = true;
                        }

                        if (var24 <= var9
                            && var9 < var25
                            && "java/lang/Throwable".equals(var39.type)
                            && var26 + 6 == var13
                            && var3.get(var26) instanceof VarInsnNode var27
                            && var27.getOpcode() == 58
                            && var27.var == var18.var) {
                            var36 = true;
                        }
                    }

                    return var36
                            && var37
                            && var4.stream().filter(var0x -> var0x.equals("177:")).count() == 1L
                            && var4.stream()
                                    .filter(
                                        var0x -> var0x.equals(
                                                "182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:close:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)V:false"
                                            )
                                    )
                                    .count()
                                >= 1L
                        ? timerShape(var0, var2, List.of(), List.of(), var2.maxLocals)
                        : null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static boolean timerWrapper(String[] var0, ClassNode var1, ClassNode var2, List<String> var3) {
        if (!lateTimerSite(var0)
            || var1 == null
            || var2 == null
            || !var2.name.replace('/', '.').equals(var0[0])
            || !var1.name.replace('/', '.').equals(var0[1])) {
            return false;
        } else if (var3 != null && !var3.isEmpty() && ((String)var3.get(0)).startsWith("locals:")) {
            int var4 = Integer.parseInt(((String)var3.get(0)).substring(7));
            MethodNode var5 = canonicalTimerHandler(var1, var2, false);
            ClassNode var6 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin", var1);
            MethodNode var7 = canonicalTimerHandler(var1, var6, true);
            if (var5 != null && var7 != null) {
                List var8 = var1.methods
                    .stream()
                    .filter(
                        var0x -> matchesSelector(
                                var0x,
                                "writePlanHitTimer(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;Ljava/lang/String;Lnet/minecraft/nbt/Tag;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterStep;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;ILnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;)V"
                            )
                    )
                    .toList();
                if (var8.size() == 1 && ((MethodNode)var8.get(0)).access == 8) {
                    MethodNode var9 = (MethodNode)var8.get(0);
                    List var10 = executable(var9);
                    ArrayList var11 = new ArrayList();
                    ArrayList var12 = new ArrayList();
                    ArrayList var13 = new ArrayList();
                    int[] var14 = new int[3];

                    for (int var15 = 0; var15 < var10.size(); var15++) {
                        if (var10.get(var15) instanceof MethodInsnNode var16 && var16.owner.equals(var1.name)) {
                            MethodNode var18;
                            byte var26;
                            if (call((AbstractInsnNode)var10.get(var15), var5, var1.name)) {
                                var26 = 2;
                                var18 = var5;
                            } else if (call((AbstractInsnNode)var10.get(var15), var7, var1.name)) {
                                var26 = 1;
                                var18 = var7;
                            } else {
                                String var19 = member(var16.name);
                                if (!var19.equals("ftboss$actualOriginalReceiver")) {
                                    continue;
                                }

                                var26 = 0;
                                ClassNode var20 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin", var1);
                                var18 = timerHandler(var1, var20, var19);
                                if (var18 == null || !call((AbstractInsnNode)var10.get(var15), var18, var1.name)) {
                                    return false;
                                }
                            }

                            String var31 = var26 == 0 ? timerOperation(0, false) : ownerOriginal(var3, var26 == 1 ? 3 : 4, 1);
                            if (var31 == null) {
                                return false;
                            }

                            boolean var33 = var26 != 0 && var31.equals(timerOperation(var26, true));
                            int[] var21 = timerWindow(var1, var9, var15, var18, var26, var33, var4, var13);
                            if (var21 == null) {
                                return false;
                            }

                            var14[var26]++;
                            var11.add(var21);
                            var12.add(var31);
                        }
                    }

                    for (int var29 : var14) {
                        if (var29 != 1) {
                            return false;
                        }
                    }

                    if (!var3.equals(timerShape(var1, var9, var11, var12, var4))) {
                        return false;
                    } else {
                        int var23 = 0;

                        for (MethodNode var28 : var1.methods) {
                            for (AbstractInsnNode var32 : var28.instructions) {
                                if (call(var32, var5, var1.name)) {
                                    if (var28 != var9) {
                                        return false;
                                    }

                                    var23++;
                                }
                            }
                        }

                        return var23 == 1;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean absentAlias(ClassNode var0, ClassNode var1, String var2) {
        return var1.methods.stream().noneMatch(var1x -> var1x.name.equals(var2)) && var0.methods.stream().noneMatch(var1x -> member(var1x.name).equals(var2));
    }

    private static MethodNode canonicalTimerHandler(ClassNode var0, ClassNode var1, boolean var2) {
        String var3 = "net/farmingtales/ftboss/mixin/" + (var2 ? "NativeExpectedTimerCopyMixin" : "NativeExplicitTimerPutMixin");
        String var4 = var2 ? "ftboss$actualOriginalCopy" : "ftboss$r2$timerPut";
        if (var1 != null && var1.name.equals(var3) && absentAlias(var0, var1, var4)) {
            MethodNode var5 = timerHandler(var0, var1, var4 + "Srg");
            if (var5 == null) {
                return null;
            } else {
                List var6 = var1.methods.stream().filter(var2x -> var2x.name.equals(var4 + "Srg") && var2x.desc.equals(var5.desc)).toList();
                if (var6.size() == 1 && timerGroup((MethodNode)var6.get(0), var2)) {
                    int var7 = 0;

                    for (MethodNode var9 : var0.methods) {
                        for (AbstractInsnNode var11 : var9.instructions) {
                            if (call(var11, var5, var0.name)) {
                                var7++;
                            }
                        }
                    }

                    return var7 == 1 ? var5 : null;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    static boolean canonicalAliasSite(String[] var0) {
        return var0 != null
            && (List.of(var0).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$actualOriginalReceiver")) ||
                List.of(var0)
                        .equals(
                            List.of(
                                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                                "net.farmingtales.ftboss.item.FoodWeaponRanged",
                                "ftboss$originalRangedPersistentData",
                                "ftboss$originalRangedCompound",
                                "ftboss$originalRangedCompoundSrg",
                                "ftboss$originalRangedLong",
                                "ftboss$originalRangedLongSrg",
                                "ftboss$originalRangedRoot",
                                "ftboss$originalRangedRootSrg"
                            )
                        )
                    || List.of(var0)
                        .equals(
                            List.of(
                                "net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin",
                                "net.farmingtales.ftboss.item.FoodWeaponEvolution",
                                "ftboss$actualOriginalCopy",
                                "ftboss$actualOriginalCopySrg"
                            )
                        )
            );
    }

    private static String rangedOperation(int var0, boolean var1) {
        return timerOperation(var0 == 0 ? 5 : (var0 == 1 ? 3 : (var0 == 2 ? 4 : 2)), var1);
    }

    private static String rangedOriginal(List<String> var0, int var1, int var2) {
        String var3 = null;
        int var4 = 0;

        for (String var6 : var0) {
            if (var6.equals(rangedOperation(var1, false)) || var6.equals(rangedOperation(var1, true))) {
                if (var3 != null && !var3.equals(var6)) {
                    return null;
                }

                var3 = var6;
                var4++;
            }
        }

        return var4 == var2 ? var3 : null;
    }

    private static List<List<String>> rangedParentSnapshot(ClassNode var0) {
        ArrayList var1 = new ArrayList();
        int[][] var2 = new int[][]{{1, 1, 1, 0}, {2, 1, 1, 1}};

        for (int var3 = 0; var3 < RANGED_PARENTS.size(); var3++) {
            MethodNode var4 = ownerParent(var0, RANGED_PARENTS.get(var3), 9);
            if (var4 == null) {
                return null;
            }

            List<String> var5 = body(var4, var0.name, var0.name);

            for (int var6 = 0; var6 < var2[var3].length; var6++) {
                if (var2[var3][var6] == 0) {
                    for (String var8 : var5) {
                        if (var8.equals(rangedOperation(var6, false)) || var8.equals(rangedOperation(var6, true))) {
                            return null;
                        }
                    }
                } else if (rangedOriginal(var5, var6, var2[var3][var6]) == null) {
                    return null;
                }
            }

            List var9 = timerShape(var0, var4, List.of(), List.of(), var4.maxLocals);
            if (var9 == null) {
                return null;
            }

            var1.add(var9);
        }

        return List.copyOf(var1);
    }

    private static boolean rangedAnnotation(MethodNode var0, int var1) {
        int var2 = var1 == 0 ? 3 : (var1 == 3 ? 1 : 2);
        List var3 = annotations(var0).stream().filter(var0x -> var0x.desc.equals("Lorg/spongepowered/asm/mixin/injection/Group;")).toList();
        if (var1 == 0 ? var3.isEmpty() : var3.size() == 1) {
            if (var1 == 0
                || annotationKeys((AnnotationNode)var3.get(0), List.of("name", "min", "max"))
                    && (var1 == 1 ? "ftboss$rangedCompoundSite" : (var1 == 2 ? "ftboss$rangedLongSite" : "ftboss$rangedRootSite"))
                        .equals(value((AnnotationNode)var3.get(0), "name"))
                    && Integer.valueOf(var2).equals(value((AnnotationNode)var3.get(0), "min"))
                    && Integer.valueOf(var2).equals(value((AnnotationNode)var3.get(0), "max"))) {
                AnnotationNode var4 = visibleAnnotation(var0, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
                if (annotationKeys(
                        var4, var1 == 0 ? List.of("method", "at", "require", "expect", "allow", "remap") : List.of("method", "at", "require", "allow", "remap")
                    )
                    && (var1 == 3 ? List.of(RANGED_PARENTS.get(1)) : RANGED_PARENTS).equals(value(var4, "method"))
                    && Integer.valueOf(var1 == 0 ? 3 : 0).equals(value(var4, "require"))
                    && Integer.valueOf(var2).equals(value(var4, "allow"))
                    && Boolean.FALSE.equals(value(var4, "remap"))
                    && (var1 != 0 || Integer.valueOf(3).equals(value(var4, "expect")))) {
                    if (value(var4, "at") instanceof List var6
                        && var6.size() == 1
                        && var6.get(0) instanceof AnnotationNode var7
                        && annotationKeys(var7, List.of("value", "target", "remap"))
                        && "INVOKE".equals(value(var7, "value"))
                        && Boolean.FALSE.equals(value(var7, "remap"))) {
                        String var9 = var1 == 0
                            ? "Lnet/minecraft/server/level/ServerPlayer;getPersistentData()Lnet/minecraft/nbt/CompoundTag;"
                            : (
                                var1 == 1
                                    ? "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
                                    : (
                                        var1 == 2
                                            ? "Lnet/minecraft/nbt/CompoundTag;putLong(Ljava/lang/String;J)V"
                                            : "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
                                    )
                            );
                        return var9.equals(value(var7, "target"));
                    }

                    return false;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static int[] rangedWindow(ClassNode var0, MethodNode var1, int var2, MethodNode var3, int var4, boolean var5, int var6, List<String> var7) {
        List var8 = executable(var1);
        int var9 = var4 == 0 ? 1 : (var4 == 1 ? 2 : 3);
        int var10 = var2 - 1;
        int var11 = var10 - 2 * var9;
        if (var11 >= 0 && var8.get(var2) instanceof MethodInsnNode var12 && var12.getOpcode() == 184 && !var12.itf && call(var12, var3, var0.name)) {
            int[] var20 = new int[var9];
            int[] var14 = new int[var9];

            for (int var15 = 0; var15 < var9; var15++) {
                boolean var16 = var4 == 2 && var15 == 0;
                int var17 = var16 ? 2 : 1;
                if (!(var8.get(var11 + var15) instanceof VarInsnNode var18)
                    || var18.getOpcode() != (var16 ? 55 : 58)
                    || var18.var < var6
                    || var18.var + var17 > var1.maxLocals) {
                    return null;
                }

                var20[var15] = var18.var;
                var14[var15] = var17;

                for (int var26 = 0; var26 < var15; var26++) {
                    if (var20[var26] < var18.var + var17 && var18.var < var20[var26] + var14[var26]) {
                        return null;
                    }
                }
            }

            for (int var22 = 0; var22 < var9; var22++) {
                if (!(var8.get(var11 + var9 + var22) instanceof VarInsnNode var21)
                    || var21.getOpcode() != (var4 == 2 && var22 == 2 ? 22 : 25)
                    || var21.var != var20[var9 - var22 - 1]) {
                    return null;
                }
            }

            if (var8.get(var10) instanceof InvokeDynamicInsnNode var23
                && timerBridge(var0, var23, var4 == 0 ? 5 : (var4 == 1 ? 3 : (var4 == 2 ? 4 : 2)), var5, var7)) {
                return new int[]{var11, var2};
            }

            return null;
        } else {
            return null;
        }
    }

    static boolean canonicalRangedWrapper(String[] var0, ClassNode var1, ClassNode var2, List<List<String>> var3) {
        if (canonicalAliasSite(var0)
            && var0[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin")
            && var1 != null
            && var2 != null
            && var2.name.replace('/', '.').equals(var0[0])
            && var1.name.replace('/', '.').equals(var0[1])
            && var3 != null
            && var3.size() == 2) {
            int[] var4 = new int[]{2, 4, 6, 8};
            int[] var5 = new int[]{3, 2, 2, 1};
            int[][] var6 = new int[][]{{1, 1, 1, 0}, {2, 1, 1, 1}};
            MethodNode[] var7 = new MethodNode[var4.length];

            for (int var8 = 0; var8 < var4.length; var8++) {
                String var9 = var0[var4[var8]];
                if (var8 != 0 && !absentAlias(var1, var2, var0[var4[var8] - 1])) {
                    return false;
                }

                var7[var8] = ownerHandler(var1, var2, var9, false);
                if (var7[var8] == null) {
                    return false;
                }

                List var10 = var2.methods
                    .stream()
                    .filter(var2x -> var2x.name.equals(var9) && var2x.desc.equals(OwnerFieldInstallationCatalogue.descriptor(var0[0], var9)))
                    .toList();
                if (var10.size() != 1 || !rangedAnnotation((MethodNode)var10.get(0), var8)) {
                    return false;
                }
            }

            int[] var22 = new int[var4.length];
            ArrayList var23 = new ArrayList();
            int var24 = 0;

            while (var24 < RANGED_PARENTS.size()) {
                List var11 = (List)var3.get(var24);
                if (var11 != null && !var11.isEmpty() && ((String)var11.get(0)).startsWith("locals:")) {
                    int var12 = Integer.parseInt(((String)var11.get(0)).substring(7));
                    MethodNode var13 = ownerParent(var1, RANGED_PARENTS.get(var24), 9);
                    if (var13 == null) {
                        return false;
                    }

                    List var14 = executable(var13);
                    ArrayList var15 = new ArrayList();
                    ArrayList var16 = new ArrayList();
                    int[] var17 = new int[var4.length];

                    for (int var18 = 0; var18 < var14.size(); var18++) {
                        for (int var19 = 0; var19 < var7.length; var19++) {
                            if (call((AbstractInsnNode)var14.get(var18), var7[var19], var1.name)) {
                                if (var6[var24][var19] == 0) {
                                    return false;
                                }

                                String var20 = rangedOriginal(var11, var19, var6[var24][var19]);
                                if (var20 == null) {
                                    return false;
                                }

                                int[] var21 = rangedWindow(var1, var13, var18, var7[var19], var19, var20.equals(rangedOperation(var19, true)), var12, var23);
                                if (var21 == null) {
                                    return false;
                                }

                                var15.add(var21);
                                var16.add(var20);
                                var17[var19]++;
                                var22[var19]++;
                            }
                        }
                    }

                    if (Arrays.equals(var17, var6[var24]) && var11.equals(timerShape(var1, var13, var15, var16, var12))) {
                        var24++;
                        continue;
                    }

                    return false;
                }

                return false;
            }

            for (int var25 = 0; var25 < var7.length; var25++) {
                int var26 = 0;

                for (MethodNode var28 : var1.methods) {
                    for (AbstractInsnNode var30 : var28.instructions) {
                        if (call(var30, var7[var25], var1.name)) {
                            var26++;
                        }
                    }
                }

                if (var22[var25] != var5[var25] || var26 != var5[var25]) {
                    return false;
                }
            }

            return var23.size() == 8;
        } else {
            return false;
        }
    }

    static int ownerSlot(String[] var0) {
        if (var0 == null) {
            return -1;
        } else if (var0.length == 6
            && var0[0].equals("net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin")
            && var0[1].equals("net.farmingtales.ftboss.item.FoodWeaponEvolution")
            && var0[2].equals("ftboss$ownerLocalDataCopy")
            && var0[3].equals("ftboss$ownerLocalBaselineCopy")
            && var0[4].equals("ftboss$ownerTargetReceiver")
            && var0[5].equals("ftboss$ownerTargetWrite")) {
            return 2;
        } else {
            return var0.length == 4
                    && var0[0].equals("net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin")
                    && var0[1].equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf")
                    && var0[2].equals("ftboss$ownerNativeCopy")
                    && var0[3].equals("ftboss$ownerNativePut")
                ? 3
                : -1;
        }
    }

    private static String ownerOperation(int var0, boolean var1) {
        if (var0 == 0) {
            return "182:net/minecraft/world/entity/LivingEntity:getPersistentData:()Lnet/minecraft/nbt/CompoundTag;:false";
        } else if (var0 == 1) {
            return "182:net/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder:write:(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V:false";
        } else if (var0 == 2) {
            return "182:net/minecraft/nbt/CompoundTag:" + (var1 ? "m_6426_" : "copy") + ":()Lnet/minecraft/nbt/CompoundTag;:false";
        } else if (var0 == 3) {
            return timerOperation(1, var1);
        } else {
            return var0 == 4
                ? timerOperation(2, var1)
                : "182:net/minecraft/nbt/CompoundTag:" + (var1 ? "m_128473_" : "remove") + ":(Ljava/lang/String;)V:false";
        }
    }

    private static String ownerOriginal(List<String> var0, int var1, int var2) {
        String var3 = null;
        int var4 = 0;

        for (String var6 : var0) {
            if (var6.equals(ownerOperation(var1, false)) || var6.equals(ownerOperation(var1, true))) {
                if (var3 != null && !var3.equals(var6)) {
                    return null;
                }

                var3 = var6;
                var4++;
            }
        }

        return var4 == var2 ? var3 : null;
    }

    private static MethodNode ownerParent(ClassNode var0, String var1, int var2) {
        List var3 = var0.methods.stream().filter(var1x -> matchesSelector(var1x, var1)).toList();
        return var3.size() == 1 && ((MethodNode)var3.get(0)).access == var2 ? (MethodNode)var3.get(0) : null;
    }

    private static MethodNode ownerHandler(ClassNode var0, ClassNode var1, String var2, boolean var3) {
        String var4 = OwnerFieldInstallationCatalogue.descriptor(var1.name.replace('/', '.'), var2);
        if (var4 == null) {
            return null;
        } else {
            List var5 = var1.methods.stream().filter(var2x -> var2x.name.equals(var2) && var2x.desc.equals(var4)).toList();
            List var6 = var0.methods
                .stream()
                .filter(var3x -> member(var3x.name).equals(var2) && var3x.desc.equals(var4) && merged(var3x, var1.name.replace('/', '.')))
                .toList();
            int var7 = var3 ? 2 : 10;
            return var5.size() == 1
                    && var6.size() == 1
                    && ((MethodNode)var5.get(0)).access == var7
                    && ((MethodNode)var6.get(0)).access == var7
                    && body((MethodNode)var5.get(0), var1.name, var0.name).equals(body((MethodNode)var6.get(0), var0.name, var0.name))
                ? (MethodNode)var6.get(0)
                : null;
        }
    }

    private static boolean ownerAnnotation(MethodNode var0, int var1, int var2) {
        AnnotationNode var3 = visibleAnnotation(var0, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        boolean var4 = var1 < 2;
        if (annotationKeys(var3, var4 ? List.of("method", "at", "require", "expect", "allow") : List.of("method", "at", "require"))
            && List.of(var4 ? "quoteFoodBlessingHits" : (var1 == 2 ? "quoteFinalWeaponHit" : "apply")).equals(value(var3, "method"))
            && Integer.valueOf(var1 == 0 ? 8 : (var1 == 1 ? 7 : 1)).equals(value(var3, "require"))) {
            if (!var4 || Integer.valueOf(var1 == 0 ? 8 : 7).equals(value(var3, "expect")) && Integer.valueOf(var1 == 0 ? 8 : 7).equals(value(var3, "allow"))) {
                if (value(var3, "at") instanceof List var6 && var6.size() == 1 && var6.get(0) instanceof AnnotationNode var7) {
                    List var14 = var1 == 1
                        ? List.of("value", "target")
                        : (var1 == 2 ? List.of("value", "target", "ordinal", "remap") : List.of("value", "target", "remap"));
                    String var9 = ownerOperation(var1, false);
                    int var10 = var9.indexOf(58, var9.indexOf(58) + 1);
                    String var11 = var9.substring(var9.indexOf(58) + 1, var10);
                    String var12 = var9.substring(var10 + 1, var9.lastIndexOf(58));
                    String var13 = "L" + var11 + ";" + var12.replaceFirst(":", "");
                    if (annotationKeys(var7, var14) && "INVOKE".equals(value(var7, "value")) && var13.equals(value(var7, "target"))) {
                        if (var1 != 1 && !Boolean.valueOf(var1 != 0).equals(value(var7, "remap"))) {
                            return false;
                        }

                        return var1 != 2 || Integer.valueOf(var2).equals(value(var7, "ordinal"));
                    }

                    return false;
                }

                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static List<String> ownerShape(ClassNode var0, MethodNode var1, List<int[]> var2, List<String> var3, int var4) {
        List var5 = executable(var1);
        if (var2.size() != var3.size()) {
            return null;
        } else {
            int var6 = -1;

            for (int[] var8 : var2) {
                if (var8.length != 2 || var8[0] <= var6 || var8[0] < 0 || var8[1] < var8[0] || var8[1] >= var5.size()) {
                    return null;
                }

                var6 = var8[1];
            }

            int[] var20 = new int[var5.size() + 1];
            Arrays.fill(var20, -1);
            int var21 = 0;
            int var9 = 0;
            int var10 = 0;

            while (var10 < var5.size()) {
                var20[var10] = var21++;
                if (var9 < var2.size() && ((int[])var2.get(var9))[0] == var10) {
                    var10 = ((int[])var2.get(var9++))[1] + 1;
                } else {
                    var10++;
                }
            }

            var20[var5.size()] = var21;
            List var23 = body(var1, var0.name, var0.name);
            ArrayList var11 = new ArrayList<>(List.of("locals:" + var4));
            var9 = 0;

            for (int var12 = 0; var12 < var5.size(); var12++) {
                if (var9 < var2.size() && ((int[])var2.get(var9))[0] == var12) {
                    var11.add((String)var3.get(var9));
                    var12 = ((int[])var2.get(var9++))[1];
                } else if (var5.get(var12) instanceof JumpInsnNode var13) {
                    int var34 = var20[position(var1, var13.label)];
                    if (var34 < 0) {
                        return null;
                    }

                    var11.add(var13.getOpcode() + ":" + var34);
                } else if (var5.get(var12) instanceof TableSwitchInsnNode var14) {
                    int var32 = var20[position(var1, var14.dflt)];
                    if (var32 < 0) {
                        return null;
                    }

                    StringBuilder var36 = new StringBuilder().append(var14.min).append(':').append(var14.max).append(':').append(var32);

                    for (LabelNode var38 : var14.labels) {
                        var32 = var20[position(var1, var38)];
                        if (var32 < 0) {
                            return null;
                        }

                        var36.append(':').append(var32);
                    }

                    var11.add(var14.getOpcode() + ":" + var36);
                } else if (!(var5.get(var12) instanceof LookupSwitchInsnNode var15)) {
                    var11.add((String)var23.get(var12));
                } else {
                    int var30 = var20[position(var1, var15.dflt)];
                    if (var30 < 0) {
                        return null;
                    }

                    StringBuilder var17 = new StringBuilder(var15.keys.toString()).append(':').append(var30);

                    for (LabelNode var19 : var15.labels) {
                        var30 = var20[position(var1, var19)];
                        if (var30 < 0) {
                            return null;
                        }

                        var17.append(':').append(var30);
                    }

                    var11.add(var15.getOpcode() + ":" + var17);
                }
            }

            for (TryCatchBlockNode var25 : var1.tryCatchBlocks) {
                int var26 = var20[position(var1, var25.start)];
                int var27 = var20[position(var1, var25.end)];
                int var35 = var20[position(var1, var25.handler)];
                if (var26 < 0 || var27 < 0 || var35 < 0) {
                    return null;
                }

                var11.add("try:" + var25.type + ":" + var26 + ":" + var27 + ":" + var35);
            }

            return List.copyOf(var11);
        }
    }

    private static boolean ownerBridge(ClassNode var0, InvokeDynamicInsnNode var1, int var2, String var3, List<String> var4) {
        String[] var5 = var2 == 8 ? new String[]{"net/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication", "net/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication"} : var2 == 9 ? new String[]{"com/mojang/serialization/Codec", "com/mojang/serialization/DynamicOps", "java/lang/Object"} : var2 == 6 ? new String[]{"net/farmingtales/ftboss/item/FoodWeaponHitPlan$TypedLeaf", "net/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit", "net/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge"} : var2 == 7 ? new String[]{"net/minecraft/nbt/CompoundTag"} : var2 == 5 ? new String[]{"net/minecraft/world/entity/Entity", "net/minecraft/nbt/CompoundTag"} : var2 == 0
            ? new String[]{"net/minecraft/world/entity/LivingEntity"}
            : (
                var2 == 1
                    ? new String[]{
                        "net/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder",
                        "net/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain",
                        "net/minecraft/nbt/CompoundTag",
                        "java/lang/String",
                        "net/minecraft/nbt/Tag"
                    }
                    : (
                        var2 == 2
                            ? new String[]{"net/minecraft/nbt/CompoundTag"}
                            : (
                                var2 == 3
                                    ? new String[]{"net/minecraft/nbt/Tag"}
                                    : new String[]{"net/minecraft/nbt/CompoundTag", "java/lang/String", "net/minecraft/nbt/Tag"}
                            )
                    )
            );
        String var6 = var2 == 9 ? "Lcom/mojang/serialization/DataResult;" : var2 == 7 ? "Ljava/lang/Boolean;" : var2 == 1 || var2 == 5 || var2 == 6 || var2 == 8 ? "Ljava/lang/Void;" : (var2 != 0 && var2 != 2 ? "Lnet/minecraft/nbt/Tag;" : "Lnet/minecraft/nbt/CompoundTag;");
        String var7 = "([Ljava/lang/Object;)" + var6;
        String var8 = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String var9 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (var1.name.equals("call")
            && var1.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;")
            && var1.bsm.getTag() == 6
            && !var1.bsm.isInterface()
            && var1.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
            && var1.bsm.getName().equals("metafactory")
            && var1.bsm.getDesc().equals(var9)
            && var1.bsmArgs.length == 3
            && var1.bsmArgs[0] instanceof Type var10
            && var10.getDescriptor().equals(var8)
            && var1.bsmArgs[1] instanceof Handle var11
            && var11.getTag() == 6
            && !var11.isInterface()
            && var11.getOwner().equals(var0.name)
            && var11.getDesc().equals(var7)
            && !var4.contains(var11.getName())
            && var1.bsmArgs[2] instanceof Type var12
            && var12.getDescriptor().equals(var7)) {
            List var30 = var0.methods.stream().filter(var2x -> var2x.name.equals(var11.getName()) && var2x.desc.equals(var7)).toList();
            if (var30.size() != 1) {
                return false;
            } else {
                MethodNode var14 = (MethodNode)var30.get(0);
                if (var14.access == 4106
                    && var14.signature == null
                    && var14.tryCatchBlocks.isEmpty()
                    && (var14.exceptions == null || var14.exceptions.isEmpty())
                    && annotations(var14).isEmpty()) {
                    ArrayList var15 = new ArrayList();

                    for (String var19 : var5) {
                        var15.add(var19.replace('/', '.'));
                    }

                    ArrayList var31 = new ArrayList<>(
                        List.of(
                            "25:0",
                            "16:" + var5.length,
                            "18:java.lang.String:[" + String.join(", ", var15) + "]",
                            "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false",
                            "25:0"
                        )
                    );

                    for (int var32 = 0; var32 < var5.length; var32++) {
                        var31.addAll(List.of("89:", "16:" + var32, "50:", "192:" + var5[var32], "95:"));
                    }

                    var31.addAll(List.of("87:", var3));
                    if (var2 == 1 || var2 == 5 || var2 == 6 || var2 == 8) {
                        var31.addAll(List.of("1:", "192:java/lang/Void"));
                    }

                    if (var2 == 7) var31.add("184:java/lang/Boolean:valueOf:(Z)Ljava/lang/Boolean;:false");
                    var31.add("176:");
                    if (!body(var14, var0.name, var0.name).equals(var31)) {
                        return false;
                    } else {
                        int var33 = 0;

                        for (MethodNode var35 : var0.methods) {
                            for (AbstractInsnNode var21 : var35.instructions) {
                                if (var21 instanceof MethodInsnNode var22
                                    && var22.owner.equals(var0.name)
                                    && var22.name.equals(var11.getName())
                                    && var22.desc.equals(var7)) {
                                    return false;
                                }

                                if (var21 instanceof LdcInsnNode var36
                                    && var36.cst instanceof Handle var23
                                    && var23.getOwner().equals(var0.name)
                                    && var23.getName().equals(var11.getName())) {
                                    return false;
                                }

                                if (var21 instanceof InvokeDynamicInsnNode var37) {
                                    for (Object var27 : var37.bsmArgs) {
                                        if (var27 instanceof Handle) {
                                            Handle var38 = (Handle)var27;
                                            if (var38.getOwner().equals(var0.name) && var38.getName().equals(var11.getName()) && var38.getDesc().equals(var7)) {
                                                var33++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (var33 != 1) {
                            return false;
                        } else {
                            var4.add(var11.getName());
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static int[] ownerWindow(ClassNode var0, MethodNode var1, int var2, MethodNode var3, int var4, String var5, int var6, List<String> var7) {
        List var8 = executable(var1);
        int var9 = var4 == 1 ? 5 : (var4 == 4 ? 3 : 1);
        boolean var10 = var4 >= 3;
        int[] var11 = var4 < 2 ? new int[]{0, 1, 2, 3, 4, 6, 7} : (var4 == 3 ? new int[]{1, 2} : new int[0]);
        int var12 = var2 - var11.length - 1;
        int var13 = var12 - var9;
        int var14 = var13 - (var10 ? 1 : 0);
        int var15 = var14 - var9;
        if (var15 >= 0
            && var8.get(var2) instanceof MethodInsnNode var16
            && var16.getOpcode() == (var10 ? 183 : 184)
            && !var16.itf
            && call((AbstractInsnNode)var8.get(var2), var3, var0.name)) {
            int[] var22 = new int[var9];
            int var18 = 0;

            while (var18 < var9) {
                if (var8.get(var15 + var18) instanceof VarInsnNode var19 && var19.getOpcode() == 58 && var19.var >= var6 && var19.var < var1.maxLocals) {
                    var22[var18] = var19.var;

                    for (int var28 = 0; var28 < var18; var28++) {
                        if (var22[var28] == var19.var) {
                            return null;
                        }
                    }

                    var18++;
                    continue;
                }

                return null;
            }

            for (int loadIndex = 0; loadIndex < var9; loadIndex++) {
                if (!(var8.get(var13 + loadIndex) instanceof VarInsnNode load) || load.getOpcode() != 25 || load.var != var22[var9 - loadIndex - 1]) return null;
            }
            if (var10 && (!(var8.get(var14) instanceof VarInsnNode var23) || var23.getOpcode() != 25 || var23.var != 0)) {
                return null;
            } else {
                if (var8.get(var12) instanceof InvokeDynamicInsnNode var24 && ownerBridge(var0, var24, var4, var5, var7)) {
                    for (int var30 = 0; var30 < var11.length; var30++) {
                        if (!(var8.get(var12 + 1 + var30) instanceof VarInsnNode var27)
                            || var27.var != var11[var30]
                            || var27.getOpcode() != (var4 < 2 && var30 == 4 ? 22 : 25)) {
                            return null;
                        }
                    }

                    return new int[]{var15, var2};
                }

                return null;
            }
        } else {
            return null;
        }
    }

    private static List<String> ownerSnapshot(ClassNode var0, int var1) {
        MethodNode var2 = ownerParent(
            var0,
            var1 == 0
                ? "quoteFoodBlessingHits(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V"
                : (
                    var1 == 1
                        ? "quoteFinalWeaponHit(Lnet/farmingtales/ftboss/event/NativeWeaponDamageEvent$BeforeMitigation;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;"
                        : "apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"
                ),
            var1 == 0 ? 10 : (var1 == 1 ? 9 : 0)
        );
        if (var2 == null) {
            return null;
        } else {
            List var3 = ownerShape(var0, var2, List.of(), List.of(), var2.maxLocals);
            if (var3 == null) {
                return null;
            } else {
                List var4 = executable(var2);
                List var5 = body(var2, var0.name, var0.name);
                if (var1 == 1) {
                    return ownerOriginal(var3, 2, 3) == null ? null : var3;
                } else if (var1 != 0) {
                    String var17 = ownerOriginal(var3, 3, 1);
                    String var18 = ownerOriginal(var3, 4, 1);
                    String var19 = ownerOriginal(var3, 5, 1);
                    if (var17 != null && var18 != null && var19 != null) {
                        int var20 = var5.indexOf(var17);
                        int var21 = var5.indexOf(var18);
                        int var22 = var5.indexOf(var19);
                        String var23 = "180:" + var0.name + ":data:Lnet/minecraft/nbt/CompoundTag;";
                        String var24 = "180:" + var0.name + ":key:Ljava/lang/String;";
                        return var20 >= 2
                                && var21 >= 5
                                && var22 >= 4
                                && var20 + 1 < var4.size()
                                && var21 + 1 < var4.size()
                                && var5.subList(var20 - 2, var20).equals(List.of("25:0", "180:" + var0.name + ":value:Lnet/minecraft/nbt/Tag;"))
                                && var4.get(var20 + 1) instanceof VarInsnNode var26
                                && var26.getOpcode() == 58
                                && var5.subList(var21 - 5, var21).equals(List.of("25:0", var23, "25:0", var24, "25:" + var26.var))
                                && var4.get(var21 + 1) instanceof VarInsnNode var28
                                && var28.getOpcode() == 58
                                && var5.subList(var22 - 4, var22).equals(List.of("25:0", var23, "25:0", var24))
                            ? var3
                            : null;
                    } else {
                        return null;
                    }
                } else {
                    String var6 = ownerOriginal(var3, 0, 8);
                    String var7 = ownerOriginal(var3, 1, 7);
                    if (var6 != null && var7 != null) {
                        int var8 = 0;
                        int var9 = 0;
                        int var10 = 0;

                        for (int var11 = 0; var11 < var4.size(); var11++) {
                            if (((String)var5.get(var11)).equals(var6) && (var11 < 1 || !((String)var5.get(var11 - 1)).equals("25:2"))) {
                                return null;
                            }

                            if (((String)var5.get(var11)).equals(var7)) {
                                int var12 = var11 - 1;

                                while (var12 >= 0 && var11 - var12 <= 64 && !((String)var5.get(var12)).equals("25:0")) {
                                    var12--;
                                }

                                if (var12 < 0 || var11 - var12 > 64 || var12 + 3 >= var11) {
                                    return null;
                                }

                                AbstractInsnNode var13 = (AbstractInsnNode)var4.get(var12 + 1);
                                if (var13.getOpcode() != 1
                                    && (!(var13 instanceof VarInsnNode var14) || var14.getOpcode() != 25)
                                    && (
                                        !(var13 instanceof FieldInsnNode var15)
                                            || var15.getOpcode() != 178
                                            || !var15.desc.equals("Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;")
                                    )) {
                                    return null;
                                }

                                if (((String)var5.get(var12 + 2)).equals("25:2") && ((String)var5.get(var12 + 3)).equals(var6)) {
                                    var8++;
                                } else {
                                    if (!((String)var5.get(var12 + 2)).equals("25:3")) {
                                        return null;
                                    }

                                    var9++;
                                }

                                if (((AbstractInsnNode)var4.get(var11 - 1)).getOpcode() == 1) {
                                    if (!((String)var5.get(var12 + 2)).equals("25:2")) {
                                        return null;
                                    }

                                    var10++;
                                } else if (!(var4.get(var11 - 1) instanceof MethodInsnNode var25)
                                    || var25.getOpcode() != 184
                                    || var25.itf
                                    || (
                                            !var25.owner.equals("net/minecraft/nbt/IntTag")
                                                || !var25.desc.equals("(I)Lnet/minecraft/nbt/IntTag;")
                                                || !var25.name.equals("valueOf") && !var25.name.equals("m_128679_")
                                        )
                                        && (
                                            !var25.owner.equals("net/minecraft/nbt/LongTag")
                                                || !var25.desc.equals("(J)Lnet/minecraft/nbt/LongTag;")
                                                || !var25.name.equals("valueOf") && !var25.name.equals("m_128882_")
                                        )) {
                                    return null;
                                }
                            }
                        }

                        return var8 == 5 && var9 == 2 && var10 == 2 ? var3 : null;
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    static List<List<String>> ownerParentsSnapshot(ClassNode var0) {
        if (var0.name.equals("net/farmingtales/ftboss/item/FoodWeaponRanged")) {
            return rangedParentSnapshot(var0);
        } else if (!var0.name.equals("net/farmingtales/ftboss/item/FoodWeaponEvolution")) {
            if (var0.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf")) {
                List var3 = ownerSnapshot(var0, 2);
                return var3 == null ? null : List.of(var3);
            } else {
                return null;
            }
        } else {
            List var1 = ownerSnapshot(var0, 0);
            List var2 = ownerSnapshot(var0, 1);
            return var1 != null && var2 != null ? List.of(var1, var2) : null;
        }
    }

    static boolean ownerWrapper(String[] var0, ClassNode var1, ClassNode var2, List<List<String>> var3) {
        int var4 = ownerSlot(var0);
        if (var4 >= 2
            && var1 != null
            && var2 != null
            && var3 != null
            && var3.size() == (var4 == 2 ? 2 : 1)
            && var0[0].equals(var2.name.replace('/', '.'))
            && var0[1].equals(var1.name.replace('/', '.'))) {
            int[] var5 = var4 == 2 ? new int[]{2, 2, 0, 1} : new int[]{3, 4};
            int[] var6 = var4 == 2 ? new int[]{1, 1, 8, 7} : new int[]{1, 1};
            MethodNode[] var7 = new MethodNode[var5.length];

            for (int var8 = 0; var8 < var7.length; var8++) {
                String var27 = var0[var8 + 2];
                var7[var8] = ownerHandler(var1, var2, var27, var4 == 3);
                if (var7[var8] == null) {
                    return false;
                }

                String var29 = OwnerFieldInstallationCatalogue.descriptor(var0[0], var27);
                List var31 = var2.methods.stream().filter(var2x -> var2x.name.equals(var27) && var2x.desc.equals(var29)).toList();
                if (var31.size() != 1 || !ownerAnnotation((MethodNode)var31.get(0), var5[var8], var8 + 1)) {
                    return false;
                }
            }

            int[] var26 = new int[var7.length];
            ArrayList var9 = new ArrayList();

            for (int var10 = 0; var10 < var3.size(); var10++) {
                List var11 = (List)var3.get(var10);
                if (var11 == null || var11.isEmpty() || !((String)var11.get(0)).startsWith("locals:")) {
                    return false;
                }

                int var12 = Integer.parseInt(((String)var11.get(0)).substring(7));
                String var13 = var4 == 3
                    ? "apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V"
                    : (
                        var10 == 0
                            ? "quoteFoodBlessingHits(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V"
                            : "quoteFinalWeaponHit(Lnet/farmingtales/ftboss/event/NativeWeaponDamageEvent$BeforeMitigation;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;"
                    );
                MethodNode var14 = ownerParent(var1, var13, var4 == 3 ? 0 : (var10 == 0 ? 10 : 9));
                if (var14 == null) {
                    return false;
                }

                List var15 = executable(var14);
                List var16 = body(var14, var1.name, var1.name);
                ArrayList var17 = new ArrayList();
                ArrayList var18 = new ArrayList();
                int var19 = 0;

                for (int var20 = 0; var20 < var15.size(); var20++) {
                    if (var4 == 2
                        && var10 == 1
                        && (((String)var16.get(var20)).equals(ownerOperation(2, false)) || ((String)var16.get(var20)).equals(ownerOperation(2, true)))) {
                        var19++;
                    }

                    for (int var21 = 0; var21 < var7.length; var21++) {
                        if (call((AbstractInsnNode)var15.get(var20), var7[var21], var1.name)) {
                            int var22 = var5[var21];
                            if (var4 == 2 && var10 == 0 != var22 < 2) {
                                return false;
                            }

                            if (var22 == 2 && var19++ != var21 + 1) {
                                return false;
                            }

                            int var23 = var4 == 2 ? (var22 == 0 ? 8 : (var22 == 1 ? 7 : 3)) : 1;
                            String var24 = ownerOriginal(var11, var22, var23);
                            if (var24 == null) {
                                return false;
                            }

                            int[] var25 = ownerWindow(var1, var14, var20, var7[var21], var22, var24, var12, var9);
                            if (var25 == null) {
                                return false;
                            }

                            var17.add(var25);
                            var18.add(var24);
                            var26[var21]++;
                        }
                    }
                }

                if (var4 == 2 && var10 == 1 && var19 != 3) {
                    return false;
                }

                if (!var11.equals(ownerShape(var1, var14, var17, var18, var12))) {
                    return false;
                }
            }

            for (int var28 = 0; var28 < var7.length; var28++) {
                if (var26[var28] != var6[var28]) {
                    return false;
                }

                int var30 = 0;

                for (MethodNode var33 : var1.methods) {
                    for (AbstractInsnNode var35 : var33.instructions) {
                        if (call(var35, var7[var28], var1.name)) {
                            var30++;
                        }
                    }
                }

                if (var30 != var6[var28]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    static boolean nativeAssignmentObservation185(String[] originalRequirement) {
        if (originalRequirement == null || !Arrays.asList(originalRequirement).equals(List.of(
            "net.farmingtales.ftboss.mixin.NativeExpectedTagAssignmentMixin",
            "net.minecraft.world.item.ItemStack","ftboss$originalTagAssigned"))) return false;
        String[] names = {"item/NativeExpectedFieldObservation","item/NativeTimerTransitionPlan","item/NativeLifecycleItemData","item/NativeItemStackInstallationProof165","item/NativeItemComponentObservation","item/NativeComponentCommitPlan","item/FoodWeaponOriginalSequence$Session","mixin/NativeItemComponentStackFieldsMixin","item/FoodWeaponData"};
        String[] hashes = {"adbf5bf4716d8377c3f66a3c47f9a96e4672616428af92c120e7059523884a3b","f6f14b8de27f689c9ef0854ed7eda17e0e9ac9edbab293263cfaed360705c4dc","81fe18f5bce311f94c38fba936f384b4b97da54574e47c69bebb09c3e1201f3b","3ad59e8594251413017afcd85331b5f9d7a1714157e69a64586422776d7b9ee5","7994a2a96a836e91447c89c75cd905edd942792edad320f3e7256537f31b5c38","88c482c97355057bf315342c0c73cd27414e0ccc187a0f13ebddfa1691a165a5","284abd24c2505281a80884a99e4610740862a88bb513c539b410d40750849809","f8f2691b395d1e520e71c7a78d99cb05087f85200d7623d0cb3b8575088ab71e","b0840052ca72ad4e5afa512c690e4fc2b983512bae0da9fb5d1d24df427a18f5"};
        for (int index = 0; index < names.length; index++) {
            try (var stream = OwnerFieldInstallationProof.class.getResourceAsStream("/net/farmingtales/ftboss/" + names[index] + ".class")) {
                if (stream == null || !java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(stream.readAllBytes())).equals(hashes[index])) return false;
            } catch (java.io.IOException | java.security.NoSuchAlgorithmException failure) {
                return false;
            }
        }
        return true;
    }

    static boolean complete(String[] var0, ClassNode var1, ClassNode var2) {
        if (var1 != null && var2 != null && var2.name.replace('/', '.').equals(var0[0]) && var1.name.replace('/', '.').equals(var0[1])) {
            if (nativeTickSite(var0)) return OwnerFieldCoverageRegistry.nativeTickComplete(var0, var1, var2);
            if (nativePutSlot(var0) >= 0) return OwnerFieldCoverageRegistry.nativePutComplete(var0, var1, var2);
            if (nativeCaptureSite(var0)) return OwnerFieldCoverageRegistry.nativeCaptureComplete(var0, var1, var2);
            if (nativeChainSlot(var0) >= 0) return OwnerFieldCoverageRegistry.nativeChainComplete(var0, var1, var2);
            if (nativeLoadSlot(var0) >= 0) return OwnerFieldCoverageRegistry.nativeLoadComplete(var0, var1, var2);
            if (nativeOperationSlot(var0) >= 0) return OwnerFieldCoverageRegistry.operationComplete(var0, var1, var2);
            if (entityOwnerSite(var0)) return entityLoadWrapper(var0, var1, var2, OwnerFieldCoverageRegistry.entityParent(var0, var1, var2));
            if (var0[0].equals("net.farmingtales.ftboss.mixin.OwnerNativeMaterialEntityIdentityMixin")) return nativeEntityIdentity(var0, var1, var2);
            if (observedFieldSite(var0)) return composedFieldView(var0, var1, var2, OwnerFieldCoverageRegistry.fieldSources(var0, var1));
            if (var0[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin")) {
                ClassNode timerSource = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", var1);
                return timerHandler(var1, var2, "ftboss$actualOriginalReceiver") != null
                    && timerWrapper(new String[]{"net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$r2$timerPut", "ftboss$r2$timerPutSrg"}, var1, timerSource, OwnerFieldCoverageRegistry.timerParent(var1, timerSource));
            }
            if (canonicalAliasSite(var0)) {
                if (var0[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin")) {
                    return canonicalRangedWrapper(var0, var1, var2, OwnerFieldCoverageRegistry.rangedParents(var0, var1, var2));
                } else if (canonicalTimerHandler(var1, var2, true) == null) {
                    return false;
                } else {
                    ClassNode var10 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", var1);
                    return timerWrapper(
                        new String[]{
                            "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin",
                            "net.farmingtales.ftboss.item.FoodWeaponEvolution",
                            "ftboss$r2$timerPut",
                            "ftboss$r2$timerPutSrg"
                        },
                        var1,
                        var10,
                        OwnerFieldCoverageRegistry.timerParent(var1, var10)
                    );
                }
            } else if (lateTimerSite(var0)) {
                return timerWrapper(var0, var1, var2, OwnerFieldCoverageRegistry.timerParent(var1, var2));
            } else if (ownerSlot(var0) >= 2) {
                return ownerWrapper(var0, var1, var2, OwnerFieldCoverageRegistry.ownerParents(var0, var1, var2));
            } else {
                for (int var3 = 2; var3 < var0.length; var3++) {
                    String var4 = var0[var3];
                    String var5 = OwnerFieldInstallationCatalogue.descriptor(var0[0], var4);
                    if (var5 == null) {
                        return false;
                    }

                    List var6 = var2.methods.stream().filter(var2x -> var2x.name.equals(var4) && var2x.desc.equals(var5)).toList();
                    List var7 = var1.methods
                        .stream()
                        .filter(var3x -> member(var3x.name).equals(var4) && var3x.desc.equals(var5) && merged(var3x, var0[0]))
                        .toList();
                    if (var6.size() != 1 || var7.size() != 1) {
                        return false;
                    }

                    MethodNode var8 = (MethodNode)var6.get(0);
                    MethodNode var9 = (MethodNode)var7.get(0);
                    if (annotations(var8).stream().anyMatch(var0x -> var0x.desc.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;"))) {
                        if (!accessorGetter(var0, var1, var2, var8, var9)) {
                            return false;
                        }
                    } else if (lateMapSite(var0)) {
                        if (!mapWrapper(var1, var2, var8, var9)) {
                            return false;
                        }
                    } else if (!sameLambdaBody(var8, var9, var2, var1) || !siteAndOrder(var1, var8, var9)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }
    static MethodNode nativeItemStackOperationBridge165(ClassNode host, InvokeDynamicInsnNode dynamic, String[] arguments, String result, String operation) {
        return nativeChainBridge(host, dynamic, arguments, result, operation, false);
    }


    private static ClassNode nativeKubeJsEntitySource270() {
        String resource = "dev/latvian/mods/kubejs/core/mixin/EntityMixin.class";
        try (java.io.InputStream stream = OwnerFieldInstallationProof.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) return null;
            byte[] bytes = stream.readAllBytes();
            String actual = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
            if (!actual.equals("fc4ba43ee6466430ccc8e1500cc8da6c1453bf12748514a73e9ebbdcb2690c9b")) return null;
            ClassNode source = new ClassNode();
            new org.objectweb.asm.ClassReader(bytes).accept(source, 0);
            return source;
        } catch (Exception failure) { return null; }
    }

    private static MethodNode nativeEntityExternalParent270(ClassNode host, MethodNode parent, MethodNode quote) {
        String origin = "dev.latvian.mods.kubejs.core.mixin.EntityMixin";
        List<MethodNode> external = new ArrayList<>();
        for (MethodNode candidate : host.methods) if (merged(candidate, origin)) external.add(candidate);
        if (external.isEmpty()) {
            for (FieldNode field : host.fields) if (field.name.equals("kjs$persistentData")) return null;
            return proofClone(parent);
        }
        ClassNode source = nativeKubeJsEntitySource270();
        if (source == null || !source.name.replace('/', '.').equals(origin) || !host.interfaces.containsAll(source.interfaces)) return null;
        int fields = 0;
        for (FieldNode field : host.fields) if (field.name.equals("kjs$persistentData")) {
            if (field.access != 2 || !field.desc.equals("Lnet/minecraft/nbt/CompoundTag;") || field.value != null) return null;
            fields++;
        }
        if (fields != 1) return null;
        List<MethodNode> verified = new ArrayList<>();
        MethodNode callback = null;
        for (MethodNode original : source.methods) {
            if (original.name.equals("<init>") || (original.access & 1024) != 0) continue;
            MethodNode actual = null;
            for (MethodNode candidate : external) {
                if (candidate.desc.equals(original.desc) && (candidate.name.equals(original.name) || candidate.name.endsWith("$kubejs$" + original.name))) {
                    if (actual != null) return null;
                    actual = candidate;
                }
            }
            if (actual == null || !mergedSession(actual, origin, quote) || !sameLambdaBody(original, actual, source, host) || actual.maxLocals != original.maxLocals || !actual.exceptions.equals(original.exceptions)) return null;
            verified.add(actual);
            if (original.name.equals("loadKJS")) {
                AnnotationNode inject = visibleAnnotation(original, "Lorg/spongepowered/asm/mixin/injection/Inject;");
                if (!annotationKeys(inject, List.of("method", "at")) || !List.of("load").equals(value(inject, "method")) || !(value(inject, "at") instanceof List<?> points) || points.size() != 1 || !(points.getFirst() instanceof AnnotationNode point) || !annotationKeys(point, List.of("value")) || !"RETURN".equals(value(point, "value"))) return null;
                callback = actual;
            }
        }
        if (verified.size() != external.size() || callback == null) return null;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(host.name) && field.name.equals("kjs$persistentData") && field.getOpcode() == 181 && !verified.contains(method)) return null;
        }
        MethodNode restored = proofClone(parent);
        List<AbstractInsnNode> instructions = executable(restored);
        List<AbstractInsnNode> removed = new ArrayList<>();
        int returns = 0;
        int invocations = 0;
        for (MethodNode method : host.methods) for (AbstractInsnNode instruction : method.instructions) if (call(instruction, callback, host.name)) {
            if (method != parent) return null;
            invocations++;
        }
        for (int index = 0; index < instructions.size(); index++) if (instructions.get(index).getOpcode() == 177) {
            if (index < 4 || !(instructions.get(index - 4) instanceof VarInsnNode self) || self.getOpcode() != 25 || self.var != 0 || !(instructions.get(index - 3) instanceof VarInsnNode tag) || tag.getOpcode() != 25 || tag.var != 1 || instructions.get(index - 2).getOpcode() != 1 || !call(instructions.get(index - 1), callback, host.name) || instructions.get(index - 1).getOpcode() != 183) return null;
            for (AbstractInsnNode instruction : instructions) {
                List<LabelNode> labels = new ArrayList<>();
                if (instruction instanceof JumpInsnNode jump) labels.add(jump.label);
                if (instruction instanceof TableSwitchInsnNode table) { labels.add(table.dflt); labels.addAll(table.labels); }
                if (instruction instanceof LookupSwitchInsnNode lookup) { labels.add(lookup.dflt); labels.addAll(lookup.labels); }
                for (LabelNode label : labels) { int target = position(restored, label); if (target > index - 4 && target <= index) return null; }
            }
            for (TryCatchBlockNode handler : restored.tryCatchBlocks) { int target = position(restored, handler.handler); if (target > index - 4 && target <= index) return null; }
            for (int offset = 4; offset >= 1; offset--) removed.add(instructions.get(index - offset));
            returns++;
        }
        if (returns == 0 || returns != invocations) return null;
        for (AbstractInsnNode instruction : removed) restored.instructions.remove(instruction);
        return restored;
    }
}
