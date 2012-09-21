package pmsoft.sam.meta;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.architecture.model.SamArchitecture;

public interface SamArchitectureParser {

	public SamArchitecture createArchitectureModel(SamArchitectureDefinition definition);
}
