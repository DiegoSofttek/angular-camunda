# XSLT Transforms (XML Transformation in BPMN)

Used to build SOAP requests or transform XML data within a process.

```xml
<camunda:inputParameter name="soapRequest">
  ${xslt20('<empty/>').styleSheet(execution.getVariable('/META-INF/com/example/xsl/Request.xsl')).variables(xsltVariables).transform()}
</camunda:inputParameter>
```

**XSLT variables passed as a map:**
```xml
<camunda:inputParameter name="xsltVariables">
  <camunda:map>
    <camunda:entry key="orderId">${JSON(orderRequest).prop('orderId').stringValue()}</camunda:entry>
  </camunda:map>
</camunda:inputParameter>
```

Place `.xsl` files in `src/main/resources/META-INF/com/camunda/template/xsl/`.
