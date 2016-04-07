package com.fang.example.spring.di.util;

import java.util.List;
import java.util.Map;

/**
 * Created by andy on 4/6/16.
 * Add bean entry
 */
public class BeanEntry extends Entry {
    /**
     * id is bean id in the spring xml, like this <bean id ="$id"
     */
    private String _id;
    /**
     * className is the bean class
     */
    private String _className;
    /**
     * all class this bean depend on
     */
    private List<String> _relClasses;

    public BeanEntry(@NotNull String id, @NotNull String className, @NotNull List<String> relClasses) {
        _id = id;
        _className = className;
        _relClasses = relClasses;
    }
    @Override
    public String write(Map<String, Entry> map) throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<bean id=\"%s\" class=\"%s\"> \n", _id, _className));
        if (_relClasses == null) {
            sb.append("</bean> \n");
            return sb.toString();
        }

        for (String cname : _relClasses) {
            Entry entry = map.get(cname);
            if (!(entry instanceof BeanEntry))
                continue;
            if (entry == null)
                throw new Exception("can not find relation bean id, id=" + cname);
            sb.append( String.format("<constructor-arg ref=\"%s\" /> \n", ((BeanEntry)entry)._id) + "\n");

        }
        sb.append("</bean> \n");
        return sb.toString();
    }
}
