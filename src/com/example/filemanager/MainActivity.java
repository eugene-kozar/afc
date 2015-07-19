package com.example.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	private List<String> directoryEntries = new ArrayList<String>();
	private String filePath;
	public static File currentDirectory = new File("/");

	// start of application
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_main);
		browseTo(new File("/sdcard"));
	}

	// browse to parent directory
	private void upOneLevel() {
		if (this.currentDirectory.getParent() != null) {
			this.browseTo(this.currentDirectory.getParentFile());
		}
	}

	// browse to file or directory
	void browseTo(final File aDirectory) {
		// if we want to browse directory
		if (aDirectory.isDirectory()) {
			// fill list with files from this directory
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
			// set title text
			TextView titleManager = (TextView) findViewById(R.id.titleManager);
			titleManager.setText(aDirectory.getAbsolutePath());
		} else {
			filePath = aDirectory.toString();
			// if we want to encrypt/decrypt file, show this dialog:
			if (!aDirectory.toString().endsWith(".afc")) {
				// listener when YES button clicked
				OnClickListener okButtonListener = new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent(MainActivity.this, Coder.class);
						intent.putExtra("filePath", filePath);
				        startActivityForResult(intent, 1);
					}
				};
				// listener when NO button clicked
				OnClickListener cancelButtonListener = new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// do nothing
					}
				};
				// create dialog
				new AlertDialog.Builder(this)
					.setTitle("Підтвердження") // title
					.setMessage("Бажаєте закодувати файл " + aDirectory.getName() + "?") // message
					.setPositiveButton("Так", okButtonListener) // positive button
					.setNegativeButton("Ні", cancelButtonListener) // negative button
					.show(); // show dialog
			} else {
				// listener when YES button clicked
				OnClickListener okButtonListener = new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						//decrypt(/*aDirectory.toString()*/);
						Intent intent = new Intent(MainActivity.this, Decoder.class);
						intent.putExtra("filePath", filePath);
				        startActivityForResult(intent, 3);
					}
				};
				// listener when NO button clicked
				OnClickListener cancelButtonListener = new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// do nothing
					}
				};
				// create dialog
				new AlertDialog.Builder(this)
					.setTitle("Підтвердження") // title
					.setMessage("Бажаєте розкодувати файл " + aDirectory.getName() + "?") // message
					.setPositiveButton("Так", okButtonListener) // positive button
					.setNegativeButton("Ні", cancelButtonListener) // negative button
					.show(); // show dialog
			}
		}
	}

	// fill list
	private void fill(File[] files) {
		// clear list
		this.directoryEntries.clear();

		if (!this.currentDirectory.getParentFile().equals(new File("/")))
			this.directoryEntries.add("..");

		// add every file into list
		for (File file : files) {
			this.directoryEntries.add(file.getName());
		}

		// set adapter to show everything
		setListAdapter(new MyArrayAdapter(this, directoryEntries));
	}

	// when you clicked onto item
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// get selected file name
		int selectionRowID = position;
		String selectedFileString = this.directoryEntries.get(selectionRowID);

		// if we select ".." then go upper
		if (selectedFileString.equals("..")) {
			this.upOneLevel();
		} else {
			// browse to clicked file or directory using browseTo()
			File clickedFile = null;
			clickedFile = new File(currentDirectory.toString() + "/"
					+ selectedFileString);
			if (clickedFile != null)
				this.browseTo(clickedFile);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// refresh main activity
		if (resultCode == RESULT_OK) {
			browseTo(currentDirectory);
		}
	}

	@Override
	public void onBackPressed() {
		if (!this.currentDirectory.getParentFile().equals(new File("/")))
			upOneLevel();
		else
			super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}