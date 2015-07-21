package com.modelsolv.reprezen.soapui

import org.apache.xalan.lib.sql.QueryParameter;
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod
import com.eviware.soapui.impl.rest.mock.RestMockService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.plugins.ApiImporter;
import com.eviware.soapui.support.UISupport
import com.eviware.soapui.support.types.StringToStringsMap
import com.modelsolv.reprezen.restapi.HttpMessageParameterLocation;
import com.modelsolv.reprezen.restapi.MatrixParameter;
import com.modelsolv.reprezen.restapi.MediaType
import com.modelsolv.reprezen.restapi.MessageParameter;
import com.modelsolv.reprezen.restapi.Method
import com.modelsolv.reprezen.restapi.Parameter;
import com.modelsolv.reprezen.restapi.ResourceAPI
import com.modelsolv.reprezen.restapi.TemplateParameter;
import com.modelsolv.reprezen.restapi.TypedMessage;
import com.modelsolv.reprezen.restapi.TypedRequest;
import com.modelsolv.reprezen.restapi.ZenModel
import com.modelsolv.reprezen.restapi.libraries.util.PrimitiveTypes;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup
import com.eviware.soapui.plugins.PluginApiImporter;


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
			createRestResource(restService, rapidResource)
		}
		return restService
	}

	private createRestResource(RestService restService, com.modelsolv.reprezen.restapi.ResourceDefinition rapidResource) {
		def soapUiResource = restService.addNewResource(rapidResource.name, "/"+rapidResource.getURI().toString())
		soapUiResource.description = rapidResource.documentation?.text
		rapidResource.getURI().getUriParameters().each{param ->
			createParamFromNamedProperty(soapUiResource.params, param)
		}
		rapidResource.methods.each {rapidMethod->
			createMethod(soapUiResource, rapidMethod)
		}
	}

	private createMethod(RestResource soapUiResource, Method rapidMethod) {
		def RestMethod soapUiMethod = soapUiResource.addNewMethod(methodName(rapidMethod))
		soapUiMethod.method = HttpMethod.valueOf(rapidMethod.httpMethod.getName())
		soapUiMethod.description = rapidMethod.documentation?.text
		createRequest(soapUiMethod, rapidMethod)
		createResponses(soapUiMethod, rapidMethod)
	}

	private createRequest(RestMethod soapUiMethod, Method rapidMethod) {
		if (rapidMethod.request != null) {
			def TypedRequest rapidRequest= rapidMethod.request
			createMethodRepresentations(soapUiMethod, rapidRequest, RestRepresentation.Type.REQUEST)
			if (rapidRequest.mediaTypes.size() > 0) {
				rapidRequest.mediaTypes.each {MediaType mediaType ->
					def RestRequest soapUiRequest = soapUiMethod.addNewRequest("Request " + mediaType.name)
					soapUiRequest.accept = mediaType.name
					rapidRequest.parameters.each {param ->
						createParamFromNamedProperty(soapUiRequest.getParams(), param)
					}
				}
			} else {
				def RestRequest soapUiRequest = soapUiMethod.addNewRequest("Request 1")
				rapidRequest.parameters.each {param ->
					createParamFromNamedProperty(soapUiRequest.getParams(), param)
				}
			}
		}
	}

	private List<RestRepresentation> createMethodRepresentations(RestMethod soapUiMethod, TypedMessage rapidMessage, RestRepresentation.Type type) {
		rapidMessage.allExamples.collect { example ->
			def RestRepresentation representation = soapUiMethod.addNewRepresentation(type)
			representation.description = rapidMessage.documentation?.text
			if (!rapidMessage.mediaTypes.isEmpty()) {
				representation.mediaType = rapidMessage.mediaTypes.get(0).name
			}
			// What's equivalent of exampleContent? The defaultContent is r/o
			// representation.defaultContent = example.body;
			representation
		}
	}

	private createResponses(RestMethod soapUiMethod, Method rapidMethod) {
		rapidMethod.responses.each{rapidResponse->
			def List<RestRepresentation> soapUiResponses = createMethodRepresentations(soapUiMethod, rapidResponse,
					rapidResponse.statusCode < 400 ? RestRepresentation.Type.RESPONSE : RestRepresentation.Type.FAULT)
			soapUiResponses.each{it.status = [rapidResponse.statusCode]}
		}
	}

	private RestParameter createParamFromNamedProperty(RestParamsPropertyHolder soapUiParams, Parameter rapidParameter) {
		RestParameter param = soapUiParams.addProperty(rapidParameter.name)

		param.style = getParameterStyle(rapidParameter)
		param.description = rapidParameter.documentation?.text
		param.defaultValue = rapidParameter.default

		param.required =rapidParameter.required

		// TODO - support enumerations

		//		if( param.options == null || param.options.length == 0 )
		//			param.options = p.enumeration
		// TODO - set type
		//		switch (rapidParameter.type) {
		//			case PrimitiveTypes.DOUBLE: param.type = XmlDouble.type.name; break;
		//			case PrimitiveTypes.INTEGER: param.type = XmlInteger.type.name; break;
		//			case PrimitiveTypes.DATE: param.type = XmlDate.type.name; break;
		//			case PrimitiveTypes.BOOLEAN: param.type = XmlBoolean.type.name; break;
		//		}

		return param
	}

	private ParameterStyle getParameterStyle(TemplateParameter rapidParameter) {
		return ParameterStyle.TEMPLATE
	}

	private ParameterStyle getParameterStyle(MatrixParameter rapidParameter) {
		return ParameterStyle.MATRIX
	}

	private ParameterStyle getParameterStyle(MessageParameter rapidParameter) {
		switch (rapidParameter.type) {
			case HttpMessageParameterLocation.HEADER: return ParameterStyle.HEADER
			case HttpMessageParameterLocation.QUERY: return ParameterStyle.QUERY
		}
	}

	public void setCreateSampleRequests(boolean createSampleRequests) {
		this.createSampleRequests = createSampleRequests;
	}

	public void setRestMockService(RestMockService restMockService) {
		this.restMockService = restMockService;
	}

	def private methodName(Method method) {
		if (method.id == null || !method.id.trim().isEmpty())
			method.httpMethod.toString().toLowerCase() + method.containingResourceDefinition.name
		else
			method.id
	}
}
