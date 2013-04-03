package doc.wjug.graph;

import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

import java.io.*;

public class GraphModuleExternal extends AbstractModule {

    public static void main(String[] args) {
        Injector injectorComplex = Guice.createInjector(new GraphModuleExternal());
        Injector injectorSimple = Guice.createInjector(new GraphModuleSimple());

        graphGood("/tmp/canonical.dot", injectorComplex);
        graphGood("/tmp/simple.dot", injectorSimple);
    }

    @Override
    protected void configure() {
        bind(ServiceInterface.class).to(ServiceImplementation.class);
        bind(ExternalServiceInterface.class).toProvider(new Provider<ExternalServiceInterface>() {
            @Inject
            Provider<ExternalBindingSwitch> delaySwitchProvider;

            public ExternalServiceInterface get() {
                ExternalBindingSwitch intermedium = delaySwitchProvider.get();
                return intermedium.getReferenceObject();
            }
        });
        bind(ExternalBindingSwitch.class).to(ExternalBindingSwitchProxy.class);
    }

    public final static Injector graphGood(String filename, Injector inj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter out = new PrintWriter(baos);

            Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
            GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
            renderer.setOut(out).setRankdir("TB");

            injector.getInstance(InjectorGrapher.class).of(inj).graph();

            out = new PrintWriter(new File(filename), "UTF-8");
            String s = baos.toString("UTF-8");
            s = fixGrapherBug(s);
            s = hideClassPaths(s);
            out.write(s);
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inj;
    }

    public static String hideClassPaths(String s) {
        s = s.replaceAll("\\w[a-z\\d_\\.]+\\.([A-Z][A-Za-z\\d_]*)", "");
        s = s.replaceAll("value=[\\w-]+", "random");
        return s;
    }

    public static String fixGrapherBug(String s) {
        s = s.replaceAll("style=invis", "style=solid");
        return s;
    }


}
