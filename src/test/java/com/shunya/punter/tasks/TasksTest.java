package com.shunya.punter.tasks;

import com.shunya.punter.utils.FieldPropertiesMap;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TasksTest {
    @Test
    public void ShouldReturnListOfInputProperties(){
        try {
            FieldPropertiesMap inProp = Tasks.listInputParams((Tasks) Class.forName("com.shunya.punter.tasks.HttpGetTask").newInstance());
            System.out.println("inProp = " + inProp);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTimeConvertedToFormattedString() {
        long millis=203400L;
        DateFormat formatter = new SimpleDateFormat("HH'h:'mm'm:'ss's'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        String dateFormatted = formatter.format(new Date(millis));
        System.out.println(dateFormatted);
    }
}
