package com.modelsolv.reprezen.soapui.actions;

import java.util.List;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressMonitor;
import com.modelsolv.reprezen.soapui.RepreZenImporter;

/**
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
public class RepreZenImporterWorker extends Worker.WorkerAdapter {
    private final String zenModelUrl;
    private WsdlProject project;

    public RepreZenImporterWorker(String finalExpUrl, WsdlProject project) {
        this.zenModelUrl = finalExpUrl;
        this.project = project;
    }

    public Object construct(XProgressMonitor monitor) {
        try {
            RepreZenImporter importer = new RepreZenImporter(project);
            List<RestService> restServices = importer.importZenModel(zenModelUrl);
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
 
}
