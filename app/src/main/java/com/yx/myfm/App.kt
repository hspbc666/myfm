package com.yx.myfm

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.yx.myfm.receiver.MyPlayerReceiver
import com.yx.myfm.util.AppContext
import com.umeng.commonsdk.UMConfigure
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest
import com.ximalaya.ting.android.opensdk.player.appnotification.XmNotificationCreater
import com.ximalaya.ting.android.opensdk.util.BaseUtil
import com.ximalaya.ting.android.opensdk.util.Logger
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager
import com.ximalaya.ting.android.sdkdownloader.http.RequestParams
import com.ximalaya.ting.android.sdkdownloader.http.app.RequestTracker
import com.ximalaya.ting.android.sdkdownloader.http.request.UriRequest

/**
 * @Description:
 * @Author:      jerry
 * @CreateDate:  2020-06-14 15:06
 * @Email:       309032663@qq.com
 */
open class App : Application() {

    private val requestTracker = object : RequestTracker {
        override fun onWaiting(params: RequestParams) {
            Logger.log("TingApplication : onWaiting $params")
        }

        override fun onStart(params: RequestParams) {
            Logger.log("TingApplication : onStart $params")
        }

        override fun onRequestCreated(request: UriRequest) {
            Logger.log("TingApplication : onRequestCreated $request")
        }

        override fun onSuccess(request: UriRequest, result: Any) {
            Logger.log("TingApplication : onSuccess $request   result = $result")
        }

        override fun onRemoved(request: UriRequest) {
            Logger.log("TingApplication : onRemoved $request")
        }

        override fun onCancelled(request: UriRequest) {
            Logger.log("TingApplication : onCanclelled $request")
        }

        override fun onError(request: UriRequest, ex: Throwable, isCallbackError: Boolean) {
            Logger.log("TingApplication : onError $request   ex = $ex   isCallbackError = $isCallbackError")
        }

        override fun onFinished(request: UriRequest) {
            Logger.log("TingApplication : onFinished $request")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val mXimalaya = CommonRequest.getInstanse()
        val mAppSecret = "6a7986a3e4326f196b4cf50c1483f09b"
        mXimalaya.setAppkey("9e7d0b81dd09d3a5c047c5ff841f8691")
        mXimalaya.setPackid("com.rickon.ximalayakotlin")
        mXimalaya.init(this, mAppSecret)

        AppContext.initialize(this)

        /**
         * ??????: ??????????????????AndroidManifest.xml????????????appkey???channel??????????????????App????????????
         * ????????????????????????????????????AndroidManifest.xml???????????????appkey???channel??????
         * UMConfigure.init?????????appkey???channel???????????????null??????
         */
        val appkey = "5e4172ddcb23d27e2a00015e"
        val channel = "github"
        UMConfigure.init(this,appkey, channel, UMConfigure.DEVICE_TYPE_PHONE, null)

        val mp3 = getExternalFilesDir("mp3")?.absolutePath
        println("?????????  $mp3")

        if (!BaseUtil.isPlayerProcess(this)) {
            XmDownloadManager.Builder(this)
                    .maxDownloadThread(3)            // ????????????????????? ?????????1 ?????????3
                    .maxSpaceSize(java.lang.Long.MAX_VALUE)    // ????????????????????????????????????????????????????????????????????????????????????
                    .connectionTimeOut(15000)        // ?????????????????????????????? ,???????????? ?????? 30000
                    .readTimeOut(15000)                // ?????????????????????????????? ,???????????? ?????? 30000
                    .fifo(false)                    // ???????????????????????????????????????????????????. false???????????????????????????(????????????????????????????????????????????????) ?????????true
                    .maxRetryCount(3)                // ???????????????????????? ??????2???
                    .progressCallBackMaxTimeSpan(1000)//  ?????????progress ??????????????? ?????????800
                    .requestTracker(requestTracker)    // ?????? ????????????????????????
                    .savePath(mp3)    // ??????????????? ?????????????????????????????????
                    .create()
        }

        if (BaseUtil.getCurProcessName(this).contains(":player")) {
            val instanse = XmNotificationCreater.getInstanse(this)
            instanse.setNextPendingIntent(null as PendingIntent?)
            instanse.setPrePendingIntent(null as PendingIntent?)

            val actionName = "com.rickon.ximalayakotlin.Action_Close"
            val intent = Intent(actionName)
            intent.setClass(this, MyPlayerReceiver::class.java)
            val broadcast = PendingIntent.getBroadcast(this, 0, intent, 0)
            instanse.setClosePendingIntent(broadcast)

            val pauseActionName = "com.rickon.ximalayakotlin.Action_PAUSE_START"
            val intent1 = Intent(pauseActionName)
            intent1.setClass(this, MyPlayerReceiver::class.java)
            val broadcast1 = PendingIntent.getBroadcast(this, 0, intent1, 0)
            instanse.setStartOrPausePendingIntent(broadcast1)
        }
    }

}
