package com.sapient.punter.utils;

import java.util.HashMap;
import java.util.Map;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.tasks.EchoTask;

public class Test {
	class MyKey{
		String name;
		PunterTask ann;
		public MyKey(String name,PunterTask ann) {
			this.name=name;
			this.ann=ann;
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
public static void main(String[] args) {
	String taskPackage="com.sapient.punter.tasks";;
	Package pkg=Package.getPackage(taskPackage);
	PunterTask ann = EchoTask.class.getAnnotation(PunterTask.class);
	System.err.println(ann.description());
	Test test=new Test();
	MyKey myKey= test.new MyKey("Munish Chandel", ann);
	Map<MyKey,Object> map=new HashMap<MyKey,Object>();
	map.put(myKey, "munish");
	String out=(String) map.get(myKey);
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
}
}
