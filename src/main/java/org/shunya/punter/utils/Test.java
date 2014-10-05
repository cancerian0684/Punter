package org.shunya.punter.utils;

import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.tasks.EchoTask;

import javax.swing.text.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Test {
    private static int lineBufferSize = 300;
    private static Document logDocument = new PlainDocument() {
        public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
            super.insertString(offs, str, a);

        }

        ;

        protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
            super.insertUpdate(chng, attr);
            Element root = getDefaultRootElement();
            while (root.getElementCount() > lineBufferSize) {
                Element firstLine = root.getElement(0);

                try {
//					System.err.println("removing");
                    remove(0, firstLine.getEndOffset());

                } catch (BadLocationException ble) {
                    System.out.println(ble + " = " + lineBufferSize);
                }
            }
        }

        ;
    };


    class MyKey {
        String name;
        PunterTask ann;

        public MyKey(String name, PunterTask ann) {
            this.name = name;
            this.ann = ann;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MyKey other = (MyKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        private Test getOuterType() {
            return Test.this;
        }


    }

    public static void main1(String[] args) {
        String taskPackage = "org.shunya.punter.tasks";
        ;
        Package pkg = Package.getPackage(taskPackage);
        PunterTask ann = EchoTask.class.getAnnotation(PunterTask.class);
        System.err.println(ann.description());
        Test test = new Test();
        MyKey myKey = test.new MyKey("Munish Chandel", ann);
        Map<MyKey, Object> map = new HashMap<MyKey, Object>();
        map.put(myKey, "munish");
        String out = (String) map.get(myKey);
        System.err.println(out);
    /*double f=3.226;
    double g=f+1;
	System.out.println(f);
	System.out.println(g);
	System.out.println(Double.parseDouble("12560.23565498794645465")*.2);
	System.out.println("--- Normal Print-----");
	System.out.println(2.00 - 1.1);
	System.out.println(2.00 - 1.2);
	System.out.println(2.00 - 1.3);
	System.out.println(2.00 - 1.4);
	System.out.println(2.00 - 1.5);
	System.out.println(2.00 - 1.6);
	System.out.println(2.00 - 1.7);
	System.out.println(2.00 - 1.8);
	System.out.println(2.00 - 1.9);
	for(int i=0;i<=100;i++)
	System.out.println(2.00005 - 2.0);*/
        try {
            int i = 0;
            while (i < 5) {
                logDocument.insertString(logDocument.getLength(),
                        "My Name is Munish\r\n", null);
                System.out.println(logDocument.getLength());
                i++;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        List<Car> cars = new ArrayList<>();
        cars.add(new Car("Audi W", 2005));
        cars.add(new Car("Audi X", 2006));
        cars.add(new Car("BMW Y", 2013));
        cars.add(new Car("Ford Z", 2014));

//        Prior to Java SE8, we would use something like this to loop over the list:

        for (Car c : cars) {
            if (c.year > 2007)
                System.out.println(c.name);
        }

//        Now, using lambda expression and functional operations, the above code can be simplified into this:

        System.out.println("After the use of Lambda");
        cars.stream().filter((c) -> (c.year > 2007)).forEach(System.out::println);
        cars.parallelStream().filter((c) -> (c.year > 2007)).forEach(car -> System.out.println(car.year + " => " + car.name));
        cars.stream().filter((c) -> (c.year > 2007)).forEach(car -> System.out.println(car.year + " => " + car.name));

        System.out.println("removing all cars older than 2007");
        cars.removeIf(c -> c.year < 2007);
        cars.stream().forEach(car -> System.out.println(car.year + " => " + car.name));

        try (Stream<Path> entries = Files.walk(Paths.get("C:\\tdm-batch-parent"), 2)) {
            // Contains all descendants, visited in depth-first order
            entries.forEach(System.out::println);
//            System.out.println("entries = " + entries);
        }
    }

    private static class Car {
        private final String name;
        private final int year;

        public Car(String name, int year) {
            this.name = name;
            this.year = year;
        }

        @Override
        public String toString() {
            return "Car{" +
                    "name='" + name + '\'' +
                    ", year=" + year +
                    '}';
        }
    }
}
