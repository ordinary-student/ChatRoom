package com.client.utils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * 窗口工具类
 * 
 * @author ordinary-student
 *
 */
public class WindowUtil
{
	/**
	 * 窗口居中
	 * 
	 * @param window
	 */
	public static void center(Window window)
	{
		// 获取默认工具
		Toolkit toolKit = Toolkit.getDefaultToolkit();
		// 获取屏幕尺寸
		Dimension screenSize = toolKit.getScreenSize();
		// 获取窗口大小
		Dimension windowSize = window.getSize();

		// 判断是否超过屏幕尺寸
		if (windowSize.height > screenSize.height)
		{
			windowSize.height = screenSize.height;
		}

		if (windowSize.width > screenSize.width)
		{
			windowSize.width = screenSize.width;
		}

		// 窗口居中
		window.setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
	}
}
