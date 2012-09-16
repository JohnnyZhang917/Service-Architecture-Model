package pmsoft.sam.meta;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.ImmutableBiMap;


public interface TypeRegistry<K, T> {
	public T createInstance(K key);

	public K getInstanceKey(T recorder);

	public <V> ImmutableBiMap<K, V> createImmutableView(Converter<T, V> converter);
}
