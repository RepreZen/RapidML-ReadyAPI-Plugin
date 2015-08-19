package com.modelsolv.reprezen.soapui;

import com.google.common.base.Function;
import com.modelsolv.reprezen.restapi.datatypes.PrimitiveType;
import com.modelsolv.reprezen.restapi.xtext.loaders.ZenLibraries;

/**
 * 
 * @author Tatiana Fesenko
 *
 */
public class PrimitiveTypeRegistry extends LibraryRegistry<PrimitiveType> {
	public PrimitiveTypeRegistry() {
		super(PrimitiveType.class, ZenLibraries.PRIMITIVE_TYPES);
	}

	@Override
	protected Function<PrimitiveType, String> getElementId() {
		return new Function<PrimitiveType, String>() {

			@Override
			public String apply(PrimitiveType element) {
				return element.getName();
			}

		};

	}

}
