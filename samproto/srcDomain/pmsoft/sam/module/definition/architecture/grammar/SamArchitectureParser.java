package pmsoft.sam.module.definition.architecture.grammar;

import pmsoft.sam.module.definition.architecture.SamArchitectureDefinition;
import pmsoft.sam.module.model.SamArchitecture;

public interface SamArchitectureParser {

	public SamArchitecture createArchitectureModel(SamArchitectureDefinition definition);
}
