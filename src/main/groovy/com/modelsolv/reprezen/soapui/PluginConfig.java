package com.modelsolv.reprezen.soapui;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

/**
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
@PluginConfiguration(groupId = "com.smartbear.soapui.plugins", name = "RepreZen RAPID-ML Plugin", version = "1.0", 
	autoDetect = true, description = "Import/Export REST API models in RAPID-ML format, used in RepreZen API Studio", 
	infoUrl = "https://github.com/ModelSolv/soapui-reprezen-plugin")
public class PluginConfig extends PluginAdapter {
	
	@Override
	public void initialize() {
		super.initialize();
	}
}
