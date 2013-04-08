package eu.pmsoft.testing;

import com.google.inject.Inject;

public class LibraryTwoInternal implements LibraryTwoApi {

    private final CommonLogApi log;

    @Inject
    public LibraryTwoInternal(CommonLogApi log) {
        this.log = log;
    }

    @Override
    public void twoApiCall() {
        log.log("library two method call");
    }
}
