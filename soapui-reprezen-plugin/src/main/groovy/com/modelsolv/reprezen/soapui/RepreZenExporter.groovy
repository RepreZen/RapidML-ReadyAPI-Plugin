package com.modelsolv.reprezen.soapui

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequestInterface
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils
import com.google.common.collect.Maps
import com.modelsolv.reprezen.core.workspace.EmfWorkspace;
import com.modelsolv.reprezen.core.workspace.Workspace;
import com.modelsolv.reprezen.restapi.Documentable
import com.modelsolv.reprezen.restapi.HTTPMethods;
import com.modelsolv.reprezen.restapi.HttpMessageParameterLocation
import com.modelsolv.reprezen.restapi.InlineExample
import com.modelsolv.reprezen.restapi.MediaType;
import com.modelsolv.reprezen.restapi.MessageParameter;
import com.modelsolv.reprezen.restapi.Method
import com.modelsolv.reprezen.restapi.Parameter
import com.modelsolv.reprezen.restapi.PrimitiveTypeSourceReference;
import com.modelsolv.reprezen.restapi.ResourceAPI
import com.modelsolv.reprezen.restapi.URI
import com.modelsolv.reprezen.restapi.ResourceDefinition;
import com.modelsolv.reprezen.restapi.RestapiFactory
import com.modelsolv.reprezen.restapi.ServiceDataResource
import com.modelsolv.reprezen.restapi.TemplateParameter;
import com.modelsolv.reprezen.restapi.TypedRequest
import com.modelsolv.reprezen.restapi.TypedResponse;
import com.modelsolv.reprezen.restapi.URIParameter
import com.modelsolv.reprezen.restapi.URISegment;
import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.datatypes.DatatypesFactory
import com.modelsolv.reprezen.restapi.datatypes.PrimitiveType
import com.modelsolv.reprezen.restapi.xtext.RestApiXtextPlugin;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup
import com.modelsolv.reprezen.restapi.xtext.loaders.DslRestModelLoader;
import com.modelsolv.reprezen.restapi.xtext.loaders.RestModelLoader;
import com.modelsolv.reprezen.restapi.xtext.loaders.ZenLibraries;
import com.modelsolv.reprezen.restapi.xtext.serializers.RepreZenTextSerializer;


class RepreZenExporter {

	private final WsdlProject project
	private final RestapiFactory restapiFactory = RestapiFactory.eINSTANCE
	private final DatatypesFactory datatypesFactory = DatatypesFactory.eINSTANCE
	private final PrimitiveTypeRegistry primitiveTypeRegistry = new PrimitiveTypeRegistry();
	private final MediaTypeRegistry mediaTypeRegistry = new MediaTypeRegistry();

	public RepreZenExporter( WsdlProject project ) {
		this.project = project
	}

	String createRepreZen(String name, RestService service, String baseUri) {

		ZenModel zenModel = restapiFactory.createZenModel()

		zenModel.name = normalize(name)

		ResourceAPI resourceAPI = restapiFactory.createResourceAPI();
		resourceAPI.name = normalize(name)
		resourceAPI.baseURI = baseUri
		zenModel.resourceAPIs.add(resourceAPI)

		exportRestService( service, resourceAPI)

		RepreZenTextSerializer serializer = new RepreZenTextSerializer()
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration().injectMembers(serializer)
		return serializer.serializeToDslString(zenModel)
	}

	def exportRestService(RestService restService, ResourceAPI resourceAPI) {
		restService.resourceList.each {
			def ResourceDefinition res = createResourceDefinition( it, resourceAPI )
			resourceAPI.ownedResourceDefinitions.add(res)
		}
	}

	def ResourceDefinition createResourceDefinition( RestResource resource, ResourceAPI resourceAPI ) {
		ServiceDataResource result = restapiFactory.createObjectResource()
		result.name = normalize(resource.name)
		if( hasContent(resource.description))
			addDocumentation(result, resource.description)


		URI uri = restapiFactory.createURI()
		Collection<RestParameter> templateParams = resource.params.values().findAll{it.style == RestParamsPropertyHolder.ParameterStyle.TEMPLATE}
		resource.path.split("/").each {
			String segment = it
			URISegment uriSegment
			RestParameter templateParam = templateParams.find{param->"{"+param.name+"}" == segment}
			if (templateParam != null) {
				uriSegment = restapiFactory.createURISegmentWithParameter()
				TemplateParameter zenParameter = createUriParameter(templateParam)
				zenParameter.uriSegment = uriSegment
				PrimitiveTypeSourceReference sourceReference = restapiFactory.createPrimitiveTypeSourceReference()
				// TODO find the most appropriate type
				sourceReference.simpleType = primitiveTypeRegistry.getElement("string")
				zenParameter.sourceReference = sourceReference
				uri.uriParameters.add(zenParameter)
				uriSegment.name = templateParam.name
			} else {
				uriSegment = restapiFactory.createURISegment()
				uriSegment.name = segment
			}
			uri.segments.add(uriSegment)
		}
		result.URI = uri

		resource.restMethodList.each {
			Method method = createZenMethod( it, resourceAPI )
			result.methods.add(method)
		}

		resource.childResourceList.each {
			ResourceDefinition res = createResourceDefinition( it, resourceAPI )
			resourceAPI.ownedResourceDefinitions.add(res )
		}

		return result
	}

	def createZenMethod( RestMethod restMethod, ResourceAPI resourceAPI ) {
		Method result = restapiFactory.createMethod()
		result.id = normalize(restMethod.name)
		result.setHttpMethod(HTTPMethods.valueOf(restMethod.method.name().toUpperCase()))

		// TODO process method parameters (header and query)

		TypedRequest request = restapiFactory.createTypedRequest()
		result.request = request
		restMethod.requestList.findAll{it.response == null}.each {
			MediaType mediaType = mediaTypeRegistry.getElement(it.mediaType)
			if (mediaType != null) {
				request.mediaTypes.add(mediaType)
			}
			if(hasContent( it.requestContent ))
			{
				InlineExample example = restapiFactory.createInlineExample();
				example.setBody(it.requestContent)
				request.getExamples().add(example)
			}
		}
		List<RestRepresentation> responses = restMethod.representations.findAll{it.type == RestRepresentation.Type.RESPONSE || it.type == RestRepresentation.Type.FAULT }
		responses.each{restResponse ->
			TypedResponse response = restapiFactory.createTypedResponse()
			result.responses.add(response)
			MediaType mediaType = mediaTypeRegistry.getElement(restResponse.mediaType)
			if (mediaType != null) {
				request.mediaTypes.add(mediaType)
			}
			// TODO create Zen responses for each status code
			restResponse.status.each {response.statusCode = it}
			restMethod.requestList.each {
				if( it.response != null && it.response.statusCode == response.statusCode) {
					response.examples.add(it.response.contentAsString)
				}
			}
		}
		return result
	}

	def createQueryParameter( RestParameter p ) {
		MessageParameter zenParameter = restapiFactory.createMessageParameter();
		zenParameter.setHttpLocation(HttpMessageParameterLocation.QUERY)
		return setDefaultParameterProperties(p, zenParameter)
	}

	def createHeaderParameter( RestParameter p ) {
		MessageParameter zenParameter = restapiFactory.createMessageParameter();
		zenParameter.setHttpLocation(HttpMessageParameterLocation.HEADER)
		return setDefaultParameterProperties(p, zenParameter)
	}

	def URIParameter createUriParameter(RestParameter p ) {
		URIParameter result = setDefaultParameterProperties(p, restapiFactory.createTemplateParameter())
		return result
	}

	public def setDefaultParameterProperties(RestParameter p, Parameter result) {
		result.required = p.required

		if (hasContent(p.defaultValue))
			result.default = p.defaultValue

		if (hasContent(p.description))
			addDocumentation(result, p.description)

		return result
	}

	def String normalize(String nodeName) {
		return escape(nodeName);
	}

	def String escape(String nodeName) {
		return nodeName.replaceAll("/", "_").replaceAll("\\.", "_").replaceAll("-", "_").replaceAll(" ", "_");
	}

	def addDocumentation(Documentable documentable, String documentationValue) {
		def result = restapiFactory.createDocumentation
		// Trim is essential here. A leading whitespace causes a
		// "All 0 values of<...> have been consumed. More are needed to continue here." error
		result.text = documentationValue.trim
		documentable.documentation = result
	}

	public static String createFileName( String path, String title ) {
		return path + File.separatorChar + StringUtils.createFileName( title, (char)'-' ) + ".zen"
	}

	static def hasContent( String str ) {
		return str != null && str.trim().length() > 0
	}
}
