package com.emagroup.imsdk.save;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.emagroup.imsdk.ImConstants;

/**
 * Created by beyearn on 2017/5/16.
 */

public class ChatSaveHelper extends SQLiteOpenHelper {

    //类没有实例化,是不能用作父类构造器的参数,必须声明为静态
    public static final String DB_NAME = "ChatLog"; //数据库名称
    private static final int DB_VERSION = 1; //数据库版本


    public ChatSaveHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " +
                DB_NAME +
                "(id INTEGER PRIMARY KEY, " +
                ImConstants.APP_ID + " varchar(20), " +
                ImConstants.FNAME + " varchar(20), " +
                ImConstants.FUID + " varchar(32), " +
                ImConstants.HANDLER + " varchar(5), " +
                ImConstants.MSG + " varchar(100), " +
                ImConstants.MSG_ID + " varchar(20), " +
                ImConstants.TID + " varchar(40), " +
                ImConstants.EXT + " varchar(100), " +
                ImConstants.MARK + " varchar(6));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
