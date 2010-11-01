package com.sapient.server;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class ServerSettings implements ServerSettingsMBean {
	private static ServerSettings instance;
	private StaticDaoFacade sdf;
	private int maxResultsToDisplay=10;
	public static synchronized ServerSettings getInstance(){
		if(instance==null){
			instance=new ServerSettings();
		}
		return instance;
	}
	private ServerSettings() {
		sdf = StaticDaoFacade.getInstance();
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this,new ObjectName("punter.log.mbean:type=Punter-ServerSettings"));
			System.err.println("ServerSettings registered with MBean Server.");
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refreshIndexes() {
		System.err.println("Refreshing indexe's");
		sdf.rebuildIndex();
		System.err.println("Indexes refreshed");
	}
	@Override
	public void stopServer() {
		System.err.println("Stopping system.");
		System.exit(0);
	}
	@Override
	public void setMaxResultsToDisplay(int maxResults) {
		this.maxResultsToDisplay=maxResults;
		
	}
	@Override
	public int getMaxResultsToDisplay() {
		if(maxResultsToDisplay<5)
			maxResultsToDisplay=7;
		return maxResultsToDisplay;
	}
	@Override
	public void optimizeIndex() {
		LuceneIndexDao.getInstance().optimizeIndex();
	}
}
