package com.modelsolv.reprezen.soapui;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

/**
 * @author <a href="mailto:tatiana.fesenko@reprezen.com">Tatiana Fesenko</a>
 *
 */
@PluginConfiguration(groupId = "com.smartbear.soapui.plugins", name = "RepreZen API Studio Plugin", version = "1.0", autoDetect = true, description = "Provides RepreZen API Studio import/export functionality for REST APIs", infoUrl = "https://github.com/ModelSolv/soapui-reprezen-plugin")
public class PluginConfig extends PluginAdapter {
	@Override
	public void initialize() {
		super.initialize();
	}
}
