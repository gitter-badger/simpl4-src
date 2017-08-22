package org.ms123.common.process.listener;

/**
 * @author Ronny Bräunlich
 */
public interface Topics {

  public static final String TASK_EVENT_TOPIC = "processService/TaskEvent";

  public static final String EXECUTION_EVENT_TOPIC = "processService/ExecutionEvent";
  
  public static final String ALL_EVENTING_EVENTS_TOPIC = "processService/*";
}
