<template>
  <div class="app-container">
    <el-form label-width="120px">
      <el-form-item label="医院名称">
        <el-input v-model="hospitalSet.hosname" />
      </el-form-item>

      <el-form-item label="医院编号">
        <el-input v-model="hospitalSet.hoscode" />
      </el-form-item>

      <el-form-item label="api基础路径">
        <el-input v-model="hospitalSet.apiUrl" />
      </el-form-item>

      <el-form-item label="联系人姓名">
        <el-input v-model="hospitalSet.contactsName" />
      </el-form-item>
      <el-form-item label="联系人手机">
        <el-input v-model="hospitalSet.contactsPhone" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="saveOrUpdate">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import hospSet from "@/api/hospSet";
export default {
  data() {
    return {
      hospitalSet: {}, //封装页面数据
    };
  },
  created() {
    // 获取路由里的id值
    if (this.$route.params && this.$route.params.id) {
      const id = this.$route.params.id;
      this.getHospSet(id);
    }
  },
  methods: {
    //回显
    getHospSet(id) {
      hospSet.getHospSet(id).then((res) => {
        this.hospitalSet = res.data;
      });
    },
    //添加
    save() {
      hospSet.saveHospSet(this.hospitalSet).then((res) => {
        //提示信息
        this.$message({
          type: "success",
          message: "医院设置添加成功",
        });
        //跳转列表页面，使用路由跳转方式实现
        this.$router.push({ path: "/hospSet/list" });
      });
    },
    //更新
    update() {
      hospSet.updateHospSet(this.hospitalSet).then((res) => {
        //提示信息
        this.$message({
          type: "success",
          message: "医院设置修改成功",
        });
        //跳转列表页面，使用路由跳转方式实现
        this.$router.push({ path: "/hospSet/list" });
      });
    },
    saveOrUpdate() {
      if (!this.hospitalSet.id) {
        this.save();
      } else {
        this.update();
      }
    },
  },
};
</script>

