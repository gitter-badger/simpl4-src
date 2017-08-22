package org.ms123.common.process.listener;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.*;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import static com.jcabi.log.Logger.info;

/**
 * @author Ronny Bräunlich
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class OSGiEventDistributor implements TaskListener, ExecutionListener, Serializable {

	private static final long serialVersionUID = -3778622638807349820L;

	private EventAdmin eventAdmin;
	private String tenant;

	public OSGiEventDistributor(EventAdmin eventAdmin, String tenant) {
		this.eventAdmin = eventAdmin;
		this.tenant = tenant;
	}

	@Override
	public void notify(DelegateExecution execution) {
		Event event = createEvent(execution);
		eventAdmin.postEvent(event);
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		Event event = createEvent(delegateTask);
		eventAdmin.postEvent(event);
	}

	private Event createEvent(DelegateTask delegateTask) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		fillDictionary( delegateTask, properties, true);
		return new Event(Topics.TASK_EVENT_TOPIC+"/"+this.tenant, properties);
	}

	private Event createEvent(DelegateExecution execution) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		fillDictionary( execution, properties, true);
		return new Event(Topics.EXECUTION_EVENT_TOPIC+"/"+this.tenant, properties);
	}

	private void fillDictionary(Object o,Dictionary properties, boolean isTask) {
		Class clazz = o.getClass();
		if( isTask ){
			initializeFormKey(o,clazz);
		}
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			Class returnType = m.getReturnType();
			String getter = m.getName();
			String baseName = getBaseName(getter);
			String prefix = getGetterPrefix(getter);
			if (!Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				Map<String, Object> map = new HashMap<String, Object>();
				try{
					Method method = clazz.getMethod(getter);
					Object value = method.invoke(o);
					if (value != null ) {
						properties.put( baseName, value);
					}
				}catch(Exception e){
					info(this,"Exception("+baseName+"):"+e);
				}
			}
		}
	}
	private boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
	}

	private void initializeFormKey(Object o, Class clazz){
		try{
			Method method = clazz.getMethod("initializeFormKey");
			method.invoke(o);
		}catch(Exception e){
			info(this,"Exception(initializeFormKey):"+e);
		}
	}

	private String[] getterPrefixes = new String[] { "is", "has", "get" };
	private String getGetterPrefix(String mName) {
		for (String pre : getterPrefixes) {
			if (mName.startsWith(pre)) {
				return pre;
			}
		}
		return null;
	}
	private String getBaseName(String methodName) {
		if (methodName.startsWith("get")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("has")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("is")) {
			return firstToLower(methodName.substring(2));
		}
		return firstToLower(methodName);
	}
	private String firstToLower(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

}

