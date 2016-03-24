package com.modelsolv.reprezen.soapui;

import java.util.List;

import org.eclipse.emf.common.util.URI;

import com.google.common.collect.Lists;
import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.loaders.HeadlessZenModelLoader;

public class ZenModelUtils {

	/**
	 * @param modelUri
	 * @return
	 * @throws RuntimeException
	 *             if the model is invalid, e.g. the file does not exist or it
	 *             cannot be parsed.
	 */
	public static ZenModel loadModel(String modelUri) throws RuntimeException {
		// loadAndValidateModel() throws an exception if the model is invalid
		ZenModel zenModel = HeadlessZenModelLoader.loadAndValidateModel(URI.createURI(modelUri));
		zenModel.generateImplicitValues();
		return zenModel;
	}

	public static List<String> getWarnings(ZenModel zenModel) {
		List<String> result = Lists.newArrayList();
		if (zenModel.getResourceAPIs().isEmpty()) {
			result.add("The selected RAPID-ML model does not contain any interface definitions!");
		}
		return result;
	}
}
