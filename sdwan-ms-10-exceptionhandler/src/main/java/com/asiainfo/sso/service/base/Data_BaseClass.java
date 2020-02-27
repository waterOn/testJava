package com.asiainfo.sso.service.base;

import boss.net.pojo.*;
import boss.net.pojo.bill.Bill_box_detail;
import boss.net.pojo.resource_config.*;
import boss.net.service.BillClientService;
import boss.net.service.OrderClientService;
import boss.net.service.ProdClientService;
import boss.net.util.GetUUID;
import boss.net.util.TypeCastCP;
import com.asiainfo.sso.service.common.OrderDB;
import com.asiainfo.sso.service.common.ProdDB;
import com.asiainfo.sso.util.Logs;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static boss.net.staticDatas.StaticDataBase.*;
import static boss.net.staticDatas.StaticDataBase.S2ADDSITE;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-06
 */
public class Data_BaseClass implements Data_Interface {
    @Autowired(required = false)
    public OrderClientService orderClientService;
    @Autowired(required = false)
    public boss.net.service.ResConfigClientService ResConfigClientService;
    @Autowired
    public ProdClientService prodClientService;
    @Autowired(required = false)
    public BillClientService billClientService;

    //定义对象
    public So_order so_order;
    public List<So_ord_site_box> so_ord_site_boxLs;
    public So_prod_inst so_prod_inst;
    public List<So_prod_site_box> so_prod_site_boxLs;
    public List<Config_conv> config_convLs;
    public List<Config_prep> config_prepLs;
    public List<Config_box> config_boxLs;
    public List<Prod_inst_conv> prod_inst_convLs;
    public List<Prod_inst_prep> prod_inst_prepLs;
    public List<Prod_inst_box> prod_inst_boxLs;
    public List<Res_vpn> res_vpnLs;
    public List<Res_vlan_id> res_vlan_idLs;
//    public List<Res_siteid> res_siteidLs;
    public List<Res_pre_gw_unit_vpn> res_pre_gw_unit_vpnLs;
    public List<Res_conv_gw_unit_vpn> res_conv_gw_unit_vpnLs;

    OrderDB OrderDB = null;

    public List<So_prod_site_box> needUpdate_siteBoxList;
    public List<So_ord_site_box> newOrd_site_boxList;

    public void init(So_order so_order, Map<String,String> orderIdQueryMap) throws Exception{
        OrderDB = new OrderDB();
        this.so_order = so_order;


        String order_id = so_order.getOrder_id().toString();
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();
        Map prodInstIdQueryMap = new HashMap();
        prodInstIdQueryMap.put("prod_inst_id",prod_inst_id);
        Map boxMap = new HashMap();
        boxMap.put("prod_inst_id",prod_inst_id);
        boxMap.put("prod_state","down");
        Logs.createLogs("查询实例的map参数："+prodInstIdQueryMap.toString());

            so_ord_site_boxLs = orderClientService.select_so_ord_site_box(orderIdQueryMap);
            // 查主实例、盒子实例
            so_prod_inst=prodClientService.search_so_prod_inst(prodInstIdQueryMap);
            so_prod_site_boxLs=prodClientService.search_so_prod_site_box(boxMap);

            config_boxLs = ResConfigClientService.getConfigBoxByIdOrStatus(Integer.parseInt(order_id),null);
            config_convLs = ResConfigClientService.getConfigConvByIdOrStatus(Integer.parseInt(order_id),null);
            config_prepLs = ResConfigClientService.getConfigPreByIdOrStatus(Integer.parseInt(order_id),null);

            prod_inst_prepLs = ResConfigClientService.getConfigPreProdByIdOrStatus(Integer.parseInt(prod_inst_id),null);
            prod_inst_convLs = ResConfigClientService.getConfigConvProdByIdOrStatus(Integer.parseInt(prod_inst_id),null);
            prod_inst_boxLs = ResConfigClientService.getConfigPreBoxByIdOrStatus(Integer.parseInt(prod_inst_id),"nodown");

            // 资源

            res_vpnLs=ResConfigClientService.selectResVpn(Integer.parseInt(prod_inst_id));
            res_pre_gw_unit_vpnLs=ResConfigClientService.selectResPreGwUnitVpn(Integer.parseInt(prod_inst_id));
            res_vlan_idLs=ResConfigClientService.selectResVlanId(Integer.parseInt(prod_inst_id));
            res_conv_gw_unit_vpnLs=ResConfigClientService.selectResConvGwUnitVpn(Integer.parseInt(prod_inst_id));



        Logs.createLogs("so_ord_site_boxLs contents before");
        for (So_ord_site_box obj:so_ord_site_boxLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("so_prod_site_boxLs contents before:");



        //需要更新的盒子实例
        needUpdate_siteBoxList = new ArrayList<>();

        //订单盒子
       newOrd_site_boxList = new ArrayList<>(so_ord_site_boxLs);//为了不污染不影响源数据，取数据待用

        List<So_prod_site_box> procee_so_prod_site_boxLs = new ArrayList<>(so_prod_site_boxLs);

        //比对订单和实例，找到新的盒子
        for (int f = so_ord_site_boxLs.size() - 1; f >= 0; f--) {
            So_ord_site_box obj = so_ord_site_boxLs.get(f);
            String site_name = obj.getSite_name();
            if(site_name ==null || site_name.length()==0)
                throw new RuntimeException("盒子的站点名为空"+"订单号："+so_order.getOrder_id());


            for (int i = 0; i < procee_so_prod_site_boxLs.size(); i++) {
                So_prod_site_box prod = procee_so_prod_site_boxLs.get(i);
                if (prod.getSite_name()!=null && prod.getSite_name().equalsIgnoreCase(site_name)) {
                    //新订购，新增站点，更新run_state,conf_state
                    if(!order_type.equalsIgnoreCase(S2NEW)&& !order_type.equalsIgnoreCase(S2ADDSITE)){
//                        logger.warn("旧的盒子ylwsiteid:"+obj.getYlw_site_id());
//                        logger.warn("运行状态："+obj.getRun_state());
//                        logger.warn("配置状态："+obj.getConf_state());
                        obj.setRun_state(null);
                        obj.setConf_state(null);
//                        logger.warn("不是新订购或增加站点的盒子不更新运行状态");
                    }
                    procee_so_prod_site_boxLs.remove(i);
//                    so_prod_site_boxLs.add(i, TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, prod_inst_id.toString(), FINISH));
                    needUpdate_siteBoxList.add(TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, prod_inst_id.toString(), FINISH));
                    newOrd_site_boxList.remove(f);

                    break;
                }
            }

        }


            // res_siteID
            //ResConfigClientService.searchResSiteId();

        Logs.createLogs("config_boxLs的size:"+config_boxLs.size());
        Logs.createLogs("config_convLs的size:"+config_convLs.size());
        Logs.createLogs("config_prepLs的size:"+config_prepLs.size());

        Logs.createLogs("prod_inst_prepLs的size:"+prod_inst_prepLs.size());
        Logs.createLogs("prod_inst_convLs的size:"+prod_inst_convLs.size());
        Logs.createLogs("prod_inst_boxLs的size:"+prod_inst_boxLs.size());



        Logs.createLogs("so_ord_site_boxLs的size:"+so_ord_site_boxLs.size());
        Logs.createLogs("so_prod_site_boxLs的size:"+so_prod_site_boxLs.size());

        Logs.createLogs("res_vpnLs的size:"+res_vpnLs.size());
        Logs.createLogs("res_pre_gw_unit_vpnLs的size:"+res_pre_gw_unit_vpnLs.size());
        Logs.createLogs("res_vlan_idLs的size:"+res_vlan_idLs.size());
        Logs.createLogs("res_conv_gw_unit_vpnLss的size:"+res_conv_gw_unit_vpnLs.size());

        Logs.createLogs("needUpdate_siteBoxList的size:"+needUpdate_siteBoxList.size());
        Logs.createLogs("newOrd_site_boxList的size:"+newOrd_site_boxList.size());


        Logs.createLogs("config_boxLs contents");
        for (Config_box obj:config_boxLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("config_convLs contents");
        for (Config_conv obj:config_convLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("config_prepLs contents:");

        for (Config_prep obj:config_prepLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("prod_inst_prepLs contents:");
        for (Prod_inst_prep obj:prod_inst_prepLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("prod_inst_convLs contents");
        for (Prod_inst_conv obj:prod_inst_convLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("prod_inst_boxLs contents:");

        for (Prod_inst_box obj:prod_inst_boxLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("so_ord_site_boxLs contents");
        for (So_ord_site_box obj:so_ord_site_boxLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("so_prod_site_boxLs contents:");

        for (So_prod_site_box obj:so_prod_site_boxLs){
            Logs.createLogs(obj.toString());
        }


        Logs.createLogs("res_vpnLs contents:");
        for (Res_vpn obj:res_vpnLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("res_pre_gw_unit_vpnLs contents:");

        for (Res_pre_gw_unit_vpn obj:res_pre_gw_unit_vpnLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("res_vlan_idLs contents");
        for (Res_vlan_id obj:res_vlan_idLs){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("res_conv_gw_unit_vpnLs contents:");

        for (Res_conv_gw_unit_vpn obj:res_conv_gw_unit_vpnLs){
            Logs.createLogs(obj.toString());
        }

        Logs.createLogs("needUpdate_siteBoxList contents:");
        for (So_prod_site_box obj:needUpdate_siteBoxList){
            Logs.createLogs(obj.toString());
        }
        Logs.createLogs("newOrd_site_boxList contents:");
        for (So_ord_site_box obj:newOrd_site_boxList){
            Logs.createLogs(obj.toString());
        }

    };
    @Override
    public void order() throws Exception {
        OrderDB.updateOrderStatusInDB(so_order,so_ord_site_boxLs,FINISH);
        Logs.createLogs("so_order updateDB:"+so_order.toString());
    }

    @Override
    public void order_conf() throws Exception {
        OrderDB.updateOrder_confStatusInDB(config_convLs,config_prepLs,config_boxLs,FINISH);
    }

    @Override
    public void prod() throws Exception {
        if(so_prod_inst != null){
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)) {
                Logs.createLogs("主实例"+S2ENDVPN);
                ProdDB.updateProdStatusInDB(so_prod_inst,DOWN);
            }
            else if(so_order.getOrder_type().trim().equalsIgnoreCase(S2STOPVPN)) {
                ProdDB.updateProdStatusInDB(so_prod_inst,PAUSE);
            }
            else if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ADDTIME)){
                Integer bandwidth_add = 0;
                bandwidth_add = Integer.parseInt(so_order.getInsert_time())+Integer.parseInt(so_prod_inst.getInsert_time());
                so_prod_inst.setInsert_time(bandwidth_add.toString());
                ProdDB.updateProdStatusInDB(so_prod_inst,FINISH);
            }
            else {
                ProdDB.updateProdStatusInDB(so_prod_inst,FINISH);
            }


        }

    }

    @Override
    //旧盒子更新，新盒子插入
    public void prod_box() throws Exception {
        String order_type = so_order.getOrder_type();
        String prod_inst_id = so_order.getProd_inst_id().toString();



        //新的盒子订单转换为——新的盒子实例
        if (newOrd_site_boxList.size() > 0) {
            for (So_ord_site_box obj : newOrd_site_boxList) {
                System.out.println("订单中的盒子" + obj.getConf_state() + " " + obj.getRun_state());
                //新的盒子订单转换为——》新的盒子实例
                So_prod_site_box pojo = TypeCastCP.So_ord_site_boxToSo_prod_site_box(obj, prod_inst_id.toString(), FINISH);
                System.out.println("实例中的盒子" + pojo.getConf_state() + " " + pojo.getRun_state());
                // 插入数据库
                prodClientService.add_prod_site_box(pojo);
                Logs.createLogs("so_prod_site_box addDB:"+pojo.toString());
//                logger.warn("新的盒子ylwsiteid:"+pojo.getYlw_site_id());
//                logger.warn("运行状态："+pojo.getRun_state());
//                logger.warn("配置状态："+pojo.getConf_state());

            }

        }

        //更新需要更新的盒子实例对象
        for(So_prod_site_box obj:needUpdate_siteBoxList){
            obj.setProd_state(FINISH);
            obj.setState_time(new Date());

            // 更新数据库
            prodClientService.update_so_prod_site_boxCP(obj);
            Logs.createLogs("so_prod_site_box updateDB:"+obj.toString());

        }

    }

    @Override
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

                    if(obj.getsPeerBGPList() != null && !obj.getsPeerBGPList().equals(""))
                        prod.setsPeerBGPList(obj.getsPeerBGPList());

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
        //需要更新的盒子实例


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


        for(Prod_inst_conv obj:needChange_gatewayList){
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

    @Override
    public void resource() throws Exception{
        for(Res_vpn obj:res_vpnLs){
            Logs.createLogs("goto resource  on res_vpn");
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)){
                obj.setIs_occupy(RESERVED);
            }else {
                obj.setIs_occupy(OCCUPY);
            }
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResVpn(obj);
            Logs.createLogs("Res_vpn updateDB:"+obj.toString());
        }
        for(Res_vlan_id obj:res_vlan_idLs){
            Logs.createLogs("goto resource  on res_vlan_id");
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)){
                obj.setIs_occupy(RESERVED);
            }else {
                obj.setIs_occupy(OCCUPY);
            }
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResVlanId(obj);
            Logs.createLogs("Res_vlan_id updateDB:"+obj.toString());
        }

        //前置网关通道和汇聚网关通道转到  思科适配器中维护
//        for(Res_pre_gw_unit_vpn obj:res_pre_gw_unit_vpnLs){
//            Logs.createLogs("goto resource  on res_pre_gw_unit_vpn");
//            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)){
//                obj.setIs_occupy(RESERVED);
//            }else {
//                obj.setIs_occupy(OCCUPY);
//            }
//            obj.setOccupy_time(new Date());
//            ResConfigClientService.updateResPreGwUnitVpn(obj);
//            Logs.createLogs("Res_pre_gw_unit_vpn updateDB:"+obj.toString());
//        }
//        for(Res_conv_gw_unit_vpn obj:res_conv_gw_unit_vpnLs){
//            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)){
//                obj.setIs_occupy(RESERVED);
//            }else {
//                obj.setIs_occupy(OCCUPY);
//            }
//            obj.setOccupy_time(new Date());
//            ResConfigClientService.updateResConvGwUnitVpn(obj);
//            Logs.createLogs("Res_conv_gw_unit_vpn updateDB:"+obj.toString());
//        }



    }

    @Override
    public void current_lt_box() throws Exception {

    }

    @Override
    public void hi_so_prod_site_box() throws Exception {

    }

    @Override
    public void hi_so_prod_inst() throws Exception {

    }

    @Override
    public void feeData() {


    }

    public void clearCache(){
        //清除needUpdate_siteBoxList  缓存
        Logs.createLogs("clear clearCache");
        Logs.createLogs("clear before needUpdate_siteBoxList:"+needUpdate_siteBoxList.size());
        needUpdate_siteBoxList.clear();
        Logs.createLogs("clear needUpdate_siteBoxList:"+needUpdate_siteBoxList.size());

        Logs.createLogs("clear newOrd_site_boxList:"+newOrd_site_boxList.size());
        newOrd_site_boxList.clear();
        Logs.createLogs("clear newOrd_site_boxList:"+newOrd_site_boxList.size());


    };

}
