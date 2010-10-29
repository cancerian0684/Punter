package com.sapient.punter.gui;

import java.awt.Dimension;

public interface AppSettingsMBean {
	public abstract void setMaxResults(int maxResults);
	public abstract int getMaxResults();
	public abstract void setKBFrameDimension(Dimension kBFrameDimension);
	public abstract Dimension getKBFrameDimension();
	public abstract void setMultiSearchEnable(boolean multiSearchEnable);
	public abstract boolean isMultiSearchEnable();
}