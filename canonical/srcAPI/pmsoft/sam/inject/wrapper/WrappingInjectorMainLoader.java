package pmsoft.sam.inject.wrapper;

import java.net.URL;

import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;

import com.google.inject.Module;

public interface WrappingInjectorMainLoader extends WrappingInjectorLoader {

	public Module createWrappingInjector(CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL);

}
