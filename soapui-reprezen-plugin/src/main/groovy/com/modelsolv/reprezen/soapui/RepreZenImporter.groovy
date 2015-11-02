package com.modelsolv.reprezen.soapui

import org.apache.xmlbeans.XmlBoolean
import org.apache.xmlbeans.XmlDate
import org.apache.xmlbeans.XmlDouble
import org.apache.xmlbeans.XmlInteger
import org.apache.xmlbeans.XmlString;
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod
import com.eviware.soapui.impl.rest.mock.RestMockService
import com.eviware.soapui.impl.rest.support.RestParameter
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.support.StringUtils
import com.modelsolv.reprezen.restapi.HttpMessageParameterLocation
import com.modelsolv.reprezen.restapi.MatrixParameter
import com.modelsolv.reprezen.restapi.MediaType
import com.modelsolv.reprezen.restapi.MessageParameter
import com.modelsolv.reprezen.restapi.Method
import com.modelsolv.reprezen.restapi.Parameter
import com.modelsolv.reprezen.restapi.ResourceAPI
import com.modelsolv.reprezen.restapi.TemplateParameter
import com.modelsolv.reprezen.restapi.TypedMessage
import com.modelsolv.reprezen.restapi.TypedRequest
import com.modelsolv.reprezen.restapi.ZenModel
import com.modelsolv.reprezen.restapi.libraries.util.PrimitiveTypes
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup
import com.modelsolv.reprezen.restapi.xtext.loaders.HeadlessZenModelLoader;


/**
 * Import a RAPID model to Ready! API model.
 *
 *@author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
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
		logger.info("Importing RepreZen / RAPID-ML model [$url]")
		ZenModel zenModel = HeadlessZenModelLoader.loadModel(URI.createURI(url))
		zenModel.generateImplicitValues()
		def List<RestService> result = zenModel.resourceAPIs.collect {
			RestService restService = createRestService(it)
		}
		result
	}

	private RestService createRestService(ResourceAPI resourceAPI) {
		String name = resourceAPI?.name
		RestService restService = project.addNewInterface(name, RestServiceFactory.REST_TYPE)
		restService.description = resourceAPI.documentation?.text
		String path = resourceAPI.getBaseURI()
		if (path != null) {
			URL endpointUrl = new URL(path);
			String basePath = endpointUrl.path;
			if (StringUtils.hasContent(basePath)) {
				restService.addEndpoint(path.substring(0, path.length() - basePath.length()));
				restService.setBasePath(basePath);
			} else {
				restService.basePath = path
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
			def List<MediaType> mediaTypes = rapidRequest.mediaTypes
//			if (mediaTypes.isEmpty()) {
//				mediaTypes = rapidMethod.getContainingResourceDefinition().getMediaTypes()
//			}
			if (mediaTypes.size() > 0) {
				mediaTypes.each {MediaType mediaType ->
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

	private createResponses(RestMethod soapUiMethod, Method rapidMethod) {
		rapidMethod.responses.each{rapidResponse->
			def List<RestRepresentation> soapUiResponses = createMethodRepresentations(soapUiMethod, rapidResponse,
					rapidResponse.statusCode < 400 ? RestRepresentation.Type.RESPONSE : RestRepresentation.Type.FAULT)
			soapUiResponses.each{
				it.status = [rapidResponse.statusCode];
			}
		}
	}

	private List<RestRepresentation> createMethodRepresentations(RestMethod soapUiMethod, TypedMessage rapidMessage, RestRepresentation.Type type) {
		if (!rapidMessage.mediaTypes.isEmpty()) {
			rapidMessage.mediaTypes.collect { mediaType ->
				def RestRepresentation representation = createMethodRepresentation(soapUiMethod, rapidMessage, type)
				representation.mediaType = mediaType.name
				if (!rapidMessage.getAllExamples().isEmpty()) {
					representation.sampleContent = rapidMessage.getAllExamples().get(0).body;
				}
				representation
			}
		} else {
			def RestRepresentation representation = createMethodRepresentation(soapUiMethod, rapidMessage, type)
			if (!rapidMessage.getAllExamples().isEmpty()) {
				representation.sampleContent = rapidMessage.getAllExamples().get(0).body;
			}
			[representation]
		}
	}

	private RestRepresentation createMethodRepresentation(RestMethod soapUiMethod, TypedMessage rapidMessage, RestRepresentation.Type type) {
		def RestRepresentation representation = soapUiMethod.addNewRepresentation(type)
		representation.description = rapidMessage.documentation?.text
		if (!rapidMessage.mediaTypes.isEmpty()) {
			representation.mediaType = rapidMessage.mediaTypes.get(0).name
		}
		representation
	}

	private RestParameter createParamFromNamedProperty(RestParamsPropertyHolder soapUiParams, Parameter rapidParameter) {
		RestParameter param = soapUiParams.addProperty(rapidParameter.name)
		param.style = getParameterStyle(rapidParameter)
		param.description = rapidParameter.documentation?.text
		param.defaultValue = rapidParameter.default
		param.required = rapidParameter.required

		switch (rapidParameter.primitiveType) {
			case PrimitiveTypes.DOUBLE: param.type = XmlDouble.type.name; break;
			case PrimitiveTypes.INTEGER: param.type = XmlInteger.type.name; break;
			case PrimitiveTypes.DATE: param.type = XmlDate.type.name; break;
			case PrimitiveTypes.BOOLEAN: param.type = XmlBoolean.type.name; break;
			case PrimitiveTypes.STRING: param.type = XmlString.type.name; break;
		}
		return param
	}

	private ParameterStyle getParameterStyle(TemplateParameter rapidParameter) {
		return ParameterStyle.TEMPLATE
	}

	private ParameterStyle getParameterStyle(MatrixParameter rapidParameter) {
		return ParameterStyle.MATRIX
	}

	private ParameterStyle getParameterStyle(MessageParameter rapidParameter) {
		switch (rapidParameter.httpLocation) {
			case HttpMessageParameterLocation.HEADER: return ParameterStyle.HEADER
			case HttpMessageParameterLocation.QUERY: return ParameterStyle.QUERY
			default: return ParameterStyle.QUERY
		}
	}

	private void setCreateSampleRequests(boolean createSampleRequests) {
		this.createSampleRequests = createSampleRequests;
	}

	private void setRestMockService(RestMockService restMockService) {
		this.restMockService = restMockService;
	}

	private String methodName(Method method) {
		if (method.id == null || !method.id.trim().isEmpty())
			method.httpMethod.toString().toLowerCase() + method.containingResourceDefinition.name
		else
			method.id
	}
}
