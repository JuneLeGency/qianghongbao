package com.nearucenterplaza.redenvelopeassistant.service;

import com.nearucenterplaza.redenvelopeassistant.R;
import com.nearucenterplaza.redenvelopeassistant.ui.fragmant.WeChatFragment;
import com.nearucenterplaza.redenvelopeassistant.service.core.Notifier;
import com.nearucenterplaza.redenvelopeassistant.service.core.RedEnvelopeHelper;
import com.nearucenterplaza.redenvelopeassistant.service.core.SettingHelper;
import com.nearucenterplaza.redenvelopeassistant.utils.ActivityHelper;
import com.nearucenterplaza.redenvelopeassistant.utils.XLog;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class WechatAccService extends AccessibilityService {

	public static void log(String message) {
		XLog.e("WechatAccService", message);
	} 
	
	/**
	 * {@inheritDoc}
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	public void onServiceConnected() {

		Log.d("test","service connected");
		AccessibilityServiceInfo accessibilityServiceInfo = getServiceInfo();
		if (accessibilityServiceInfo == null)
			accessibilityServiceInfo = new AccessibilityServiceInfo();
		accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
		accessibilityServiceInfo.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
		accessibilityServiceInfo.packageNames = new String[] { WeChatFragment.WECHAT_PACKAGENAME };
		accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
		accessibilityServiceInfo.notificationTimeout = 10;
		setServiceInfo(accessibilityServiceInfo);
		// 4.0之后可通过xml进行配置,以下加入到Service里面
		/*
		 * <meta-data android:name="android.accessibilityservice"
		 * android:resource="@xml/accessibility" />
		 */
		Notifier.getInstance().notify(getString(R.string.app_name), getString(R.string.wechat_acc_service_start_notification), getString(R.string.wechat_acc_service_start_notification),
				Notifier.TYPE_WECHAT_SERVICE_RUNNING, false);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		log("新事件");
		Log.d("event", "class Name :" + event.getClassName());

		if (event == null)
			return;
		if(SettingHelper.getREAutoMode()){
			handleNotificationChange(event);
		}
		AccessibilityNodeInfo nodeInfo = event.getSource();
		if (nodeInfo == null) {
			return;
		}

		AccessibilityNodeInfo rowNode = nodeInfo;// we can also use getRootInActiveWindow() instead;
		if (rowNode == null) {
			log( "noteInfo is　null");
			return;
		}

		// String currentActivityName =
		// ActivityHelper.getTopActivityName(RedEnvelopeApplication.getInstance());
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			CharSequence currentActivityName = event.getClassName();
			if ("com.tencent.mm.ui.LauncherUI".equals(currentActivityName)) {// 聊天以及主页 chat page and the main page
				log( "Chat page");
				if (SettingHelper.getREAutoMode()) {
					handleChatPage(rowNode);
				}
			} else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"
					.equals(currentActivityName)) {//打开红包主页 red envelope open page
				log("LuckyMoneyReceiveUI page");
				if (SettingHelper.getREAutoMode()
						|| SettingHelper.getRESafeMode())
					handleLuckyMoneyReceivePage(rowNode);
			} else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"
					.equals(currentActivityName)) {
				Log.d("test","进入详情页 回到主页面");
					// 红包详情主页 red envelope detail page
				if (SettingHelper.getREAutoMode())
					handleLuckyMoneyDetailPage(rowNode);

			} else {

				log( currentActivityName + " page");
			}
		}
		//尝试任何时候都开红包
		handleChatPage(rowNode);
	}

	/** handle notification notice */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void handleNotificationChange(AccessibilityEvent event) {
		if (event == null)
			return;
		if (!(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {
			return;
		}
		log("通知事件");
		if (event.getParcelableData() instanceof Notification) {
			Notification notification = (Notification) event
					.getParcelableData();
			Log.d("消息", notification.tickerText.toString());
			if (notification.tickerText != null
					&& notification.tickerText.toString().contains(getString(R.string.wechat_acc_service_red_envelope_notification_identification))) {
				log("来红包啦 get red envelope message");
				RedEnvelopeHelper.openNotification(event);
			}
		}
	}

	/**
	 * 处理 主页面 和聊天页面的信息
	 * @param node
	 */
	public void handleChatPage(AccessibilityNodeInfo node) {
		log("handle chat page");
		if (node == null)
			return;
		AccessibilityNodeInfo tempNode=RedEnvelopeHelper.getLastWechatRedEnvelopeNode(node,this);
		click(tempNode);
	}

	public void handleLuckyMoneyReceivePage(AccessibilityNodeInfo node) {
		if (node == null)
			return;
		AccessibilityNodeInfo nodeDetail = RedEnvelopeHelper
				.getWechatRedEnvelopeOpenDetailNode(node);
		if (nodeDetail != null) {// the red envelope already opened
			Log.d("test","红包已经打开过了");
			if (SettingHelper.getREAutoMode())
				ActivityHelper.goHome(this);
		} else {
			Log.d("test","红包有数据");
			AccessibilityNodeInfo nodeOpen = RedEnvelopeHelper
					.getWechatRedEnvelopeOpenNode(node, this);
			click(nodeOpen);
		}
	}

	public void handleLuckyMoneyDetailPage(AccessibilityNodeInfo node) {
		if (node == null)
			return;
		CharSequence money = RedEnvelopeHelper.getWechatRedEnvelopeMoney(node);
		Toast.makeText(this,"恭喜你抢到了 "+ money +"元",Toast.LENGTH_LONG ).show();
		//返回主界面
		AccessibilityNodeInfo backButton = RedEnvelopeHelper.getWechatRedBack(node);
		click(backButton);
//        AccessibilityNodeInfo liuyan= RedEnvelopeHelper.findNodeInfoOneByText(node,"留言");

//        click(liuyan);
//		ActivityHelper.goHome(this);
	}
    void click(AccessibilityNodeInfo nodeInfo){
        if (nodeInfo != null) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            nodeInfo.recycle();
        } else {// this page is loading red envelope data, no action

        }
    }
	

	@Override
	public void onInterrupt() {
		log("onInterrupt");
	}

	public void onDestroy() {
		super.onDestroy();
		Notifier.getInstance().cancelByType(
				Notifier.TYPE_WECHAT_SERVICE_RUNNING);
	}

}
