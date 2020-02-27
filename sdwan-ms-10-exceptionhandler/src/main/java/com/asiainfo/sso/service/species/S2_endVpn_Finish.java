package com.asiainfo.sso.service.species;

import boss.net.pojo.So_prod_site_box;
import boss.net.pojo.resource_config.Prod_inst_box;
import boss.net.pojo.resource_config.Prod_inst_conv;
import boss.net.pojo.resource_config.Prod_inst_prep;
import com.asiainfo.sso.service.base.Data_BaseClass;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.stereotype.Service;

import java.util.Date;

import static boss.net.staticDatas.StaticDataBase.DOWN;
import static boss.net.staticDatas.StaticDataBase.FINISH;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service(value = "S2-endVpn_Finish")
public class S2_endVpn_Finish extends Data_BaseClass {
    public void prod_box() throws Exception {
        for(So_prod_site_box obj:so_prod_site_boxLs){
            obj.setProd_state(DOWN);
            obj.setState_time(new Date());
            prodClientService.update_so_prod_site_box(obj);
        }
    }

    public void prod_conf() throws Exception {
        for(Prod_inst_conv obj:prod_inst_convLs){
            obj.setProd_inst_state(DOWN);
            obj.setUpdate_time(new Date());

            //todo 更新数据库
            ResConfigClientService.updateProdInstConv(obj);
            Logs.createLogs("Prod_inst_conv updateDB"+obj.toString());
        }
        for(Prod_inst_prep obj:prod_inst_prepLs){
            obj.setProd_inst_state(DOWN);
            obj.setUpdate_time(new Date());

            //todo 更新数据库
            ResConfigClientService.updateProdInstPrep(obj);
            Logs.createLogs("Prod_inst_prep updateDB"+obj.toString());
        }
        for(Prod_inst_box obj:prod_inst_boxLs){
            obj.setProd_inst_state(DOWN);
            obj.setUpdate_time(new Date());

            //todo 更新数据库
            ResConfigClientService.updateProdInstBox(obj);
            Logs.createLogs("Prod_inst_box updateDB"+obj.toString());
        }
    }
}
