package com.nearucenterplaza.redenvelopeassistant.service.core;

import java.util.ArrayList;
import java.util.List;

import com.nearucenterplaza.redenvelopeassistant.R;
import com.nearucenterplaza.redenvelopeassistant.data.RedEnv;
import com.nearucenterplaza.redenvelopeassistant.service.WechatAccService;
import com.nearucenterplaza.redenvelopeassistant.utils.XLog;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class RedEnvelopeHelper {


	//红包聊天页的 ITEM  id  com.tencent.mm:id/wm


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void openNotification(AccessibilityEvent event) {
        if( !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
	
    /**获得红包详情页面打开节点*/
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static AccessibilityNodeInfo getWechatRedEnvelopeOpenNode(AccessibilityNodeInfo info) {
		if (info == null)
			return null;
		List<AccessibilityNodeInfo> list = info.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ar6");
		AccessibilityNodeInfo tempNode=null;
		for(int i=0;i<list.size();i++){
			tempNode=list.get(i);
			XLog.e("WechatAccService", "e2ee"+tempNode.isVisibleToUser()+"-"+tempNode.isEnabled());
			if ("android.widget.Button".equals(tempNode.getClassName())&&tempNode.isVisibleToUser()){
				return tempNode;
			}
		}
		return null;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static AccessibilityNodeInfo getWechatRedEnvelopeOpenDetailNode(AccessibilityNodeInfo info) {
		if (info == null)
			return null;
		List<AccessibilityNodeInfo> list = info.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aqx");
		AccessibilityNodeInfo tempNode=null;
		for(int i=0;i<list.size();i++){
			tempNode=list.get(i);
			XLog.e("WechatAccService", "eee"+tempNode.isVisibleToUser()+"-"+tempNode.isEnabled());
			if ("android.widget.TextView".equals(tempNode.getClassName())&&tempNode.isVisibleToUser()){
				return tempNode;
			}
		}
		return null;
	}
	
	

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static boolean isWechatRedEnvelopeOpenNode(AccessibilityNodeInfo info) {
		if (info == null)
			return false;
		String residName = info.getViewIdResourceName();
		if ("com.tencent.mm:id/amt".equals(residName)) {
			if ("android.widget.Button".equals(info.getClassName())) {
				return true;
				/*AccessibilityNodeInfo infoChild22 = info.getChild(0);
				XLog.e(TAG, "red main layout2 "+infoChild22.getChildCount());
				if (infoChild22 != null && infoChild22.getChildCount() == 2 && "android.widget.RelativeLayout".equals(infoChild22.getClassName())) {
					XLog.e(TAG, "red main layout3");
					AccessibilityNodeInfo infoChild30 = infoChild22.getChild(0);
					if (infoChild30 != null && "微信红包".equals(infoChild30.getText() == null ? "" : infoChild30.getText().toString())) {
						XLog.e(TAG, "red main layout4");
						return true;

					}
				}*/
			}
		}
		return false;
	}
	

	/**
	 * 获取红包 里面的按钮信息
	 * @param info
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static AccessibilityNodeInfo getWechatRedEnvelopeOpenNode(AccessibilityNodeInfo info, Context context) {
		if (info == null)
			return null;
		List<AccessibilityNodeInfo> list = findNodeInfo(info, "com.tencent.mm:id/aww", context.getString(R.string.wechat_acc_service_red_envelope_open_btn_str));
		AccessibilityNodeInfo tempNode;
		for(int i=0;i<list.size();i++){
			tempNode=list.get(i);
			XLog.e("WechatAccService", "e2ee"+tempNode.isVisibleToUser()+"-"+tempNode.isEnabled());
			if ("android.widget.Button".equals(tempNode.getClassName())&&tempNode.isVisibleToUser()){
				return tempNode;
			}
		}
		return null;
	}

	/**
	 *
	 * 聊天页面
	 * 获取最后一个 红包
	 * @param node
	 * @param context
	 * @return
	 */
	public static AccessibilityNodeInfo getLastWechatRedEnvelopeNode(AccessibilityNodeInfo node, WechatAccService context) {
		if (node == null)
			return null;

		List<AccessibilityNodeInfo> resultList = findNodeInfo(node, "com.tencent.mm:id/wm", context.getString(R.string.wechat_acc_service_red_envelope_list_identification));

		if(resultList!=null && !resultList.isEmpty()) {
			Log.d("获取成功","发现"+resultList.size()+"个红包");
			for (AccessibilityNodeInfo nodeInfo :resultList){
				Log.d("hash", "hash" + nodeInfo.hashCode());
				List<RedEnv> a = RedEnv.find(RedEnv.class, "hash = ?", "" + nodeInfo.hashCode());
				if(a == null || a.isEmpty()){
					//没抢过
					AccessibilityNodeInfo parent = resultList.get(resultList.size()-1).getParent();
					new RedEnv(nodeInfo.hashCode(),System.currentTimeMillis()).save();
					return parent;
				}else{
					Log.d("test","红包已经抢过了");
				}
			}
		}
		return null;
	}

	public static CharSequence getWechatRedEnvelopeMoney(AccessibilityNodeInfo node) {
		List<AccessibilityNodeInfo> resultList = findNodeInfo(node, "com.tencent.mm:id/aub", null);

		if(resultList!=null && !resultList.isEmpty()) {
			CharSequence money = resultList.get(0).getText();
			Log.d("你得到了红包", "" + money);
			return money;
		}
		return null;
	}
	public static AccessibilityNodeInfo getWechatRedBack(AccessibilityNodeInfo node) {
//		List<AccessibilityNodeInfo> resultList = findNodeInfo(node, "com.tencent.mm:id/f5", null);
		List<AccessibilityNodeInfo> resultList = findNodeInfo(node, "com.tencent.mm:id/ei", null);
		if(resultList!=null && !resultList.isEmpty()) {
			return resultList.get(0);
		}
		return null;
	}

	public static AccessibilityNodeInfo findNodeInfoOneById(AccessibilityNodeInfo node, String id){
		return findNodeInfoOne(node,id,null);
	}

	public static AccessibilityNodeInfo findNodeInfoOneByText(AccessibilityNodeInfo node,String text){
		return findNodeInfoOne(node, null, text);
	}

	public static AccessibilityNodeInfo findNodeInfoOne(AccessibilityNodeInfo node, String id, String text){
		List<AccessibilityNodeInfo> a = findNodeInfo(node, id, text);
		if(a !=null && !a.isEmpty())return a.get(0);
		else return null;
	}

	public static List<AccessibilityNodeInfo> findNodeInfo(AccessibilityNodeInfo node, String id, String text){
		List<AccessibilityNodeInfo> resultList =new ArrayList<>();
		if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT && !TextUtils.isEmpty(id)) {
//			Log.d("findNodeInfo","using ID");
			resultList = node.findAccessibilityNodeInfosByViewId(id);
//			if( resultList == null || resultList.isEmpty())
//				Log.d("findNodeInfo","using ID empty");
		}

		if( resultList == null || resultList.isEmpty() && android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN && !TextUtils.isEmpty(text)) {
//			Log.d("findNodeInfo","using Name");
			resultList = node.findAccessibilityNodeInfosByText(text);
//			if( resultList == null || resultList.isEmpty())
//				Log.d("findNodeInfo","using Name empty");
		}
		return resultList;
	}


}
