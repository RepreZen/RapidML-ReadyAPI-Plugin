package com.modelsolv.reprezen.soapui.importer;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.plugins.ApiImporter;
import com.eviware.soapui.plugins.PluginApiImporter;
import com.modelsolv.reprezen.soapui.actions.ImportRepreZenAction;

import java.util.ArrayList;
import java.util.List;

@PluginApiImporter( label = "RepreZen API Studio" )
public class RepreZenApiImporter implements ApiImporter {
    @Override
    public List<Interface> importApis(Project project) {

        List<Interface> result = new ArrayList<>();
        int cnt = project.getInterfaceCount();

        ImportRepreZenAction importRepreZenAction = new ImportRepreZenAction();
        importRepreZenAction.perform( (WsdlProject)project, null );

        for( int c = cnt; c < project.getInterfaceCount(); c++)
        {
            result.add( project.getInterfaceAt( c ));
        }

        return result;
    }
}
