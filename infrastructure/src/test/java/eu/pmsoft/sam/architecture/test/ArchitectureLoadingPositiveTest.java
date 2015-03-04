/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.sam.architecture.test;

import eu.pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.sam.model.SamArchitecture;
import eu.pmsoft.sam.model.SamModelBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class ArchitectureLoadingPositiveTest {

    @DataProvider(name = "correctArchitecture")
    public Object[][] createCorrectArchitectures() {
        return new Object[][]{{new ArchitectureCorrectOne()}, {new ArchitectureCorrectTwo()}};
    }

    @Test(dataProvider = "correctArchitecture")
    public void loadArchitecturesCorrectly(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamArchitecture architectureObject = SamModelBuilder.loadArchitectureDefinition(definition);
        assertNotNull(architectureObject);
        assertTrue(architectureObject.categories().size() > 0);
    }

    @Test(dataProvider = "correctArchitecture")
    public void loadArchitecturesCategories(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamArchitecture architectureObject = SamModelBuilder.loadArchitectureDefinition(definition);
        assertNotNull(architectureObject);
        String[] expected = {"one", "two", "three", "four"};
        String[] noExpected = {"oneNOT", "twoNOT", "threeNOT", "fourNOT"};
        for (int i = 0; i < noExpected.length; i++) {
            String dontExist = noExpected[i];
//            Option<SamCategoryObject> category = architectureObject.categories().get(dontExist);
//            assertTrue(category.isEmpty());
        }
        for (int i = 0; i < expected.length; i++) {
            String mustExist = expected[i];
//            Option<SamCategoryObject> category = architectureObject.categories().get(mustExist);
//            assertNotNull(category);
//            assertTrue(category.nonEmpty());
//            assertEquals(category.get().id(), mustExist);
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
            withKey(fakeServiceInterface0.class);
        }
    }

    static class SD_12 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface0.class);
        }
    }

    static class SD_13 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface0.class);
        }
    }

    static class SD_14 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface0.class);
            withKey(fakeServiceInterface1.class);
            withKey(fakeServiceInterface2.class);
            withKey(fakeServiceInterface3.class);
        }
    }

    static class SD_21 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface1.class);
        }
    }

    static class SD_31 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface2.class);
        }
    }

    static class SD_41 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface3.class);
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
