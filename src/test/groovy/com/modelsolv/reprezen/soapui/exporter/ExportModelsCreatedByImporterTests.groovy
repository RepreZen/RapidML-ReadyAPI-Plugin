package com.modelsolv.reprezen.soapui.exporter

import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.soapui.RepreZenExporter
import com.modelsolv.reprezen.soapui.importer.RepreZenImporterTest

class ExportModelsCreatedByImporterTests extends ExporterTestBase {

	public void testTaxBlaster() {
		String modelText = exportImportedModel("TaxBlaster.rapid")
		Console.println( modelText )
		assertModelIsValid(modelText)
	}

	public void testTaxBlasterWithExamples() {
		String modelText = exportImportedModel("TaxBlasterWithExamples.rapid")
		Console.println( modelText )
		assertModelIsValid(modelText)
	}

	protected String exportImportedModel(String modelFile) {
		WsdlProject project = new WsdlProject()
		RestService restService = importRepreZenAndGenFirstService(modelFile)
		project.getInterfaceList().add(restService)
		RepreZenExporter exporter = new RepreZenExporter( project )
		return exporter.createRepreZenAsText( restService.name, restService, restService.getBasePath())
	}
}
