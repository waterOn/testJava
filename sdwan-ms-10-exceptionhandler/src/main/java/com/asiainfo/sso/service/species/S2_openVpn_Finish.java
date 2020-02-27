package com.asiainfo.sso.service.species;

import boss.net.pojo.resource_config.*;
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
 * @Date:Created in 2019-09-09
 */
@Service(value = "S2-openVpn_Finish")
public class S2_openVpn_Finish extends Data_BaseClass {
    public void resource() {}

    public void prod_conf() throws Exception {
        String prod_inst_id = so_order.getProd_inst_id().toString();
        //3.根据订单，更新和增加实例
        //处理汇聚实例
        List<Config_conv> newOrd_gatewayList = new ArrayList<>(config_convLs);
//        需要更新的汇聚实例
        List<Prod_inst_conv> needChange_gatewayList = new ArrayList<>();
        for (int m = config_convLs.size() - 1; m >= 0; m--) {
            Config_conv obj = config_convLs.get(m);
            Integer bandwidth = obj.getnLifBandWidth();
//            Integer pop = obj.getPop_area_id();
            String mgt_ip = obj.getGwDevID();
            for (Prod_inst_conv prod : prod_inst_convLs) {
                if (prod.getGwDevID().trim().equalsIgnoreCase(mgt_ip)) {
                    prod.setnLifBandWidth(bandwidth.intValue());
//                    prod.setBandwidth2(bandwidth);
                    if(obj.getPe_ip() != null && !obj.getPe_ip().equals(""))
                        prod.setPe_ip(obj.getPe_ip());
                    if(obj.getnPeerIP() != null && !obj.getnPeerIP().equals(""))
                        prod.setnPeerIP(obj.getnPeerIP());
                    if(obj.getPe_vlanid() != null && !obj.getPe_vlanid().equals(""))
                        prod.setPe_vlanid(obj.getPe_vlanid());
                    if(obj.getnLifVlanID() != null && !obj.getnLifVlanID().equals(""))
                        prod.setnLifVlanID(obj.getnLifVlanID());

                    newOrd_gatewayList.remove(m);
                    needChange_gatewayList.add(prod);
                    break;
                }
            }
        }

        //有新的汇聚网关实例
        if (newOrd_gatewayList.size() > 0) {
            for (Config_conv obj : newOrd_gatewayList) {
                //新的汇聚网关订单转换为——》新的汇聚网关实例
                // 转换
                Prod_inst_conv pojo= TypeCastCP.Config_convToProd_inst_conv(obj, prod_inst_id, FINISH);
                //插入数据库
                ResConfigClientService.addProdInstConv(pojo);
                Logs.createLogs("Config_conv addDB:"+pojo.toString());

//                ResConfigClientService.addConfigConv(pojo);
            }

        }





        for(Prod_inst_conv obj:prod_inst_convLs){
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2STOPVPN)) {
                obj.setProd_inst_state(PAUSE);
            }else {
                obj.setProd_inst_state(FINISH);
            }
            obj.setUpdate_time(new Date());

            Logs.createLogs("update prod_inst_conv");
            Logs.createLogs("prod_inst_conv id:"+obj.getGwDevID());
            Logs.createLogs("prod_inst_conv bandwidth: "+obj.getnLifBandWidth());

            // 更新数据库
            ResConfigClientService.updateProdInstConv(obj);
            Logs.createLogs("Prod_inst_conv updateDB:"+obj.toString());
        }


    }
}
