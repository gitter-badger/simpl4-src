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
package org.ms123.common.smtp;

import org.subethamail.smtp.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.system.thread.ThreadContext;

@SuppressWarnings("unchecked")
public class SWMessageHandlerFactory implements MessageHandlerFactory {

	ActivitiService m_activitiService;

	PermissionService m_permissionService;

	String m_from = null;

	public SWMessageHandlerFactory(ActivitiService as, PermissionService ps) {
		m_activitiService = as;
		m_permissionService = ps;
	}

	public MessageHandler create(MessageContext ctx) {
		return new Handler(ctx);
	}

	class Handler implements MessageHandler {

		MessageContext ctx;

		public Handler(MessageContext ctx) {
			this.ctx = ctx;
		}

		public void from(String from) throws RejectException {
			System.out.println("FROM:" + from);
			m_from = from;
		}

		public void recipient(String recipient) throws RejectException {
			System.out.println("RECIPIENT:" + recipient);
		}

		public void data(InputStream is) throws IOException {
			System.out.println("Data:"+Thread.currentThread()+"/"+Thread.currentThread().hashCode()+"/"+Thread.currentThread().getClass());
			MimeMessage mm = null;
			Map result=null;
			try {
				mm = new MimeMessage(this.getSession(), is);
				System.out.println("subject:" + mm.getSubject());
				String subject = mm.getSubject();
				String s[] = subject.split(",");
				if( s.length != 4){
					sendingReplay(mm, "Simpl4:Subject not correct:"+subject, "Usage:username,password,appname,messagename");
					return;
				}
				System.out.println("ThreadContext:"+ThreadContext.getThreadContext());
				if( ThreadContext.getThreadContext() != null){
					System.out.println("ThreadContext.sc:"+ThreadContext.getThreadContext().get(ThreadContext.SESSION_MANAGER));
				}
				m_permissionService.login(s[2], s[0], s[1]);
				result = m_activitiService.startProcessInstance(s[2], -1, null, null, null, s[3], null, new HashMap());
				sendingReplay(mm, "SimpleWorkflow:process started", "The Process startet:"+result);
			} catch (Throwable e) {
				e.printStackTrace();
				if (mm != null) {
					try {
						while (e.getCause() != null) {
							e = e.getCause();
							System.out.println("\t:cause:" + e.getMessage());
						}
						sendingReplay(mm, "SimpleWorkflow:Error starting process", e.getMessage() != null ? e.getMessage() : "Unknown error");
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
			}finally{
				if( ThreadContext.getThreadContext() != null){
					ThreadContext.getThreadContext().finalize(null);
					ThreadContext.getThreadContext().remove();
				}
				System.gc();
			}
		}

		public void done() {
			System.out.println("Finished");
		}

		private void sendingReplay( MimeMessage mm, String subject, String body){
			try{
				MimeMessage m = (MimeMessage) mm.reply(true);
				m.setText(body);
				m.setFrom(new InternetAddress("sw@osshosting.org"));
				m.setSubject(subject);
				m.setRecipients(Message.RecipientType.TO, m_from );
				Transport.send(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected Session getSession() {
			return Session.getDefaultInstance(new Properties());
		}

		public String convertStreamToString(InputStream is) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
	}
}
