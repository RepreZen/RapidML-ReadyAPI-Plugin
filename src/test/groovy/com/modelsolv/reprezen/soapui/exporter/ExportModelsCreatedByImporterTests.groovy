package com.modelsolv.reprezen.soapui.exporter

import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.CollectionResource
import com.modelsolv.reprezen.restapi.ObjectResource;
import com.modelsolv.reprezen.soapui.RepreZenExporter
import com.modelsolv.reprezen.soapui.importer.RepreZenImporterTest

class ExportModelsCreatedByImporterTests extends GroovyTestCase {

	public void testTaxBlasterIsValid() {
		String modelText = exportImportedModel("TaxBlaster.rapid")
		//Console.println( modelText )
		RepreZenExporterTests.validateModel(modelText)
	}

	public void testCollectionResourceTypePreserved() {
		String modelText = exportImportedModel("TaxBlaster.rapid")
		def zenModel = RepreZenExporterTests.loadModel(modelText)
		def resources = zenModel.resourceAPIs.get(0).ownedResourceDefinitions;

		def taxFilingObject = resources.find{it.name == "TaxFilingObject"}
		assert taxFilingObject instanceof ObjectResource

		def taxFilingCollection = resources.find{it.name == "TaxFilingCollection"}
		assert taxFilingCollection instanceof CollectionResource
	}

	public void testTaxBlasterWithExamples() {
		String modelText = exportImportedModel("TaxBlasterWithExamples.rapid")
		//Console.println( modelText )
		RepreZenExporterTests.validateModel(modelText)
	}

	protected String exportImportedModel(String modelFile) {
		WsdlProject project = new WsdlProject()
		RestService restService = RepreZenImporterTest.importRepreZenAndGenFirstService(modelFile)
		project.getInterfaceList().add(restService)
		RepreZenExporter exporter = new RepreZenExporter( project )
		return exporter.createRepreZenAsText( restService.name, restService, restService.getBasePath())
	}
}
