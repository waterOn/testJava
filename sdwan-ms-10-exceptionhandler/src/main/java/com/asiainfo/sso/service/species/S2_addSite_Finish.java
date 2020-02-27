package com.asiainfo.sso.service.species;

import boss.net.pojo.*;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.pojo.resource_config.Prod_inst_box;
import boss.net.pojo.resource_config.Prod_inst_conv;
import boss.net.pojo.resource_config.Prod_inst_prep;
import boss.net.util.FormatDate;
import boss.net.util.GetUUID;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.*;
import static boss.net.util.FormatDate.GenerateOneDate;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service(value = "S2-addSite_Finish")
public class S2_addSite_Finish extends Data_BaseClass {
    public void prod_box() throws Exception {
        for (So_ord_site_box obj:newOrd_site_boxList){
            So_prod_site_box inst = TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, so_order.getProd_inst_id().toString(), FINISH);
            inst.setFlow_type("long");
            obj.setStart_time(obj.getStart_time() != null ? obj.getStart_time():so_order.getStart_time());
            obj.setEnd_time(so_prod_inst.getEnd_time() != null ? so_prod_inst.getEnd_time() : "empty");

            // todo 插入数据库
            prodClientService.add_prod_site_box(inst);
            Logs.createLogs("So_prod_site_box insert:" + inst.toString());

            Hi_so_prod_site_box addObj = TypeCastCP.So_prod_site_boxToHi_so_prod_site_box(inst, so_order.getProd_inst_id().toString(), S2NEW);
            prodClientService.insertHi_so_prod_site_box(addObj);
            Logs.createLogs("Hi_so_prod_site_box insertDB:" + addObj.toString());

        }


    }



    @Override
    public void resource() throws Exception{
        for(Res_vpn obj:res_vpnLs){
            obj.setIs_occupy(OCCUPY);
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResVpn(obj);
        }
        for(Res_vlan_id obj:res_vlan_idLs){
            obj.setIs_occupy(OCCUPY);
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResVlanId(obj);
        }
        for(Res_pre_gw_unit_vpn obj:res_pre_gw_unit_vpnLs){
            obj.setIs_occupy(OCCUPY);
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResPreGwUnitVpn(obj);
        }
        for(Res_conv_gw_unit_vpn obj:res_conv_gw_unit_vpnLs){
            obj.setIs_occupy(OCCUPY);
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResConvGwUnitVpn(obj);
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
            Hi_so_prod_site_box inst = TypeCastCP.So_prod_site_boxToHi_so_prod_site_box(obj, so_order.getProd_inst_id().toString(), S2ADDSITE);
            prodClientService.insertHi_so_prod_site_box(inst);
            Logs.createLogs("Hi_so_prod_site_box insertDB:" + inst.toString());

        }

    }

    //历史主实例表录入
    public void hi_so_prod_inst() throws Exception{
        Hi_so_prod_inst inst = TypeCastCP.So_prod_instToHi_so_prod_inst(so_prod_inst, so_order.getProd_inst_id().toString(), S2ADDSITE);
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
            longDetail.setSerialNumber(obj.getSerialNumber()==null?"empty":obj.getSerialNumber());
            longDetail.setSale(Double.valueOf(so_order.getNet_base_sale().replace("%",""))*0.01);
            try {
            Date start_time = FormatDate.StringToDate(obj.getStart_time() == null?"2020-01-01":obj.getStart_time());


            Date start_time_addOne = GenerateOneDate(start_time);
            longDetail.setStart_time(FormatDate.dateToStr(start_time_addOne));
//            longDetail.setStart_time(obj.getStart_time());
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
