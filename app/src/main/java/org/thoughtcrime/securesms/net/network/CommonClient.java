package org.thoughtcrime.securesms.net.network;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;


/**
 * Created by Author on 2020/4/29
 */

public class CommonClient {

    private static volatile Web3j instance;

    public static String rpc = "";

    public static Web3j instance(String rpc) {
        if (!rpc.equals(CommonClient.rpc)) {
            CommonClient.rpc = rpc;
            return newInstance(rpc);
        }
        CommonClient.rpc = rpc;
        if (instance == null) {
            synchronized (Web3j.class) {
                if (instance == null) {
                    instance = Web3j.build(new HttpService(rpc));
                }
            }
        }
        return instance;
    }

    public static Web3j newInstance(String rpc) {
        return instance = Web3j.build(new HttpService(rpc));
    }
}
