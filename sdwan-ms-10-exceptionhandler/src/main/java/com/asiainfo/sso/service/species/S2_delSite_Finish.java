package com.asiainfo.sso.service.species;

import boss.net.pojo.So_ord_site_box;
import boss.net.pojo.So_prod_site_box;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.util.DateFormat;
import boss.net.util.GetUUID;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.DOWN;
import static boss.net.staticDatas.StaticDataBase.FINISH;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service(value = "S2-delSite_Finish")
public class S2_delSite_Finish extends Data_BaseClass {
    public void prod_box() throws Exception {
       String prod_inst_id = so_order.getProd_inst_id().toString();

        for (So_ord_site_box obj : so_ord_site_boxLs) {
            System.out.println("订单中的盒子" + obj.getConf_state() + " " + obj.getRun_state());
            //新的盒子订单转换为——》新的盒子实例
            So_prod_site_box pojo = TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, prod_inst_id, DOWN);
            System.out.println("实例中的盒子" + pojo.getConf_state() + " " + pojo.getRun_state());
            //todo 插入数据库
            prodClientService.update_so_prod_site_box(pojo);
//                logger.warn("新的盒子ylwsiteid:"+pojo.getYlw_site_id());
//                logger.warn("运行状态："+pojo.getRun_state());
//                logger.warn("配置状态："+pojo.getConf_state());

        }
    }
    public void resource() throws Exception{}

    public void feeData() {
        //遍历新的盒子
        for (So_ord_site_box obj : so_ord_site_boxLs) {
            //查询数据 todo
            try {
                List<Bill_box_detail> Bill_box_detailLs = billClientService.searchBillBoxDetail(so_order.getProd_inst_id().toString(), obj.getSite_name(), "long");
                Logs.createLogs("search Bill_box_detail size:" + Bill_box_detailLs.size());
                if (Bill_box_detailLs.size() >0) {

                    //修改bill_box_detail，灵活带宽截止日期
                    Bill_box_detail longDetail = Bill_box_detailLs.get(0);
                    longDetail.setEnd_time(DateFormat.dateToStrYmd());
                    billClientService.updateBillBoxDetail(longDetail);
                    Logs.createLogs("Bill_box_detail  flexDetail  End_time:" + longDetail.getEnd_time());
                    Logs.createLogs("Bill_box_detail  flexDetail  updateDB:" + longDetail.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
