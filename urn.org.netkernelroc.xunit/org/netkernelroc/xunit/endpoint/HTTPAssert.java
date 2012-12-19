package org.netkernelroc.xunit.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.regex.Pattern;

public class HTTPAssert extends StandardAccessorImpl
  {

  public HTTPAssert()
    {
    declareThreadSafe();
    }

  public void onSource(INKFRequestContext context) throws Exception
    {
    IHDSNode responseHeaderNode;

    StringBuilder expected = new StringBuilder();
    StringBuilder received = new StringBuilder();

    Boolean result = Boolean.TRUE;

    INKFResponseReadOnly response = context.source("arg:response", INKFResponseReadOnly.class);

    IHDSNode responseHeaders = (IHDSNode) response.getHeader("HTTP_ACCESSOR_RESPONSE_HEADERS_METADATA");
    Integer httpReturnCode = (Integer) response.getHeader("HTTP_ACCESSOR_STATUS_CODE_METADATA");

    IHDSNode assertions = context.source("arg:test", IHDSNode.class);
    for (IHDSNode assertion : assertions.getNodes("/assertions/*"))
      {
      String test = assertion.getName();
      String value = (String) assertion.getValue();

      if ("returnCode".equals(test))
        {
        if (!Pattern.matches(value, httpReturnCode.toString()))
          {
          result = Boolean.FALSE;
          expected.append(" returnCode: " + value);
          received.append(" returnCode: " + httpReturnCode);
          }
        }
      else
        {
        // We have an HTTP header test
        responseHeaderNode = responseHeaders.getFirstNode("//" + test);
        if (responseHeaderNode == null)
          {
          // We want a header and it is not there. Test fails
          result = Boolean.FALSE;
          expected.append(" " + test);
          received.append(" " + test + " not found");
          }
        else
          {
          // We found a response header.
          if (value != null && !"".equals(value))
            {
            String headerResponseValue = (String) responseHeaderNode.getValue();
            if (!headerResponseValue.equals(value))
              {
              if (!Pattern.matches(value, headerResponseValue))
                {
                result = Boolean.FALSE;
                expected.append(" " + test + "=" + value);
                received.append(" " + test + "=" + headerResponseValue);
                }
              }
            }
          }
        }
      }
    context.sink("active:assert/Expected", expected.toString());
    context.sink("active:assert/Received", received.toString());

    context.createResponseFrom(result).setNoCache();
    }

  }
