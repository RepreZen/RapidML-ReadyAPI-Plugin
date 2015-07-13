package com.modelsolv.reprezen.soapui

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.eviware.soapui.impl.rest.*
import com.eviware.soapui.impl.rest.mock.RestMockService
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.google.inject.Guice
import com.google.inject.Injector
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

	public RestService importZenModel(String url) {

	//	if (url.startsWith("file:"))
			File file = new File(new URL(url).toURI())

		logger.info("Importing RepreZen model [$url]")

		def zenModel = loadModel(file)
		//				RestService restService = createRestService(swagger.basePath, swagger.info)
		//				swagger.paths.each {
		//					importPath(restService, it.key, it.value)
		//				}
		//
		//				result.add(restService)
		//				ensureEndpoint(restService, url)
		zenModel

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

	private RestService createRestService(String path, ZenModel zenModel) {
		String name = zenModel?.name
		if (name == null)
			name = path

		RestService restService = project.addNewInterface(name, RestServiceFactory.REST_TYPE)
//		restService.description = zenModel.documentation?.body

		if (path != null) {
			try {
				if (path.startsWith("/")) {
					if (path.length() > 1) {
						restService.basePath = path
					}
				} else {
					URL url = new URL(path)
					def pathPos = path.length() - url.path.length()

					restService.basePath = path.substring(pathPos)
					restService.addEndpoint(path.substring(0, pathPos))
				}
			}
			catch (Exception e) {
				SoapUI.logError(e)
			}
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
