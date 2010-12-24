package com.taobao.timetunnel.savefile.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.savefile.writer.FileWriter;
import com.taobao.timetunnel.util.filter.ContentFilter;
import static com.taobao.timetunnel.client.TimeTunnel.*;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public class SaveFileApp {
	private List<StoppableService> writers;
	public static Map<String, List<ContentFilter>> filters = new HashMap<String, List<ContentFilter>>();
	private static final Logger log = Logger.getLogger(SaveFileApp.class);

	public SaveFileApp() {
		writers = new ArrayList<StoppableService>();
	}

	public void start() {
		timetunnelAuth();
		String[] topicStrs = Conf.getInstance().getTopics();
		try {
			filterLoad(topicStrs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Integer[] timeout = Conf.getInstance().getTimeout();
		Integer[] rcvSize = Conf.getInstance().getRcvSize();

		String[] samplingRateStrs = Conf.getInstance().getSamplingrates();
		for (int i = 0; i < topicStrs.length; i++) {
			int samplingRate = Integer.valueOf(samplingRateStrs[i]);
			FileWriter fileWriter = new FileWriter(topicStrs[i], timeout[i], rcvSize[i], new FileWriter.WriteCompletionHandler() {
				@Override
				public void onCompletion(List<Message> messages, boolean isSuccess) {
				}
			}, samplingRate);
			writers.add(fileWriter);
			fileWriter.start();
		}

	}

	private void timetunnelAuth() {
		String user = Conf.getInstance().getUser();
		String password = Conf.getInstance().getPassword();
		use(passport(user, password));
	}

	private void filterLoad(String[] topicStrs) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String filterName = Conf.getInstance().getFilterStr();
		if (filterName == null || filterName.equalsIgnoreCase("null") || "".equals(filterName.trim())) {
			return;
		}
		if ((filterName != null)) {
			String[] filterNames = filterName.split(",");
			for (int i = 0; i < filterNames.length; i++) {
				String fName = filterNames[i].trim();
				if (fName.equalsIgnoreCase("") || fName.equalsIgnoreCase("null")) {
					filters.put(topicStrs[i], null);
					continue;
				}
				String[] ss = fName.split(";");
				List<ContentFilter> v = new ArrayList<ContentFilter>();
				for (String s : ss) {
					Class<?> filterClass = Class.forName(s);
					ContentFilter filter = (ContentFilter) filterClass.newInstance();
					v.add(filter);
					log.debug("load filter " + filter.getClass().getName() + " for topic: " + topicStrs[i] + " and filter name: " + s);
				}
				filters.put(topicStrs[i], v);
			}
		}
	}

	private void destroy() {
		for (StoppableService fw : writers) {
			fw.stop();
		}
	}

	public Runnable createShutdownHook() {
		return new ShutdownJob();
	}

	class ShutdownJob implements Runnable {
		@Override
		public void run() {
			destroy();
		}
	}

	public static void main(String[] args) {
		SaveFileApp app = new SaveFileApp();
		Runtime.getRuntime().addShutdownHook(new Thread(app.createShutdownHook(), "shutdown hook"));
		app.start();
	}

}
