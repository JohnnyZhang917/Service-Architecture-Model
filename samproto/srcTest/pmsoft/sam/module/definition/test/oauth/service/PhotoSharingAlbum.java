package pmsoft.sam.module.definition.test.oauth.service;


public interface PhotoSharingAlbum {

	public void uploadPhoto(String photoInformation, byte[] photoData);
	
	public PhotoInfo[] getPhotoList();
	
	public PhotoInfo getPhotoInfo(int photoId);
	
	public PhotoResource getPhotoSharingResource(int photoId);

	
}
