package com.echeloneditor.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.echeloneditor.os.OsConstants;

public class WindowsExcuter {
	public final static Logger log = Logger.getLogger(WindowsExcuter.class);
	public static Process p;
	public static ProcessBuilder pb;

	/**
	 * 
	 * @param dir
	 * @param windowCommand
	 * @throws Exception
	 */
	public static void excute(File dir, String windowCommand, boolean logPrint) throws Exception {
		Debug.log.info(windowCommand);
		List<String> cmdList = new ArrayList<String>();
		if (OsConstants.isWindows()) {
			cmdList.add("cmd.exe");
			cmdList.add("/c");
		}else if (OsConstants.isMacOS()) {
			
		}
		for (String cmd : windowCommand.split(" ")) {
			cmdList.add(cmd);
		}
		excute(dir, cmdList, logPrint);
	}

	/**
	 * 
	 * @param dir
	 * @param cmdList
	 * @throws Exception
	 */
	public static void excute(File dir, List<String> cmdList, boolean logPrint) throws Exception {
		pb = new ProcessBuilder(cmdList);
		pb.directory(dir);

		p = pb.start();
		
		if (logPrint) {
			InputStream is = p.getInputStream();
			InputStream isErr = p.getErrorStream();
			print(is, isErr);
			p.waitFor();
			p.destroy();
		}
	}

	/**
	 * 
	 * @param cmd
	 * @throws Exception
	 */
	public static void writeCommand(String cmd) throws Exception {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		bw.write(cmd);
		bw.flush();

		InputStream is = p.getInputStream();
		InputStream isErr = p.getErrorStream();
		print(is, isErr);

		p.waitFor();
	}

	public static void printLog(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("GBK")));
		String line = null;
		while ((line = br.readLine()) != null) {
			log.debug(line);
		}
		is.close();
		br.close();
	}

	private static void print(InputStream in, InputStream err) throws IOException {
		printLog(in);
		printLog(err);
	}

	public static void main(String[] args) throws Exception {
		/*
		 * List<String> cmdList = new ArrayList<String>(); cmdList.add("ipconfig"); cmdList.add("/all"); WindowsExcuter.excute(new File("."), cmdList);
		 * 
		 * WindowsExcuter.excute(new File("."), "cmd /c ipconfig/all");
		 */
		List<String> cmdList = new ArrayList<String>();

		File file = new File("D:\\SLE二代母卡工具HexGhostV0.4");
		cmdList.add("cmd.exe");
		cmdList.add("/c");
		cmdList.add("start");
		cmdList.add("E:/KeilSpace/JC30_CUT_Platform_zhangli/SRC");

		WindowsExcuter.excute(new File("."), cmdList,true);
	}
}
