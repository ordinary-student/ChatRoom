package com.server.main;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.server.thread.ServerThread;
import com.server.ui.ServerFrame;

/**
 * 服务器
 * 
 * @author ordinary-student
 *
 */
public class Server
{
	private ServerFrame serverFrame;
	private ServerThread serverThread;

	/**
	 * 构造方法
	 */
	public Server()
	{
	}

	/**
	 * 获取服务器窗口
	 * 
	 * @return
	 */
	public ServerFrame getServerFrame()
	{
		return serverFrame;
	}

	/**
	 * 设置服务器窗口
	 * 
	 * @param serverFrame
	 */
	public void setServerFrame(ServerFrame serverFrame)
	{
		this.serverFrame = serverFrame;
	}

	/**
	 * 启动服务器
	 */
	public void startServer()
	{
		try
		{
			// 创建服务器线程
			serverThread = new ServerThread(serverFrame);
			// 设置退出标志
			serverThread.setExitFlag(true);
			// 启动服务器线程
			serverThread.start();
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(serverFrame, "服务器启动失败！", "错误", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * 停止服务器
	 */
	public void stopServer()
	{
		// 停止服务器
		synchronized (serverThread.messagesVector)
		{
			// 停止服务器的消息
			String message = "@serverexit";
			// 加入消息集合
			serverThread.messagesVector.add(message);
		}

		// 设置退出信息
		serverThread.serverFrame.displayChatMessage("@exit");
		serverThread.serverFrame.displayUsers("@exit");
		// 停止服务器线程
		serverThread.stopServerThread();
	}

	/**
	 * 关闭服务器
	 */
	public void close()
	{
		// 若服务器线程未停止，则去停止它
		if (serverThread != null)
		{
			if (serverThread.isAlive())
			{
				serverThread.stopServerThread();
			}
		}

		// 退出
		System.exit(0);
	}

	/**
	 * 主方法
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			// 设置窗口风格样式
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "加载本地系统窗口样式失败！");
		} finally
		{
			// 创建服务器
			Server server = new Server();
			// 创建服务器窗口
			ServerFrame serverFrame = new ServerFrame(server);
			// 设置服务器窗口
			server.setServerFrame(serverFrame);
			// 设置可视
			serverFrame.setVisible(true);
		}
	}

}
