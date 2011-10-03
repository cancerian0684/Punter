package com.sapient.punter.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

class MyLogDevice implements ILogDevice
	{
		private Logger logger;
		public MyLogDevice(Logger logger) {
			this.logger=logger;
		}
		@Override
		public void log(String str) {
			logger.log(Level.INFO, str);
		}

	}
