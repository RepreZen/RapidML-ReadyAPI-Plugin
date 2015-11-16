package com.modelsolv.reprezen.soapui.exporter

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest
import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.wsdl.WsdlProject
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
	}
}
