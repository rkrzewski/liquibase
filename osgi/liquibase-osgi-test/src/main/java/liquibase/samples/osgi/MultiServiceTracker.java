package liquibase.samples.osgi;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Tracks multiple services simultaneously and posts information about service
 * availability to a process running in a dedicated thread.
 * 
 * @param <S>
 *            custom {@link MultiServiceTracker.State} implementation.
 * @param <P>
 *            custom {@link MultiServiceTracker.Process} implementation.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class MultiServiceTracker<S extends MultiServiceTracker.State, P extends MultiServiceTracker.Process<S>>
		extends ServiceTracker {

	private S current;

	private final P process;

	private final ProcessRunner runner = new ProcessRunner();

	private final Queue<S> queue = new ConcurrentLinkedQueue<S>();

	public MultiServiceTracker(BundleContext context, Filter filter, S initial,
			P process) {
		super(context, filter, null);
		current = initial;
		this.process = process;
	}

	@Override
	public void open() {
		queue.offer(current);
		Executors.defaultThreadFactory().newThread(runner).start();
		super.open();
	}

	@Override
	public void close() {
		super.close();
		runner.quit();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		current.added(reference, service);
		synchronized (queue) {
			queue.offer((S) current.clone());
			queue.notify();
		}
		return service;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void modifiedService(ServiceReference reference, Object service) {
		current.modified(reference, service);
		synchronized (queue) {
			queue.offer((S) current.clone());
			queue.notify();
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removedService(ServiceReference reference, Object service) {
		current.removed(reference, service);
		synchronized (queue) {
			queue.offer((S) current.clone());
			queue.notify();
		}
		context.ungetService(reference);
	}

	/**
	 * A snapshot of available service objects at a given point of time.
	 * 
	 * <p>
	 * Subclasses may either define fields that will be assigned with service
	 * objects as they become available and cleared as they disappear or
	 * override both {@link #added(ServiceReference, Object)} and
	 * {@link #removed(ServiceReference, Object)} methods.
	 * </p>
	 * 
	 * <p>
	 * When default {@code added} / {@code removed} implementations are used,
	 * care must me taken to define fields with such types that each field might
	 * be assigned from only one of the tracked services.
	 * </p>
	 */
	public static abstract class State implements Cloneable {
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(
						"Unexpected CloneNotSupportedException", e);
			}
		}

		@SuppressWarnings("rawtypes")
		public void added(ServiceReference reference, Object service) {
			setProperty(service, service);
		}

		@SuppressWarnings("rawtypes")
		public void modified(ServiceReference reference, Object service) {

		}

		@SuppressWarnings("rawtypes")
		public void removed(ServiceReference reference, Object service) {
			setProperty(service, null);
		}

		protected final void setProperty(Object service, Object actual) {
			Field found = null;
			for (Field field : getClass().getDeclaredFields()) {
				if (field.getType().isAssignableFrom(service.getClass())) {
					if (found == null) {
						found = field;
					} else {
						throw new RuntimeException(
								"more than one field assignable from type "
										+ service.getClass());
					}
				}
			}
			if (found == null) {
				throw new RuntimeException("no fields assignable from type "
						+ service.getClass());
			} else {
				try {
					found.setAccessible(true);
					found.set(this, actual);
				} catch (Exception e) {
					throw new RuntimeException("unable to assign field "
							+ found.toString(), e);
				}
			}
		}
	}

	/**
	 * The process will react to service's state changes.
	 * 
	 * 
	 * @param <S>
	 *            custom {@link MultiServiceTracker.State} implementation.
	 */
	public static abstract class Process<S> {

		/**
		 * Handle state change.
		 * 
		 * <p>
		 * The method will run in a dedicated thread, not interfering with
		 * framework's event dispatch process. The thread invoking this method
		 * will not hold any locks, which means you can safely call framework
		 * operations from it (service registration etc.)
		 * </p>
		 * 
		 * @param state
		 *            current state. The state object is guaranteed not be
		 *            mutated during execution of this call.
		 */
		public abstract void enter(S state);
	}

	/**
	 * A simple task runner.
	 */
	private class ProcessRunner implements Runnable {

		private boolean quit = false;

		private Thread thread;

		public void run() {
			// store our thread reference so that quit() method may interrupt
			// the wait
			thread = Thread.currentThread();
			while (!quit) {
				S state;
				synchronized (queue) {
					try {
						// check for items posted before thread started
						state = queue.poll();
						if (state == null) {
							// queue empty, wait for notification, then re-check
							queue.wait();
							state = queue.poll();
						}
					} catch (InterruptedException e) {
						return;
					}
				}
				// re-check if we've actually picked up an item - guard against
				// out-of-band notification
				if (state != null) {
					// do work outside of synchronized block
					try {
						process.enter(state);
					} catch (RuntimeException e) {
						// sub-optimal handing, but we can't allow it to
						// propagate as it would stop the worker thread
						System.out.println("Unhandled runtime exception");
						e.printStackTrace();
					}
				}
			}
		}

		public void quit() {
			quit = true;
			thread.interrupt();
		}
	}
}
