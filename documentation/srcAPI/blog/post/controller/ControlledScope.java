package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class ControlledScope implements Scope, BusinessContextControl {

    private final ThreadLocal<BusinessData> data = new ThreadLocal<BusinessData>();
    private final ThreadLocal<Person> person = new ThreadLocal<Person>();
    private final ThreadLocal<Manager> manager = new ThreadLocal<Manager>();
    private final ThreadLocal<Context> context = new ThreadLocal<Context>();

    private static class ThreadLocalWrapperProvider<T> implements Provider<T> {
        private final ThreadLocal<T> reference;

        public T get() {
            return reference.get();
        }

        private ThreadLocalWrapperProvider(ThreadLocal<T> reference) {
            this.reference = reference;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        if (Key.get(BusinessData.class).equals(key)) {
            return (Provider<T>) new ThreadLocalWrapperProvider<BusinessData>(data);
        } else if (Key.get(Person.class).equals(key)) {
            return (Provider<T>) new ThreadLocalWrapperProvider<Person>(person);
        } else if (Key.get(Manager.class).equals(key)) {
            return (Provider<T>) new ThreadLocalWrapperProvider<Manager>(manager);
        } else if (Key.get(Context.class).equals(key)) {
            return (Provider<T>) new ThreadLocalWrapperProvider<Context>(context);
        } else {
            throw new IllegalArgumentException("This controlled scope is only for specific business object types.");
        }
    }

    public void clearBusinessContext() {
        this.data.set(null);
        this.person.set(null);
        this.manager.set(null);
        this.context.set(null);
    }

    public void setupBusinessContext(BusinessData data, Person person, Manager manager, Context context) {
        this.data.set(data);
        this.person.set(person);
        this.manager.set(manager);
        this.context.set(context);
    }

}
