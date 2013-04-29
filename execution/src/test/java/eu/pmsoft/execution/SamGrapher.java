package eu.pmsoft.execution;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;
import eu.pmsoft.sam.see.SEEServer;
import eu.pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;

import java.io.*;

public class SamGrapher {

    public static void main(String[] args) {
        int anyPort = 1234;
//        Injector samServerInjector = Guice.createInjector(SEEServer.createServerModule(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(anyPort)));

//        graphGood("/tmp/sam.dot", samServerInjector);
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
