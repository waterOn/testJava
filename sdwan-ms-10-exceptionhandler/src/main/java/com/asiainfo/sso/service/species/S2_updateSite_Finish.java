package com.asiainfo.sso.service.species;

import boss.net.pojo.*;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.util.DateFormat;
import boss.net.util.GetUUID;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.*;
import static boss.net.staticDatas.StaticDataBase.S2LongTermBandwidth;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service(value = "S2-updateSite_Finish")
public class S2_updateSite_Finish extends Data_BaseClass {


    public void prod_box() throws Exception {
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();



        //更新需要更新的盒子实例对象
        for (So_prod_site_box obj : needUpdate_siteBoxList) {
            obj.setProd_state(FINISH);
            obj.setState_time(new Date());
            obj.setFlow_type("flex");
            obj.setLink_order_id(so_order.getOrder_id());


            // 更新数据库
            prodClientService.update_so_prod_site_boxCP(obj);
            Logs.createLogs("so_prod_site_box updateDB:" + obj.toString());

        }

    }


    //盒子历史实例表hi_so_prod_site_box录入
    public void hi_so_prod_site_box() throws Exception {
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();




        for (So_prod_site_box obj : needUpdate_siteBoxList) {
            Hi_so_prod_site_box inst = TypeCastCP.So_prod_site_boxToHi_so_prod_site_box(obj, so_order.getProd_inst_id().toString(), S2LongTermBandwidth);
            prodClientService.insertHi_so_prod_site_box(inst);
            Logs.createLogs("Hi_so_prod_site_box insertDB:" + inst.toString());

        }

    }

    //历史主实例表录入
    public void hi_so_prod_inst() throws Exception {
        Hi_so_prod_inst inst = TypeCastCP.So_prod_instToHi_so_prod_inst(so_prod_inst, so_order.getProd_inst_id().toString(), S2LongTermBandwidth);
        prodClientService.insertHi_so_prod_inst(inst);
        Logs.createLogs("Hi_so_prod_inst insertDB:" + inst.toString());
    }


    public void resource() {
    }


    public void feeData() {
        //遍历新的盒子
        for(So_ord_site_box obj:so_ord_site_boxLs){
            //录入bill_box_detail，灵活带宽
            Bill_box_detail longDetail = new Bill_box_detail();
            // TODO: 2020-01-02
            String main_id = GetUUID.getYear()+"-"+GetUUID.getMonth()+so_order.getProd_inst_id()+obj.getSite_name();
            longDetail.setMain_id(main_id);
            longDetail.setCust_id(so_order.getCust_id().toString());
            longDetail.setProd_inst_id(so_order.getProd_inst_id().toString());
            longDetail.setSite_id("empty");
            longDetail.setSite_name(obj.getSite_name());
            longDetail.setSerialNumber(obj.getSerialNumber()==null?"empty":obj.getSerialNumber());
            longDetail.setSale(Double.valueOf(so_prod_inst.getNet_base_sale()==null?"60":so_prod_inst.getNet_base_sale().replace("%",""))*0.01);
            longDetail.setStart_time(obj.getStart_time());
            longDetail.setEnd_time(obj.getEnd_time());
            longDetail.setBandwidth(obj.getBox_bandwith().toString());
            longDetail.setMark("1");
            longDetail.setBill_type("flex");
            try {
                billClientService.insertBillBoxDetail(longDetail);
                Logs.createLogs("Bill_box_detail  flexDetail   End_time:"+longDetail.getEnd_time());
                Logs.createLogs("Bill_box_detail  flexDetail   insertDB:"+longDetail.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
