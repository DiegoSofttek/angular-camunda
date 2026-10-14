# Complex Variables (Spin JSON/XML, Typed Values)

**Reading/Writing JSON with Spin:**
```java
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;

SpinJsonNode json = Spin.JSON(execution.getVariable("inputRequest"));
String name = json.prop("name").stringValue();
int amount = json.prop("amount").numberValue().intValue();

SpinJsonNode response = Spin.JSON("{}");
response.prop("status", "OK");
response.prop("total", 100);
execution.setVariable("outputResponse", response.toString());
```

**Reading XML with Spin:**
```java
import static org.camunda.spin.Spin.XML;

String timezone = XML(xmlString)
    .xPath("//*[local-name()='timezone']/text()")
    .string();
```

**Using Spin in BPMN expressions:**
```xml
${JSON(appointmentRequest).prop('ClubNumber').stringValue()}
```
