package com.modelsolv.reprezen.soapui

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParamProperty
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.wsdl.WsdlProject


class RepreZenImporterTest extends GroovyTestCase {

	public void testTaxBlaster() {
		RestService restService = importRepreZen("TaxBlaster.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assertFalse("Expecting at least one resource to be generated", resources.isEmpty())
	}

	public void testInlineExamples() {
		RestService restService = importRepreZen("TaxBlasterWithExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assertFalse("Expecting at least one resource to be generated", resources.isEmpty())
	}

	public void testExternalExamples() {
		RestService restService = importRepreZen("externalExamples/TaxBlasterWithExternalExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assertFalse("Expecting at least one resource to be generated", resources.isEmpty())
	}

	public void testDataModelImport() {
		RestService restService = importRepreZen("dataModelImport/TaxBlaster.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assertFalse("Expecting at least one resource to be generated", resources.isEmpty())
	}

	public static def RestService importRepreZen( def path ) {
		WsdlProject project = new WsdlProject()
		RepreZenImporter importer = new RepreZenImporter( project )
		String uri = new File( "src/test/resources/" + path ).toURI().toURL().toString();
		return importer.importZenModel(uri).get(0);
	}
}
