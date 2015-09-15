package com.modelsolv.reprezen.soapui.actions;

import java.io.File;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Shows a simple dialog for importing a RepreZen model
 *
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 */

@ActionConfiguration( actionGroup = "EnabledWsdlProjectActions", afterAction = "AddWadlAction")
public class ImportRepreZenAction extends AbstractSoapUIAction<WsdlProject> {
    private XFormDialog dialog;

    public ImportRepreZenAction() {
        super("Import RepreZen Model", "Imports a RepreZen model into SoapUI");
    }

    public void perform(final WsdlProject project, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        } else {
            dialog.setValue(Form.REPREZEN_MODEL_PATH, "");
        }


        while (dialog.show()) {
            try {
                String zenModelUrl = dialog.getValue(Form.REPREZEN_MODEL_PATH).trim();
                if (StringUtils.hasContent(zenModelUrl)) {
                    String expandedZenModelUrl = PathUtils.expandPath(zenModelUrl, project);

                    // if this is a file - convert it to a file URL
                    if (new File(expandedZenModelUrl).exists())
                        expandedZenModelUrl = new File(expandedZenModelUrl).toURI().toURL().toString();

                    XProgressDialog dlg = UISupport.getDialogs().createProgressDialog("Importing API", 0, "", false);
                    dlg.run(new RepreZenImporterWorker(expandedZenModelUrl, project));

                    Analytics.trackAction("ImportRepreZenModel");
                    break;
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    @AForm(name = "Import RepreZen model", description = "Creates a REST API from the specified RepreZen model")
    public interface Form {
        @AField(name = "Import RepreZen model", description = "Location or URL of RepreZen model", type = AFieldType.FILE)
        public final static String REPREZEN_MODEL_PATH = "Import RepreZen model";
 
     }
}