// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1, build EA-R39)

package junk.webservices.client;

import com.sun.xml.rpc.encoding.DeserializationException;
import com.sun.xml.rpc.encoding.SOAPInstanceBuilder;
import com.sun.xml.rpc.util.exception.LocalizableExceptionAdapter;

public class GMT_WebServiceAPI_runGMT_Script_RequestStruct_SOAPBuilder implements SOAPInstanceBuilder {
    private junk.webservices.client.GMT_WebServiceAPI_runGMT_Script_RequestStruct _instance;
    private java.lang.String[] arrayOfString_1;
    private javax.activation.DataHandler[] arrayOfDataHandler_2;
    private static final int myARRAYOFSTRING_1_INDEX = 0;
    private static final int myARRAYOFDATAHANDLER_2_INDEX = 1;

    public GMT_WebServiceAPI_runGMT_Script_RequestStruct_SOAPBuilder() {
    }

    public void setArrayOfString_1(java.lang.String[] arrayOfString_1) {
        this.arrayOfString_1 = arrayOfString_1;
    }

    public void setArrayOfDataHandler_2(javax.activation.DataHandler[] arrayOfDataHandler_2) {
        this.arrayOfDataHandler_2 = arrayOfDataHandler_2;
    }

    public int memberGateType(int memberIndex) {
        switch (memberIndex) {
            case myARRAYOFSTRING_1_INDEX:
                return GATES_INITIALIZATION | REQUIRES_CREATION;
            case myARRAYOFDATAHANDLER_2_INDEX:
                return GATES_INITIALIZATION | REQUIRES_CREATION;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void construct() {
    }

    public void setMember(int index, Object memberValue) {
        try {
            switch(index) {
                case myARRAYOFSTRING_1_INDEX:
                    _instance.setArrayOfString_1((java.lang.String[])memberValue);
                    break;
                case myARRAYOFDATAHANDLER_2_INDEX:
                    _instance.setArrayOfDataHandler_2((javax.activation.DataHandler[])memberValue);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(e));
        }
    }

    public void initialize() {
    }

    public void setInstance(Object instance) {
        _instance = (junk.webservices.client.GMT_WebServiceAPI_runGMT_Script_RequestStruct)instance;
    }

    public Object getInstance() {
        return _instance;
    }
}
