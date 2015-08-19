package com.modelsolv.reprezen.soapui;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup;
import com.modelsolv.reprezen.restapi.xtext.loaders.RepreZenXtextResourceSet;

/**
 * 
 * @author Tatiana Fesenko
 *
 */
public class HeadlessZenModelLoader {

	public static ZenModel loadModel(String fileUrl) {
		return loadModel(URI.createURI(fileUrl));
	}

	public static ZenModel loadModel(File file) {
		return loadModel(URI.createFileURI(file.getAbsolutePath()));
	}

	public static ZenModel loadModel(URI modelUri) {
		XtextResource xtextResource = loadXtextResource(modelUri);
		ZenModel model = (ZenModel) xtextResource.getContents().get(0);
		return model;
	}

	public static ZenModel loadAndValidateModel(URI modelUri) {
		XtextResource xtextResource = loadXtextResource(modelUri);
		ZenModel model = (ZenModel) xtextResource.getContents().get(0);
		if (!xtextResource.getErrors().isEmpty()) {
			for (Resource.Diagnostic error : xtextResource.getErrors()) {
				throw new RuntimeException("Selected RepreZen model contains errors: " + error.getMessage());
			}
		}
		return model;
	}

	public static XtextResource loadXtextResource(URI modelUri) {
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = new RepreZenXtextResourceSet();
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		org.eclipse.emf.ecore.resource.Resource resource = resourceSet.getResource(modelUri, true);
		return (XtextResource) resource;
	}
}
