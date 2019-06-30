package com.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import com.client.main.Client;
import com.client.utils.WindowUtil;

/**
 * 群聊窗口
 * 
 * @author ordinary-student
 *
 */
public class GroupChatFrame extends KFrame
{
	private static final long serialVersionUID = 6153007230054255636L;
	private JTextField inputMessageTextField;
	private JTextArea chatMessageRecordTextArea;
	private JButton sendMessageButton;

	private JList<String> usersOnLineList;
	private JScrollPane usersOnLineScrollPanel;
	private Client client;

	// 右键弹出菜单
	private JPopupMenu popupMenu;
	private JMenuItem popupMenu_clear;
	private JMenuItem popupMenu_singleChat;

	/*
	 * 构造方法
	 */
	public GroupChatFrame(Client client, String title)
	{
		this.client = client;
		// 初始化界面
		initUI(title);
	}


	/**
	 * 初始化界面
	 */
	private void initUI(String title)
	{
		// 设置标题
		setTitle("欢迎" + title);
		// 设置图标
		setIconImage(Toolkit.getDefaultToolkit().getImage("res/client.png"));
		// 设置大小
		setSize(400, 300);
		// 限制窗口大小
		setMinimumSize(new Dimension(400, 300));
		// 窗口居中
		WindowUtil.center(this);
		// 设置布局
		getContentPane().setLayout(new BorderLayout(5, 5));
		// 添加监听
		addWindowListener(this);

		// 聊天消息面板
		JScrollPane messageScrollPane = new JScrollPane();
		messageScrollPane.setBorder(BorderFactory.createTitledBorder("聊天消息"));
		messageScrollPane.setPreferredSize(new Dimension(300, 200));
		messageScrollPane.setWheelScrollingEnabled(true);
		getContentPane().add(messageScrollPane, BorderLayout.CENTER);

		// 聊天消息记录
		chatMessageRecordTextArea = new JTextArea();
		chatMessageRecordTextArea.setEditable(false);
		chatMessageRecordTextArea.addMouseListener(this);
		messageScrollPane.setViewportView(chatMessageRecordTextArea);

		// 在线用户面板
		usersOnLineScrollPanel = new JScrollPane();
		usersOnLineScrollPanel.setBorder(BorderFactory.createTitledBorder("在线用户"));
		usersOnLineScrollPanel.setPreferredSize(new Dimension(150, 200));
		getContentPane().add(usersOnLineScrollPanel, BorderLayout.EAST);

		// 在线用户列表
		usersOnLineList = new JList<String>();
		usersOnLineList.setSelectedIndex(0);
		usersOnLineList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		usersOnLineList.addListSelectionListener(this);
		usersOnLineList.addMouseListener(this);
		usersOnLineScrollPanel.setViewportView(usersOnLineList);

		// 输入面板
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(BorderFactory.createTitledBorder("发送消息"));
		inputPanel.setLayout(new BorderLayout(5, 5));

		// 消息输入框
		inputMessageTextField = new JTextField();
		inputMessageTextField.setPreferredSize(new Dimension(250, 30));
		inputMessageTextField.addKeyListener(this);
		inputPanel.add(inputMessageTextField, BorderLayout.CENTER);

		// 发送按钮
		sendMessageButton = new JButton("发送");
		sendMessageButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
		sendMessageButton.setPreferredSize(new Dimension(100, 30));
		sendMessageButton.setFocusPainted(false);
		sendMessageButton.addActionListener(this);
		inputPanel.add(sendMessageButton, BorderLayout.EAST);

		getContentPane().add(inputPanel, BorderLayout.SOUTH);

		// 右键菜单
		popupMenu = new JPopupMenu();
		// 清空聊天记录-右键菜单项
		popupMenu_clear = new JMenuItem("清空聊天记录");
		popupMenu_clear.setFocusPainted(false);
		popupMenu_clear.addActionListener(this);
		popupMenu.add(popupMenu_clear);
		// 单聊
		popupMenu_singleChat = new JMenuItem("单聊");
		popupMenu_singleChat.setFocusPainted(false);
		popupMenu_singleChat.addActionListener(this);
		popupMenu.add(popupMenu_singleChat);

		validate();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// 判断来源
		if (e.getSource() == popupMenu_clear)
		{
			// 清空聊天记录
			chatMessageRecordTextArea.setText("");

		} else if (e.getSource() == sendMessageButton)
		{
			// 发送消息
			sendMessage();

		} else if (e.getSource() == popupMenu_singleChat)
		{
			// 获取选择的用户名
			String userName = (String) usersOnLineList.getSelectedValue();

			if (!client.singleChatFramesMap.containsKey(userName))
			{
				// 如果没有单聊窗口就创建
				createSingleChatFrame(userName);
			} else
			{
				// 有就激活它
				client.singleChatFramesMap.get(userName).setFocusableWindowState(true);
			}
		}
	}

	/**
	 * 发送消息
	 */
	private void sendMessage()
	{
		// 获取待发消息
		String mess = inputMessageTextField.getText();
		// 清空输入框
		inputMessageTextField.setText("");
		// 不为空才发送
		if (!mess.equals(""))
		{
			client.transferChatMessage(mess);
		}
	}

	@Override
	public void keyReleased(KeyEvent ke)
	{
		// 回车发送
		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (ke.getSource() == inputMessageTextField)
			{
				// 发送消息
				sendMessage();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		// 判断来源
		if (e.getSource() == usersOnLineList)
		{
			// 选择不为空
			if (usersOnLineList.getSelectedValue() != null)
			{
				// 获取用户名
				String name = (String) usersOnLineList.getSelectedValue();
				// 更改菜单项文字
				popupMenu_singleChat.setText("与" + name + "单聊");
			}
		}
	}

	/**
	 * 显示聊天消息
	 * 
	 * @param message
	 */
	public void displayChatMessage(String message)
	{
		// 分割获取聊天消息
		int index = message.indexOf("@chat");
		// 显示
		chatMessageRecordTextArea.append(message.substring(0, index) + "\n");
		chatMessageRecordTextArea.setCaretPosition(chatMessageRecordTextArea.getText().length());
	}

	/**
	 * 显示用户列表
	 * 
	 * @param message
	 */
	public void displayUsers(String message)
	{
		// 分割
		String[] mess = message.split("@userlist");
		// 在线用户数组
		String[] usersOnLine = new String[mess.length / 2];
		// 遍历
		for (int i = 1; i < mess.length; i++)
		{
			// 获取ID
			int userID = Integer.parseInt(mess[i]);
			// 判断
			if (client.getThreadID() == userID)
			{
				if (!client.username.equals(mess[i - 1]))
				{
					JOptionPane.showMessageDialog(this, "由于有同名的用户登录，所以在您的用户名后面加上了编号");
					client.username = mess[i - 1];
					this.setTitle("欢迎" + client.username);
					break;
				} else
				{
					break;
				}
			} else
			{
				i++;
			}

		}

		//
		if (mess.length == 2)
		{
			String[] s = new String[] {};

			// 集合不为空
			if (!client.singleChatFramesMap.isEmpty())
			{
				// 遍历所有在线用户
				ListModel<String> list = usersOnLineList.getModel();
				// 告知对方我已离线
				for (int i = 0; i < list.getSize(); i++)
				{
					if (client.singleChatFramesMap.get(list.getElementAt(i)) != null)
					{
						// 离线通知
						client.singleChatFramesMap.get(list.getElementAt(i)).offlineNotification();
					}
				}
			}

			// 清空自己的列表
			usersOnLineList.removeAll();
			usersOnLineList.setListData(s);

		} else
		{
			if ((mess.length / 2 - 1) < client.userNameOnLineList.size())
			{
				// 有用户下线
				List<String> rec = new ArrayList<String>();
				// 获取最新用户列表
				int i = 0;
				for (; i < mess.length; i++)
				{
					// 加入最新用户列表
					rec.add(0, mess[i++]);
				}

				// 比较最新列表与在线列表
				for (i = 0; i < client.userNameOnLineList.size(); i++)
				{
					// 最新列表里没有，说明他已下线
					if (!rec.contains(client.userNameOnLineList.get(i)))
					{
						break;
					}
				}

				// 获取名字
				String name = client.userNameOnLineList.get(i);
				// 从在线列表移除
				client.userNameOnLineList.remove(i);
				client.userIdList.remove(i);

				// 关闭单聊窗口
				if (client.singleChatFramesMap.containsKey(name))
				{
					client.singleChatFramesMap.get(name).closeSingleChatFrame();
					client.singleChatFramesMap.remove(name);
				}

			} else
			{
				// 有用户上线
				List<Integer> online = new ArrayList<Integer>();

				for (int i = 0; i < client.userNameOnLineList.size(); i++)
				{
					online.add(0, client.userIdList.get(i));
				}

				// 如果在线列表为空
				if (online.isEmpty())
				{
					// 加上去
					for (int i = 1; i < mess.length; i++)
					{
						if ((int) Integer.parseInt(mess[i]) != client.getThreadID())
						{
							client.userNameOnLineList.add(0, mess[i - 1]);
							client.userIdList.add(0, Integer.parseInt(mess[i]));
						}
						i++;
					}
				} else
				{
					for (int i = 1; i < mess.length; i++)
					{
						if (Integer.parseInt(mess[i]) != client.getThreadID())
						{
							if (!online.contains(Integer.parseInt(mess[i])))
							{
								client.userNameOnLineList.add(0, mess[i - 1]);
								client.userIdList.add(0, Integer.parseInt(mess[i]));
							} else
							{
								String name = client.userNameOnLineList
										.get(client.userIdList.indexOf(Integer.parseInt(mess[i])));
								if (!name.equals(mess[i - 1]))
								{
									// 若单聊窗口已存在
									if (client.singleChatFramesMap.containsKey(name))
									{
										// 重新创建
										SingleChatFrame cf = client.singleChatFramesMap.get(name);
										cf.setTitle(name);
										// 移除旧的
										client.singleChatFramesMap.remove(name);
										// 添加新的
										client.singleChatFramesMap.put(name, cf);
										cf.setVisible(false);
									}

									// 移除旧的
									client.userNameOnLineList.remove(name);
									client.userIdList.remove(new Integer(Integer.parseInt(mess[i])));
									// 添加新的
									client.userNameOnLineList.add(0, mess[i - 1]);
									client.userIdList.add(0, Integer.parseInt(mess[i]));
								}
							}
						}
						i++;
					}
				}

			}

			for (int i = 0; i < client.userNameOnLineList.size(); i++)
			{
				usersOnLine[i] = client.userNameOnLineList.get(i);
			}

			usersOnLineList.removeAll();
			usersOnLineList.setListData(usersOnLine);
		}
	}

	/**
	 * 关闭客户端
	 */
	public void closeClient()
	{
		JOptionPane.showMessageDialog(this, "服务器已关闭！", "提示", JOptionPane.OK_OPTION);
		client.exitClient();
	}

	/**
	 * 创建单聊窗口
	 * 
	 * @param name
	 */
	private void createSingleChatFrame(String userName)
	{
		// 创建单聊窗口
		SingleChatFrame singleChatFrame = new SingleChatFrame(client, userName);
		// 加入单聊窗口集合
		client.singleChatFramesMap.put(userName, singleChatFrame);

		try
		{
			// 设置ID
			singleChatFrame.userThreadID = client.userIdList.get(client.userNameOnLineList.indexOf(userName));
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// 设置可视
		singleChatFrame.setVisible(true);
	}

	/**
	 * 显示单聊窗口
	 * 
	 * @param message
	 */
	public void showSingleChatFrame(String message)
	{
		// message = 用户名 @single ID @single 对方ID @single 聊天消息 @single
		// 分割
		String[] mess = message.split("@single");
		// 获取用户名
		String userName = mess[0];
		// 获取聊天消息
		String chatMessage = mess[3];

		try
		{
			// 有单聊窗口就显示聊天消息
			if (client.singleChatFramesMap.containsKey(userName))
			{
				client.singleChatFramesMap.get(userName).appendChatMessage(chatMessage);
			} else
			{
				// 没有就创建后再显示聊天消息
				createSingleChatFrame(userName);
				client.singleChatFramesMap.get(userName).appendChatMessage(chatMessage);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// 显示右键菜单
		showPopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// 显示右键菜单
		showPopupMenu(e);
	}

	/*
	 * 显示右键菜单
	 */
	private void showPopupMenu(MouseEvent e)
	{
		// 判断是否触发弹出菜单事件
		if (e.isPopupTrigger())
		{
			// 判断来源
			if (e.getSource() == usersOnLineList)
			{
				popupMenu_clear.setVisible(false);
				// 选择不为空
				if (usersOnLineList.getSelectedValue() != null)
				{
					popupMenu_singleChat.setVisible(true);
					// 显示弹出菜单
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			} else
			{
				popupMenu_singleChat.setVisible(false);
				popupMenu_clear.setVisible(true);
				// 显示弹出菜单
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		// 退出
		exit();
	}

	/**
	 * 退出方法
	 */
	public void exit()
	{
		this.dispose();
		client.exitChat();
	}
}
