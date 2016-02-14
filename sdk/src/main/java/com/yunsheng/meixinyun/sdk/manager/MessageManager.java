package com.yunsheng.meixinyun.sdk.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.yunsheng.meixinyun.sdk.comm.Constant;
import com.yunsheng.meixinyun.sdk.db.DBManager;
import com.yunsheng.meixinyun.sdk.db.SQLiteTemplate;
import com.yunsheng.meixinyun.sdk.db.SQLiteTemplate.RowMapper;
import com.yunsheng.meixinyun.sdk.javabean.IMMessage;
import com.yunsheng.meixinyun.sdk.util.StringUtil;

import java.util.List;

/**
 *
 * 消息历史记录，
 *
 */
public class MessageManager {
	private static MessageManager messageManager = null;
	private static DBManager manager = null;

	private MessageManager(Context context) {
		SharedPreferences sharedPre = context.getSharedPreferences(
				Constant.LOGIN_SET, Context.MODE_PRIVATE);
		String databaseName = sharedPre.getString(Constant.USERNAME, null);
		manager = DBManager.getInstance(context, databaseName);
	}

	public static MessageManager getInstance(Context context) {

		if (messageManager == null) {
			messageManager = new MessageManager(context);
		}

		return messageManager;
	}

	/**
	 *
	 * 保存消息.
	 *
	 * @param msg
	 */
	public long saveIMMessage(IMMessage msg) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		if (StringUtil.notEmpty(msg.getContent())) {
			contentValues.put("content", StringUtil.doEmpty(msg.getContent()));
		}
		if (StringUtil.notEmpty(msg.getFromSubJid())) {
			contentValues.put("msg_from",
					StringUtil.doEmpty(msg.getFromSubJid()));
		}
		contentValues.put("msg_type", msg.getMsgType());
		contentValues.put("msg_time", msg.getTime());
		return st.insert("im_msg_his", contentValues);
	}

	/**
	 *
	 * 更新状态.
	 *
	 * @param status
	 */
	public void updateStatus(String id, Integer status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		st.updateById("im_msg_his", id, contentValues);
	}

	/**
	 *
	 * 查找与某人的聊天记录聊天记录
	 *
	 * @param pageNum
	 *            第几页
	 * @param pageSize
	 *            要查的记录条数
	 * @return
	 */
	public List<IMMessage> getMessageListByFrom(String fromUser, int pageNum,
												int pageSize) {
		if (StringUtil.empty(fromUser)) {
			return null;
		}
		int fromIndex = (pageNum - 1) * pageSize;
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<IMMessage> list = st.queryForList(
				new RowMapper<IMMessage>() {
					@Override
					public IMMessage mapRow(Cursor cursor, int index) {
						IMMessage msg = new IMMessage();
						msg.setContent(cursor.getString(cursor
								.getColumnIndex("content")));
						msg.setFromSubJid(cursor.getString(cursor
								.getColumnIndex("msg_from")));
						msg.setMsgType(cursor.getInt(cursor
								.getColumnIndex("msg_type")));
						msg.setTime(cursor.getString(cursor
								.getColumnIndex("msg_time")));
						return msg;
					}
				},
				"select content,msg_from, msg_type,msg_time from im_msg_his where msg_from=? order by msg_time desc limit ? , ? ",
				new String[] { "" + fromUser, "" + fromIndex, "" + pageSize });
		return list;

	}

	/**
	 *
	 * 查找与某人的聊天记录总数
	 *
	 * @return
	 */
	public int getChatCountWithSb(String fromUser) {
		if (StringUtil.empty(fromUser)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st
				.getCount(
						"select _id,content,msg_from msg_type  from im_msg_his where msg_from=?",
						new String[] { "" + fromUser });

	}

	/**
	 * 删除与某人的聊天记录
	 *
	 * @param fromUser
	 */
	public int delChatHisWithSb(String fromUser) {
		if (StringUtil.empty(fromUser)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition("im_msg_his", "msg_from=?",
				new String[] { "" + fromUser });
	}


}
