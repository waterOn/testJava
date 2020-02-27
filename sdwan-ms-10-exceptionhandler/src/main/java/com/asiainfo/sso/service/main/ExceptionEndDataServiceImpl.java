package com.asiainfo.sso.service.main;

import boss.net.pojo.So_order;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.service.base.Exception_BaseClass;
import com.asiainfo.sso.util.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service
public class ExceptionEndDataServiceImpl implements ExceptionEndDataService {

    @Override
    public Boolean mainHandler(So_order obj, Map<String, String> orderIdQueryMap) throws Exception {
//        ExceptionException_BaseClass inst = new Exception_BaseClass();
        Boolean returnValue = false;
        try {
        Exception_BaseClass inst = SpringContextHolder.getBean("Exception_BaseClass");
        inst.init(obj,orderIdQueryMap);
        //处理各个表的状态
        inst.order();  //So_order,So_ord_site_box
        //inst.order_conf();//Config_conv,Config_prep,Config_box
        inst.prod();   //So_prod_inst
        inst.prod_box(); //So_prod_site_box
        inst.prod_conf();  //Prod_inst_conv,Prod_inst_prep,Prod_inst_box
        inst.resource();   //Res_vpn,Res_vlan_id,Res_pre_gw_unit_vpn,Res_conv_gw_unit_vpn
    }catch (Exception e){
        e.printStackTrace();
        new RuntimeException(e.getMessage());
    }
         return returnValue;
}}
