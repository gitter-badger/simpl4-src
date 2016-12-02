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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({ "unchecked", "deprecation" })
@UriEndpoint(scheme = "deepzoom", title = "DeepZoom", syntax = "deepzoom:pattern", consumerClass = DeepZoomConsumer.class)
public class DeepZoomEndpoint extends DefaultEndpoint {

	@UriParam
	private String pdfile;

	@UriParam
	private String outputdir;

	@UriParam
	private String vfsroot;

	@UriParam
	private String hotspotregex;

	@UriParam
	private double factor;

	public DeepZoomEndpoint(String uri, DeepZoomComponent component, String remaining) {
		super(uri, component);
	}

	@Override
	public DeepZoomComponent getComponent() {
		return (DeepZoomComponent) super.getComponent();
	}

	public Producer createProducer() throws Exception {
		return new DeepZoomProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		DeepZoomConsumer consumer = new DeepZoomConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getPdfFile() {
		return this.pdfile;
	}

	public void setPdfFile(String p) {
		this.pdfile = p;
	}

	public String getOutputDirectory() {
		return this.outputdir;
	}

	public void setOutputDirectory(String outputdir) {
		this.outputdir = outputdir;
	}

	public String getHotspotRegex() {
		return this.hotspotregex;
	}

	public void setHotspotRegex(String h) {
		this.hotspotregex = h;
	}

	public String getVfsRoot() {
		return this.vfsroot;
	}

	public void setVfsRoot(String v) {
		this.vfsroot = v;
	}

	public double getFactor() {
		return this.factor;
	}

	public void setFactor(String d) {
		try {
			this.factor = Double.parseDouble(d);
		} catch (Exception e) {
			this.factor = 1.0;
		}
	}
}

