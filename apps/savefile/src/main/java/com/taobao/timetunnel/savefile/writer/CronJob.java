package com.taobao.timetunnel.savefile.writer;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.taobao.timetunnel.savefile.app.Conf;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public class CronJob {
	private static final Logger log = Logger.getLogger(CronJob.class);
	private static OutputStreamManager outputStreamManager = OutputStreamManager.getInstance();
	private Scheduler scheduler;

	public CronJob() {
	}

	public void start() {
		init();
	}

	public static class QuartzJob implements Job {
		public QuartzJob() {
		}

		public void execute(JobExecutionContext context) throws JobExecutionException {
			log.error("Flush output streams");
			try {
				outputStreamManager.switchAllOutputStreams();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private void init() {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			JobDetail job = new JobDetail("job", "group", QuartzJob.class);
			Trigger trigger = new CronTrigger("trigger", "group", Conf.getInstance().getFileSwitchCronJob());
			log.error("Quartz cron : " + Conf.getInstance().getFileSwitchCronJob());
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			log.error("Fail to start quarts job", e);
			System.exit(2);
		}
	}

	public void stop() {
		log.error("FileWriter close all outputStream");
		synchronized (outputStreamManager) {
			try {
				outputStreamManager.closeAllOutputStreams();
			} catch (IOException e) {
				log.error("close output stream when stop CrobJob:", e);
			}
		}
		log.error("FileWriter stop scheduler");
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("shutdown scheduler error:", e);
		}
	}
}
