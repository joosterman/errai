package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class StringMarshaller implements Marshaller<JSONValue, String> {
  @Override
  public Class<String> getTypeHandled() {
    return String.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public String demarshall(JSONValue o, MarshallingContext ctx) {
    return o.isString().stringValue();
  }

  @Override
  public String marshall(String o, MarshallingContext ctx) {
    return "\"" + o.replaceAll("\\\\", "\\\\\\\\").replaceAll("[\\\\]{0}\\\"", "\\\\\"")  + "\"";
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isString() != null;
  }
}