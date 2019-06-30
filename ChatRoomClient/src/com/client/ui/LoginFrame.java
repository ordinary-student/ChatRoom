package com.client.ui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.client.main.Client;
import com.client.utils.WindowUtil;

/**
 * 登录窗口
 * 
 * @author ordinary-student
 *
 */
public class LoginFrame extends KFrame
{
	private static final long serialVersionUID = 7808091997933662045L;
	private JTextField userNameTextField;
	private JTextField serverIpTextField;
	private JTextField serverPortTextField;
	private JButton loginButton;
	private Client client;

	/*
	 * 构造方法
	 */
	public LoginFrame(Client client)
	{
		this.client = client;

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
	private void initUI() throws UnknownHostException
	{
		// 设置标题
		setTitle("登录聊天室");
		// 设置图标
		setIconImage(Toolkit.getDefaultToolkit().getImage("res/client.png"));
		// 设置大小
		setSize(300, 250);
		// 不可改变大小
		setResizable(false);
		// 窗口居中
		WindowUtil.center(this);
		// 设置布局
		getContentPane().setLayout(null);
		// 添加监听
		addWindowListener(this);
		// 设置关闭方式
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 用户名标签
		JLabel userNameLabel = new JLabel("用户名");
		userNameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		userNameLabel.setBounds(20, 25, 100, 30);
		getContentPane().add(userNameLabel);

		// 输入框
		userNameTextField = new JTextField(InetAddress.getLocalHost().getHostAddress());
		userNameTextField.setBounds(130, 25, 150, 30);
		userNameTextField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		userNameTextField.addKeyListener(this);
		getContentPane().add(userNameTextField);

		// 服务器地址
		JLabel serverIpLabel = new JLabel("服务器地址");
		serverIpLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		serverIpLabel.setBounds(20, 65, 100, 30);
		getContentPane().add(serverIpLabel);

		serverIpTextField = new JTextField(InetAddress.getLocalHost().getHostAddress());
		serverIpTextField.setBounds(130, 65, 150, 30);
		serverIpTextField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		serverIpTextField.addKeyListener(this);
		getContentPane().add(serverIpTextField);

		// 端口号
		JLabel serverPortLabel = new JLabel("端口号");
		serverPortLabel.setBounds(20, 105, 100, 30);
		serverPortLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		getContentPane().add(serverPortLabel);

		serverPortTextField = new JTextField("5000");
		serverPortTextField.setBounds(130, 105, 150, 30);
		serverPortTextField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		serverPortTextField.addKeyListener(this);
		getContentPane().add(serverPortTextField);

		// 按钮
		loginButton = new JButton("登录");
		loginButton.setBounds(90, 165, 120, 35);
		loginButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
		loginButton.addActionListener(this);
		getContentPane().add(loginButton);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// 登录
		login();
	}

	@Override
	public void keyPressed(KeyEvent ke)
	{
		// 回车键
		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (ke.getSource() == userNameTextField)
			{
				serverIpTextField.requestFocus();
			} else if (ke.getSource() == serverIpTextField)
			{
				serverPortTextField.requestFocus();
			} else if (ke.getSource() == serverPortTextField)
			{
				// 登录
				login();
			}
		}
	}

	/**
	 * 登录
	 */
	private void login()
	{
		// 获取用户名
		String userName = userNameTextField.getText().trim();
		// 获取服务器地址
		String serverIp = serverIpTextField.getText().trim();
		// 获取端口号
		String serverPort = serverPortTextField.getText().trim();

		// 判断
		if (userName.equals(""))
		{
			JOptionPane.showMessageDialog(this, "用户名不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (serverIp.equals(""))
		{
			JOptionPane.showMessageDialog(this, "服务器地址不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (serverPort.equals(""))
		{
			JOptionPane.showMessageDialog(this, "端口号不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// 如果登录成功
		if (client.login(userName, serverIp, serverPort))
		{
			this.dispose();
			client.showChatFrame(userName);
		}
	}
}
