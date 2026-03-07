<template>
  <div class="page">
    <div class="page-cards" v-draggable-cards>
      <el-card>
      <template #header>系统配置</template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="user">
          <el-input v-model="userKeyword" placeholder="用户名/姓名" clearable style="width:200px;margin-right:8px" />
          <el-button type="primary" size="small" @click="loadUsers">查询</el-button>
          <el-button type="primary" size="small" @click="openUserForm()">新增用户</el-button>
          <el-table :data="users" v-loading="userLoading" style="margin-top:12px" border>
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="realName" label="姓名" min-width="80" />
            <el-table-column prop="email" label="邮箱" min-width="120" />
            <el-table-column prop="phone" label="手机" min-width="110" />
            <el-table-column prop="status" label="状态" min-width="72">
              <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-dropdown trigger="click" @command="(cmd) => onUserCommand(cmd, row)">
                    <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="roles">分配角色</el-dropdown-item>
                        <el-dropdown-item command="del" divided :disabled="row.username === 'admin'">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="userPageNum" v-model:page-size="userSize" :total="userTotal" layout="total, prev, pager, next" @current-change="loadUsers" style="margin-top:12px" />
        </el-tab-pane>
        <el-tab-pane label="角色管理" name="role">
          <el-button type="primary" size="small" @click="openRoleForm()">新增角色</el-button>
          <el-table :data="roles" style="margin-top:12px" border>
            <el-table-column prop="code" label="角色编码" min-width="100" />
            <el-table-column prop="name" label="角色名称" min-width="100" />
            <el-table-column prop="status" label="状态" min-width="72">
              <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-dropdown trigger="click" @command="(cmd) => onRoleCommand(cmd, row)">
                    <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="menus">分配菜单</el-dropdown-item>
                        <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="字典配置" name="dict">
          <el-button type="primary" size="small" @click="openDictTypeForm()">新增字典类型</el-button>
          <el-table :data="dictTypes" style="margin-top:12px" border>
            <el-table-column prop="code" label="编码" min-width="100" />
            <el-table-column prop="name" label="名称" min-width="100" />
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-dropdown trigger="click" @command="(cmd) => onDictTypeCommand(cmd, row)">
                    <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="items">字典项</el-dropdown-item>
                        <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-dialog v-model="dictItemDialogVisible" :title="'字典项：' + currentDictType?.name" width="560px">
            <el-button type="primary" size="small" @click="openDictItemForm()">新增项</el-button>
            <el-table :data="dictItems" style="margin-top:12px" border>
              <el-table-column prop="itemKey" label="键" min-width="100" />
              <el-table-column prop="itemValue" label="值" min-width="100" />
              <el-table-column prop="sortOrder" label="排序" min-width="64" />
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <div class="table-actions-cell">
                    <el-dropdown trigger="click" @command="(cmd) => cmd === 'edit' ? openDictItemForm(row) : deleteDictItem(row.id)">
                      <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="edit">编辑</el-dropdown-item>
                          <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <el-dialog v-model="dictItemFormVisible" :title="dictItemForm.id ? '编辑字典项' : '新增字典项'" width="400px" append-to-body>
              <el-form :model="dictItemForm" label-width="80px">
                <el-form-item label="键"><el-input v-model="dictItemForm.itemKey" /></el-form-item>
                <el-form-item label="值"><el-input v-model="dictItemForm.itemValue" /></el-form-item>
                <el-form-item label="排序"><el-input-number v-model="dictItemForm.sortOrder" :min="0" /></el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="dictItemFormVisible = false">取消</el-button>
                <el-button type="primary" @click="saveDictItem">确定</el-button>
              </template>
            </el-dialog>
          </el-dialog>
        </el-tab-pane>
        <el-tab-pane label="审批流配置" name="approval">
          <el-button type="primary" size="small" @click="openFlowForm()">新增审批流</el-button>
          <el-button type="primary" size="small" @click="loadFlows">刷新</el-button>
          <el-table :data="flows" style="margin-top:12px" border>
            <el-table-column prop="code" label="流程编码" min-width="100" />
            <el-table-column prop="name" label="流程名称" min-width="120" />
            <el-table-column prop="bizType" label="业务类型" min-width="90" />
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-dropdown trigger="click" @command="(cmd) => cmd === 'edit' ? openFlowForm(row) : deleteFlow(row)">
                    <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="表单配置" name="form">
          <el-button type="primary" size="small" @click="openFormConfigForm()">新增表单</el-button>
          <el-button type="primary" size="small" @click="loadFormConfigs">刷新</el-button>
          <el-table :data="formConfigs" style="margin-top:12px" border>
            <el-table-column prop="formCode" label="表单编码" min-width="100" />
            <el-table-column prop="formName" label="表单名称" min-width="120" />
            <el-table-column prop="bizType" label="业务类型" min-width="90" />
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-dropdown trigger="click" @command="(cmd) => cmd === 'edit' ? openFormConfigForm(row) : deleteFormConfig(row)">
                    <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="菜单管理" name="menu">
          <div style="margin-bottom:12px">
            <el-button type="primary" size="small" @click="openMenuForm()">新增根菜单</el-button>
            <el-button type="primary" size="small" @click="openMenuForm(null, selectedMenuId)" :disabled="!selectedMenuId">新增子菜单</el-button>
            <el-button type="primary" size="small" @click="openMenuForm(selectedMenuNode)" :disabled="!selectedMenuId">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteMenuConfirm" :disabled="!selectedMenuId">删除</el-button>
            <el-button size="small" @click="loadMenuTree">刷新</el-button>
          </div>
          <el-tree ref="menuManageTreeRef" :data="menuManageTree" :props="{ label: 'name' }" node-key="id" highlight-current @node-click="onMenuNodeClick">
            <template #default="{ node, data }">
              <span>{{ getMenuNodeLabel(data) }}</span>
            </template>
          </el-tree>
        </el-tab-pane>
        <el-tab-pane label="关联页签配置" name="tab">
          <el-button type="primary" size="small" @click="openTabForm()">新增页签</el-button>
          <el-button type="primary" size="small" @click="loadTabs">刷新</el-button>
          <el-table :data="tabs" style="margin-top:12px" border>
            <el-table-column prop="bizType" label="业务类型" min-width="90" />
            <el-table-column prop="tabCode" label="页签编码" min-width="100" />
            <el-table-column prop="tabName" label="页签名称" min-width="100" />
            <el-table-column prop="tabPath" label="路径" min-width="120" />
            <el-table-column prop="sortOrder" label="排序" min-width="64" />
            <el-table-column label="操作" min-width="120" align="center">
              <template #default="{ row }">
                <div class="table-actions-cell">
                  <el-button link type="primary" @click="openTabForm(row)">编辑</el-button>
                  <el-button link type="danger" @click="deleteTab(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    </div>
    <el-dialog v-model="userFormVisible" :title="userForm.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="userForm.username" placeholder="用户名" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item :label="userForm.id ? '新密码' : '密码'" :required="!userForm.id">
          <el-input v-model="userForm.password" type="password" placeholder="不填则保持原密码或默认123456" show-password />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="userForm.realName" placeholder="姓名" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="userForm.email" placeholder="邮箱" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="userForm.phone" placeholder="手机" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="userRolesVisible" title="分配角色" width="400px">
      <el-checkbox-group v-model="userRoleIds">
        <div style="display:flex;flex-direction:column;gap:8px">
          <el-checkbox v-for="r in roles" :key="r.id" :label="r.id">{{ r.name }} ({{ r.code }})</el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="userRolesVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserRoles">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="roleFormVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="400px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色编码" required><el-input v-model="roleForm.code" placeholder="如 ADMIN" /></el-form-item>
        <el-form-item label="角色名称" required><el-input v-model="roleForm.name" placeholder="如 管理员" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="roleMenusVisible" title="分配菜单" width="400px">
      <el-tree ref="menuTreeRef" :data="menuTreeData" show-checkbox node-key="id" :props="{ label: 'name' }" />
      <template #footer>
        <el-button @click="roleMenusVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoleMenus">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="dictTypeFormVisible" :title="dictTypeForm.id ? '编辑字典类型' : '新增字典类型'" width="400px">
      <el-form :model="dictTypeForm" label-width="80px">
        <el-form-item label="编码"><el-input v-model="dictTypeForm.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="dictTypeForm.name" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictTypeFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDictType">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="flowFormVisible" :title="flowForm.id ? '编辑审批流' : '新增审批流'" width="640px">
      <el-form :model="flowForm" label-width="90px">
        <el-form-item label="流程编码" required><el-input v-model="flowForm.code" placeholder="如 CHANGE_APPROVAL" /></el-form-item>
        <el-form-item label="流程名称" required><el-input v-model="flowForm.name" placeholder="流程名称" /></el-form-item>
        <el-form-item label="业务类型"><el-input v-model="flowForm.bizType" placeholder="如 change" /></el-form-item>
        <el-form-item label="审批节点">
          <div style="margin-bottom:8px"><el-button size="small" @click="addFlowNode">添加节点</el-button></div>
          <el-table :data="flowNodes" size="small" border>
            <el-table-column prop="nodeName" label="节点名称" min-width="120">
              <template #default="{ row, $index }"><el-input v-model="row.nodeName" placeholder="节点名" size="small" /></template>
            </el-table-column>
            <el-table-column prop="approverType" label="审批人类型" min-width="108">
              <template #default="{ row }">
                <el-select v-model="row.approverType" placeholder="类型" size="small" style="width:100%">
                  <el-option label="角色" value="ROLE" /><el-option label="用户" value="USER" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="approverId" label="审批人ID">
              <template #default="{ row }"><el-input v-model="row.approverId" placeholder="角色ID或用户ID" size="small" /></template>
            </el-table-column>
            <el-table-column label="操作" min-width="72" align="center">
              <template #default="{ $index }">
                <div class="table-actions-cell">
                  <el-button link type="danger" size="small" @click="flowNodes.splice($index, 1)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="flowFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFlow">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="formConfigFormVisible" :title="formConfigForm.id ? '编辑表单配置' : '新增表单配置'" width="920px">
      <el-form :model="formConfigForm" label-width="100px">
        <el-form-item label="表单编码" required><el-input v-model="formConfigForm.formCode" placeholder="如 project，与菜单/动态页 formCode 关联" /></el-form-item>
        <el-form-item label="表单名称" required><el-input v-model="formConfigForm.formName" placeholder="表单名称" /></el-form-item>
        <el-form-item label="业务类型"><el-input v-model="formConfigForm.bizType" placeholder="如 project" /></el-form-item>
        <el-form-item label="列表接口"><el-input v-model="formConfigForm.apiListPath" placeholder="如 /project/page，分页列表" /></el-form-item>
        <el-form-item label="保存接口"><el-input v-model="formConfigForm.apiSavePath" placeholder="如 /project，新增/更新基础路径" /></el-form-item>
        <el-form-item label="字段列表">
          <div style="margin-bottom:8px"><el-button size="small" @click="addFormField">添加字段</el-button></div>
          <el-table :data="formFields" size="small" max-height="280" border>
            <el-table-column prop="fieldName" label="字段名" min-width="90">
              <template #default="{ row }"><el-input v-model="row.fieldName" size="small" placeholder="fieldName" /></template>
            </el-table-column>
            <el-table-column prop="fieldLabel" label="标签" min-width="70">
              <template #default="{ row }"><el-input v-model="row.fieldLabel" size="small" placeholder="标签" /></template>
            </el-table-column>
            <el-table-column prop="fieldType" label="类型" min-width="80">
              <template #default="{ row }">
                <el-select v-model="row.fieldType" size="small" style="width:100%">
                  <el-option label="文本" value="text" /><el-option label="数字" value="number" /><el-option label="日期" value="date" /><el-option label="下拉" value="select" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="required" label="必填" min-width="56">
              <template #default="{ row }"><el-checkbox v-model="row.required" :true-value="1" :false-value="0" /></template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" min-width="56">
              <template #default="{ row }"><el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" /></template>
            </el-table-column>
            <el-table-column prop="showInList" label="列表" min-width="56">
              <template #default="{ row }"><el-checkbox v-model="row.showInList" :true-value="1" :false-value="0" /></template>
            </el-table-column>
            <el-table-column prop="listOrder" label="列序" min-width="56">
              <template #default="{ row }"><el-input-number v-model="row.listOrder" :min="0" size="small" controls-position="right" /></template>
            </el-table-column>
            <el-table-column prop="listWidth" label="列宽" min-width="64">
              <template #default="{ row }"><el-input v-model="row.listWidth" size="small" placeholder="120" /></template>
            </el-table-column>
            <el-table-column label="操作" min-width="64" align="center">
              <template #default="{ $index }">
                <div class="table-actions-cell">
                  <el-button link type="danger" size="small" @click="formFields.splice($index, 1)">删</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formConfigFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFormConfig">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="menuFormVisible" :title="menuForm.id ? '编辑菜单' : '新增菜单'" width="480px">
      <el-form :model="menuForm" label-width="90px">
        <el-form-item label="父菜单"><el-input :value="menuForm.parentId === 0 || !menuForm.parentId ? '根菜单' : (menuManageTreeFlat.find(m => m.id === menuForm.parentId)?.name || menuForm.parentId)" disabled /></el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="menuForm.name" placeholder="菜单名称（与左侧系统菜单显示一致，修改后保存即联动更新）" />
        </el-form-item>
        <el-form-item label="路由路径"><el-input v-model="menuForm.path" placeholder="如 /project 或 /model/project" /></el-form-item>
        <el-form-item label="图标">
          <el-select v-model="menuForm.icon" placeholder="图标" clearable style="width:100%">
            <el-option label="List" value="List" /><el-option label="Folder" value="Folder" /><el-option label="Money" value="Money" /><el-option label="Warning" value="Warning" /><el-option label="Edit" value="Edit" /><el-option label="Flag" value="Flag" /><el-option label="Setting" value="Setting" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="menuForm.sortOrder" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="menuForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="tabFormVisible" :title="tabForm.id ? '编辑页签' : '新增页签'" width="480px">
      <el-form :model="tabForm" label-width="90px">
        <el-form-item label="业务类型" required><el-input v-model="tabForm.bizType" placeholder="如 project" /></el-form-item>
        <el-form-item label="页签编码" required><el-input v-model="tabForm.tabCode" placeholder="如 cost" /></el-form-item>
        <el-form-item label="页签名称" required><el-input v-model="tabForm.tabName" placeholder="页签名称" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="tabForm.tabPath" placeholder="如 /project/:id/cost" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="tabForm.sortOrder" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="是否启用">
          <el-radio-group v-model="tabForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tabFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTab">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { dictTypeList, dictItemList, dictTypeSave, dictTypeDelete, dictItemSave, dictItemDelete } from '@/api/dict'
import { approvalFlowList, approvalFlowGet, approvalFlowNodes, approvalFlowSave, approvalFlowDelete, formConfigList, formConfigGet, formConfigFields, formConfigSave, formConfigDelete, tabConfigList, tabConfigSave, tabConfigDelete } from '@/api/config'
import { userPage, userSave, userUpdate, userDelete, getUserRoleIds, userAssignRoles } from '@/api/user'
import { roleList, roleGet, roleSave, roleUpdate, roleDelete, roleMenuIds, roleAssignMenus } from '@/api/role'
import { menuTree, menuSave, menuDelete } from '@/api/menu'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('user')

const userKeyword = ref('')
const userPageNum = ref(1)
const userSize = ref(20)
const userTotal = ref(0)
const users = ref([])
const userLoading = ref(false)
const userFormVisible = ref(false)
const userForm = reactive({ id: null, username: '', password: '', realName: '', email: '', phone: '', status: 1 })
const userRolesVisible = ref(false)
const currentUserForRoles = ref(null)
const userRoleIds = ref([])

const roles = ref([])
const roleFormVisible = ref(false)
const roleForm = reactive({ id: null, code: '', name: '', status: 1 })
const roleMenusVisible = ref(false)
const currentRoleForMenus = ref(null)
const menuTreeData = ref([])
const menuTreeRef = ref(null)
const dictTypes = ref([])
const dictTypeFormVisible = ref(false)
const dictTypeForm = reactive({ id: null, code: '', name: '' })

const currentDictType = ref(null)
const dictItems = ref([])
const dictItemDialogVisible = ref(false)
const dictItemFormVisible = ref(false)
const dictItemForm = reactive({ id: null, typeCode: '', itemKey: '', itemValue: '', sortOrder: 0 })

const flows = ref([])
const flowFormVisible = ref(false)
const flowForm = reactive({ id: null, code: '', name: '', bizType: '', status: 1 })
const flowNodes = ref([])

const formConfigs = ref([])
const formConfigFormVisible = ref(false)
const formConfigForm = reactive({ id: null, formCode: '', formName: '', bizType: '', apiListPath: '', apiSavePath: '', status: 1 })
const formFields = ref([])

const menuManageTree = ref([])
const menuManageTreeFlat = ref([])
const menuFormVisible = ref(false)
const menuForm = reactive({ id: null, parentId: 0, name: '', path: '', icon: '', sortOrder: 0, status: 1 })
const selectedMenuId = ref(null)
const selectedMenuNode = ref(null)
const menuManageTreeRef = ref(null)

const tabs = ref([])
const tabFormVisible = ref(false)
const tabForm = reactive({ id: null, bizType: '', tabCode: '', tabName: '', tabPath: '', sortOrder: 0, status: 1 })

async function loadDictTypes() {
  dictTypes.value = await dictTypeList()
}

function openDictTypeForm(row) {
  if (row) {
    dictTypeForm.id = row.id
    dictTypeForm.code = row.code || ''
    dictTypeForm.name = row.name || ''
  } else {
    dictTypeForm.id = null
    dictTypeForm.code = ''
    dictTypeForm.name = ''
  }
  dictTypeFormVisible.value = true
}
function deleteDictType(row) {
  ElMessageBox.confirm('确定删除该字典类型？其下字典项将一并删除。', '提示', { type: 'warning' }).then(() => {
    dictTypeDelete(row.id).then(() => { ElMessage.success('已删除'); loadDictTypes() })
  }).catch(() => {})
}

function onDictTypeCommand(cmd, row) {
  if (cmd === 'edit') openDictTypeForm(row)
  else if (cmd === 'items') openDictItemList(row)
  else if (cmd === 'del') deleteDictType(row)
}

async function saveDictType() {
  await dictTypeSave(dictTypeForm)
  ElMessage.success('保存成功')
  dictTypeFormVisible.value = false
  loadDictTypes()
}

async function openDictItemList(row) {
  currentDictType.value = row
  dictItems.value = await dictItemList(row.code)
  dictItemDialogVisible.value = true
}

function openDictItemForm(row) {
  if (row) {
    dictItemForm.id = row.id
    dictItemForm.typeCode = currentDictType.value.code
    dictItemForm.itemKey = row.itemKey
    dictItemForm.itemValue = row.itemValue
    dictItemForm.sortOrder = row.sortOrder ?? 0
  } else {
    dictItemForm.id = null
    dictItemForm.typeCode = currentDictType.value?.code || ''
    dictItemForm.itemKey = ''
    dictItemForm.itemValue = ''
    dictItemForm.sortOrder = 0
  }
  dictItemFormVisible.value = true
}

async function saveDictItem() {
  dictItemForm.typeCode = currentDictType.value.code
  await dictItemSave(dictItemForm)
  ElMessage.success('保存成功')
  dictItemFormVisible.value = false
  dictItems.value = await dictItemList(currentDictType.value.code)
}

async function deleteDictItem(id) {
  await dictItemDelete(id)
  ElMessage.success('已删除')
  dictItems.value = await dictItemList(currentDictType.value.code)
}

async function loadFlows() {
  flows.value = await approvalFlowList()
}

function addFlowNode() {
  flowNodes.value.push({ nodeName: '', approverType: 'ROLE', approverId: '' })
}

async function openFlowForm(row) {
  if (row) {
    flowForm.id = row.id
    flowForm.code = row.code || ''
    flowForm.name = row.name || ''
    flowForm.bizType = row.bizType || ''
    flowForm.status = row.status ?? 1
    flowNodes.value = (await approvalFlowNodes(row.id)) || []
    flowNodes.value = flowNodes.value.map(n => ({ id: n.id, nodeName: n.nodeName, approverType: n.approverType || 'ROLE', approverId: n.approverId || '' }))
  } else {
    flowForm.id = null
    flowForm.code = ''
    flowForm.name = ''
    flowForm.bizType = ''
    flowForm.status = 1
    flowNodes.value = []
  }
  flowFormVisible.value = true
}

async function saveFlow() {
  if (!flowForm.code?.trim() || !flowForm.name?.trim()) { ElMessage.warning('请填写流程编码和名称'); return }
  await approvalFlowSave({ id: flowForm.id, code: flowForm.code, name: flowForm.name, bizType: flowForm.bizType, status: flowForm.status, nodes: flowNodes.value })
  ElMessage.success('保存成功')
  flowFormVisible.value = false
  loadFlows()
}

function deleteFlow(row) {
  ElMessageBox.confirm('确定删除该审批流？', '提示', { type: 'warning' }).then(() => {
    approvalFlowDelete(row.id).then(() => { ElMessage.success('已删除'); loadFlows() })
  }).catch(() => {})
}

async function loadFormConfigs() {
  formConfigs.value = await formConfigList()
}

function addFormField() {
  formFields.value.push({ fieldName: '', fieldLabel: '', fieldType: 'text', required: 0, sortOrder: formFields.value.length, showInList: 0, listOrder: 0, listWidth: '', extraJson: '' })
}

async function openFormConfigForm(row) {
  if (row) {
    formConfigForm.id = row.id
    formConfigForm.formCode = row.formCode || ''
    formConfigForm.formName = row.formName || ''
    formConfigForm.bizType = row.bizType || ''
    formConfigForm.apiListPath = row.apiListPath || ''
    formConfigForm.apiSavePath = row.apiSavePath || ''
    formConfigForm.status = row.status ?? 1
    formFields.value = (await formConfigFields(row.id)) || []
    formFields.value = formFields.value.map(f => ({
      fieldName: f.fieldName, fieldLabel: f.fieldLabel, fieldType: f.fieldType || 'text', required: f.required ?? 0, sortOrder: f.sortOrder ?? 0,
      showInList: f.showInList ?? 0, listOrder: f.listOrder ?? 0, listWidth: f.listWidth || '', extraJson: f.extraJson || ''
    }))
  } else {
    formConfigForm.id = null
    formConfigForm.formCode = ''
    formConfigForm.formName = ''
    formConfigForm.bizType = ''
    formConfigForm.apiListPath = ''
    formConfigForm.apiSavePath = ''
    formConfigForm.status = 1
    formFields.value = []
  }
  formConfigFormVisible.value = true
}

async function saveFormConfig() {
  if (!formConfigForm.formCode?.trim() || !formConfigForm.formName?.trim()) { ElMessage.warning('请填写表单编码和名称'); return }
  await formConfigSave({
    id: formConfigForm.id, formCode: formConfigForm.formCode, formName: formConfigForm.formName, bizType: formConfigForm.bizType,
    apiListPath: formConfigForm.apiListPath || null, apiSavePath: formConfigForm.apiSavePath || null, status: formConfigForm.status,
    fields: formFields.value
  })
  ElMessage.success('保存成功')
  formConfigFormVisible.value = false
  loadFormConfigs()
}

function deleteFormConfig(row) {
  ElMessageBox.confirm('确定删除该表单配置？', '提示', { type: 'warning' }).then(() => {
    formConfigDelete(row.id).then(() => { ElMessage.success('已删除'); loadFormConfigs() })
  }).catch(() => {})
}

function flattenMenuTree(items, out) {
  for (const m of items || []) {
    out.push(m)
    if (m.children?.length) flattenMenuTree(m.children, out)
  }
}
async function loadMenuTree() {
  menuManageTree.value = await menuTree(true)
  const flat = []
  flattenMenuTree(menuManageTree.value, flat)
  menuManageTreeFlat.value = flat
}
function onMenuNodeClick(data) {
  selectedMenuId.value = data.id
  selectedMenuNode.value = data
}
function getMenuNodeLabel(data) {
  if (menuFormVisible.value && data.id === selectedMenuId.value) return menuForm.name || data.name || ''
  return data.name || ''
}
function openMenuForm(node, parentIdForNew) {
  if (node) {
    menuForm.id = node.id
    menuForm.parentId = node.parentId ?? 0
    menuForm.name = node.name || ''
    menuForm.path = node.path || ''
    menuForm.icon = node.icon || ''
    menuForm.sortOrder = node.sortOrder ?? 0
    menuForm.status = node.status ?? 1
  } else {
    menuForm.id = null
    menuForm.parentId = parentIdForNew ?? 0
    menuForm.name = ''
    menuForm.path = ''
    menuForm.icon = ''
    menuForm.sortOrder = 0
    menuForm.status = 1
  }
  menuFormVisible.value = true
}
async function saveMenu() {
  if (!menuForm.name?.trim()) { ElMessage.warning('请填写菜单名称'); return }
  await menuSave({ ...menuForm, parentId: menuForm.parentId || 0, type: 1 })
  ElMessage.success('保存成功')
  menuFormVisible.value = false
  await loadMenuTree()
  await userStore.fetchInfo()
}
async function deleteMenuConfirm() {
  if (!selectedMenuId.value) return
  try {
    await ElMessageBox.confirm('确定删除该菜单？子菜单将变为根菜单。', '提示', { type: 'warning' })
    await menuDelete(selectedMenuId.value)
    ElMessage.success('已删除')
    selectedMenuId.value = null
    selectedMenuNode.value = null
    await loadMenuTree()
    await userStore.fetchInfo()
  } catch (_) {}
}

async function loadTabs() {
  tabs.value = await tabConfigList()
}

function openTabForm(row) {
  if (row) {
    tabForm.id = row.id
    tabForm.bizType = row.bizType || ''
    tabForm.tabCode = row.tabCode || ''
    tabForm.tabName = row.tabName || ''
    tabForm.tabPath = row.tabPath || ''
    tabForm.sortOrder = row.sortOrder ?? 0
    tabForm.status = row.status ?? 1
  } else {
    tabForm.id = null
    tabForm.bizType = ''
    tabForm.tabCode = ''
    tabForm.tabName = ''
    tabForm.tabPath = ''
    tabForm.sortOrder = 0
    tabForm.status = 1
  }
  tabFormVisible.value = true
}

async function saveTab() {
  if (!tabForm.bizType?.trim() || !tabForm.tabCode?.trim() || !tabForm.tabName?.trim()) { ElMessage.warning('请填写业务类型、页签编码和名称'); return }
  await tabConfigSave(tabForm)
  ElMessage.success('保存成功')
  tabFormVisible.value = false
  loadTabs()
}

function deleteTab(row) {
  ElMessageBox.confirm('确定删除该页签配置？', '提示', { type: 'warning' }).then(() => {
    tabConfigDelete(row.id).then(() => { ElMessage.success('已删除'); loadTabs() })
  }).catch(() => {})
}

async function loadUsers() {
  userLoading.value = true
  try {
    const res = await userPage({ page: userPageNum.value, size: userSize.value, keyword: userKeyword.value || undefined })
    users.value = res.list || []
    userTotal.value = res.total || 0
  } finally {
    userLoading.value = false
  }
}

function openUserForm(row) {
  if (row) {
    userForm.id = row.id
    userForm.username = row.username
    userForm.password = ''
    userForm.realName = row.realName || ''
    userForm.email = row.email || ''
    userForm.phone = row.phone || ''
    userForm.status = row.status ?? 1
  } else {
    userForm.id = null
    userForm.username = ''
    userForm.password = '123456'
    userForm.realName = ''
    userForm.email = ''
    userForm.phone = ''
    userForm.status = 1
  }
  userFormVisible.value = true
}

async function saveUser() {
  if (!userForm.username?.trim()) { ElMessage.warning('请输入用户名'); return }
  if (!userForm.id && !userForm.password?.trim()) { ElMessage.warning('请输入密码'); return }
  if (userForm.id) await userUpdate(userForm)
  else await userSave(userForm)
  ElMessage.success('保存成功')
  userFormVisible.value = false
  loadUsers()
}

async function openUserRoles(row) {
  currentUserForRoles.value = row
  userRoleIds.value = await getUserRoleIds(row.id)
  userRolesVisible.value = true
}

async function saveUserRoles() {
  await userAssignRoles(currentUserForRoles.value.id, userRoleIds.value)
  ElMessage.success('保存成功')
  userRolesVisible.value = false
}

function deleteUser(row) {
  ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' }).then(() => {
    userDelete(row.id).then(() => { ElMessage.success('已删除'); loadUsers() })
  }).catch(() => {})
}

function onUserCommand(cmd, row) {
  if (cmd === 'edit') openUserForm(row)
  else if (cmd === 'roles') openUserRoles(row)
  else if (cmd === 'del') deleteUser(row)
}

async function loadRoles() {
  roles.value = await roleList()
}

function openRoleForm(row) {
  if (row) {
    roleForm.id = row.id
    roleForm.code = row.code
    roleForm.name = row.name
    roleForm.status = row.status ?? 1
  } else {
    roleForm.id = null
    roleForm.code = ''
    roleForm.name = ''
    roleForm.status = 1
  }
  roleFormVisible.value = true
}

async function saveRole() {
  if (!roleForm.code?.trim() || !roleForm.name?.trim()) { ElMessage.warning('请填写编码和名称'); return }
  if (roleForm.id) await roleUpdate(roleForm)
  else await roleSave(roleForm)
  ElMessage.success('保存成功')
  roleFormVisible.value = false
  loadRoles()
}

async function openRoleMenus(row) {
  currentRoleForMenus.value = row
  menuTreeData.value = await menuTree()
  const ids = await roleMenuIds(row.id)
  roleMenusVisible.value = true
  await nextTick()
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(ids || [])
  }
}

async function saveRoleMenus() {
  const half = menuTreeRef.value?.getHalfCheckedKeys() || []
  const full = menuTreeRef.value?.getCheckedKeys() || []
  await roleAssignMenus(currentRoleForMenus.value.id, [...half, ...full])
  ElMessage.success('保存成功')
  roleMenusVisible.value = false
}

function deleteRole(row) {
  ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' }).then(() => {
    roleDelete(row.id).then(() => { ElMessage.success('已删除'); loadRoles() })
  }).catch(() => {})
}

function onRoleCommand(cmd, row) {
  if (cmd === 'edit') openRoleForm(row)
  else if (cmd === 'menus') openRoleMenus(row)
  else if (cmd === 'del') deleteRole(row)
}

watch(activeTab, (name) => {
  if (name === 'user') loadUsers()
  if (name === 'role') loadRoles()
  if (name === 'approval') loadFlows()
  if (name === 'form') loadFormConfigs()
  if (name === 'menu') loadMenuTree()
  if (name === 'tab') loadTabs()
})

onMounted(() => {
  loadDictTypes()
  if (activeTab.value === 'user') loadUsers()
  if (activeTab.value === 'role') loadRoles()
})
</script>

<style scoped>
.tip { margin-top: 12px; color: #666; font-size: 12px; }
</style>
