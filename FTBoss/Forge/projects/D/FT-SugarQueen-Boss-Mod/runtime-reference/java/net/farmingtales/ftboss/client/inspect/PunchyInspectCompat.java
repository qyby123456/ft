/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.inspect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PunchyInspectCompat {
    private static Object lastPunchyData;
    private static Object lastMotionLibrary;

    private PunchyInspectCompat() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void ensureInstalled(Object object) {
        if (object == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("punchy.config.PunchyConfig", false, PunchyInspectCompat.class.getClassLoader());
            Field field = clazz.getDeclaredField("DATA");
            field.setAccessible(true);
            Object object2 = field.get(null);
            if (object2 == null || object2 == lastPunchyData && object == lastMotionLibrary) {
                return;
            }
            Set<String> set = PunchyInspectCompat.collectInspectableIds(object);
            if (set.isEmpty()) {
                lastPunchyData = object2;
                lastMotionLibrary = object;
                return;
            }
            Field field2 = object2.getClass().getField("itemBlacklist");
            ArrayList<String> arrayList = (ArrayList<String>)field2.get(object2);
            if (arrayList == null) {
                arrayList = new ArrayList<String>();
                field2.set(object2, arrayList);
            }
            ArrayList<String> arrayList2 = arrayList;
            synchronized (arrayList2) {
                for (String string : set) {
                    if (arrayList.contains(string)) continue;
                    arrayList.add(string);
                }
            }
            lastPunchyData = object2;
            lastMotionLibrary = object;
        }
        catch (ClassNotFoundException classNotFoundException) {
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static Set<String> collectInspectableIds(Object object) throws ReflectiveOperationException {
        Object object2;
        Object object3;
        Object object4;
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        Method method = PunchyInspectCompat.accessibleMethod(object.getClass(), "items");
        Object object5 = method.invoke(object, new Object[0]);
        if (object5 instanceof Map) {
            object4 = (Map)object5;
            object3 = object4.keySet().iterator();
            while (object3.hasNext()) {
                object2 = object3.next();
                PunchyInspectCompat.addIdentifier(linkedHashSet, object2);
            }
        }
        if ((object3 = ((Method)(object4 = PunchyInspectCompat.accessibleMethod(object.getClass(), "variants"))).invoke(object, new Object[0])) instanceof Iterable) {
            object2 = (Iterable)object3;
            Iterator iterator = object2.iterator();
            while (iterator.hasNext()) {
                Object t = iterator.next();
                if (t == null) continue;
                Method method2 = PunchyInspectCompat.accessibleMethod(t.getClass(), "item");
                PunchyInspectCompat.addIdentifier(linkedHashSet, method2.invoke(t, new Object[0]));
            }
        }
        return linkedHashSet;
    }

    private static Method accessibleMethod(Class<?> clazz, String string) throws ReflectiveOperationException {
        Method method;
        try {
            method = clazz.getDeclaredMethod(string, new Class[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            method = clazz.getMethod(string, new Class[0]);
        }
        method.setAccessible(true);
        return method;
    }

    private static void addIdentifier(Set<String> set, Object object) {
        if (!(object instanceof String)) {
            return;
        }
        String string = (String)object;
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        if (!string2.isEmpty() && string2.indexOf(58) > 0) {
            set.add(string2);
        }
    }
}

