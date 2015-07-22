/**
 *  Copyright 2013 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.modelsolv.reprezen.soapui.actions;

import java.awt.Dimension;
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
import com.modelsolv.reprezen.soapui.RepreZenImporterException;

/**
 * Shows a simple dialog for importing a RepreZen model
 *
 * @author Tatiana Fesenko
 */

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions", afterAction = "AddWadlAction")
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
				String path = dialog.getValue(Form.REPREZEN_MODEL_PATH);
				if (StringUtils.hasContent(path)) {
					path = PathUtils.expandPath(path, project);
					File file = new File(path.trim());
					if (file.exists()) {
						XProgressDialog dlg = UISupport.getDialogs()
								.createProgressDialog("Importing RepreZen API model", 0, "", false);
						dlg.run(new RepreZenImporterWorker(file, project));
						Analytics.trackAction("ImportRepreZenModel");
					} else {
						UISupport.showExtendedInfo("Error", "File does not exist.", "Selected file does not exist.",
								new Dimension(200, 50));
					}
					break;
				}
			} catch (Throwable ex) {
				UISupport.showErrorMessage("Error") ;//, "An error occured."); //, ex.toString(), new Dimension(200, 50));
			}
		}
	}

	@AForm(name = "Import RepreZen model", description = "Creates a REST API from the specified RepreZen model")
	public interface Form {
		@AField(name = "Select RepreZen model", description = "Location of RepreZen model (a *.zen or *.rml file)", type = AFieldType.FILE)
		public final static String REPREZEN_MODEL_PATH = "Import RepreZen model";

	}
}