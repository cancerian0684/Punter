package com.shunya.punter.tasks;

import com.shunya.punter.utils.FieldPropertiesMap;
import org.junit.Test;

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
}
