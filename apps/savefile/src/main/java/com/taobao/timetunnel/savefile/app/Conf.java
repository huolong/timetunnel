package com.taobao.timetunnel.savefile.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public class Conf {
	private static Conf instance = new Conf();
	private static final Logger log = Logger.getLogger(Conf.class);

	static class Constant {
		static final String BASEDIR = "app.data_dir";
		static final String FILE_PATH_COMPATIBLE = "app.file_path_compatible";
		static final String SERIALIZABLE = "app.serializable";
		static final String FILE_SWITCH_CRON_STR = "app.file_switch_cron_str";
		static final String USER = "app.user";
		static final String PASSWORD = "app.password";
		static final String TOPICS = "app.topics";
		static final String SAMPLE_RATE = ".sample_rate";
		static final String TIMEOUT = ".timeout";
		static final String RECEIVE_SIZE = ".receive_size";
		static final String FILTER_CLASS = ".filter_class";
	}

	private Properties p;

	private Conf() {
		load();
	}

	private void load() {
		InputStream rs = Conf.class.getClassLoader().getResourceAsStream("savefile.conf");
		p = new Properties();
		try {
			p.load(rs);
		} catch (IOException e) {
			log.error("load conf error and exit", e);
			System.exit(-1);
		}
	}

	public static Conf getInstance() {
		return instance;
	}

	public String getBaseDir() {
		String baseDir = (String) p.get(Constant.BASEDIR);
		if (baseDir == null || "".equals(baseDir.trim())) {
			throw new RuntimeException("base dir is null");
		}
		log.info("base dir from conf: " + baseDir);
		return baseDir;
	}

	public boolean getfilePathCompatible() {
		String fpcStr = (String) p.get(Constant.FILE_PATH_COMPATIBLE);
		if (fpcStr == null || "".equals(fpcStr.trim()) || "true".equalsIgnoreCase(fpcStr.trim())) {
			log.info("filePathCompatible from conf: true");
			return true;
		}
		log.info("filePathCompatible from conf: false");
		return true;
	}

	public String getFileSwitchCronJob() {
		String fsc = (String) p.get(Constant.FILE_SWITCH_CRON_STR);
		if (fsc == null || "".equals(fsc.trim())) {
			throw new RuntimeException("FILE_SWITCH_CRON_STR is null");
		}
		log.info("FILE_SWITCH_CRON_STR from conf: " + fsc);
		return fsc;
	}

	public boolean getSerializable() {
		String serStr = (String) p.get(Constant.SERIALIZABLE);
		if (serStr == null || "".equals(serStr.trim()) || !"true".equalsIgnoreCase(serStr.trim())) {
			log.info("SERIALIZABLE from conf: false");
			return false;
		}
		log.info("SERIALIZABLE from conf: true");
		return true;
	}

	public String[] getTopics() {
		String topics = (String) p.get(Constant.TOPICS);
		if (topics == null || "".equals(topics.trim())) {
			throw new RuntimeException("TOPICS is null");
		}
		log.info("TOPICS from conf: " + topics);
		return topics.split(",");
	}

	public String[] getSamplingrates() {
		String[] tps = getTopics();
		String[] ret = new String[tps.length];
		int i = 0;
		for (String name : tps) {
			String rate = (String) p.get(name.trim() + Constant.SAMPLE_RATE);
			if (rate == null || "".equals(rate.trim()) || "null".equalsIgnoreCase(rate.trim())) {
				rate = "100";
			}
			ret[i++] = rate;
		}
		log.info("SAMPLE_RATE from conf: " + Arrays.asList(ret));
		return ret;
	}

	public Integer[] getTimeout() {
		String[] tps = getTopics();
		Integer[] ret = new Integer[tps.length];
		int i = 0;
		for (String name : tps) {
			String timeout = (String) p.get(name.trim() + Constant.TIMEOUT);
			if (timeout == null || "".equals(timeout.trim()) || "null".equalsIgnoreCase(timeout.trim())) {
				timeout = "1800";
			}
			ret[i++] = Integer.parseInt(timeout);
		}
		log.info("TIMEOUT from conf: " + Arrays.asList(ret));
		return ret;
	}

	public Integer[] getRcvSize() {
		String[] tps = getTopics();
		Integer[] ret = new Integer[tps.length];
		int i = 0;
		for (String name : tps) {
			String rcvSize = (String) p.get(name.trim() + Constant.RECEIVE_SIZE);
			if (rcvSize == null || "".equals(rcvSize.trim()) || "null".equalsIgnoreCase(rcvSize.trim())) {
				rcvSize = "200";
			}
			ret[i++] = Integer.parseInt(rcvSize);
		}
		log.info("RECEIVE_SIZE from conf: " + Arrays.asList(ret));
		return ret;
	}

	public String getUser() {
		String user = (String) p.get(Constant.USER);
		if (user == null || "".equals(user.trim())) {
			throw new RuntimeException("user is null");
		}
		log.info("user from conf: " + user);
		return user;
	}

	public String getPassword() {
		String pwd = (String) p.get(Constant.PASSWORD);
		if (pwd == null || "".equals(pwd.trim())) {
			throw new RuntimeException("pwd is null");
		}
		log.info("pwd from conf: " + pwd);
		return pwd;
	}

	public String getFilterStr() {
		String[] tps = getTopics();
		StringBuilder ret = new StringBuilder();
		int i = 0;
		for (String name : tps) {
			String filter = (String) p.get(name.trim() + Constant.FILTER_CLASS);
			if (filter == null || "".equals(filter.trim()) || "null".equalsIgnoreCase(filter.trim())) {
				filter = "null";
			}
			ret.append(filter);
			if (i++ != tps.length - 1)
				ret.append(",");
		}
		log.info("FILTER_CLASS from conf: " + ret.toString());
		return ret.toString();
	}

	public static void main(String[] args) {
		Conf conf = Conf.getInstance();
		conf.getBaseDir();
		conf.getfilePathCompatible();
		conf.getFileSwitchCronJob();
		conf.getFilterStr();
		conf.getPassword();
		conf.getRcvSize();
		conf.getSamplingrates();
		conf.getSerializable();
		conf.getTimeout();
		conf.getTopics();
		conf.getUser();
	}

}
