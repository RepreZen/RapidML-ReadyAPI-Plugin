package com.modelsolv.reprezen.soapui.exporter

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.URIConverter
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.validation.Issue

import com.eviware.soapui.impl.rest.RestService
import com.modelsolv.reprezen.restapi.ZenModel
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup
import com.modelsolv.reprezen.soapui.importer.ImporterTestBase

abstract class ExporterTestBase extends ImporterTestBase {

	protected void assertModelIsValid(String modelText) {
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResource resource = new XtextResourceSet().createResource(URI.createURI("resource.rapid"));
		resource.load(new URIConverter.ReadableInputStream(modelText, "UTF-8"), null);
		for (Resource.Diagnostic error: resource.validateConcreteSyntax()) {
			fail(error.getMessage());
		}
		for (Resource.Diagnostic error: resource.getParseResult().getSyntaxErrors()) {
			fail(error.getMessage());
		}
		IResourceValidator validator = resource.getResourceServiceProvider()
				.getResourceValidator();
		ZenModel zenModel = resource.getContents().get(0);
		for (Issue issue: validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl)) {
			fail(issue.getMessage());
		}
		// generateImplicitValues() throws an exception if a resource does not have a corresponding data type
		zenModel.generateImplicitValues()
	}
}
