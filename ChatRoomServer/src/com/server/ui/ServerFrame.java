package com.server.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.server.main.Server;
import com.server.utils.WindowUtil;

/**
 * 服务器窗口
 * 
 * @author ordinary-student
 *
 */
public class ServerFrame extends KFrame implements ActionListener
{
	private static final long serialVersionUID = 699027939792350090L;
	private JButton startServerButton;
	private JButton stopServerButton;
	private JButton exitServerButton;
	private JTextArea chatMessageTextArea;
	private JList<String> userList;

	private Server server;

	public List<String> userNameOnLineList;
	public List<Integer> userIdOnLineList;

	/**
	 * 构造方法
	 * 
	 * @param server
	 */
	public ServerFrame(Server server)
	{
		this.server = server;
		// 创建在线用户集合
		userNameOnLineList = new ArrayList<String>();
		userIdOnLineList = new ArrayList<Integer>();

		try
		{
			// 初始化界面
			initUI();
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 初始化界面
	 * 
	 * @throws UnknownHostException
	 */
	public void initUI() throws UnknownHostException
	{
		// 设置标题
		setTitle("服务器" + InetAddress.getLocalHost().getHostAddress());
		// 设置图标
		setIconImage(Toolkit.getDefaultToolkit().getImage("res/server.png"));
		// 设置大小
		setSize(450, 350);
		// 不可改变大小
		setResizable(false);
		// 窗口居中
		WindowUtil.center(this);
		// 添加监听
		addWindowListener(this);
		// 设置布局
		getContentPane().setLayout(null);

		// 按钮
		startServerButton = new JButton("启动服务器");
		startServerButton.setBounds(30, 20, 105, 35);
		startServerButton.addActionListener(this);
		getContentPane().add(startServerButton);

		stopServerButton = new JButton("停止服务器");
		stopServerButton.setBounds(150, 20, 105, 35);
		stopServerButton.setEnabled(false);
		stopServerButton.addActionListener(this);
		getContentPane().add(stopServerButton);

		exitServerButton = new JButton("退出服务器");
		exitServerButton.setBounds(270, 20, 105, 35);
		exitServerButton.addActionListener(this);
		getContentPane().add(exitServerButton);

		// 消息面板
		JScrollPane messageScrollPane = new JScrollPane();
		messageScrollPane.setBounds(10, 70, 221, 230);
		messageScrollPane.setWheelScrollingEnabled(true);
		messageScrollPane.setBorder(BorderFactory.createTitledBorder("聊天消息"));
		getContentPane().add(messageScrollPane);

		// 聊天消息
		chatMessageTextArea = new JTextArea();
		messageScrollPane.setViewportView(chatMessageTextArea);

		// 用户面板
		JScrollPane userScrollPane = new JScrollPane();
		userScrollPane.setBounds(260, 70, 157, 230);
		userScrollPane.setBorder(BorderFactory.createTitledBorder("在线用户"));
		getContentPane().add(userScrollPane);

		// 在线用户列表
		userList = new JList<String>();
		userList.setVisibleRowCount(8);
		userScrollPane.setViewportView(userList);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		// 判断来源
		if (ae.getSource() == startServerButton)
		{
			// 启动服务器
			startServerButton.setEnabled(false);
			stopServerButton.setEnabled(true);
			server.startServer();
		} else if (ae.getSource() == stopServerButton)
		{
			// 停止服务器
			int result = JOptionPane.showConfirmDialog(this, "是否要停止服务器？", "", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.OK_OPTION)
			{
				server.stopServer();
				startServerButton.setEnabled(true);
				stopServerButton.setEnabled(false);
			}
		} else if (ae.getSource() == exitServerButton)
		{
			// 退出
			if (stopServerButton.isEnabled())
			{
				stopServerButton.doClick();
			}
			// 关闭服务器
			server.close();
		}
	}

	/**
	 * 显示用户
	 * 
	 * @param usersMessage
	 */
	public void displayUsers(String usersMessage)
	{
		// 如果所有用户都下线了
		if (usersMessage.equals("@userlist"))
		{
			// 移除所有
			userList.removeAll();
			String[] user_null = {};
			userList.setListData(user_null);
		} else
		{
			// 判断
			if (usersMessage.contains("@userlist"))
			{
				// 包含用户名和ID的数组
				String[] userNameAndIds = usersMessage.split("@userlist");
				// 待显示的用户名数组
				String[] userNamesToDisplay = new String[userNameAndIds.length / 2];

				int j = 0;
				// 遍历获取待显示的用户名数组
				for (int i = 0; i < userNameAndIds.length; i = i + 2)
				{
					userNamesToDisplay[j] = userNameAndIds[i];
					j = j + 1;
				}

				// 移除旧列表
				userList.removeAll();
				// 显示新列表
				userList.setListData(userNamesToDisplay);
			}

			// 如果是退出
			if (usersMessage.contains("@exit"))
			{
				String[] user_null = {};
				userList.setListData(user_null);
			}
		}
	}

	/**
	 * 显示聊天消息
	 * 
	 * @param chatMessage
	 */
	public void displayChatMessage(String chatMessage)
	{
		// 如果是聊天
		if (chatMessage.contains("@chat"))
		{
			// 获取索引
			int index = chatMessage.indexOf("@chat");
			// 消息区追加聊天消息
			chatMessageTextArea.append(chatMessage.substring(0, index) + "\r\n");
			// 光标移至最后
			chatMessageTextArea.setCaretPosition(chatMessageTextArea.getText().length());

		} else if (chatMessage.contains("@exit"))
		{
			// 如果是退出
			chatMessageTextArea.setText("");
		}
	}

	/**
	 * 设置开启服务器不可用
	 */
	public void setStartAndStopUnable()
	{
		JOptionPane.showMessageDialog(this, "不能同时开启两个服务器");
		startServerButton.setEnabled(false);
		stopServerButton.setEnabled(false);
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		exitServerButton.doClick();
	}
}
