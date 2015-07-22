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

	public void testTaxBlasterWithExamples() {
		RestService restService = importRepreZen("TaxBlasterWithExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assertFalse("Expecting at least one resource to be generated", resources.isEmpty())
	}

	public static def RestService importRepreZen( def path ) {
		WsdlProject project = new WsdlProject()
		RepreZenImporter importer = new RepreZenImporter( project )
		File file = new File( "src/test/resources/" + path );
		return importer.importZenModel(file).get(0);
	}
}
