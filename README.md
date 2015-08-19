# soapui-reprezen-plugin
RepreZen API Studio plugin for SoapUI

### Download & Install

Use the "Plugin Manager" button in Ready!API 1.3.1 to load the jar created by Maven build using the "Load plugin from file" button. 

Both importer and exporter are implemented. Note that the export action is available on the RestResource, not the project inself.
### Build it yourself

Clone the Git repository, make sure you have maven installed, and run

```
mvn clean install assembly:single
```
Note that Maven creates two jars, we need `soapui-reprezen-plugin/target/soapui-reprezen-plugin-1.0-dist.jar` as it contains dependencies.

### Questions
* RAML generator (and some other generators) defines an API Importer class which extends  `com.eviware.soapui.plugins.ApiImporter` and has `com.eviware.soapui.plugins.PluginApiImporter` annotation. I created a similar `RepreZenApiImporter` for RepreZen, but I don't completely understand why we need it.

(http://community.smartbear.com/t5/Ready-API-and-SoapUI-PlugIn/Purpose-of-PluginApiImporter-annotation/m-p/104035#U104035)

* How can we specify a message schema (XSD or JSON schema) in Ready! API?

(http://community.smartbear.com/t5/Ready-API-and-SoapUI-PlugIn/Message-Payload-Schema/m-p/104037#U104037)

Some remaining issues are marked with "TODO" in the exporter
