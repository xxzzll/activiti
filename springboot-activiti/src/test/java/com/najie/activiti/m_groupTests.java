package com.najie.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityImpl;
import org.activiti.engine.impl.persistence.entity.UserEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.List;

/**
 * @author xixi
 * @Description： 测试    角色组
 * @create 2020/6/27
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class m_groupTests {

    private Logger logger = LoggerFactory.getLogger(m_groupTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/task/task.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/task/task.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("组任务") // 部署名称
                .addInputStream("task.bpmn", inputStreamBpmn)
                .addInputStream("task.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());

        /**
         * 添加角色和用户数据时报错：（未解决！！！）
         * org.activiti.engine.ActivitiOptimisticLockingException:
         *          org.activiti.engine.impl.persistence.entity.GroupEntityImpl@3a00b15d was updated by another transaction concurrently
         *
         */

        // 定义角色
        IdentityService identityService = processEngine.getIdentityService();
        GroupEntityImpl departmentManager = new GroupEntityImpl();
        departmentManager.setName("部门经理");
        GroupEntityImpl generalManager = new GroupEntityImpl();
        generalManager.setName("总经理");
        identityService.saveGroup(departmentManager);
        identityService.saveGroup(generalManager);

        // 定义人员
        UserEntityImpl userEntity1 = new UserEntityImpl();
        userEntity1.setLastName("张三");
        UserEntityImpl userEntity2 = new UserEntityImpl();
        userEntity2.setLastName("李四");
        UserEntityImpl userEntity3 = new UserEntityImpl();
        userEntity3.setLastName("王五");
        identityService.saveUser(userEntity1);
        identityService.saveUser(userEntity2);
        identityService.saveUser(userEntity3);

        // 维护角色与人员的对应关系
        identityService.createMembership(userEntity1.getId(), departmentManager.getId());
        identityService.createMembership(userEntity2.getId(), departmentManager.getId());
        identityService.createMembership(userEntity3.getId(), generalManager.getId());
    }

    // 2. 启动流程实例
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "task";
        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());
    }

    @Test
    public void deleteProcessDefinitions(){
        String deploymentId = "275001";
        processEngine.getRepositoryService()
                .deleteDeployment(deploymentId, true); // 级联删除,可以删除已经开启的流程处理
        System.out.println("删除成功！");

    }

    /*3. 查询当前人的个人任务*/
    @Test
    public void findMyPersonTaskList(){
        String assignee = "大F";
        List<Task> list = processEngine.getTaskService() // 与正在执行的任务管理有关的 Service
                .createTaskQuery() // 创建任务查询对象
                .taskAssignee(assignee) // 指定个人任务查询，指定办理人
                .list();

        list.forEach(task -> {
            logger.info("任务 id={}", task.getId());
            logger.info("任务名称 {}", task.getName());
            logger.info("任务创建时间 {}", task.getCreateTime());
            logger.info("任务办理人 {}", task.getAssignee());
            logger.info("流程实例 id={}", task.getProcessInstanceId());
            logger.info("执行对象 id={}", task.getExecutionId());
            logger.info("流程定义 id={}", task.getProcessDefinitionId());
            logger.info("###################################################");

        });
    }

    /*3. 查询当前人的个人任务*/
    @Test
    public void findGroupTaskList(){
        String candidateUser = "小A";
        List<Task> list = processEngine.getTaskService() // 与正在执行的任务管理有关的 Service
                .createTaskQuery() // 创建任务查询对象
                .taskCandidateUser(candidateUser)
                .list();

        list.forEach(task -> {
            logger.info("任务 id={}", task.getId());
            logger.info("任务名称 {}", task.getName());
            logger.info("任务创建时间 {}", task.getCreateTime());
            logger.info("任务办理人 {}", task.getAssignee());
            logger.info("流程实例 id={}", task.getProcessInstanceId());
            logger.info("执行对象 id={}", task.getExecutionId());
            logger.info("流程定义 id={}", task.getProcessDefinitionId());
            logger.info("###################################################");

        });
    }

    // 查询正在执行的任务分配人员
    /**
     * 查询结果： （候选人）
     * 282505   candidate   小A   null
     * 282505   candidate   小B   null
     * 282505   candidate   小C   null
     * 282505   candidate   小D   null
     */
    @Test
    public void findRuTaskAssignee(){
        String taskId = "282505";
        List<IdentityLink> identityLinksForTask = processEngine.getTaskService()
                .getIdentityLinksForTask(taskId);
        identityLinksForTask.forEach(identityLink -> {
            System.out.println(identityLink.getTaskId() + "   " + identityLink.getType() + "   " + identityLink.getUserId() + "   " + identityLink.getProcessInstanceId());
        });
    }

    // 查询历史任务分配人员
    @Test
    public void findHiTaskAssignee(){
        String taskId = "282505";
        String processInstanceId = "282501";

        List<HistoricIdentityLink> historicIdentityLinksForTask1 = processEngine.getHistoryService()
                .getHistoricIdentityLinksForTask(taskId);

        List<HistoricIdentityLink> historicIdentityLinksForTask2 = processEngine.getHistoryService()
                .getHistoricIdentityLinksForProcessInstance(processInstanceId);

        /**
         * 282505   candidate   小A   null
         * 282505   candidate   小B   null
         * 282505   candidate   小C   null
         * 282505   candidate   小D   null
         */
        historicIdentityLinksForTask1.forEach(historicIdentityLink -> {
            System.out.println(historicIdentityLink.getTaskId() + "   " + historicIdentityLink.getType() + "   " + historicIdentityLink.getUserId() + "   " + historicIdentityLink.getProcessInstanceId());
        });

        /**
         * null   participant   小B   282501
         * null   participant   小D   282501
         * null   participant   小A   282501
         * null   participant   小C   282501
         */
        historicIdentityLinksForTask2.forEach(historicIdentityLink -> {
            System.out.println(historicIdentityLink.getTaskId() + "   " + historicIdentityLink.getType() + "   " + historicIdentityLink.getUserId() + "   " + historicIdentityLink.getProcessInstanceId());
        });
    }

    // 拾取任务，将组任务分配给个人任务，指定任务办理人字段
    @Test
    public void claim(){
        String taskId = "282505";

        // 分配的个人任务（可以是组任务中的成员，也可以是非组任务中的成员）
        String userId = "小A";

        processEngine.getTaskService()
                .claim(taskId, userId);
        System.out.println("拾取任务成功！");
    }

    // 将个人任务回退到组任务中，前提：之前是组任务
    @Test
    public void setAssignee(){
        String taskId = "282505";

        processEngine.getTaskService()
                .setAssignee(taskId, null);
        System.out.println("回退成功！");
    }

    // 向组中添加成员(新来人)
    @Test
    public void addCandidateUser(){
        String taskId = "282505";
        // 新成员
        String userId = "大H";
        processEngine.getTaskService()
                .addCandidateUser(taskId, userId);
        System.out.println("添加成员成功！");
    }

    // 删除组中成员(人离职了)
    @Test
    public void deleteCandidateUser(){
        String taskId = "282505";
        // 旧成员
        String userId = "小B";
        processEngine.getTaskService()
                .deleteCandidateUser(taskId, userId);
        System.out.println("删除成员成功！");
    }

    /*4. 完成我的任务*/
    @Test
    public void completeMyPersonTask(){
        String taskId = "282505";
        processEngine.getTaskService()
                .complete(taskId);

        logger.info("完成我的任务，任务id={}", taskId);
    }
}
