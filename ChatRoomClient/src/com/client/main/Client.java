package com.client.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.client.ui.GroupChatFrame;
import com.client.ui.LoginFrame;
import com.client.ui.SingleChatFrame;

/**
 * 客户端线程
 * 
 * @author ordinary-student
 *
 */
public class Client extends Thread
{
	private GroupChatFrame groupChatFrame;
	private LoginFrame loginFrame;
	private SingleChatFrame singleChatFrame;
	private boolean runFlag = false;
	private int threadID;

	public Socket socket;
	public DataInputStream dis = null;
	public DataOutputStream dos = null;
	public Map<String, SingleChatFrame> singleChatFramesMap;
	public List<String> userNameOnLineList;
	public List<Integer> userIdList;
	public String username = null;
	public String message;

	/*
	 * 构造方法
	 */
	public Client()
	{
		// 单聊窗口集合
		singleChatFramesMap = new HashMap<String, SingleChatFrame>();
		// 在线用户集合
		userNameOnLineList = new ArrayList<String>();
		// 用户ID集合
		userIdList = new ArrayList<Integer>();
	}

	/**
	 * 主方法
	 * 
	 * @param args
	 * @throws UnknownHostException
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
			// 创建客户端线程
			Client client = new Client();
			// 创建登录窗口
			LoginFrame lFrame = new LoginFrame(client);
			// 设置登录窗口
			client.setLoginFrame(lFrame);
			// 设置可视
			lFrame.setVisible(true);
		}
	}

	@Override
	public void run()
	{
		while (runFlag)
		{
			try
			{
				// 读取信息
				message = dis.readUTF();
			} catch (IOException e)
			{
				e.printStackTrace();
				// 出错就停止
				runFlag = false;
				// 若不是退出信息，则清空
				if (!message.contains("@serverexit"))
				{
					message = null;
				}
			}

			// 若信息不为空
			if (message != null)
			{
				// 判断
				if (message.contains("@clientThread"))
				{
					// 刚创建的新线程
					// 获取索引
					int index = message.indexOf("@clientThread");
					// 设置线程ID
					setThreadID(Integer.parseInt(message.substring(0, index)));

					try
					{
						// 发送登陆信息
						dos.writeUTF(username + "@login" + getThreadID() + "@login");
					} catch (IOException e)
					{
						e.printStackTrace();
					}

				} else if (message.contains("@userlist"))
				{
					// 显示用户列表
					groupChatFrame.displayUsers(message);

				} else if (message.contains("@chat"))
				{
					// 显示聊天信息
					groupChatFrame.displayChatMessage(message);

				} else if (message.contains("@serverexit"))
				{
					// 关闭客户端
					groupChatFrame.closeClient();

				} else if (message.contains("@single"))
				{
					// 显示单聊窗口
					groupChatFrame.showSingleChatFrame(message);
				}
			}

		}
	}

	/**
	 * 登录方法
	 * 
	 * @param userName
	 * @param serverIp
	 * @param serverPort
	 * @return
	 */
	public boolean login(String userName, String serverIp, String serverPort)
	{
		this.username = userName;
		boolean flag = false;
		// 判断是否重名
		for (String nameOnLine : userNameOnLineList)
		{
			System.out.println(nameOnLine);
			if (userName.equals(nameOnLine))
			{
				JOptionPane.showMessageDialog(this.loginFrame, "此用户名已经被占用！", "通知", JOptionPane.INFORMATION_MESSAGE);
				flag = false;
				break;
			}
		}

		try
		{
			// 建立连接
			socket = new Socket(serverIp, Integer.parseInt(serverPort));
			flag = true;
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this.loginFrame, "端口号填写错误！", "错误", JOptionPane.ERROR_MESSAGE);
			return false;

		} catch (UnknownHostException e)
		{
			JOptionPane.showMessageDialog(this.loginFrame, "服务器地址填写错误！", "错误", JOptionPane.ERROR_MESSAGE);
			return false;

		} catch (IOException e)
		{
			JOptionPane.showMessageDialog(this.loginFrame, "连接服务器失败！", "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return flag;
	}

	/**
	 * 显示聊天窗口
	 * 
	 * @param userName
	 */
	public void showChatFrame(String userName)
	{
		// 获取通信数据流
		getDataStream();
		// 创建群聊窗口
		groupChatFrame = new GroupChatFrame(this, userName);
		// 设置可视
		groupChatFrame.setVisible(true);
		// 更改运行标志
		runFlag = true;
		// 启动客户端线程
		this.start();
	}

	/**
	 * 获取通信数据流
	 */
	private void getDataStream()
	{
		try
		{
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 传输聊天消息
	 * 
	 * @param chatMessage
	 */
	public void transferChatMessage(String chatMessage)
	{
		try
		{
			// 发送聊天消息
			dos.writeUTF(username + "@chat" + getThreadID() + "@chat" + chatMessage + "@chat");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 退出聊天
	 */
	public void exitChat()
	{
		try
		{
			// 发送退出信息
			dos.writeUTF(username + "@exit" + getThreadID() + "@exit");
			runFlag = false;
			System.exit(0);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 退出客户端
	 */
	public void exitClient()
	{
		runFlag = false;
		System.exit(0);
	}

	/***** get, set方法 *****/
	public GroupChatFrame getGroupChatFrame()
	{
		return this.groupChatFrame;
	}

	public void setGroupChatFrame(GroupChatFrame groupChatFrame)
	{
		this.groupChatFrame = groupChatFrame;
	}

	public SingleChatFrame getSingleChatFrame()
	{
		return this.singleChatFrame;
	}

	public void setSingleChatFrame(SingleChatFrame singleChatFrame)
	{
		this.singleChatFrame = singleChatFrame;
	}

	public LoginFrame getLoginFrame()
	{
		return this.loginFrame;
	}

	public void setLoginFrame(LoginFrame loginFrame)
	{
		this.loginFrame = loginFrame;
	}

	public int getThreadID()
	{
		return this.threadID;
	}

	public void setThreadID(int threadID)
	{
		this.threadID = threadID;
	}
}
