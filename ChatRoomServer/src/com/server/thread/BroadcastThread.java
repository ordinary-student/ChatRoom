package com.server.thread;

import java.io.IOException;

/**
 * 广播线程
 * 
 * @author ordinary-student
 *
 */
public class BroadcastThread extends Thread
{
	private ClientThread clientThread;
	private ServerThread serverThread;
	private String message;
	private boolean exitFlag = false;

	/*
	 * 构造方法
	 */
	public BroadcastThread(ServerThread serverThread)
	{
		this.serverThread = serverThread;
	}

	@Override
	public void run()
	{
		// 新消息标志
		boolean newMessageFlag = true;

		while (exitFlag)
		{
			// 获取消息集合中的消息
			synchronized (serverThread.messagesVector)
			{
				// 若消息集合为空
				if (serverThread.messagesVector.isEmpty())
				{
					continue;
				} else
				{
					// 取第一条消息
					message = (String) serverThread.messagesVector.firstElement();
					// 在集合中移除它
					serverThread.messagesVector.removeElement(message);
					// 判断是新旧消息
					if (message.contains("@clientThread"))
					{
						// clientThread说明是刚刚登录进来
						newMessageFlag = false;
					}
				}
			}

			// 向每一个客户端发送数据信息
			synchronized (serverThread.clientThreadsVector)
			{
				// 遍历客户端线程集合
				for (int i = 0; i < serverThread.clientThreadsVector.size(); i++)
				{
					// 获取单个客户端线程
					clientThread = serverThread.clientThreadsVector.elementAt(i);

					// 如果是新消息
					if (newMessageFlag)
					{
						try
						{
							// 如果是退出
							if (message.contains("@exit"))
							{
								// 从线程集合移除自己
								serverThread.clientThreadsVector.remove(i);
								// 关闭自己
								clientThread.closeClientThread(clientThread);
								// 向客户端发消息
								clientThread.dos.writeUTF(message);
							}
							// 如果是聊天
							else if ((message.contains("@chat")) || (message.contains("@userlist"))
									|| (message.contains("@serverexit")))
							{
								// 向客户端发消息
								clientThread.dos.writeUTF(message);
							}
							// 如果是单聊
							else if (message.contains("@single"))
							{
								// message = 用户名 @single ID @single 对方ID @single 发送的消息 @single

								// 分割
								String[] info = message.split("@single");
								// 获取对方ID
								int desID = Integer.parseInt(info[2]);
								// 遍历所有客户端
								for (int j = 0; j < serverThread.clientThreadsVector.size(); j++)
								{
									// 判断
									if (desID == serverThread.clientThreadsVector.get(j).getId())
									{
										// 如果找到对方，给对方发消息
										serverThread.clientThreadsVector.get(j).dos.writeUTF(message);
										// 结束外循环
										i = serverThread.clientThreadsVector.size();
										// 结束内循环
										break;
									}
								}
							}
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					} else// 是旧消息
					{
						// 获取用户ID
						String value = serverThread.usersMap.get((int) clientThread.getId());
						// 如果是登录
						if (value.equals("@login@"))
						{
							newMessageFlag = true;
							try
							{
								// 向客户端发消息
								clientThread.dos.writeUTF(message);
								// 如果是退出
								if (message.contains("@exit"))
								{
									serverThread.clientThreadsVector.remove(i);
									clientThread.closeClientThread(clientThread);
								}
							} catch (IOException e)
							{
								e.printStackTrace();
							}

							// 结束外循环
							break;
						}
					}
				}
			}

			// 如果消息是服务器停止
			if (message.contains("@serverexit"))
			{
				// 清空用户集合
				serverThread.usersMap.clear();
				// 退出死循环
				exitFlag = false;
			}
		}
	}

	/**
	 * 设置退出标志
	 * 
	 * @param b
	 */
	public void setExitFlag(boolean b)
	{
		exitFlag = b;
	}

	/**
	 * 停止广播线程
	 */
	public void stopBroadcastThread()
	{
		exitFlag = false;
	}
}
