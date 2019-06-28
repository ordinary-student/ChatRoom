package com.server.thread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

/**
 * 客户端线程
 * 
 * @author ordinary-student
 *
 */
public class ClientThread extends Thread
{
	public Socket clientSocket;
	public ServerThread serverThread;
	public DataInputStream dis;
	public DataOutputStream dos;
	public String client_userID;
	// 退出标志
	private boolean exitFlag = false;

	/*
	 * 构造方法
	 */
	public ClientThread(Socket socket, ServerThread serverThread)
	{
		this.clientSocket = socket;
		this.serverThread = serverThread;
		try
		{
			// 客户端通信数据流
			this.dis = new DataInputStream(clientSocket.getInputStream());
			this.dos = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		while (exitFlag)
		{
			try
			{
				// 读取消息
				String message0 = dis.readUTF();
				String message = message0;

				// 判断
				if (message.contains("@login"))
				{
					// 登录
					message = login(message);

				} else if (message.contains("@exit"))
				{
					// 退出
					message = exit(message);

				} else if (message.contains("@chat"))
				{
					// 群聊
					message = groupChat(message);

				} else if (message.contains("@single"))
				{
					// 单聊
					singleChat(message);
				}

				// 添加信息进集合
				synchronized (serverThread.messagesVector)
				{
					if (message != null)
					{
						serverThread.messagesVector.addElement(message);
					}
				}

				// 退出
				if (message0.contains("@exit"))
				{
					// 关闭通信
					this.clientSocket.close();
					exitFlag = false;
				}

			} catch (IOException e)
			{
			}
		}
	}

	/**
	 * 登录
	 * 
	 * @param message
	 */
	private String login(String message)
	{
		// message = 用户名 @login 线程ID @login

		// 分割
		String[] userInfo = message.split("@login");
		// 获取用户名
		String userName = userInfo[0];
		// 获取ID
		int userID = Integer.parseInt(userInfo[1]);

		// 移除
		serverThread.usersMap.remove(userID);

		// 若列表中包含相同用户名
		if (serverThread.usersMap.containsValue(userName))
		{
			// 遍历客户端线程集合
			for (int i = 0; i < serverThread.clientThreadsVector.size(); i++)
			{
				// 获取线程ID
				int id = (int) serverThread.clientThreadsVector.get(i).getId();
				// 获取用户名
				String name = serverThread.usersMap.get(id);
				// 若有用户名相同，改名
				if (name.equals(userName))
				{
					// 先移除
					serverThread.usersMap.remove(id);
					// 在用户名后面加上ID，重新加入集合
					serverThread.usersMap.put(id, userName + "_" + id);
					break;
				}
			}

			// 现在新的也在用户名后面加上ID
			serverThread.usersMap.put(userID, userName + "_" + userID);
		} else
		{
			// 若列表中没有相同的用户名，则直接加入
			serverThread.usersMap.put(userID, userName);
		}

		// 新建字符串
		StringBuffer sb = new StringBuffer();

		// 写消息
		synchronized (serverThread.clientThreadsVector)
		{
			// 遍历客户端线程集合
			for (int i = 0; i < serverThread.clientThreadsVector.size(); i++)
			{
				// 获取线程ID
				int threadID = (int) serverThread.clientThreadsVector.elementAt(i).getId();
				// 加入用户名
				sb.append((String) serverThread.usersMap.get(new Integer(threadID)) + "@userlist");
				// 加入线程ID
				sb.append(threadID + "@userlist");
				// message = 用户名 @userlist 线程ID @userlist
			}
		}

		// 包含所有用户名的字符串
		String usersMessage = new String(sb);
		// 服务器显示用户在线
		serverThread.serverFrame.displayUsers(usersMessage);

		// 返回
		return usersMessage;
	}

	/**
	 * 退出
	 * 
	 * @param message
	 */
	private String exit(String message)
	{
		// message = 用户名 @exit 线程ID @exit

		// 分割
		String[] userInfo = message.split("@exit");
		// 获取ID
		int userID = Integer.parseInt(userInfo[1]);
		// 移除线程
		serverThread.usersMap.remove(userID);

		// 创建新字符串
		StringBuffer sb = new StringBuffer();

		// 写消息
		synchronized (serverThread.clientThreadsVector)
		{
			// 遍历客户端线程集合
			for (int i = 0; i < serverThread.clientThreadsVector.size(); i++)
			{
				// 获取线程ID
				int threadID = (int) serverThread.clientThreadsVector.elementAt(i).getId();
				// 若相同则移除
				if (userID == threadID)
				{
					serverThread.clientThreadsVector.removeElementAt(i);
					i--;
				} else
				{
					// 不同
					// 加入用户名
					sb.append((String) serverThread.usersMap.get(new Integer(threadID)) + "@userlist");
					// 加入线程ID
					sb.append(threadID + "@userlist");
					// message = 用户名 @userlist 线程ID @userlist
				}
			}
		}

		// 包含所有用户名的字符串
		String usersMessage = new String(sb);
		// 若为空说明所有用户都下线了
		if (usersMessage.equals(""))
		{
			serverThread.serverFrame.displayUsers("@userlist");
		} else
		{
			serverThread.serverFrame.displayUsers(usersMessage);
		}

		// 返回
		return usersMessage;
	}

	/**
	 * 群聊
	 * 
	 * @param message
	 */
	private String groupChat(String message)
	{
		// message = 用户名 @chat 线程ID @chat 聊天信息 @chat

		// 分割
		String[] chat = message.split("@chat");
		// 聊天信息
		StringBuffer sb = new StringBuffer();
		// 时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		// 格式化时间
		String date = format.format(new Date());

		// 写消息
		sb.append(chat[0] + "  " + date + "\r\n");
		sb.append(chat[2] + "@chat");

		// 显示聊天消息
		String chatMessage = new String(sb);
		serverThread.serverFrame.displayChatMessage(chatMessage);

		// 返回
		return chatMessage;
	}

	/**
	 * 单聊
	 * 
	 * @param message
	 */
	private void singleChat(String message)
	{
	}

	/**
	 * 关闭客户端线程
	 * 
	 * @param clientThread
	 */
	public void closeClientThread(ClientThread clientThread)
	{
		if (clientThread.clientSocket != null)
		{
			try
			{
				clientThread.clientSocket.close();
			} catch (IOException e)
			{
				JOptionPane.showMessageDialog(serverThread.serverFrame, "客户端连接为空！", "错误", JOptionPane.ERROR_MESSAGE);
			}
		}

		// 设置退出标志
		setExitFlag(false);
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
}
