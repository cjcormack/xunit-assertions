package org.netkernelroc.xunit.endpoint;


import org.netkernel.layer0.nkf.INKFRequestContext;
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
    Document doc = context.source("arg:result", Document.class);
    String test = context.source("arg:test", String.class);
    String[] parts = test.split("/");

    int count = Integer.parseInt(parts[1].trim());
    String css = parts[0];

    DOMNodeSelector domNodeSelector = new DOMNodeSelector(doc);
    Set<Node> set = domNodeSelector.querySelectorAll(css);

    context.createResponseFrom(set.size()==count).setNoCache();
    }

  }
