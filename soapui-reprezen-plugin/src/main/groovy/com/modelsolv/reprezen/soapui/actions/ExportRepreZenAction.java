package com.modelsolv.reprezen.soapui.actions;

import java.io.File;
import java.io.FileWriter;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.modelsolv.reprezen.soapui.RepreZenExporter;

/**
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
@ActionConfiguration( actionGroup = "RestServiceActions", afterAction = "ExportWadlAction", separatorBefore = true )
public class ExportRepreZenAction extends AbstractSoapUIAction<RestService>
{
    private static final String TARGET_PATH = Form.class.getName() + Form.FOLDER;

    private XFormDialog dialog;

    public ExportRepreZenAction()
    {
        super( "Export RepreZen", "Creates a RepreZen model for selected REST API" );
    }

    public void perform( RestService restService, Object param )
    {
        // initialize form
        XmlBeansSettingsImpl settings = restService.getSettings();
        if( dialog == null )
        {
            dialog = ADialogBuilder.buildDialog(Form.class);

            String name = restService.getName();
            if( name.startsWith("/") || name.toLowerCase().startsWith("http"))
                name = restService.getProject().getName() + " - " + name;

            dialog.setValue(Form.TITLE, name);
            String baseUri = restService.getEndpoints().length != 0 ? restService.getEndpoints()[0] : "";
            dialog.setValue(Form.BASEURI, baseUri);
            dialog.setValue(Form.FOLDER, settings.getString(TARGET_PATH, ""));
        }

        while( dialog.show() )
        {
            try
            {
                RepreZenExporter exporter = new RepreZenExporter( restService.getProject() );
                String name = dialog.getValue(Form.TITLE);
                if (!RepreZenExporter.hasContent(name)) {
                	name = "MyZenModel";
                }

                String zenModel = exporter.createRepreZenAsText(name, restService,
                        dialog.getValue(Form.BASEURI));


                String folder = dialog.getValue( Form.FOLDER );

                File file = new File( exporter.createFileName( folder, name));
				try (FileWriter writer = new FileWriter(file)) {
					writer.write(zenModel);
					writer.close();
				}

                UISupport.showInfoMessage("ZenModel has been created at [" + file.getAbsolutePath() + "]");

                settings.setString(TARGET_PATH, dialog.getValue(Form.FOLDER));
                Analytics.trackAction("ExportRepreZen");

                break;
            }
            catch( Exception ex )
            {
                UISupport.showErrorMessage( ex );
            }
        }
    }

    @AForm( name = "Export RepreZen model", description = "Creates a RepreZen API model for selected REST APIs in this project" )
    public interface Form
    {
        @AField( name = "Target Folder", description = "Where to save the RepreZen model", type = AField.AFieldType.FOLDER )
        public final static String FOLDER = "Target Folder";

        @AField( name = "Title", description = "The API Title", type = AField.AFieldType.STRING )
        public final static String TITLE = "Title";

        @AField( name = "Base URI", description = "The resource model baseUri", type = AField.AFieldType.STRING )
        public final static String BASEURI = "Base URI";

    }
}
