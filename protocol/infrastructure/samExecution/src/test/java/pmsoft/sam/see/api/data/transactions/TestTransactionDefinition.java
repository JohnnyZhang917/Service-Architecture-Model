package pmsoft.sam.see.api.data.transactions;

import static pmsoft.sam.see.api.transaction.SamTransactionConfigurationUtil.createTransactionOn;
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;
import pmsoft.sam.see.api.data.architecture.TestServiceZero;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

/**
 * Transaction definition based on definition of the service implementations.
 * 
 * @author pawel
 * 
 */
public class TestTransactionDefinition {

	public static SamInjectionConfiguration createServiceZeroConfiguration(SIID serviceInstance) {
		return createTransactionOn(TestServiceZero.class).providedByServiceInstance(serviceInstance);
	}
	
	public static SamInjectionConfiguration createServiceOneConfiguration(SIID serviceInstance) {
		return createTransactionOn(TestServiceOne.class).providedByServiceInstance(serviceInstance);
	}

	public static SamInjectionConfiguration createServiceTwoConfiguration(SIID serviceTwoIID, SIID serviceOneIID, SIID serviceZeroIID) {
		return createTransactionOn(TestServiceTwo.class)
				.idBinding(TestServiceZero.class, serviceZeroIID)
				.idBinding(TestServiceOne.class, serviceOneIID)
				.providedByServiceInstance(serviceTwoIID);
	}

	public static SamInjectionConfiguration createServiceTwoConfiguration(SIID serviceTwoIID, SIURL serviceOneURL, SIURL serviceZeroURL) {
		return createTransactionOn(TestServiceTwo.class)
				.urlBinding(TestServiceZero.class, serviceZeroURL)
				.urlBinding(TestServiceOne.class, serviceOneURL)
				.providedByServiceInstance(serviceTwoIID);
	}
}
