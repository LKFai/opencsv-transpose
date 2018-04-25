import com.google.common.collect.Lists;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class Playground {

    public class Person {
        private String name;
        private Integer age;
        private String sex;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }
    }

    public class ValueHolder {
        private String key;
        private String value;

        public ValueHolder(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void test() throws Exception {
        Person p1 = new Person();
        p1.setAge(30);
        p1.setName("Jon");
        p1.setSex("male");

        List<ValueHolder> content = Lists.newArrayList();
        for (Field field : p1.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!Modifier.isPrivate(field.getModifiers())) {
                continue;
            }
            content.add(new ValueHolder(field.getName(), String.valueOf(field.get(p1))));
        }

        ColumnPositionMappingStrategy<ValueHolder> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(ValueHolder.class);
        strategy.setColumnMapping("key", "value");
        FileWriter writer = new FileWriter("./target/settings.csv");
        StatefulBeanToCsvBuilder<ValueHolder> csvBuilder = new StatefulBeanToCsvBuilder<>(writer);
        StatefulBeanToCsv<ValueHolder> beanWriter = csvBuilder.withSeparator(';').withMappingStrategy(strategy).build();
        beanWriter.write(content);
        writer.close();
    }
}
