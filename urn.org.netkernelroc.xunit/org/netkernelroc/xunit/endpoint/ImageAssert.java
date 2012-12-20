package org.netkernelroc.xunit.endpoint;

import org.netkernel.image.rep.ImageAspect;
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

public class ImageAssert extends StandardAccessorImpl
  {

  public ImageAssert()
    {
    declareThreadSafe();
    }

  public void onSource(INKFRequestContext context) throws Exception
    {
    StringBuilder expected = new StringBuilder();
    StringBuilder received = new StringBuilder();

    Boolean result = Boolean.TRUE;

    INKFResponseReadOnly response = context.source("arg:result", INKFResponseReadOnly.class);

    ImageAspect imageAspect = null;
    if (response.getRepresentation() instanceof ImageAspect)
      {
      imageAspect = (ImageAspect) response.getRepresentation();
      }
    else
      {
      imageAspect = context.transrept(response.getRepresentation(), ImageAspect.class);
      }
    Image image = imageAspect.getImageReadOnly();

    IHDSNode assertions = context.source("arg:test", IHDSNode.class);

    for (IHDSNode assertion : assertions.getNodes("/assertions/*"))
      {
      String test = assertion.getName();
      String value = (String) assertion.getValue();

      if ("height".equals(test))
        {
        if (convertToInt(value) != image.getHeight(null))
          {
          result = Boolean.FALSE;
          expected.append(" height = " + value);
          received.append(" height = " + image.getHeight(null));
          }
        }
      else if ("width".equals(test))
        {
        if (convertToInt(value) != image.getWidth(null))
          {
          result = Boolean.FALSE;
          expected.append(" width = " + value);
          received.append(" width = " + image.getWidth(null));
          }
        }
      else if ("color".equals(test))
        {
        INKFRequest request = context.createRequest("active:JSONToHDS");
        request.addArgumentByValue("operand", value);
        IHDSNode colorValue = (IHDSNode) context.issueRequest(request);
        if (!imageTestOK(colorValue, image))
          {
          result = Boolean.FALSE;
          int[] rgbColors = rgbColors(image, (Integer) colorValue.getFirstValue("//x"), (Integer) colorValue.getFirstValue("//y"));
          expected.append(" color @(" + colorValue.getFirstValue("//x") + "," + colorValue.getFirstValue("//y") + ")=[" + colorValue.getFirstValue("//red") + "," + colorValue.getFirstValue("//green") + "," + colorValue.getFirstValue("//blue") + "]");
          received.append(" color @(" + colorValue.getFirstValue("//x") + "," + colorValue.getFirstValue("//y") + ")=[" + rgbColors[0] + "," + rgbColors[1] + "," + rgbColors[2] + "]");
          }
        }
      else if ("equals".equals(test))
        {
        // The value is the URI for the image we are using as the test comparison
        ImageAspect testImage = context.source(value, ImageAspect.class);
        if (!imagesEqual(testImage, imageAspect))
          {
          result = Boolean.FALSE;
          expected.append(" equal   images");
          received.append(" unequal images");
          }
        }
      else
        {
        // TODO: How do I log this?
        }
      }

    context.sink("active:assert/Expected", expected.toString());
    context.sink("active:assert/Received", received.toString());

    context.createResponseFrom(result).setNoCache();
    }

  private int convertToInt(String value) throws Exception
    {
    return Integer.parseInt(value);
    }

  private boolean imageTestOK(IHDSNode colorValue, Image image)
    {
    int color = ((BufferedImage) image).getRGB((Integer) colorValue.getFirstValue("//x"), (Integer) colorValue.getFirstValue("//y"));
    int testColor = new Color((Integer) colorValue.getFirstValue("//red"), (Integer) colorValue.getFirstValue("//green"), (Integer) colorValue.getFirstValue("//blue"), 255).getRGB();

    return color == testColor;
    }

  private int[] rgbColors(Image image, int x, int y)
    {
    int rgb = ((BufferedImage) image).getRGB(x, y);
    int red = (rgb >> 16) & 0xff;   // red
    int green = (rgb >> 8) & 0xff;  // green
    int blue = rgb & 0xff;          // blue
    return new int[]{red, green, blue};
    }

  private boolean imagesEqual(ImageAspect image1, ImageAspect image2)
    {
    Raster raster1 = ((BufferedImage) image1.getImageReadOnly()).getData();
    Raster raster2 = ((BufferedImage) image2.getImageReadOnly()).getData();
    // Simple comparison of height and width before checking bytes
    if ((raster1.getWidth() != raster2.getWidth()) || (raster1.getHeight() != raster2.getHeight()))
      {
      return false;
      }

    DataBuffer image1Data = raster1.getDataBuffer();
    DataBuffer image2Data = raster2.getDataBuffer();

    for (int i = 0; i < image1Data.getSize(); i = i + 1)
      {
      if (image1Data.getElem(i) != image2Data.getElem(i))
        {
        return false;
        }
      }
    return true;
    }

  }
