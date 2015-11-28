package org.shunya.punter.tasks;

import org.slf4j.Logger;

class MyLogDevice implements ILogDevice
	{
		private Logger logger;
		public MyLogDevice(Logger logger) {
			this.logger=logger;
		}
		@Override
		public void log(String str) {
			logger.info(str);
		}

	}
