/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.camel.components.deepzoom;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.Message;
import org.ms123.common.camel.api.ExchangeUtils;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

@SuppressWarnings({ "unchecked", "deprecation" })
public class DeepZoomProducer extends DefaultAsyncProducer {

	public DeepZoomProducer(DeepZoomEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public DeepZoomEndpoint getEndpoint() {
		return (DeepZoomEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String input = getEndpoint().getInputpath();
		String outdir = getEndpoint().getOutputdir();
		info(this, "Input:" + input);
		info(this, "Outdir:" + outdir);
		input = ExchangeUtils.getParameter(input, exchange, String.class, "inputpath");
		outdir = ExchangeUtils.getParameter(outdir, exchange, String.class, "outputdir");
		info(this, "Input2:" + input);
		info(this, "Outdir2:" + outdir);
		DeepZoom dz = new DeepZoom();
		try {
			dz.processImageFile(new File(input), new File(outdir));
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("DeepZoomProducer",e);
		}
		return true;
	}

}

