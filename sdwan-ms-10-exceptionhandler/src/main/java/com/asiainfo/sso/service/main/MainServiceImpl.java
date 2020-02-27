package com.asiainfo.sso.service.main;

import boss.net.pojo.So_order;
import boss.net.pojo.Work_order_ms;
import boss.net.service.OrderClientService;
import boss.net.service.BillClientService;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.service.species.S2New_Finish;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.SpringContextHolder;
//import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static boss.net.util.WorkFlowStaticVar.*;
import static boss.net.util.WorkFlowStaticVar.BoxOnLineCheckReset;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-06
 */
@Service
public class MainServiceImpl implements MainService{
    @Autowired(required = false)
    public OrderClientService orderClientService;


    @Value(value = "${fee.mark}")
    private String feeMark;

    @Override
//    @LcnTransaction
    public Boolean mainHandler(So_order obj, Map<String,String> orderIdQueryMap) throws Exception {
        Boolean returnValue = false;
        String species = obj.getOrder_type()+"_Finish";
        try {
            Data_BaseClass handler = SpringContextHolder.getBean(species);
            Logs.createLogs("init datas");
            //初始化数据
            handler.init(obj, orderIdQueryMap);
            Logs.createLogs("handler order");
            //处理各个表的状态

            //handler.order_conf();//Config_conv,Config_prep,Config_box


            Logs.createLogs("handler prod_box");
            handler.prod_box(); //So_prod_site_box
            Logs.createLogs("handler prod_conf");
            handler.prod_conf();  //Prod_inst_conv,Prod_inst_prep,Prod_inst_box
            Logs.createLogs("handler resource");
            handler.resource();   //Res_vpn,Res_vlan_id
            handler.order();  //So_order,So_ord_site_box
            Logs.createLogs("handler prod");
            handler.prod();   //So_prod_inst
            handler.current_lt_box();//盒子的长期带宽属性表current_lt_box录入
            handler.hi_so_prod_site_box();//盒子历史实例表hi_so_prod_site_box录入
            handler.hi_so_prod_inst();//历史主实例表录入

            handler.clearCache(); //清除缓存

            Logs.createLogs("feeMark："+feeMark);
            if(Objects.nonNull(feeMark)&& "true".equals(feeMark)){
                Logs.createLogs("fee data：");
                handler.feeData();//计费模块数据

            }


            returnValue=true;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("竣工运行异常："+e.getMessage());
        }finally {
            Work_order_ms work_order_ms = new Work_order_ms();
            work_order_ms.setStart_time(new Date());
            work_order_ms.setTask_desc("竣工重做");
            if(returnValue){
                work_order_ms.setWork_order_status("finish");
            }else {
                work_order_ms.setWork_order_status("ing");
            }

            work_order_ms.setWork_order_type("竣工");
            work_order_ms.setEnd_time(new Date());
            work_order_ms.setOrder_id(obj.getOrder_id());
            orderClientService.insert_work_order_ms_only(work_order_ms);
        }
         return returnValue;

    }


}
