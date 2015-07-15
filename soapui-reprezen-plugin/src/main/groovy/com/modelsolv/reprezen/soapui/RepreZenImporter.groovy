package com.modelsolv.reprezen.soapui

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.eviware.soapui.support.UISupport

import com.eviware.soapui.impl.rest.*
import com.eviware.soapui.impl.rest.mock.RestMockService
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.google.inject.Guice
import com.google.inject.Injector
import com.modelsolv.reprezen.restapi.ResourceAPI;
import com.modelsolv.reprezen.restapi.ZenModel
import com.modelsolv.reprezen.restapi.xtext.XtextDslRuntimeModule
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup


/**
 * A simple RepreZen importer for SOAP UI
 *
 * @author Tatiana Fesenko
 */

class RepreZenImporter {

	private static Logger logger = LoggerFactory.getLogger(RepreZenImporter.class)
	private final WsdlProject project
	private boolean createSampleRequests
	private RestMockService restMockService
	private Map<String,RestResource> resourceMap = new HashMap<>();

	public RepreZenImporter(WsdlProject project) {
		this.project = project
	}

	public List<RestService> importZenModel(String url) {

		//	if (url.startsWith("file:"))
		File file = new File(new URL(url).toURI())

		logger.info("Importing RepreZen model [$url]")
		ZenModel zenModel = loadModel(file)
		def List<RestService> result = zenModel.resourceAPIs.collect {
			RestService restService = createRestService(it)
		}
		result

	}

	private ZenModel loadModel(File file) {
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = new XtextResourceSet();
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		org.eclipse.emf.ecore.resource.Resource resource = resourceSet.getResource(
				URI.createFileURI(file.getAbsolutePath()), true);
		XtextResource xtextResource = (XtextResource) resource;
		ZenModel model = (ZenModel) xtextResource.getContents().get(0);
		if (!xtextResource.getErrors().isEmpty()) {
			for (Diagnostic error : xtextResource.getErrors()) {
				System.err.println(error);
			}
		}
		return model;
	}

	private RestService createRestService(ResourceAPI resourceAPI) {
		String name = resourceAPI?.name
		RestService restService = project.addNewInterface(name, RestServiceFactory.REST_TYPE)
		restService.description = resourceAPI.documentation?.text
		// TODO generate REST services for resource APIs
		String path = resourceAPI.getBaseURI()
		if (path != null) {
			try {
				restService.basePath = path
				// TODO set endpoint
			}
			catch (Exception e) {
				UISupport.showErrorMessage(e)
			}
		}
		resourceAPI.getOwnedResourceDefinitions().each{rapidResource ->
			def soapUiResource = restService.addNewResource(rapidResource.name, "/"+rapidResource.getURI().toString())
			soapUiResource.description = rapidResource.documentation?.text
		}
		return restService
	}

	public void setCreateSampleRequests(boolean createSampleRequests) {
		this.createSampleRequests = createSampleRequests;
	}

	public void setRestMockService(RestMockService restMockService) {
		this.restMockService = restMockService;
	}
}
