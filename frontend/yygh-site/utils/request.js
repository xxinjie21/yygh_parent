import axios from 'axios'
import cookie from 'js-cookie'
import {
  MessageBox,
  Message
} from 'element-ui'
// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8888',
  timeout: 15000 // 请求超时时间
})
// http request 拦截器
service.interceptors.request.use(config => {
  // token 先不处理，后续使用时在完善
  //判断 cookie 是否有 token 值
  if (cookie.get('token')) {
    //token 值放到 cookie 里面
    config.headers['token'] = cookie.get('token')
  }
  return config
}, err => {
  return Promise.reject(err)
})
// http response 拦截器
service.interceptors.response.use(response => {
  //状态码是 208
  if (response.data.code === 208) {
    //弹出登录输入框
    loginEvent.$emit('loginDialogEvent')
    return
  } else {
    if (response.data.code !== 200) {
      Message({
        message: response.data.message,
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(response.data)
    } else {
      return response.data
    }
  }

}, error => {
  return Promise.reject(error.response)
})
export default service
