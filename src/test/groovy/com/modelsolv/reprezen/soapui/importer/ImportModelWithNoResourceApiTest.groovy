package com.modelsolv.reprezen.soapui.importer

import com.eviware.soapui.impl.rest.RestService
import com.modelsolv.reprezen.soapui.ZenModelUtils


class ImportModelWithNoResourceApiTest extends ImporterTestBase {

	public void testUndefinedResourceHasValidationWarning() {
		def zenModel = ZenModelUtils.loadModel(getTestModelUri("UndefinedResourceAPI.rapid"))
		assert "The selected RAPID-ML model does not contain any interface definitions!" in ZenModelUtils.getWarnings(zenModel)
	}

	public void testUndefinedResourceAPIImportedNormally() {
		def result = importRepreZen("UndefinedResourceAPI.rapid")
		assert result.isEmpty()
	}

	public void testEmptyResourceAPIImportedNormally() {
		RestService restService = importRepreZenAndGenFirstService("EmptyResourceAPI.rapid")
		assert restService != null
	}
}
