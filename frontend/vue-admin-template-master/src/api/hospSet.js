import request from '@/utils/request'

export default {
  //分页查询医院列表
  getHospSetList(current, limit, searchObj) {
    return request({
      url: `/admin/hosp/hospitalSet/findPageHospSet`,
      method: 'post',
      data: { page: current, size: limit, ...searchObj }
    })
  },
  //删除医院设置
  deleteHospSet(id) {
    return request({
      url: `/admin/hosp/hospitalSet/${id}`,
      method: 'delete'
    })
  },
  //批量删除医院设置
  batchRemoveHospSet(idList) {
    return request({
      url: '/admin/hosp/hospitalSet/batchRemove',
      method: 'delete',
      data: idList
    })
  },
  //锁定和取消锁定
  lockHospSet(id, status) {
    return request({
      url: `/admin/hosp/hospitalSet/lockHospitalSet/${id}/${status}`,
      method: 'put'
    })
  },
  //添加医院设置
  saveHospSet(hospitalSet) {
    return request({
      url: `/admin/hosp/hospitalSet/saveHospitalSet`,
      method: 'post',
      data: hospitalSet
    })
  },
  //回显医院设置信息
  getHospSet(id) {
    return request({
      url: `/admin/hosp/hospitalSet/getHospSet/${id}`,
      method: 'get'
    })
  },
  //修改医院设置信息
  updateHospSet(hospitalSet) {
    return request({
      url: `/admin/hosp/hospitalSet/updateHospitalSet`,
      method: 'post',
      data: hospitalSet
    })
  }
}
