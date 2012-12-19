package org.netkernelroc.xunit.endpoint

import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.layer0.representation.IHDSNode
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

/**
 *
 *
 *
 */
public class HTTPAssert extends StandardAccessorImpl
{

  @Override public void onSource(INKFRequestContext context) throws Exception
  {
    def result = true
    def response = context.source("arg:result")
    def assertions = context.source("arg:test", IHDSNode.class)

    def responseHeaders = response.getHeader("HTTP_ACCESSOR_RESPONSE_HEADERS_METADATA")

    // Perform assertions for headers
    assertions.getNodes("/headers/*").each { node ->
      def headerNode = responseHeaders.getFirstNode("//${node.name}".toString())

      if (node.value != headerNode?.value)
      {
        println "Assertion Failed for header: ${node.name} expected: ${node.value} actual: ${headerNode?.value}"
        result = false
      }
    }

    context.createResponseFrom(result).setNoCache();
  }
}
