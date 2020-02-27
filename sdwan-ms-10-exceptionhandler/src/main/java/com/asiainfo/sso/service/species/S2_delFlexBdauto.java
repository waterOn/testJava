package com.asiainfo.sso.service.species;

import boss.net.pojo.Hi_so_prod_inst;
import boss.net.pojo.Hi_so_prod_site_box;
import boss.net.pojo.So_ord_site_box;
import boss.net.pojo.So_prod_site_box;
import boss.net.util.DateFormat;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.*;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-12-16
 */
@Service(value = "S2-delFlexBd-auto_Finish")

public class S2_delFlexBdauto extends Data_BaseClass {

    public void prod_box() throws Exception {

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
}
