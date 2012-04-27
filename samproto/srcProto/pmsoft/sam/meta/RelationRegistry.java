package pmsoft.sam.meta;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.ImmutableSetMultimap;

public interface RelationRegistry<T, V> {

	boolean addRelationEntry(T source, V target);

	boolean removeRelationEntry(T source, V target);

	public <K, R> ImmutableSetMultimap<K, R> createImmutableView(Converter<T, K> keyConverter, Converter<V, R> valueConverter);

	public <K> ImmutableSetMultimap<K, V> createImmutableView(Converter<T, K> keyConverter);
	
	public RelationRegistry<V, T> inverse();

}
