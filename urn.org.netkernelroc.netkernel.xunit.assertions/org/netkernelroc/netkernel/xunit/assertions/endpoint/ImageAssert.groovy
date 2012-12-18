package org.netkernelroc.netkernel.xunit.assertions.endpoint

import groovy.json.JsonSlurper
import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.layer0.nkf.INKFResponseReadOnly
import org.netkernel.layer0.representation.IHDSNode
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

import java.awt.*

import org.netkernel.image.rep.ImageAspect

class ImageAssert extends StandardAccessorImpl {

	private testMethods = [:]

	ImageAssert() {
		this.declareThreadSafe()

		def integerCheck = {name, node, image ->
			Integer.parseInt(node.value) == image."$name"
		}

		testMethods."height" = integerCheck.curry("height")
		testMethods."width" = integerCheck.curry("width")
		testMethods."color" = colorCheck
	}


	void onSource(INKFRequestContext context) throws Exception {
		INKFResponseReadOnly response = context.source("arg:result");
		IHDSNode assertions = context.source("arg:test", IHDSNode.class);

		ImageAspect imageAspect = null
		if(response.getRepresentation() instanceof ImageAspect) {
			imageAspect = response.representation
		} else {
			imageAspect = context.transrept(response.representation, ImageAspect.class)
		}

		boolean result = true

		assertions.getNodes("/assertions/*").each { node ->
			// If there is a valid test for this assertion (width, color, etc.) ...
			if(testMethods[node.getName()]) {
				result = testMethods[node.getName()](node, imageAspect.imageReadOnly)
			}
		}
		context.createResponseFrom(result).setNoCache();
	}

	def colorCheck = { node, image ->
		def colorpoint = new JsonSlurper().parseText(node.value)
		int color = image.getRGB(colorpoint.x, colorpoint.y)
		int testColor = new Color(colorpoint.red, colorpoint.green, colorpoint.blue, 255).getRGB()
		color == testColor
	}

}
