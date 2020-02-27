package com.asiainfo.sso.service.base;

import boss.net.pojo.So_ord_site_box;
import boss.net.pojo.So_order;

import java.util.List;
import java.util.Map;

/**
 * @Author:chengpei
 * @Desc
 * @Date:Created in 2019-09-06
 */
public interface Data_Interface {
    void init(So_order so_order, Map<String,String> orderIdQueryMap)throws Exception;
    //    订单数据（so_order,so_ord_site_box）
    void order() throws Exception;
//    void order(So_order obj, List<So_ord_site_box> so_ord_site_boxLs);
    //    订单配置数据(conf_conv,conf_prep,conf_box)
    void order_conf() throws Exception;
    //    主实例(so_prod_inst)
    void prod() throws Exception;
    //    盒子实例(so_prod_site_box)
    void prod_box() throws Exception;
    //    实例配置数据(prod_inst_box,prod_inst_conv,prod_inst_prep)
    void prod_conf() throws Exception;
    //    资源数据(res_vpn,res_vlan_id,res_site_id,res_pre_gw_unit_vpn,res_conv_gw_unit_vpn)
    void resource() throws Exception;

    void current_lt_box() throws Exception;//盒子的长期带宽属性表current_lt_box录入
    void  hi_so_prod_site_box() throws Exception ;//盒子历史实例表hi_so_prod_site_box录入
    void  hi_so_prod_inst() throws Exception ;//历史主实例表录入

    void feeData();//计费模块

}
