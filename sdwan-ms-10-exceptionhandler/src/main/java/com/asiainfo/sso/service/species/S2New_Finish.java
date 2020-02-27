package com.asiainfo.sso.service.species;

import boss.net.pojo.*;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.util.FormatDate;
import boss.net.util.GetUUID;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.service.common.ProdDB;
import com.asiainfo.sso.util.Logs;
import org.springframework.stereotype.Service;

import java.util.Date;

import static boss.net.staticDatas.StaticDataBase.FINISH;
import static boss.net.staticDatas.StaticDataBase.S2NEW;
import static boss.net.util.FormatDate.GenerateOneDate;

/**
 * @Author:chengpei
 * @Desc 场景二新订购  处理同父类
 * @Date:Created in 2019-09-06
 */
@Service("S2-New_Finish")
public class S2New_Finish extends Data_BaseClass {

    public void prod() throws Exception {
        if (so_prod_inst != null) {
            //更新主实例中的带宽折扣
            String Net_base_sale = so_order.getNet_base_sale();
            so_prod_inst.setNet_base_sale(Net_base_sale == null || Net_base_sale.equalsIgnoreCase("") ? "30%" : Net_base_sale);
            Logs.createLogs("so_prod_inst updateSale:" + Net_base_sale);
            ProdDB.updateProdStatusInDB(so_prod_inst, FINISH);
            Logs.createLogs("so_prod_inst updateDB:" + so_prod_inst.toString());
        }

    }


    public void prod_box() throws Exception {
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();


        for (So_ord_site_box obj : so_ord_site_boxLs) {
            So_prod_site_box returnObj = TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, prod_inst_id, FINISH);
            returnObj.setSerialNumber(obj.getSerialNumber() != null ? obj.getSerialNumber() : "empty");
            returnObj.setFlow_type(returnObj.getFlow_type() == null ? "long" : returnObj.getFlow_type());
            returnObj.setStart_time(obj.getStart_time() != null ? obj.getStart_time() : so_order.getStart_time());
            returnObj.setEnd_time(so_order.getEnd_time() != null ? so_order.getEnd_time() : "empty");

            Logs.createLogs("so_prod_inst updateSerialNumber:" + returnObj.getSerialNumber());
            Logs.createLogs("so_prod_inst updateFlow_type:" + returnObj.getFlow_type());
            Logs.createLogs("so_prod_inst updateStart_time:" + returnObj.getStart_time());
            Logs.createLogs("so_prod_inst updateEnd_time:" + returnObj.getEnd_time());


            // 更新数据库
            prodClientService.update_so_prod_site_box(returnObj);
            Logs.createLogs("so_prod_site_box updateDB:" + returnObj.toString());
        }

    }

    //盒子的长期带宽属性表current_lt_box录入
    public void current_lt_box() throws Exception {
        for (So_ord_site_box obj : so_ord_site_boxLs) {
            Current_lt_box inst = TypeCastCP.So_ord_site_boxToCurrent_lt_box(obj, so_order.getProd_inst_id().toString());
            prodClientService.insertCurrent_lt_box(inst);
            Logs.createLogs("Current_lt_box insertDB:" + inst.toString());

        }

    }


    //盒子历史实例表hi_so_prod_site_box录入
    public void hi_so_prod_site_box() throws Exception {
        for (So_prod_site_box obj : so_prod_site_boxLs) {
            Hi_so_prod_site_box inst = TypeCastCP.So_prod_site_boxToHi_so_prod_site_box(obj, so_order.getProd_inst_id().toString(), S2NEW);
            prodClientService.insertHi_so_prod_site_box(inst);
            Logs.createLogs("Hi_so_prod_site_box insertDB:" + inst.toString());

        }

    }

    //历史主实例表录入
    public void hi_so_prod_inst() throws Exception {
        Hi_so_prod_inst inst = TypeCastCP.So_prod_instToHi_so_prod_inst(so_prod_inst, so_order.getProd_inst_id().toString(), S2NEW);
        prodClientService.insertHi_so_prod_inst(inst);
        Logs.createLogs("Hi_so_prod_inst insertDB:" + inst.toString());
    }

    @Override
    public void feeData() {


//遍历新的盒子
        for (So_ord_site_box obj : so_ord_site_boxLs) {
            //录入bill_box_detail，长期带宽和盒子月租费用
            Bill_box_detail longDetail = new Bill_box_detail();
            // TODO: 2020-01-02
            String main_id = GetUUID.getYear() + "-" + GetUUID.getMonth() + so_order.getProd_inst_id() + obj.getSite_name();
            longDetail.setMain_id(main_id);
            longDetail.setCust_id(so_order.getCust_id().toString());
            longDetail.setProd_inst_id(so_order.getProd_inst_id().toString());
            longDetail.setSite_id("empty");
            longDetail.setSite_name(obj.getSite_name());
            longDetail.setSerialNumber(obj.getSerialNumber() == null ? "empty" : obj.getSerialNumber());
            longDetail.setSale(Double.valueOf(so_order.getNet_base_sale().replace("%", "")) * 0.01);
            //

            try {
                Date start_time = FormatDate.StringToDate(obj.getStart_time() == null ? "2020-01-01" : obj.getStart_time());


                Date start_time_addOne = GenerateOneDate(start_time);
                longDetail.setStart_time(FormatDate.dateToStr(start_time_addOne));
                longDetail.setEnd_time(FormatDate.dateToStr(FormatDate.lastDay()));
                longDetail.setBandwidth(obj.getBox_bandwith().toString());
                longDetail.setMark("1");
                longDetail.setBill_type("long");

                billClientService.insertBillBoxDetail(longDetail);
                Logs.createLogs("Bill_box_detail  longDetail  insertDB:" + longDetail.toString());
                longDetail.setOrigin_fee("0");
                longDetail.setBill_fee("0");
                longDetail.setBill_type("box_rent");
                billClientService.insertBillBoxDetail(longDetail);
                Logs.createLogs("Bill_box_detail  box_rent  insertDB:" + longDetail.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
