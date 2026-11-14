/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Handle
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.AnnotationNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.FieldInsnNode
 *  org.objectweb.asm.tree.FieldNode
 *  org.objectweb.asm.tree.IincInsnNode
 *  org.objectweb.asm.tree.IntInsnNode
 *  org.objectweb.asm.tree.InvokeDynamicInsnNode
 *  org.objectweb.asm.tree.JumpInsnNode
 *  org.objectweb.asm.tree.LabelNode
 *  org.objectweb.asm.tree.LdcInsnNode
 *  org.objectweb.asm.tree.LookupSwitchInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.MultiANewArrayInsnNode
 *  org.objectweb.asm.tree.TableSwitchInsnNode
 *  org.objectweb.asm.tree.TryCatchBlockNode
 *  org.objectweb.asm.tree.TypeInsnNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package net.farmingtales.ftboss.item;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import net.farmingtales.ftboss.item.OwnerFieldInstallationCatalogue;
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
    private static final List<String> RANGED_PARENTS = List.of("prepareHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V", "saveHitTimers(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/nbt/CompoundTag;)V");
    private static final String OWNER_QUOTE = "quoteFoodBlessingHits(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/nbt/CompoundTag;JLnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)V";
    private static final String OWNER_FINAL = "quoteFinalWeaponHit(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;Lnet/farmingtales/ftboss/item/FoodWeaponBondHits$Hit;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$RandomQuotes;)Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan;";
    private static final String OWNER_APPLY = "apply(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;)V";

    private OwnerFieldInstallationProof() {
    }

    private static Object value(AnnotationNode annotationNode, String string) {
        if (annotationNode == null || annotationNode.values == null) {
            return null;
        }
        int n = 0;
        while (n + 1 < annotationNode.values.size()) {
            if (string.equals(annotationNode.values.get(n))) {
                return annotationNode.values.get(n + 1);
            }
            n += 2;
        }
        return null;
    }

    private static List<AnnotationNode> annotations(MethodNode methodNode) {
        ArrayList<AnnotationNode> arrayList = new ArrayList<AnnotationNode>();
        if (methodNode.visibleAnnotations != null) {
            arrayList.addAll(methodNode.visibleAnnotations);
        }
        if (methodNode.invisibleAnnotations != null) {
            arrayList.addAll(methodNode.invisibleAnnotations);
        }
        return arrayList;
    }

    private static boolean merged(MethodNode methodNode, String string) {
        return OwnerFieldInstallationProof.annotations(methodNode).stream().anyMatch(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") && string.equals(OwnerFieldInstallationProof.value(annotationNode, "mixin")));
    }

    private static String member(String string) {
        int n = string.indexOf("ftboss$");
        return n < 0 ? string : string.substring(n);
    }

    private static String owner(String string, String string2, String string3) {
        return string.equals(string2) ? string3 : string;
    }

    private static String constant(Object object, String string, String string2) {
        if (object instanceof Handle) {
            Handle handle = (Handle)object;
            return "handle:" + handle.getTag() + ":" + OwnerFieldInstallationProof.owner(handle.getOwner(), string, string2) + ":" + OwnerFieldInstallationProof.member(handle.getName()) + ":" + handle.getDesc() + ":" + handle.isInterface();
        }
        if (object instanceof Type) {
            Type type = (Type)object;
            return "type:" + type.getDescriptor().replace(string, string2);
        }
        return object == null ? "null" : object.getClass().getName() + ":" + object;
    }

    private static int position(MethodNode methodNode, AbstractInsnNode abstractInsnNode) {
        int n = 0;
        for (AbstractInsnNode abstractInsnNode2 = methodNode.instructions.getFirst(); abstractInsnNode2 != null; abstractInsnNode2 = abstractInsnNode2.getNext()) {
            if (abstractInsnNode2 == abstractInsnNode) {
                return n;
            }
            if (abstractInsnNode2.getOpcode() < 0) continue;
            ++n;
        }
        throw new IllegalArgumentException("Foreign instruction label");
    }

    private static List<String> body(MethodNode methodNode, String string, String string2) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Object object : methodNode.instructions) {
            if (object.getOpcode() < 0) continue;
            Object object2 = "";
            if (object instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)object;
                object2 = OwnerFieldInstallationProof.owner(fieldInsnNode.owner, string, string2) + ":" + OwnerFieldInstallationProof.member(fieldInsnNode.name) + ":" + fieldInsnNode.desc;
            } else if (object instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode)object;
                object2 = OwnerFieldInstallationProof.owner(methodInsnNode.owner, string, string2) + ":" + OwnerFieldInstallationProof.member(methodInsnNode.name) + ":" + methodInsnNode.desc + ":" + methodInsnNode.itf;
            } else if (object instanceof TypeInsnNode) {
                TypeInsnNode typeInsnNode = (TypeInsnNode)object;
                object2 = OwnerFieldInstallationProof.owner(typeInsnNode.desc, string, string2);
            } else if (object instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode)object;
                object2 = Integer.toString(varInsnNode.var);
            } else if (object instanceof IntInsnNode) {
                IntInsnNode intInsnNode = (IntInsnNode)object;
                object2 = Integer.toString(intInsnNode.operand);
            } else if (object instanceof IincInsnNode) {
                IincInsnNode iincInsnNode = (IincInsnNode)object;
                object2 = iincInsnNode.var + ":" + iincInsnNode.incr;
            } else if (object instanceof LdcInsnNode) {
                LdcInsnNode ldcInsnNode = (LdcInsnNode)object;
                object2 = OwnerFieldInstallationProof.constant(ldcInsnNode.cst, string, string2);
            } else if (object instanceof JumpInsnNode) {
                JumpInsnNode jumpInsnNode = (JumpInsnNode)object;
                object2 = Integer.toString(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)jumpInsnNode.label));
            } else if (object instanceof MultiANewArrayInsnNode) {
                MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode)object;
                object2 = multiANewArrayInsnNode.desc + ":" + multiANewArrayInsnNode.dims;
            } else if (object instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode)object;
                var19_19 = new StringBuilder(invokeDynamicInsnNode.name).append(':').append(invokeDynamicInsnNode.desc).append(':').append(OwnerFieldInstallationProof.constant(invokeDynamicInsnNode.bsm, string, string2));
                for (Object object3 : invokeDynamicInsnNode.bsmArgs) {
                    var19_19.append(':').append(OwnerFieldInstallationProof.constant(object3, string, string2));
                }
                object2 = var19_19.toString();
            } else if (object instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode)object;
                var19_19 = new StringBuilder().append(tableSwitchInsnNode.min).append(':').append(tableSwitchInsnNode.max).append(':').append(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tableSwitchInsnNode.dflt));
                for (LabelNode labelNode : tableSwitchInsnNode.labels) {
                    var19_19.append(':').append(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)labelNode));
                }
                object2 = var19_19.toString();
            } else if (object instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode)object;
                var19_19 = new StringBuilder(lookupSwitchInsnNode.keys.toString()).append(':').append(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)lookupSwitchInsnNode.dflt));
                for (LabelNode labelNode : lookupSwitchInsnNode.labels) {
                    var19_19.append(':').append(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)labelNode));
                }
                object2 = var19_19.toString();
            }
            arrayList.add(object.getOpcode() + ":" + (String)object2);
        }
        ArrayList arrayList2 = new ArrayList();
        for (Object object2 : methodNode.tryCatchBlocks) {
            arrayList2.add(OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)((TryCatchBlockNode)object2).start) + ":" + OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)((TryCatchBlockNode)object2).end) + ":" + OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)((TryCatchBlockNode)object2).handler) + ":" + ((TryCatchBlockNode)object2).type);
        }
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    private static boolean matchesSelector(MethodNode methodNode, String string) {
        int n = string.indexOf(40);
        return n < 0 ? methodNode.name.equals(string) : methodNode.name.equals(string.substring(0, n)) && methodNode.desc.equals(string.substring(n));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean call(AbstractInsnNode abstractInsnNode, MethodNode methodNode, String string) {
        if (!(abstractInsnNode instanceof MethodInsnNode)) return false;
        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
        if (!methodInsnNode.owner.equals(string)) return false;
        if (!methodInsnNode.name.equals(methodNode.name)) return false;
        if (!methodInsnNode.desc.equals(methodNode.desc)) return false;
        return true;
    }

    private static boolean straightReturnOrder(MethodNode methodNode, MethodNode methodNode2, String string) {
        boolean bl = false;
        boolean bl2 = false;
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode.getOpcode() < 0) {
                if (abstractInsnNode.getType() != 8) continue;
                bl = false;
                continue;
            }
            if (OwnerFieldInstallationProof.call(abstractInsnNode, methodNode2, string)) {
                bl = true;
            }
            if (abstractInsnNode instanceof JumpInsnNode || abstractInsnNode instanceof LookupSwitchInsnNode || abstractInsnNode instanceof TableSwitchInsnNode) {
                bl = false;
            }
            if (abstractInsnNode.getOpcode() < 172 || abstractInsnNode.getOpcode() > 177) continue;
            bl2 = true;
            if (!bl) {
                return false;
            }
            bl = false;
        }
        return bl2;
    }

    private static boolean straightHeadOrder(MethodNode methodNode, MethodNode methodNode2, String string) {
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (OwnerFieldInstallationProof.call(abstractInsnNode, methodNode2, string)) {
                return true;
            }
            if (abstractInsnNode instanceof JumpInsnNode || abstractInsnNode instanceof LookupSwitchInsnNode || abstractInsnNode instanceof TableSwitchInsnNode || abstractInsnNode instanceof FieldInsnNode || abstractInsnNode.getOpcode() == 191 || abstractInsnNode.getOpcode() >= 172 && abstractInsnNode.getOpcode() <= 177) {
                return false;
            }
            if (!(abstractInsnNode instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
            if (methodInsnNode.owner.startsWith("org/spongepowered/asm/mixin/injection/callback/")) continue;
            return false;
        }
        return false;
    }

    private static boolean siteAndOrder(ClassNode classNode, MethodNode methodNode, MethodNode methodNode2) {
        for (AnnotationNode annotationNode2 : OwnerFieldInstallationProof.annotations(methodNode)) {
            Object object;
            List list;
            block10: {
                block9: {
                    if (annotationNode2.desc.equals("Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;")) {
                        return false;
                    }
                    if (!annotationNode2.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) continue;
                    Object object2 = OwnerFieldInstallationProof.value(annotationNode2, "method");
                    Object object3 = OwnerFieldInstallationProof.value(annotationNode2, "at");
                    if (!(object2 instanceof List)) break block9;
                    list = (List)object2;
                    if (!(object3 instanceof List)) break block9;
                    List list2 = (List)object3;
                    if (!list.isEmpty() && list2.size() == 1 && (object = list2.get(0)) instanceof AnnotationNode) break block10;
                }
                return false;
            }
            AnnotationNode annotationNode3 = (AnnotationNode)object;
            object = String.valueOf(OwnerFieldInstallationProof.value(annotationNode3, "value"));
            if (!((String)object).equals("HEAD") && !((String)object).equals("RETURN")) {
                return false;
            }
            boolean bl = false;
            for (Object e : list) {
                if (!(e instanceof String)) {
                    return false;
                }
                String string = (String)e;
                for (MethodNode methodNode3 : classNode.methods) {
                    if (!OwnerFieldInstallationProof.matchesSelector(methodNode3, string)) continue;
                    bl = true;
                    if (!(((String)object).equals("HEAD") ? !OwnerFieldInstallationProof.straightHeadOrder(methodNode3, methodNode2, classNode.name) : !OwnerFieldInstallationProof.straightReturnOrder(methodNode3, methodNode2, classNode.name))) continue;
                    return false;
                }
            }
            return bl;
        }
        return OwnerFieldInstallationProof.annotations(methodNode).stream().anyMatch(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/Unique;"));
    }

    private static AnnotationNode visibleAnnotation(MethodNode methodNode, String string) {
        List<AnnotationNode> list = OwnerFieldInstallationProof.annotations(methodNode).stream().filter(annotationNode -> string.equals(annotationNode.desc)).toList();
        if (list.size() != 1 || methodNode.visibleAnnotations == null || !methodNode.visibleAnnotations.contains(list.get(0))) {
            return null;
        }
        return list.get(0);
    }

    private static boolean annotationKeys(AnnotationNode annotationNode, List<String> list) {
        if (annotationNode == null || annotationNode.values == null || annotationNode.values.size() % 2 != 0) {
            return false;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            String string;
            Object e = annotationNode.values.get(i);
            if (!(e instanceof String) || !list.contains(string = (String)e) || arrayList.contains(string)) {
                return false;
            }
            arrayList.add(string);
        }
        return true;
    }

    private static boolean accessorFieldName(ClassNode classNode, ClassNode classNode2, MethodNode methodNode, AnnotationNode annotationNode, String string) {
        String string2;
        Object object = OwnerFieldInstallationProof.value(annotationNode, "value");
        Object object2 = OwnerFieldInstallationProof.value(annotationNode, "remap");
        if (!(object instanceof String) || (string2 = (String)object).isEmpty() || object2 != null && !(object2 instanceof Boolean)) {
            return false;
        }
        if (string2.equals(string)) {
            return true;
        }
        return !Boolean.FALSE.equals(object2) && classNode2.name.equals("net/farmingtales/ftboss/mixin/OwnerFieldSourceAccessMixin") && classNode.name.equals("net/minecraft/world/damagesource/DamageSource") && methodNode.name.equals("ftboss$ownerFieldDirectEntity") && methodNode.desc.equals("()Lnet/minecraft/world/entity/Entity;") && string2.equals("directEntity") && string.equals("f_268595_");
    }

    private static boolean accessorGetter(String[] stringArray, ClassNode classNode, ClassNode classNode2, MethodNode methodNode, MethodNode methodNode2) {
        Object object;
        AbstractInsnNode abstractInsnNode;
        Object object2;
        String string;
        Object object3;
        String string2 = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
        AnnotationNode annotationNode = OwnerFieldInstallationProof.visibleAnnotation(methodNode, string2);
        AnnotationNode annotationNode2 = OwnerFieldInstallationProof.visibleAnnotation(methodNode2, string2);
        AnnotationNode annotationNode3 = OwnerFieldInstallationProof.visibleAnnotation(methodNode2, "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
        List<String> list = List.of("value", "remap", "target");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode, list) && OwnerFieldInstallationProof.annotationKeys(annotationNode2, list) && OwnerFieldInstallationProof.annotationKeys(annotationNode3, List.of("mixin", "priority", "sessionId")) && stringArray[0].equals(OwnerFieldInstallationProof.value(annotationNode3, "mixin")) && OwnerFieldInstallationProof.value(annotationNode3, "priority") instanceof Integer && (object3 = OwnerFieldInstallationProof.value(annotationNode3, "sessionId")) instanceof String && !(string = (String)object3).isEmpty())) {
            return false;
        }
        if (!((classNode2.access & 0x200) != 0 && classNode.interfaces != null && classNode.interfaces.contains(classNode2.name) && methodNode.access == 1025 && methodNode2.access == 4097 && methodNode2.name.equals(methodNode.name) && methodNode2.desc.equals(methodNode.desc) && methodNode.desc.startsWith("()") && !methodNode.desc.equals("()V") && methodNode2.signature == null && methodNode.tryCatchBlocks.isEmpty() && methodNode2.tryCatchBlocks.isEmpty() && (methodNode2.exceptions == null || methodNode2.exceptions.isEmpty()))) {
            return false;
        }
        object3 = methodNode.instructions.iterator();
        while (object3.hasNext()) {
            object2 = (AbstractInsnNode)object3.next();
            if (object2.getOpcode() < 0) continue;
            return false;
        }
        if (OwnerFieldInstallationProof.annotations(methodNode).size() != 1 || OwnerFieldInstallationProof.annotations(methodNode2).size() != 2 || !((object2 = OwnerFieldInstallationProof.value(annotationNode, "value")) instanceof String) || ((String)(object3 = (String)object2)).isEmpty() || !((String)object3).equals(OwnerFieldInstallationProof.value(annotationNode2, "value"))) {
            return false;
        }
        object2 = OwnerFieldInstallationProof.value(annotationNode, "remap");
        if (object2 == null ? OwnerFieldInstallationProof.value(annotationNode2, "remap") != null : !object2.equals(OwnerFieldInstallationProof.value(annotationNode2, "remap"))) {
            return false;
        }
        ArrayList<AbstractInsnNode> arrayList = new ArrayList<AbstractInsnNode>();
        ListIterator listIterator = methodNode2.instructions.iterator();
        while (listIterator.hasNext()) {
            abstractInsnNode = (AbstractInsnNode)listIterator.next();
            if (abstractInsnNode.getOpcode() < 0) continue;
            arrayList.add(abstractInsnNode);
        }
        if (!(arrayList.size() == 3 && (object = arrayList.get(0)) instanceof VarInsnNode && (listIterator = (VarInsnNode)object).getOpcode() == 25 && ((VarInsnNode)listIterator).var == 0 && (object = arrayList.get(1)) instanceof FieldInsnNode && (abstractInsnNode = (FieldInsnNode)object).getOpcode() == 180 && abstractInsnNode.owner.equals(classNode.name) && OwnerFieldInstallationProof.accessorFieldName(classNode, classNode2, methodNode, annotationNode, abstractInsnNode.name))) {
            return false;
        }
        object = Type.getReturnType((String)methodNode.desc);
        if (!abstractInsnNode.desc.equals(object.getDescriptor()) || ((AbstractInsnNode)arrayList.get(2)).getType() != 0 || ((AbstractInsnNode)arrayList.get(2)).getOpcode() != object.getOpcode(172) || methodNode2.maxLocals != object.getSize() || methodNode2.maxStack != object.getSize()) {
            return false;
        }
        String string3 = abstractInsnNode.name + ":" + abstractInsnNode.desc;
        if (!string3.equals(OwnerFieldInstallationProof.value(annotationNode2, "target")) || OwnerFieldInstallationProof.value(annotationNode, "target") != null && !string3.equals(OwnerFieldInstallationProof.value(annotationNode, "target"))) {
            return false;
        }
        List<FieldNode> list2 = classNode.fields.stream().filter(arg_0 -> OwnerFieldInstallationProof.lambda$accessorGetter$3((FieldInsnNode)abstractInsnNode, arg_0)).toList();
        return list2.size() == 1 && (list2.get((int)0).access == 2 || list2.get((int)0).access == 18);
    }

    static boolean lateMapSite(String[] stringArray) {
        return stringArray != null && stringArray.length == 3 && stringArray[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin") && stringArray[1].equals("net.minecraft.nbt.CompoundTag") && stringArray[2].equals("ftboss$originalFieldMapPut");
    }

    private static List<AbstractInsnNode> executable(MethodNode methodNode) {
        ArrayList<AbstractInsnNode> arrayList = new ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode.getOpcode() < 0) continue;
            arrayList.add(abstractInsnNode);
        }
        return arrayList;
    }

    private static boolean mapBridge(ClassNode classNode, InvokeDynamicInsnNode invokeDynamicInsnNode, List<String> list) {
        int n;
        Type type;
        Handle handle;
        Type type2;
        List<MethodNode> list2;
        String string = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String string2 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (!(invokeDynamicInsnNode.name.equals("call") && invokeDynamicInsnNode.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;") && invokeDynamicInsnNode.bsm.getTag() == 6 && !invokeDynamicInsnNode.bsm.isInterface() && invokeDynamicInsnNode.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && invokeDynamicInsnNode.bsm.getName().equals("metafactory") && invokeDynamicInsnNode.bsm.getDesc().equals(string2) && invokeDynamicInsnNode.bsmArgs.length == 3 && (list2 = invokeDynamicInsnNode.bsmArgs[0]) instanceof Type && (type2 = (Type)list2).getDescriptor().equals(string) && (list2 = invokeDynamicInsnNode.bsmArgs[1]) instanceof Handle && (handle = (Handle)list2).getTag() == 6 && !handle.isInterface() && handle.getOwner().equals(classNode.name) && handle.getDesc().equals(string) && (list2 = invokeDynamicInsnNode.bsmArgs[2]) instanceof Type && (type = (Type)list2).getDescriptor().equals(string) && !list.contains(handle.getName()))) {
            return false;
        }
        list2 = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(handle.getName()) && methodNode.desc.equals(handle.getDesc())).toList();
        if (list2.size() != 1) {
            return false;
        }
        MethodNode methodNode2 = list2.get(0);
        if (methodNode2.access != 4106 || methodNode2.signature != null || !methodNode2.tryCatchBlocks.isEmpty() || methodNode2.exceptions != null && !methodNode2.exceptions.isEmpty() || !OwnerFieldInstallationProof.annotations(methodNode2).isEmpty()) {
            return false;
        }
        ArrayList<String> arrayList = new ArrayList<String>(List.of("25:0", "16:3", "18:java.lang.String:[java.util.Map, java.lang.Object, java.lang.Object]", "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false", "25:0"));
        String[] stringArray = new String[]{"java/util/Map", "java/lang/Object", "java/lang/Object"};
        for (n = 0; n < stringArray.length; ++n) {
            arrayList.addAll(List.of("89:", "16:" + n, "50:", "192:" + stringArray[n], "95:"));
        }
        arrayList.addAll(List.of("87:", "185:java/util/Map:put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;:true", "176:"));
        if (!OwnerFieldInstallationProof.body(methodNode2, classNode.name, classNode.name).equals(arrayList)) {
            return false;
        }
        n = 0;
        for (MethodNode methodNode3 : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode3.instructions) {
                Handle handle2;
                MethodInsnNode methodInsnNode;
                if (abstractInsnNode instanceof MethodInsnNode) {
                    methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(handle.getName()) && methodInsnNode.desc.equals(string)) {
                        return false;
                    }
                }
                if (abstractInsnNode instanceof LdcInsnNode) {
                    methodInsnNode = (LdcInsnNode)abstractInsnNode;
                    Object[] objectArray = methodInsnNode.cst;
                    if (objectArray instanceof Handle && (handle2 = (Handle)objectArray).getOwner().equals(classNode.name) && handle2.getName().equals(handle.getName())) {
                        return false;
                    }
                }
                if (!(abstractInsnNode instanceof InvokeDynamicInsnNode)) continue;
                methodInsnNode = (InvokeDynamicInsnNode)abstractInsnNode;
                for (Object object : methodInsnNode.bsmArgs) {
                    if (!(object instanceof Handle) || !(handle2 = (Handle)object).getOwner().equals(classNode.name) || !handle2.getName().equals(handle.getName()) || !handle2.getDesc().equals(string)) continue;
                    ++n;
                }
            }
        }
        if (n != 1) {
            return false;
        }
        list.add(handle.getName());
        return true;
    }

    private static boolean mapSite(ClassNode classNode, MethodNode methodNode, MethodNode methodNode2, int n, List<String> list) {
        int n2;
        InvokeDynamicInsnNode invokeDynamicInsnNode;
        Object object;
        Object object2;
        if (methodNode.access != 1 || !methodNode.tryCatchBlocks.isEmpty()) {
            return false;
        }
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode);
        int n3 = -1;
        for (int i = 0; i < list2.size(); ++i) {
            object2 = list2.get(i);
            if (object2 instanceof MethodInsnNode) {
                object = (MethodInsnNode)object2;
                if (((MethodInsnNode)object).owner.equals("java/util/Map") && ((MethodInsnNode)object).name.equals("put")) {
                    return false;
                }
            }
            if (!OwnerFieldInstallationProof.call(list2.get(i), methodNode2, classNode.name)) continue;
            if (n3 >= 0 || !((object2 = list2.get(i)) instanceof MethodInsnNode) || (object = (MethodInsnNode)object2).getOpcode() != 183 || ((MethodInsnNode)object).itf) {
                return false;
            }
            n3 = i;
        }
        if (n3 < 8 || n3 + 3 != list2.size() || !((object = list2.get(n3 - 1)) instanceof InvokeDynamicInsnNode) || !OwnerFieldInstallationProof.mapBridge(classNode, invokeDynamicInsnNode = (InvokeDynamicInsnNode)object, list)) {
            return false;
        }
        object = OwnerFieldInstallationProof.body(methodNode, classNode.name, classNode.name);
        object2 = new ArrayList<String>(List.of("25:0", "180:" + classNode.name + ":f_128329_:Ljava/util/Map;", "25:1"));
        if (n == 1) {
            object2.addAll(List.of("22:2", "184:net/minecraft/nbt/LongTag:m_128882_:(J)Lnet/minecraft/nbt/LongTag;:false"));
        } else {
            object2.add("25:2");
            if (n == 2) {
                object2.add("184:net/minecraft/nbt/StringTag:m_129297_:(Ljava/lang/String;)Lnet/minecraft/nbt/StringTag;:false");
            }
        }
        int n4 = n3 - 8 - object2.size();
        if (n4 < 0 || !object.subList(n4, n4 + object2.size()).equals(object2)) {
            return false;
        }
        int[] nArray = new int[3];
        int n5 = n == 1 ? 4 : 3;
        for (n2 = 0; n2 < 3; ++n2) {
            VarInsnNode varInsnNode;
            AbstractInsnNode abstractInsnNode = list2.get(n3 - 8 + n2);
            if (!(abstractInsnNode instanceof VarInsnNode) || (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() != 58 || varInsnNode.var < n5 || varInsnNode.var >= methodNode.maxLocals) {
                return false;
            }
            nArray[n2] = varInsnNode.var;
            for (int i = 0; i < n2; ++i) {
                if (nArray[i] != varInsnNode.var) continue;
                return false;
            }
        }
        if (!((String)object.get(n3 - 5)).equals("25:0")) {
            return false;
        }
        for (n2 = 0; n2 < 3; ++n2) {
            if (((String)object.get(n3 - 4 + n2)).equals("25:" + nArray[2 - n2])) continue;
            return false;
        }
        if (!object.subList(n3 + 1, object.size()).equals(n == 0 ? List.of("192:net/minecraft/nbt/Tag", "176:") : List.of("87:", "177:"))) {
            return false;
        }
        for (VarInsnNode varInsnNode : methodNode.instructions) {
            if (varInsnNode instanceof JumpInsnNode) {
                JumpInsnNode jumpInsnNode = (JumpInsnNode)varInsnNode;
                int n6 = OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)jumpInsnNode.label);
                if (n6 <= n4 || n6 >= list2.size()) continue;
                return false;
            }
            if (!(varInsnNode instanceof LookupSwitchInsnNode) && !(varInsnNode instanceof TableSwitchInsnNode)) continue;
            return false;
        }
        return true;
    }

    private static boolean mapWrapper(ClassNode classNode, ClassNode classNode2, MethodNode methodNode2, MethodNode methodNode3) {
        int n;
        AnnotationNode annotationNode;
        Object object;
        List list;
        String string = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
        AnnotationNode annotationNode2 = OwnerFieldInstallationProof.visibleAnnotation(methodNode2, string);
        List<String> list2 = List.of("m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "m_128356_(Ljava/lang/String;J)V", "m_128359_(Ljava/lang/String;Ljava/lang/String;)V");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode2, List.of("method", "at", "require", "expect", "allow", "remap")) && list2.equals(OwnerFieldInstallationProof.value(annotationNode2, "method")) && Integer.valueOf(3).equals(OwnerFieldInstallationProof.value(annotationNode2, "require")) && Integer.valueOf(3).equals(OwnerFieldInstallationProof.value(annotationNode2, "expect")) && Integer.valueOf(3).equals(OwnerFieldInstallationProof.value(annotationNode2, "allow")) && Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode2, "remap")) && methodNode2.access == 2 && methodNode3.access == 2 && methodNode2.desc.equals("(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Ljava/lang/Object;") && OwnerFieldInstallationProof.body(methodNode2, classNode2.name, classNode.name).equals(OwnerFieldInstallationProof.body(methodNode3, classNode.name, classNode.name)))) {
            return false;
        }
        Object object2 = OwnerFieldInstallationProof.value(annotationNode2, "at");
        if (!(object2 instanceof List && (list = (List)object2).size() == 1 && (object = list.get(0)) instanceof AnnotationNode && OwnerFieldInstallationProof.annotationKeys(annotationNode = (AnnotationNode)object, List.of("value", "target", "remap")) && "INVOKE".equals(OwnerFieldInstallationProof.value(annotationNode, "value")) && "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;".equals(OwnerFieldInstallationProof.value(annotationNode, "target")) && Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode, "remap")))) {
            return false;
        }
        object = classNode.fields.stream().filter(fieldNode -> fieldNode.name.equals("f_128329_") && fieldNode.desc.equals("Ljava/util/Map;")).toList();
        if (object.size() != 1 || ((FieldNode)object.get((int)0)).access != 18) {
            return false;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        ArrayList<MethodNode> arrayList2 = new ArrayList<MethodNode>();
        for (n = 0; n < list2.size(); ++n) {
            String string2 = list2.get(n);
            MethodNode methodNode4 = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.matchesSelector(methodNode, string2)).toList();
            if (methodNode4.size() != 1 || !OwnerFieldInstallationProof.mapSite(classNode, (MethodNode)methodNode4.get(0), methodNode3, n, arrayList)) {
                return false;
            }
            arrayList2.add((MethodNode)methodNode4.get(0));
        }
        n = 0;
        for (MethodNode methodNode4 : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode4.instructions) {
                if (!OwnerFieldInstallationProof.call(abstractInsnNode, methodNode3, classNode.name)) continue;
                if (!arrayList2.contains(methodNode4)) {
                    return false;
                }
                ++n;
            }
        }
        return n == 3 && arrayList.size() == 3;
    }

    static boolean lateTimerSite(String[] stringArray) {
        return stringArray != null && stringArray.length == 4 && "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin".equals(stringArray[0]) && "net.farmingtales.ftboss.item.FoodWeaponEvolution".equals(stringArray[1]) && "ftboss$r2$timerPut".equals(stringArray[2]) && "ftboss$r2$timerPutSrg".equals(stringArray[3]);
    }

    static int lateSlot(String[] stringArray) {
        return OwnerFieldInstallationProof.lateMapSite(stringArray) ? 0 : (OwnerFieldInstallationProof.lateTimerSite(stringArray) ? 1 : OwnerFieldInstallationProof.ownerSlot(stringArray));
    }

    private static MethodNode timerHandler(ClassNode classNode, ClassNode classNode2, String string) {
        if (classNode2 == null) {
            return null;
        }
        String string2 = OwnerFieldInstallationCatalogue.descriptor(classNode2.name.replace('/', '.'), string);
        if (string2 == null) {
            return null;
        }
        List<MethodNode> list = classNode2.methods.stream().filter(methodNode -> methodNode.name.equals(string) && methodNode.desc.equals(string2)).toList();
        List<MethodNode> list2 = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.member(methodNode.name).equals(string) && methodNode.desc.equals(string2) && OwnerFieldInstallationProof.merged(methodNode, classNode.name.replace('/', '.'))).toList();
        if (list.size() != 1 || list2.size() != 1 || list.get((int)0).access != 10 || list2.get((int)0).access != 10 || !OwnerFieldInstallationProof.body(list.get(0), classNode2.name, classNode.name).equals(OwnerFieldInstallationProof.body(list2.get(0), classNode.name, classNode.name))) {
            return null;
        }
        return list2.get(0);
    }

    private static boolean timerGroup(MethodNode methodNode, boolean bl) {
        Object e;
        List list;
        List<AnnotationNode> list2 = OwnerFieldInstallationProof.annotations(methodNode).stream().filter(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/injection/Group;")).toList();
        if (!(list2.size() == 1 && OwnerFieldInstallationProof.annotationKeys(list2.get(0), List.of("name", "min", "max")) && (bl ? "ftboss$actualCopySite" : "ftboss$r2$timerPut").equals(OwnerFieldInstallationProof.value(list2.get(0), "name")) && Integer.valueOf(1).equals(OwnerFieldInstallationProof.value(list2.get(0), "min")) && Integer.valueOf(1).equals(OwnerFieldInstallationProof.value(list2.get(0), "max")))) {
            return false;
        }
        AnnotationNode annotationNode2 = OwnerFieldInstallationProof.visibleAnnotation(methodNode, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode2, List.of("method", "at", "require", "allow", "remap")) && List.of(TIMER_PARENT).equals(OwnerFieldInstallationProof.value(annotationNode2, "method")) && Integer.valueOf(0).equals(OwnerFieldInstallationProof.value(annotationNode2, "require")) && Integer.valueOf(1).equals(OwnerFieldInstallationProof.value(annotationNode2, "allow")) && Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode2, "remap")))) {
            return false;
        }
        Object object = OwnerFieldInstallationProof.value(annotationNode2, "at");
        if (!(object instanceof List) || (list = (List)object).size() != 1 || !((e = list.get(0)) instanceof AnnotationNode)) {
            return false;
        }
        AnnotationNode annotationNode3 = (AnnotationNode)e;
        return OwnerFieldInstallationProof.annotationKeys(annotationNode3, bl ? List.of("value", "target", "remap") : List.of("value", "target")) && "INVOKE".equals(OwnerFieldInstallationProof.value(annotationNode3, "value")) && (!bl || Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode3, "remap"))) && (bl ? "Lnet/minecraft/nbt/Tag;m_6426_()Lnet/minecraft/nbt/Tag;" : "Lnet/minecraft/nbt/CompoundTag;m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;").equals(OwnerFieldInstallationProof.value(annotationNode3, "target"));
    }

    private static String timerOperation(int n, boolean bl) {
        if (n == 0) {
            return "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:writableData:(Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;)Lnet/minecraft/nbt/CompoundTag;:false";
        }
        if (n == 1) {
            return "185:net/minecraft/nbt/Tag:" + (bl ? "m_6426_" : "copy") + ":()Lnet/minecraft/nbt/Tag;:true";
        }
        if (n == 2) {
            return "182:net/minecraft/nbt/CompoundTag:" + (bl ? "m_128365_" : "put") + ":(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;:false";
        }
        if (n == 3) {
            return "182:net/minecraft/nbt/CompoundTag:" + (bl ? "m_128469_" : "getCompound") + ":(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;:false";
        }
        if (n == 4) {
            return "182:net/minecraft/nbt/CompoundTag:" + (bl ? "m_128356_" : "putLong") + ":(Ljava/lang/String;J)V:false";
        }
        return n == 5 ? "182:net/minecraft/server/level/ServerPlayer:getPersistentData:()Lnet/minecraft/nbt/CompoundTag;:false" : null;
    }

    private static boolean timerBridge(ClassNode classNode, InvokeDynamicInsnNode invokeDynamicInsnNode, int n, boolean bl, List<String> list) {
        int n2;
        String[] stringArray;
        Type type;
        Handle handle;
        Type type2;
        List<MethodNode> list2;
        String string = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String string2 = n == 4 ? "([Ljava/lang/Object;)Ljava/lang/Void;" : "([Ljava/lang/Object;)Lnet/minecraft/nbt/" + (n == 0 || n == 3 || n == 5 ? "CompoundTag;" : "Tag;");
        String string3 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (!(invokeDynamicInsnNode.name.equals("call") && invokeDynamicInsnNode.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;") && invokeDynamicInsnNode.bsm.getTag() == 6 && !invokeDynamicInsnNode.bsm.isInterface() && invokeDynamicInsnNode.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && invokeDynamicInsnNode.bsm.getName().equals("metafactory") && invokeDynamicInsnNode.bsm.getDesc().equals(string3) && invokeDynamicInsnNode.bsmArgs.length == 3 && (list2 = invokeDynamicInsnNode.bsmArgs[0]) instanceof Type && (type2 = (Type)list2).getDescriptor().equals(string) && (list2 = invokeDynamicInsnNode.bsmArgs[1]) instanceof Handle && (handle = (Handle)list2).getTag() == 6 && !handle.isInterface() && handle.getOwner().equals(classNode.name) && handle.getDesc().equals(string2) && !list.contains(handle.getName()) && (list2 = invokeDynamicInsnNode.bsmArgs[2]) instanceof Type && (type = (Type)list2).getDescriptor().equals(string2))) {
            return false;
        }
        list2 = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(handle.getName()) && methodNode.desc.equals(string2)).toList();
        if (list2.size() != 1) {
            return false;
        }
        MethodNode methodNode2 = list2.get(0);
        if (methodNode2.access != 4106 || methodNode2.signature != null || !methodNode2.tryCatchBlocks.isEmpty() || methodNode2.exceptions != null && !methodNode2.exceptions.isEmpty() || !OwnerFieldInstallationProof.annotations(methodNode2).isEmpty()) {
            return false;
        }
        if (n == 0) {
            String[] stringArray2 = new String[2];
            stringArray2[0] = "net/minecraft/world/item/ItemStack";
            stringArray = stringArray2;
            stringArray2[1] = "net/farmingtales/ftboss/item/FoodWeaponEvolution$Profile";
        } else if (n == 1) {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = "net/minecraft/nbt/Tag";
        } else if (n == 3) {
            String[] stringArray4 = new String[2];
            stringArray4[0] = "net/minecraft/nbt/CompoundTag";
            stringArray = stringArray4;
            stringArray4[1] = "java/lang/String";
        } else if (n == 4) {
            String[] stringArray5 = new String[3];
            stringArray5[0] = "net/minecraft/nbt/CompoundTag";
            stringArray5[1] = "java/lang/String";
            stringArray = stringArray5;
            stringArray5[2] = "long";
        } else if (n == 5) {
            String[] stringArray6 = new String[1];
            stringArray = stringArray6;
            stringArray6[0] = "net/minecraft/server/level/ServerPlayer";
        } else {
            String[] stringArray7 = new String[3];
            stringArray7[0] = "net/minecraft/nbt/CompoundTag";
            stringArray7[1] = "java/lang/String";
            stringArray = stringArray7;
            stringArray7[2] = "net/minecraft/nbt/Tag";
        }
        String[] stringArray8 = stringArray;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string4 : stringArray8) {
            arrayList.add(string4.replace('/', '.'));
        }
        ArrayList<String> arrayList2 = new ArrayList<String>(List.of("25:0", "16:" + stringArray8.length, "18:java.lang.String:[" + String.join((CharSequence)", ", arrayList) + "]", "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false", "25:0"));
        for (n2 = 0; n2 < stringArray8.length; ++n2) {
            arrayList2.addAll(List.of("89:", "16:" + n2, "50:"));
            if (stringArray8[n2].equals("long")) {
                arrayList2.addAll(List.of("192:java/lang/Long", "182:java/lang/Long:longValue:()J:false", "93:", "88:"));
                continue;
            }
            arrayList2.addAll(List.of("192:" + stringArray8[n2], "95:"));
        }
        arrayList2.addAll(List.of("87:", OwnerFieldInstallationProof.timerOperation(n, bl)));
        if (n == 4) {
            arrayList2.addAll(List.of("1:", "192:java/lang/Void"));
        }
        arrayList2.add("176:");
        if (!OwnerFieldInstallationProof.body(methodNode2, classNode.name, classNode.name).equals(arrayList2)) {
            return false;
        }
        n2 = 0;
        for (String string4 : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : ((MethodNode)string4).instructions) {
                Handle handle2;
                MethodInsnNode methodInsnNode;
                if (abstractInsnNode instanceof MethodInsnNode) {
                    methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(handle.getName()) && methodInsnNode.desc.equals(string2)) {
                        return false;
                    }
                }
                if (abstractInsnNode instanceof LdcInsnNode) {
                    methodInsnNode = (LdcInsnNode)abstractInsnNode;
                    Object[] objectArray = methodInsnNode.cst;
                    if (objectArray instanceof Handle && (handle2 = (Handle)objectArray).getOwner().equals(classNode.name) && handle2.getName().equals(handle.getName())) {
                        return false;
                    }
                }
                if (!(abstractInsnNode instanceof InvokeDynamicInsnNode)) continue;
                methodInsnNode = (InvokeDynamicInsnNode)abstractInsnNode;
                for (Object object : methodInsnNode.bsmArgs) {
                    if (!(object instanceof Handle) || !(handle2 = (Handle)object).getOwner().equals(classNode.name) || !handle2.getName().equals(handle.getName()) || !handle2.getDesc().equals(string2)) continue;
                    ++n2;
                }
            }
        }
        if (n2 != 1) {
            return false;
        }
        list.add(handle.getName());
        return true;
    }

    private static int[] timerWindow(ClassNode classNode, MethodNode methodNode, int n, MethodNode methodNode2, int n2, boolean bl, int n3, List<String> list) {
        InvokeDynamicInsnNode invokeDynamicInsnNode;
        MethodInsnNode methodInsnNode;
        Object object;
        int n4;
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode);
        int n5 = n2 == 2 ? 9 : 0;
        int n6 = n - n5;
        int n7 = n6 - 1;
        int n8 = n7 - (n4 = n2 == 0 ? 2 : (n2 == 1 ? 1 : 3));
        int n9 = n8 - n4;
        if (n9 < 0 || !((object = list2.get(n)) instanceof MethodInsnNode) || (methodInsnNode = (MethodInsnNode)object).getOpcode() != 184 || methodInsnNode.itf || !OwnerFieldInstallationProof.call(list2.get(n), methodNode2, classNode.name)) {
            return null;
        }
        object = new int[n4];
        for (int i = 0; i < n4; ++i) {
            VarInsnNode varInsnNode;
            AbstractInsnNode abstractInsnNode = list2.get(n9 + i);
            if (!(abstractInsnNode instanceof VarInsnNode) || (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() != 58 || varInsnNode.var < n3 || varInsnNode.var >= methodNode.maxLocals) {
                return null;
            }
            object[i] = (AbstractInsnNode)varInsnNode.var;
            for (int j = 0; j < i; ++j) {
                if (object[j] != varInsnNode.var) continue;
                return null;
            }
        }
        for (int i = 0; i < n4; ++i) {
            VarInsnNode varInsnNode;
            AbstractInsnNode abstractInsnNode = list2.get(n9 + n4 + i);
            if (abstractInsnNode instanceof VarInsnNode && (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() == 25 && varInsnNode.var == object[n4 - 1 - i]) continue;
            return null;
        }
        AbstractInsnNode abstractInsnNode = list2.get(n7);
        if (!(abstractInsnNode instanceof InvokeDynamicInsnNode) || !OwnerFieldInstallationProof.timerBridge(classNode, invokeDynamicInsnNode = (InvokeDynamicInsnNode)abstractInsnNode, n2, bl, list)) {
            return null;
        }
        for (int i = 0; i < n5; ++i) {
            AbstractInsnNode abstractInsnNode2 = list2.get(n6 + i);
            if (abstractInsnNode2 instanceof VarInsnNode) {
                abstractInsnNode = (VarInsnNode)abstractInsnNode2;
                if (abstractInsnNode.var == i && abstractInsnNode.getOpcode() == (i == 6 ? 21 : 25)) continue;
            }
            return null;
        }
        return new int[]{n9, n};
    }

    private static List<String> timerShape(ClassNode classNode, MethodNode methodNode, List<int[]> list, List<String> list2, int n) {
        List<AbstractInsnNode> list3 = OwnerFieldInstallationProof.executable(methodNode);
        if (list.size() != list2.size()) {
            return null;
        }
        int n2 = -1;
        for (int[] nArray : list) {
            if (nArray.length != 2 || nArray[0] <= n2 || nArray[0] < 0 || nArray[1] < nArray[0] || nArray[1] >= list3.size()) {
                return null;
            }
            n2 = nArray[1];
        }
        Object object = new int[list3.size() + 1];
        Arrays.fill((int[])object, -1);
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        while (n5 < list3.size()) {
            object[n5] = n3++;
            if (n4 < list.size() && list.get(n4)[0] == n5) {
                n5 = list.get(n4++)[1] + 1;
                continue;
            }
            ++n5;
        }
        object[list3.size()] = n3;
        List<String> list4 = OwnerFieldInstallationProof.body(methodNode, classNode.name, classNode.name);
        ArrayList<CallSite> arrayList = new ArrayList<CallSite>(List.of("locals:" + n));
        n4 = 0;
        for (int i = 0; i < list3.size(); ++i) {
            if (n4 < list.size() && list.get(n4)[0] == i) {
                arrayList.add((CallSite)((Object)list2.get(n4)));
                i = list.get(n4++)[1];
                continue;
            }
            AbstractInsnNode abstractInsnNode = list3.get(i);
            if (abstractInsnNode instanceof JumpInsnNode) {
                TryCatchBlockNode tryCatchBlockNode = (JumpInsnNode)abstractInsnNode;
                Object object2 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.label)];
                if (object2 < 0) {
                    return null;
                }
                arrayList.add((CallSite)((Object)(tryCatchBlockNode.getOpcode() + ":" + (int)object2)));
                continue;
            }
            if (list3.get(i) instanceof TableSwitchInsnNode || list3.get(i) instanceof LookupSwitchInsnNode) {
                return null;
            }
            arrayList.add((CallSite)((Object)list4.get(i)));
        }
        for (TryCatchBlockNode tryCatchBlockNode : methodNode.tryCatchBlocks) {
            Object object3 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.start)];
            Object object4 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.end)];
            Object object5 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.handler)];
            if (object3 < 0 || object4 < 0 || object5 < 0) {
                return null;
            }
            arrayList.add((CallSite)((Object)("try:" + tryCatchBlockNode.type + ":" + (int)object3 + ":" + (int)object4 + ":" + (int)object5)));
        }
        return List.copyOf(arrayList);
    }

    static List<String> timerParentSnapshot(ClassNode classNode) {
        int n;
        int n2;
        JumpInsnNode jumpInsnNode;
        VarInsnNode varInsnNode;
        VarInsnNode varInsnNode2;
        VarInsnNode varInsnNode3;
        VarInsnNode varInsnNode4;
        AbstractInsnNode abstractInsnNode;
        int n3;
        VarInsnNode varInsnNode5;
        VarInsnNode varInsnNode6;
        AbstractInsnNode abstractInsnNode2;
        List<MethodNode> list = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.matchesSelector(methodNode, TIMER_PARENT)).toList();
        if (list.size() != 1 || list.get((int)0).access != 8) {
            return null;
        }
        MethodNode methodNode2 = list.get(0);
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode2);
        List<String> list3 = OwnerFieldInstallationProof.body(methodNode2, classNode.name, classNode.name);
        if (!(list2.size() >= 20 && list3.subList(0, 2).equals(List.of("25:5", "184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:originalSequence:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;:false")) && (abstractInsnNode2 = list2.get(2)) instanceof VarInsnNode && (varInsnNode6 = (VarInsnNode)abstractInsnNode2).getOpcode() == 58 && varInsnNode6.var >= 9 && list3.subList(3, 9).equals(List.of("25:5", "25:8", "25:2", "25:1", "184:net/farmingtales/ftboss/item/FoodWeaponEvolution:planHitTimerProfile:(Lnet/farmingtales/ftboss/item/FoodWeaponEvolution$Profile;)Ljava/lang/String;:false", "184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:beginOriginalTimer:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;:false")) && (abstractInsnNode2 = list2.get(9)) instanceof VarInsnNode && (varInsnNode5 = (VarInsnNode)abstractInsnNode2).getOpcode() == 58 && varInsnNode5.var >= 9 && varInsnNode5.var != varInsnNode6.var)) {
            return null;
        }
        int n4 = list3.indexOf(OwnerFieldInstallationProof.timerOperation(0, false));
        int n5 = list3.indexOf(OwnerFieldInstallationProof.timerOperation(1, false));
        if (n5 < 0) {
            n5 = list3.indexOf(OwnerFieldInstallationProof.timerOperation(1, true));
        }
        if ((n3 = list3.indexOf(OwnerFieldInstallationProof.timerOperation(2, false))) < 0) {
            n3 = list3.indexOf(OwnerFieldInstallationProof.timerOperation(2, true));
        }
        String string2 = "(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;)V:false";
        int n6 = list3.indexOf("184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:timerOriginalPutReturned:" + string2);
        int n7 = list3.indexOf("182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:originalReturned:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;Ljava/lang/Object;)V:false");
        int n8 = list3.indexOf("182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:originalThrown:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;Ljava/lang/Throwable;)V:false");
        int n9 = list3.indexOf("184:net/farmingtales/ftboss/item/FoodWeaponHitPlan:retainOriginalTimerThrowable:(Lnet/farmingtales/ftboss/item/FoodWeaponHitPlan$FinalEffectsCommit;Ljava/lang/Throwable;)V:false");
        if (!(n4 >= 2 && n5 > n4 && n3 > n5 && n6 == n3 + 3 && n7 > n6 && n8 > n7 && n9 > n8 && n9 + 2 < list2.size() && list3.subList(n4 - 2, n4).equals(List.of("25:0", "25:1")) && list3.get(n5 - 1).equals("25:3") && (abstractInsnNode = list2.get(n4 + 1)) instanceof VarInsnNode && (varInsnNode4 = (VarInsnNode)abstractInsnNode).getOpcode() == 58 && (abstractInsnNode = list2.get(n5 + 1)) instanceof VarInsnNode && (varInsnNode3 = (VarInsnNode)abstractInsnNode).getOpcode() == 58 && list3.subList(n3 - 3, n3).equals(List.of("25:" + varInsnNode4.var, "25:2", "25:" + varInsnNode3.var)) && (abstractInsnNode = list2.get(n3 + 1)) instanceof VarInsnNode && (varInsnNode2 = (VarInsnNode)abstractInsnNode).getOpcode() == 58 && list3.get(n3 + 2).equals("25:5") && list3.subList(n7 - 3, n7).equals(List.of("25:" + varInsnNode6.var, "25:" + varInsnNode5.var, "25:" + varInsnNode2.var)) && (abstractInsnNode = list2.get(n9 - 1)) instanceof VarInsnNode && (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() == 25 && list3.get(n9 - 2).equals("25:5") && list3.subList(n8 - 3, n8).equals(List.of("25:" + varInsnNode6.var, "25:" + varInsnNode5.var, "25:" + varInsnNode.var)) && (abstractInsnNode = list2.get(n8 - 4)) instanceof JumpInsnNode && (jumpInsnNode = (JumpInsnNode)abstractInsnNode).getOpcode() == 198 && OwnerFieldInstallationProof.position(methodNode2, (AbstractInsnNode)jumpInsnNode.label) == n9 - 2 && list3.get(n8 - 5).equals("25:" + varInsnNode6.var) && list3.subList(n9 + 1, n9 + 3).equals(List.of("25:" + varInsnNode.var, "191:")))) {
            return null;
        }
        for (n2 = 0; n2 < 3; ++n2) {
            n = 0;
            for (String string3 : list3) {
                if (!string3.equals(OwnerFieldInstallationProof.timerOperation(n2, false)) && !string3.equals(OwnerFieldInstallationProof.timerOperation(n2, true))) continue;
                ++n;
            }
            if (n == true) continue;
            return null;
        }
        n2 = 0;
        n = 0;
        for (String string3 : methodNode2.tryCatchBlocks) {
            VarInsnNode varInsnNode7;
            AbstractInsnNode abstractInsnNode3;
            int n10 = OwnerFieldInstallationProof.position(methodNode2, (AbstractInsnNode)((TryCatchBlockNode)string3).start);
            int n11 = OwnerFieldInstallationProof.position(methodNode2, (AbstractInsnNode)((TryCatchBlockNode)string3).end);
            int n12 = OwnerFieldInstallationProof.position(methodNode2, (AbstractInsnNode)((TryCatchBlockNode)string3).handler);
            if (n10 <= n3 && n3 < n11 && ((TryCatchBlockNode)string3).type == null) {
                n = 1;
            }
            if (n10 > n3 || n3 >= n11 || !"java/lang/Throwable".equals(((TryCatchBlockNode)string3).type) || n12 + 6 != n8 || !((abstractInsnNode3 = list2.get(n12)) instanceof VarInsnNode) || (varInsnNode7 = (VarInsnNode)abstractInsnNode3).getOpcode() != 58 || varInsnNode7.var != varInsnNode.var) continue;
            n2 = 1;
        }
        if (n2 == 0 || n == 0 || list3.stream().filter(string -> string.equals("177:")).count() != 1L || list3.stream().filter(string -> string.equals("182:net/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session:close:(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)V:false")).count() < 1L) {
            return null;
        }
        return OwnerFieldInstallationProof.timerShape(classNode, methodNode2, List.of(), List.of(), methodNode2.maxLocals);
    }

    static boolean timerWrapper(String[] stringArray, ClassNode classNode, ClassNode classNode2, List<String> list) {
        if (!(OwnerFieldInstallationProof.lateTimerSite(stringArray) && classNode != null && classNode2 != null && classNode2.name.replace('/', '.').equals(stringArray[0]) && classNode.name.replace('/', '.').equals(stringArray[1]))) {
            return false;
        }
        if (list == null || list.isEmpty() || !list.get(0).startsWith("locals:")) {
            return false;
        }
        int n = Integer.parseInt(list.get(0).substring(7));
        MethodNode methodNode2 = OwnerFieldInstallationProof.canonicalTimerHandler(classNode, classNode2, false);
        ClassNode classNode3 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin", classNode);
        MethodNode methodNode3 = OwnerFieldInstallationProof.canonicalTimerHandler(classNode, classNode3, true);
        if (methodNode2 == null || methodNode3 == null) {
            return false;
        }
        List<MethodNode> list2 = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.matchesSelector(methodNode, TIMER_PARENT)).toList();
        if (list2.size() != 1 || list2.get((int)0).access != 8) {
            return false;
        }
        MethodNode methodNode4 = list2.get(0);
        List<AbstractInsnNode> list3 = OwnerFieldInstallationProof.executable(methodNode4);
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        ArrayList<String> arrayList2 = new ArrayList<String>();
        ArrayList<String> arrayList3 = new ArrayList<String>();
        int[] nArray = new int[3];
        for (int i = 0; i < list3.size(); ++i) {
            String string;
            MethodNode methodNode5;
            int n2;
            AbstractInsnNode abstractInsnNode = list3.get(i);
            if (!(abstractInsnNode instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
            if (!methodInsnNode.owner.equals(classNode.name)) continue;
            if (OwnerFieldInstallationProof.call(list3.get(i), methodNode2, classNode.name)) {
                n2 = 2;
                methodNode5 = methodNode2;
            } else if (OwnerFieldInstallationProof.call(list3.get(i), methodNode3, classNode.name)) {
                n2 = 1;
                methodNode5 = methodNode3;
            } else {
                string = OwnerFieldInstallationProof.member(methodInsnNode.name);
                if (!string.equals("ftboss$actualOriginalReceiver")) continue;
                n2 = 0;
                ClassNode classNode4 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin", classNode);
                methodNode5 = OwnerFieldInstallationProof.timerHandler(classNode, classNode4, string);
                if (methodNode5 == null || !OwnerFieldInstallationProof.call(list3.get(i), methodNode5, classNode.name)) {
                    return false;
                }
            }
            String string2 = n2 == 0 ? OwnerFieldInstallationProof.timerOperation(0, false) : (string = OwnerFieldInstallationProof.ownerOriginal(list, n2 == 1 ? 3 : 4, 1));
            if (string == null) {
                return false;
            }
            boolean bl = n2 != 0 && string.equals(OwnerFieldInstallationProof.timerOperation(n2, true));
            int[] nArray2 = OwnerFieldInstallationProof.timerWindow(classNode, methodNode4, i, methodNode5, n2, bl, n, arrayList3);
            if (nArray2 == null) {
                return false;
            }
            int n3 = n2;
            nArray[n3] = nArray[n3] + 1;
            arrayList.add(nArray2);
            arrayList2.add(string);
        }
        for (int n4 : nArray) {
            if (n4 == 1) continue;
            return false;
        }
        if (!list.equals(OwnerFieldInstallationProof.timerShape(classNode, methodNode4, arrayList, arrayList2, n))) {
            return false;
        }
        int n5 = 0;
        for (MethodNode methodNode6 : classNode.methods) {
            for (String string : methodNode6.instructions) {
                if (!OwnerFieldInstallationProof.call((AbstractInsnNode)string, methodNode2, classNode.name)) continue;
                if (methodNode6 != methodNode4) {
                    return false;
                }
                ++n5;
            }
        }
        return n5 == 1;
    }

    private static boolean absentAlias(ClassNode classNode, ClassNode classNode2, String string) {
        return classNode2.methods.stream().noneMatch(methodNode -> methodNode.name.equals(string)) && classNode.methods.stream().noneMatch(methodNode -> OwnerFieldInstallationProof.member(methodNode.name).equals(string));
    }

    private static MethodNode canonicalTimerHandler(ClassNode classNode, ClassNode classNode2, boolean bl) {
        String string;
        String string2 = "net/farmingtales/ftboss/mixin/" + (bl ? "NativeExpectedTimerCopyMixin" : "NativeExplicitTimerPutMixin");
        String string3 = string = bl ? "ftboss$actualOriginalCopy" : "ftboss$r2$timerPut";
        if (classNode2 == null || !classNode2.name.equals(string2) || !OwnerFieldInstallationProof.absentAlias(classNode, classNode2, string)) {
            return null;
        }
        MethodNode methodNode = OwnerFieldInstallationProof.timerHandler(classNode, classNode2, string + "Srg");
        if (methodNode == null) {
            return null;
        }
        List<MethodNode> list = classNode2.methods.stream().filter(methodNode2 -> methodNode2.name.equals(string + "Srg") && methodNode2.desc.equals(methodNode.desc)).toList();
        if (list.size() != 1 || !OwnerFieldInstallationProof.timerGroup(list.get(0), bl)) {
            return null;
        }
        int n = 0;
        for (MethodNode methodNode3 : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode3.instructions) {
                if (!OwnerFieldInstallationProof.call(abstractInsnNode, methodNode, classNode.name)) continue;
                ++n;
            }
        }
        return n == 1 ? methodNode : null;
    }

    static boolean canonicalAliasSite(String[] stringArray) {
        return stringArray != null && (List.of(stringArray).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$originalRangedPersistentData", "ftboss$originalRangedCompound", "ftboss$originalRangedCompoundSrg", "ftboss$originalRangedLong", "ftboss$originalRangedLongSrg", "ftboss$originalRangedRoot", "ftboss$originalRangedRootSrg")) || List.of(stringArray).equals(List.of("net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$actualOriginalCopy", "ftboss$actualOriginalCopySrg")));
    }

    private static String rangedOperation(int n, boolean bl) {
        return OwnerFieldInstallationProof.timerOperation(n == 0 ? 5 : (n == 1 ? 3 : (n == 2 ? 4 : 2)), bl);
    }

    private static String rangedOriginal(List<String> list, int n, int n2) {
        String string = null;
        int n3 = 0;
        for (String string2 : list) {
            if (!string2.equals(OwnerFieldInstallationProof.rangedOperation(n, false)) && !string2.equals(OwnerFieldInstallationProof.rangedOperation(n, true))) continue;
            if (string != null && !string.equals(string2)) {
                return null;
            }
            string = string2;
            ++n3;
        }
        return n3 == n2 ? string : null;
    }

    private static List<List<String>> rangedParentSnapshot(ClassNode classNode) {
        ArrayList<List<String>> arrayList = new ArrayList<List<String>>();
        int[][] nArrayArray = new int[][]{{1, 1, 1, 0}, {2, 1, 1, 1}};
        for (int i = 0; i < RANGED_PARENTS.size(); ++i) {
            MethodNode methodNode = OwnerFieldInstallationProof.ownerParent(classNode, RANGED_PARENTS.get(i), 9);
            if (methodNode == null) {
                return null;
            }
            List<String> list = OwnerFieldInstallationProof.body(methodNode, classNode.name, classNode.name);
            for (int j = 0; j < nArrayArray[i].length; ++j) {
                if (nArrayArray[i][j] == 0) {
                    for (String string : list) {
                        if (!string.equals(OwnerFieldInstallationProof.rangedOperation(j, false)) && !string.equals(OwnerFieldInstallationProof.rangedOperation(j, true))) continue;
                        return null;
                    }
                    continue;
                }
                if (OwnerFieldInstallationProof.rangedOriginal(list, j, nArrayArray[i][j]) != null) continue;
                return null;
            }
            List<String> list2 = OwnerFieldInstallationProof.timerShape(classNode, methodNode, List.of(), List.of(), methodNode.maxLocals);
            if (list2 == null) {
                return null;
            }
            arrayList.add(list2);
        }
        return List.copyOf(arrayList);
    }

    private static boolean rangedAnnotation(MethodNode methodNode, int n) {
        AnnotationNode annotationNode2;
        Object object;
        List list;
        int n2 = n == 0 ? 3 : (n == 3 ? 1 : 2);
        List<AnnotationNode> list2 = OwnerFieldInstallationProof.annotations(methodNode).stream().filter(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/injection/Group;")).toList();
        if (n == 0 ? !list2.isEmpty() : list2.size() != 1) {
            return false;
        }
        if (!(n == 0 || OwnerFieldInstallationProof.annotationKeys(list2.get(0), List.of("name", "min", "max")) && (n == 1 ? "ftboss$rangedCompoundSite" : (n == 2 ? "ftboss$rangedLongSite" : "ftboss$rangedRootSite")).equals(OwnerFieldInstallationProof.value(list2.get(0), "name")) && Integer.valueOf(n2).equals(OwnerFieldInstallationProof.value(list2.get(0), "min")) && Integer.valueOf(n2).equals(OwnerFieldInstallationProof.value(list2.get(0), "max")))) {
            return false;
        }
        AnnotationNode annotationNode3 = OwnerFieldInstallationProof.visibleAnnotation(methodNode, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode3, n == 0 ? List.of("method", "at", "require", "expect", "allow", "remap") : List.of("method", "at", "require", "allow", "remap")) && (n == 3 ? List.of(RANGED_PARENTS.get(1)) : RANGED_PARENTS).equals(OwnerFieldInstallationProof.value(annotationNode3, "method")) && Integer.valueOf(n == 0 ? 3 : 0).equals(OwnerFieldInstallationProof.value(annotationNode3, "require")) && Integer.valueOf(n2).equals(OwnerFieldInstallationProof.value(annotationNode3, "allow")) && Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode3, "remap")) && (n != 0 || Integer.valueOf(3).equals(OwnerFieldInstallationProof.value(annotationNode3, "expect"))))) {
            return false;
        }
        Object object2 = OwnerFieldInstallationProof.value(annotationNode3, "at");
        if (!(object2 instanceof List && (list = (List)object2).size() == 1 && (object = list.get(0)) instanceof AnnotationNode && OwnerFieldInstallationProof.annotationKeys(annotationNode2 = (AnnotationNode)object, List.of("value", "target", "remap")) && "INVOKE".equals(OwnerFieldInstallationProof.value(annotationNode2, "value")) && Boolean.FALSE.equals(OwnerFieldInstallationProof.value(annotationNode2, "remap")))) {
            return false;
        }
        object = n == 0 ? "Lnet/minecraft/server/level/ServerPlayer;getPersistentData()Lnet/minecraft/nbt/CompoundTag;" : (n == 1 ? "Lnet/minecraft/nbt/CompoundTag;m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;" : (n == 2 ? "Lnet/minecraft/nbt/CompoundTag;m_128356_(Ljava/lang/String;J)V" : "Lnet/minecraft/nbt/CompoundTag;m_128365_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"));
        return ((String)object).equals(OwnerFieldInstallationProof.value(annotationNode2, "target"));
    }

    private static int[] rangedWindow(ClassNode classNode, MethodNode methodNode, int n, MethodNode methodNode2, int n2, boolean bl, int n3, List<String> list) {
        InvokeDynamicInsnNode invokeDynamicInsnNode;
        int n4;
        MethodInsnNode methodInsnNode;
        Object object;
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode);
        int n5 = n - 1;
        int n6 = n2 == 0 ? 1 : (n2 == 1 ? 2 : 3);
        int n7 = n5 - 2 * n6;
        if (n7 < 0 || !((object = list2.get(n)) instanceof MethodInsnNode) || (methodInsnNode = (MethodInsnNode)object).getOpcode() != 184 || methodInsnNode.itf || !OwnerFieldInstallationProof.call((AbstractInsnNode)methodInsnNode, methodNode2, classNode.name)) {
            return null;
        }
        object = new int[n6];
        int[] nArray = new int[n6];
        for (int i = 0; i < n6; ++i) {
            VarInsnNode varInsnNode;
            n4 = n2 == 2 && i == 0 ? 1 : 0;
            int n8 = n4 != 0 ? 2 : 1;
            AbstractInsnNode abstractInsnNode = list2.get(n7 + i);
            if (!(abstractInsnNode instanceof VarInsnNode) || (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() != (n4 != 0 ? 55 : 58) || varInsnNode.var < n3 || varInsnNode.var + n8 > methodNode.maxLocals) {
                return null;
            }
            object[i] = (AbstractInsnNode)varInsnNode.var;
            nArray[i] = n8;
            for (int j = 0; j < i; ++j) {
                if (object[j] >= varInsnNode.var + n8 || varInsnNode.var >= object[j] + nArray[j]) continue;
                return null;
            }
        }
        for (n4 = 0; n4 < n6; ++n4) {
            VarInsnNode varInsnNode;
            AbstractInsnNode abstractInsnNode = list2.get(n7 + n6 + n4);
            if (abstractInsnNode instanceof VarInsnNode && (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() == (n2 == 2 && n4 == 2 ? 22 : 25) && varInsnNode.var == object[n6 - n4 - 1]) continue;
            return null;
        }
        AbstractInsnNode abstractInsnNode = list2.get(n5);
        if (!(abstractInsnNode instanceof InvokeDynamicInsnNode) || !OwnerFieldInstallationProof.timerBridge(classNode, invokeDynamicInsnNode = (InvokeDynamicInsnNode)abstractInsnNode, n2 == 0 ? 5 : (n2 == 1 ? 3 : (n2 == 2 ? 4 : 2)), bl, list)) {
            return null;
        }
        return new int[]{n7, n};
    }

    static boolean canonicalRangedWrapper(String[] stringArray, ClassNode classNode, ClassNode classNode2, List<List<String>> list) {
        int n;
        Object object;
        if (!(OwnerFieldInstallationProof.canonicalAliasSite(stringArray) && stringArray[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin") && classNode != null && classNode2 != null && classNode2.name.replace('/', '.').equals(stringArray[0]) && classNode.name.replace('/', '.').equals(stringArray[1]) && list != null && list.size() == 2)) {
            return false;
        }
        int[] nArray = new int[]{2, 4, 6, 8};
        int[] nArray2 = new int[]{3, 2, 2, 1};
        int[][] nArrayArray = new int[][]{{1, 1, 1, 0}, {2, 1, 1, 1}};
        MethodNode[] methodNodeArray = new MethodNode[nArray.length];
        for (int i = 0; i < nArray.length; ++i) {
            object = stringArray[nArray[i]];
            if (i != 0 && !OwnerFieldInstallationProof.absentAlias(classNode, classNode2, stringArray[nArray[i] - 1])) {
                return false;
            }
            methodNodeArray[i] = OwnerFieldInstallationProof.ownerHandler(classNode, classNode2, (String)object, false);
            if (methodNodeArray[i] == null) {
                return false;
            }
            List<MethodNode> list2 = classNode2.methods.stream().filter(arg_0 -> OwnerFieldInstallationProof.lambda$canonicalRangedWrapper$19((String)object, stringArray, arg_0)).toList();
            if (list2.size() == 1 && OwnerFieldInstallationProof.rangedAnnotation(list2.get(0), i)) continue;
            return false;
        }
        int[] nArray3 = new int[nArray.length];
        object = new ArrayList();
        for (n = 0; n < RANGED_PARENTS.size(); ++n) {
            List<String> list3 = list.get(n);
            if (list3 == null || list3.isEmpty() || !list3.get(0).startsWith("locals:")) {
                return false;
            }
            int n2 = Integer.parseInt(list3.get(0).substring(7));
            MethodNode methodNode = OwnerFieldInstallationProof.ownerParent(classNode, RANGED_PARENTS.get(n), 9);
            if (methodNode == null) {
                return false;
            }
            List<AbstractInsnNode> list4 = OwnerFieldInstallationProof.executable(methodNode);
            Object object2 = new ArrayList();
            ArrayList<String> arrayList = new ArrayList<String>();
            int[] nArray4 = new int[nArray.length];
            for (int i = 0; i < list4.size(); ++i) {
                for (int j = 0; j < methodNodeArray.length; ++j) {
                    if (!OwnerFieldInstallationProof.call((AbstractInsnNode)list4.get(i), methodNodeArray[j], classNode.name)) continue;
                    if (nArrayArray[n][j] == 0) {
                        return false;
                    }
                    String string = OwnerFieldInstallationProof.rangedOriginal(list3, j, nArrayArray[n][j]);
                    if (string == null) {
                        return false;
                    }
                    int[] nArray5 = OwnerFieldInstallationProof.rangedWindow(classNode, methodNode, i, methodNodeArray[j], j, string.equals(OwnerFieldInstallationProof.rangedOperation(j, true)), n2, (List<String>)object);
                    if (nArray5 == null) {
                        return false;
                    }
                    object2.add(nArray5);
                    arrayList.add(string);
                    int n3 = j;
                    nArray4[n3] = nArray4[n3] + 1;
                    int n4 = j;
                    nArray3[n4] = nArray3[n4] + 1;
                }
            }
            if (Arrays.equals(nArray4, nArrayArray[n]) && list3.equals(OwnerFieldInstallationProof.timerShape(classNode, methodNode, (List<int[]>)object2, arrayList, n2))) continue;
            return false;
        }
        for (n = 0; n < methodNodeArray.length; ++n) {
            int n5 = 0;
            for (MethodNode methodNode : classNode.methods) {
                for (Object object2 : methodNode.instructions) {
                    if (!OwnerFieldInstallationProof.call((AbstractInsnNode)object2, methodNodeArray[n], classNode.name)) continue;
                    ++n5;
                }
            }
            if (nArray3[n] == nArray2[n] && n5 == nArray2[n]) continue;
            return false;
        }
        return object.size() == 8;
    }

    static int ownerSlot(String[] stringArray) {
        if (stringArray == null) {
            return -1;
        }
        if (stringArray.length == 6 && stringArray[0].equals("net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin") && stringArray[1].equals("net.farmingtales.ftboss.item.FoodWeaponEvolution") && stringArray[2].equals("ftboss$ownerLocalDataCopy") && stringArray[3].equals("ftboss$ownerLocalBaselineCopy") && stringArray[4].equals("ftboss$ownerTargetReceiver") && stringArray[5].equals("ftboss$ownerTargetWrite")) {
            return 2;
        }
        if (stringArray.length == 4 && stringArray[0].equals("net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin") && stringArray[1].equals("net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf") && stringArray[2].equals("ftboss$ownerNativeCopy") && stringArray[3].equals("ftboss$ownerNativePut")) {
            return 3;
        }
        return -1;
    }

    private static String ownerOperation(int n, boolean bl) {
        if (n == 0) {
            return "182:net/minecraft/world/entity/LivingEntity:getPersistentData:()Lnet/minecraft/nbt/CompoundTag;:false";
        }
        if (n == 1) {
            return "182:net/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder:write:(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V:false";
        }
        if (n == 2) {
            return "182:net/minecraft/nbt/CompoundTag:" + (bl ? "m_6426_" : "copy") + ":()Lnet/minecraft/nbt/CompoundTag;:false";
        }
        if (n == 3) {
            return OwnerFieldInstallationProof.timerOperation(1, bl);
        }
        if (n == 4) {
            return OwnerFieldInstallationProof.timerOperation(2, bl);
        }
        return "182:net/minecraft/nbt/CompoundTag:" + (bl ? "m_128473_" : "remove") + ":(Ljava/lang/String;)V:false";
    }

    private static String ownerOriginal(List<String> list, int n, int n2) {
        String string = null;
        int n3 = 0;
        for (String string2 : list) {
            if (!string2.equals(OwnerFieldInstallationProof.ownerOperation(n, false)) && !string2.equals(OwnerFieldInstallationProof.ownerOperation(n, true))) continue;
            if (string != null && !string.equals(string2)) {
                return null;
            }
            string = string2;
            ++n3;
        }
        return n3 == n2 ? string : null;
    }

    private static MethodNode ownerParent(ClassNode classNode, String string, int n) {
        List<MethodNode> list = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.matchesSelector(methodNode, string)).toList();
        return list.size() == 1 && list.get((int)0).access == n ? list.get(0) : null;
    }

    private static MethodNode ownerHandler(ClassNode classNode, ClassNode classNode2, String string, boolean bl) {
        int n;
        String string2 = OwnerFieldInstallationCatalogue.descriptor(classNode2.name.replace('/', '.'), string);
        if (string2 == null) {
            return null;
        }
        List<MethodNode> list = classNode2.methods.stream().filter(methodNode -> methodNode.name.equals(string) && methodNode.desc.equals(string2)).toList();
        List<MethodNode> list2 = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.member(methodNode.name).equals(string) && methodNode.desc.equals(string2) && OwnerFieldInstallationProof.merged(methodNode, classNode.name.replace('/', '.'))).toList();
        int n2 = n = bl ? 2 : 10;
        if (list.size() != 1 || list2.size() != 1 || list.get((int)0).access != n || list2.get((int)0).access != n || !OwnerFieldInstallationProof.body(list.get(0), classNode2.name, classNode.name).equals(OwnerFieldInstallationProof.body(list2.get(0), classNode.name, classNode.name))) {
            return null;
        }
        return list2.get(0);
    }

    private static boolean ownerAnnotation(MethodNode methodNode, int n, int n2) {
        Object object;
        List list;
        boolean bl;
        AnnotationNode annotationNode = OwnerFieldInstallationProof.visibleAnnotation(methodNode, "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode, (bl = n < 2) ? List.of("method", "at", "require", "expect", "allow") : List.of("method", "at", "require")) && List.of(bl ? "quoteFoodBlessingHits" : (n == 2 ? "quoteFinalWeaponHit" : "apply")).equals(OwnerFieldInstallationProof.value(annotationNode, "method")) && Integer.valueOf(n == 0 ? 8 : (n == 1 ? 7 : 1)).equals(OwnerFieldInstallationProof.value(annotationNode, "require")))) {
            return false;
        }
        if (!(!bl || Integer.valueOf(n == 0 ? 8 : 7).equals(OwnerFieldInstallationProof.value(annotationNode, "expect")) && Integer.valueOf(n == 0 ? 8 : 7).equals(OwnerFieldInstallationProof.value(annotationNode, "allow")))) {
            return false;
        }
        Object object2 = OwnerFieldInstallationProof.value(annotationNode, "at");
        if (!(object2 instanceof List) || (list = (List)object2).size() != 1 || !((object = list.get(0)) instanceof AnnotationNode)) {
            return false;
        }
        AnnotationNode annotationNode2 = (AnnotationNode)object;
        object = n == 1 ? List.of("value", "target") : (n == 2 ? List.of("value", "target", "ordinal", "remap") : List.of("value", "target", "remap"));
        String string = OwnerFieldInstallationProof.ownerOperation(n, false);
        int n3 = string.indexOf(58, string.indexOf(58) + 1);
        String string2 = string.substring(string.indexOf(58) + 1, n3);
        String string3 = string.substring(n3 + 1, string.lastIndexOf(58));
        String string4 = "L" + string2 + ";" + string3.replaceFirst(":", "");
        if (!(OwnerFieldInstallationProof.annotationKeys(annotationNode2, object) && "INVOKE".equals(OwnerFieldInstallationProof.value(annotationNode2, "value")) && string4.equals(OwnerFieldInstallationProof.value(annotationNode2, "target")))) {
            return false;
        }
        if (n != 1 && !Boolean.valueOf(n != 0).equals(OwnerFieldInstallationProof.value(annotationNode2, "remap"))) {
            return false;
        }
        return n != 2 || Integer.valueOf(n2).equals(OwnerFieldInstallationProof.value(annotationNode2, "ordinal"));
    }

    private static List<String> ownerShape(ClassNode classNode, MethodNode methodNode, List<int[]> list, List<String> list2, int n) {
        List<AbstractInsnNode> list3 = OwnerFieldInstallationProof.executable(methodNode);
        if (list.size() != list2.size()) {
            return null;
        }
        int n2 = -1;
        for (int[] nArray : list) {
            if (nArray.length != 2 || nArray[0] <= n2 || nArray[0] < 0 || nArray[1] < nArray[0] || nArray[1] >= list3.size()) {
                return null;
            }
            n2 = nArray[1];
        }
        Object object = new int[list3.size() + 1];
        Arrays.fill((int[])object, -1);
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        while (n5 < list3.size()) {
            object[n5] = n3++;
            if (n4 < list.size() && list.get(n4)[0] == n5) {
                n5 = list.get(n4++)[1] + 1;
                continue;
            }
            ++n5;
        }
        object[list3.size()] = n3;
        List<String> list4 = OwnerFieldInstallationProof.body(methodNode, classNode.name, classNode.name);
        ArrayList<CallSite> arrayList = new ArrayList<CallSite>(List.of("locals:" + n));
        n4 = 0;
        for (int i = 0; i < list3.size(); ++i) {
            StringBuilder stringBuilder;
            if (n4 < list.size() && list.get(n4)[0] == i) {
                arrayList.add((CallSite)((Object)list2.get(n4)));
                i = list.get(n4++)[1];
                continue;
            }
            AbstractInsnNode abstractInsnNode = list3.get(i);
            if (abstractInsnNode instanceof JumpInsnNode) {
                TryCatchBlockNode tryCatchBlockNode = (JumpInsnNode)abstractInsnNode;
                Object object2 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.label)];
                if (object2 < 0) {
                    return null;
                }
                arrayList.add((CallSite)((Object)(tryCatchBlockNode.getOpcode() + ":" + (int)object2)));
                continue;
            }
            abstractInsnNode = list3.get(i);
            if (abstractInsnNode instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode)abstractInsnNode;
                Object object3 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tableSwitchInsnNode.dflt)];
                if (object3 < 0) {
                    return null;
                }
                stringBuilder = new StringBuilder().append(tableSwitchInsnNode.min).append(':').append(tableSwitchInsnNode.max).append(':').append((int)object3);
                for (LabelNode labelNode : tableSwitchInsnNode.labels) {
                    object3 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)labelNode)];
                    if (object3 < 0) {
                        return null;
                    }
                    stringBuilder.append(':').append((int)object3);
                }
                arrayList.add((CallSite)((Object)(tableSwitchInsnNode.getOpcode() + ":" + stringBuilder)));
                continue;
            }
            abstractInsnNode = list3.get(i);
            if (abstractInsnNode instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode)abstractInsnNode;
                Object object4 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)lookupSwitchInsnNode.dflt)];
                if (object4 < 0) {
                    return null;
                }
                stringBuilder = new StringBuilder(lookupSwitchInsnNode.keys.toString()).append(':').append((int)object4);
                for (LabelNode labelNode : lookupSwitchInsnNode.labels) {
                    object4 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)labelNode)];
                    if (object4 < 0) {
                        return null;
                    }
                    stringBuilder.append(':').append((int)object4);
                }
                arrayList.add((CallSite)((Object)(lookupSwitchInsnNode.getOpcode() + ":" + stringBuilder)));
                continue;
            }
            arrayList.add((CallSite)((Object)list4.get(i)));
        }
        for (TryCatchBlockNode tryCatchBlockNode : methodNode.tryCatchBlocks) {
            Object object5 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.start)];
            Object object6 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.end)];
            Object object7 = object[OwnerFieldInstallationProof.position(methodNode, (AbstractInsnNode)tryCatchBlockNode.handler)];
            if (object5 < 0 || object6 < 0 || object7 < 0) {
                return null;
            }
            arrayList.add((CallSite)((Object)("try:" + tryCatchBlockNode.type + ":" + (int)object5 + ":" + (int)object6 + ":" + (int)object7)));
        }
        return List.copyOf(arrayList);
    }

    private static boolean ownerBridge(ClassNode classNode, InvokeDynamicInsnNode invokeDynamicInsnNode, int n, String string, List<String> list) {
        int n2;
        Type type;
        Handle handle;
        Type type2;
        List<MethodNode> list2;
        String[] stringArray;
        if (n == 0) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = "net/minecraft/world/entity/LivingEntity";
        } else if (n == 1) {
            String[] stringArray3 = new String[5];
            stringArray3[0] = "net/farmingtales/ftboss/item/FoodWeaponHitPlan$Builder";
            stringArray3[1] = "net/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain";
            stringArray3[2] = "net/minecraft/nbt/CompoundTag";
            stringArray3[3] = "java/lang/String";
            stringArray = stringArray3;
            stringArray3[4] = "net/minecraft/nbt/Tag";
        } else if (n == 2) {
            String[] stringArray4 = new String[1];
            stringArray = stringArray4;
            stringArray4[0] = "net/minecraft/nbt/CompoundTag";
        } else if (n == 3) {
            String[] stringArray5 = new String[1];
            stringArray = stringArray5;
            stringArray5[0] = "net/minecraft/nbt/Tag";
        } else {
            String[] stringArray6 = new String[3];
            stringArray6[0] = "net/minecraft/nbt/CompoundTag";
            stringArray6[1] = "java/lang/String";
            stringArray = stringArray6;
            stringArray6[2] = "net/minecraft/nbt/Tag";
        }
        String[] stringArray7 = stringArray;
        String string2 = n == 1 ? "Ljava/lang/Void;" : (n == 0 || n == 2 ? "Lnet/minecraft/nbt/CompoundTag;" : "Lnet/minecraft/nbt/Tag;");
        String string3 = "([Ljava/lang/Object;)" + string2;
        String string4 = "([Ljava/lang/Object;)Ljava/lang/Object;";
        String string5 = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
        if (!(invokeDynamicInsnNode.name.equals("call") && invokeDynamicInsnNode.desc.equals("()Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;") && invokeDynamicInsnNode.bsm.getTag() == 6 && !invokeDynamicInsnNode.bsm.isInterface() && invokeDynamicInsnNode.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && invokeDynamicInsnNode.bsm.getName().equals("metafactory") && invokeDynamicInsnNode.bsm.getDesc().equals(string5) && invokeDynamicInsnNode.bsmArgs.length == 3 && (list2 = invokeDynamicInsnNode.bsmArgs[0]) instanceof Type && (type2 = (Type)list2).getDescriptor().equals(string4) && (list2 = invokeDynamicInsnNode.bsmArgs[1]) instanceof Handle && (handle = (Handle)list2).getTag() == 6 && !handle.isInterface() && handle.getOwner().equals(classNode.name) && handle.getDesc().equals(string3) && !list.contains(handle.getName()) && (list2 = invokeDynamicInsnNode.bsmArgs[2]) instanceof Type && (type = (Type)list2).getDescriptor().equals(string3))) {
            return false;
        }
        list2 = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(handle.getName()) && methodNode.desc.equals(string3)).toList();
        if (list2.size() != 1) {
            return false;
        }
        MethodNode methodNode2 = list2.get(0);
        if (methodNode2.access != 4106 || methodNode2.signature != null || !methodNode2.tryCatchBlocks.isEmpty() || methodNode2.exceptions != null && !methodNode2.exceptions.isEmpty() || !OwnerFieldInstallationProof.annotations(methodNode2).isEmpty()) {
            return false;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string6 : stringArray7) {
            arrayList.add(string6.replace('/', '.'));
        }
        ArrayList<String> arrayList2 = new ArrayList<String>(List.of("25:0", "16:" + stringArray7.length, "18:java.lang.String:[" + String.join((CharSequence)", ", arrayList) + "]", "184:com/llamalad7/mixinextras/injector/wrapoperation/WrapOperationRuntime:checkArgumentCount:([Ljava/lang/Object;ILjava/lang/String;)V:false", "25:0"));
        for (n2 = 0; n2 < stringArray7.length; ++n2) {
            arrayList2.addAll(List.of("89:", "16:" + n2, "50:", "192:" + stringArray7[n2], "95:"));
        }
        arrayList2.addAll(List.of("87:", string));
        if (n == 1) {
            arrayList2.addAll(List.of("1:", "192:java/lang/Void"));
        }
        arrayList2.add("176:");
        if (!OwnerFieldInstallationProof.body(methodNode2, classNode.name, classNode.name).equals(arrayList2)) {
            return false;
        }
        n2 = 0;
        for (String string6 : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : ((MethodNode)string6).instructions) {
                Handle handle2;
                MethodInsnNode methodInsnNode;
                if (abstractInsnNode instanceof MethodInsnNode) {
                    methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(handle.getName()) && methodInsnNode.desc.equals(string3)) {
                        return false;
                    }
                }
                if (abstractInsnNode instanceof LdcInsnNode) {
                    methodInsnNode = (LdcInsnNode)abstractInsnNode;
                    Object[] objectArray = methodInsnNode.cst;
                    if (objectArray instanceof Handle && (handle2 = (Handle)objectArray).getOwner().equals(classNode.name) && handle2.getName().equals(handle.getName())) {
                        return false;
                    }
                }
                if (!(abstractInsnNode instanceof InvokeDynamicInsnNode)) continue;
                methodInsnNode = (InvokeDynamicInsnNode)abstractInsnNode;
                for (Object object : methodInsnNode.bsmArgs) {
                    if (!(object instanceof Handle) || !(handle2 = (Handle)object).getOwner().equals(classNode.name) || !handle2.getName().equals(handle.getName()) || !handle2.getDesc().equals(string3)) continue;
                    ++n2;
                }
            }
        }
        if (n2 != 1) {
            return false;
        }
        list.add(handle.getName());
        return true;
    }

    private static int[] ownerWindow(ClassNode classNode, MethodNode methodNode, int n, MethodNode methodNode2, int n2, String string, int n3, List<String> list) {
        InvokeDynamicInsnNode invokeDynamicInsnNode;
        VarInsnNode varInsnNode;
        AbstractInsnNode abstractInsnNode;
        AbstractInsnNode abstractInsnNode2;
        MethodInsnNode methodInsnNode;
        Object object;
        int[] nArray;
        boolean bl;
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode);
        int n4 = n2 == 1 ? 5 : (n2 == 4 ? 3 : 1);
        boolean bl2 = bl = n2 >= 3;
        if (n2 < 2) {
            int[] nArray2 = new int[7];
            nArray2[0] = 0;
            nArray2[1] = 1;
            nArray2[2] = 2;
            nArray2[3] = 3;
            nArray2[4] = 4;
            nArray2[5] = 6;
            nArray = nArray2;
            nArray2[6] = 7;
        } else if (n2 == 3) {
            int[] nArray3 = new int[2];
            nArray3[0] = 1;
            nArray = nArray3;
            nArray3[1] = 2;
        } else {
            nArray = new int[]{};
        }
        int[] nArray4 = nArray;
        int n5 = n - nArray4.length - 1;
        int n6 = n5 - n4;
        int n7 = n6 - (bl ? 1 : 0);
        int n8 = n7 - n4;
        if (n8 < 0 || !((object = list2.get(n)) instanceof MethodInsnNode) || (methodInsnNode = (MethodInsnNode)object).getOpcode() != (bl ? 183 : 184) || methodInsnNode.itf || !OwnerFieldInstallationProof.call(list2.get(n), methodNode2, classNode.name)) {
            return null;
        }
        object = new int[n4];
        for (int i = 0; i < n4; ++i) {
            AbstractInsnNode abstractInsnNode3 = list2.get(n8 + i);
            if (!(abstractInsnNode3 instanceof VarInsnNode) || (abstractInsnNode2 = (VarInsnNode)abstractInsnNode3).getOpcode() != 58 || abstractInsnNode2.var < n3 || abstractInsnNode2.var >= methodNode.maxLocals) {
                return null;
            }
            object[i] = (AbstractInsnNode)abstractInsnNode2.var;
            for (int j = 0; j < i; ++j) {
                if (object[j] != abstractInsnNode2.var) continue;
                return null;
            }
            abstractInsnNode = list2.get(n6 + i);
            if (abstractInsnNode instanceof VarInsnNode && (abstractInsnNode3 = (VarInsnNode)abstractInsnNode).getOpcode() == 25 && abstractInsnNode3.var == object[n4 - i - 1]) continue;
            return null;
        }
        if (bl && (!((abstractInsnNode2 = list2.get(n7)) instanceof VarInsnNode) || (varInsnNode = (VarInsnNode)abstractInsnNode2).getOpcode() != 25 || varInsnNode.var != 0)) {
            return null;
        }
        abstractInsnNode2 = list2.get(n5);
        if (!(abstractInsnNode2 instanceof InvokeDynamicInsnNode) || !OwnerFieldInstallationProof.ownerBridge(classNode, invokeDynamicInsnNode = (InvokeDynamicInsnNode)abstractInsnNode2, n2, string, list)) {
            return null;
        }
        for (int i = 0; i < nArray4.length; ++i) {
            abstractInsnNode = list2.get(n5 + 1 + i);
            if (abstractInsnNode instanceof VarInsnNode) {
                abstractInsnNode2 = (VarInsnNode)abstractInsnNode;
                if (abstractInsnNode2.var == nArray4[i] && abstractInsnNode2.getOpcode() == (n2 < 2 && i == 4 ? 22 : 25)) continue;
            }
            return null;
        }
        return new int[]{n8, n};
    }

    private static List<String> ownerSnapshot(ClassNode classNode, int n) {
        VarInsnNode varInsnNode;
        VarInsnNode varInsnNode2;
        AbstractInsnNode abstractInsnNode;
        MethodNode methodNode = OwnerFieldInstallationProof.ownerParent(classNode, n == 0 ? OWNER_QUOTE : (n == 1 ? OWNER_FINAL : OWNER_APPLY), n == 0 ? 10 : (n == 1 ? 9 : 0));
        if (methodNode == null) {
            return null;
        }
        List<String> list = OwnerFieldInstallationProof.ownerShape(classNode, methodNode, List.of(), List.of(), methodNode.maxLocals);
        if (list == null) {
            return null;
        }
        List<AbstractInsnNode> list2 = OwnerFieldInstallationProof.executable(methodNode);
        List<String> list3 = OwnerFieldInstallationProof.body(methodNode, classNode.name, classNode.name);
        if (n == 1) {
            return OwnerFieldInstallationProof.ownerOriginal(list, 2, 3) == null ? null : list;
        }
        if (n == 0) {
            String string = OwnerFieldInstallationProof.ownerOriginal(list, 0, 8);
            String string2 = OwnerFieldInstallationProof.ownerOriginal(list, 1, 7);
            if (string == null || string2 == null) {
                return null;
            }
            int n2 = 0;
            int n3 = 0;
            int n4 = 0;
            for (int i = 0; i < list2.size(); ++i) {
                FieldInsnNode fieldInsnNode;
                VarInsnNode varInsnNode3;
                int n5;
                if (list3.get(i).equals(string) && (i < 1 || !list3.get(i - 1).equals("25:2"))) {
                    return null;
                }
                if (!list3.get(i).equals(string2)) continue;
                for (n5 = i - 1; n5 >= 0 && i - n5 <= 64 && !list3.get(n5).equals("25:0"); --n5) {
                }
                if (n5 < 0 || i - n5 > 64 || n5 + 3 >= i) {
                    return null;
                }
                AbstractInsnNode abstractInsnNode2 = list2.get(n5 + 1);
                if (!(abstractInsnNode2.getOpcode() == 1 || abstractInsnNode2 instanceof VarInsnNode && (varInsnNode3 = (VarInsnNode)abstractInsnNode2).getOpcode() == 25 || abstractInsnNode2 instanceof FieldInsnNode && (fieldInsnNode = (FieldInsnNode)abstractInsnNode2).getOpcode() == 178 && fieldInsnNode.desc.equals("Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptDomain;"))) {
                    return null;
                }
                if (list3.get(n5 + 2).equals("25:2") && list3.get(n5 + 3).equals(string)) {
                    ++n2;
                } else if (list3.get(n5 + 2).equals("25:3")) {
                    ++n3;
                } else {
                    return null;
                }
                if (list2.get(i - 1).getOpcode() == 1) {
                    if (!list3.get(n5 + 2).equals("25:2")) {
                        return null;
                    }
                    ++n4;
                    continue;
                }
                fieldInsnNode = list2.get(i - 1);
                if (fieldInsnNode instanceof MethodInsnNode && (varInsnNode3 = (MethodInsnNode)fieldInsnNode).getOpcode() == 184 && !varInsnNode3.itf && (varInsnNode3.owner.equals("net/minecraft/nbt/IntTag") && varInsnNode3.desc.equals("(I)Lnet/minecraft/nbt/IntTag;") && (varInsnNode3.name.equals("valueOf") || varInsnNode3.name.equals("m_128679_")) || varInsnNode3.owner.equals("net/minecraft/nbt/LongTag") && varInsnNode3.desc.equals("(J)Lnet/minecraft/nbt/LongTag;") && (varInsnNode3.name.equals("valueOf") || varInsnNode3.name.equals("m_128882_")))) continue;
                return null;
            }
            return n2 == 5 && n3 == 2 && n4 == 2 ? list : null;
        }
        String string = OwnerFieldInstallationProof.ownerOriginal(list, 3, 1);
        String string3 = OwnerFieldInstallationProof.ownerOriginal(list, 4, 1);
        String string4 = OwnerFieldInstallationProof.ownerOriginal(list, 5, 1);
        if (string == null || string3 == null || string4 == null) {
            return null;
        }
        int n6 = list3.indexOf(string);
        int n7 = list3.indexOf(string3);
        int n8 = list3.indexOf(string4);
        String string5 = "180:" + classNode.name + ":data:Lnet/minecraft/nbt/CompoundTag;";
        String string6 = "180:" + classNode.name + ":key:Ljava/lang/String;";
        if (!(n6 >= 2 && n7 >= 5 && n8 >= 4 && n6 + 1 < list2.size() && n7 + 1 < list2.size() && list3.subList(n6 - 2, n6).equals(List.of("25:0", "180:" + classNode.name + ":value:Lnet/minecraft/nbt/Tag;")) && (abstractInsnNode = list2.get(n6 + 1)) instanceof VarInsnNode && (varInsnNode2 = (VarInsnNode)abstractInsnNode).getOpcode() == 58 && list3.subList(n7 - 5, n7).equals(List.of("25:0", string5, "25:0", string6, "25:" + varInsnNode2.var)) && (abstractInsnNode = list2.get(n7 + 1)) instanceof VarInsnNode && (varInsnNode = (VarInsnNode)abstractInsnNode).getOpcode() == 58 && list3.subList(n8 - 4, n8).equals(List.of("25:0", string5, "25:0", string6)))) {
            return null;
        }
        return list;
    }

    static List<List<String>> ownerParentsSnapshot(ClassNode classNode) {
        if (classNode.name.equals("net/farmingtales/ftboss/item/FoodWeaponRanged")) {
            return OwnerFieldInstallationProof.rangedParentSnapshot(classNode);
        }
        if (classNode.name.equals("net/farmingtales/ftboss/item/FoodWeaponEvolution")) {
            List<String> list = OwnerFieldInstallationProof.ownerSnapshot(classNode, 0);
            List<String> list2 = OwnerFieldInstallationProof.ownerSnapshot(classNode, 1);
            return list == null || list2 == null ? null : List.of(list, list2);
        }
        if (classNode.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf")) {
            List<String> list = OwnerFieldInstallationProof.ownerSnapshot(classNode, 2);
            return list == null ? null : List.of(list);
        }
        return null;
    }

    static boolean ownerWrapper(String[] stringArray, ClassNode classNode, ClassNode classNode2, List<List<String>> list) {
        int n;
        List<String> list2;
        Object object;
        int[] nArray;
        int[] nArray2;
        int n2 = OwnerFieldInstallationProof.ownerSlot(stringArray);
        if (n2 < 2 || classNode == null || classNode2 == null || list == null || list.size() != (n2 == 2 ? 2 : 1) || !stringArray[0].equals(classNode2.name.replace('/', '.')) || !stringArray[1].equals(classNode.name.replace('/', '.'))) {
            return false;
        }
        if (n2 == 2) {
            int[] nArray3 = new int[4];
            nArray3[0] = 2;
            nArray3[1] = 2;
            nArray3[2] = 0;
            nArray2 = nArray3;
            nArray3[3] = 1;
        } else {
            int[] nArray4 = new int[2];
            nArray4[0] = 3;
            nArray2 = nArray4;
            nArray4[1] = 4;
        }
        int[] nArray5 = nArray2;
        if (n2 == 2) {
            int[] nArray6 = new int[4];
            nArray6[0] = 1;
            nArray6[1] = 1;
            nArray6[2] = 8;
            nArray = nArray6;
            nArray6[3] = 7;
        } else {
            int[] nArray7 = new int[2];
            nArray7[0] = 1;
            nArray = nArray7;
            nArray7[1] = 1;
        }
        int[] nArray8 = nArray;
        MethodNode[] methodNodeArray = new MethodNode[nArray5.length];
        for (int i = 0; i < methodNodeArray.length; ++i) {
            object = stringArray[i + 2];
            methodNodeArray[i] = OwnerFieldInstallationProof.ownerHandler(classNode, classNode2, (String)object, n2 == 3);
            if (methodNodeArray[i] == null) {
                return false;
            }
            String string = OwnerFieldInstallationCatalogue.descriptor(stringArray[0], (String)object);
            list2 = classNode2.methods.stream().filter(arg_0 -> OwnerFieldInstallationProof.lambda$ownerWrapper$24((String)object, string, arg_0)).toList();
            if (list2.size() == 1 && OwnerFieldInstallationProof.ownerAnnotation((MethodNode)list2.get(0), nArray5[i], i + 1)) continue;
            return false;
        }
        int[] nArray9 = new int[methodNodeArray.length];
        object = new ArrayList();
        for (n = 0; n < list.size(); ++n) {
            Object object2;
            list2 = list.get(n);
            if (list2 == null || list2.isEmpty() || !list2.get(0).startsWith("locals:")) {
                return false;
            }
            int n3 = Integer.parseInt(list2.get(0).substring(7));
            String string = n2 == 3 ? OWNER_APPLY : (object2 = n == 0 ? OWNER_QUOTE : OWNER_FINAL);
            ListIterator listIterator = OwnerFieldInstallationProof.ownerParent(classNode, (String)object2, n2 == 3 ? 0 : (n == 0 ? 10 : 9));
            if (listIterator == null) {
                return false;
            }
            Object object3 = OwnerFieldInstallationProof.executable((MethodNode)listIterator);
            List<String> list3 = OwnerFieldInstallationProof.body((MethodNode)listIterator, classNode.name, classNode.name);
            ArrayList<int[]> arrayList = new ArrayList<int[]>();
            ArrayList<String> arrayList2 = new ArrayList<String>();
            int n4 = 0;
            for (int i = 0; i < object3.size(); ++i) {
                if (n2 == 2 && n == 1 && (list3.get(i).equals(OwnerFieldInstallationProof.ownerOperation(2, false)) || list3.get(i).equals(OwnerFieldInstallationProof.ownerOperation(2, true)))) {
                    ++n4;
                }
                for (int j = 0; j < methodNodeArray.length; ++j) {
                    if (!OwnerFieldInstallationProof.call((AbstractInsnNode)object3.get(i), methodNodeArray[j], classNode.name)) continue;
                    int n5 = nArray5[j];
                    if (n2 == 2 && n == 0 != n5 < 2) {
                        return false;
                    }
                    if (n5 == 2 && n4++ != j + 1) {
                        return false;
                    }
                    int n6 = n2 == 2 ? (n5 == 0 ? 8 : (n5 == 1 ? 7 : 3)) : 1;
                    String string2 = OwnerFieldInstallationProof.ownerOriginal(list2, n5, n6);
                    if (string2 == null) {
                        return false;
                    }
                    int[] nArray10 = OwnerFieldInstallationProof.ownerWindow(classNode, (MethodNode)listIterator, i, methodNodeArray[j], n5, string2, n3, (List<String>)object);
                    if (nArray10 == null) {
                        return false;
                    }
                    arrayList.add(nArray10);
                    arrayList2.add(string2);
                    int n7 = j;
                    nArray9[n7] = nArray9[n7] + 1;
                }
            }
            if (n2 == 2 && n == 1 && n4 != 3) {
                return false;
            }
            if (list2.equals(OwnerFieldInstallationProof.ownerShape(classNode, (MethodNode)listIterator, arrayList, arrayList2, n3))) continue;
            return false;
        }
        for (n = 0; n < methodNodeArray.length; ++n) {
            if (nArray9[n] != nArray8[n]) {
                return false;
            }
            int n8 = 0;
            for (Object object2 : classNode.methods) {
                for (Object object3 : object2.instructions) {
                    if (!OwnerFieldInstallationProof.call((AbstractInsnNode)object3, methodNodeArray[n], classNode.name)) continue;
                    ++n8;
                }
            }
            if (n8 == nArray8[n]) continue;
            return false;
        }
        return true;
    }

    static boolean complete(String[] stringArray, ClassNode classNode, ClassNode classNode2) {
        if (classNode == null || classNode2 == null || !classNode2.name.replace('/', '.').equals(stringArray[0]) || !classNode.name.replace('/', '.').equals(stringArray[1])) {
            return false;
        }
        if (OwnerFieldInstallationProof.canonicalAliasSite(stringArray)) {
            if (stringArray[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin")) {
                return OwnerFieldInstallationProof.canonicalRangedWrapper(stringArray, classNode, classNode2, OwnerFieldCoverageRegistry.rangedParents(stringArray, classNode, classNode2));
            }
            if (OwnerFieldInstallationProof.canonicalTimerHandler(classNode, classNode2, true) == null) {
                return false;
            }
            ClassNode classNode3 = OwnerFieldCoverageRegistry.timerSource("net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", classNode);
            return OwnerFieldInstallationProof.timerWrapper(new String[]{"net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$r2$timerPut", "ftboss$r2$timerPutSrg"}, classNode, classNode3, OwnerFieldCoverageRegistry.timerParent(classNode, classNode3));
        }
        if (OwnerFieldInstallationProof.lateTimerSite(stringArray)) {
            return OwnerFieldInstallationProof.timerWrapper(stringArray, classNode, classNode2, OwnerFieldCoverageRegistry.timerParent(classNode, classNode2));
        }
        if (OwnerFieldInstallationProof.ownerSlot(stringArray) >= 2) {
            return OwnerFieldInstallationProof.ownerWrapper(stringArray, classNode, classNode2, OwnerFieldCoverageRegistry.ownerParents(stringArray, classNode, classNode2));
        }
        for (int i = 2; i < stringArray.length; ++i) {
            String string = stringArray[i];
            String string2 = OwnerFieldInstallationCatalogue.descriptor(stringArray[0], string);
            if (string2 == null) {
                return false;
            }
            List<MethodNode> list = classNode2.methods.stream().filter(methodNode -> methodNode.name.equals(string) && methodNode.desc.equals(string2)).toList();
            List<MethodNode> list2 = classNode.methods.stream().filter(methodNode -> OwnerFieldInstallationProof.member(methodNode.name).equals(string) && methodNode.desc.equals(string2) && OwnerFieldInstallationProof.merged(methodNode, stringArray[0])).toList();
            if (list.size() != 1 || list2.size() != 1) {
                return false;
            }
            MethodNode methodNode2 = list.get(0);
            MethodNode methodNode3 = list2.get(0);
            if (!(OwnerFieldInstallationProof.annotations(methodNode2).stream().anyMatch(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;")) ? !OwnerFieldInstallationProof.accessorGetter(stringArray, classNode, classNode2, methodNode2, methodNode3) : (OwnerFieldInstallationProof.lateMapSite(stringArray) ? !OwnerFieldInstallationProof.mapWrapper(classNode, classNode2, methodNode2, methodNode3) : !OwnerFieldInstallationProof.body(methodNode2, classNode2.name, classNode.name).equals(OwnerFieldInstallationProof.body(methodNode3, classNode.name, classNode.name)) || !OwnerFieldInstallationProof.siteAndOrder(classNode, methodNode2, methodNode3)))) continue;
            return false;
        }
        return true;
    }

    private static /* synthetic */ boolean lambda$ownerWrapper$24(String string, String string2, MethodNode methodNode) {
        return methodNode.name.equals(string) && methodNode.desc.equals(string2);
    }

    private static /* synthetic */ boolean lambda$canonicalRangedWrapper$19(String string, String[] stringArray, MethodNode methodNode) {
        return methodNode.name.equals(string) && methodNode.desc.equals(OwnerFieldInstallationCatalogue.descriptor(stringArray[0], string));
    }

    private static /* synthetic */ boolean lambda$accessorGetter$3(FieldInsnNode fieldInsnNode, FieldNode fieldNode) {
        return fieldNode.name.equals(fieldInsnNode.name) && fieldNode.desc.equals(fieldInsnNode.desc);
    }
}

