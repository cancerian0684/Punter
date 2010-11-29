package com.sapient.punter.gui;

import java.awt.Dimension;

public interface AppSettingsMBean {
	public abstract void setMaxResults(int maxResults);
	public abstract int getMaxResults();
	public abstract void setKBFrameDimension(Dimension kBFrameDimension);
	public abstract Dimension getKBFrameDimension();
	public abstract void setMultiSearchEnable(boolean multiSearchEnable);
	public abstract boolean isMultiSearchEnable();
	public abstract void setKeyStrokeFlush(int keystrokeFlush);
	public abstract int getKeyStrokeFlush();
	public abstract int getMaxKeyStrokeDelay();
	public abstract void setMaxKeyStrokeDelay(int maxKeyStrokeDelay);
	public abstract void setMaxExecutorSize(int maxExecutorSize);
	public abstract int getMaxExecutorSize();
}