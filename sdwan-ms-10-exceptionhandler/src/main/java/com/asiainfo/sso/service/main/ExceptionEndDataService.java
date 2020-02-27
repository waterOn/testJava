package com.asiainfo.sso.service.main;

import boss.net.pojo.So_order;

import java.util.Map;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
public interface ExceptionEndDataService {
    Boolean mainHandler(So_order obj, Map<String,String> orderIdQueryMap) throws Exception;
}
