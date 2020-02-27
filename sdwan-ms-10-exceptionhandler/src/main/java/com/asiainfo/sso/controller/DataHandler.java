package com.asiainfo.sso.controller;


import boss.net.pojo.So_order;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.service.OrderClientService;
import boss.net.util.JsonRequest;
import com.asiainfo.sso.service.base.Exception_BaseClass;
import com.asiainfo.sso.service.main.ExceptionEndDataService;
import com.asiainfo.sso.service.main.MainService;
import com.asiainfo.sso.util.Logs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static boss.net.staticDatas.StaticDataBase.*;
import static boss.net.util.StaticDataDef.Order_orderType.*;

/**
 * @Author:chengpei
 * @Desc  业务竣工数据处理，异常数据处理
 * @Date:Created in 2019-09-03
 */
@RestController
@Slf4j
public class DataHandler {
    @Autowired
    MainService mainService;
    @Autowired(required = false)
    OrderClientService orderClientService;
    @Autowired
    ExceptionEndDataService exceptionEndDataService;


    @RequestMapping(value = "/data/handler", method = RequestMethod.POST)
    public Boolean dataInceptor(@RequestParam(value = "order_id") String order_id, @RequestParam(value = "type")String type)  {
        Logs.createLogs("启动数据处理");
        Logs.createLogs("order_id:"+order_id);
        Logs.createLogs("type:"+type);
        Boolean returnValue = false;
        if(order_id==null || type==null || order_id.length()==0 || type.length()==0) {
            Logs.createLogs("参数为空");
            return false;

        }




        Logs.createLogs("调用订单服务查询主订单信息");
        Map<String,String> orderIdQueryMap = new HashMap<>();
        orderIdQueryMap.put("order_id",order_id);
        Logs.createLogs("发送给订单服务的参数是："+orderIdQueryMap);
        try {
            List<So_order> main_orderList = orderClientService.select_so_order(orderIdQueryMap);
            Logs.createLogs("order_id:"+order_id+"  查出的结果数量为"+main_orderList.size());
            if (main_orderList.size() ==1) {
                So_order obj = (So_order) main_orderList.get(0);
                Logs.createLogs("order_id:"+order_id+"查出的订单状态为："+obj.getOrder_status());
                if(obj.getOrder_status()!=null && !obj.getOrder_status().equalsIgnoreCase(FINISH)&& !obj.getOrder_status().equalsIgnoreCase(DOWN)){
                    Logs.createLogs("订单类型："+obj.getOrder_type());
                    if(type.equalsIgnoreCase(FINISH_DATAHANDLER)){
                        Logs.createLogs("竣工数据处理");
                        if(mainService.mainHandler(obj,orderIdQueryMap))
                            returnValue = true;


                    }
                    if(type.equalsIgnoreCase(EXCEPTION_DATAHANDLER)){
                        Logs.createLogs("异常数据处理");
                        exceptionEndDataService.mainHandler(obj,orderIdQueryMap);
                        returnValue = true;
                    }

                }

            }


        }catch (Exception  e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        Logs.createLogs("return value:"+returnValue);
        return  returnValue;
    }



}
