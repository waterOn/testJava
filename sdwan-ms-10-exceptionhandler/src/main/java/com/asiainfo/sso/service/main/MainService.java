package com.asiainfo.sso.service.main;

import boss.net.pojo.So_order;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-06
 */
public interface MainService {
    Boolean mainHandler(So_order obj, Map<String,String> orderIdQueryMap) throws Exception;

}
