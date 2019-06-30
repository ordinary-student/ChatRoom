package com.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.client.main.Client;
import com.client.utils.WindowUtil;

/**
 * 单聊窗口
 * 
 * @author ordinary-student
 *
 */
public class SingleChatFrame extends KFrame
{
	private static final long serialVersionUID = -495048197783604523L;
	private static JTextArea chatRecordTextArea;
	private JTextField inputMessage;
	private JButton sendButton;

	public int userThreadID = 0;

	private Client client;

	/*
	 * 构造方法
	 */
	public SingleChatFrame(Client client, String title)
	{
		this.client = client;
		// 初始化界面
		initUI(title);
	}

	/*
	 * 初始化界面
	 */
	private void initUI(String title)
	{
		// 设置标题
		setTitle(title);
		// 设置图标
		setIconImage(Toolkit.getDefaultToolkit().getImage("res/client.png"));
		// 设置大小
		setSize(400, 400);
		// 限制大小
		setMinimumSize(new Dimension(400, 300));
		// 设置居中
		WindowUtil.center(this);
		// 设置布局
		getContentPane().setLayout(new BorderLayout(5, 5));
		// 添加监听
		addWindowListener(this);

		// 聊天记录面板
		JPanel chatRecordPanel = new JPanel();
		chatRecordPanel.setBorder(BorderFactory.createTitledBorder("聊天记录"));
		chatRecordPanel.setLayout(new BorderLayout(5, 5));

		// 聊天记录区
		chatRecordTextArea = new JTextArea();
		chatRecordTextArea.setEditable(false);
		chatRecordPanel.add(new JScrollPane(chatRecordTextArea), BorderLayout.CENTER);

		// 添加聊天记录面板
		getContentPane().add(chatRecordPanel, BorderLayout.CENTER);

		// 输入面板
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(BorderFactory.createTitledBorder("发送消息"));
		inputPanel.setLayout(new BorderLayout(5, 5));

		// 输入框
		inputMessage = new JTextField();
		inputMessage.addKeyListener(this);
		inputPanel.add(inputMessage, BorderLayout.CENTER);

		// 发送按钮
		sendButton = new JButton("发送");
		sendButton.setFocusPainted(false);
		sendButton.addActionListener(this);
		inputPanel.add(sendButton, BorderLayout.EAST);

		// 添加
		getContentPane().add(inputPanel, BorderLayout.SOUTH);
	}

	@Override
	public void keyPressed(KeyEvent ke)
	{
		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (ke.getSource() == inputMessage)
			{
				sendButton.doClick();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == sendButton)
		{
			// 发送消息
			sendMessage();
		}
	}

	/*
	 * 发送消息
	 */
	private void sendMessage()
	{
		// 获取聊天消息
		String chatMessage = inputMessage.getText();
		// 清空输入区
		inputMessage.setText("");

		// 不为空才发送
		if (!chatMessage.equals(""))
		{
			// 格式化时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
			String date = format.format(new Date());

			// 消息
			String message = client.username + "  " + date + "\r\n" + chatMessage;
			// 记录区追加消息
			chatRecordTextArea.append(message + "\r\n");
			// 光标移至最后
			chatRecordTextArea.setCaretPosition(chatRecordTextArea.getText().length());

			// 获取索引
			int index = client.userNameOnLineList.indexOf(this.getTitle());
			// 向服务器发送的消息
			String messageToServer = client.username + "@single" + client.getThreadID() + "@single"
					+ (int) client.userIdList.get(index) + "@single" + message + "@single";
			try
			{
				// 发送
				client.dos.writeUTF(messageToServer);
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		// 关闭单聊窗口
		closeSingleChatFrame();
	}

	/**
	 * 追加聊天消息
	 * 
	 * @param chatMessage
	 */
	public void appendChatMessage(String chatMessage)
	{
		// 记录区追加
		chatRecordTextArea.append(chatMessage + "\n");
		// 光标移至最后
		chatRecordTextArea.setCaretPosition(chatRecordTextArea.getText().length());
	}

	/**
	 * 关闭单聊窗口
	 */
	public void closeSingleChatFrame()
	{
		// 从集合中移除
		client.singleChatFramesMap.remove(this.getTitle());
		// 关闭
		this.dispose();
	}

	/**
	 * 离线通知
	 */
	public void offlineNotification()
	{
		String title = this.getTitle() + "(离线)";
		this.setTitle(title);
		sendButton.setEnabled(false);
	}
}
