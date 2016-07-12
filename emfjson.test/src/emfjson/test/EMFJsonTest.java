/**
 * Copyright (c) 2011-2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Dirix - initial API and implementation
 */
package emfjson.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecp.makeithappen.model.task.TaskPackage;
import org.emfjson.EMFJs;
import org.emfjson.jackson.resource.JsonResource;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests EMF -> JSON -> EMF Round Trip.
 *
 * @author Stefan Dirix (sdirix@eclipsesource.com)
 *
 */
public class EMFJsonTest {

	private ResourceSet jsonResourceSet;
	private HashMap<String, Object> saveOptions;

	/**
	 * Initialization needed for emfjson.
	 *
	 * @throws Exception if something went wrong.
	 */
	@Before
	public void setUp() throws Exception {
		jsonResourceSet = new ResourceSetImpl();
		jsonResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new JsonResourceFactory() { //$NON-NLS-1$
			@Override
			public Resource createResource(URI uri) {
				return new JsonResource(uri) {
					@Override
					protected boolean useUUIDs() {
						return true;
					}
				};
			}
		});
		saveOptions = new HashMap<String, Object>();
		saveOptions.put(EMFJs.OPTION_USE_ID, true);
	}

	/**
	 * Removes all generated files.
	 *
	 * @throws Exception if something went wrong.
	 */
	@After
	public void tearDown() throws Exception {
		// XXX: Comment the following code to keep all created files
		for (final Resource resource : new ArrayList<Resource>(jsonResourceSet.getResources())) {
			resource.delete(null);
		}
	}

	/**
	 * Create the Ecore Ecore JSON resource.
	 *
	 * @throws IOException if something went wrong.
	 */
	@Ignore
	@Test
	public void createEcoreJson() throws IOException {
		final Resource resource = jsonResourceSet.createResource(URI.createURI("testoutput/ecoreJSON.json")); //$NON-NLS-1$
		resource.getContents().add(EcoreUtil.copy(EcorePackage.eINSTANCE));
		resource.save(saveOptions);
	}

	/**
	 * Create the Task Ecore JSON resource.
	 *
	 * @throws IOException if something went wrong.
	 */
	@Ignore
	@Test
	public void createTaskJson() throws IOException {
		final Resource resource = jsonResourceSet.createResource(URI.createURI("testoutput/taskJSON.json")); //$NON-NLS-1$
		final EPackage copy = EcoreUtil.copy(TaskPackage.eINSTANCE);
		resource.getContents().add(copy);
		resource.save(saveOptions);
	}

	/**
	 * Create and reload the Task Ecore JSON resource.
	 *
	 * @throws IOException if something went wrong.
	 */
	@Ignore
	@Test
	public void reloadTaskJson() throws IOException {
		final Resource resource = jsonResourceSet.createResource(URI.createURI("testoutput/taskJSON.json")); //$NON-NLS-1$
		resource.getContents().add(EcoreUtil.copy(TaskPackage.eINSTANCE));
		resource.save(saveOptions);
		resource.unload();
		resource.load(null);
	}

	/**
	 * Tests the EMF -> JSON -> EMF round trip.
	 *
	 * Saves the Task Ecore as JSON file. Then parses the JSON file and saves it as a new Task Ecore.
	 *
	 * @throws IOException if something went wrong.
	 */
	@Ignore
	@Test
	public void convertToJsonToEcore() throws IOException {
		// Create Task Json
		final String taskJsonURI = "testoutput/taskJSON.json"; //$NON-NLS-1$

		final Resource resource = jsonResourceSet.createResource(URI.createURI(taskJsonURI));
		resource.getContents().add(EcoreUtil.copy(TaskPackage.eINSTANCE));
		resource.save(saveOptions);
		resource.unload();

		// Reload Task Json
		resource.load(null);

		// Create new Ecore Resource
		final Resource newEcoreResource = jsonResourceSet.createResource(URI.createURI("testoutput/taskEcore.ecore")); //$NON-NLS-1$
		newEcoreResource.getContents().add(resource.getContents().get(0));
		newEcoreResource.save(null);

		// TODO:
		// Compare with original ecore file.
	}

	/**
	 * Tests the EMF -> JSON (+ modifications) -> EMF usage round trip.
	 *
	 * First the Task Ecore JSON resource is created. After that a new attribute is added. The modified JSON resource is
	 * then loaded and used via EMF reflection.
	 *
	 * @throws IOException if something went wrong.
	 */
	@Test
	public void convertToAndModifyAndUseJSON() throws IOException {
		// Create Task Json
		final String taskJsonURI = "testoutput/taskJSON.json"; //$NON-NLS-1$
		final String modifiedTaskJsonURI = "testoutput/taskJSONModified.json"; //$NON-NLS-1$
		final String newStringAttribute = "myNewlyAddedName"; //$NON-NLS-1$

		final Resource resource = jsonResourceSet.createResource(URI.createURI(taskJsonURI));
		resource.getContents().add(EcoreUtil.copy(TaskPackage.eINSTANCE));
		resource.save(saveOptions);
		resource.unload();

		// Create Modified Task Json
		final JsonParser parser = new JsonParser();
		final JsonElement taskPackageElement = parser.parse(new FileReader(taskJsonURI));
		final JsonObject taskPackageObject = taskPackageElement.getAsJsonObject();

		final JsonArray classifiers = taskPackageObject.getAsJsonArray("eClassifiers"); //$NON-NLS-1$

		final Iterator<JsonElement> classifierIterator = classifiers.iterator();
		while (classifierIterator.hasNext()) {
			final JsonObject classifierObject = classifierIterator.next().getAsJsonObject();
			if (classifierObject.get("name").getAsString().equals("Task")) { //$NON-NLS-1$ //$NON-NLS-2$
				final JsonArray structuralFeatures = classifierObject.getAsJsonArray("eStructuralFeatures"); //$NON-NLS-1$

				final JsonObject newFeatureObject = new JsonObject();
				newFeatureObject.addProperty("eClass", "http://www.eclipse.org/emf/2002/Ecore#//EAttribute"); //$NON-NLS-1$ //$NON-NLS-2$
				newFeatureObject.addProperty("name", newStringAttribute); //$NON-NLS-1$

				final JsonObject refObject = new JsonObject();
				refObject.addProperty("$ref", "http://www.eclipse.org/emf/2002/Ecore#//EString"); //$NON-NLS-1$ //$NON-NLS-2$

				newFeatureObject.add("eType", refObject); //$NON-NLS-1$

				structuralFeatures.add(newFeatureObject);
			}
		}
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final FileWriter fileWriter = new FileWriter(modifiedTaskJsonURI);
		gson.toJson(taskPackageObject, fileWriter);
		fileWriter.close();

		// Read in and Use modified Task JSON
		final Resource modifiedTaskURI = jsonResourceSet.getResource(URI.createURI(modifiedTaskJsonURI), true);
		final EPackage modifiedTaskPackage = (EPackage) modifiedTaskURI.getContents().get(0);

		final EClass modifiedTaskClass = (EClass) modifiedTaskPackage.getEClassifier("Task"); //$NON-NLS-1$
		final EObject modifiedTaskClassInstance = EcoreUtil.create(modifiedTaskClass);

		for (final EAttribute attribute : modifiedTaskClassInstance.eClass().getEAllAttributes()) {
			if (attribute.getName().equals(newStringAttribute)) {
				modifiedTaskClassInstance.eSet(attribute, "New Name!"); //$NON-NLS-1$
				assertEquals(modifiedTaskClassInstance.eGet(attribute), "New Name!"); //$NON-NLS-1$
				return;
			}
		}

		fail("The attribute " + newStringAttribute + " should have been set and checked"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
