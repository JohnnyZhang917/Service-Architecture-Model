package eu.pmsoft.sam.architecture.test;

import eu.pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.architecture.model.SamCategory;
import eu.pmsoft.sam.architecture.model.SamService;
import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Collection;
import java.util.List;

import static org.testng.AssertJUnit.*;

public class ArchitectureLoadingPositiveTest {

    @DataProvider(name = "correctArchitecture")
    public Object[][] createCorrectArchitectures() {
        return new Object[][]{{new ArchitectureCorrectOne()}, {new ArchitectureCorrectTwo()}};
    }

    @Test(dataProvider = "correctArchitecture")
    public void loadArchitecturesCorrectly(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamArchitecture architecture = ArchitectureModelLoader.loadArchitectureModel(definition);
        assertNotNull(architecture);
        assertTrue(architecture.getAllCategories().size() > 0);
        assertTrue(architecture.getAllService().size() > 0);
        assertNotNull(architecture.getArchitectureAnnotations());

        for (SamCategory category : architecture.getAllCategories()) {
            // No isolated categories can exist
            assertTrue(category.getAccessibleCategories().size() > 0 || category.getInverseAccessibleCategories().size() > 0);
            assertTrue(category.getDefinedServices().size() > 0);
        }
        for (SamService service : architecture.getAllService()) {
            assertTrue(service.getServiceContractAPI().size() > 0);
        }
    }

    @Test(dataProvider = "correctArchitecture")
    public void loadArchitecturesCategories(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamArchitecture architecture = ArchitectureModelLoader.loadArchitectureModel(definition);
        assertNotNull(architecture);
        String[] expected = {"one", "two", "three", "four"};
        String[] noExpected = {"oneNOT", "twoNOT", "threeNOT", "fourNOT"};
        for (int i = 0; i < noExpected.length; i++) {
            String dontExist = noExpected[i];
            SamCategory category = architecture.getCategory(dontExist);
            assertNull(category);
        }
        for (int i = 0; i < expected.length; i++) {
            String mustExist = expected[i];
            SamCategory category = architecture.getCategory(mustExist);
            assertNotNull(category);
            assertEquals(category.getCategoryId(), mustExist);
        }

    }

    @Test(dataProvider = "correctArchitecture")
    public void architectureModelHasImmutableSet(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamArchitecture architecture = ArchitectureModelLoader.loadArchitectureModel(definition);
        assertNotNull(architecture);
        List<Collection<?>> expectedImmutable = Lists.newArrayList();
        expectedImmutable.add(architecture.getAllCategories());
        expectedImmutable.add(architecture.getAllService());
        expectedImmutable.add(architecture.getArchitectureAnnotations());
        for (SamCategory category : architecture.getAllCategories()) {
            expectedImmutable.add(category.getAccessibleCategories());
            expectedImmutable.add(category.getInverseAccessibleCategories());
            expectedImmutable.add(category.getDefinedServices());
        }
        for (SamService service : architecture.getAllService()) {
            expectedImmutable.add(service.getServiceContractAPI());
        }
        assertTrue("bad test data, no sets to check", expectedImmutable.size() > 0);

        for (Collection<?> mustBeImmutable : expectedImmutable) {
            try {
                mustBeImmutable.clear();
                assertTrue("set is mutable", false);
            } catch (Exception e) {
                // OK, expected
            }
        }
    }

    static class ArchitectureCorrectOne extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
            SamCategoryLoader one = createCategory("one");
            SamCategoryLoader two = createCategory("two");
            SamCategoryLoader three = createCategory("three");
            SamCategoryLoader four = createCategory("four");

            one.accessToCategory(two);
            two.accessToCategory(three);
            three.accessToCategory(four);
            four.accessToCategory(one);

            one.withService(new SD_11());
            one.withService(new SD_12());

            two.withService(new SD_21());
            three.withService(new SD_31());
            four.withService(new SD_41());
        }
    }

    static class ArchitectureCorrectTwo extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
            SamCategoryLoader one = createCategory("one");
            SamCategoryLoader two = createCategory("two");
            SamCategoryLoader three = createCategory("three");
            SamCategoryLoader four = createCategory("four");

            one.accessToCategory(two);
            two.accessToCategory(three);
            three.accessToCategory(four);
            four.accessToCategory(one);

            one.withService(new SD_11());
            one.withService(new SD_12());
            one.withService(new SD_13());
            one.withService(new SD_14());

            two.withService(new SD_21());
            three.withService(new SD_31());
            four.withService(new SD_41());
        }
    }

    static class SD_11 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface0.class);
        }
    }

    static class SD_12 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface0.class);
        }
    }

    static class SD_13 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface0.class);
        }
    }

    static class SD_14 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface0.class);
            addInterface(fakeServiceInterface1.class);
            addInterface(fakeServiceInterface2.class);
            addInterface(fakeServiceInterface3.class);
        }
    }

    static class SD_21 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface1.class);
        }
    }

    static class SD_31 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface2.class);
        }
    }

    static class SD_41 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(fakeServiceInterface3.class);
        }
    }

    static interface fakeServiceInterface0 {
    }

    ;

    static interface fakeServiceInterface1 {
    }

    ;

    static interface fakeServiceInterface2 {
    }

    ;

    static interface fakeServiceInterface3 {
    }

    ;

}
