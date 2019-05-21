package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.FeedbackActivity;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.databinding.ActivityWalletInitBinding;
import systems.v.wallet.ui.BaseActivity;

public class WalletInitActivity extends BaseActivity implements View.OnClickListener {

    public static void launch(Activity from) {
        Intent intent = new Intent(from, WalletInitActivity.class);
        from.startActivity(intent);
        openAlpha(from);
    }

    private ActivityWalletInitBinding mBinding;
    private Dialog mUpdateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_init);
        initListener();
        showWarningDialog();

        FeedbackActivity.setBarImmersive(true);
        PgyCrashManager.register();

        new PgyUpdateManager.Builder()
                .setForced(false)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk， 默认为true
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        //没有更新是回调此方法
                        Log.d("pgyer", "there is no new version");
                    }
                    @Override
                    public void onUpdateAvailable(AppBean appBean) {
                        //有更新回调此方法
                        Log.d("pgyer", "there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());
                        //调用以下方法，DownloadFileListener 才有效；
                        //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                        final AppBean appBeanEx = appBean;
                        if (mUpdateDialog == null) {
                            mUpdateDialog = new AlertDialog.Builder(mActivity)
                                    .setTitle(R.string.update_title)
                                    .setMessage("")
                                    .setPositiveButton(R.string.basic_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PgyUpdateManager.downLoadApk(appBeanEx.getDownloadURL());
                                        }
                                    })
                                    .setNegativeButton(R.string.basic_alert_dialog_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                        }
                        mUpdateDialog.setCanceledOnTouchOutside(false);
                        mUpdateDialog.show();
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        //更新检测失败回调
                        //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                        Log.e("pgyer", "check update failed ", e);
                    }
                })
                //注意 ：
                //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                //想要使用蒲公英的默认下载进度的UI则不设置此方法
                .setDownloadFileListener(new DownloadFileListener() {
                    @Override
                    public void downloadFailed() {
                        //下载失败
                        System.out.println("download apk failed");
                    }

                    @Override
                    public void downloadSuccessful(Uri uri) {
                        System.out.println("download apk success");
                        // 使用蒲公英提供的安装方法提示用户 安装apk
                        PgyUpdateManager.installApk(uri);
                    }

                    @Override
                    public void onProgressUpdate(Integer... integers) {
                        System.out.println("update download apk progress" + integers);
                    }})
                .register();
    }

    @Override
    public void finish() {
        super.finish();
        closeAlpha(mActivity);
    }

    private void initListener() {
        mBinding.btnCreate.setOnClickListener(this);
        mBinding.btnImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_import:
                // import wallet
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_IMPORT);
                break;
            case R.id.btn_create:
                // create wallet
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_CREATE);
                break;
        }
    }


    private void showWarningDialog() {
        new AlertDialog.Builder(mActivity)
                .setIcon(R.drawable.basic_ico_alert)
                .setTitle(R.string.official_warning)
                .setMessage(R.string.wallet_init_official_warning_dialog_msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinding.llWalletInitContainer.setVisibility(View.VISIBLE);
                    }
                })
                .alert();
    }
}
