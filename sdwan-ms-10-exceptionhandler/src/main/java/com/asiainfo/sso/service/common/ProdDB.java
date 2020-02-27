package com.asiainfo.sso.service.common;

import boss.net.pojo.So_prod_inst;
import boss.net.pojo.So_prod_site_box;
import boss.net.service.OrderClientService;
import boss.net.service.ProdClientService;
import boss.net.service.ResConfigClientService;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

import static boss.net.staticDatas.StaticDataBase.*;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Component
public class ProdDB {
    @Autowired
    public  OrderClientService orderClientService;
    @Autowired
    public boss.net.service.ResConfigClientService ResConfigClientService;
    @Autowired
    public  ProdClientService prodClientService;

    private static ProdDB prodDB;

    @PostConstruct
    public void init(){
        prodDB=this;
        prodDB.orderClientService=this.orderClientService;
        prodDB.ResConfigClientService=this.ResConfigClientService;
        prodDB.prodClientService=this.prodClientService;
    }

    public static boolean updateProdStatusInDB(So_prod_inst so_prod_inst,String status) throws Exception {
        try {
            if (so_prod_inst != null) {
                Logs.createLogs("updateProdStatusInDB的staus"+status);
                so_prod_inst.setProd_state(status);
                so_prod_inst.setState_time(new Date());
                Logs.createLogs("update so_prod_inst 调用prodClientService.update_so_prod_inst接口");
                Logs.createLogs("入参："+so_prod_inst.toString());
                Logs.createLogs("prod_state"+so_prod_inst.getProd_state());
                // 主实例的更新数据库
                prodDB.prodClientService.update_so_prod_inst(so_prod_inst);
                Logs.createLogs("so_prod_inst updateDB:"+so_prod_inst.toString());
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
