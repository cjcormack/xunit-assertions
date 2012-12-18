package org.netkernelroc.netkernel.xunit.assertions.endpoint;


import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.regex.Pattern;


public class HTTPOldAssert extends StandardAccessorImpl
  {

  @Override
  public void onSource(INKFRequestContext context) throws Exception
    {
    boolean result;
    IHDSNode responseHeaders;
    IHDSNode responseHeaderNode;

    String headerName;
    String headerTestValue;
    Integer httpReturnCode;

    INKFResponseReadOnly response = context.source("arg:response", INKFResponseReadOnly.class);
    responseHeaders = (IHDSNode) response.getHeader("HTTP_ACCESSOR_RESPONSE_HEADERS_METADATA");
    httpReturnCode = (Integer) response.getHeader("HTTP_ACCESSOR_STATUS_CODE_METADATA");

    headerName = context.source("arg:headerName", String.class);
    headerTestValue = context.source("arg:headerTestValue", String.class);


    // Are we testing for the Return Code?
    if ("ReturnCode".equals(headerName))
      {
      result = Pattern.matches(headerTestValue, httpReturnCode.toString());
      }
    else
      {
      // We have an HTTP header test
      responseHeaderNode = responseHeaders.getFirstNode("//" + headerName);
      if (responseHeaderNode == null)
        {
        // We want a header and it is not there. Test fails
        result = false;
        }
      else
        {
        // We found a response header.
        if ("".equals(headerTestValue))
          {
          result = true;
          }
        else
          {
          String headerResponseValue = (String) responseHeaderNode.getValue();
          if (headerTestValue.equals(headerResponseValue))
            {
            result = true;
            }
          else
            {
            result = Pattern.matches(headerTestValue, headerResponseValue);
            }
          }
        }
      }

    context.createResponseFrom(result).setNoCache();
    }
  }
