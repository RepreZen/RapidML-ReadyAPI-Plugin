package com.modelsolv.reprezen.soapui.importer

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class ImportedDataModelTest extends GroovyTestCase {

	public void testDataModelImport() {
		RestService restService = RepreZenImporterTest.importRepreZen("dataModelImport/TaxBlaster.rapid")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 4
	}

	public void testMessageSchema() {
		// TODO How can we specify message schema (XSD or message schema in Ready! API)
	}
}
