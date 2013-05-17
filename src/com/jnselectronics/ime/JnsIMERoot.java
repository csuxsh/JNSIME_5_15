package com.jnselectronics.ime;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.jnselectronics.ime.jni.JnsIMEConsole;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

@SuppressLint("SdCardPath")
public class JnsIMERoot {
	private static final String TAG = "JnsIMERoot";
	private static FileOutputStream fos;
	private static Context mContext = null;
	private static DataOutputStream dos = null;


	public static void setContext(Context context) {
		mContext = context;
	}

	public static void initConsole() {
		int[] pid = new int[1];
		String cmd = "/system/bin/sh";
		FileDescriptor fd = JnsIMEConsole.createSubprocess(cmd, null, null, pid);
		fos = new FileOutputStream(fd);
		String initCommand = "export PATH=/data/local/bin:$PATH \r";
		@SuppressWarnings("unused")
		byte[] buffer = new byte[255];
		try {

			fos.write(initCommand.getBytes());
			fos.flush();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@SuppressLint("SdCardPath")
	public static boolean movingJNSInput() {
		try {
			InputStream is = mContext.getAssets().open("jnsinput.jar");
			int size = is.available();
			if (size > 0) {
				File file = new File("/mnt/sdcard/jnsinput/jnsinput.jar");
				byte[] buffer = new byte[size];
				is.read(buffer);
				FileOutputStream os = new FileOutputStream(file);
				os.write(buffer);
				os.flush();
				os.close();
				os = null;
				file = null;
			} else {
				return false;
			} 
			is.close();
			is = null;
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}


	@SuppressLint("SdCardPath")
	private static boolean moveJNSInput() {
		File file = new File("/mnt/sdcard/jnsinput");
		if (file.exists()) {
			@SuppressWarnings("unused")
			File file1 = new File("/mnt/sdcard/jnsinput/jnsinput.jar");
			//		if (file1.exists()) {
			//			return true;
			//		}
			return movingJNSInput();
		} else {
			if (file.mkdir()) {
				return movingJNSInput();
			} else {
				Log.e(TAG, "make /data/jnsinput directory error");
			}
		}
		return false;
	}

	private static void runJNSInput() {
		try {
			//	if (!new File("/data/jnsinput").exists()) {
			dos.write("mkdir /data/jnsinput \n".getBytes());
			dos.flush();
			dos.write("rm /data/jnsinput/jnsinput.jar \n".getBytes());
			dos.flush();
			//	}
			String cmd = "export LD_LIBRARY_PATH=/vender/lib; export CLASSPATH=/mnt/sdcard/jnsinput/jnsinput.jar; exec app_process /system/bin com.blueocean.jnsinput.JNSInputServer \n";
			dos.write(cmd.getBytes());
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean rooted() {
		Process process = null;
		while(true)
		{	
			try {
				process = Runtime.getRuntime().exec("su");
				dos = new DataOutputStream(process.getOutputStream());
				DataInputStream dis = new DataInputStream(process.getInputStream());
				new DataInputStream(process.getErrorStream());
				try {
					dos.write("id \n".getBytes());
					dos.flush();
					@SuppressWarnings("deprecation")
					String line = dis.readLine();
					Log.e(TAG, "fffffffff line = " + line);
					if (line == null) return false;
					if (line.contains("uid=0(root)")) {
						initConsole();
						if (moveJNSInput()) {
							runJNSInput();
						}
						//	new Thread(chmodEventRunnable).start();
						return true;
					}
					//return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
