package com.modelsolv.reprezen.soapui.importer

import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.soapui.RepreZenImporter
import com.modelsolv.reprezen.soapui.ZenModelUtils

abstract class ImporterTestBase extends GroovyTestCase {
	protected  RestService importRepreZenAndGenFirstService( def path ) {
		return importRepreZen(path).get(0);
	}

	protected List<RestService> importRepreZen( def path ) {
		WsdlProject project = new WsdlProject()
		RepreZenImporter importer = new RepreZenImporter( project )
		String uri = getTestModelUri(path);
		def zenModel = ZenModelUtils.loadModel(uri)
		return importer.importZenModel(zenModel);
	}

	protected String getTestModelUri(String path) {
		new File( "src/test/resources/" + path ).toURI().toURL().toString();
	}
}
