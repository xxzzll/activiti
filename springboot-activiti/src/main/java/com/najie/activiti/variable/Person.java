package com.najie.activiti.variable;

import java.io.Serializable;

/**
 * @author xixi
 * @Description： 流程变量设置对象实体
 * @create 2020/6/26
 * @since 1.0.0
 */
public class Person implements Serializable {

    private static final long serialVersionUID = -6178525619293275720L;

    private int id;
    private String name;

    private String education;

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Person() {
    }

    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
