package com.asiainfo.sso.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:chengpei
 * @Desc  输出各种级别的日志
 * @Date:Created in 2019-09-03
 */
@Slf4j
public class Logs {
    public static void createLogs(String contents){
        log.error(contents);
        log.info(contents);
        log.debug(contents);

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        System.out.println(list.size());
        list.clear();
        System.out.println(list.size());
    }

}
