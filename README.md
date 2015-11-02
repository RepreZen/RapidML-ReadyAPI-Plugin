# RapidML-ReadyAPI-Plugin
RepreZen API Studio plugin for SoapUI:
* Allows you to import RepreZen files into SoapUI for testing your REST APIs
* Allows you to generate a RepreZen file for any REST API defined in SoapUI

### Download & Install

Use the "Plugin Manager" button in Ready!API 1.3.1 to load the jar created by Maven build using the "Load plugin from file" button. 

Both importer and exporter are implemented. Note that the export action is available on the RestResource, not the project itself.
### Build it yourself

Clone the Git repository, make sure you have maven installed, and run

```
mvn clean install assembly:single
```
Note that Maven creates two jars, we use `target/RapidML-ReadyAPI-Plugin-1.0-dist` as it contains dependencies.
