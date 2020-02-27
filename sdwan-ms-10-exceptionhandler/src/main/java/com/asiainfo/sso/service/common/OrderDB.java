package com.asiainfo.sso.service.common;

import boss.net.pojo.So_ord_site_box;
import boss.net.pojo.So_order;
import boss.net.pojo.resource_config.Config_box;
import boss.net.pojo.resource_config.Config_conv;
import boss.net.pojo.resource_config.Config_prep;
import boss.net.service.*;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import boss.net.service.OrderClientService;
import boss.net.service.ProdClientService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Component
public class OrderDB {
    @Autowired
    public  OrderClientService orderClientService;
    @Autowired
    public ResConfigClientService ResConfigClientService;
    @Autowired
    public  ProdClientService prodClientService;

    private static OrderDB orderDB;

    @PostConstruct
    public void init(){
        orderDB=this;
        orderDB.orderClientService=this.orderClientService;
        orderDB.ResConfigClientService=this.ResConfigClientService;
        orderDB.prodClientService=this.prodClientService;
    }

    public  boolean updateOrderStatusInDB(So_order so_order, List<So_ord_site_box> so_ord_site_boxLs,String Status) throws Exception {

        System.out.println("order");
        //更改主订单和盒子订单状态为finish
        if(so_order != null) {
            so_order.setOrder_status(Status);
            so_order.setOrder_time(new Date());
            orderDB.orderClientService.update_so_order(so_order);
            if(so_ord_site_boxLs.size()>0){
                for(So_ord_site_box obj:so_ord_site_boxLs){
                    obj.setOrder_status(Status);
                    obj.setStatus_time(new Date());
                    Map map = MapHandler.object2Map(obj);
                    Logs.createLogs(MapHandler.object2Map(obj).toString());
                    //更新数据库
                    orderDB.orderClientService.update_so_ord_site_box(obj);
                }
            }
            return true;

        }else {
            return false;
        }

    }

    public  boolean updateOrder_confStatusInDB(List<Config_conv> config_convLs,List<Config_prep> config_prepLs,List<Config_box> config_boxLs,String status) throws Exception {
        try {

                for (Config_conv obj : config_convLs) {
                    obj.setConfig_state(status);
                    obj.setUpdate_time(new Date());
                    orderDB.ResConfigClientService.updateConfigConv(obj);
                }


                for (Config_prep obj : config_prepLs) {
                    obj.setConfig_state(status);
                    obj.setUpdate_time(new Date());
                    orderDB.ResConfigClientService.updateConfigPrep(obj);
                }

                for (Config_box obj : config_boxLs) {
                    obj.setConfig_State(status);
                    obj.setUpdate_time(new Date());
                    orderDB.ResConfigClientService.updateConfigBoxInfo(obj);
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
