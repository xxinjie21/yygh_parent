import request from '@/utils/request'

export default {
  //医院列表
  getHospList(page, limit, searchObj) {
    return request({
      url: `/admin/hosp/hospital/list`,
      method: 'post',
      data: { page, size: limit, ...searchObj }
    })
  },
  //查询dictCode查询下级数据字典
  findByDictCode(dictCode) {
    return request({
      url: `/admin/cmn/dict/findByDictCode/${dictCode}`,
      method: 'get'
    })
  },

  //根据id查询下级数据字典
  findChlidData(dictCode) {
    return request({
      url: `/admin/cmn/dict/findChildData/${dictCode}`,
      method: 'get'
    })
  },
  //更新医院上线状态
  updateStatus(id, status) {
    return request({
      url: `/admin/hosp/hospital/updateHospStatus/${id}/${status}`,
      method: "get",
    });
  },
  //查看医院详情
  getHospById(id) {
    return request({
      url: `/admin/hosp/hospital/showHospDetail/${id}`,
      method: 'get'
    })
  },
  //查看医院科室
  getDeptByHoscode(hoscode) {
    return request({
      url: `/admin/hosp/department/getDeptList/${hoscode}`,
      method: 'get'
    })
  },
  //查看预约规则
  getScheduleRule(page, limit, hoscode, depcode) {
    return request({
      url: `/admin/hosp/Schedule/getScheduleRule`,
      method: 'post',
      data: { page, size: limit, hoscode, depcode }
    })
  },
  //查询排班详情
  getScheduleDetail(hoscode, depcode, workDate) {
    return request({
      url: `/admin/hosp/Schedule/getScheduleDetail/${hoscode}/${depcode}/${workDate}`,
      method: 'get'
    })
  }
}
