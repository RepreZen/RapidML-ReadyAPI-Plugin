package com.modelsolv.reprezen.soapui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup;

public class HeadlessZenModelLoader {

	public static ZenModel loadModel(String fileUrl) throws MalformedURLException, URISyntaxException {
		return loadModel(URI.createURI(new URL(fileUrl).toString()));
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
		XtextResourceSet resourceSet = new XtextResourceSet();
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		org.eclipse.emf.ecore.resource.Resource resource = resourceSet.getResource(modelUri, true);
		return (XtextResource) resource;
	}
}
