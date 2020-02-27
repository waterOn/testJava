package com.asiainfo.sso.service.species;

import boss.net.pojo.Hi_so_prod_inst;
import boss.net.pojo.Hi_so_prod_site_box;
import boss.net.pojo.So_ord_site_box;
import boss.net.pojo.So_prod_site_box;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.util.DateFormat;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.FINISH;
import static boss.net.staticDatas.StaticDataBase.S2LongTermBandwidth;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-12-16
 */
@Service(value = "S2-delFlexBd-user_Finish")
public class S2_delFlexBduser extends Data_BaseClass {

    public void prod_box() throws Exception {
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();


        //更新需要更新的盒子实例对象
        for (So_prod_site_box obj : needUpdate_siteBoxList) {
            obj.setProd_state(FINISH);
            obj.setState_time(new Date());
            obj.setFlow_type("long");
            obj.setStart_time(DateFormat.dateToStrYmd());
            obj.setEnd_time(so_prod_inst.getEnd_time());

            // 更新数据库
            prodClientService.update_so_prod_site_boxCP(obj);
            Logs.createLogs("so_prod_site_box updateDB:" + obj.toString());

        }

    }


    //盒子历史实例表hi_so_prod_site_box录入
    public void hi_so_prod_site_box() throws Exception {
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


    public void feeData() {
        //遍历新的盒子
        for (So_ord_site_box obj : so_ord_site_boxLs) {
            //查询数据 todo
            try {
                List<Bill_box_detail> Bill_box_detailLs = billClientService.searchBillBoxDetail(so_order.getProd_inst_id().toString(), obj.getSite_name(), "flex");
                Logs.createLogs("search Bill_box_detail size:" + Bill_box_detailLs.size());
                if (Bill_box_detailLs.size() > 0) {

                    //修改bill_box_detail，灵活带宽截止日期
                    Bill_box_detail longDetail = Bill_box_detailLs.get(0);
                    longDetail.setEnd_time(DateFormat.dateToStrYmd());
                    billClientService.updateBillBoxDetail(longDetail);
                    Logs.createLogs("Bill_box_detail  flexDetail  End_time:" + longDetail.getEnd_time());
                    Logs.createLogs("Bill_box_detail  flexDetail  updateDB:" + longDetail.toString());

                }} catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
