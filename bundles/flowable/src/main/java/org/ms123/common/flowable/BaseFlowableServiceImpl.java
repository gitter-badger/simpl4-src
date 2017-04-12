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
package org.ms123.common.flowable;

import flexjson.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.lang.reflect.*;
import org.osgi.framework.wiring.*;
import org.ms123.common.git.GitService;
import org.ms123.common.stencil.api.StencilService;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.store.StoreDesc;
import org.osgi.framework.BundleContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParsePosition;

/**
 *
 */
@SuppressWarnings("unchecked")
class BaseFlowableServiceImpl {

	protected BundleContext bc;

	protected DataLayer dataLayer;

	protected GitService gitService;

	protected JSONDeserializer ds = new JSONDeserializer();

	protected JSONSerializer js = new JSONSerializer();


}
