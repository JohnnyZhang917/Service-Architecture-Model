package pmsoft.sam.architecture.test;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class ArchitectureLoadingNegativeTest {

	@DataProvider(name = "incorrectArchitecture")
	public Object[][] createCorrectArchitectures() {
		return new Object[][] { { new ArchitectureInCorrectEmpty() }, { new ArchitectureInCorrectDuplicateID() },
				{ new ArchitectureInCorrectSelfAccessCategory() }, { new ArchitectureInCorrectEmptyCategory() } };
	}

	@Test(dataProvider = "incorrectArchitecture", expectedExceptions = {IncorrectArchitectureDefinition.class, IllegalStateException.class})
	public void loadArchitecturesCorrectly(SamArchitectureDefinition definition) throws IncorrectArchitectureDefinition {
		ArchitectureModelLoader.loadArchitectureModel(definition);
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
	};

	static interface fakeServiceInterface1 {
	};

	static interface fakeServiceInterface2 {
	};

	static interface fakeServiceInterface3 {
	};

}
