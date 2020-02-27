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
@Service(value = "S2-stopVpn_Finish")
public class S2_stopVpn_Finish extends Data_BaseClass {
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


        Logs.createLogs("config_prepLs的size:"+config_prepLs.size());
        //处理前置实例
        List<Config_prep> newOrd_pre_gatewayList = new ArrayList<>(config_prepLs);
//        需要更新的前置实例
        List<Prod_inst_prep> needChange_Pre_gatewayList = new ArrayList<>();

        for (int j = config_prepLs.size() - 1; j >= 0; j--) {
            Config_prep obj = config_prepLs.get(j);
//            Integer Pre_gw_unit_id = obj.getPre_gw_unit_id();
            String mgt_ip = obj.getGwDevID();
            Integer bandwidth = obj.getBandwith();
            for (int i = 0; i < prod_inst_prepLs.size(); i++) {
                Prod_inst_prep prod = prod_inst_prepLs.get(i);
//                Integer test = prod.getPre_gw_unit_id();
                boolean t = prod.getGwDevID().trim().equalsIgnoreCase(mgt_ip);
                if (prod.getGwDevID().trim().equalsIgnoreCase(mgt_ip)) {
                    prod.setBandwith(bandwidth.intValue());
                    Logs.createLogs("change prod_inst_prep");
                    Logs.createLogs("setBandwith:"+bandwidth);
                    Logs.createLogs("prod_inst_prep id"+mgt_ip);
                    Logs.createLogs("prod_inst_prep bandwidth"+prod_inst_prepLs.get(i).getBandwith());
                    newOrd_pre_gatewayList.remove(j);
                    needChange_Pre_gatewayList.add(prod);
                    break;
                }
            }


        }
        //有新的前置网关实例
        if (newOrd_pre_gatewayList.size() > 0) {
            for (Config_prep obj : newOrd_pre_gatewayList) {
                // 新的前置网关订单转换为——》新的前置网关实例
                Prod_inst_prep pojo = TypeCastCP.Config_prepToProd_inst_prep(obj, prod_inst_id.toString(), FINISH);
                //插入数据库
                ResConfigClientService.addProdInstPrep(pojo);
                Logs.createLogs("Config_prep addDB:"+pojo.toString());

            }
        }

        //处理盒子配置实例
        List<Config_box> newConfig_boxList = new ArrayList<>(config_boxLs);
        //需要更新的盒子配置实例
        List<Prod_inst_box> needChangeConfig_boxList = new ArrayList<>();

        for (int j = config_boxLs.size() - 1; j >= 0; j--) {
            Config_box obj = config_boxLs.get(j);
//            Integer Pre_gw_unit_id = obj.getPre_gw_unit_id();
            String site_name = obj.getSite_name();
            if(site_name ==null || site_name.length()==0)
                throw new RuntimeException("盒子的站点名为空"+"订单号："+so_order.getOrder_id());
            for (int i = 0; i < prod_inst_boxLs.size(); i++) {
                Prod_inst_box prod = prod_inst_boxLs.get(i);
//                Integer test = prod.getPre_gw_unit_id();
                boolean t = prod.getSite_name().trim().equalsIgnoreCase(site_name);
                if (prod.getSite_name().trim().equalsIgnoreCase(site_name)) {
                    if(so_order.getOrder_type().trim().equalsIgnoreCase(S2DELSITE)) {
                        Prod_inst_box pojo= TypeCastCP.Config_boxToProd_inst_box(obj, prod.getProd_inst_id().toString(), DOWN);
                        prod_inst_boxLs.remove(i);
//                        prod_inst_boxLs.add(i,pojo);
                        needChangeConfig_boxList.add(pojo);
                    }else{
                        Prod_inst_box pojo= TypeCastCP.Config_boxToProd_inst_box(obj, prod.getProd_inst_id().toString(), FINISH);
                        prod_inst_boxLs.remove(i);
//                        prod_inst_boxLs.add(i,pojo);
                        needChangeConfig_boxList.add(pojo);
                    }
//                    prod.setBoxSerNum(obj.getBoxSerNum());
                    newConfig_boxList.remove(j);

                    break;
                }
            }


        }
        //有新的盒子配置实例
        if (newConfig_boxList.size() > 0) {
            for (Config_box obj : newConfig_boxList) {
                // 新的盒子配置转换为——》新的盒子配置实例
                Prod_inst_box pojo= TypeCastCP.Config_boxToProd_inst_box(obj, prod_inst_id.toString(), FINISH);

                //插入数据库
                ResConfigClientService.addProdInstBox(pojo);
                Logs.createLogs("Prod_inst_box addDB:"+pojo.toString());

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
        for(Prod_inst_prep obj:needChange_Pre_gatewayList){
            obj.setProd_inst_state(FINISH);
            obj.setUpdate_time(new Date());

            Logs.createLogs("update prod_inst_prep");
            Logs.createLogs("prod_inst_prep id:"+obj.getGwDevID());
            Logs.createLogs("prod_inst_prep bandwidth: "+obj.getBandwith());
            // 更新数据库
            ResConfigClientService.updateProdInstPrep(obj);
            Logs.createLogs("Prod_inst_prep updateDB:"+obj.toString());
        }
        for(Prod_inst_box obj:needChangeConfig_boxList){
            obj.setUpdate_time(new Date());

            Logs.createLogs("update prod_inst_box");
            Logs.createLogs("prod_inst_box id:"+obj.getSite_name());
            Logs.createLogs("prod_inst_box bandwidth: "+obj.getVpnBandWidth());
            //增加站点不更新配置盒子实例表
            if(!so_order.getOrder_type().trim().equalsIgnoreCase(S2ADDSITE)) {
                // 更新数据库
                ResConfigClientService.updateProdInstBox(obj);
                Logs.createLogs("Prod_inst_box updateDB:" + obj.toString());
            }
        }

    }
}
