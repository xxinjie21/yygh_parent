package com.yygh.hospital.service;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * 医院API服务接口，定义医院、科室、排班等数据的同步操作
 *
 * @author XXJ
 */
public interface ApiService {

    String getHoscode();

    String getSignKey();

    JSONObject getHospital();

    boolean saveHospital(String data);

    Map<String, Object> findDepartment(int pageNum, int pageSize);

    boolean saveDepartment(String data);

    boolean removeDepartment(String depcode);

    Map<String, Object> findSchedule(int pageNum, int pageSize);

    boolean saveSchedule(String data);

    boolean removeSchedule(String hosScheduleId);

    void  saveBatchHospital() throws IOException;
}
