/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.domainobjects;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PrimaryKey;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.ms123.common.entity.api.EntityService;
import static org.ms123.common.entity.api.Constants.LEFT_ENTITY;
import static org.ms123.common.entity.api.Constants.RIGHT_ENTITY;
import static org.ms123.common.entity.api.Constants.LEFT_FIELD;
import static org.ms123.common.entity.api.Constants.RIGHT_FIELD;
import static org.ms123.common.entity.api.Constants.RELATION;
import org.ms123.common.utils.annotations.RelatedTo;
import org.ms123.common.utils.annotations.Functional;
import org.ms123.common.libhelper.Inflector;
import org.osgi.framework.BundleContext;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.store.StoreDesc;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "kind=nucleus,name=classGen" })
public class ClassGenServiceImpl implements org.ms123.common.domainobjects.api.ClassGenService {

	protected Inflector m_inflector = Inflector.getInstance();

	private int TEXT_LEN = 128000;

	protected EntityService m_entityService;

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}
	public ClassLoader getClassLoader( StoreDesc sdesc){
		return null;
	}

	public List<String> generate(StoreDesc sdesc, List<Map> entities, String outDir) throws Exception {
		ClassPool cp = new ClassPool(true);
		info(this,"--->generate:" + sdesc.getString());
		List<String> classnameList = new ArrayList();
		for (int i = 0; i < entities.size(); i++) { //Make empty classes to resolve relations
			Map m = entities.get(i);
			String classname = getFQN(sdesc, m);
			info(this,"\tmakeClass:" + classname);
			cp.makeClass(classname);
		}
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String name = (String) entMap.get("name");
			String classname = getFQN(sdesc, entMap);
			List fields = getEntityMetaData(sdesc, name);
			makeClass(sdesc, cp, fields, classname, sdesc.getNamespace(), entMap, true);
			classnameList.add(classname);
			List<String> pkNameList = (List) entMap.get("primaryKeys");
			if (pkNameList != null && pkNameList.size() > 1) {
				String classnamePK = classname + "_PK";
				classnameList.add(classnamePK);
				new PrimaryKeyClassCreator().makePKClass(sdesc, cp, pkNameList, classnamePK, fields);
			}
		}
		List<Map> rels = getRelations(sdesc);
		for (int i = 0; rels != null && i < rels.size(); i++) {
			addRelations(cp, rels.get(i), sdesc);
		}
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String classname = getFQN(sdesc, entMap);
			boolean genDefFields = getBoolean(entMap.get("default_fields"), false);
			boolean genStateFields = getBoolean(entMap.get("state_fields"), false);
			boolean team_security = getBoolean(entMap.get("team_security"), false);
info(this,"genStateFields:"+genStateFields+"/"+genDefFields+"/"+team_security);
			if (genDefFields) {
				CtClass ctClass = cp.get(classname);
				List<Map> defaultFields = m_entityService.getDefaultFields();
				makeDefaultFields(cp, ctClass, sdesc, classname, defaultFields);
			} else if (team_security) {
				CtClass ctClass = cp.get(classname);
				List<Map> defaultFields = m_entityService.getTeamFields();
				makeDefaultFields(cp, ctClass, sdesc, classname, defaultFields);
			}
			if (genStateFields) {
				CtClass ctClass = cp.get(classname);
				List<Map> stateFields = m_entityService.getStateFields();
				makeDefaultFields(cp, ctClass, sdesc, classname, stateFields);
			}
		}
		File out = new File(outDir);
		if (!out.exists()) {
			out.mkdirs();
		}
		for (String classname : classnameList) {
			CtClass ctClass = cp.get(classname);
			ctClass.writeFile(outDir);
		}
		return classnameList;
	}

	protected void makeDefaultFields(ClassPool cp, CtClass ctClass, StoreDesc sdesc, String entity, List<Map> defaultFields) throws Exception {
		if (sdesc.isAidPack()) {
			return;
		}
		String pack = StoreDesc.getPackName(entity,sdesc.getPack());
		entity = StoreDesc.getSimpleEntityName(entity);
		String fqn = sdesc.getJavaPackage(pack) + "." + m_inflector.getClassName(entity);
		for (Map<String, String> field : defaultFields) {
			String datatype = field.get("datatype");
			if (datatype.startsWith("array")) {
				Map<String, Object> rel = new HashMap();
				rel.put(RELATION, "one-to-many");
				rel.put(LEFT_ENTITY, entity);
				rel.put(LEFT_FIELD, field.get("name"));
				rel.put(RIGHT_ENTITY, StoreDesc.PACK_AID + ".Team");
				rel.put("dependent", true);
				addRelations(cp, rel, sdesc);
			} else {
				makeField(sdesc, cp, ctClass, field.get("name"), null, datatype, null, null, null, fqn, null, true, true);
			}
		}
	}

	protected void addRelations(ClassPool cp, Map<String, Object> rel, StoreDesc sdesc) throws Exception {
		String app = sdesc.getNamespace();
		if (rel.get(LEFT_ENTITY) == null) {
			info(this,"addRelations.leftEntity is null");
			return;
		}
		String relation = (String) rel.get(RELATION);
		String leftEntity = sdesc.insertJavaPackage((String) rel.get(LEFT_ENTITY));
		String leftField = (String) rel.get(LEFT_FIELD);
		String foreignKeyField = (String) rel.get("foreignKeyField");
		String foreignKeyColumn = (String) rel.get("foreignKeyColumn");
		String rightEntity = sdesc.insertJavaPackage((String) rel.get(RIGHT_ENTITY));
		String rightField = (String) rel.get(RIGHT_FIELD);
		boolean dependent = getBoolean(rel.get("dependent"), false);
		info(this,"addRelations:" + leftEntity + "/" + rightEntity + "/" + dependent + "/" + relation);
		boolean manyToMany = "many-to-many".equals(relation);
		boolean oneToMany = "one-to-many".equals(relation) || "one-to-many-map".equals(relation);
		boolean isMap = "one-to-many-map".equals(relation);
		boolean oneToManyBi = "one-to-many-bi".equals(relation);
		boolean oneToOne = "one-to-one".equals(relation);
		boolean oneToOneBi = "one-to-one-bi".equals(relation);
		if (leftField == null || "".equals(leftField)) {
			if (manyToMany) {
				leftField = m_inflector.pluralize(rightEntity).toLowerCase();
			}
			if (oneToMany || oneToManyBi || oneToOne || oneToOneBi) {
				leftField = m_inflector.pluralize(rightEntity).toLowerCase();
			}
		}
		if (oneToOne || oneToOneBi) {
			leftField = m_inflector.singularize(leftField).toLowerCase();
		}
		if (rightField == null || "".equals(rightField)) {
			if (manyToMany) {
				rightField = m_inflector.pluralize(leftEntity).toLowerCase();
			}
			if (oneToMany || oneToOne) {
			}
			if (oneToManyBi || oneToOneBi) {
				rightField = m_inflector.singularize(leftEntity).toLowerCase();
			}
		}
		rightField = removePackageName(rightField);
		leftField = removePackageName(leftField);
		info(this,"rightField:" + rightField + "/" + leftField + "/" + oneToMany + "/" + oneToOneBi + "/" + manyToMany + "/" + oneToOne + "/" + oneToOneBi);
		CtClass ctClass = cp.get(leftEntity);
		info(this,"ctClass:" + ctClass);
		if (ctClass == null) {
			throw new RuntimeException("ClassGenService.addRelations:leftEntity(" + leftEntity + ") is null");
		}
		if (oneToMany || oneToManyBi || manyToMany) {
			info(this,"relation:" + relation + ",lm:" + leftEntity + "/" + leftField + ",rm:" + rightEntity + "/" + rightField);
			Class type = isMap ? Map.class : Set.class;
			CtField f = createField(ctClass, leftField, type.getName());
			if (oneToManyBi) {
				addAnnotationOne(f, "javax.jdo.annotations.Persistent", "mappedBy", rightField);
			}
			if (manyToMany) {
				addAnnotationOne(f, "javax.jdo.annotations.Join", "column", rightField.toLowerCase() + "_id");
				addAnnotationOne(f, "javax.jdo.annotations.Persistent", "table", getJoinTableName(leftEntity, leftField, rightEntity, rightField, isMap));
			}
			if (oneToManyBi || manyToMany) {
				createRightField(cp, leftEntity, rightEntity, leftField, rightField, manyToMany, foreignKeyColumn);
			}
			if (oneToMany) {
				if (foreignKeyField == null) {
					addAnnotationOne(f, "javax.jdo.annotations.Persistent", "table", getJoinTableName(leftEntity, leftField, null, null, isMap));
					addAnnotationOne(f, "javax.jdo.annotations.Join", "column", getLeftName(leftEntity, leftField));
				}
				if (isMap) {
					addAnnotationOne(f, "javax.jdo.annotations.Key", "a#types", "java.lang.String");
					addAnnotationOne(f, "javax.jdo.annotations.Value", "a#types", rightEntity);
				} else {
					if (foreignKeyField != null) {
						addAnnotationThree(f, "javax.jdo.annotations.Element", "a#types", rightEntity, "column", foreignKeyColumn, "dependent", dependent ? "true" : "false");
					} else {
						//Is column really needed?,@@@MS
						addAnnotationThree(f, "javax.jdo.annotations.Element", "a#types", rightEntity, "column", removePackageName(rightEntity).replace('.', '_').toLowerCase(), "dependent", dependent ? "true" : "false");
					}
				}
			} else if (manyToMany) {
				addAnnotationTwo(f, "javax.jdo.annotations.Element", "a#types", rightEntity, "column", (leftField + "_id").toLowerCase());
			} else {
				if (dependent) {
					addAnnotationTwo(f, "javax.jdo.annotations.Element", "a#types", rightEntity, "dependent", "true");
				} else {
					addAnnotationOne(f, "javax.jdo.annotations.Element", "a#types", rightEntity);
				}
			}
			addGetterSetter(ctClass, leftField, type.getName());
		} else if (oneToOne || oneToOneBi) {
			CtField f = createField(ctClass, leftField, rightEntity);
			if (dependent) {
				addAnnotationOne(f, "javax.jdo.annotations.Persistent", "dependent", "true");
			} else {
				addEmptyAnnotation(f, "javax.jdo.annotations.Persistent");
			}
			if (foreignKeyField != null) {
					addAnnotationOne(f, "javax.jdo.annotations.Column", "name", foreignKeyColumn);
			}
			addGetterSetter(ctClass, leftField, rightEntity);
			if (oneToOneBi) {
				createRightFieldOneToOneBi(cp, leftEntity, rightEntity, leftField, rightField, manyToMany,foreignKeyColumn);
			}
		}
	}

	protected void createRightField(ClassPool cp, String leftEntity, String rightEntity, String leftField, String rightField, boolean many, String column) throws Exception {
		CtClass ctClass = cp.get(rightEntity);
		info(this,"createRightField:" + rightEntity + "/" + ctClass);
		Class type = Set.class;
		if (!many) {
			if (ctClass == null) {
				info(this,"ClassGenService.createRightField:" + rightEntity + " not exists");
				throw new RuntimeException("ClassGenService.createRightField:rightEntity(" + rightEntity + ") is null");
			}
			CtField f = null;
			if ((f=ctClass.getField(rightField)) != null) {
				// RelatedTo
				if( column != null){
					addAnnotationOne(f, "javax.jdo.annotations.Column", "name", column);
				}
				return;
			}
			f = createField(ctClass, rightField, leftEntity);
			addEmptyAnnotation(f, "javax.jdo.annotations.Persistent");
			addGetterSetter(ctClass, m_inflector.singularize(rightField), leftEntity);
		} else {
			CtField f = createField(ctClass, rightField, type.getName());
			addGetterSetter(ctClass, rightField, type.getName());
			addAnnotationOne(f, "javax.jdo.annotations.Element", "a#types", leftEntity);
			addAnnotationOne(f, "javax.jdo.annotations.Persistent", "mappedBy", leftField);
		}
	}

	protected void createRightFieldOneToOneBi(ClassPool cp, String leftEntity, String rightEntity, String leftField, String rightField, boolean many, String column) throws Exception {
		CtClass ctClass = cp.get(rightEntity);
		ConstPool constPool = ctClass.getClassFile().getConstPool();
		CtField f = createField(ctClass, rightField, leftEntity);
		addGetterSetter(ctClass, rightField, leftEntity);
		addAnnotationOne(f, "javax.jdo.annotations.Persistent", "mappedBy", leftField);
		if( column != null){
			addAnnotationOne(f, "javax.jdo.annotations.Column", "name", column);
		}
	}

	private CtField createField(CtClass ctClass, String name, String typeName) throws Exception {
		return createField(ctClass, name, typeName, AccessFlag.PRIVATE);
	}

	private CtField createField(CtClass ctClass, String name, String typeName, int access) throws Exception {
		final CtClass typeClass = ctClass.getClassPool().get(typeName);
		CtField f = new CtField(typeClass, name, ctClass);
		f.setModifiers(access);
		if (typeName.equals("java.util.Set")) {
			ctClass.addField(f, CtField.Initializer.byExpr("new java.util.HashSet()"));
		} else {
			ctClass.addField(f);
		}
		return f;
	}

	private void addGetterSetter(CtClass ctClass, String name, String typeName) throws Exception {
		String setName = "set" + firstToUpper(name);
		String getName = "get" + firstToUpper(name);
		if (typeName.equals("[B")) {
			typeName = "byte[]";
		}
		String setBody = "public void " + setName + " (" + typeName + " " + name + "){this." + name + "=" + name + ";}";
		String getBody = "public " + typeName + " " + getName + "(){return " + name + ";}";
		//info(this,"setBody:"+setBody);
		//info(this,"getBody:"+getBody);
		CtMethod nameSetMethod = CtNewMethod.make(setBody, ctClass);
		ctClass.addMethod(nameSetMethod);
		CtMethod nameGetMethod = CtNewMethod.make(getBody, ctClass);
		ctClass.addMethod(nameGetMethod);
	}

	private void addGetNamespace(CtClass ctClass, String namespace) throws Exception {
		String body = "public String __getNamespace(){return \"" + namespace + "\";}";
		CtMethod nsMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(nsMethod);
	}

	private Class _getClass(String dt) {
		Class type = String.class;
		if (dt.equals("date")) {
			type = Date.class;
		} else if (dt.equals("boolean")) {
			type = Boolean.class;
		} else if (dt.equals("float")) {
			type = Float.class;
		} else if (dt.equals("double")) {
			type = Double.class;
		} else if (dt.equals("decimal")) {
			type = Double.class;
		} else if (dt.equals("integer")) {
			type = Integer.class;
		} else if (dt.equals("array/string")) {
			type = String.class;
		} else if (dt.equals("map_string_string")) {
			type = Map.class;
		} else if (dt.equals("map_string_integer")) {
			type = Map.class;
		} else if (dt.equals("map_integer_string")) {
			type = Map.class;
		} else if (dt.equals("list_string")) {
			type = List.class;
		} else if (dt.equals("list_integer")) {
			type = List.class;
		} else if (dt.equals("list_double")) {
			type = List.class;
		} else if (dt.equals("long")) {
			type = Long.class;
		} else if (dt.equals("text")) {
			type = String.class;
		} else if (dt.equals("binary")) {
			type = byte[].class;
		} else if (dt.equals("geopoint")) {
			type = String.class;
		} else if (dt.startsWith("related")) {
		} else if (dt.equals("number")) {
			type = Integer.class;
		}
		return type;
	}

	private boolean noDefaultFields(String entity) {
		if (entity.toLowerCase().equals("common.logmessage")) {
			return true;
		}
		return false;
	}

	private void addAnnotationOne(CtField ctField, String classname, String key, Object value) {
		AnnotationsAttribute attr = getAnnotationsAttribute(ctField);
		ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
		Annotation anno = new Annotation(classname, constPool);
		String type = getTypeFromKey(key);
		if (type != null)
			key = key.substring(2);
		anno.addMemberValue(key, createMemberValue(type, value, constPool));
		attr.addAnnotation(anno);
	}

	private String getTypeFromKey(String key) {
		if (key.startsWith("a#"))
			return "a";
		return null;
	}

	private void addAnnotationTwo(CtField ctField, String classname, String key1, Object value1, String key2, Object value2) {
		AnnotationsAttribute attr = getAnnotationsAttribute(ctField);
		ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
		Annotation anno = new Annotation(classname, constPool);
		String type = getTypeFromKey(key1);
		if (type != null) {
			key1 = key1.substring(2);
		}
		anno.addMemberValue(key1, createMemberValue(type, value1, constPool));
		type = getTypeFromKey(key2);
		if (type != null) {
			key2 = key2.substring(2);
		}
		anno.addMemberValue(key2, createMemberValue(type, value2, constPool));
		attr.addAnnotation(anno);
	}

	private void addAnnotationThree(CtField ctField, String classname, String key1, Object value1, String key2, Object value2, String key3, Object value3) {
		AnnotationsAttribute attr = getAnnotationsAttribute(ctField);
		ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
		Annotation anno = new Annotation(classname, constPool);
		String type = getTypeFromKey(key1);
		if (type != null) {
			key1 = key1.substring(2);
		}
		anno.addMemberValue(key1, createMemberValue(type, value1, constPool));

		type = getTypeFromKey(key2);
		if (type != null) {
			key2 = key2.substring(2);
		}
		anno.addMemberValue(key2, createMemberValue(type, value2, constPool));

		type = getTypeFromKey(key3);
		if (type != null) {
			key3 = key3.substring(2);
		}
		anno.addMemberValue(key3, createMemberValue(type, value3, constPool));

		attr.addAnnotation(anno);
	}

	private void addEmptyAnnotation(CtField ctField, String classname) {
		AnnotationsAttribute attr = getAnnotationsAttribute(ctField);
		ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
		Annotation anno = new Annotation(classname, constPool);
		attr.addAnnotation(anno);
	}

	private void addMapAnnotation(CtField f, String key, String val) {
		addAnnotationOne(f, "javax.jdo.annotations.Key", "a#types", key);
		addAnnotationOne(f, "javax.jdo.annotations.Value", "a#types", val);
		addEmptyAnnotation(f, "javax.jdo.annotations.Join");
	}

	private void addElementListAnnotation(AnnotationsAttribute attr, ConstPool constPool, String type) {
		Annotation anno = new Annotation("javax.jdo.annotations.Element", constPool);
		anno.addMemberValue("types", createMemberValue("a", type, constPool));
		attr.addAnnotation(anno);
		anno = new Annotation("javax.jdo.annotations.Join", constPool);
		attr.addAnnotation(anno);
	}

	//maybe as example
	private void addElementColumnsAnnotation(CtField ctField, String rightEntity, String colName) {
		AnnotationsAttribute attr = getAnnotationsAttribute(ctField);
		ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
		Annotation anno = new Annotation("javax.jdo.annotations.Element", constPool);
		anno.addMemberValue("types", createMemberValue("a", rightEntity, constPool));

		Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
		MemberValue mv = new StringMemberValue(colName, constPool);
		columnAnnotation.addMemberValue("name", mv);
		columnAnnotation.addMemberValue("nullable", new BooleanMemberValue(false, constPool));
		mv = new AnnotationMemberValue(columnAnnotation, constPool);
		anno.addMemberValue("columns", mv);
		attr.addAnnotation(anno);
	}

	private MemberValue createMemberValue(String type, Object value, ConstPool constPool) {
		if (type != null) {
			if ("a".equals(type)) {
				MemberValue[] aarray = new ClassMemberValue[1];
				aarray[0] = new ClassMemberValue((String) value, constPool);
				ArrayMemberValue a = new ArrayMemberValue(constPool);
				a.setValue(aarray);
				return a;
			}
		}
		if (value instanceof String) {
			return new StringMemberValue((String) value, constPool);
		}
		if (value instanceof Integer) {
			return new IntegerMemberValue(constPool, (Integer) value);
		}
		return null;
	}

	private AnnotationsAttribute getAnnotationsAttribute(CtField ctField) {
		AnnotationsAttribute attr = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
		if (attr == null) {
			ConstPool constPool = ctField.getDeclaringClass().getClassFile().getConstPool();
			attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
			ctField.getFieldInfo().addAttribute(attr);
		}
		return attr;
	}

	private List<Map> getPrimaryKeys(List<Map<String, String>> fields) {
		List<Map> pkList = new ArrayList<Map>();
		String idField = "id";
		String idColumn = null;
		Class idClass = String.class;
		Object idConstraint = null;
		if (fields != null) {
			Iterator it = fields.iterator();
			while (it.hasNext()) {
				Map m = (Map) it.next();
				boolean hasPrimaryKey = getBoolean(m.get("primary_key"), false);
				if (hasPrimaryKey) {
					info(this,"primary_key:" + m.get("name"));
					idField = (String) m.get("name");
					idColumn = (String) m.get("columnName");
					if( "number".equals(m.get("datatype"))){
						idClass = Long.class;
					}else if( "decimal".equals(m.get("datatype"))){
						idClass = Double.class;
					}else if( "date".equals(m.get("datatype"))){
						idClass = Date.class;
					}else{
						idClass = String.class;
					}
					idConstraint = m.get("constraints");
					Map pkMap = new HashMap();
					pkMap.put("idClass", idClass);
					pkMap.put("idField", idField);
					pkMap.put("idColumn", idColumn);
					pkMap.put("idConstraint", idConstraint);
					pkList.add(pkMap);
				}
			}
		}
		if (pkList.size() == 0) {
			Map pkMap = new HashMap();
			pkMap.put("generated", "true");
			pkMap.put("idClass", idClass);
			pkMap.put("idField", idField);
			pkMap.put("idConstraint", idConstraint);
			pkList.add(pkMap);
		}
		return pkList;
	}

	private Map<String, Object> getPkMap(List<Map> pkLIst, String idField) {
		for (Map<String, Object> m : pkLIst) {
			String _idField = (String) m.get("idField");
			if (_idField.equals(idField)) {
				return m;
			}
		}
		return null;
	}

	private Map<String, Object> getField(List<Map> fields, String pk) {
		for (Map<String, Object> m : fields) {
			String name = (String) m.get("name");
			if (name.equals(pk)) {
				return m;
			}
		}
		return null;
	}

	private List sortFields(List<Map> fields, List<String> primaryKeys) {
		if( primaryKeys == null){
			return  fields;
		}
		info(this,"fields1:" + fields);
		List result = new ArrayList();
		for (String pk : primaryKeys) {
			Map field = getField(fields, pk);
			result.add(field);
		}
		for (Map field : fields) {
			if (!result.contains(field)) {
				result.add(field);
			}
		}
		info(this,"fields2:" + result);
		return result;
	}

	protected void makeClass(StoreDesc sdesc, ClassPool cp, List fields, String classname, String namespace, Map<String, Object> entMap, boolean withAnnotation) throws Exception {
		CtClass ctClass = cp.get(classname);
		info(this,"makeClass:" + ctClass + "/" + classname);
		ConstPool constPool = ctClass.getClassFile().getConstPool();
		ctClass.addInterface(cp.makeClass("java.io.Serializable"));
		AnnotationsAttribute classAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		ctClass.getClassFile().addAttribute(classAttr);

		fields = sortFields(fields, (List) entMap.get("primaryKeys"));
		List<Map> pkList = getPrimaryKeys(fields);
		
		boolean pkGenerated = pkList.size()==1 && pkList.get(0).get("generated")!=null;

		Annotation annotation = new Annotation("javax.jdo.annotations.PersistenceCapable", constPool);
		if( pkGenerated == false ){
			if (pkList.size() == 1) {
				EnumMemberValue mv = new EnumMemberValue(constPool);
				mv.setType("javax.jdo.annotations.IdentityType");
				mv.setValue(javax.jdo.annotations.IdentityType.APPLICATION.toString());
				annotation.addMemberValue("identityType", mv);
			} else {
				String pkClass = classname + "_PK";
				annotation.addMemberValue("objectIdClass", new ClassMemberValue(pkClass, constPool));
				EnumMemberValue mv = new EnumMemberValue(constPool);
				mv.setType("javax.jdo.annotations.IdentityType");
				mv.setValue(javax.jdo.annotations.IdentityType.APPLICATION.toString());
				annotation.addMemberValue("identityType", mv);
			}
		}

		String tableName = (String) entMap.get("tableName");
		String schemaName = (String) entMap.get("schemaName");
		if (tableName != null) {
			annotation.addMemberValue("table", new StringMemberValue(tableName, constPool));
		}
		if (schemaName != null) {
			annotation.addMemberValue("schema", new StringMemberValue(schemaName, constPool));
		}

		classAttr.addAnnotation(annotation);
		Map<String, Object> pkMap = pkList.get(0);
		Object idConstraint = pkMap.get("idConstraint");
		String idField = (String) pkMap.get("idField");
		Class idClass = (Class) pkMap.get("idClass");
		if (pkList.size() == 1) {
			CtField f = createField(ctClass, idField, idClass.getName());
			AnnotationsAttribute idAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
			f.getFieldInfo().addAttribute(idAttr);
			annotation = new Annotation("javax.jdo.annotations.Persistent", constPool);
			if( pkGenerated){
				EnumMemberValue emv = new EnumMemberValue(constPool);
				emv.setType("javax.jdo.annotations.IdGeneratorStrategy");
				emv.setValue(javax.jdo.annotations.IdGeneratorStrategy.UUIDHEX.toString());
				annotation.addMemberValue("valueStrategy", emv);
			}
			idAttr.addAnnotation(annotation);
			annotation = new Annotation("javax.jdo.annotations.PrimaryKey", constPool);
			if (pkMap.get("idColumn") != null) {
				Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
				columnAnnotation.addMemberValue("name", new StringMemberValue((String) pkMap.get("idColumn"), constPool));
				idAttr.addAnnotation(columnAnnotation);
			}
			idAttr.addAnnotation(annotation);
			if (idConstraint != null) {
				if (idConstraint instanceof String) {
					idConstraint = JSONArray.fromObject((String) idConstraint);
				}
				generateConstraints(f, (List) idConstraint);
			}
			addGetterSetter(ctClass, idField, idClass.getName());
		}
		if (fields == null) {
			return;
		}
		Iterator it = fields.iterator();
		while (it.hasNext()) {
			Map m = (Map) it.next();
			String[] val = new String[2];
			if (m.get("enabled") != null && ((Boolean) m.get("enabled") == false)) {
				info(this,"ClassGenService.makeClass(" + classname + "," + m.get("name") + "):disabled");
				continue;
			}
			val[0] = (String) (m.get("name"));
			val[1] = (String) m.get("datatype");
			String edittype = (String) m.get("edittype");
			String defaultValue = (m.get("default_value") != null) ? (m.get("default_value") + "") : null;
			if (pkList.size() == 1 && val[0].equals(idField)) {
				continue;
			}
			if (val[1].startsWith("object") || val[1].startsWith("related")) {
				int first = val[1].indexOf("/");
				int last = val[1].lastIndexOf("/");
				String datatype = null;
				String relatedToField = null;
				if (first == last) {
					datatype = val[1].substring(first + 1);
				} else {
					datatype = val[1].substring(first + 1, last);
					relatedToField = val[1].substring(last + 1);
				}
				info(this,"\tdatatype2:" + datatype);
				info(this,"\tdatatype2.relatedToField:" + relatedToField);
				String fieldName = relatedToField != null ? relatedToField : val[0];
				CtField f = createField(ctClass, fieldName, datatype);
				AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
				f.getFieldInfo().addAttribute(fieldAttr);
				if (val[1].startsWith("related")) {
					annotation = new Annotation("org.ms123.common.utils.annotations.RelatedTo", constPool);
					if (relatedToField != null) {
						annotation.addMemberValue("value", new StringMemberValue(relatedToField, constPool));
					}
					fieldAttr.addAnnotation(annotation);
					annotation = new Annotation("javax.jdo.annotations.Persistent", constPool);
					fieldAttr.addAnnotation(annotation);
					annotation = new Annotation("flexjson.JSON", constPool);
					annotation.addMemberValue("include", new BooleanMemberValue(false, constPool));
					fieldAttr.addAnnotation(annotation);
				}
				addGetterSetter(ctClass, fieldName, datatype);
			} else {
				if ("decimalnumber".equals(val[0])) {
					info(this,"m:" + m);
				}
				boolean withIndex = getBoolean(m.get("index"), true);
				AnnotationsAttribute fieldAttr = makeField(sdesc, cp, ctClass, val[0], (String) m.get("columnName"), val[1], edittype, (String) m.get("sqltype"), defaultValue, classname, m.get("constraints"), withAnnotation, withIndex);
				Map _pkMap = getPkMap(pkList, val[0]);
				if (_pkMap != null) {
					annotation = new Annotation("javax.jdo.annotations.PrimaryKey", constPool);
					fieldAttr.addAnnotation(annotation);
				}
			}
		}
		addGetNamespace(ctClass, namespace);
	}

	private AnnotationsAttribute makeField(StoreDesc sdesc, ClassPool cp, CtClass ctClass, String name, String columnName, String datatype, String edittype, String sqltype, String defaultValue, String classname, Object co, boolean withAnnotation, boolean withIndex) throws Exception {
		name = firstToLower(name);
		ConstPool constPool = ctClass.getClassFile().getConstPool();
		Class type = _getClass(datatype);
		CtField f = createField(ctClass, name, type.getName());
		AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		f.getFieldInfo().addAttribute(fieldAttr);
		if (withAnnotation) {
			boolean isH2_FT = StoreDesc.VENDOR_H2.equals(sdesc.getVendor()) && "fulltext".equals(datatype);

			boolean isGraphical = (edittype != null && edittype.startsWith("graphical"));
			boolean isFunctional = (edittype != null && edittype.equals("functional"));
			if ("textarea".equals(edittype) || isH2_FT || isGraphical || datatype.equals("text") || datatype.equals("array/string")) {
				int len = isGraphical ? 128000 : TEXT_LEN;
				Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
				MemberValue mv = new StringMemberValue("VARCHAR", constPool);
				columnAnnotation.addMemberValue("jdbcType", mv);

				if (columnName != null) {
					columnAnnotation.addMemberValue("name", new StringMemberValue(columnName, constPool));
				}
				if (sqltype != null) {
					columnAnnotation.addMemberValue("sqltype", new StringMemberValue(sqltype, constPool));
				}

				mv = new IntegerMemberValue(constPool, len);
				columnAnnotation.addMemberValue("length", mv);
				if (defaultValue != null) {
					mv = new StringMemberValue(defaultValue, constPool);
					columnAnnotation.addMemberValue("defaultValue", mv);
				}
				fieldAttr.addAnnotation(columnAnnotation);
			} else if (isFunctional) {
				addEmptyAnnotation(f, "org.ms123.common.utils.annotations.Functional");
				Annotation ann = new Annotation("javax.jdo.annotations.NotPersistent", constPool);
				ann.addMemberValue("defaultFetchGroup", new StringMemberValue("false", constPool));
				fieldAttr.addAnnotation(ann);
			} else if ("map_string_integer".equals(datatype)) {
				addMapAnnotation(f, "java.lang.String", "java.lang.Integer");
			} else if ("map_integer_string".equals(datatype)) {
				addMapAnnotation(f, "java.lang.Integer", "java.lang.String");
			} else if ("map_string_string".equals(datatype)) {
				addMapAnnotation(f, "java.lang.String", "java.lang.String");
			} else if ("list_string".equals(datatype)) {
				addElementListAnnotation(fieldAttr, constPool, "java.lang.String");
			} else if ("list_integer".equals(datatype)) {
				addElementListAnnotation(fieldAttr, constPool, "java.lang.Integer");
			} else if ("list_double".equals(datatype)) {
				addElementListAnnotation(fieldAttr, constPool, "java.lang.Double");
			} else if ("ltree".equals(datatype)) {
				MemberValue[] aarray = new AnnotationMemberValue[2];
				Annotation ann = new Annotation("javax.jdo.annotations.Extension", constPool);
				ann.addMemberValue("vendorName", new StringMemberValue("datanucleus", constPool));
				ann.addMemberValue("key", new StringMemberValue("update-function", constPool));
				ann.addMemberValue("value", new StringMemberValue("text2ltree(?)", constPool));
				aarray[0] = new AnnotationMemberValue(ann, constPool);
				ann = new Annotation("javax.jdo.annotations.Extension", constPool);
				ann.addMemberValue("vendorName", new StringMemberValue("datanucleus", constPool));
				ann.addMemberValue("key", new StringMemberValue("insert-function", constPool));
				ann.addMemberValue("value", new StringMemberValue("text2ltree(?)", constPool));
				aarray[1] = new AnnotationMemberValue(ann, constPool);
				ann = new Annotation("javax.jdo.annotations.Extensions", constPool);
				ArrayMemberValue a = new ArrayMemberValue(constPool);
				a.setValue(aarray);
				ann.addMemberValue("value", a);
				fieldAttr.addAnnotation(ann);
				Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
				columnAnnotation.addMemberValue("jdbcType", new StringMemberValue("LTREE", constPool));
				fieldAttr.addAnnotation(columnAnnotation);
			} else if ("fulltext".equals(datatype)) {
				MemberValue[] aarray = new AnnotationMemberValue[2];
				Annotation ann = new Annotation("javax.jdo.annotations.Extension", constPool);
				ann.addMemberValue("vendorName", new StringMemberValue("datanucleus", constPool));
				ann.addMemberValue("key", new StringMemberValue("update-function", constPool));
				ann.addMemberValue("value", new StringMemberValue("to_tsvector('simple', ?)", constPool));
				aarray[0] = new AnnotationMemberValue(ann, constPool);
				ann = new Annotation("javax.jdo.annotations.Extension", constPool);
				ann.addMemberValue("vendorName", new StringMemberValue("datanucleus", constPool));
				ann.addMemberValue("key", new StringMemberValue("insert-function", constPool));
				ann.addMemberValue("value", new StringMemberValue("to_tsvector('simple', ?)", constPool));
				aarray[1] = new AnnotationMemberValue(ann, constPool);
				ann = new Annotation("javax.jdo.annotations.Extensions", constPool);
				ArrayMemberValue a = new ArrayMemberValue(constPool);
				a.setValue(aarray);
				ann.addMemberValue("value", a);
				fieldAttr.addAnnotation(ann);
				Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
				columnAnnotation.addMemberValue("jdbcType", new StringMemberValue("TSVECTOR", constPool));
				fieldAttr.addAnnotation(columnAnnotation);
				ann = new Annotation("javax.jdo.annotations.Persistent", constPool);
				ann.addMemberValue("defaultFetchGroup", new StringMemberValue("false", constPool));
				fieldAttr.addAnnotation(ann);
			} else {
				boolean used = false;
				Annotation columnAnnotation = new Annotation("javax.jdo.annotations.Column", constPool);
				if (defaultValue != null) {
					columnAnnotation.addMemberValue("defaultValue", new StringMemberValue(defaultValue, constPool));
					used = true;
				}
				if (columnName != null) {
					columnAnnotation.addMemberValue("name", new StringMemberValue(columnName, constPool));
					used = true;
				}
				if (sqltype != null) {
					columnAnnotation.addMemberValue("sqltype", new StringMemberValue(sqltype, constPool));
					used = true;
				}
				if (used) {
					fieldAttr.addAnnotation(columnAnnotation);
				}
			}
			if ("auto".equals(edittype) && type.equals(Integer.class)) {
				Annotation ann = new Annotation("javax.jdo.annotations.Persistent", constPool);
				ann.addMemberValue("customValueStrategy", new StringMemberValue("increment", constPool));
				fieldAttr.addAnnotation(ann);
			} else {
				if (co != null) {
					if (co instanceof String) {
						co = JSONArray.fromObject((String) co);
					}
					generateConstraints(f, (List) co);
				}
			}
			if (!datatype.equals("binary") && !datatype.equals("fulltext") && !isGraphical && !datatype.startsWith("map_") && !datatype.startsWith("list_")) {
				if (withIndex) {
					Annotation ann = new Annotation("javax.jdo.annotations.Index", constPool);
					ann.addMemberValue("name", new StringMemberValue("index_" + classname.replaceAll("\\.", "_").toLowerCase() + "_" + name, constPool));
					fieldAttr.addAnnotation(ann);
				}
			}
		}
		addGetterSetter(ctClass, name, type.getName());
		return fieldAttr;
	}

	protected void generateConstraints(CtField f, List<Map> constraints) {
		info(this,"generateConstraints:" + f + "/" + constraints);
		if (constraints == null) {
			return;
		}
		AnnotationsAttribute attr = getAnnotationsAttribute(f);
		ConstPool constPool = f.getDeclaringClass().getClassFile().getConstPool();

		Map<String, Object[]> cMap = initConstraints();
		Map<String, Boolean> processed = new HashMap<String, Boolean>();
		while (true) {
			List<Map> cl = getNextConstraint(constraints, processed);
			if (cl.size() == 0) {
				break;
			}
			info(this,"cl:" + cl);
			if (cl.size() == 1) {
				Map<String, Object> c = cl.get(0);
				String a = (String) c.get("annotation");
				Object[] params = cMap.get(a);
				Object p1 = (c.get("parameter1") instanceof JSONNull) ? null : c.get("parameter1");
				Object p2 = (c.get("parameter2") instanceof JSONNull) ? null : c.get("parameter2");
				String msg = (c.get("message") instanceof JSONNull) ? null : (String) c.get("message");
				if (params == null) {
					return;
				}
				Map<String, Object> pmap = parseParameter(p1, p2, msg, params);
				Class annoClass = (Class) params[0];
				Annotation anno = new Annotation(annoClass.getName(), constPool);
				for (String key : pmap.keySet()) {
					if (pmap.get(key) instanceof Integer) {
						anno.addMemberValue(key, new IntegerMemberValue(constPool, (Integer) pmap.get(key)));
					}
					if (pmap.get(key) instanceof Long) {
						anno.addMemberValue(key, new LongMemberValue((Long) pmap.get(key), constPool));
					}
					if (pmap.get(key) instanceof Double) {
						anno.addMemberValue(key, new DoubleMemberValue((Double) pmap.get(key), constPool));
					}
					if (pmap.get(key) instanceof String) {
						anno.addMemberValue(key, new StringMemberValue((String) pmap.get(key), constPool));
					}
					if (pmap.get(key) instanceof Pattern.Flag) {
						EnumMemberValue e = new EnumMemberValue(constPool);
						e.setType("javax.validation.constraints.Pattern.Flag");
						e.setValue((String) pmap.get(key));
						anno.addMemberValue(key, e);
					}
				}
				attr.addAnnotation(anno);
			} else {
				String a = (String) cl.get(0).get("annotation");
				Object[] params = cMap.get(a);
				Object[] paramsList = cMap.get(a + ".List");
				Annotation xanno = new Annotation(((Class) paramsList[0]).getName(), constPool);

				List<Annotation> aList = new ArrayList();
				for (Map c : cl) {
					Object p1 = (c.get("parameter1") instanceof JSONNull) ? null : c.get("parameter1");
					Object p2 = (c.get("parameter2") instanceof JSONNull) ? null : c.get("parameter2");
					String msg = (c.get("message") instanceof JSONNull) ? null : (String) c.get("message");
					Map<String, Object> pmap = parseParameter(p1, p2, msg, params);
					Annotation anno = new Annotation(((Class) params[0]).getName(), constPool);
					aList.add(anno);
					for (String key : pmap.keySet()) {
						if (pmap.get(key) instanceof Integer) {
							anno.addMemberValue(key, new IntegerMemberValue(constPool, (Integer) pmap.get(key)));
						}
						if (pmap.get(key) instanceof Double) {
							anno.addMemberValue(key, new DoubleMemberValue((Double) pmap.get(key), constPool));
						}
						if (pmap.get(key) instanceof String) {
							anno.addMemberValue(key, new StringMemberValue((String) pmap.get(key), constPool));
						}
						if (pmap.get(key) instanceof Pattern.Flag) {
							EnumMemberValue e = new EnumMemberValue(constPool);
							e.setType("javax.validation.constraints.Pattern.Flag");
							e.setValue((String) pmap.get(key));
							anno.addMemberValue(key, e);
						}
					}
				}
				MemberValue[] aarray = new AnnotationMemberValue[aList.size()];
				int i = 0;
				for (Annotation ae : aList) {
					aarray[i++] = new AnnotationMemberValue(ae, constPool);
				}
				ArrayMemberValue amv = new ArrayMemberValue(constPool);
				amv.setValue(aarray);
				xanno.addMemberValue("value", amv);
				attr.addAnnotation(xanno);

			}
		}
	}

	protected List<Map> getNextConstraint(List<Map> constraints, Map<String, Boolean> processed) {
		List<Map> ret = new ArrayList();
		for (Map<String, String> constraint : constraints) {
			String annotation = constraint.get("annotation");
			if (processed.get(annotation) != null) {
				continue;
			} else {
				for (Map<String, String> c : constraints) {
					String a = c.get("annotation");
					if (a.equals(annotation)) {
						ret.add(c);
					}
				}
				processed.put(annotation, true);
				return ret;
			}
		}
		return ret;
	}

	protected Map<String, Object> parseParameter(Object p1, Object p2, String msg, Object[] params) {
		Map<String, Object> paramMap = new HashMap();
		try {
			Object[] values = { p1, p2 };
			for (int i = 0; i < params.length; i++) {
				Object val = "";
				if (values != null && i < values.length && values[i] != null) {
					val = values[i];
				}
				String param = "";
				if (i < (params.length - 1)) {
					param = (String) params[i + 1];
				}
				int ind = param.indexOf(":");
				if (ind != -1) {
					String suffix = param.substring(ind + 1);
					Object value = getValue(suffix, val);
					if (value != null) {
						param = param.substring(0, ind);
						paramMap.put(param, value);
					}
				} else {
					if (param != null && param.length() > 0) {
						paramMap.put(param, val);
					}
				}
			}
			if (msg != null && msg.trim().length() > 0) {
				paramMap.put("message", msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paramMap;
	}

	protected Object getValue(String s, Object value) {
		boolean optional = s.indexOf("o") != -1;
		if (s.indexOf("d") != -1) {
			return getDecimal(value);
		}
		if (s.indexOf("i") != -1) {
			return getInteger(value);
		}
		if (s.indexOf("l") != -1) {
			return getLong(value);
		}
		if (s.indexOf("f") != -1) {
			return getFlags((String) value);
		}
		if ("".equals(value) && optional) {
			return null;
		}
		return value;
	}

	protected Double getDecimal(Object value) {
		try {
			if (value instanceof String) {
				return Double.parseDouble((String) value);
			}
			if (value instanceof Double) {
				return (Double) value;
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected Integer getInteger(Object value) {
		try {
			if (value instanceof Integer) {
				return (Integer) value;
			}
			if (value instanceof String) {
				return Integer.parseInt((String) value);
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected Long getLong(Object value) {
		try {
			if (value instanceof Long) {
				return (Long) value;
			}
			if (value instanceof String) {
				return Long.parseLong((String) value);
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected Pattern.Flag getFlags(String s) {
		Pattern.Flag pf = null;
		if (s.indexOf("i") != -1) {
			pf = Flag.CASE_INSENSITIVE;
		} else if (s.indexOf("d") != -1) {
			pf = Flag.DOTALL;
		}
		return pf;
	}

	public Map<String, Object[]> initConstraints() {
		Map<String, Object[]> mMap = new HashMap<String, Object[]>();
		mMap.put("AssertFalse", new Object[] { AssertFalse.class });
		mMap.put("AssertFalse.List", new Object[] { AssertFalse.List.class });
		mMap.put("AssertTrue", new Object[] { AssertTrue.class });
		mMap.put("AssertTrue.List", new Object[] { AssertTrue.List.class });
		mMap.put("CreditCardNumber", new Object[] { CreditCardNumber.class });
		mMap.put("CreditCardNumber.List", new Object[] { CreditCardNumber.List.class });
		mMap.put("DecimalMax", new Object[] { DecimalMax.class, "value" });
		mMap.put("DecimalMax.List", new Object[] { DecimalMax.List.class });
		mMap.put("DecimalMin", new Object[] { DecimalMin.class, "value" });
		mMap.put("DecimalMin.List", new Object[] { DecimalMin.List.class });
		mMap.put("Digits", new Object[] { Digits.class, "integer:i", "fraction:i" });
		mMap.put("Digits.List", new Object[] { Digits.List.class });
		mMap.put("Email", new Object[] { Email.class });
		mMap.put("Email.List", new Object[] { Email.List.class });
		mMap.put("Future", new Object[] { Future.class });
		mMap.put("Future.List", new Object[] { Future.List.class });
		mMap.put("Length", new Object[] { Length.class, "min:io", "max:io" });
		mMap.put("Length.List", new Object[] { Length.List.class });
		mMap.put("Max", new Object[] { Max.class, "value:l" });
		mMap.put("Max.List", new Object[] { Max.List.class });
		mMap.put("Min", new Object[] { Min.class, "value:l" });
		mMap.put("Min.List", new Object[] { Min.List.class });
		mMap.put("NotBlank", new Object[] { NotBlank.class });
		mMap.put("NotBlank.List", new Object[] { NotBlank.List.class });
		mMap.put("NotEmpty", new Object[] { NotEmpty.class });
		mMap.put("NotEmpty.List", new Object[] { NotEmpty.List.class });
		mMap.put("NotNull", new Object[] { NotNull.class });
		mMap.put("NotNull.List", new Object[] { NotNull.List.class });
		mMap.put("Null", new Object[] { Null.class });
		mMap.put("Null.List", new Object[] { Null.List.class });
		mMap.put("Past", new Object[] { Past.class });
		mMap.put("Past.List", new Object[] { Past.List.class });
		mMap.put("Pattern", new Object[] { Pattern.class, "regexp", "flags:fo" });
		mMap.put("Pattern.List", new Object[] { Pattern.List.class });
		mMap.put("Range", new Object[] { Range.class, "min:lo", "max:lo" });
		mMap.put("Range.List", new Object[] { Range.List.class });
		// mMap.put("ScriptAssert",				new Object[] { ScriptAssert.class,		"script","lang","alias:o"});
		// mMap.put("ScriptAssert.List",		new Object[] { ScriptAssert.List.class});
		mMap.put("Size", new Object[] { Size.class, "min:io", "max:io" });
		mMap.put("Size.List", new Object[] { Size.List.class });
		mMap.put("URL", new Object[] { URL.class });
		mMap.put("URL.List", new Object[] { URL.List.class });
		return mMap;
	}

	private String firstToUpper(String s) {
		String fc = s.substring(0, 1);
		return fc.toUpperCase() + s.substring(1);
	}

	private String firstToLower(String s) {
		String fc = s.substring(0, 1);
		return fc.toLowerCase() + s.substring(1);
	}

	protected List getEntityMetaData(StoreDesc sdesc, String entity) throws Exception {
		List list = m_entityService.getFields(sdesc, entity, false);
		return list;
	}

	private String removePackageName(String s) {
		if (s == null) {
			return s;
		}
		int dot = s.lastIndexOf(".");
		if (dot == -1) {
			return s;
		}
		return s.substring(dot + 1);
	}

	protected List getRelations(StoreDesc sdesc) throws Exception {
		List list = m_entityService.getRelations(sdesc);
		info(this,"getRelations:" + list);
		return list;
	}

	private String getFQN(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		name = StoreDesc.getSimpleEntityName(name);
		String className = m_inflector.getClassName((String) name);
		return sdesc.getJavaPackage(pack) + "." + className;
	}

	private String getJoinTableName(String leftEntity, String leftField, String rightEntity, String rightField, boolean isMap) {
		String ret = removePackageName(leftEntity);
		if (leftField != null) {
			ret += "_" + leftField;
		}
		if (rightEntity != null) {
			ret += "_" + removePackageName(rightEntity);
			if (rightField != null) {
				ret += "_" + rightField;
			}
		}
		if (isMap) {
			ret = ret.replaceAll("_list", "_map");
		}
		return ret.toLowerCase();
	}

	private String getLeftName(String leftEntity, String leftField) {
		String ret = removePackageName(leftEntity).toLowerCase();
		if (leftField != null && !leftField.toLowerCase().equals(ret)) {
			ret += "_" + leftField;
		}
		return ret.toLowerCase();
	}

	private boolean getBoolean(Object o, boolean def) {
		if (o == null)
			return def;
		try {
			boolean b = (Boolean) o;
			return b;
		} catch (Exception e) {
		}
		return def;
	}

	private class PrimaryKeyClassCreator {
		private void makePKClass(StoreDesc sdesc, ClassPool cp, List<String> pkNameList, String classname, List<Map> fields) throws Exception {
			CtClass ctClass = cp.makeClass(classname);
			ConstPool constPool = ctClass.getClassFile().getConstPool();
			ctClass.addInterface(cp.makeClass("java.io.Serializable"));
			for (String pkName : pkNameList) {
				Map<String, Object> field = getField(fields, pkName);
				Class type = _getClass((String) field.get("datatype"));
				CtField f = createField(ctClass, pkName, type.getName(), AccessFlag.PUBLIC);
			}
			ctClass.addConstructor(getConstructor1(ctClass, classname, pkNameList));
			ctClass.addConstructor(getConstructor2(ctClass, classname, pkNameList, fields));
			ctClass.addMethod(_getToStringMethod(ctClass));
			ctClass.addMethod(getCheckNullMethod(ctClass));
			ctClass.addMethod(getToStringMethod(ctClass, pkNameList));
			ctClass.addMethod(getEqualsMethod(ctClass, classname, pkNameList));
			ctClass.addMethod(getHashCodeMethod(ctClass, pkNameList));
		}

		private CtConstructor getConstructor1(CtClass ctClass, String classname, List<String> pkNameList) throws Exception {
			String m = "public " + removePackageName(classname) + "(){";
			m += "}";
			return CtNewConstructor.make(m, ctClass);
		}

		private CtConstructor getConstructor2(CtClass ctClass, String classname, List<String> pkNameList, List<Map> fields) throws Exception {
			String m = "public " + removePackageName(classname) + "(String  str){";
			m += "String tokens[] = str.split(\":\");";
			int i = 0;
			for (String pkName : pkNameList) {
				Class type = getType(fields,pkName);
				m += "String token" + i + " = tokens["+i+"];";
				m += "this." + pkName + " = (\"null\".equals(token"+i+") ) ? null : "+ type.getName() +".valueOf(token" + i + ");";
				i++;
			}
			m += "}";
			return CtNewConstructor.make(m, ctClass);
		}

		private CtMethod getToStringMethod(CtClass ctClass, List<String> pkNameList) throws Exception {
			String m = "public String toString() {";
			m += "java.lang.StringBuilder sb = new java.lang.StringBuilder();";
			boolean first = true;
			for (String pkName : pkNameList) {
				if (!first) {
					m += "sb.append(\":\");";
				}
				m += "sb.append(toString(checkNull(this." + pkName + ")));";
				first = false;
			}
			m += "return sb.toString();";
			m += "}";
			return CtNewMethod.make(m, ctClass);
		}

		private CtMethod getEqualsMethod(CtClass ctClass, String classname, List<String> pkNameList) throws Exception {
			String m = "public boolean equals(Object obj) {";
			m += "if (obj == this){";
			m += "  return true;";
			m += "}";
			m += "if (!(obj instanceof " + classname + ")){";
			m += "  return false;";
			m += "}";
			m += classname + " other = (" + classname + ")obj;";

			boolean first = true;
			m += "return ";
			for (String pkName : pkNameList) {
				if (!first) {
					m += " && ";
				}
				m += "(this." + pkName + ".equals(other." + pkName + "))";
				first = false;
			}
			m += ";}";
			return CtNewMethod.make(m, ctClass);
		}

		private CtMethod getHashCodeMethod(CtClass ctClass, List<String> pkNameList) throws Exception {
			boolean first = true;
			String m = "public boolean hashCode() {";
			m += "return ";
			for (String pkName : pkNameList) {
				if (!first) {
					m += " ^ ";
				}
				m += "checkNull(this." + pkName + ").hashCode()";
				first = false;
			}
			m += ";}";
			return CtNewMethod.make(m, ctClass);
		}

		private CtMethod _getToStringMethod(CtClass ctClass) throws Exception {
			String m = "public Object toString(Object obj) {";
			m += "if( obj instanceof Double) {";
			m += "  long l = ((Double)obj).longValue();";
			m += "  double d = ((Double)obj).doubleValue();";
			m += "  if(d  == l) {";
    	m += "    return Long.toString(((Double)obj).longValue());";
			m += "  }";
			m += "}";
			m += "return obj.toString();";
			m += "}";
			return CtNewMethod.make(m, ctClass);
		}

		private CtMethod getCheckNullMethod(CtClass ctClass) throws Exception {
			String m = "public Object checkNull(Object obj) {";
			m += "if( obj == null) return \"null\";";
			m += "return obj;";
			m += "}";
			return CtNewMethod.make(m, ctClass);
		}
		private Class getType( List<Map> fields, String name){
			Map<String,String> m = (Map)getField(fields, name);
			return _getClass(m.get("datatype"));
		}
	}


	@Reference
	public void setEntityService(EntityService paramEntityService) {
		m_entityService = paramEntityService;
		info(this,"ClassGenServiceImpl.setEntityService:" + paramEntityService);
	}

}

