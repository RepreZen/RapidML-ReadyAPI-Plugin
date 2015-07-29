package com.modelsolv.reprezen.soapui

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class ExamplesTest extends GroovyTestCase {

	public void testInlineExamples() {
		RestService restService = RepreZenImporterTest.importRepreZen("TaxBlasterWithExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 5
	}

	public void testExternalExamples() {
		RestService restService = RepreZenImporterTest.importRepreZen("externalExamples/TaxBlasterWithExternalExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 2
	}
}
