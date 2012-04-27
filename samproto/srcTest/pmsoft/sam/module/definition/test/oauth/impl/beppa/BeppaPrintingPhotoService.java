package pmsoft.sam.module.definition.test.oauth.impl.beppa;

import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAPI;

import com.google.inject.AbstractModule;

public class BeppaPrintingPhotoService extends AbstractModule {

	@Override
	protected void configure() {
		bind(PhotoSharingAPI.class).to(BeppaPhotoSharingAPI.class).asEagerSingleton();
	}

}
