package pmsoft.injectionUtils.logger;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class Slf4jTypeListener implements TypeListener {

	public <I> void hear(TypeLiteral<I> aTypeLiteral, TypeEncounter<I> aTypeEncounter) {
		Class<? super I> mainType = aTypeLiteral.getRawType();
		while( mainType != null) {
			for (Field field : mainType.getDeclaredFields()) {
				if (field.getType() == Logger.class && field.isAnnotationPresent(InjectLogger.class)) {
					aTypeEncounter.register(new Slf4jMembersInjector<I>(field));
				}
			}
			mainType = mainType.getSuperclass();
		}
	}
}
