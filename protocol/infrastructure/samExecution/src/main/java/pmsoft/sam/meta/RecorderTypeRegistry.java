package pmsoft.sam.meta;


import java.util.Map;

import ch.lambdaj.function.convert.Converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.inject.Provider;
import static ch.lambdaj.Lambda.*;


public class RecorderTypeRegistry<K,T> implements TypeRegistry<K,T> {

	private BiMap<K, T> recorders = HashBiMap.create();
	
	private final Provider<T> provider;
	
	public RecorderTypeRegistry(Provider<T> provider) {
		super();
		this.provider = provider;
	}

	public T createTypeInstance(){
		return provider.get();
	}
	
	public T createInstance(K key) {
		Preconditions.checkNotNull(key);
		Preconditions.checkState(!recorders.containsKey(key));
		T rec = createTypeInstance();
		recorders.put(key, rec);
		return rec;
	}
	
	public K getInstanceKey(T recorder) {
		return recorders.inverse().get(recorder);
	}

	public <V> ImmutableBiMap<K, V> createImmutableView(Converter<T, V> converter) {
		Map<K, V> changed = convertMap(recorders, converter);
		return ImmutableBiMap.copyOf(changed);
	}
	
	
	
}
