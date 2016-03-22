package com.modelsolv.reprezen.soapui.importer

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class ImportModelWithNoResourceApiTest extends GroovyTestCase {

	public void testUndefinedResourceAPI() {
		def result = RepreZenImporterTest.importRepreZen("UndefinedResourceAPI.rapid")
		assert result.isEmpty()
	}

	public void testEmptyResourceAPI() {
		RestService restService = RepreZenImporterTest.importRepreZenAndGenFirstService("EmptyResourceAPI.rapid")
		assert restService != null
	}
}
