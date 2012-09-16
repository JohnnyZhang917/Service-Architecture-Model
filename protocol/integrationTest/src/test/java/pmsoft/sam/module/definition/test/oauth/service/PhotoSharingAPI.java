package pmsoft.sam.module.definition.test.oauth.service;

public interface PhotoSharingAPI {

	public String register(String userRegistrationData);
	
	public PhotoSharingAlbum getAccessToAlbum(String credentials);
	
}
