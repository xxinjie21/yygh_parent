import request from '@/utils/request'
const api_name = `/api/user`
export default {
  sendCode(mobile) {
    return request({
      url: `${api_name}/sendCode/${mobile}`,
      method: `get`
    })
  }
}
