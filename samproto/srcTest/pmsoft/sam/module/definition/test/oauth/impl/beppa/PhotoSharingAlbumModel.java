package pmsoft.sam.module.definition.test.oauth.impl.beppa;

import static com.google.common.base.Preconditions.checkPositionIndex;

import java.util.List;

import pmsoft.sam.module.definition.test.oauth.service.PhotoInfo;
import pmsoft.sam.module.definition.test.oauth.service.PhotoResource;
import pmsoft.sam.module.definition.test.oauth.service.PhotoResourceExtended;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAlbum;

import com.google.common.collect.Lists;

public class PhotoSharingAlbumModel implements PhotoSharingAlbum {

	List<PhotoInfoData> photos = Lists.newArrayList();
	private final String userId;

	public PhotoSharingAlbumModel(String user) {
		this.userId = user;
	}

	public void uploadPhoto(String photoInformation, byte[] photoData) {
		int photoId = photos.size();
		PhotoInfoData newphoto = new PhotoInfoData(photoId, photoInformation, photoData);
		photos.add(newphoto);
	}

	public PhotoInfo getPhotoInfo(int photoId) {
		checkPositionIndex(photoId, photos.size());
		return photos.get(photoId).getInfo();
	}
	
	public PhotoInfo[] getPhotoList() {
		PhotoInfo[] info = new PhotoInfo[photos.size()];
		for (int i = 0; i < photos.size(); i++) {
			info[i] = photos.get(i).getInfo();
		}
		return info;
	}

	public PhotoResource getPhotoSharingResource(int photoId) {
		checkPositionIndex(photoId, photos.size());
		return photos.get(photoId).getPhotoSharingResource();
	}

	private class PhotoInfoData implements PhotoResourceExtended{

		private final int photoId;
		private String photoInformation;
		private byte[] data;

		public PhotoInfoData(int photoId, String photoInformation, byte[] photoData) {
			this.photoId = photoId;
			this.photoInformation = photoInformation;
			this.data = photoData;
		}
		
		public boolean checkNestedInterserviceInteraction() {
			return true;
		}

		public PhotoInfo getInfo() {
			PhotoInfo info = new PhotoInfo();
			info.description = photoInformation;
			info.photoId = photoId;
			return info;
		}

		public PhotoResource getPhotoSharingResource() {
			return new PhotoResource() {

				public String getPhotoID() {
					return userId + "_" + photoId;
				}

				public byte[] getPhotoData() {
					return data;
				}
				
				public PhotoResourceExtended getExtendedApi() {
					return PhotoInfoData.this;
				}

			};
		}

	}
}