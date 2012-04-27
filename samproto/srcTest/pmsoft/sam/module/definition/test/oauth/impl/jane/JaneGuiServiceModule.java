package pmsoft.sam.module.definition.test.oauth.impl.jane;

import pmsoft.sam.module.definition.test.oauth.service.JaneGuiService;
import pmsoft.sam.module.definition.test.oauth.service.PhotoInfo;
import pmsoft.sam.module.definition.test.oauth.service.PhotoResource;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAPI;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAlbum;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoAPI;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoOrder;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class JaneGuiServiceModule extends AbstractModule {

	private static class JaneGuiInteraction implements JaneGuiService {

		@Inject
		private PhotoSharingAPI sharingApi;

		@Inject
		private PrintingPhotoAPI printingApi;

		public boolean runPrintingPhotoTest() {
			System.out.println("JANE: I am back from her Scotland vacation, lets share photos");
			System.out.println("JANE: registering in PhotoSharingService");
			String credentials = sharingApi.register("JANE");
			PhotoSharingAlbum myVacationAlbum = sharingApi.getAccessToAlbum(credentials);

			System.out.println("JANE: upload photos");
			myVacationAlbum.uploadPhoto("landscape photo", new String("LANDSCAPE PHOTO").getBytes());
			myVacationAlbum.uploadPhoto("water photo", new String("WATER PHOTO").getBytes());

			System.out.println("JANE: Lets send the photos to grandmother");
			System.out.println("JANE: create print photo order");
			PrintingPhotoOrder order = printingApi.createPrintingOrder();
			order.addAdressInformation("grandmother direction");

			PhotoInfo[] vacationPhotos = myVacationAlbum.getPhotoList();
			PhotoResource[] photos = new PhotoResource[vacationPhotos.length];
			for (int i = 0; i < vacationPhotos.length; i++) {
				// this reference is a proxy with references to Canonical
				// protocol recording context
				System.out.println("JANE: send photo resource references");
				photos[i] = myVacationAlbum.getPhotoSharingResource(vacationPhotos[i].photoId);
			}

			// The prototype implementation calls methods in strict order, so in
			// this example first record all calls to one service and then call
			// the other.
			// Strong assumptions about service implementations are needed to change
			// this.

			for (int i = 0; i < photos.length; i++) {
				// the order instance will record the reference number and
				// object type (KEY<T), and all calls in the real implementation
				// of the printing service will be passed back to the canonical
				// protocol in the form of
				// #ref_nr.getPhotoID()#return_ref_nr;
				// #ref_nr.getPhotoData()#return_ref_nr;
				order.addPhotoResourceReference(photos[i]);
			}

			System.out.println("JANE: now submit the printing order");
			return order.submitOrder();

		}
	}

	@Override
	protected void configure() {
		bind(JaneGuiService.class).to(JaneGuiInteraction.class).asEagerSingleton();
	}

}
