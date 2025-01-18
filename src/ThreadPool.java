import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
	private final int nThreads;
	private final BlockingCounter blockingCounter;

	private final BlockingQueue<TranslationGenerator> tasks = new LinkedBlockingQueue<TranslationGenerator>();

	private final WorkerThread[] threads;

	private volatile boolean isShutdown = false;

	public ThreadPool(int nThreads) {
		this.blockingCounter = new BlockingCounter();
		this.nThreads = nThreads;
		threads = new WorkerThread[nThreads];

		for (int i=0; i < nThreads; i++) {
			threads[i] = new WorkerThread(tasks, blockingCounter);
			threads[i].start();
		}
	}


	public synchronized void execute(TranslationGenerator r) {
		if (isShutdown) {
			throw new AssertionError("Unable to execute a thread after ThreadPool shutdown.");
		}
		blockingCounter.increment();
		tasks.add(r);
	}

	public synchronized void waitAll() {
		while (blockingCounter.intValue() > 0) {
			if (isShutdown) {
				return;
			}
			blockingCounter.waitUntilZero(5*1000);
			list(); // for debugging, print out status every 5 seconds
		}
		System.out.println("All thread tasks complete.");
	}

	public synchronized void shutdown() {
		System.out.println("Shutting down threads.");
		isShutdown = true;
		for (WorkerThread t : threads) {
			t.shutdown();
			t.interrupt();
		}
	}

	private synchronized void list() {
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();

		System.out.println("Memory avail="+(runtime.totalMemory()/mb)+"MB free="+runtime.freeMemory()/mb+"MB max="+runtime.maxMemory()/mb+"MB");
		for (WorkerThread t : threads) {
			System.out.println(t.info());
		}
	}


	private class WorkerThread extends Thread {

		private final BlockingQueue<TranslationGenerator> poolTaskQueue;
		private final BlockingCounter blockingCounter;

		private final Object printLock = new Object();
		private final Object querryLock = new Object();

		private volatile boolean isRunning = false;
		private volatile TranslationGenerator generator = null;

		public WorkerThread(BlockingQueue<TranslationGenerator> poolTaskQueue, BlockingCounter blockingCounter) {
			this.poolTaskQueue = poolTaskQueue;
			this.blockingCounter = blockingCounter;
		}

		private volatile boolean isShutdown = false;

		public synchronized void shutdown() {
			isShutdown = true;
		}

		@Override
		public void run() {
			while (true) {
				if (isShutdown) {
					System.out.println("Exiting thread");
					return;
				}

				try {
					synchronized(querryLock) {
						isRunning = false;
					}
					generator = poolTaskQueue.take(); // blocks
					isRunning = true;
					generator.run();
					synchronized(querryLock) {
						generator = null;
						isRunning = false;
						blockingCounter.decrement();
					}
				} catch (InterruptedException e) {
				}
			}
		}

		public String info() {
			synchronized (querryLock) {
				return "Running(" + isRunning + ") " + (generator != null ? generator.info() : "null");
			}
		}
	}

	private class BlockingCounter {
		private final AtomicInteger counter;
		private final Object counterNotify = new Object();

		public BlockingCounter(int initialValue) {
			counter = new AtomicInteger(initialValue);
		}

		public BlockingCounter() {
			counter = new AtomicInteger(0);
		}

		public synchronized void increment() {
			synchronized (counterNotify) {
				counter.incrementAndGet();
			}
		}

		public synchronized void decrement() {
			synchronized (counterNotify) {
				counter.decrementAndGet();
				counterNotify.notifyAll();
			}
		}

		public synchronized void waitUntilZero() {
			synchronized (counterNotify) {
				while (counter.intValue() > 0) {
					try {
						counterNotify.wait();
					} catch (InterruptedException e) {
						// continue
					}
				}
			}
		}

		public synchronized void waitUntilZero(final long timeout_ms) {
			final long start_time = System.currentTimeMillis();
			long t;
			synchronized (counterNotify) {
				while (counter.intValue() > 0) {
					try {
						t = timeout_ms - (System.currentTimeMillis() - start_time);
						if (t <= 0)
							break;

						counterNotify.wait(t);
					} catch(InterruptedException e) {
						// continue
					}
				}
			}
		}
		
		public synchronized int intValue() {
			return counter.intValue();
		}

	}

}
