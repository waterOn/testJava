package com.asiainfo.sso.service.base;

import boss.net.pojo.*;
import boss.net.pojo.resource_config.*;
import boss.net.service.OrderClientService;
import boss.net.service.ProdClientService;
import boss.net.service.ResConfigClientService;
import com.asiainfo.sso.service.common.OrderDB;
import com.asiainfo.sso.util.MapHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static boss.net.staticDatas.StaticDataBase.*;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-09
 */
@Service(value = "Exception_BaseClass")
public class Exception_BaseClass implements Data_Interface{
    @Autowired(required = false)
    public OrderClientService orderClientService;
    @Autowired(required = false)
    public ResConfigClientService ResConfigClientService;
    @Autowired
    public ProdClientService prodClientService;

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



    private static Exception_BaseClass exception_BaseClass;

    @PostConstruct
    public void init(){
        exception_BaseClass=this;
        exception_BaseClass.orderClientService=this.orderClientService;
        exception_BaseClass.ResConfigClientService=this.ResConfigClientService;
        exception_BaseClass.prodClientService=this.prodClientService;
    }

    public void init(So_order so_order, Map<String,String> orderIdQueryMap){
        this.so_order = so_order;
        String order_id = so_order.getOrder_id().toString();
        String prod_inst_id = so_order.getProd_inst_id().toString();
//        Map prodInstIdQueryMap = MapHandler.getStringToMap("{'prod_inst_id':"+prod_inst_id+"}");
        Map<String,String> prodInstIdQueryMap = new HashMap();
        prodInstIdQueryMap.put("prod_inst_id",prod_inst_id);

        try {
            so_ord_site_boxLs=orderClientService.select_so_ord_site_box(orderIdQueryMap);
            so_ord_site_boxLs=exception_BaseClass.orderClientService.select_so_ord_site_box(orderIdQueryMap);
//            so_ord_site_boxLs = orderClientService.select_so_ord_site_box(orderIdQueryMap);
            // 查主实例、盒子实例
            so_prod_inst=exception_BaseClass.prodClientService.search_so_prod_inst(prodInstIdQueryMap);
            so_prod_site_boxLs=exception_BaseClass.prodClientService.search_so_prod_site_box(prodInstIdQueryMap);


            config_boxLs = ResConfigClientService.getConfigBoxByIdOrStatus(Integer.parseInt(order_id),null);
            config_convLs = ResConfigClientService.getConfigConvByIdOrStatus(Integer.parseInt(order_id),null);
            config_prepLs = ResConfigClientService.getConfigPreByIdOrStatus(Integer.parseInt(order_id),null);


            prod_inst_prepLs = ResConfigClientService.getConfigPreProdByIdOrStatus(Integer.parseInt(prod_inst_id),null);
            prod_inst_convLs = ResConfigClientService.getConfigConvProdByIdOrStatus(Integer.parseInt(prod_inst_id),null);
            prod_inst_boxLs = ResConfigClientService.getConfigPreBoxByIdOrStatus(Integer.parseInt(prod_inst_id),null);


            // 资源

            res_vpnLs=exception_BaseClass.ResConfigClientService.selectResVpn(Integer.parseInt(prod_inst_id));
            res_pre_gw_unit_vpnLs=exception_BaseClass.ResConfigClientService.selectResPreGwUnitVpn(Integer.parseInt(prod_inst_id));
            res_vlan_idLs=exception_BaseClass.ResConfigClientService.selectResVlanId(Integer.parseInt(prod_inst_id));
            res_conv_gw_unit_vpnLs=exception_BaseClass.ResConfigClientService.selectResConvGwUnitVpn(Integer.parseInt(prod_inst_id));

            // res_siteID
            //ResConfigClientService.searchResSiteId();

        }catch (Exception e){
            e.printStackTrace();
        }
    };


    @Override
    public void order() throws Exception {
        System.out.println("order");
        //更改主订单为down和盒子订单状态为exception
        if(so_order != null) {
            so_order.setOrder_status(DOWN);
            so_order.setOrder_time(new Date());
            orderClientService.update_so_order(so_order);
            if(so_ord_site_boxLs.size()>0){
                for(So_ord_site_box obj:so_ord_site_boxLs){
                    obj.setOrder_status(DOWN);
                    obj.setOrder_time(new Date());
                    //更新数据库
                    orderClientService.update_so_ord_site_box(obj);
                }
            }

        }else {

        }
    }

    @Override
    public void order_conf() throws Exception {
        if(config_convLs.size()>0){
            for(Config_conv obj:config_convLs){
                obj.setConfig_state(EXCEPTION);
                obj.setUpdate_time(new Date());
                ResConfigClientService.updateConfigConv(obj);
            }
        }
        if(config_prepLs.size()>0){
            for (Config_prep obj:config_prepLs){
                obj.setConfig_state(EXCEPTION);
                obj.setUpdate_time(new Date());
                ResConfigClientService.updateConfigPrep(obj);
            }
        }

        if(config_boxLs.size()>0){
            for(Config_box obj:config_boxLs){
                obj.setConfig_State(EXCEPTION);
                obj.setUpdate_time(new Date());
                ResConfigClientService.updateConfigBoxInfo(obj);
            }
        }


    }

    @Override
    public void prod() throws Exception {
        if(so_prod_inst != null){
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2NEW)) {
                so_prod_inst.setProd_state(EXCEPTION);
            }
            else if(so_order.getOrder_type().trim().equalsIgnoreCase(S2OPENVPN)){
                so_prod_inst.setProd_state(PAUSE);
            }
            else {
                so_prod_inst.setProd_state(FINISH);
            }
            so_prod_inst.setState_time(new Date());
            // 主实例的更新数据库
            prodClientService.update_so_prod_inst(so_prod_inst);
        }
    }

    @Override
    public void prod_box() throws Exception {
        if(so_order.getOrder_type().trim().equalsIgnoreCase(S2NEW)) {
            for (So_prod_site_box obj : so_prod_site_boxLs) {
                obj.setProd_state(EXCEPTION);
                obj.setState_time(new Date());

                // 更新数据库
                prodClientService.update_so_prod_site_box(obj);
            }
        }

    }

    @Override
    public void prod_conf() throws Exception {
        if(so_order.getOrder_type().trim().equalsIgnoreCase(S2NEW)) {
        for(Prod_inst_conv obj:prod_inst_convLs){
            obj.setProd_inst_state(EXCEPTION);
            obj.setUpdate_time(new Date());

            // 更新数据库
            ResConfigClientService.updateProdInstConv(obj);
        }
        for(Prod_inst_prep obj:prod_inst_prepLs){
            obj.setProd_inst_state(EXCEPTION);
            obj.setUpdate_time(new Date());

            // 更新数据库
            ResConfigClientService.updateProdInstPrep(obj);
        }
        for(Prod_inst_box obj:prod_inst_boxLs){
            obj.setProd_inst_state(EXCEPTION);
            obj.setUpdate_time(new Date());

            // 更新数据库
            ResConfigClientService.updateProdInstBox(obj);
        }
        }
    }

    @Override
    public void resource() throws Exception{
        if(so_order.getOrder_type().trim().equalsIgnoreCase(S2NEW)) {
            for (Res_vpn obj : res_vpnLs) {
                obj.setIs_occupy(EXCEPTION);
                obj.setOccupy_time(new Date());
                obj.setProd_inst_id(null);
                obj.setOccupy_time(new Date());
                ResConfigClientService.updateResVpn(obj);
            }

        for(Res_vlan_id obj:res_vlan_idLs){
            if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ENDVPN)){
                obj.setIs_occupy(EXCEPTION);
            }else {
                obj.setIs_occupy(EXCEPTION);
            }
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResVlanId(obj);
        }
        }
        if(so_order.getOrder_type().trim().equalsIgnoreCase(S2ADDSITE)) {
            for (Res_pre_gw_unit_vpn obj : res_pre_gw_unit_vpnLs) {
                    obj.setIs_occupy(EXCEPTION);
                obj.setOccupy_time(new Date());
                obj.setVpn_num("0");
                ResConfigClientService.updateResPreGwUnitVpn(obj);
            }

        for(Res_conv_gw_unit_vpn obj:res_conv_gw_unit_vpnLs){
            obj.setIs_occupy(EXCEPTION);
            obj.setProd_inst_id(0);
            obj.setVpn_num("0");
            obj.setOccupy_time(new Date());
            ResConfigClientService.updateResConvGwUnitVpn(obj);
        }
        }
    }

    @Override
    public void current_lt_box() {

    }

    @Override
    public void hi_so_prod_site_box() {

    }

    @Override
    public void hi_so_prod_inst() {

    }

    @Override
    public void feeData() {

    }
}
