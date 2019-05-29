package systems.v.wallet.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.main.MainActivity;
import systems.v.wallet.ui.view.wallet.WalletInitActivity;
import systems.v.wallet.utils.PermissionUtil;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.DownloadUtil;
import systems.v.wallet.utils.IntentUtils;

public class SplashActivity extends BaseActivity {

    static String  appUrl = "https://link-e-pro.oss-cn-beijing.aliyuncs.com/wallet/hot.wallet_release.apk";
    static String  serverUrl = "http://47.75.180.164:8080/v1/appVsersion";
    private Dialog mUpdateDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Bundle data = msg.getData();
                    String val = data.getString("value");
                    try {
                        JSONObject object = new JSONObject(val);
                        final  int serverVersion = object.getJSONObject("data").getInt("hotAppVersion");
                        if(serverVersion > GetCurrentAppVersion()){
                            if (mUpdateDialog == null) {
                                mUpdateDialog = new AlertDialog.Builder(mActivity)
                                        .setTitle(R.string.update_title)
                                        .setMessage("")
                                        .setPositiveButton(R.string.basic_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                DownloadApp(appUrl,String.valueOf(serverVersion));
                                                launch();
                                            }
                                        })
                                        .setNegativeButton(R.string.basic_alert_dialog_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                launch();
                                            }
                                        }).create();
                            }
                            mUpdateDialog.setCanceledOnTouchOutside(false);
                            mUpdateDialog.show();
                        }
                        else {
                            launch();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(){
                public void run(){
                    String result = sendGet(serverUrl,"");
                    Message msg = Message.obtain();
                    Bundle data = new Bundle();
                    data.putString("value",result);
                    msg.setData(data);
                    if (result.isEmpty()){
                        launch();
                    }else {
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        }
    }

    private int GetCurrentAppVersion(){
        int versionCode = 0;
        try {
            PackageInfo pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            versionCode = pkg.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  versionCode;
    }

    public static String sendGet(String url, String param)
    {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String getCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    private void DownloadApp(String url,String version){
        File file = new File(getCacheDir(mActivity), "hot.wallet_v"  + version + ".apk");
        if (file.exists()) {
            IntentUtils.installApk(mActivity, file);
            return;
        }
        DownloadUtil.DownloadParam downloadParam = new DownloadUtil.DownloadParam(url, file);
        new DownloadUtil(new DownloadUtil.DownloadCallBack() {
            @Override
            public void downLoadStart(DownloadUtil.DownloadParam param) {
//                pb_update.setVisibility(View.VISIBLE);
//                tv_update.setVisibility(View.GONE);
            }

            @Override
            public void onProgressUpdate(DownloadUtil.DownloadParam param, int downloadSize, int totalSize) {
//                pb_update.setMax(totalSize);
//                pb_update.setProgress(downloadSize);
            }

            @Override
            public void downloadEnd(DownloadUtil.DownloadParam param) {
                if (param.getSaveFile().exists()) {
                    IntentUtils.installApk(mActivity, param.getSaveFile());
                }
//                pb_update.setVisibility(View.GONE);
//                tv_update.setVisibility(View.VISIBLE);
            }
        }).download(downloadParam);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE:
                if (!PermissionUtil.permissionGranted(this)) {
                    ToastUtil.showToast(R.string.grant_permissions);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    launch();
                }
        }
    }

    private void launch() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FileUtil.walletExists(mActivity)) {
                    MainActivity.launch(SplashActivity.this, false);
                } else {
                    WalletInitActivity.launch(mActivity);
                }
                finish();
            }
        }, 500);

    }
}
