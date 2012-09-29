package pmsoft.sam.module.see.transaction;

import java.net.URL;

import com.google.inject.Injector;

public interface SamTransaction {

	public Injector getInjector();
	
	public URL getTransactionURL();

}
