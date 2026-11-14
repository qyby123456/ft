package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

public final class NativeOpaqueProviderReadFence201 {
    private static final int LIMIT = 65536;
    private static final List<Watch> WATCHES = new ArrayList<>();

    private NativeOpaqueProviderReadFence201() {
    }

    public static void before(Object provider,ItemStack stack) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeExpectedFieldObservation.class || !caller.getMethodName().equals("nativeProviderQueryBefore")
            || !caller.getDescriptor().equals("(Ljava/lang/Object;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;")) {
            throw new SecurityException("Actual original provider invocation required");
        }
        if (!(provider instanceof ICapabilityProvider) || stack == null) return;
        NativeItemHandlerLifetime232.providerReferences(provider, stack);
        Graph graph = null;
        try { graph = Graph.capture(provider); }
        catch (ReflectiveOperationException | RuntimeException unsupported) { }
        boolean exhausted;
        synchronized (NativeOpaqueProviderReadFence201.class) {
            compact();
            for (int index = 0; index < WATCHES.size(); index++) {
                Watch previous = WATCHES.get(index);
                if (previous.owner.get() == stack && previous.provider.get() == provider) {
                    WATCHES.set(index,new Watch(new WeakReference<>(stack),new WeakReference<>(provider),graph,Thread.currentThread()));
                    return;
                }
            }
            exhausted = WATCHES.size() >= LIMIT;
            if (!exhausted) WATCHES.add(new Watch(new WeakReference<>(stack),new WeakReference<>(provider),graph,Thread.currentThread()));
        }
        if (exhausted) OwnerFullRepairGuard.changed(stack,40);
    }

    public static void read(ItemStack stack) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeReadCutObserver.class || !caller.getMethodName().equals("weapon")
            || !caller.getDescriptor().equals("(Ljava/lang/Object;)V")) throw new SecurityException("Actual weapon read fence required");
        List<Watch> pending;
        synchronized (NativeOpaqueProviderReadFence201.class) {
            compact();
            pending = WATCHES.stream().filter(watch -> watch.owner.get() == stack).toList();
        }
        boolean changed = false;
        for (Watch watch : pending) {
            Object provider = watch.provider.get();
            if (provider == null) continue;
            Graph current = null;
            boolean same = watch.thread == Thread.currentThread();
            if (same) try { current = Graph.capture(provider);same = watch.graph != null && watch.graph.same(current); }
            catch (ReflectiveOperationException | RuntimeException unsupported) { same = false; }
            if (!same) {
                synchronized (NativeOpaqueProviderReadFence201.class) {
                    for (int index = 0; index < WATCHES.size(); index++) {
                        if (WATCHES.get(index) == watch) {
                            if (current != null) WATCHES.set(index,new Watch(watch.owner,watch.provider,current,watch.thread));
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
        if (changed) OwnerFullRepairGuard.changed(stack,20);
    }

    private static void compact() {
        Iterator<Watch> entries = WATCHES.iterator();
        while (entries.hasNext()) {
            Watch watch = entries.next();
            if (watch.owner.get() == null || watch.provider.get() == null) entries.remove();
        }
    }

    private record Watch(WeakReference<ItemStack> owner,WeakReference<Object> provider,Graph graph,Thread thread) {
    }

    private static final class Graph {
        private final IdentityHashMap<Object,Boolean> seen = new IdentityHashMap<>();
        private final List<Value> values = new ArrayList<>();
        private int nodes;
        private int slots;

        private static Graph capture(Object provider) throws ReflectiveOperationException {
            Graph graph = new Graph();
            graph.walk(provider,0);
            graph.seen.clear();
            return graph;
        }

        private void value(Object value,boolean primitive) {
            if (++slots > 32768) throw new IllegalArgumentException("Opaque provider physical field budget");
            if (primitive && value instanceof Float number) value = Float.floatToRawIntBits(number);
            if (primitive && value instanceof Double number) value = Double.doubleToRawLongBits(number);
            values.add(primitive ? new Value(null,value,true) : new Value(value == null ? null : new WeakReference<>(value),null,false));
        }

        private static boolean immutable(Class<?> type) {
            return type == String.class || type == Class.class || type == Boolean.class || type == Byte.class || type == Short.class
                || type == Character.class || type == Integer.class || type == Long.class || type == Float.class || type == Double.class;
        }

        private void walk(Object object,int depth) throws ReflectiveOperationException {
            value(object,false);
            if (object == null || immutable(object.getClass()) || seen.put(object,Boolean.TRUE) != null) return;
            if (depth > 64 || ++nodes > 4096) throw new IllegalArgumentException("Opaque provider graph depth or object budget");
            Class<?> type = object.getClass();
            if (type.isArray()) {
                int length = Array.getLength(object);
                if (length > 32768 - slots) throw new IllegalArgumentException("Opaque provider array budget");
                value(length,true);
                for (int index = 0; index < length; index++) {
                    Object element = Array.get(object,index);
                    if (type.getComponentType().isPrimitive()) value(element,true);else walk(element,depth+1);
                }
                return;
            }
            if (type == ArrayList.class) {
                if (((ArrayList<?>)object).size() > 32768 - slots) throw new IllegalArgumentException("Opaque provider list budget");
                Object[] entries = ((ArrayList<?>)object).toArray();
                value(entries.length,true);
                for (Object entry : entries) walk(entry,depth+1);
                return;
            }
            if (type == IdentityHashMap.class || type == HashMap.class || type == LinkedHashMap.class) {
                Map<?,?> entries = (Map<?,?>)object;
                if (entries.size() > (32768 - slots) / 2) throw new IllegalArgumentException("Opaque provider map budget");
                value(entries.size(),true);
                for (Map.Entry<?,?> entry : entries.entrySet()) { walk(entry.getKey(),depth+1);walk(entry.getValue(),depth+1); }
                return;
            }
            if (type.getName().startsWith("java.") || type.getName().startsWith("jdk.") || type.getName().startsWith("sun.")) {
                throw new IllegalArgumentException("Unobservable JDK provider state");
            }
            List<Field> fields = new ArrayList<>();
            for (Class<?> owner = type; owner != Object.class; owner = owner.getSuperclass()) {
                if (owner == null) throw new IllegalArgumentException("Invalid provider class graph");
                for (Field field : owner.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (!Modifier.isFinal(field.getModifiers()) || !field.getType().isPrimitive() && field.getType() != String.class) {
                            throw new IllegalArgumentException("Provider static state is outside instance read fence");
                        }
                    } else {
                        if (fields.size() >= 32768 - slots) throw new IllegalArgumentException("Opaque provider declared field budget");
                        fields.add(field);
                    }
                }
            }
            fields.sort(Comparator.comparing((Field field) -> field.getDeclaringClass().getName()).thenComparing(Field::getName));
            for (Field field : fields) {
                if (!field.trySetAccessible()) throw new IllegalAccessException("Opaque provider field is inaccessible");
                Object actual = field.get(object);
                if (field.getType().isPrimitive()) value(actual,true);else walk(actual,depth+1);
            }
        }

        private boolean same(Graph current) {
            if (values.size() != current.values.size() || nodes != current.nodes) return false;
            for (int index = 0; index < values.size(); index++) {
                Value before = values.get(index), after = current.values.get(index);
                if (before.primitive != after.primitive) return false;
                if (before.primitive) {
                    if (!before.scalar.equals(after.scalar)) return false;
                } else if (before.reference == null || after.reference == null) {
                    if (before.reference != after.reference) return false;
                } else {
                    Object original = before.reference.get();
                    if (original == null || original != after.reference.get()) return false;
                }
            }
            return true;
        }
    }

    private record Value(WeakReference<Object> reference,Object scalar,boolean primitive) {
    }
}
