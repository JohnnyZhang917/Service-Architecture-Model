package eu.pmsoft.testing;

import com.google.inject.Inject;

public class ClientApiImplementation implements ClientApi {

    private final LibraryOneApi libraryOne;
    private final LibraryTwoApi libraryTwo;

    @Inject
    public ClientApiImplementation(LibraryOneApi libraryOne, LibraryTwoApi libraryTwo) {
        this.libraryOne = libraryOne;
        this.libraryTwo = libraryTwo;
    }

    @Override
    public void testLogging() {
        System.out.println("Start test On client");
        libraryOne.oneApiCall();
        libraryTwo.twoApiCall();
        System.out.println("End test On client");
    }
}
