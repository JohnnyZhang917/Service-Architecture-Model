package pmsoft.sam.module.definition.test.oauth.service;

public interface PhotoResource {

	public String getPhotoID();
	public byte[] getPhotoData();
	
	// added to test ServerBindingInstanceReferences
	public PhotoResourceExtended getExtendedApi();
}
