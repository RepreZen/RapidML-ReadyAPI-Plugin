package com.modelsolv.reprezen.soapui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.RestApiXtextPlugin;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup;

/**
 * 
 * @author Tatiana Fesenko
 *
 */
public abstract class LibraryRegistry<T extends EObject> {
	private final Map<String, T> elementsByName = Maps.newHashMap();
	private final Class<T> type;
	private final URI libraryURI;

	public LibraryRegistry(Class<T> type, URI libraryURI) {
		this.type = type;
		this.libraryURI = libraryURI;

	}

	public T getElement(String name) {
		if (elementsByName.isEmpty()) {
			for (T element : EcoreUtil2.getAllContentsOfType(getLibraryModel(libraryURI), type)) {
				elementsByName.put(getElementId().apply(element), element);
			}
		}
		return elementsByName.get(name);
	}

	protected ZenModel getLibraryModel(org.eclipse.emf.common.util.URI modelURI) {
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = new XtextResourceSet();
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		org.eclipse.emf.ecore.resource.Resource resource = resourceSet.createResource(modelURI);
		try {
			InputStream stream = RestApiXtextPlugin.class.getResourceAsStream("/libraries/" + modelURI.lastSegment()); //$NON-NLS-1$
			resource.load(stream, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return (ZenModel) resource.getContents().get(0);
	}

	abstract protected Function<T, String> getElementId();

}
