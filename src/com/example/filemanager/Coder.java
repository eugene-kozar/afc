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
import java.security.Provider;
import java.security.Security;

//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
import javax.crypto.*;
import javax.crypto.spec.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Coder extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coding);
		// setup button listener
		Button codeButton = (Button) findViewById(R.id.coding_button);
		codeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String filePath = getIntent().getStringExtra("filePath");
				EditText pass1 = (EditText)findViewById(R.id.editText1);
				EditText pass2 = (EditText)findViewById(R.id.editText2);	
				String str_pass1 = pass1.getText().toString();
				String str_pass2 = pass2.getText().toString();
				if(!str_pass1.equals(str_pass2)) {
					Toast toast = Toast.makeText(getApplicationContext(), 
							"Ви ввели різні паролі!",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 20);
					toast.show();
				} else if(str_pass1.length()<4) {
					Toast toast = Toast.makeText(getApplicationContext(), 
							"Занадто короткий пароль, введіть хоча б 4 символа!",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 20);
					toast.show();
				} else {					
					str_pass1 = generateEncryptionKey(str_pass1);
					try {
						encrypt(filePath, str_pass1);
						Intent answerIntent = new Intent();
						setResult(RESULT_OK, answerIntent);
						finish();
					} catch (Exception e) {
						e.printStackTrace();
						Toast toast = Toast.makeText(getApplicationContext(), 
								"Не вдалося закодувати файл!\n" + e.getMessage(),
								Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 20);
						toast.show();
					}
				}
			}
		});
	}

	public String generateEncryptionKey(String pass) {
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

	public void encrypt(String filePath, String encryptionKey) throws Exception {
		//Read data for encryption
		Long totalStart = System.currentTimeMillis();
		File f = new File(filePath);
		byte [] data = new byte [(int)f.length()];
		BufferedInputStream bis = new BufferedInputStream (new FileInputStream (f));
		bis.read(data);
		bis.close();
		//Encryption of data
		Long cipherStart = System.currentTimeMillis();
		//Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		//SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
//		Cipher cipher = Cipher.getInstance("Blowfish");
//		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "Blowfish");
		//Cipher cipher = Cipher.getInstance("ARC4", "BC");
		//SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "ARC4");
//		Cipher cipher = Cipher.getInstance("RC4");
//		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "RC4");
		Cipher cipher = Cipher.getInstance("DESede");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "DESede");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedData = cipher.doFinal(data);
		Long cipherEnd = System.currentTimeMillis();
		//Write encrypted data to file
		BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream (new File (filePath + ".afc")));
		bos.write(encryptedData);
		bos.close();
		//Saving password
		savePassword(encryptionKey, filePath + ".afc");
		//deleting old file
		new File(filePath).delete();
		//Message about finish of coding file
		Long totalEnd = System.currentTimeMillis();
		Toast toast = Toast.makeText(getApplicationContext(), 
				"Файл "	+ filePath.substring(filePath.lastIndexOf('/') + 1)	+ " закодований!\n\n" +
				"В цілому витрачено " + (totalEnd-totalStart) + " мілісекунд.\n" +
				"Шифрування зайняло " + (cipherEnd-cipherStart) + " мілісекунд.",
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public void savePassword(String encryptionKey, String filePath) throws FileNotFoundException {
		boolean wasSuchPass = false;
		String line;
		String allPasswords = "";
		File folder = new File ("/sdcard/Android/data/");
		folder.mkdirs();
		File f = new File ("/sdcard/Android/data/.afcinfo");
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				Toast toast = Toast.makeText(getApplicationContext(), 
						"Помилка створення файлу\n" + e.getMessage(),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}

		BufferedReader in = new BufferedReader(new FileReader(f));
		try {
			while ((line = in.readLine()) != null) {
				if(line.endsWith(filePath)) {
					line = encryptionKey.hashCode() + " " + filePath;
					wasSuchPass = true;
				}
				allPasswords += line;
				allPasswords += "\n";
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
		if(!wasSuchPass)
			allPasswords += encryptionKey.hashCode() + " " + filePath;
		PrintWriter pw = new PrintWriter("/sdcard/Android/data/.afcinfo"); 
		pw.printf(allPasswords); 
		pw.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coder, menu);
		return true;
	}
}
