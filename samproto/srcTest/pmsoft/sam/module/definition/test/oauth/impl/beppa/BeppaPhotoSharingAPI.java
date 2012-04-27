package pmsoft.sam.module.definition.test.oauth.impl.beppa;

import java.util.Map;

import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAPI;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAlbum;

import com.google.common.collect.Maps;

public class BeppaPhotoSharingAPI implements PhotoSharingAPI {

	private Map<String, String> registeredUsers = Maps.newHashMap();

	private int userNr = 0;

	public String register(String userRegistrationData) {
		if (registeredUsers.containsKey(userRegistrationData)) {
			return registeredUsers.get(userRegistrationData);
		}
		String newUserCredentials = "user" + userNr;
		registeredUsers.put(userRegistrationData, newUserCredentials);
		userAlbums.put(newUserCredentials, new PhotoSharingAlbumModel(newUserCredentials));
		return newUserCredentials;
	}

	private Map<String, PhotoSharingAlbum> userAlbums = Maps.newHashMap();

	public PhotoSharingAlbum getAccessToAlbum(String credentials) {
		return userAlbums.get(credentials);
	}



}
