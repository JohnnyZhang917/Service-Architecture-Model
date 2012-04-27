package pmsoft.sam.module.definition.test.oauth.service;

/**
 * This service will bind Faji photoSharing service and Beppa
 * PrintingPhotoService and execute the interaction described in OAuth example
 * 
 * In real live, methods mapping to a user GUI can be provided to allow JANE to access the services
 * 
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public interface JaneGuiService {

	public boolean runPrintingPhotoTest();
}
