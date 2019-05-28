package systems.v.wallet.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private Dialog mUpdateDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            final int serverVersion = GetServerAppVersion();
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

    public static String doPost(String url, String param)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try
        {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl
                    .openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            // 设置通用的请求属性
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");

            if (param != null && !param.trim().equals(""))
            {
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result += line;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return result;
    }


    private int GetServerAppVersion(){
        int  versionCode = 3;

        return  versionCode;
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
