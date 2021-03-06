package com.gerenhua.tool.logic.apdu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gerenhua.tool.logic.Constants;
import com.gerenhua.tool.logic.apdu.board.BoardChannel;
import com.gerenhua.tool.logic.apdu.pcsc.PcscChannel;
import com.gerenhua.tool.logic.apdu.readerx.ReaderXChannel;
import com.gerenhua.tool.utils.Config;
import com.gerenhua.tool.utils.FileUtil;
import com.watchdata.commons.crypto.WD3DesCryptoUtil;
import com.watchdata.commons.crypto.pboc.WDPBOCUtil;
import com.watchdata.commons.jce.JceBase.Padding;
import com.watchdata.commons.lang.WDAssert;
import com.watchdata.commons.lang.WDStringUtil;

/**
 * 
 * @description: 普通指令模块封装
 * @author: juan.jiang 2012-3-21
 * @version: 1.0.0
 * @modify:
 * @Copyright: watchdata
 */
public class CommonAPDU extends AbstractAPDU {
	public static IAPDUChannel apduChannel;
	private static String secureityLevel = "00";
	private static String encKey;
	private static String macKey;
	private static String kekKey;
	private static String smac;
	private static String initResp;
	public final String NO_SECUREITY_LEVEL = "00";
	public final String MACONLY = "01";
	public final String MACENC = "03";

	public String getInitResp() {
		return initResp;
	}

	public void setInitResp(String initResp) {
		this.initResp = initResp;
	}

	public CommonAPDU() {
		// apduChannel = new PcscChannel();
		// log.setLogArea(TradePanel.textPane);
	}

	public String getKekKey() {
		return kekKey;
	}

	public void setKekKey(String kekKey) {
		this.kekKey = kekKey;
	}

	public String getSmac() {
		return smac;
	}

	public void setSmac(String smac) {
		this.smac = smac;
	}

	public String getEncKey() {
		return encKey;
	}

	public void setEncKey(String encKey) {
		this.encKey = encKey;
	}

	public String getMacKey() {
		return macKey;
	}

	public void setMacKey(String macKey) {
		this.macKey = macKey;
	}

	public String getSecureityLevel() {
		return secureityLevel;
	}

	public void setSecureityLevel(String secureityLevel) {
		this.secureityLevel = secureityLevel;
	}

	public boolean init(String readerName) {
		if (readerName.indexOf(":") > 0) {
			apduChannel = new BoardChannel();
		} else if (readerName.startsWith("USB")) {
			apduChannel = new ReaderXChannel();
		} else {
			apduChannel = new PcscChannel();
		}
		return apduChannel.init(readerName);
	}

	/**
	 * 复位指令
	 * 
	 * @return
	 */
	public HashMap<String, String> reset() {
		HashMap<String, String> res = new HashMap<String, String>();

		String reader = Config.getValue("Terminal_Data", "reader");
		if (reader.indexOf(':') > 0) {
			String[] board = reader.split(":");
			FileUtil.updateBoradFile(board[0], board[1]);
		}
		res = reset(reader);
		return res;
	}

	/**
	 * 复位指令
	 * 
	 * @return
	 */
	public HashMap<String, String> reset(String reader) {
		HashMap<String, String> res = new HashMap<String, String>();
		close();
		boolean flag = init(reader);

		if (flag) {
			String responseApdu = apduChannel.reset();
			String sw = responseApdu.substring(responseApdu.length() - 4, responseApdu.length());
			String atr = responseApdu.substring(0, responseApdu.length() - 4);
			if (("9000").equals(sw)) {
				res.put("sw", "9000");
				res.put("atr", atr);
			} else {
				res.put("sw", sw);
			}
		}
		return res;
	}

	/**
	 * 选择指令
	 * 
	 * @param name
	 *            文件或目录名
	 * @return
	 */
	public HashMap<String, String> select(String name) {
		HashMap<String, String> result = new HashMap<String, String>();
		String commandApdu = packApdu("SELECT", name);
		String responseApdu = apduChannel.send(commandApdu);
		result = unpackApdu(responseApdu);
		result.put("apdu", commandApdu);
		result.put("res", responseApdu.substring(0, responseApdu.length() - 4));
		return result;
	}

	/**
	 * 读记录指令
	 * 
	 * @param sfi
	 *            短文件标识
	 * @param record
	 *            记录号
	 * @return
	 */
	public HashMap<String, String> readRecord(String sfi, String record) {
		HashMap<String, String> result = new HashMap<String, String>();
		String dgiHead = CommonHelper.getDgiHead(sfi);
		log.debug("--------------------------------------------------------------" + dgiHead + record);
		String commandApdu = packApdu("READ_RECORD", "", record, sfi);
		String responseApdu = apduChannel.send(commandApdu);

		result = unpackApdu(responseApdu);

		result.put("apdu", commandApdu);
		result.put("res", responseApdu.substring(0, responseApdu.length() - 4));
		return result;

	}

	/**
	 * 读记录指令
	 * 
	 * @param sfi
	 *            短文件标识
	 * @param record
	 *            记录号
	 * @return
	 */
	public HashMap<String, String> readRecordCommon(String sfi, String record) {
		HashMap<String, String> result = new HashMap<String, String>();
		String dgiHead = CommonHelper.getDgiHead(sfi);
		log.debug("--------------------------------------------------------------" + dgiHead + record);
		String commandApdu = packApdu("READ_RECORD", "", record, sfi);
		String responseApdu = apduChannel.send(commandApdu);

		String sw = responseApdu.substring(responseApdu.length() - 4);
		result.put("sw", sw);
		// if (dgiHead.equalsIgnoreCase("0B")) {
		if (!responseApdu.substring(0, 2).equals("70")) {
			result.put("res", responseApdu);
		} else {
			result = unpackApdu(responseApdu);
		}
		result.put("apdu", commandApdu);
		result.put("res", responseApdu.substring(0, responseApdu.length() - 4));
		return result;

	}

	/**
	 * 读目录指令
	 * 
	 * @param sfi
	 * @return
	 */
	public List<HashMap<String, String>> readDir(String sfi) {
		List<HashMap<String, String>> dirList = new ArrayList<HashMap<String, String>>();
		int b = Integer.parseInt(sfi);
		b = (b << 3) + 4;
		int index = Integer.parseInt(sfi);
		String responseApdu = "";
		while (true) {
			String commandApdu = packApdu("READ_RECORD", "", WDStringUtil.paddingHeadZero(String.valueOf(index), 2), WDStringUtil.paddingHeadZero(Integer.toHexString(b), 2));
			responseApdu = apduChannel.send(commandApdu);
			if (Constants.SW_SUCCESS.equalsIgnoreCase(responseApdu.substring(responseApdu.length() - 4))) {
				dirList.add(unpackApdu(responseApdu));
			} else {
				break;
			}
			index++;
		}

		return dirList;
	}

	/**
	 * 读目录指令
	 * 
	 * @param sfi
	 * @return
	 */
	public HashMap<String, String> readDirCommon(String sfi, String rec) {
		HashMap<String, String> result = new HashMap<String, String>();
		int b = Integer.parseInt(sfi, 16);
		b = (b << 3) + 4;
		String responseApdu = "";
		String commandApdu = packApdu("READ_RECORD", "", WDStringUtil.paddingHeadZero(rec, 2), WDStringUtil.paddingHeadZero(Integer.toHexString(b), 2));
		responseApdu = apduChannel.send(commandApdu);
		String sw = responseApdu.substring(responseApdu.length() - 4);
		if (Constants.SW_SUCCESS.equalsIgnoreCase(sw)) {
			result.put("apdu", commandApdu);
			result.put("res", responseApdu);
		}
		result.put("sw", sw);
		return result;
	}

	/**
	 * GPO指令
	 * 
	 * @param loadGPO
	 *            PDOL参数
	 * @return
	 */
	public HashMap<String, String> gpo(String loadGPO) {
		String commandApdu = packApdu("GPO", loadGPO);
		String responseApdu = apduChannel.send(commandApdu);
		HashMap<String, String> result = unpackApdu(responseApdu);
		if (Constants.SW_SUCCESS.equalsIgnoreCase(result.get("sw"))) {
			if (WDAssert.isNotEmpty(result.get("80"))) {
				result.put("82", result.get("80").substring(0, 4));
				result.put("94", result.get("80").substring(4));
			}
		}
		result.put("apdu", commandApdu);
		result.put("res", responseApdu.substring(0, responseApdu.length() - 4));

		return result;
	}

	/**
	 * 内部认证指令
	 * 
	 * @param random
	 *            终端随机数
	 * @return
	 */
	public HashMap<String, String> internalAuthenticate(String random) {
		HashMap<String, String> result = new HashMap<String, String>();
		String commandApdu = packApdu("INTERNAL_AUTHENTICATE", random);
		String responseApdu = apduChannel.send(commandApdu);
		result = unpackApdu(responseApdu);
		result.put("apdu", commandApdu);
		result.put("res", responseApdu.substring(0, responseApdu.length() - 4));
		return result;
	}

	/**
	 * 外部认证指令
	 * 
	 * @param iad
	 *            发卡行认证数据
	 * @return
	 */
	public HashMap<String, String> externalAuthenticate(String iad) {
		HashMap<String, String> result = new HashMap<String, String>();
		String commandApdu = packApdu("EXTERNAL_AUTHENTICATE", iad);
		String responseApdu = apduChannel.send(commandApdu);
		result = unpackApdu(responseApdu);
		result.put("apdu", commandApdu);
		result.put("res", responseApdu);
		return result;
	}

	/**
	 * 生成AC指令
	 * 
	 * @return
	 */
	public HashMap<String, String> generateAC(String data, String p1) {
		String commandApdu = packApdu("GENERATE_AC", data, p1);
		String responseApdu = apduChannel.send(commandApdu);
		HashMap<String, String> result = unpackApdu(responseApdu);
		if (Constants.SW_SUCCESS.equalsIgnoreCase(result.get("sw"))) {
			if (WDAssert.isNotEmpty(result.get("80"))) {
				result.put("9F27", result.get("80").substring(0, 2)); // CID
				result.put("9F36", result.get("80").substring(2, 6)); // ATC
				result.put("9F26", result.get("80").substring(6, 22)); // AC密文
				result.put("9F10", result.get("80").substring(22));// 发卡行应用数据
				result.put("apdu", commandApdu);
				result.put("res", responseApdu.substring(0, responseApdu.length() - 4));
			}
		}

		return result;
	}

	/**
	 * put data指令
	 * 
	 * @return
	 */
	public HashMap<String, String> putData(String data) {
		HashMap<String, String> result = new HashMap<String, String>();
		String responseApdu = apduChannel.send(data);
		result = unpackApdu(responseApdu);
		result.put("apdu", data);
		result.put("res", responseApdu);
		return result;
	}

	/**
	 * get data指令
	 * 
	 * @param tag
	 * @return
	 */
	public HashMap<String, String> getData(String tag) {
		HashMap<String, String> result = new HashMap<String, String>();
		String commandApdu = packApdu("GET_DATA", "", tag.substring(0, 2), tag.substring(2, 4));
		String responseApdu = apduChannel.send(commandApdu);
		result = unpackApdu(responseApdu);
		result.put("apdu", commandApdu);
		result.put("res", responseApdu);
		return result;
	}

	/**
	 * pin验证
	 * 
	 * @param pin
	 * @return
	 */
	public HashMap<String, String> verifyPin(String pin) {
		HashMap<String, String> result = new HashMap<String, String>();
		
		pin = WDStringUtil.paddingTail("2"+pin.length() + pin, 16, "FF");
		String commandApdu = packApdu("VERIFY", pin);
		String responseApdu = apduChannel.send(commandApdu);
		result = unpackApdu(responseApdu);
		result.put("apdu", commandApdu);
		result.put("res", responseApdu);
		return result;
	}

	/**
	 * externalAuthenticate
	 * 
	 * @param secureity_level
	 * @param keyVersion
	 * @param keyId
	 * @param kenc
	 * @param kmac
	 * @param kdek
	 */
	public void externalAuthenticate(String secureity_level, String keyVersion, String keyId, String kenc, String kmac, String kdek) {
		String data;
		String hostRandom = WDStringUtil.getRandomHexString(16);
		// initializeUpdate
		String strResp = apduChannel.send("8050" + keyVersion + keyId + "08" + hostRandom);
		setInitResp(strResp);

		String Rcard = strResp.substring(24, 40); // random of card
		String Rter = hostRandom; // random of terminal

		String resp = strResp.substring(24, 28);

		String encKey = WD3DesCryptoUtil.cbc_encrypt(kenc, "0182" + resp + "000000000000000000000000", Padding.NoPadding, "0000000000000000");
		String macKey = WD3DesCryptoUtil.cbc_encrypt(kmac, "0101" + resp + "000000000000000000000000", Padding.NoPadding, "0000000000000000");
		String kekKey = WD3DesCryptoUtil.cbc_encrypt(kdek, "0181" + resp + "000000000000000000000000", Padding.NoPadding, "0000000000000000");
		setEncKey(encKey);
		setMacKey(macKey);
		setKekKey(kekKey);
		// host
		data = Rcard + Rter + "8000000000000000";
		String Host = WD3DesCryptoUtil.cbc_encrypt(encKey, data, Padding.NoPadding, "0000000000000000");
		Host = Host.substring(Host.length() - 16);
		// Smac
		data = "8482" + secureity_level + "0010" + Host;
		data = data + "80";
		while (data.length() % 16 != 0) {
			data = data + "00";
		}
		String Smac = WDPBOCUtil.triple_des_mac(macKey, data, Padding.NoPadding, "0000000000000000");
		int lc = Host.length() + Smac.length();
		String lcStr = Integer.toHexString(lc / 2);
		resp = apduChannel.send("8482" + secureity_level + "00" + lcStr + Host + Smac);
		if (resp.equalsIgnoreCase("9000")) {
			setSecureityLevel(secureity_level);
			setSmac(Smac);
		}
	}

	/**
	 * putkey javacard
	 * 
	 * @param keyVersion
	 * @param newKeyVersion
	 * @param keyIndex
	 * @param CDKenc
	 * @param CDKmac
	 * @param CDKdek
	 * @throws Exception
	 */
	public boolean putKey(String keyVersion, String keyId, String newKeyVersion, String CDKenc, String CDKmac, String CDKdek) throws Exception {
		// externalAuthenticate(secureity_level, keyVersion, keyId, CDKenc, CDKmac, CDKdek);

		String P1 = Integer.toHexString(Integer.parseInt(keyVersion) & 0x7f);
		String P2 = Integer.toHexString(Integer.parseInt(keyId) | 0x80);
		if (P1.length() < 2) {
			P1 = WDStringUtil.paddingHeadZero(P1, 2);
		}

		if (P2.length() < 2) {
			P2 = WDStringUtil.paddingHeadZero(P2, 2);
		}
		String data = newKeyVersion;

		String CDKencLen = Integer.toHexString(CDKenc.length() / 2);
		String CDKmacLen = Integer.toHexString(CDKmac.length() / 2);
		String CDKdekLen = Integer.toHexString(CDKdek.length() / 2);

		String EncCDKenc = WD3DesCryptoUtil.ecb_encrypt(getKekKey(), CDKenc, Padding.NoPadding);
		String EncCDKmac = WD3DesCryptoUtil.ecb_encrypt(getKekKey(), CDKmac, Padding.NoPadding);
		String EncCDKdek = WD3DesCryptoUtil.ecb_encrypt(getKekKey(), CDKdek, Padding.NoPadding);

		String CDKencKcv = WD3DesCryptoUtil.ecb_encrypt(CDKenc, "0000000000000000", Padding.NoPadding).substring(0, 6);
		String CDKmacKcv = WD3DesCryptoUtil.ecb_encrypt(CDKmac, "0000000000000000", Padding.NoPadding).substring(0, 6);
		String CDKdekKcv = WD3DesCryptoUtil.ecb_encrypt(CDKdek, "0000000000000000", Padding.NoPadding).substring(0, 6);

		String CDKencData = "80" + CDKencLen + EncCDKenc + "03" + CDKencKcv;
		String CDKmacData = "80" + CDKmacLen + EncCDKmac + "03" + CDKmacKcv;
		String CDKdekData = "80" + CDKdekLen + EncCDKdek + "03" + CDKdekKcv;

		data += CDKencData + CDKmacData + CDKdekData;
		data = Integer.toHexString(data.length() / 2) + data;

		String resp = send("80D8" + P1 + P2 + data);

		if (resp.equalsIgnoreCase("9000")) {
			return true;
		}
		return false;
	}

	/**
	 * send apdu command
	 * 
	 * @param apdu
	 * @return
	 * @throws Exception
	 */
	public String send(String apdu) throws Exception {
		String classByte = apdu.substring(0, 2);
		String insByte = apdu.substring(2, 4);
		if (classByte.equalsIgnoreCase("80")&&!(classByte.equalsIgnoreCase("80") && insByte.startsWith("0"))) {
			if (getSecureityLevel().equalsIgnoreCase(MACONLY)) {
				int cla = (Integer.parseInt(apdu.substring(0, 2), 16) & 0xF0) | 0x04;
				apdu = Integer.toHexString(cla) + apdu.substring(2);
				int lc = Integer.parseInt(apdu.substring(8, 10), 16);
				lc += 8;

				String encIv = WDPBOCUtil.single_des_mac(getMacKey().substring(0, 16), getSmac(), Padding.NoPadding, "0000000000000000");

				String macData = apdu.substring(0, 8) + WDStringUtil.paddingHeadZero(Integer.toHexString(lc), 2) + apdu.substring(10);
				macData = macData + "80";
				while (macData.length() % 16 != 0) {
					macData = macData + "00";
				}
				String cMac = WDPBOCUtil.triple_des_mac(getMacKey(), macData.toUpperCase(), Padding.NoPadding, encIv);

				setSmac(cMac);

				apdu = apdu.substring(0, 8) + WDStringUtil.paddingHeadZero(Integer.toHexString(lc), 2) + apdu.substring(10) + cMac;

			} else if (getSecureityLevel().equalsIgnoreCase(MACENC)) {
				int cla = (Integer.parseInt(apdu.substring(0, 2), 16) & 0xF0) | 0x04;
				apdu = Integer.toHexString(cla) + apdu.substring(2);
				int lc = Integer.parseInt(apdu.substring(8, 10), 16);
				int L = lc;
				lc += 8;

				String encIv = WDPBOCUtil.single_des_mac(getMacKey().substring(0, 16), getSmac(), Padding.NoPadding, "0000000000000000");

				String macData = apdu.substring(0, 8) + WDStringUtil.paddingHeadZero(Integer.toHexString(lc), 2) + apdu.substring(10);
				macData = macData + "80";
				while (macData.length() % 16 != 0) {
					macData = macData + "00";
				}
				String cMac = WDPBOCUtil.triple_des_mac(getMacKey(), macData.toUpperCase(), Padding.NoPadding, encIv);

				setSmac(cMac);

				String encData = apdu.substring(10);
				encData = encData + "80";
				L = L + 8 - L % 8;
				while (encData.length() < 2 * L) {
					encData = encData + "00";
				}
				lc = encData.length() / 2 + 8;
				String encResult = WD3DesCryptoUtil.cbc_encrypt(getEncKey(), encData, Padding.NoPadding, "0000000000000000");

				apdu = apdu.substring(0, 8) + WDStringUtil.paddingHeadZero(Integer.toHexString(lc), 2) + encResult + cMac;
			}
		}
		
		return apduChannel.send(apdu);
	}

	public void close() {
		if (apduChannel != null) {
			apduChannel.close();
		}
	}

	public static void main(String[] args) {
		System.out.println(String.format("%04x", 10000));
		System.out.println(Integer.parseInt("01") | 0x80);
	}
}
