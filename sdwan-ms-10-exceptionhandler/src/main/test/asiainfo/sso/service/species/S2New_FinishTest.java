package asiainfo.sso.service.species;

import boss.net.pojo.bill.Bill_box_detail;
import boss.net.service.BillClientService;
import boss.net.util.DateFormat;
import boss.net.util.GetUUID;
import com.asiainfo.sso.SdwanExceptionhandler5001;
import com.asiainfo.sso.service.main.MainService;
import com.asiainfo.sso.util.Logs;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/** 
* S2New_Finish Tester. 
* 
* @author <Authors name> 
* @since <pre>1月 2, 2020</pre> 
* @version 1.0 
*/ 
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={SdwanExceptionhandler5001.class,S2New_FinishTest.class })
public class S2New_FinishTest {
    @Autowired
   public MainService mainService;
@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: prod() 
* 
*/ 
@Test
public void testProd() throws Exception {
    mainService.test();
//TODO: Test goes here... 
} 

/** 
* 
* Method: prod_box() 
* 
*/ 
@Test
public void testProd_box() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: current_lt_box() 
* 
*/ 
@Test
public void testCurrent_lt_box() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: hi_so_prod_site_box() 
* 
*/ 
@Test
public void testHi_so_prod_site_box() throws Exception {
    List<Bill_box_detail> Bill_box_detailLs = billClientService.searchBillBoxDetail("6", "siteName", "flex");
    Logs.createLogs("search Bill_box_detail size:" + Bill_box_detailLs.size());
    if (Bill_box_detailLs.size() <= 0)
        throw new RuntimeException("search Bill_box_detail size:0");

    //修改bill_box_detail，灵活带宽截止日期
    Bill_box_detail longDetail = Bill_box_detailLs.get(0);
    longDetail.setEnd_time("2010-01-04");
    billClientService.updateBillBoxDetail(longDetail);
    Logs.createLogs("Bill_box_detail  longDetail  End_time:" + longDetail.getEnd_time());
    Logs.createLogs("Bill_box_detail  longDetail  updateDB:" + longDetail.toString());
} 

/** 
* 
* Method: hi_so_prod_inst() 
* 
*/
@Autowired(required = false)
public BillClientService billClientService;
@Test
public void testHi_so_prod_inst() throws Exception {
    System.out.println("");
    //录入bill_box_detail，灵活带宽
    Bill_box_detail longDetail = new Bill_box_detail();
    // TODO: 2020-01-02
    String main_id = GetUUID.getYear()+"-"+GetUUID.getMonth()+"33"+"siteName";
    longDetail.setMain_id(main_id);
    longDetail.setCust_id("33");
    longDetail.setProd_inst_id("666");
    longDetail.setSite_id("empty");
    longDetail.setSite_name("siteName");
    longDetail.setSerialNumber("obj.getSerialNumber()");
    longDetail.setSale(Double.valueOf("0.6"));
    longDetail.setStart_time("2010-01-02");
    longDetail.setEnd_time("2010-01-30");
    longDetail.setBandwidth("2");
    longDetail.setMark("1");
    longDetail.setBill_type("flex");
    try {
        billClientService.insertBillBoxDetail(longDetail);
        Logs.createLogs("Bill_box_detail  flexDetail   End_time:"+longDetail.getEnd_time());
        Logs.createLogs("Bill_box_detail  flexDetail   updateDB:"+longDetail.toString());
    } catch (Exception e) {
        e.printStackTrace();
    }
} 


} 
