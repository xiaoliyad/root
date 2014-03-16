package com.watchdata.cardcheck.app;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.watchdata.cardcheck.utils.menu.FolderPane;
import com.watchdata.cardcheck.utils.menu.ListPane;

public class LeftPanel extends JPanel {

	private static final long serialVersionUID = 3433561501023042680L;
	public LeftPanel() {
		super(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		FolderPane fp = getFolderPane();
		// 添加到JScrollPane
		JScrollPane jsp = new JScrollPane(fp);
		// 为了好看，Viewport背景设置成FolderPane的背景。这儿可以自己控制
		jsp.getViewport().setBackground(fp.getBackground());
		add(jsp, BorderLayout.CENTER);

	}

	private FolderPane getFolderPane() {
		FolderPane fp = new FolderPane();
		fp.setAnimated(true);
		fp.addFolder("设置", getConfigFileFolderPane());
		fp.addFolder("交易", getTradeFileFolderPane());
		fp.addFolder("检测", getCheckFileFolderPane());
		fp.addFolder("卡片", getCardFileFolderPane());
		return fp;
	}

	private ListPane getConfigFileFolderPane() {
		ListPane p = new ListPane();
		p.addItem("AID设置", "/com/watchdata/cardcheck/resources/images/menu/drive.png");
		p.addItem("CA管理", "/com/watchdata/cardcheck/resources/images/menu/mydoc.png");
		p.addItem("应用密钥", "/com/watchdata/cardcheck/resources/images/menu/shareddoc.png");
		p.addItem("终端限制", "/com/watchdata/cardcheck/resources/images/menu/mycom.png");
		p.addItem("终端性能", "/com/watchdata/cardcheck/resources/images/menu/neighbor.png");
		p.addItem("终端类型", "/com/watchdata/cardcheck/resources/images/menu/neighbor.png");
		p.addItem("皮肤设置", "/com/watchdata/cardcheck/resources/images/menu/neighbor.png");
		p.addItem("读卡器", "/com/watchdata/cardcheck/resources/images/menu/neighbor.png");
		p.setSize(140,240);
		return p;
	}
	private ListPane getTradeFileFolderPane() {
		ListPane p = new ListPane();
		p.addItem("交易", "/com/watchdata/cardcheck/resources/images/menu/drive.png");
		p.setSize(140,30);
		return p;
	}
	private ListPane getCheckFileFolderPane() {
		ListPane p = new ListPane();
		p.addItem("检测", "/com/watchdata/cardcheck/resources/images/menu/drive.png");
		p.setSize(140,30);
		return p;
	}
	private ListPane getCardFileFolderPane() {
		ListPane p = new ListPane();
		p.addItem("卡片", "/com/watchdata/cardcheck/resources/images/menu/drive.png");
		p.setSize(140,30);
		return p;
	}
}