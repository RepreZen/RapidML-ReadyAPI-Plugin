package com.modelsolv.reprezen.soapui

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class RepreZenImporterTest extends GroovyTestCase {

	public void testTaxBlaster() {
		RestService restService = importRepreZen("TaxBlaster.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 5
		assert resources.values().collect{it.name} as Set == ["IndexObject", "TaxFilingObject", "TaxFilingCollection", "PersonObject", "PersonCollection"] as Set
		assert resources.keySet() as Set == ["/index", "/people", "/people/{id}", "/taxFilings", "/taxFilings/{id}"] as Set
		RestResource objectResource = resources.get("/people/{id}")
		assert objectResource.description == "An individual user by ID. "

		RestParameter idParam = objectResource.params.get("id")
		assert idParam.description == "taxpayerID of the requested Person "
		assert idParam.type.getLocalPart() == "string"
		assert objectResource.methods.collect{it.name} == ["getPersonObject", "putPersonObject"]
		
//		/** An individual user by ID. */
//		objectResource PersonObject type Person
//			URI people/{id}
//				/** taxpayerID of the requested Person */
//				required templateParam id property taxpayerID
//
//			mediaTypes
//				application/xml
//			method GET getPersonObject
//				request
//				response PersonObject statusCode 200
//
//			method PUT putPersonObject
//				request PersonObject
//				response statusCode 200
//				response statusCode 400
		
		RestMethod getMethod = objectResource.methods.find {it.name == "getPersonObject"}
		assert getMethod.method.name() == "GET"
		
		RestMethod putMethod = objectResource.methods.find {it.name == "putPersonObject"}
	}

	public void testInlineExamples() {
		RestService restService = importRepreZen("TaxBlasterWithExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 5
	}

	public void testExternalExamples() {
		RestService restService = importRepreZen("externalExamples/TaxBlasterWithExternalExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 2
	}

	public void testDataModelImport() {
		RestService restService = importRepreZen("dataModelImport/TaxBlaster.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 4
	}

	public static def RestService importRepreZen( def path ) {
		WsdlProject project = new WsdlProject()
		RepreZenImporter importer = new RepreZenImporter( project )
		String uri = new File( "src/test/resources/" + path ).toURI().toURL().toString();
		return importer.importZenModel(uri).get(0);
	}
}
