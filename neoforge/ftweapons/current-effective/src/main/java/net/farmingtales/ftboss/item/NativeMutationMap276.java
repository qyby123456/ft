package net.farmingtales.ftboss.item;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class NativeMutationMap276<K, V> implements Map<K, V> {
    private final Map<K, V> delegate;
    private final Object nativeOwner;
    private final Runnable mutation;
    private volatile boolean observationFailed;
    private Set<K> keys;
    private Collection<V> values;
    private Set<Entry<K, V>> entries;
    private Set<K> delegateKeys;
    private Collection<V> delegateValues;
    private Set<Entry<K, V>> delegateEntries;

    public NativeMutationMap276(Map<K, V> delegate, Runnable mutation) {
        this(null, delegate, mutation);
    }

    public NativeMutationMap276(Object nativeOwner, Map<K, V> delegate, Runnable mutation) {
        this.nativeOwner = nativeOwner;
        this.delegate = Objects.requireNonNull(delegate);
        this.mutation = Objects.requireNonNull(mutation);
    }

    public boolean observationFailed() { return observationFailed; }

    boolean belongsTo(Object owner) { return owner != null && nativeOwner == owner && delegate.getClass() == java.util.HashMap.class && !observationFailed; }

    private void changing() {
        try { mutation.run(); }
        catch (Throwable failure) { observationFailed = true; }
    }

    @Override public int size() { return delegate.size(); }
    @Override public boolean isEmpty() { return delegate.isEmpty(); }
    @Override public boolean containsKey(Object key) { return delegate.containsKey(key); }
    @Override public boolean containsValue(Object value) { return delegate.containsValue(value); }
    @Override public V get(Object key) { return delegate.get(key); }
    @Override public V getOrDefault(Object key, V fallback) { return delegate.getOrDefault(key, fallback); }
    @Override public void forEach(BiConsumer<? super K, ? super V> action) { delegate.forEach(action); }
    @Override public V put(K key, V value) { changing(); return delegate.put(key, value); }
    @Override public V remove(Object key) { changing(); return delegate.remove(key); }
    @Override public void putAll(Map<? extends K, ? extends V> input) { changing(); delegate.putAll(input); }
    @Override public void clear() { changing(); delegate.clear(); }
    @Override public V putIfAbsent(K key, V value) { changing(); return delegate.putIfAbsent(key, value); }
    @Override public boolean remove(Object key, Object value) { changing(); return delegate.remove(key, value); }
    @Override public boolean replace(K key, V oldValue, V newValue) { changing(); return delegate.replace(key, oldValue, newValue); }
    @Override public V replace(K key, V value) { changing(); return delegate.replace(key, value); }
    @Override public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) { changing(); delegate.replaceAll(function); }
    @Override public V computeIfAbsent(K key, Function<? super K, ? extends V> function) { changing(); return delegate.computeIfAbsent(key, function); }
    @Override public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> function) { changing(); return delegate.computeIfPresent(key, function); }
    @Override public V compute(K key, BiFunction<? super K, ? super V, ? extends V> function) { changing(); return delegate.compute(key, function); }
    @Override public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> function) { changing(); return delegate.merge(key, value, function); }
    @Override public boolean equals(Object other) { return other == this || delegate.equals(other); }
    @Override public int hashCode() { return delegate.hashCode(); }
    @Override public String toString() { return delegate.toString(); }

    private <T> Iterator<T> iterator(Iterator<T> original) {
        return new Iterator<>() {
            @Override public boolean hasNext() { return original.hasNext(); }
            @Override public T next() { return original.next(); }
            @Override public void remove() { changing(); original.remove(); }
            @Override public void forEachRemaining(Consumer<? super T> action) { original.forEachRemaining(action); }
        };
    }

    private Entry<K, V> entry(Entry<K, V> original) {
        return new Entry<>() {
            @Override public K getKey() { return original.getKey(); }
            @Override public V getValue() { return original.getValue(); }
            @Override public V setValue(V value) { changing(); return original.setValue(value); }
            @Override public boolean equals(Object other) { return other == this || original.equals(other); }
            @Override public int hashCode() { return original.hashCode(); }
            @Override public String toString() { return original.toString(); }
        };
    }

    private Iterator<Entry<K, V>> entryIterator(Iterator<Entry<K, V>> original) {
        return new Iterator<>() {
            @Override public boolean hasNext() { return original.hasNext(); }
            @Override public Entry<K, V> next() { return entry(original.next()); }
            @Override public void remove() { changing(); original.remove(); }
            @Override public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                original.forEachRemaining(action == null ? null : value -> action.accept(entry(value)));
            }
        };
    }

    private Spliterator<Entry<K, V>> entrySpliterator(Spliterator<Entry<K, V>> original) {
        return new Spliterator<>() {
            @Override public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                return original.tryAdvance(action == null ? null : value -> action.accept(entry(value)));
            }
            @Override public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                original.forEachRemaining(action == null ? null : value -> action.accept(entry(value)));
            }
            @Override public Spliterator<Entry<K, V>> trySplit() {
                Spliterator<Entry<K, V>> split = original.trySplit();
                return split == null ? null : entrySpliterator(split);
            }
            @Override public long estimateSize() { return original.estimateSize(); }
            @Override public int characteristics() { return original.characteristics(); }
            @Override public Comparator<? super Entry<K, V>> getComparator() { return original.getComparator(); }
        };
    }

    @Override public Set<K> keySet() {
        Set<K> original = delegate.keySet();
        if (keys == null || original != delegateKeys) {
            delegateKeys = original;
            keys = new AbstractSet<>() {
                @Override public int size() { return original.size(); }
                @Override public boolean contains(Object value) { return original.contains(value); }
                @Override public Iterator<K> iterator() { return NativeMutationMap276.this.iterator(original.iterator()); }
                @Override public Spliterator<K> spliterator() { return original.spliterator(); }
                @Override public void forEach(Consumer<? super K> action) { original.forEach(action); }
                @Override public boolean remove(Object value) { changing(); return original.remove(value); }
                @Override public boolean removeAll(Collection<?> input) { changing(); return original.removeAll(input); }
                @Override public boolean retainAll(Collection<?> input) { changing(); return original.retainAll(input); }
                @Override public boolean removeIf(Predicate<? super K> predicate) { changing(); return original.removeIf(predicate); }
                @Override public void clear() { changing(); original.clear(); }
            };
        }
        return keys;
    }

    @Override public Collection<V> values() {
        Collection<V> original = delegate.values();
        if (values == null || original != delegateValues) {
            delegateValues = original;
            values = new AbstractCollection<>() {
                @Override public int size() { return original.size(); }
                @Override public boolean contains(Object value) { return original.contains(value); }
                @Override public Iterator<V> iterator() { return NativeMutationMap276.this.iterator(original.iterator()); }
                @Override public Spliterator<V> spliterator() { return original.spliterator(); }
                @Override public void forEach(Consumer<? super V> action) { original.forEach(action); }
                @Override public boolean remove(Object value) { changing(); return original.remove(value); }
                @Override public boolean removeAll(Collection<?> input) { changing(); return original.removeAll(input); }
                @Override public boolean retainAll(Collection<?> input) { changing(); return original.retainAll(input); }
                @Override public boolean removeIf(Predicate<? super V> predicate) { changing(); return original.removeIf(predicate); }
                @Override public void clear() { changing(); original.clear(); }
            };
        }
        return values;
    }

    @Override public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> original = delegate.entrySet();
        if (entries == null || original != delegateEntries) {
            delegateEntries = original;
            entries = new AbstractSet<>() {
                @Override public int size() { return original.size(); }
                @Override public boolean contains(Object value) { return original.contains(value); }
                @Override public Iterator<Entry<K, V>> iterator() { return entryIterator(original.iterator()); }
                @Override public Spliterator<Entry<K, V>> spliterator() { return entrySpliterator(original.spliterator()); }
                @Override public void forEach(Consumer<? super Entry<K, V>> action) {
                    original.forEach(action == null ? null : value -> action.accept(entry(value)));
                }
                @Override public boolean remove(Object value) { changing(); return original.remove(value); }
                @Override public boolean removeAll(Collection<?> input) { changing(); return original.removeAll(input); }
                @Override public boolean retainAll(Collection<?> input) { changing(); return original.retainAll(input); }
                @Override public boolean removeIf(Predicate<? super Entry<K, V>> predicate) {
                    changing();
                    return original.removeIf(predicate == null ? null : value -> predicate.test(entry(value)));
                }
                @Override public void clear() { changing(); original.clear(); }
            };
        }
        return entries;
    }
}
