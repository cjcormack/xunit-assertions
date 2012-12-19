package org.netkernelroc.xunit.test.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

public class ReflectHTTP extends StandardAccessorImpl
  {

  public ReflectHTTP()
    {
    declareThreadSafe();
    }

  public void onSource(INKFRequestContext context) throws Exception
    {
    String header = context.getThisRequest().getArgumentValue("header");
    String value = context.getThisRequest().getArgumentValue("value");

    INKFResponse response = context.createResponseFrom("HTTP Reflection: [" + header + "], [" + value + "]");

    if ("returnCode".equals(header))
      {
      response.setHeader("httpResponse:/code", value);
      }


    response.setMimeType("text/plain");
    }

  }
