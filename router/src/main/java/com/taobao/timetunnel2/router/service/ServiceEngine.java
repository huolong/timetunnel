package com.taobao.timetunnel2.router.service;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public abstract class ServiceEngine implements Seveice{
	private static final Logger log = Logger.getLogger(ServiceEngine.class);
	private static ServiceEngine instance; 
	protected ServiceProperties serviceProperties;
	
	private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean starting = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicBoolean shuttingdown = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    
    public void start() throws Exception {
    	this.serviceProperties = new ServiceProperties();
        if (!started.get()) {
            if (starting.compareAndSet(false, true)) {
                boolean childrenStarted = false;
                Exception ex = null;
                try {                	
                	doStart();
                } catch (ServiceException e) {
                    ex = e;
                } finally {
                    if (ex != null) {
                        try {
                            stop(childrenStarted);
                        } catch (Exception e) {
                        }
                        throw ex;
                    } else {
                        started.set(true);
                        starting.set(false);
                        stopping.set(false);
                        stopped.set(false);
                        shutdown.set(false);
                        shuttingdown.set(false);
                        log.info("router server status=started");
                        try{
                        	doServe();
                        }catch(ServiceException e){
                        	throw e;
                        }
                    }
                }
            }
        }
    }
    
    private void stop(boolean childrenStarted) throws Exception {
        if (stopping.compareAndSet(false, true)) {
            try {
                try {
                    starting.set(false);
                    if (childrenStarted) {
                        doStop();
                    }
                } finally {
                    started.set(false);
                }
            } finally {
                stopped.set(true);
                stopping.set(false);
                starting.set(false);
                started.set(false);
                shutdown.set(false);
                shuttingdown.set(false);
            }
        }
    }

    public void stop() throws Exception {
        if (started.get()) {
            stop(true);
        }
    }

    public void shutdown() throws Exception {
        // ensure we are stopped first
        stop();

        if (shuttingdown.compareAndSet(false, true)) {
            try {
                try {
                    doShutdown();
                } finally {
                }
            } finally {
                // shutdown is also stopped so only set shutdown flags
                shutdown.set(true);
                shuttingdown.set(false);
            }
        }
    }

    
    /**
     * @return true if this service has been started
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * @return true if this service is
     */
    public boolean isStarting() {
        return starting.get();
    }

    /**
     * @return true if this service is in the process of closing
     */
    public boolean isStopping() {
        return stopping.get();
    }

    /**
     * @return true if this service is closed
     */
    public boolean isStopped() {
        return stopped.get();
    }

    /**
     * Helper methods so the service knows if it should keep running.
     * Returns false if the service is being stopped or is stopped.
     *
     * @return true if the service should continue to run.
     */
    public boolean isRunAllowed() {
        return !(stopping.get() || stopped.get());
    }
    
    protected abstract void doServe() throws ServiceException;
    
    protected abstract void doStart() throws ServiceException;

    protected abstract void doStop() throws ServiceException;

    protected void doShutdown() throws ServiceException {
        // noop
    }

    public static ServiceEngine getInstance(String name) throws ServiceException{
		if (instance==null){
			try {
				instance = (ServiceEngine) Class.forName(name).newInstance();				
			} catch (InstantiationException e) {
				throw new ServiceException(String.format(
						"Creating the Service Engine[%s] is failed.[%s]",
						name, e.getCause()));
			} catch (IllegalAccessException e) {
				throw new ServiceException(String.format(
						"Creating the Service Engine[%s] is failed.[%s]",
						name, e.getCause()));
			} catch (ClassNotFoundException e) {
				throw new ServiceException(String.format(
						"Creating the Service Engine[%s] is failed.[%s]",
						name, e.getCause()));
			}
		}
		return instance;
	}
    
	public static void main(String[] args) {
		Thread.currentThread().setName("main service engine");
		//PropertyConfigurator.configure(RouterConsts.LOG_PATH);
		
		final Logger log = Logger.getLogger(ServiceEngine.class);
		log.info(RouterConsts.LOG_PATH);
		log.info("initialize....");

		try {
			Properties prop = RouterContext.getContext().getAppParam();
			String name = prop.getProperty(ParamsKey.Service.serverType, "BLOCK");
			String classname = ParamsKey.Service.serverClass.BLOCK.getClassname();
			try{
				classname = ParamsKey.Service.serverClass.valueOf(name).getClassname();
			}catch(IllegalArgumentException e){
				log.error("Invalid param[SERVER_TYPE]="+name+",so use default value.");
			}
			final ServiceEngine srv = ServiceEngine.getInstance(classname);
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						srv.shutdown();
					} catch (Exception e) {
						log.error(e);
						System.exit(-1);
					}
				}
			}));
			
			try {
				srv.start();
			} catch (Exception e) {
				log.error(e);
				System.exit(-1);
			}
		} catch (ServiceException e) {
			log.error(e);
			System.exit(-1);
		}

	}

}
