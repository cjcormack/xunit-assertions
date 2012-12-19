package org.netkernelroc.xunit.endpoint

import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

import java.awt.image.DataBuffer

import org.netkernel.image.rep.ImageAspect

class ImageEqualityAssert extends StandardAccessorImpl {

    ImageEqualityAssert() {
	this.declareThreadSafe()
    }

    void onSource(INKFRequestContext context) throws Exception {
	def resultImage  = context.source("arg:result");
	def testImage = context.source(context.source("arg:test"), ImageAspect.class)

	ImageAspect res = context.transrept(resultImage.representation, ImageAspect.class)

	boolean result = compare(res, testImage)

	context.createResponseFrom(result).setNoCache();
    }

    private boolean compare(ImageAspect image1, ImageAspect image2) {
	boolean result = true

	def i1 = image1.imageReadOnly.data
	def i2 = image2.imageReadOnly.data

	// Simple comparison of height and width before checking bytes
	if((i1.width != i2.width) || (i1.height != i2.height)) return false;

	// Ok, the sizes are the same, let's compare bytes
	DataBuffer image1Data = i1.dataBuffer
	DataBuffer image2Data = i2.dataBuffer

	(0..(image1Data.size - 1)).each { index ->
	    if(image1Data.getElem(index) != image2Data.getElem(index)) {
		return false
	    }
	}

	return result
    }
}
