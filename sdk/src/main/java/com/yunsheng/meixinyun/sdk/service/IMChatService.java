package com.yunsheng.meixinyun.sdk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.yunsheng.meixinyun.sdk.R;
import com.yunsheng.meixinyun.sdk.comm.Constant;
import com.yunsheng.meixinyun.sdk.manager.MessageManager;
import com.yunsheng.meixinyun.sdk.manager.XmppConnectionManager;
import com.yunsheng.meixinyun.sdk.javabean.IMMessage;
import com.yunsheng.meixinyun.sdk.util.DateUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Calendar;

/**
 *
 * 聊天服务.
 *
 */
public class IMChatService extends Service {
	private Context context;
	private NotificationManager notificationManager;

	@Override
	public void onCreate() {
		context = this;
		super.onCreate();
		initChatManager();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initChatManager() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		XMPPConnection conn = XmppConnectionManager.getInstance()
				.getConnection();
		conn.addPacketListener(pListener, new MessageTypeFilter(
				Message.Type.chat));
	}

	PacketListener pListener = new PacketListener() {

		@Override
		public void processPacket(Packet arg0) {
			Message message = (Message) arg0;
			if (message != null && message.getBody() != null
					&& !message.getBody().equals("null")) {
				IMMessage msg = new IMMessage();
				// String time = (String)
				// message.getProperty(IMMessage.KEY_TIME);
				String time = DateUtil.date2Str(Calendar.getInstance(),
						Constant.MS_FORMART);
				msg.setTime(time);
				msg.setContent(message.getBody());
				if (Message.Type.error == message.getType()) {
					msg.setType(IMMessage.ERROR);
				} else {
					msg.setType(IMMessage.SUCCESS);
				}
				String from = message.getFrom().split("/")[0];
				msg.setFromSubJid(from);

				// 历史记录
				IMMessage newMessage = new IMMessage();
				newMessage.setMsgType(0);
				newMessage.setFromSubJid(from);
				newMessage.setContent(message.getBody());
				newMessage.setTime(time);

				long messageId = -1;
				messageId = MessageManager.getInstance(context).saveIMMessage(newMessage);




				if (messageId != -1) {
					Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
					intent.putExtra(IMMessage.IMMESSAGE_KEY, msg);
					sendBroadcast(intent);

				}

			}

		}

	};

	/**
	 *
	 * 发出Notification的method.
	 *
	 * @param iconId
	 *            图标
	 * @param contentTitle
	 *            标题
	 * @param contentText
	 *            你内容
	 * @param activity
	 */
	private void setNotiType(int iconId, String contentTitle,
							 String contentText, Class activity, String from) {

		/*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		// notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setAutoCancel(true)
				.setSmallIcon(iconId)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setContentIntent(appIntent);
		Notification notification = builder.build();

		notificationManager.notify(0,notification);
	}
}
