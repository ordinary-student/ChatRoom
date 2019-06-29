package com.server.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.server.ui.ServerFrame;

/**
 * 服务器线程
 * 
 * @author ordinary-student
 *
 */
public class ServerThread extends Thread
{
	public ServerFrame serverFrame;
	public ServerSocket serverSocket;
	public Vector<String> messagesVector;
	public Vector<ClientThread> clientThreadsVector;
	public Map<Integer, String> usersMap;
	public BroadcastThread broadcastThread;
	// 通信端口
	private int port = 5000;
	private boolean runFlag = false;

	/**
	 * 构造方法
	 * 
	 * @param serverFrame
	 */
	public ServerThread(ServerFrame serverFrame)
	{
		this.serverFrame = serverFrame;

		// 聊天信息集合
		messagesVector = new Vector<String>();
		// 客户端线程集合
		clientThreadsVector = new Vector<ClientThread>();
		// 用户集合(线程ID和用户名)
		usersMap = new HashMap<Integer, String>();

		try
		{
			serverSocket = new ServerSocket(port);
		} catch (IOException e)
		{
			// 不能同时启动两个服务器
			this.serverFrame.setStartAndStopUnable();
			System.exit(0);
		}

		// 广播线程
		broadcastThread = new BroadcastThread(this);
		broadcastThread.setRunFlag(true);
		broadcastThread.start();
	}

	@Override
	public void run()
	{
		Socket socket;

		while (runFlag)
		{
			try
			{
				// 如果通信关闭就退出
				if (serverSocket.isClosed())
				{
					runFlag = false;
				} else
				{
					try
					{
						// 有客户端连接
						socket = serverSocket.accept();
					} catch (SocketException e)
					{
						// 出错就退出
						socket = null;
						runFlag = false;
					}

					// 连接不为空
					if (socket != null)
					{
						// 创建客户端线程并启动
						ClientThread clientThread = new ClientThread(socket, this);
						clientThread.setRunFlag(true);
						clientThread.start();

						// 添加客户端线程进集合
						synchronized (clientThreadsVector)
						{
							clientThreadsVector.addElement(clientThread);
						}

						// 往消息集合写消息
						synchronized (messagesVector)
						{
							// 记录用户登录，刚登录用户名记为@login@
							usersMap.put((int) clientThread.getId(), "@login@");
							// 记录客户端线程消息
							messagesVector.add(clientThread.getId() + "@clientThread");
						}
					}
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 停止服务器线程
	 */
	public void stopServerThread()
	{
		// 若还存活
		if (this.isAlive())
		{
			try
			{
				// 关闭通信
				serverSocket.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			// 设置退出
			setRunFlag(false);
		}
	}

	/**
	 * 设置运行标志
	 * 
	 * @param b
	 */
	public void setRunFlag(boolean b)
	{
		runFlag = b;
	}
}
