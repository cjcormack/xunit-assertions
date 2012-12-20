package org.netkernelroc.xunit.endpoint;


import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

import java.util.Set;

/**
 *
 * @author Randolph Kahle
 *
 */

public class CSSAssert extends StandardAccessorImpl
  {

  public CSSAssert()
    {
    declareThreadSafe();
    }

  @Override
  public void onSource(INKFRequestContext context) throws Exception
    {
    StringBuilder expected = new StringBuilder();
    StringBuilder received = new StringBuilder();

    Boolean result = Boolean.TRUE;

    Document doc = context.source("arg:result", Document.class);

    IHDSNode assertions = context.source("arg:test", IHDSNode.class);

    for (IHDSNode assertion : assertions.getNodes("/assertions/*"))
      {
      String test = assertion.getName();
      String value = (String)assertion.getValue();

      String[] parts = value.split("/");

      int count = Integer.parseInt(parts[1].trim());
      String css = parts[0];

      DOMNodeSelector domNodeSelector = new DOMNodeSelector(doc);
      Set<Node> set = domNodeSelector.querySelectorAll(css);
      if (set.size()!=count)
        {
        result = Boolean.FALSE;
        expected.append(" " + css + " --> " + count);
        received.append(" " + css + " --> " + set.size());
        }
      }

    context.sink("active:assert/Expected", expected.toString());
    context.sink("active:assert/Received", received.toString());

    context.createResponseFrom(result).setNoCache();
    }

  }
