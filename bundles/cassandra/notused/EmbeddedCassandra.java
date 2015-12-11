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
package org.ms123.common.cassandra;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.cassandra.service.CassandraDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Run a cassandra instance in-process.
 */
public class EmbeddedCassandra implements AutoCloseable {


	private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("EmbeddedCassandra").build());

	private final CassandraDaemon cassandra;

	private final AtomicBoolean running = new AtomicBoolean(false);

	/**
   * Create a new {@link com.spotify.cassandra.extra.EmbeddedCassandra} instance.
   *
   * @return a new EmbeddedCassandra instance.
   * @throws IOException if the instance can't be created.
   */
	public static EmbeddedCassandra create() {
		System.setProperty("cassandra-foreground", "false");
		try{
			System.setProperty("cassandra.config", new File(System.getProperty("etc.dir"), "cassandra.yaml").toURI().toURL().toString());
		}catch(Exception e){
			throw new RuntimeException("EmbeddedCassandra.set.config:",e);
		}
		final CassandraDaemon cassandra = new CassandraDaemon();
		return new EmbeddedCassandra(cassandra);
	}

	public EmbeddedCassandra(CassandraDaemon cassandra) {
		this.cassandra = cassandra;
	}

	/**
   * Starts the embedded cassandra instance.
   *
   * @throws EmbeddedCassandraException if cassandra can't start up
   */
	public void start() {
		if (running.compareAndSet(false, true)) {
			try {
				info("Starting Embedded Cassandra");
				Future<Void> startupFuture = executorService.submit(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						cassandra.activate();
						info("Embedded Cassandra started");
						return null;
					}
				});
				startupFuture.get();
			} catch (Exception e) {
				throw new RuntimeException("EmbeddedCassandra.Can't start up cassandra", e);
			}
		}
	}

	/**
   * Stops the embedded cassandra instance.
   */
	public void stop() {
		if (running.compareAndSet(true, false)) {
			info("Stopping Embedded Cassandra");
			cassandra.deactivate();
			try{
				executorService.shutdown();
				executorService.awaitTermination(200,java.util.concurrent.TimeUnit.SECONDS);
				info("Embedded Cassandra stopped");
			}catch(Exception e){
				info("Embedded Cassandra awaitTermination.interrupt:"+e);
			}
		}
	}
	public void close() {
		stop();
	}

	protected static void debug(String msg) {
		System.err.println(msg);
		m_logger.debug(msg);
	}
	protected static void info(String msg) {
		System.out.println(msg);
		m_logger.info(msg);
	}
	private static final Logger m_logger = LoggerFactory.getLogger(EmbeddedCassandra.class);
}
