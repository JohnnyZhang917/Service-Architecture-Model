package eu.pmsoft.injectionUtils.logger;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class LoggerInjectorModule extends AbstractModule {

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new Slf4jTypeListener());
    }

}
