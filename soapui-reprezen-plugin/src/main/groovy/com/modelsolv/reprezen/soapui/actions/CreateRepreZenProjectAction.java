package com.modelsolv.reprezen.soapui.actions;

import java.io.File;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.plugins.auto.PluginImportMethod;
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
 * @author Tatiana Fesenko
 */

@PluginImportMethod( label = "RepreZen Model (REST)")
public class CreateRepreZenProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    private XFormDialog dialog;

    public CreateRepreZenProjectAction() {
        super("Import RepreZen Model", "Imports a RepreZen model into SoapUI");
    }

    public void perform(final WorkspaceImpl workspace, Object param) {
        // initialize form
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        } else {
            dialog.setValue(Form.REPREZEN_MODEL_PATH, "");
            dialog.setValue(Form.PROJECT_NAME, "");
        }

        WsdlProject project = null;

        while (dialog.show()) {
            try {
                // get the specified URL
                String zenModelUrl = dialog.getValue(Form.REPREZEN_MODEL_PATH).trim();
                if (StringUtils.hasContent(zenModelUrl)) {
                    // expand any property-expansions
                    project = workspace.createProject(dialog.getValue(Form.PROJECT_NAME));
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
        @AField(name = "Project Name", description = "Name of the project", type = AField.AFieldType.STRING)
        public final static String PROJECT_NAME = "Project Name";
        
        @AField(name = "Import RepreZen model", description = "Location or URL of RepreZen model", type = AFieldType.FILE)
        public final static String REPREZEN_MODEL_PATH = "Import RepreZen model";
 
     }
}
