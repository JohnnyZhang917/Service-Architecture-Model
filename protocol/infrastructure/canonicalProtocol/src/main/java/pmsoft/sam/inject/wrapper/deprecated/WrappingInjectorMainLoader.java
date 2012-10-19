package pmsoft.sam.inject.wrapper.deprecated;

import java.net.URL;

import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionService;

import com.google.inject.Module;

public interface WrappingInjectorMainLoader extends WrappingInjectorLoader {

	public Module createWrappingInjector(CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL);

}
