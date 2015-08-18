# soapui-reprezen-plugin
RepreZen API Studio plugin for SoapUI

### Download & Install

Use the "Plugin Manager" button in Ready!API 1.3.1 to load the jar created by Maven build using the "Load plugin from file" button.

### Build it yourself

Clone the Git repository, make sure you have maven installed, and run

```
mvn clean install assembly:single
```

### TODO
* Move RepreZenImporter#importZenModel() to the core RepreZen code for reuse
* Optimize File to string and string to File conversion for loading RepreZen model. 
* Update label in `com.modelsolv.reprezen.soapui.PluginConfig`, it sais that the plugin provides "import/export functionality", but it only provides import

### Questions
* RAML generator defines an API Importer class which extends  `com.eviware.soapui.plugins.ApiImporter`. I created a similar `RepreZenApiImporter` for RepreZen, but I don't completely understand why we need it.

* How can we specify a message schema (XSD or JSON schema) in Ready! API?

### Features
Exporter (currently only Importer is implemented)
