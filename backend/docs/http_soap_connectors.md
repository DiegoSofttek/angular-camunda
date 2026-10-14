# HTTP/SOAP Connectors (Call External Services from BPMN)

**HTTP Connector (REST call directly from BPMN):**
```xml
<bpmn:serviceTask id="Task_RestCall" name="Call REST API">
  <bpmn:extensionElements>
    <camunda:connector>
      <camunda:inputOutput>
        <camunda:inputParameter name="method">POST</camunda:inputParameter>
        <camunda:inputParameter name="url">https://api.example.com/data</camunda:inputParameter>
        <camunda:inputParameter name="headers">
          <camunda:map>
            <camunda:entry key="Content-Type">application/json</camunda:entry>
          </camunda:map>
        </camunda:inputParameter>
        <camunda:inputParameter name="payload">${requestBody}</camunda:inputParameter>
        <camunda:outputParameter name="responseBody">${response}</camunda:outputParameter>
      </camunda:inputOutput>
      <camunda:connectorId>http-connector</camunda:connectorId>
    </camunda:connector>
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

**SOAP Connector:**
```xml
<camunda:connectorId>soap-http-connector</camunda:connectorId>
<camunda:inputParameter name="url">${systemProperty('service/wsdl-url')}</camunda:inputParameter>
<camunda:inputParameter name="payload">${soapRequest}</camunda:inputParameter>
<camunda:outputParameter name="soapResponse">${response}</camunda:outputParameter>
```
