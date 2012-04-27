package pmsoft.sam.module.definition.architecture;

import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;

public interface SamServiceDefinition {
	public SamServiceLoader loadServiceDefinition(SamArchitectureLoader loader);
}
