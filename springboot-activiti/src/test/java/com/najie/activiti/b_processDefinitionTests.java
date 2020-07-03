package com.najie.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author xixi
 * @Description： 测试 管理流程定义
 * @create 2020/6/26
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class b_processDefinitionTests {

    private Logger logger = LoggerFactory.getLogger(a_helloWorldTests.class);

    @Autowired
    private ProcessEngine processEngine;


    //**************************************流程定义部署*********************************************//
    /**
     *  1. 从 classpath 下加载 xx.bpmn 和 xx.png 文件部署
     *  2. 从 xx.zip 包获取资源部署
     */
    @Test
    public void deploymentProcessDefinition_zip(){
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("processes/myprocess.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("zip包部署流程定义") // 部署名称
                .addZipInputStream(zipInputStream)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    //**************************************查询流程定义*********************************************//
    @Test
    public void findProcessDefinitions(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()
                .createProcessDefinitionQuery() // 创建"流程定义"查询
                /* where 后的查询条件*/
//                .deploymentId(deploymentId) // 部署ID
//                .processDefinitionKey(key) // 流程 key
//                .processDefinitionNameLike(name) // 模糊查询 流程定义的名称

                /* 排序*/
                .orderByProcessDefinitionKey().asc() // 按照 key 升序排序
//                .count() // 计数
//                .listPage(page, size) // 分页查询

                /* 结果封装 */
                .list();// 查询后封装的结果集
//                .singleResult() // 确认返回单个对象


        list.forEach(pd->{
            System.out.println("流程定义id=" + pd.getId()); // id = {流程定义key}:{流程版本}:{随机生成的数}
            System.out.println("流程定义name=" + pd.getName()); // bpmn 文件中的 name 属性的值
            System.out.println("流程定义key=" + pd.getKey()); // bpmn 文件中的 id 属性的值
            System.out.println("流程定义version=" + pd.getVersion()); // 当流程定义的key相同，版本升级
            System.out.println("流程定义 bpmn 资源文件=" + pd.getResourceName());
            System.out.println("流程定义 png 资源文件=" + pd.getDiagramResourceName());
            System.out.println("部署流程id=" + pd.getDeploymentId());
            System.out.println("#################################################");

        });

    }

    //**************************************删除流程定义*********************************************//
    @Test
    public void deleteProcessDefinitions(){
        String deploymentId = "82508";
        processEngine.getRepositoryService()
                .deleteDeployment(deploymentId, true); // 级联删除,可以删除已经开启的流程处理
//                .deleteDeployment(deploymentId); // 根据部署ID删除流程定义，不带级联，且不能删除已经开启的流程处理（报错）
        System.out.println("删除成功！");

    }

    //**************************************获取流程定义文件资源（流程图片）*********************************************//
    @Test
    public void getPic() throws IOException {
        String deploymentId = "82508";

        // 根据部署ID 获取资源信息
        List<String> deploymentResourceNames = processEngine.getRepositoryService()
                .getDeploymentResourceNames(deploymentId);

        String resourceName = "";
        for (String deploymentResourceName : deploymentResourceNames) {
            if (deploymentResourceName.indexOf(".png") > 0) {
                resourceName = deploymentResourceName;
                break;
            }
        }

        // 以流的形式获取资源文件
        InputStream in = processEngine.getRepositoryService()
                .getResourceAsStream(deploymentId, resourceName);

        // 将资源文件输出的磁盘上
        File file = new File("/home/xixi/Downloads/" + resourceName);
        FileUtils.copyInputStreamToFile(in, file);
    }

    //**************************************获取最新版本的程定义*********************************************//
    @Test
    public void findLatestVersionProcessDefinitions(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                // 按照流程定义的版本升序排序
                .orderByProcessDefinitionVersion().asc()
                .list();


        /**
         * 定义一个 map
         * key = 流程定义的版本
         * value = 流程定义
         * 特点：相同 key 的流程定义，后一次的值覆盖前一次的值
         */
        Map<String, ProcessDefinition> processDefinitions = new LinkedHashMap<>();
        list.forEach(pd->{
            processDefinitions.put(pd.getKey(), pd);
        });

        List<ProcessDefinition> definitions = new ArrayList<>(processDefinitions.values());

        definitions.forEach(pd->{
            System.out.println("流程定义id=" + pd.getId()); // id = {流程定义key}:{流程版本}:{随机生成的数}
            System.out.println("流程定义name=" + pd.getName()); // bpmn 文件中的 name 属性的值
            System.out.println("流程定义key=" + pd.getKey()); // bpmn 文件中的 id 属性的值
            System.out.println("流程定义version=" + pd.getVersion()); // 当流程定义的key相同，版本升级
            System.out.println("流程定义 bpmn 资源文件=" + pd.getResourceName());
            System.out.println("流程定义 png 资源文件=" + pd.getDiagramResourceName());
            System.out.println("部署流程id=" + pd.getDeploymentId());
            System.out.println("#################################################");
        });
    }

    //**************************************删除流程定义(删除 key 相同的所有版本的流程定义)*********************************************//
    @Test
    public void deleteProcessDefinitionsByKey(){
        String processDefinitionKey = "myProcess";

        // 获取 key 对应的所有流程定义
        List<ProcessDefinition> list = processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();

        // 在根据没有流程定义中的部署 ID 级联删除没有流程定义数据
        list.forEach(pd->{
            String deploymentId = pd.getDeploymentId();

            processEngine.getRepositoryService()
                    .deleteDeployment(deploymentId, true); // 级联删除
        });
    }
}
