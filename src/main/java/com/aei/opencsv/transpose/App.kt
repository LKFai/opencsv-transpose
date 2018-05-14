package com.aei.opencsv.transpose

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.opencsv.exceptions.CsvDataTypeMismatchException
import com.opencsv.exceptions.CsvRequiredFieldEmptyException
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.OutputStreamWriter


@SpringBootApplication
open class Application : CommandLineRunner, ApplicationContextAware {

    @Autowired
    private val transposeService: TransposeService? = null
    private var applicationContext: ApplicationContext? = null

    @Throws(Exception::class)
    override fun run(vararg strings: String) {
        if (strings.size != 1) {
            usage()
            (applicationContext as ConfigurableApplicationContext).close()
            System.exit(1)
        }
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(strings[0])
        transposeService!!.transpose(jsonNode)
        LOGGER.info("Transpose Completed")
    }

    private fun usage() {
        LOGGER.info("\n\n\t\tusage: java -jar opencsv-transpose <jsonBody>\n" + "\t\texample: java -jar opencsv-transpose \"{\\\"name\\\":\\\"Jon\\\", \\\"age\\\":\\\"30\\\", \\\"sex\\\":\\\"male\\\", \\\"mentor\\\": {\\\"name\\\": \\\"Mentor Chan\\\"} }\"\n")
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication(Application::class.java).run(*args)
        }
    }
}

data class ValueHolder (
        val key: String,
        val value: String
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ValueHolder?
        return Objects.equal(key, that!!.key) && Objects.equal(value, that.value)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(key, value)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("value", value)
                .toString()
    }
}

@Service
class TransposeService {

    @Throws(IOException::class, CsvDataTypeMismatchException::class, CsvRequiredFieldEmptyException::class)
    fun transpose(jsonNode: JsonNode) {

        val content = mutableListOf<ValueHolder>()
        populate(jsonNode, "", content)

        val strategy = ColumnPositionMappingStrategy<ValueHolder>()
        strategy.setType(ValueHolder::class.java)
        strategy.setColumnMapping("key", "value")
        val writer = OutputStreamWriter(System.out)
        val csvBuilder = StatefulBeanToCsvBuilder<ValueHolder>(writer)
        val beanWriter = csvBuilder.withSeparator(';')
                .withLineEnd(";" + System.lineSeparator())
                .withMappingStrategy(strategy)
                .build()
        for (valueHolder in content) {
            beanWriter.write(valueHolder)
        }
        writer.close()
    }

    private fun populate(jsonNode: JsonNode, prefix: String, content: MutableList<ValueHolder>) {
        val fieldNames = jsonNode.fieldNames()
        while (fieldNames.hasNext()) {
            val name = fieldNames.next()
            val node = jsonNode.get(name)
            if (node.isValueNode) {
                val valueHolder = ValueHolder(key = prefix + name, value = node.textValue())
                content.add(valueHolder)
            } else if (node.isContainerNode) {
                populate(node, "$name.", content)
            }
        }
    }
}