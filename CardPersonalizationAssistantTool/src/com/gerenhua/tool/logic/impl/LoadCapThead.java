package com.gerenhua.tool.logic.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JTextPane;

import com.gerenhua.tool.log.Log;
import com.gerenhua.tool.logic.Constants;
import com.gerenhua.tool.logic.apdu.CommonAPDU;
import com.gerenhua.tool.panel.CardInfoDetectPanel;
import com.gerenhua.tool.utils.Config;
import com.watchdata.commons.lang.WDByteUtil;
import com.watchdata.commons.lang.WDStringUtil;

public class LoadCapThead extends Thread {
	public static CommonAPDU commonAPDU;
	public static Log log = new Log();
	public JTextPane textPane;
	public File[] capFiles;
	public boolean isRealCard = false;

	public boolean isRealCard() {
		return isRealCard;
	}

	public void setRealCard(boolean isRealCard) {
		this.isRealCard = isRealCard;
	}

	public LoadCapThead(File[] file, CommonAPDU commonAPDU, JTextPane textPane) {
		this.capFiles = file;
		this.commonAPDU = commonAPDU;
		this.textPane = textPane;
	}

	@Override
	public void run() {
		log.setLogArea(textPane);
		textPane.setText("");
		for (File file : capFiles) {
			try {
				String resp = "";
				List<String> loadFileInfo = getCapInfo(file);
				System.out.println(loadFileInfo);
				String pkgName = loadFileInfo.get(0);
				String apduCommand = WDStringUtil.paddingHeadZero(Integer.toHexString(pkgName.length() / 2), 2) + pkgName;
				apduCommand += "00000000";
				apduCommand = WDStringUtil.paddingHeadZero(Integer.toHexString(apduCommand.length() / 2), 2) + apduCommand;
				apduCommand = "80E60200" + apduCommand.toUpperCase();
				if (isRealCard) {
					resp = commonAPDU.send(apduCommand);
				} else {
					log.out(formatLoadScript(apduCommand, "//INSTALL [for load]"), Log.LOG_COLOR_BLACK);
					resp = "9000";
				}
				if (resp.endsWith(Constants.SW_SUCCESS)) {
					for (int j = 1; j < loadFileInfo.size(); j++) {
						String p1 = "";
						if (j == loadFileInfo.size() - 1) {
							p1 = "80";
						} else {
							p1 = "00";
						}
						String p2 = "";
						if (j <= 256) {
							p2 = WDStringUtil.paddingHeadZero(Integer.toHexString(j - 1), 2);
						} else {
							p2 = WDStringUtil.paddingHeadZero(Integer.toHexString(j - 256 - 1), 2);
						}

						String lc = WDStringUtil.paddingHeadZero(Integer.toHexString(loadFileInfo.get(j).length() / 2), 2);
						String temp = "80E8" + p1 + p2 + lc;
						temp += loadFileInfo.get(j);
						temp = temp.toUpperCase();
						if (isRealCard) {
							resp = commonAPDU.send(temp);
						} else {
							log.out(formatLoadScript(temp, "//LOAD [for " + j + " Block]"), Log.LOG_COLOR_BLACK);
							resp = "9000";
						}
						if (!resp.endsWith(Constants.SW_SUCCESS)) {
							break;
						}
					}
					String msg = "load " + file.getName() + " complete.";
					if (isRealCard) {
						log.info(msg);
					}else {
						log.info("complete.");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				Config.setValue("CardInfo", "currentCap", capFiles[0].getParent());
			}
		}
		if (isRealCard) {
			CardInfoDetectPanel.refreshTree();
		}
	}

	public List<String> getCapInfo(File file) throws IOException {
		Map<String, String> mapBean = new HashMap<String, String>();
		List<String> loadFileInfo = new ArrayList<String>();
		String zipFileName = file.getPath();
		// read cap pkg
		mapBean = readZipFile(zipFileName);

		loadFileInfo.add(mapBean.get("head"));
		String capFileInfo = mapBean.get("body");

		int len = capFileInfo.length();

		System.out.println(capFileInfo);
		System.out.println(len);

		String lenInHex = Integer.toHexString(len / 2);

		capFileInfo = WDStringUtil.paddingHeadZero(lenInHex, 4) + capFileInfo;
		if (len > 128 && len < 255) {
			capFileInfo = "81" + capFileInfo;
		} else if (len > 255) {
			capFileInfo = "82" + capFileInfo;
		}
		capFileInfo = "C4" + capFileInfo;

		int count = -1;
		len = capFileInfo.length();
		if (len % 320 == 0) {
			count = len / 320;
		} else {
			count = len / 320 + 1;
		}
		int pos = 0;
		for (int i = 0; i < count; i++) {
			if (pos + 320 > capFileInfo.length()) {
				loadFileInfo.add(capFileInfo.substring(pos, capFileInfo.length()));
				pos += capFileInfo.length() - pos;
			} else {
				loadFileInfo.add(capFileInfo.substring(pos, pos + 320));
				pos += 320;
			}
		}

		return loadFileInfo;
	}

	// read pkg cap
	public Map<String, String> readZipFile(String zipFileName) throws IOException {
		Map<String, String> mapBean = new HashMap<String, String>();
		StringBuffer sb = new StringBuffer();

		ZipFile zipFile = new ZipFile(zipFileName);
		Enumeration en = zipFile.entries();
		do {
			// 清空
			sb.setLength(0);

			if (!en.hasMoreElements())
				break;
			ZipEntry ze = (ZipEntry) en.nextElement();
			String zeName = ze.getName();

			if (!zeName.equals("META-INF/MANIFEST.MF")) {
				InputStream in = zipFile.getInputStream(ze);
				byte buf[] = new byte[10240];
				do {
					int length = in.read(buf, 0, buf.length);
					if (length != -1) {
						sb.append(WDByteUtil.bytes2HEX(buf, 0, length));
					} else {
						break;
					}
				} while (true);
				zeName = zeName.substring(zeName.lastIndexOf("/") + 1);
				mapBean.put(zeName, sb.toString());
			}
		} while (true);
		zipFile.close();

		sb.setLength(0);
		sb.append(checkNull(mapBean.get("Header.cap")));
		sb.append(checkNull(mapBean.get("Directory.cap")));
		sb.append(checkNull(mapBean.get("Import.cap")));
		sb.append(checkNull(mapBean.get("Applet.cap")));
		sb.append(checkNull(mapBean.get("Class.cap")));
		sb.append(checkNull(mapBean.get("Method.cap")));
		sb.append(checkNull(mapBean.get("StaticField.cap")));
		sb.append(checkNull(mapBean.get("Export.cap")));
		sb.append(checkNull(mapBean.get("ConstantPool.cap")));
		sb.append(checkNull(mapBean.get("RefLocation.cap")));

		String headInfo = mapBean.get("Header.cap");
		int pkgLen = Integer.parseInt(headInfo.substring(24, 26), 16);

		mapBean.clear();
		mapBean.put("head", headInfo.substring(26, 26 + pkgLen * 2));
		mapBean.put("body", sb.toString());

		return mapBean;
	}

	public String checkNull(String str) {
		return str == null ? "" : str;
	}

	/**
	 * formatLoadScript
	 * 
	 * @param apdu
	 * @param desc
	 * @return
	 */
	public String formatLoadScript(String apdu, String desc) {
		StringBuilder sb = new StringBuilder();
		sb.append(desc).append("\n");
		sb.append(apdu).append("\n");
		return sb.toString();
	}
}
