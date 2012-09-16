package pmsoft.sam.meta;

import pmsoft.sam.definition.architecture.SamArchitectureDefinition;
import pmsoft.sam.model.architecture.SamArchitecture;

public interface SamArchitectureParser {

	public SamArchitecture createArchitectureModel(SamArchitectureDefinition definition);
}
