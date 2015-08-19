package com.modelsolv.reprezen.soapui;

import com.google.common.base.Function;
import com.modelsolv.reprezen.restapi.MediaType;
import com.modelsolv.reprezen.restapi.xtext.loaders.ZenLibraries;

/**
 * 
 * @author Tatiana Fesenko
 *
 */
public class MediaTypeRegistry extends LibraryRegistry<MediaType> {
	public MediaTypeRegistry() {
		super(MediaType.class, ZenLibraries.STANDARD_MEDIA_TYPES);
	}

	@Override
	protected Function<MediaType, String> getElementId() {
		return new Function<MediaType, String>() {

			@Override
			public String apply(MediaType element) {
				return element.getName();
			}

		};

	}

}
