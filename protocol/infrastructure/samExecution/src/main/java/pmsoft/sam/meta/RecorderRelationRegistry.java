package pmsoft.sam.meta;

import java.util.Collection;
import java.util.Set;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class RecorderRelationRegistry<T, V> implements RelationRegistry<T, V> {

	private final Multimap<T, V> relationValues = HashMultimap.create();

	private RecorderRelationRegistry<V, T> mainRelation;

	public RecorderRelationRegistry() {
	}

	private RecorderRelationRegistry(RecorderRelationRegistry<V, T> inverted) {
		this.mainRelation = inverted;
	}

	public RelationRegistry<V, T> inverse() {
		if (mainRelation != null) {
			return mainRelation;
		}
		return new RecorderRelationRegistry<V, T>(this);
	}

	public boolean addRelationEntry(T from, V to) {
		if (mainRelation != null) {
			return mainRelation.addRelationEntry(to, from);
		}
		return relationValues.put(from, to);
	}

	public boolean removeRelationEntry(T source, V target) {
		if (mainRelation != null) {
			return mainRelation.removeRelationEntry(target, source);
		}
		return relationValues.remove(source, target);
	}

	public <K> ImmutableSetMultimap<K, V> createImmutableView(Converter<T, K> converter) {
		Multimap<T, V> relationValuesView = relationValues;
		if (mainRelation != null) {
			Multimap<T, V> clearView = HashMultimap.create();
			relationValuesView = Multimaps.invertFrom(mainRelation.relationValues, clearView);
		}
		Builder<K, V> builder = ImmutableSetMultimap.builder();
		Set<T> typeInstance = relationValuesView.keySet();
		for (T t : typeInstance) {
			K key = converter.convert(t);
			Collection<V> values = relationValuesView.get(t);
			builder.putAll(key, values);
		}
		return builder.build();
	}

	public <K, R> ImmutableSetMultimap<K, R> createImmutableView(Converter<T, K> keyConverter, Converter<V, R> valueConverter) {
		Multimap<T, V> relationValuesView = relationValues;
		if (mainRelation != null) {
			Multimap<T, V> clearView = HashMultimap.create();
			relationValuesView = Multimaps.invertFrom(mainRelation.relationValues, clearView);
		}
		Builder<K, R> builder = ImmutableSetMultimap.builder();
		Set<T> typeInstance = relationValuesView.keySet();
		for (T t : typeInstance) {
			K key = keyConverter.convert(t);
			Collection<V> values = relationValuesView.get(t);
			for (V v : values) {
				builder.put(key, valueConverter.convert(v));
			}
		}
		return builder.build();
	}

}
