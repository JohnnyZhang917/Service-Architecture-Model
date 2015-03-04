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
import eu.pmsoft.sam.model.SamModelBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ArchitectureLoadingNegativeTest {

    @DataProvider(name = "incorrectArchitecture")
    public Object[][] createCorrectArchitectures() {
        return new Object[][]{{new ArchitectureInCorrectEmpty()}, {new ArchitectureInCorrectDuplicateID()},
                {new ArchitectureInCorrectSelfAccessCategory()}, {new ArchitectureInCorrectEmptyCategory()}};
    }

    @Test(dataProvider = "incorrectArchitecture", expectedExceptions = {IncorrectArchitectureDefinition.class})
    public void loadArchitecturesCorrectly(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
        SamModelBuilder.loadArchitectureDefinition(definition);
    }

    // Empty architecture
    static class ArchitectureInCorrectEmpty extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
        }
    }

    // Duplicate category Id
    static class ArchitectureInCorrectDuplicateID extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
            createCategory("one");
            createCategory("one");
        }
    }

    // Empty categories
    static class ArchitectureInCorrectEmptyCategory extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
            createCategory("one");
        }
    }

    // self access category
    static class ArchitectureInCorrectSelfAccessCategory extends AbstractSamArchitectureDefinition {

        @Override
        protected void loadArchitectureDefinition() {
            SamCategoryLoader one = createCategory("one");
            one.accessToCategory(one);

            one.withService(new SD_11());
            one.withService(new SD_12());
            one.withService(new SD_13());
            one.withService(new SD_14());

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

    public static class SD_14 extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(fakeServiceInterface0.class);
            withKey(fakeServiceInterface1.class);
            withKey(fakeServiceInterface2.class);
            withKey(fakeServiceInterface3.class);
        }
    }

    static interface fakeServiceInterface0 {
    }

    static interface fakeServiceInterface1 {
    }

    static interface fakeServiceInterface2 {
    }

    static interface fakeServiceInterface3 {
    }

}
