package org.stt.importer;

import java.io.StringReader;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.stt.importer.ti.DefaultItemImporter;
import org.stt.model.TimeTrackingItem;
import org.stt.persistence.ItemReader;

import com.google.common.base.Optional;

public class DefaultItemImporterTest {

	@Test
	public void multiLineCommentGetsImportedCorrectly() {
		
		//GIVEN
		StringReader stringReader = new StringReader("2012-10-10_22:00:00 2012-11-10_22:00:01 this is\\n a multiline\\r string\\r\\n with different separators");
		ItemReader theReader = new DefaultItemImporter(stringReader);
		
		//WHEN
		Optional<TimeTrackingItem> readItem = theReader.read();
		
		//THEN
		Assert.assertEquals("this is\n a multiline\r string\r\n with different separators", readItem.get().getComment().get());
	}

	@Test
	public void onlyStartTimeGiven() {
		
		//GIVEN
		StringReader stringReader = new StringReader("2012-10-10_22:00:00");
		ItemReader theReader = new DefaultItemImporter(stringReader);
		
		//WHEN
		Optional<TimeTrackingItem> readItem = theReader.read();
		
		//THEN
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 9, 10, 22, 00, 00);
		cal.set(Calendar.MILLISECOND, 0);
		//comparing the Date because if a Calendar is different, the output is way too much to compare visually
		Assert.assertEquals(cal.getTime(), readItem.get().getStart().getTime());
	}
}