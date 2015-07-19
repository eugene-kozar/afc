package com.example.filemanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
import javax.crypto.*;
import javax.crypto.spec.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Decoder extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decoding);
		Button decodeButton = (Button) findViewById(R.id.decoding_button);
		decodeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				EditText pass3 = (EditText)findViewById(R.id.editText3);
				String str_pass3 = pass3.getText().toString();
				str_pass3 = generateDecryptionKey(str_pass3);
				String filePath = getIntent().getStringExtra("filePath");
				if(passwordIsCorrect(filePath, str_pass3))	{
					try {
						decrypt(filePath, str_pass3);
						Intent answerIntent = new Intent();
						setResult(RESULT_OK, answerIntent);
						finish();
					} catch (Exception e) {
						e.printStackTrace();
						Toast toast = Toast.makeText(getApplicationContext(), 
								"Не вдалося розкодувати файл!\n" + e.getMessage(),
								Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 20);
						toast.show();
					}
				}
				else {
					Toast toast = Toast.makeText(getApplicationContext(), 
							"Ви ввели невірний пароль!",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});
	}

	public String generateDecryptionKey(String pass) {
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		int x, bytes = 0, lengthOfKey = 16;
		for (int i = 0; i < pass.length(); i++) {
			x = pass.charAt(i);
			if (x <= 127)
				bytes ++;
			else if (x > 127)
				bytes += 2;
		}
		int i = 0;
		if(pass.length()<16) {
			if(bytes<16)
				lengthOfKey = 16;
			else if(bytes<24)
				lengthOfKey = 24;
			else if(bytes<32)
				lengthOfKey = 32;
			while(bytes!=lengthOfKey) {
				pass += imei.charAt(i);
				i ++;
				bytes ++;
			}
		}
		else if(pass.length()>16)
			pass = pass.substring(0, 16);
		return pass;
	}

	public void deletePassword(String filePath) throws FileNotFoundException{
		String line;
		String allPasswords = "";
		File f = new File ("/sdcard/Android/data/.afcinfo");
		BufferedReader in = new BufferedReader(new FileReader(f));
		try {
			while ((line = in.readLine()) != null) {
				if(!line.endsWith(filePath)) {
					allPasswords += line;
					allPasswords += "\n";
				}			
			}
		} catch (NumberFormatException e) {
			Toast toast = Toast.makeText(getApplicationContext(), 
					"NumberFormatException\n" + e.getMessage(),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (IOException e) {
			Toast toast = Toast.makeText(getApplicationContext(), 
					"IOException\n" + e.getMessage(),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} 
		PrintWriter pw = new PrintWriter("/sdcard/Android/data/.afcinfo"); 
		pw.printf(allPasswords); 
		pw.close();
	}
	
	public boolean passwordIsCorrect(String filePath, String str_pass) {
		String txtLine;
		try 
		{ 
			File f = new File ("/sdcard/Android/data/.afcinfo");
			BufferedReader in = new BufferedReader(new FileReader(f));
			try {
				while ((txtLine = in.readLine()) != null) { 
					if(txtLine.endsWith(filePath)) {
						String pass = txtLine.substring(0, txtLine.indexOf(" "));
						int passInt = Integer.parseInt(pass);
						if(passInt==str_pass.hashCode())
							return true;
						else
							return false;					
					}
				}
			} catch (NumberFormatException e) {
				Toast toast = Toast.makeText(getApplicationContext(), 
						"NumberFormatException\n" + e.getMessage(),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} catch (IOException e) {
				Toast toast = Toast.makeText(getApplicationContext(), 
						"IOException\n" + e.getMessage(),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} 
		} 
		catch (FileNotFoundException ex) { 
			System.out.print(ex.getMessage()); 
		} 
		return false;
	}

	public void decrypt(String filePath, String decryptionKey) throws Exception {
		//Read data for decryption
		Long totalStart = System.currentTimeMillis();
		File f = new File(filePath);
		byte [] encryptedData = new byte [(int)f.length()];
		BufferedInputStream bis = new BufferedInputStream (new FileInputStream (f));
		bis.read(encryptedData);
		bis.close();
		//Decryption of data
		Long cipherStart = System.currentTimeMillis();
		//Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		//SecretKeySpec key = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "AES");
//		Cipher cipher = Cipher.getInstance("Blowfish");
//		SecretKeySpec key = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "Blowfish");
		//Cipher cipher = Cipher.getInstance("ARC4", "BC");
		//SecretKeySpec key = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "ARC4");
//		Cipher cipher = Cipher.getInstance("RC4");
//		SecretKeySpec key = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "RC4");
		Cipher cipher = Cipher.getInstance("DESede");
		SecretKeySpec key = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "DESede");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte [] decryptedData = cipher.doFinal(encryptedData);
		Long cipherEnd = System.currentTimeMillis();
		//Write decrypted data to file
		BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream 
				(new File (filePath.substring(0, filePath.lastIndexOf('.')))));
		bos.write(decryptedData);
		bos.close();
		//Deleting password
		deletePassword(filePath);
		//deleting old file
		new File(filePath).delete();
		//Message about finish of decoding file
		Long totalEnd = System.currentTimeMillis();
		Toast toast = Toast.makeText(getApplicationContext(), 
				"Файл "	+ filePath.substring(filePath.lastIndexOf('/') + 1)	+ " розкодований!\n\n" +
				"В цілому витрачено " + (totalEnd-totalStart) + " мілісекунд.\n" +
				"Розшифрування зайняло " + (cipherEnd-cipherStart) + " мілісекунд.",
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.decoder, menu);
		return true;
	}

}
