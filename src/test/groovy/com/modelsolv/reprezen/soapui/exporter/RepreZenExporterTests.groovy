package com.modelsolv.reprezen.soapui.exporter

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.validation.Issue;

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest
import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup;
import com.modelsolv.reprezen.restapi.xtext.loaders.HeadlessZenModelLoader;
import com.modelsolv.reprezen.soapui.RepreZenExporter;

class RepreZenExporterTests extends GroovyTestCase {

	public void testExport() {
		WsdlProject project = new WsdlProject()
		RestService restService = project.addNewInterface( "Test API", RestServiceFactory.REST_TYPE)
		restService.setBasePath( "/api/{version}" )

		RestResource resource = restService.addNewResource( "Cars", "/cars")
		resource.addProperty( "version").style = RestParamsPropertyHolder.ParameterStyle.TEMPLATE

		resource = resource.addNewChildResource( "Car", "{make}" )
		resource.addProperty( "make").style = RestParamsPropertyHolder.ParameterStyle.TEMPLATE

		RestMethod method = resource.addNewMethod( "Get Car")
		method.setMethod( RestRequestInterface.HttpMethod.GET )
		RestRepresentation representation = method.addNewRepresentation( RestRepresentation.Type.RESPONSE )
		representation.mediaType = "application/json"
		representation.status = [200]

		method = resource.addNewMethod( "Create Car")
		method.setMethod( RestRequestInterface.HttpMethod.POST )
		representation = method.addNewRepresentation( RestRepresentation.Type.REQUEST )
		representation.mediaType = "application/json"
		representation = method.addNewRepresentation( RestRepresentation.Type.RESPONSE )
		representation.mediaType = "application/json"
		representation.status = [200]

		RestRequest request = method.addNewRequest( "Test");
		request.mediaType = "application/json"
		request.requestContent = "{ \"test\" : \"value\" }"

		RepreZenExporter exporter = new RepreZenExporter( project )
		String modelText = exporter.createRepreZenAsText( restService.name, restService, restService.getBasePath())

		Console.println( modelText )

		assertNotNull modelText
		validateModel(modelText)
	}

	public static void validateModel(String modelText) {
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
