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
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
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
import com.modelsolv.reprezen.restapi.ZenModel
import com.modelsolv.reprezen.restapi.datatypes.DataModel;
import com.modelsolv.reprezen.restapi.datatypes.DatatypesFactory
import com.modelsolv.reprezen.restapi.datatypes.PrimitiveProperty;
import com.modelsolv.reprezen.restapi.datatypes.PrimitiveType
import com.modelsolv.reprezen.restapi.datatypes.Structure;
import com.modelsolv.reprezen.restapi.xtext.RestApiXtextPlugin;
import com.modelsolv.reprezen.restapi.xtext.XtextDslStandaloneSetup
import com.modelsolv.reprezen.restapi.xtext.loaders.MediaTypeRegistry;
import com.modelsolv.reprezen.restapi.xtext.loaders.PrimitiveTypeRegistry;
import com.modelsolv.reprezen.restapi.xtext.loaders.RestModelLoader;
import com.modelsolv.reprezen.restapi.xtext.loaders.ZenLibraries;
import com.modelsolv.reprezen.restapi.xtext.serializers.RepreZenTextSerializer;


/**
 * Export a Ready! API model to RAPID model supported by RepreZen API Studio 
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
class RepreZenExporter {

	private final WsdlProject project
	private final RestapiFactory restapiFactory = RestapiFactory.eINSTANCE
	private final DatatypesFactory datatypesFactory = DatatypesFactory.eINSTANCE
	private final PrimitiveTypeRegistry primitiveTypeRegistry = new PrimitiveTypeRegistry();
	private final MediaTypeRegistry mediaTypeRegistry = new MediaTypeRegistry();

	public RepreZenExporter( WsdlProject project ) {
		this.project = project
	}

	def String createRepreZenAsText(String name, RestService service, String baseUri) {
		ZenModel zenModel = createRepreZen(name, service, baseUri)
		RepreZenTextSerializer serializer = new RepreZenTextSerializer()
		new XtextDslStandaloneSetup().createInjectorAndDoEMFRegistration().injectMembers(serializer)
		return serializer.serializeToDslString(zenModel)
	}

	def ZenModel createRepreZen(String name, RestService service, String baseUri) {
		ZenModel zenModel = restapiFactory.createZenModel()
		zenModel.name = normalize(name)
		ResourceAPI resourceAPI = restapiFactory.createResourceAPI();
		resourceAPI.name = normalize(name)
		resourceAPI.baseURI = baseUri
		zenModel.resourceAPIs.add(resourceAPI)
		DataModel dataModel = datatypesFactory.createDataModel()
		dataModel.setName(normalize(name)+"DataModel")
		zenModel.getDataModels().add(dataModel)
		exportRestService( service, resourceAPI, dataModel)
		return zenModel
	}

	private def exportRestService(RestService restService, ResourceAPI resourceAPI, DataModel dataModel) {
		restService.resourceList.each {
			def ServiceDataResource res = createResourceDefinition( it, resourceAPI, dataModel)
			resourceAPI.ownedResourceDefinitions.add(res)
		}
	}

	private def ServiceDataResource createResourceDefinition( RestResource resource, ResourceAPI resourceAPI, DataModel dataModel ) {
		ServiceDataResource result = restapiFactory.createObjectResource()
		result.name = normalize(resource.name)
		if( hasContent(resource.description))
			addDocumentation(result, resource.description)


		URI uri = restapiFactory.createURI()
		Collection<RestParameter> templateParams = resource.params.values().findAll{
			it.style == RestParamsPropertyHolder.ParameterStyle.TEMPLATE
		}
		resource.path.split("/").each {
			String segment = it
			URISegment uriSegment
			RestParameter templateParam = templateParams.find{ param->
				"{"+param.name+"}" == segment
			}
			if (templateParam != null) {
				uriSegment = restapiFactory.createURISegmentWithParameter()
				TemplateParameter zenParameter = createUriParameter(templateParam)
				zenParameter.uriSegment = uriSegment
				PrimitiveTypeSourceReference sourceReference = restapiFactory.createPrimitiveTypeSourceReference()
				sourceReference.simpleType = getZenPrimitiveProperty(templateParam.type.localPart)
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
			Method method = createMethod( it, resourceAPI )
			result.methods.add(method)
		}

		resource.childResourceList.each {
			ResourceDefinition res = createResourceDefinition( it, resourceAPI, dataModel)
			resourceAPI.ownedResourceDefinitions.add(res )
		}
		Structure structure = datatypesFactory.createStructure()
		dataModel.getOwnedDataTypes().add(structure)
		structure.setName(result.getName() + "Type")
		result.setType(structure)
		return result
	}

	private def createMethod( RestMethod restMethod, ResourceAPI resourceAPI ) {
		Method result = restapiFactory.createMethod()
		result.id = normalize(restMethod.name)
		result.setHttpMethod(HTTPMethods.valueOf(restMethod.method.name().toUpperCase()))

		TypedRequest request = restapiFactory.createTypedRequest()
		result.request = request
		restMethod.requestList.findAll{it.response == null}.each {
			MediaType mediaType = mediaTypeRegistry.getElement(it.mediaType)
			if (mediaType != null) {
				request.mediaTypes.add(mediaType)
			}
			if(hasContent( it.requestContent )) {
				InlineExample example = restapiFactory.createInlineExample();
				example.setBody(it.requestContent)
				request.getExamples().add(example)
			}
			for (name in it.getPropertyNames()) {
				def RestParameter param = it.getProperty(name)
				switch (param.style) {
					case ParameterStyle.HEADER: request.getParameters().add(createHeaderParameter(param)); break;
					case ParameterStyle.QUERY: request.getParameters().add(createQueryParameter(param)); break;
				}
			}
		}
		List<RestRepresentation> responses = restMethod.representations.findAll{it.type == RestRepresentation.Type.RESPONSE || it.type == RestRepresentation.Type.FAULT }
		responses.each{restResponse ->
			restResponse.status.each {responseStatus ->
				TypedResponse response = restapiFactory.createTypedResponse()
				result.responses.add(response)
				MediaType mediaType = mediaTypeRegistry.getElement(restResponse.mediaType)
				if (mediaType != null) {
					request.mediaTypes.add(mediaType)
				}
				// Message schema is not translated to SOAP UI model.
				// Can we specify a message schema (XSD or JSON schema) in Ready! API?
				// Related forum topic - http://community.smartbear.com/t5/Ready-API-and-SoapUI-PlugIn/Message-Payload-Schema/m-p/104037#U104037

				response.statusCode = responseStatus
				restMethod.requestList.each {
					if( it.response != null && it.response.statusCode == response.statusCode) {
						response.examples.add(it.response.contentAsString)
					}
				}
			}
		}
		return result
	}

	private def createMessageParameter( RestParameter p ) {
		MessageParameter zenParameter = restapiFactory.createMessageParameter();
		zenParameter.setName(p.getName())
		PrimitiveTypeSourceReference sourceReference = restapiFactory.createPrimitiveTypeSourceReference()
		sourceReference.simpleType = getZenPrimitiveProperty(p.type.localPart)
		zenParameter.setSourceReference(sourceReference)
		return setParameterProperties(p, zenParameter)
	}

	private def createQueryParameter( RestParameter p ) {
		MessageParameter zenParameter = createMessageParameter(p);
		zenParameter.setHttpLocation(HttpMessageParameterLocation.QUERY)
		return zenParameter
	}

	private def createHeaderParameter( RestParameter p ) {
		MessageParameter zenParameter = createMessageParameter(p);
		zenParameter.setHttpLocation(HttpMessageParameterLocation.HEADER)
		return zenParameter
	}

	private def URIParameter createUriParameter(RestParameter p ) {
		URIParameter result = setParameterProperties(p, restapiFactory.createTemplateParameter())
		return result
	}

	private def setParameterProperties(RestParameter p, Parameter result) {
		result.required = p.required

		if (hasContent(p.defaultValue))
			result.default = p.defaultValue

		if (hasContent(p.description))
			addDocumentation(result, p.description)

		return result
	}

	private def String normalize(String nodeName) {
		return escape(nodeName);
	}

	private def String escape(String nodeName) {
		return nodeName.replaceAll("/", "_").replaceAll("\\.", "_").replaceAll("-", "_").replaceAll(" ", "_");
	}

	private def addDocumentation(Documentable documentable, String documentationValue) {
		def result = restapiFactory.createDocumentation()
		// Trim is essential here. A leading whitespace causes a
		// "All 0 values of<...> have been consumed. More are needed to continue here." error
		result.text = documentationValue.trim()
		documentable.documentation = result
	}

	private PrimitiveType getZenPrimitiveProperty(String soapUiTypeName) {
		String zenType
		switch (soapUiTypeName) {
			case "dateTime": zenType = "dateType"; break
			case "float": zenType = "float"; break
			case "double": zenType = "double"; break
			case "long": zenType = "long"; break
			case "short":
			case "int":
					case "integer": zenType = "int"; break
			case "boolean": zenType = "boolean"; break
			default: zenType = "string"
		}
		return primitiveTypeRegistry.getElement(zenType)
	}

	public static String createFileName( String path, String title ) {
		return path + File.separatorChar + StringUtils.createFileName( title, (char)'-' ) + ".zen"
	}

	static boolean hasContent( String str ) {
		return str != null && str.trim().length() > 0
	}
}
