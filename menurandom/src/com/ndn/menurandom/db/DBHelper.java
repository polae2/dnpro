package com.ndn.menurandom.db;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_PATH = "/data/data/com.ndn.menurandom/databases/";
	private static final String DB_NAME = "dbmenu.db";
	private static final int DATABASE_VERSION = 1;
	private SQLiteDatabase myDataBase;
	private Context context;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public void createDataBase() {
		boolean dbExist = checkDataBase();

		// if db is not exist
		if (!dbExist) {
			
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				Log.e("NHK", "Error occured while copying database");
			}
		}
	}

	// Check if the database already exist or not
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;

		try {
			checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.e("NHK", "Database is not exist.");
		}

		if (checkDB != null)
			checkDB.close();

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		InputStream myInput = context.getAssets().open(DB_NAME);				// Open local db as the input stream
		String outFileName = DB_PATH + DB_NAME;

		OutputStream myOutput = new FileOutputStream(outFileName);				// Open the empty db as the output stream
		byte[] buffer = new byte[1024];					
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public SQLiteDatabase openDataBase() {
		try {
			// Open the database
			String myPath = DB_PATH + DB_NAME;
			myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch(Exception e) {
			Log.e("NHK", "Error occured to open database");
		}
		return myDataBase;
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}
}
