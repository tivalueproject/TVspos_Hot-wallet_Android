package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import systems.v.wallet.data.bean.RespBean;

public class NodeAPI implements ITestNetNodeAPI, IMainNetNodeAPI {

    private ITestNetNodeAPI mTestNodeAPI;
    private IMainNetNodeAPI mMainNodeAPI;

    public NodeAPI(IMainNetNodeAPI m, ITestNetNodeAPI t) {
        mMainNodeAPI = m;
        mTestNodeAPI = t;
    }

    @Override
    public Observable<RespBean> records(String address, int limit) {
        if (mTestNodeAPI != null) {
            return mTestNodeAPI.records(address, limit);
        }
        return mMainNodeAPI.records(address, limit);
    }

    @Override
    public Observable<RespBean> balance(String address) {
        if (mTestNodeAPI != null) {
            return mTestNodeAPI.balance(address);
        }
        return mMainNodeAPI.balance(address);
    }

    @Override
    public Observable<RespBean> payment(Map<String, Object> payment) {
        if (mTestNodeAPI != null) {
            return mTestNodeAPI.payment(payment);
        }
        return mMainNodeAPI.payment(payment);
    }

    @Override
    public Observable<RespBean> lease(Map<String, Object> lease) {
        if (mTestNodeAPI != null) {
            return mTestNodeAPI.lease(lease);
        }
        return mMainNodeAPI.lease(lease);
    }

    @Override
    public Observable<RespBean> cancelLease(Map<String, Object> cancel) {
        if (mTestNodeAPI != null) {
            return mTestNodeAPI.cancelLease(cancel);
        }
        return mMainNodeAPI.cancelLease(cancel);
    }
}
