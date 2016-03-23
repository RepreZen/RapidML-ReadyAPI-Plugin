package com.modelsolv.reprezen.soapui.actions;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressMonitor;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.modelsolv.reprezen.restapi.ZenModel;
import com.modelsolv.reprezen.restapi.xtext.loaders.HeadlessZenModelLoader;
import com.modelsolv.reprezen.soapui.RepreZenImporter;
import com.modelsolv.reprezen.soapui.ZenModelUtils;

/**
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
public class RepreZenImporterWorker extends Worker.WorkerAdapter {
	private final String zenModelUrl;
	private WsdlProject project;
	private static Logger logger = LoggerFactory.getLogger(RepreZenImporterWorker.class);

	public RepreZenImporterWorker(String finalExpUrl, WsdlProject project) {
		this.zenModelUrl = finalExpUrl;
		this.project = project;
	}

	public Object construct(XProgressMonitor monitor) {
		try {
			RepreZenImporter importer = new RepreZenImporter(project);
			ZenModel zenModel = loadAndValidateZenModel(zenModelUrl);
			List<RestService> restServices = importer.importZenModel(zenModel);
			RestService restService = null;
			if (!restServices.isEmpty()) {
				UISupport.select(restServices.get(0));
				restService = restServices.get(0);
			}
			Analytics.trackAction("ImportRepreZen");
			return restService;
		} catch (Throwable e) {
			UISupport.showErrorMessage(e);
		}

		return null;
	}

	protected ZenModel loadAndValidateZenModel(String url) {
		logger.info("Importing RepreZen / RAPID-ML model [$url]");
		ZenModel zenModel = ZenModelUtils.loadModel(url);
		zenModel.generateImplicitValues();
		List<String> warnings = ZenModelUtils.getWarnings(zenModel);
		if (!warnings.isEmpty()) {
			UISupport.showInfoMessage(Joiner.on("\n").join(warnings), "RAPID-ML Model Validation Warnings");
		}
		return zenModel;
	}

}
