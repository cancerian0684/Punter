package com.sapient.punter.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/3/11
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */
class myLogger implements ILogDevice
	{
		private Logger logger;
		public myLogger(Logger logger) {
			this.logger=logger;
		}
		@Override
		public void log(String str) {
			logger.log(Level.INFO, str);
		}

	}
