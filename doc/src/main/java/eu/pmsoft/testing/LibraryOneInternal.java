package eu.pmsoft.testing;

import com.google.inject.Inject;

public class LibraryOneInternal implements LibraryOneApi {

    private final CommonLogApi log;

    @Inject
    public LibraryOneInternal(CommonLogApi log) {
        this.log = log;
    }

    @Override
    public void oneApiCall() {
        log.log("library one method call");
    }
}
