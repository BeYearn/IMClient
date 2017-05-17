package com.emagroup.imsdk.save;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.MsgBean;

import java.util.ArrayList;

/**
 * Created by beyearn on 2017/5/16.
 *
 */

public class ChatLogDao {

    private ChatSaveHelper helper;

    public ChatLogDao(Context context) {
        helper = new ChatSaveHelper(context);
    }

    public long add(MsgBean bean) {
        SQLiteDatabase db = helper.getWritableDatabase();

        /*db.execSQL("insert into " +
                ChatSaveHelper.DB_NAME +
                "(appId, fName, fuid, handler, msg, msgId, tID, ext, mark) values(?,?,?,?,?,?,?,?,?)",
                new Object[]{bean.getAppId(), bean.getfName(), bean.getFuid(), bean.getHandler(), bean.getMsg(), bean.getMsgId(), bean.gettID(), bean.getExt(), bean.getMark()});*/

        ContentValues values = new ContentValues();
        values.put(ImConstants.APP_ID, bean.getAppId());
        values.put(ImConstants.FNAME, bean.getfName());
        values.put(ImConstants.FUID, bean.getFuid());
        values.put(ImConstants.HANDLER, bean.getHandler());
        values.put(ImConstants.MSG, bean.getMsg());
        values.put(ImConstants.MSG_ID, bean.getMsgId());
        values.put(ImConstants.TID, bean.gettID());
        values.put(ImConstants.EXT, bean.getExt());
        values.put(ImConstants.MARK, bean.getMark());

        long rowID = db.insert(ChatSaveHelper.DB_NAME, null, values);

        db.close();
        return rowID;
    }

    public ArrayList<MsgBean> queryMsg(String selfUid, String withUid, String num) {
        ArrayList<MsgBean> msgList = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = db.query(ChatSaveHelper.DB_NAME,    //table name
                null,                                        //columns
                "fUid=? and tId=?",                          //selection
                new String[]{selfUid, withUid},               //selectionArgs
                null,                                        //groupBy
                null,                                         //having
                "msgId",                                     //orderBy
                num                                           //limit
        );

        while (cursor.moveToNext()) {
            MsgBean msgBean = new MsgBean();
            msgBean.setAppId(cursor.getString(1));   //0位是主键id
            msgBean.setfName(cursor.getString(2));
            msgBean.setFuid(cursor.getString(3));
            msgBean.setHandler(cursor.getString(4));
            msgBean.setMsg(cursor.getString(5));
            msgBean.setMsgId(cursor.getString(6));
            msgBean.settID(cursor.getString(7));
            msgBean.setExt(cursor.getString(8));
            msgBean.setMark(cursor.getString(9));
            msgList.add(msgBean);
        }
        cursor.close();
        db.close();
        return msgList;
    }

}
