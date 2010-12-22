package com.taobao.timetunnel.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel.client.disk.FCQMeta;
import com.taobao.timetunnel.client.impl.Config;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-13
 * 
 */
public class FCQMetaTest {
	@Before
	public void clear() {

		File file = new File(Config.getInstance().getMetaPath() + File.separator + "target/FCQMetaTest");
		if (!file.exists())
			return;
		for (File f : file.listFiles()) {
			System.out.println(f.getAbsolutePath());
			System.out.println(f.delete());
		}
	}

	@Test
	public void metaTest() throws Exception {
		FCQMeta q = new FCQMeta("target/FCQMetaTest");
		assertThat(0L, equalTo(q.getReadPos()));
		assertThat("null", equalTo(q.getFileName()));
		q.update(10, "test");
		assertThat(10L, equalTo(q.getReadPos()));
		assertThat("test", equalTo(q.getFileName()));
		q.update(11, "test2");
		assertThat(11L, equalTo(q.getReadPos()));
		assertThat("test2", equalTo(q.getFileName()));
		q.update(12, "test3");
		assertThat(12L, equalTo(q.getReadPos()));
		assertThat("test3", equalTo(q.getFileName()));
		q.update(13, "test4");
		assertThat(13L, equalTo(q.getReadPos()));
		assertThat("test4", equalTo(q.getFileName()));
		q.close();
	}

}
