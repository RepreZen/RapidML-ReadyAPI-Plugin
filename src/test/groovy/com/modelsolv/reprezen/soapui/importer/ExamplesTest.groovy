package com.modelsolv.reprezen.soapui.importer

import com.eviware.soapui.impl.rest.RestMethod
import com.eviware.soapui.impl.rest.RestRepresentation
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParameter;
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.modelsolv.reprezen.restapi.HTTPMethods;


class ExamplesTest extends GroovyTestCase {
	def personXmlContents = """<?xml version="1.0" encoding="UTF-8"?>
<Person version="1.0">
  <taxpayerID>ssn-xx-xxxx</taxpayerID>
  <lastName>Orwell</lastName>
  <firstName>George</firstName>
  <otherNames>Eric Arthur Blair</otherNames>
</Person>
"""

	public void testInlineExamples() {
		RestService restService = RepreZenImporterTest.importRepreZen("TaxBlasterWithExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 5
		RestResource objectResource = resources.get("/people")
		def methods = objectResource.methods
		RestMethod getMethod = objectResource.methods.find {
			it.method.name() == "GET"
		}
		assert getMethod != null
		RestRepresentation response = getMethod.representations.find {
			it.type == RestRepresentation.Type.RESPONSE
		}
		assert response != null
		assert equalsIgnoreLinebreaks(response.sampleContent,  """[
  {
    "taxpayerID": "user1",
    "lastName": "Smith",
    "firstName": "John",
    "otherNames": [
      "Jean Poupon"
    ]
  },
  {
    "taxpayerID": "user2",
    "lastName": "Williams",
    "firstName": "Nancy"
  },
  {
    "taxpayerID": "user3",
    "lastName": "Davis",
    "firstName": "Elizabeth"
  },
  {
    "taxpayerID": "user4",
    "lastName": "Johnson",
    "firstName": "Robert"
  }
]""")
	}

	public void testExternalExamplesInResponse() {
		RestService restService = RepreZenImporterTest.importRepreZen("externalExamples/TaxBlasterWithExternalExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 2
		RestResource objectResource = resources.get("/people/{id}")
		def methods = objectResource.methods
		RestMethod getMethod = objectResource.methods.find {
			it.method.name() == "PUT"
		}
		assert getMethod != null
		RestRepresentation response = getMethod.representations.find {
			it.type == RestRepresentation.Type.RESPONSE
		}
		assert response != null
		assert equalsIgnoreLinebreaks(response.sampleContent, personXmlContents)
	}

	public void testExternalExamplesInRequest() {
		RestService restService = RepreZenImporterTest.importRepreZen("externalExamples/TaxBlasterWithExternalExamples.zen")
		def Map<String, RestResource> resources = restService.getResources()
		assert resources.size() == 2
		RestResource objectResource = resources.get("/people/{id}")
		def methods = objectResource.methods
		RestMethod getMethod = objectResource.methods.find {
			it.method.name() == "PUT"
		}
		assert getMethod != null
		RestRequest request = getMethod.requests.first()
		assert request != null
		assert equalsIgnoreLinebreaks(request.representations[0].sampleContent, personXmlContents)
	}

	/**
	 * Used to compare the result of {@link com.eviware.soapui.impl.rest.RestRepresentation#sampleContent()}
	 *  with a predefined output as  {@link com.eviware.soapui.impl.rest.RestRepresentation#sampleContent()} uses OS-specific linebreaks
	 * @param s1
	 * @param s2
	 * @return
	 */
	protected boolean equalsIgnoreLinebreaks(String s1, String s2) {
		normalizeNL(s1) == normalizeNL(s2)
	}

	protected String normalizeNL(String str) {
		return str.replaceAll("\\r?\\n", "\n");
	}
}
