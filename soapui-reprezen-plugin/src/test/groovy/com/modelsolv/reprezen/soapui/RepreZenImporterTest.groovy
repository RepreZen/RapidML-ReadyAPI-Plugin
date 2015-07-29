package com.modelsolv.reprezen.soapui

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class RepreZenImporterTest extends GroovyTestCase {
	RestService restService;

	protected void setUp() {
		restService = importRepreZen("TaxBlaster.zen")
	}
	protected void tearDown() {
		restService = null
	}

	public void testResources() {
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 5
		assert resources.values().collect{it.name} as Set == [
			"IndexObject",
			"TaxFilingObject",
			"TaxFilingCollection",
			"PersonObject",
			"PersonCollection"] as Set
		assert resources.keySet() as Set == [
			"/index",
			"/people",
			"/people/{id}",
			"/taxFilings",
			"/taxFilings/{id}"] as Set
		RestResource objectResource = resources.get("/people/{id}")
		assert objectResource.description == "An individual user by ID. "

		RestParameter idParam = objectResource.params.get("id")
		assert idParam.description == "taxpayerID of the requested Person "
		assert idParam.type.getLocalPart() == "string"
	}

	public void testObjectResource() {
		def Map<String, RestResource> resources = restService.getResources()
		RestResource objectResource = resources.get("/people/{id}")
		assert objectResource.description == "An individual user by ID. "

		RestParameter idParam = objectResource.params.get("id")
		assert idParam.description == "taxpayerID of the requested Person "
		assert idParam.type.getLocalPart() == "string"

		assert objectResource.methods.collect{it.name} == [
			"getPersonObject",
			"putPersonObject"
		]
	}

	public void testGetMethod() {
		def Map<String, RestResource> resources = restService.getResources()
		RestResource objectResource = resources.get("/people/{id}")

		RestMethod getMethod = objectResource.methods.find {it.name == "getPersonObject"}
		assert getMethod.method.name() == "GET"
		RestRepresentation response = getMethod.getRepresentations(RestRepresentation.Type.RESPONSE, "application/xml")[0]
		assert response != null
		assert response.getStatus() == [200]
	}

	public void testPutMethod() {
		def Map<String, RestResource> resources = restService.getResources()
		RestResource objectResource = resources.get("/people/{id}")
		RestMethod putMethod = objectResource.methods.find {it.name == "putPersonObject"}
		def RestRequest request = objectResource.getRequestAt(0)
		assert request.mediaType == "application/xml"
		assert putMethod.method.name() == "PUT"
		RestRepresentation response200 = putMethod.getRepresentations(RestRepresentation.Type.RESPONSE, "application/xml")[0]
		assert response200 != null
		assert response200.getStatus() == [200]
		RestRepresentation response400 = putMethod.getRepresentations()[2]
		assert response400 != null
		assert response400.type == RestRepresentation.Type.FAULT
		assert response400.getStatus() == [400]
	}

	public static def RestService importRepreZen( def path ) {
		WsdlProject project = new WsdlProject()
		RepreZenImporter importer = new RepreZenImporter( project )
		String uri = new File( "src/test/resources/" + path ).toURI().toURL().toString();
		return importer.importZenModel(uri).get(0);
	}
}
